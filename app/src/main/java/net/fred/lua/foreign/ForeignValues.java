package net.fred.lua.foreign;

/**
 * Contains all native values that this project need.
 */
public class ForeignValues {

    public static final long NULL;
    public static final int RTLD_LAZY;
    public static final int RTLD_NOW;
    public static final int RTLD_GLOBAL;
    public static final int RTLD_LOCAL;

    //libffi

    public static final long FFI_TYPE_INT8;
    public static final long FFI_TYPE_INT16;
    public static final long FFI_TYPE_INT32;
    public static final long FFI_TYPE_INT64;
    public static final long FFI_TYPE_POINTER;

    static {
        System.loadLibrary("foreign");
        NULL = getNULL();
        RTLD_LAZY = getRTLD_LAZY();
        RTLD_GLOBAL = getRTLD_GLOBAL();
        RTLD_NOW = getRTLD_NOW();
        RTLD_LOCAL = getRTLD_LOCAL();

        FFI_TYPE_INT8 = getFFI_TYPE_INT8();
        FFI_TYPE_INT16 = getFFI_TYPE_INT16();
        FFI_TYPE_INT32 = getFFI_TYPE_INT32();
        FFI_TYPE_INT64 = getFFI_TYPE_INT64();
        FFI_TYPE_POINTER = getFFI_TYPE_POINTER();
    }

    /**
     * Those functions should only invoke once when launching.
     */
    private static native long getNULL();

    private static native int getRTLD_LAZY();

    private static native int getRTLD_GLOBAL();

    private static native int getRTLD_NOW();

    private static native int getRTLD_LOCAL();

    private static native long getFFI_TYPE_INT8();

    private static native long getFFI_TYPE_INT16();

    private static native long getFFI_TYPE_INT32();

    private static native long getFFI_TYPE_INT64();

    private static native long getFFI_TYPE_POINTER();
}
