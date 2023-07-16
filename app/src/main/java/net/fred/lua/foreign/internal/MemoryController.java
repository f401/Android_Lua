package net.fred.lua.foreign.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.common.Flag;
import net.fred.lua.common.Logger;
import net.fred.lua.common.utils.ThrowableUtils;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class MemoryController implements Closeable {
    private Flag freed;

    /**
     * Contains objects that need to be released together when this object is released.
     */
    private List<AutoCloseable> children;

    @Nullable
    private MemoryController parent;

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
    public final void close() {
        if (!this.freed.getFlag()) {
            onFree();
            freeChildren();
            freed.setFlag(true);
        } else {
            Logger.e("Pointer freed twice: ");
        }
    }

    public void addChild(@Nullable AutoCloseable segment) {
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

    public final boolean checkIsParent(@NonNull MemoryController needle) {
        return this == needle || (parent != null && parent.checkIsParent(needle));
    }

    protected void freeChildren() {
        if (children != null) {
            ThrowableUtils.closeAll(children);
            children = null;
        }
    }

    /*
     * This function is called when the pointer is released.
     * Can ensure that the flag is only called once without modification.
     *
     * If you don't want to call the free function to release,
     * Rewrite and implement your own release function.
     *
     * Very few cases require rewriting this method, and in most cases, only @{link MemoryController#addChild} needs to be used.
     * And @{link BasicMemoryController}.
     */
    protected void onFree() {
    }

    /**
     * Called when setting the parent.
     *
     * @param parent The set parent.
     */
    protected void onAttachParent(@NonNull MemoryController parent) {
    }
}
