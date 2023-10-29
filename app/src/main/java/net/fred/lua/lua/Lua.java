package net.fred.lua.lua;

import androidx.annotation.Nullable;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.BasicMemoryController;

public abstract class Lua extends BasicMemoryController {

    protected Lua(@Nullable Pointer holder) {
        super(holder);
    }

    public abstract void openlibs() throws NativeMethodException;

    public abstract void dofile(String file) throws NativeMethodException;

}
