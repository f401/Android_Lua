package net.fred.lua.foreign.types;

import net.fred.lua.foreign.Pointer;

/**
 * All types that can serve as pointers need to implement this interface.
 */
public interface PointerType {
    Pointer getPointer();
}
