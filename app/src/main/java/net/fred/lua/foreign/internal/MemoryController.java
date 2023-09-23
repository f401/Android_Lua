package net.fred.lua.foreign.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.common.Logger;
import net.fred.lua.common.cleaner.Cleaner;
import net.fred.lua.common.functional.Consumer;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.NativeMethodException;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryController implements Closeable {

    public static final MemoryController.PointerHolder EMPTY_HOLDER = new MemoryController.PointerHolder() {
        @Override
        public void onFree() throws NativeMethodException {
        }
    };

    private final AtomicBoolean closed;

    /**
     * Contains objects that need to be released together when this object is released.
     */
    private List<AutoCloseable> children;
    @Nullable
    private MemoryController parent;
    private final Cleaner.ReferenceCleanable cleaner;
    private final PointerHolderCleanerImpl holder;

    /**
     * Use when no custom release function is required.
     */
    public MemoryController() {
        this(EMPTY_HOLDER);
    }

    public MemoryController(PointerHolder holder) {
        this.closed = new AtomicBoolean(false);
        this.holder = new PointerHolderCleanerImpl(holder);
        this.cleaner = Cleaner.createPhantom(this, this.holder);
    }

    @Override
    public final void close() throws NativeMethodException {
        if (this.closed.compareAndSet(false, true)) {
            cleaner.performCleanup();
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

    public PointerHolder getPointerHolder() {
        return this.holder.target;
    }

    public final AutoCloseable childAt(int idx) {
        return children.get(idx);
    }

    public boolean hasChild() {
        return children != null && children.size() != 0;
    }

    public void addChild(@Nullable AutoCloseable segment) {
        ArgumentsChecker.check(!this.closed.get(), "Father has been released.");
        if (segment != this && segment != null) {
            synchronized (this) {
                if (children == null) {
                    children = new ArrayList<>(2);
                }

                if (segment instanceof MemoryController) {
                    MemoryController child = (MemoryController) segment;
                    ArgumentsChecker.check(!checkIsParent(child), "The required registered son is the father of the current object.");
                    child.attachParent(this);
                    children.add(segment);
                }
            }
        }
    }

    public final void attachParent(@NonNull MemoryController parent) {
        ArgumentsChecker.check(this.parent == null,
                "The current object already has a father. If you want to replace it, please call 'detachParent' first`.");
        ArgumentsChecker.check(this != parent,
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
     * Called by @{see #close}.
     * <p>
     * This function is called when the pointer is released.
     * Can ensure that the flag is only called once without modification.
     * <p>
     * If you don't want to call the free function to release,
     * Rewrite and implement your own release function.
     * <p>
     * Very few cases require rewriting this method, and in most cases, only @{link MemoryController#addChild} needs to be used.
     * And @{link BasicMemoryController}.
     * <p>
     * However, sometimes, children are used as management methods for memory usage rather than being
     * 'released together', and in these cases, the method also needs to be rewritten.
     * For example, @{link net.fred.lua.foreign.ffi.FunctionCaller} and @{link net.fred.lua.foreign.ffi.FunctionDescriber}
     *
     */
    protected void onFree() throws NativeMethodException {
    }

    protected interface PointerHolder {
        void onFree() throws NativeMethodException;
    }

    /**
     * Packaging for Cleanable.
     */
    private static class PointerHolderCleanerImpl implements Cleaner.Cleanable {

        private final PointerHolder target;

        PointerHolderCleanerImpl(PointerHolder target) {
            this.target = target;
        }

        @Override
        public void clean() throws NativeMethodException {
            target.onFree();
        }
    }
}
