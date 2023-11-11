package net.fred.lua.foreign;

public class Breakpad {
    static {
        System.loadLibrary("foreign");
    }

    //These defined in breakpad.cpp
    public static native void init(String crashSavePath);

    public static native void SEND_SIGNAL_SEGV();
}
