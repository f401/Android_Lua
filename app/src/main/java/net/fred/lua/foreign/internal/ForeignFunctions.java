package net.fred.lua.foreign.internal;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;

/**
 * Contains all native functions that this project need.
 */
public class ForeignFunctions {
    public static native Pointer alloc(long size) throws NativeMethodException;

    public static native void free(Pointer ptr);

    /**
     * I know {@link android.system.Os#strerror(int)}
     * but this one doesn't have {@code errno}
     */
    public static native String strerror();

    public static native void memcpy(Pointer dest, Pointer src, long length);

    public static native Pointer dlopen(String path, int flags) throws NativeMethodException;

    public static native int dlclose(Pointer ptr);

    public static native Pointer dlsym(Pointer handle, String src) throws NativeMethodException;

    // --------------------------------------------------------String--------------------------------------------------------------------------//
    public static native void putString(Pointer handle, String str);

    public static native String peekString(Pointer dest);

    public static native long obtainStringLength(Pointer dest);


    //----------------------------------------------------------value handles------------------------------------------------------------------//

    public static native void putByte(Pointer ptr, byte value);

    public static native void putChar(Pointer ptr, char value);

    public static native void putShort(Pointer ptr, short value);

    public static native void putInt(Pointer ptr, int value);

    public static native void putLong(Pointer ptr, long value);

    public static native void putPointer(Pointer ptr, Pointer value);

    public static void putFloat(Pointer ptr, float value) {
        putInt(ptr, Float.floatToIntBits(value));
    }

    public static void putDouble(Pointer ptr, double value) {
        putLong(ptr, Double.doubleToLongBits(value));
    }

    public static native byte peekByte(Pointer ptr);

    public static native char peekChar(Pointer ptr);

    public static native short peekShort(Pointer ptr);

    public static native int peekInt(Pointer ptr);

    public static native long peekLong(Pointer ptr);

    public static native Pointer peekPointer(Pointer dest);

    public static float peekFloat(Pointer ptr) {
        return Float.intBitsToFloat(peekInt(ptr));
    }

    public static double peekDouble(Pointer ptr) {
        return Double.longBitsToDouble(peekLong(ptr));
    }

    //--------------------------------------------------------------libffi--------------------------------------------------------------------/

    static {
        System.loadLibrary("foreign");
    }
}
