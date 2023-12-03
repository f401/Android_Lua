package net.fred.lua.foreign.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.child.IChildPolicy;
import net.fred.lua.foreign.child.SimpleChildHolder;
import net.fred.lua.io.Logger;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryController implements Closeable {

    private final AtomicBoolean closed;

    /**
     * Contains objects that need to be released together when this object is released.
     */
    private final IChildPolicy childPolicy;
    @Nullable
    private MemoryController parent;

    public MemoryController() {
        this.closed = new AtomicBoolean(false);
        this.childPolicy = new SimpleChildHolder(this);
    }

    /*** Release this object. After releasing it, you cannot touch it at all. */
    @Override
    public final void close() throws NativeMethodException {
        if (this.closed.compareAndSet(false, true)) {
            onFree();
            freeChildren();
            if (parent != null) {
                parent.removeChild(this);
                parent = null;
            }
        } else {
            Logger.e("Pointer freed twice");
        }
    }

    public final boolean isClosed() {
        return this.closed.get();
    }

    public final AutoCloseable childAt(int idx) {
        return childPolicy.childAt(idx);
    }

    /**
     * Returns whether the current object has children.
     */
    public boolean hasChild() {
        return childPolicy.hasChild();
    }

    public void addChild(@Nullable AutoCloseable segment) {
        childPolicy.addChild(segment);
    }

    public final void attachParent(@NonNull MemoryController parent) {
        Preconditions.checkState(this.parent == null,
                "The current object already has a father. If you want to replace it, please call 'detachParent' first`.");
        Preconditions.checkState(this != parent,
                "Cannot set oneself as Parent.");
        synchronized (this) {
            this.parent = parent;
        }
    }

    public synchronized final void detachParent() {
        this.parent = null;
    }

    public final boolean checkIsParent(@NonNull MemoryController needle) {
        return this == needle || (parent != null && parent.checkIsParent(needle));
    }


    /**
     * Remove {@code child} from the current object.
     * If successful, the {@code detachParent} of @{code child} will be automatically called.
     *
     * @param child The object you want to delete.
     * @see IChildPolicy#removeChild(AutoCloseable)
     */
    public final void removeChild(@NonNull MemoryController child) {
        childPolicy.removeChild(child);
    }

    public final void freeChildren() {
       childPolicy.closeAllChild();
    }

    /**
     * Called when releasing the current object (including garbage collector collecting the current object).
     * <p>
     * Rewriting this method normally is to free up resources.
     */
    protected void onFree() throws NativeMethodException {
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!closed.get()) {
            Logger.e("Memory hasn't released yet!, Object " + this);
            close();
        }
    }
}
