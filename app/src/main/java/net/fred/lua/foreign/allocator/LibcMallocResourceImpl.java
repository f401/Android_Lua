package net.fred.lua.foreign.allocator;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.Resource;
import net.fred.lua.foreign.internal.MemorySegment;

public final class LibcMallocResourceImpl extends Resource {
    private final Pointer pointer;
    private final long size;

    public LibcMallocResourceImpl(long size) throws NativeMethodException {
        this.pointer = MemorySegment.alloc(size);
        this.size = size;
    }

    @Override
    public Pointer getBasePointer() {
        return pointer;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void dispose(boolean finalized) throws NativeMethodException {
        super.dispose(finalized);
        MemorySegment.free(pointer);
    }
}
