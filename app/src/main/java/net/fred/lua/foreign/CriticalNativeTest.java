package net.fred.lua.foreign;

import dalvik.annotation.optimization.CriticalNative;

public class CriticalNativeTest {
    
    @CriticalNative
    public static native void test(int arg0, int arg1);
    
    public static void runTest() {
        test(0, 0);
    }
   
    static {
        System.loadLibrary("foreign");
    }
}
