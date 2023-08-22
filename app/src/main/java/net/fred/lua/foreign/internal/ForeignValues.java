package net.fred.lua.foreign.internal;

import net.fred.lua.foreign.Pointer;

/**
 * Contains all native values that this project need.
 */
public class ForeignValues {

    public static final Pointer NULL;
    public static final int RTLD_LAZY;
    public static final int RTLD_NOW;
    public static final int RTLD_GLOBAL;
    public static final int RTLD_LOCAL;

    //libffi

    public static final Pointer FFI_TYPE_INT8;
    public static final Pointer FFI_TYPE_INT16;
    public static final Pointer FFI_TYPE_INT32;
    public static final Pointer FFI_TYPE_INT64;
    public static final Pointer FFI_TYPE_POINTER;
    public static final Pointer FFI_TYPE_VOID;

    public static final Pointer FFI_TYPE_UINT8;
    public static final Pointer FFI_TYPE_UINT16;
    public static final Pointer FFI_TYPE_UINT32;
    public static final Pointer FFI_TYPE_UINT64;

    public static final Pointer FFI_TYPE_FLOAT;
    public static final Pointer FFI_TYPE_DOUBLE;

    //sizes

    public static final long SIZE_OF_FFI_CIF;
    public static final long SIZE_OF_POINTER;

    public static final int FFI_STATUS_OK;

    static {
        System.loadLibrary("foreign");
        NULL = Pointer.from(getNULL());
        RTLD_LAZY = getRTLD_LAZY();
        RTLD_GLOBAL = getRTLD_GLOBAL();
        RTLD_NOW = getRTLD_NOW();
        RTLD_LOCAL = getRTLD_LOCAL();

        FFI_TYPE_INT8 = Pointer.from(getFFI_TYPE_INT8());
        FFI_TYPE_INT16 = Pointer.from(getFFI_TYPE_INT16());
        FFI_TYPE_INT32 = Pointer.from(getFFI_TYPE_INT32());
        FFI_TYPE_INT64 = Pointer.from(getFFI_TYPE_INT64());

        FFI_TYPE_UINT8 = Pointer.from(getFFI_TYPE_UINT8());
        FFI_TYPE_UINT16 = Pointer.from(getFFI_TYPE_UINT16());
        FFI_TYPE_UINT32 = Pointer.from(getFFI_TYPE_UINT32());
        FFI_TYPE_UINT64 = Pointer.from(getFFI_TYPE_UINT64());

        FFI_TYPE_POINTER = Pointer.from(getFFI_TYPE_POINTER());
        FFI_TYPE_VOID = Pointer.from(getFFI_TYPE_VOID());
        FFI_TYPE_FLOAT = Pointer.from(getFFI_TYPE_FLOAT());
        FFI_TYPE_DOUBLE = Pointer.from(getFFI_TYPE_DOUBLE());

        FFI_STATUS_OK = getFFI_STATUS_OK();

        SIZE_OF_FFI_CIF = sizeOfFFI_CIF();
        SIZE_OF_POINTER = sizeOfPointer();
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

    private static native long getFFI_TYPE_VOID();

    private static native long getFFI_TYPE_UINT8();

    private static native long getFFI_TYPE_UINT16();

    private static native long getFFI_TYPE_UINT32();

    private static native long getFFI_TYPE_UINT64();

    private static native long getFFI_TYPE_FLOAT();

    private static native long getFFI_TYPE_DOUBLE();

    private static native int getFFI_STATUS_OK();

    private static native long sizeOfFFI_CIF();

    private static native long sizeOfPointer();
}
