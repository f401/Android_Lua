package net.fred.lua.common;

/**
 * A thread safety flag
 */
public final class Flag {

    private boolean flag;

    public Flag(boolean initVal) {
        flag = initVal;
    }

    synchronized public boolean getFlag() {
        return flag;
    }

    synchronized public void setFlag(boolean flag) {
        this.flag = flag;
    }

}
