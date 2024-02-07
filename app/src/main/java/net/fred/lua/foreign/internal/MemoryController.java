package net.fred.lua.foreign.internal;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.SharedResource;
import net.fred.lua.foreign.child.IChildPolicy;
import net.fred.lua.foreign.child.SimpleChildHolder;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryController implements Closeable {
    private static final String TAG = "MemoryController";
    private final AtomicBoolean closed;

    /**
     * Contains objects that need to be released together when this object is released.
     */
    private IChildPolicy childPolicy;
    @Nullable
    private MemoryController parent;

    public MemoryController() {
        this.closed = new AtomicBoolean(false);
        this.childPolicy = new SimpleChildHolder(this);
    }

    /*** Release this object. After releasing it, you cannot touch it at all. */
    @Override
    public final void close() throws NativeMethodException {
        close(false);
    }

    private void close(boolean finalized) throws NativeMethodException {
        if (askParentToAllowChildRelease() &&
                this.closed.compareAndSet(false, true)) {
            onFree(finalized);
            childPolicy.closeAllChild();
            if (hasParent()) {
                parent.removeChild(this);
                parent = null;
            }
        } else {
            Log.e(TAG, "Pointer freed twice");
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
    public final boolean hasChild() {
        return childPolicy.hasChild();
    }

    public final boolean hasParent() {
        return parent != null;
    }

    public final void addChild(@Nullable AutoCloseable segment) {
        if (segment == null) {
            Log.w("MemoryController", "Try to add child with null value. Ignoring.");
        }
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
        onDetachParent();
        this.parent = null;
    }

    protected void onDetachParent() {
    }

    public final boolean checkIsParent(@NonNull MemoryController needle) {
        return this == needle || (parent != null && parent.checkIsParent(needle));
    }

    /**
     * Called from child.
     * Determine whether the parent allows child release.
     *
     * @return Returning true indicates permission, while false does not allow.
     * @see SharedResource
     */
    protected boolean askParentToAllowChildRelease() {
        return true;
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

    /**
     * Called when releasing the current object (including garbage collector collecting the current object).
     * <p>
     * Rewriting this method normally is to free up resources.
     */
    protected void onFree(boolean finalized) throws NativeMethodException {
        if (finalized) {
            Log.e(TAG, "Memory hasn't released yet!, Object " + this);
        }
    }

    public IChildPolicy getChildPolicy() {
        return childPolicy;
    }

    public void setChildPolicy(IChildPolicy policy) {
        this.childPolicy = policy;
    }

    public final MemoryController getParent() {
        return parent;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!closed.get()) {
            close(true);
        }
    }
}
