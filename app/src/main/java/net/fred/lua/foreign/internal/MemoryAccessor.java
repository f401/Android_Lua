package net.fred.lua.foreign.internal;

import net.fred.lua.foreign.Pointer;

public class MemoryAccessor {
    static {
        System.loadLibrary("foreign");
    }

    // Put data without checking.
    // This may be dangerous.
    public static native void putStringUnchecked(Pointer handle, String str);

    public static native String peekStringUnchecked(Pointer dest);

    public static native void putByteUnchecked(Pointer ptr, byte value);

    public static native void putCharUnchecked(Pointer ptr, char value);

    public static native void putShortUnchecked(Pointer ptr, short value);

    public static native void putIntUnchecked(Pointer ptr, int value);

    public static native void putLongUnchecked(Pointer ptr, long value);

    public static native void putPointerUnchecked(Pointer ptr, Pointer value);

    public static void putFloatUnchecked(Pointer ptr, float value) {
        putIntUnchecked(ptr, Float.floatToIntBits(value));
    }

    public static void putDoubleUnchecked(Pointer ptr, double value) {
        putLongUnchecked(ptr, Double.doubleToLongBits(value));
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
