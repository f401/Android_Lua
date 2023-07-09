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

    public static native void duplicateStringTo(long handle, String str);


    //----------------------------------------------------------value handles------------------------------------------------------------------//

    public static native void putByte(long ptr, byte value);

    public static native void putChar(long ptr, char value);

    public static native void putShort(long ptr, short value);

    public static native void putInt(long ptr, int value);

    public static native void putLong(long ptr, long value);

    public static void putFloat(long ptr, float value) {
        putInt(ptr, Float.floatToIntBits(value));
    }

    public static void putDouble(long ptr, double value) {
        putLong(ptr, Double.doubleToLongBits(value));
    }

    public static native byte peekByte(long ptr);

    public static native char peekChar(long ptr);

    public static native short peekShort(long ptr);

    public static native int peekInt(long ptr);

    public static native long peekLong(long ptr);

    public static float peekFloat(long ptr) {
        return Float.intBitsToFloat(peekInt(ptr));
    }

    public static double peekDouble(long ptr) {
        return Double.longBitsToDouble(peekLong(ptr));
    }

    //--------------------------------------------------------------libffi--------------------------------------------------------------------//


    public static native int ffi_prep_cif(long cif, int argsCount, long returnType, long paramsType);

    static {
        System.loadLibrary("foreign");
    }
}
