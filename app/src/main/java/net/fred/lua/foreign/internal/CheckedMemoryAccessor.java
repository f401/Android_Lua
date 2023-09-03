package net.fred.lua.foreign.internal;

import androidx.annotation.NonNull;

import net.fred.lua.foreign.Pointer;

public class CheckedMemoryAccessor extends MemoryAccessor {
    private final Pointer boundary;

    public CheckedMemoryAccessor(@NonNull Pointer base, long max) {
        boundary = base.plus(max);
    }

    @Override
    public void putString(Pointer handle, String str) {
        checkBoundary(handle);
        super.putString(handle, str);
    }

    @Override
    public String peekString(Pointer dest) {
        checkBoundary(dest);
        return super.peekString(dest);
    }

    @Override
    public void putByte(Pointer ptr, byte value) {
        checkBoundary(ptr);
        super.putByte(ptr, value);
    }

    @Override
    public void putChar(Pointer ptr, char value) {
        checkBoundary(ptr);
        super.putChar(ptr, value);
    }

    @Override
    public void putShort(Pointer ptr, short value) {
        checkBoundary(ptr);
        super.putShort(ptr, value);
    }

    @Override
    public void putInt(Pointer ptr, int value) {
        checkBoundary(ptr);
        super.putInt(ptr, value);
    }

    @Override
    public void putLong(Pointer ptr, long value) {
        checkBoundary(ptr);
        super.putLong(ptr, value);
    }

    @Override
    public void putFloat(Pointer ptr, float value) {
        checkBoundary(ptr);
        super.putFloat(ptr, value);
    }

    @Override
    public void putDouble(Pointer ptr, double value) {
        checkBoundary(ptr);
        super.putDouble(ptr, value);
    }

    @Override
    public byte peekByte(Pointer ptr) {
        checkBoundary(ptr);
        return super.peekByte(ptr);
    }

    @Override
    public char peekChar(Pointer ptr) {
        checkBoundary(ptr);
        return super.peekChar(ptr);
    }

    @Override
    public short peekShort(Pointer ptr) {
        checkBoundary(ptr);
        return super.peekShort(ptr);
    }

    @Override
    public int peekInt(Pointer ptr) {
        checkBoundary(ptr);
        return super.peekInt(ptr);
    }

    @Override
    public long peekLong(Pointer ptr) {
        checkBoundary(ptr);
        return super.peekLong(ptr);
    }

    @Override
    public Pointer peekPointer(Pointer dest) {
        checkBoundary(dest);
        return super.peekPointer(dest);
    }

    @Override
    public float peekFloat(Pointer ptr) {
        checkBoundary(ptr);
        return super.peekFloat(ptr);
    }

    @Override
    public double peekDouble(Pointer ptr) {
        checkBoundary(ptr);
        return super.peekDouble(ptr);
    }

    private void checkBoundary(Pointer ptr) {
        if (!boundary.equals(ptr) && boundary.biggerThan(ptr)) {
            throw new IndexOutOfBoundsException("Boundary: " + boundary + ", but " + ptr);
        }
    }
}
