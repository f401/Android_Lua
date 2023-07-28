package net.fred.lua.lua;

import androidx.annotation.Nullable;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.BasicMemoryController;

public abstract class Lua extends BasicMemoryController {

    protected Lua(@Nullable Pointer pointer) {
        super(pointer);
    }

    @Override
    protected void onFree() throws NativeMethodException {
        luaL_close();
    }

    protected abstract void luaL_close() throws NativeMethodException;
    public abstract void openlibs() throws NativeMethodException;

    public abstract void dofile(String file) throws NativeMethodException;

}
