package net.fred.lua.lua;

import androidx.annotation.Nullable;

import net.fred.lua.foreign.MemoryController;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;

public abstract class Lua extends MemoryController {

    private final Pointer luaLib;

    protected Lua(@Nullable Pointer holder) {
        luaLib = holder;
    }

    public Pointer getPointer() {
        return luaLib;
    }

    public abstract void openlibs() throws NativeMethodException;

    public abstract void dofile(String file) throws NativeMethodException;

}
