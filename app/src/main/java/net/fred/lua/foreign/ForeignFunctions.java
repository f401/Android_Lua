package net.fred.lua.foreign;

/**
 * Contains all native functions that this project need.
 */
public class ForeignFunctions {
    public static native long alloc(long size);

    public static native void free(long ptr);

    /**
     * I know {@link android.system.Os#strerror(int)}
     * but this one doesn't have {@code errno}
     */
    public static native String strerror();

    public static native long dlopen(String path, int flags);

    public static native String dlerror();

    public static native int dlclose(long ptr);

    public static native long dlsym(long handle, String src);

    static {
        System.loadLibrary("foreign");
    }
}
