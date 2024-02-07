package net.fred.lua.foreign.allocate;

import com.google.common.base.Preconditions;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.MemorySegment;

public enum DefaultAllocator implements IAllocator {
    INSTANCE;

    @Override
    public Pointer allocateMemory(long size) throws NativeMethodException {
        Preconditions.checkPositionIndex((int) size, Integer.MAX_VALUE);
        return MemorySegment.alloc(size);
    }
}
