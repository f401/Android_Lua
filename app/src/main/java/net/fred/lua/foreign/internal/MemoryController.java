package net.fred.lua.foreign.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.Action;
import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.common.Flag;
import net.fred.lua.common.Logger;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.NativeMethodException;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class MemoryController implements Closeable {
    private Flag freed;

    /**
     * @{value #children} Contains objects that need to be released together when this object is released.
     */
    private List<AutoCloseable> children;

    @Nullable
    private MemoryController parent;

    public MemoryController() {
        this.freed = new Flag(false);
    }
    
    @NonNull
    public final Flag getFreed() {
        return freed;
    }

    public final void setFreed(@NonNull Flag freed) {
        this.freed = freed;
    }

    @Nullable
    public MemoryController getParent() {
        return parent;
    }

    @Override
    public final void close() throws NativeMethodException {
        if (!this.freed.getFlag()) {
            onFree();
            freeChildren();
            if (parent != null) {
                parent.removeChild(this);
            }
            freed.setFlag(true);
        } else {
            Logger.e("Pointer freed twice");
            throw new RuntimeException("Pointer freed twice");
        }
    }

    public void addChild(@Nullable AutoCloseable segment) {
        ArgumentsChecker.check(!this.getFreed().getFlag(), "Father has been released.");
        if (segment != this && segment != null) {
            if (children == null) {
                children = new ArrayList<>(2);
            }
            if (segment instanceof MemoryController) {
                MemoryController child = (MemoryController) segment;
                ArgumentsChecker.check(!checkIsParent(child), "The required registered son is the father of the current object.");
                child.attachParent(this);
            }
            children.add(segment);
        }
    }

    public final void attachParent(@NonNull MemoryController parent) {
        ArgumentsChecker.check(this.parent == null,
                "The current object already has a father. If you want to replace it, please call 'detachParent' first`.");
        ArgumentsChecker.check(this != parent,
                "Cannot set oneself as Parent.");
        this.parent = parent;
        onAttachParent(parent);
    }

    public final void detachParent() {
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
    public final void removeChild(@NonNull MemoryController child) {
        if (children.remove(child)) {
            child.detachParent();
        }
    }

    public final void freeChildren() {
        if (children != null && children.size() != 0) {
            //During the deletion process, the subclass will call the remove method.
            //This can cause data modification during traversal, resulting in exceptions being thrown.
            List<AutoCloseable> dest = new ArrayList<>(children.size() + 1);
            dest.addAll(children);
            ThrowableUtils.closeAll(dest, new Action<Void, AutoCloseable>() {
                @Override
                public Void action(AutoCloseable param) {
                    if (param instanceof MemoryController) {
                        ((MemoryController) param).detachParent();
                    }
                    return null;
                }
            });
            children = null;
        }
    }

    public boolean hasChild() {
        return children != null && children.size() != 0;
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

    /**
     * Called by @{see #attachParent}
     * Called when setting the parent.
     *
     * @param parent The set parent.
     */
    protected void onAttachParent(@NonNull MemoryController parent) {
    }
}
