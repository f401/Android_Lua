package net.fred.lua.io;

public final class CStandardOutputInput {

    private static CStandardOutputInput instance;

    private CStandardOutputInput() {
    }

    public static CStandardOutputInput getInstance() {
        if (instance == null) {
            instance = new CStandardOutputInput();
        }
        return instance;
    }

    public native void redirectStandardOutTo(String path);


    static {
        System.loadLibrary("bridge");
    }
}
