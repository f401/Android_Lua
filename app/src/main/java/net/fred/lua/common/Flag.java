package net.fred.lua.common;

/**
 * A thread safety flag
 */
public class Flag {
    
    private boolean flag;
	
	public Flag() {
		this(false);
	}
	
	public Flag(boolean initVal) {
		flag = initVal;
	}
    
	synchronized public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	synchronized public boolean getFlag() {
		return flag;
	}
	
}
