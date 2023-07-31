package net.fred.lua.foreign;

import net.fred.lua.common.CrashHandler;

public class Breakpad {
    static {
        System.loadLibrary("foreign");
    }

    //These defined in breakpad.cpp
    public static native void init(String crashSavePath);

    public static native void SEND_SIGNAL_SEGV();

    public static void onNativeCrashHappened(String dumpPath) {
        CrashHandler.fastHandleException(new SignalHappenedException("Dump saved to " + dumpPath));
    }

    private static class SignalHappenedException extends RuntimeException {
        public SignalHappenedException(String message) {
            super(message);
        }
    }
}
