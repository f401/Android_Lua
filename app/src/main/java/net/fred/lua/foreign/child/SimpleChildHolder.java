package net.fred.lua.foreign.child;

import com.google.common.base.Preconditions;

import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.internal.MemoryController;

import java.util.ArrayList;
import java.util.List;
import androidx.core.util.Consumer;

public class SimpleChildHolder implements IChildPolicy {

    private final MemoryController controller;
    private List<AutoCloseable> children;

    public SimpleChildHolder(MemoryController controller) {
        this.controller = controller;
    }

    @Override
    public void addChild(AutoCloseable segment) {
        Preconditions.checkState(!controller.isClosed(), "Father has been released.");
        if (segment != controller && segment != null) {
            synchronized (this) {
                if (children == null) {
                    children = new ArrayList<>(2);
                }

                if (segment instanceof MemoryController) {
                    MemoryController child = (MemoryController) segment;
                    Preconditions.checkState(
                            !controller.checkIsParent(child), "The required registered son is the father of the current object.");
                    child.attachParent(controller);
                    children.add(segment);
                }
            }
        }
    }

    @Override
    public void closeAllChild() {
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

    @Override
    public synchronized void removeChild(AutoCloseable child) {
        if (children.remove(child)) {
            if (child instanceof MemoryController) {
                ((MemoryController) child).detachParent();
            }
        }
    }

    @Override
    public boolean hasChild() {
        return children != null && children.size() != 0;
    }

    @Override
    public AutoCloseable childAt(int idx) {
        return children.get(idx);
    }
}
