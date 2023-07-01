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

    static {
        System.loadLibrary("foreign");
        NULL = getNULL();
        RTLD_LAZY = getRTLD_LAZY();
        RTLD_GLOBAL = getRTLD_GLOBAL();
        RTLD_NOW = getRTLD_NOW();
        RTLD_LOCAL = getRTLD_LOCAL();
    }

    /**
     * Those functions should only invoke once when launching.
     */
    private static native long getNULL();

    private static native int getRTLD_LAZY();

    private static native int getRTLD_GLOBAL();

    private static native int getRTLD_NOW();

    private static native int getRTLD_LOCAL();
}
