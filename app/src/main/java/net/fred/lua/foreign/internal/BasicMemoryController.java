package net.fred.lua.foreign.internal;

import net.fred.lua.foreign.Pointer;

public class BasicMemoryController extends MemoryController {

    protected Pointer pointer;

    protected BasicMemoryController(Pointer pointer) {
        this.pointer = pointer;
    }

    public Pointer getPointer() {
        return pointer;
    }
}
