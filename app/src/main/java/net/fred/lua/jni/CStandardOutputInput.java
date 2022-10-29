package net.fred.lua.jni;

public final class CStandardOutputInput {
    
    private static CStandardOutputInput instance;
    public final long defaultStandardOut;
    public final long defaultStandardErr;
    public final long defaultStandardIn;
    
    private CStandardOutputInput() {
        defaultStandardOut = getStandardOutPointer();
        defaultStandardErr = getStandardErrPointer();
        defaultStandardIn = getStandardErrPointer();
    }
    
    public static CStandardOutputInput getInstance() {
        if (instance == null) {
            instance = new CStandardOutputInput();
        }
        return instance;
    }
    
    public void redirectStandardOutToBase() {
        if (defaultStandardOut != getStandardOutPointer()) {
            closePointer(getStandardOutPointer());
            setStandardOutPointer(defaultStandardOut);
        }
    }
    
    public void redirectStandardErrToBase() {
        if (defaultStandardErr != getStandardErrPointer()) {
            closePointer(getStandardErrPointer());
            setStandardErrPointer(defaultStandardErr);
        }
    }
    
    public void redirectStandardInToBase() {
        if (defaultStandardIn != getStandardInPointer()) {
            closePointer(getStandardInPointer());
            setStandardInPointer(defaultStandardIn);
        }
    }
    
    private void setDefaultAndCloseOther(long ptr, long defaultPtr) {
        if (ptr != defaultPtr) {
            closePointer(ptr);
            
        }
    }
    
    public native long getStandardOutPointer();
    public native long getStandardErrPointer();
    public native long getStandardInPointer();
    
    //WARNING: 用不好这些，正常的资源不能释放，还可能出现Segment fault
    public native void redirectStandardOutTo(String path);
    public native void redirectStandardErrTo(String path);
    public native void redirectStandardInTo(String path);
    public native void setStandardOutPointer(long ptr);
    public native void setStandardErrPointer(long ptr);
    public native void setStandardInPointer(long ptr);
    public native void closePointer(long ptr);
    
    static {
        System.loadLibrary("bridge");
    }
}
