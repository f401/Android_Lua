package net.fred.lua.foreign.child;

/**
 * This holder will not do anything when inserting or deleting.
 */
public class RejectAllHolder implements IChildPolicy {
    @Override
    public void addChild(AutoCloseable child) {
    }

    @Override
    public void closeAllChild() {

    }

    @Override
    public void removeChild(AutoCloseable child) {

    }

    @Override
    public boolean hasChild() {
        return false;
    }

    @Override
    public AutoCloseable childAt(int idx) {
        return null;
    }
}
