package net.fred.lua.foreign.allocator;

import com.google.common.base.Preconditions;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Resource;

public enum DefaultAllocator implements IAllocator {
    INSTANCE;

    @Override
    public Resource allocateMemory(long size) throws NativeMethodException {
        Preconditions.checkPositionIndex((int) size, Integer.MAX_VALUE);
        return new LibcMallocResourceImpl(size);
    }
}
