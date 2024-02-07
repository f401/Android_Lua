package net.fred.lua.foreign.allocate;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;

public interface IAllocator {
    /**
     * Create a 'size' length and return a pointer to it.
     *
     * @param size The size needs to be created.
     * @return Pointer to.
     * @throws NativeMethodException    When creation fails
     * @throws IllegalArgumentException When {@code size} is less than or equal to 0.
     */
    Pointer allocateMemory(long size) throws NativeMethodException;
}
