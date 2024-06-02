package net.fred.lua.foreign.scoped;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.allocator.IAllocator;

import java.io.Closeable;

public interface IScopedResource extends IAllocator, Closeable {
    
    IScopedResource newScope();

    @Override
    void close() throws NativeMethodException;
}
