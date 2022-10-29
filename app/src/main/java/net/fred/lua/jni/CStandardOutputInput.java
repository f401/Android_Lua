package net.fred.lua.jni;

public final class CStandardOutputInput {
    
    private static CStandardOutputInput instance;
    public final long defaultStandardOut;
    public final long defaultStandardErr;
    
    private CStandardOutputInput() {
        defaultStandardOut = getStandardOutPointer();
        defaultStandardErr = getStandardErrPointer();
    }
    
    public static CStandardOutputInput getInstance() {
        if (instance == null) {
            instance = new CStandardOutputInput();
        }
        return instance;
    }
    
    public void redirectStandardOutToBase() {
        setStandardOutPointer(defaultStandardOut);
    }
    
    public void redirectStandardErrToBase() {
        setStandardErrPointer(defaultStandardErr);
    }
    
    public native long getStandardOutPointer();
    public native long getStandardErrPointer();
    public native void setStandardOutPointer(long ptr);
    public native void setStandardErrPointer(long ptr);
    
    public native void redirectStandardOutTo(String path);
    public native void redirectStandardErrTo(String path);
    
}
