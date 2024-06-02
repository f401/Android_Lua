package net.fred.lua.foreign.scoped;

import net.fred.lua.foreign.MemoryController;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Resource;
import net.fred.lua.foreign.allocator.IAllocator;

abstract class ScopedResourceImpl extends MemoryController implements IScopedResource {
    
    final IAllocator mAllocator;

    public ScopedResourceImpl(IAllocator allocator) {
        this.mAllocator = allocator;
    }

    /**
     * 分配被限制的内存
     */
    @Override
    public Resource allocateMemory(long size) throws NativeMethodException {
        Resource real = mAllocator.allocateMemory(size);
        addChild(real);
        return real;
    }
    
    
}
