package net.fred.lua.foreign.scoped;

import net.fred.lua.foreign.allocator.IAllocator;

public interface IScopedResource extends IAllocator {
    
    IScopedResource newScope();
    
}
