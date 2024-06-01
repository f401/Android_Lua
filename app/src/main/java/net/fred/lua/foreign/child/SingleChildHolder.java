package net.fred.lua.foreign.child;

import net.fred.lua.common.utils.ThrowableUtils;

public class SingleChildHolder implements IChildPolicy {

    private AutoCloseable child;

    @Override
    public void addChild(AutoCloseable child) {
        this.child = child;
    }

    @Override
    public void closeAllChild() {
        ThrowableUtils.closeAll(child);
    }

    @Override
    public void removeChild(AutoCloseable child) {
        child = null;
    }

    @Override
    public boolean hasChild() {
        return child != null;
    }

    @Override
    public AutoCloseable childAt(int idx) {
        return child;
    }
}
