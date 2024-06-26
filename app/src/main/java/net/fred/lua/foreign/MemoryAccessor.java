package net.fred.lua.foreign;

import dalvik.annotation.optimization.CriticalNative;

public class MemoryAccessor {

    // Put data without checking.
    // This may be dangerous.
    public static final MemoryAccessor UNCHECKED = new MemoryAccessor();

    static {
        System.loadLibrary("foreign");
    }

    public static native byte peekByteUnchecked(Pointer ptr);

    public static native char peekCharUnchecked(Pointer ptr);

    public static native short peekShortUnchecked(Pointer ptr);

    public static native int peekIntUnchecked(Pointer ptr);

    public static native long peekLongUnchecked(Pointer ptr);

    public static native Pointer peekPointerUnchecked(Pointer dest);

    public static float peekFloatUnchecked(Pointer ptr) {
        return Float.intBitsToFloat(peekIntUnchecked(ptr));
    }

    public static double peekDoubleUnchecked(Pointer ptr) {
        return Double.longBitsToDouble(peekLongUnchecked(ptr));
    }

    public void putString(Pointer handle, String str) {
        putStringUnchecked(handle, str);
    }

    public String peekString(Pointer dest) {
        return peekStringUnchecked(dest);
    }

    @CriticalNative
    private static native void nativePutByte(long address, byte value);

    @CriticalNative
    private static native void nativePutShort(long address, short value);

    @CriticalNative
    private static native void nativePutInt(long address, int value);

    @CriticalNative
    private static native void nativePutChar(long address, char value);

    @CriticalNative
    private static native void nativePutLong(long address, long value);

    public void putByte(Pointer ptr, byte value) {
        putByteUnchecked(ptr, value);
    }

    public void putChar(Pointer ptr, char value) {
        putCharUnchecked(ptr, value);
    }

    public void putShort(Pointer ptr, short value) {
        putShortUnchecked(ptr, value);
    }

    public void putInt(Pointer ptr, int value) {
        putIntUnchecked(ptr, value);
    }

    public void putLong(Pointer ptr, long value) {
        putLongUnchecked(ptr, value);
    }

    public void putFloat(Pointer ptr, float value) {
        putFloatUnchecked(ptr, value);
    }

    public static native void putStringUnchecked(Pointer handle, String str);

    public static native String peekStringUnchecked(Pointer dest);

    public static void putByteUnchecked(Pointer ptr, byte value) {
        nativePutByte(ptr.get(), value);
    }

    public static void putCharUnchecked(Pointer ptr, char value) {
        nativePutChar(ptr.get(), value);
    }

    public static void putShortUnchecked(Pointer ptr, short value) {
        nativePutShort(ptr.get(), value);
    }

    public static void putIntUnchecked(Pointer ptr, int value) {
        nativePutInt(ptr.get(), value);
    }

    public static void putLongUnchecked(Pointer ptr, long value) {
        nativePutLong(ptr.get(), value);
    }

    public static native void putPointerUnchecked(Pointer ptr, Pointer value);

    public static void putFloatUnchecked(Pointer ptr, float value) {
        putIntUnchecked(ptr, Float.floatToIntBits(value));
    }

    public static void putDoubleUnchecked(Pointer ptr, double value) {
        putLongUnchecked(ptr, Double.doubleToLongBits(value));
    }

    public void putDouble(Pointer ptr, double value) {
        putDoubleUnchecked(ptr, value);
    }

    public void putPointer(Pointer dest, Pointer ptr) {
        putPointerUnchecked(dest, ptr);
    }

    public byte peekByte(Pointer ptr) {
        return peekByteUnchecked(ptr);
    }

    public char peekChar(Pointer ptr) {
        return peekCharUnchecked(ptr);
    }

    public short peekShort(Pointer ptr) {
        return peekShortUnchecked(ptr);
    }

    public int peekInt(Pointer ptr) {
        return peekIntUnchecked(ptr);
    }

    public long peekLong(Pointer ptr) {
        return peekLongUnchecked(ptr);
    }

    public Pointer peekPointer(Pointer dest) {
        return peekPointerUnchecked(dest);
    }

    public float peekFloat(Pointer ptr) {
        return peekFloatUnchecked(ptr);
    }

    public double peekDouble(Pointer ptr) {
        return peekDoubleUnchecked(ptr);
    }


}
