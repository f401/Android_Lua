package net.fred.lua.foreign.internal;

import net.fred.lua.foreign.Pointer;

public class MemoryAccessor {
    static {
        System.loadLibrary("foreign");
    }

    // --------------------------------------------------------String--------------------------------------------------------------------------//
    public static native void putString(Pointer handle, String str);

    public static native String peekString(Pointer dest);

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
}
