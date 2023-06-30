package net.fred.lua.foreign;

/**
 * Contains all native functions that this project need.
 */
public class ForeignFunctions {
    public static native long alloc(long size);
    public static native void free(long ptr);

    static {
        System.loadLibrary("bridge");
    }
}
