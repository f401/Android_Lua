package net.fred.lua.foreign.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.fred.lua.common.functional.Consumer;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.io.Logger;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryController implements Closeable {

    private final AtomicBoolean closed;

    /**
     * Contains objects that need to be released together when this object is released.
     */
    private List<AutoCloseable> children;
    @Nullable
    private MemoryController parent;

    public MemoryController() {
        this.closed = new AtomicBoolean(false);
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
        return children.get(idx);
    }

    /**
     * Returns whether the current object has children.
     */
    public boolean hasChild() {
        return children != null && children.size() != 0;
    }

    public void addChild(@Nullable AutoCloseable segment) {
        Preconditions.checkState(!this.closed.get(), "Father has been released.");
        if (segment != this && segment != null) {
            synchronized (this) {
                if (children == null) {
                    children = new ArrayList<>(2);
                }

                if (segment instanceof MemoryController) {
                    MemoryController child = (MemoryController) segment;
                    Preconditions.checkState(!checkIsParent(child), "The required registered son is the father of the current object.");
                    child.attachParent(this);
                    children.add(segment);
                }
            }
        }
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
     * Remove @{code child} from the current object.
     * If successful, the @{code detachParent} of @{code child} will be automatically called.
     *
     * @param child The object you want to delete.
     */
    public synchronized final void removeChild(@NonNull MemoryController child) {
        if (children.remove(child)) {
            child.detachParent();
        }
    }

    public final void freeChildren() {
        if (children != null && children.size() != 0) {
            // During the deletion process, the subclass will call the remove method.
            // This can cause data modification during traversal, resulting in exceptions being thrown.
            synchronized (this) {
                List<AutoCloseable> dest = new ArrayList<>(children.size() + 1);
                dest.addAll(children);
                ThrowableUtils.closeAll(dest, new Consumer<AutoCloseable>() {
                    @Override
                    public void accept(AutoCloseable param) {
                        if (param instanceof MemoryController) {
                            ((MemoryController) param).detachParent();
                        }
                    }
                });
                children = null;
            }
        }
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
