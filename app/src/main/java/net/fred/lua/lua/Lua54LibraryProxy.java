package net.fred.lua.lua;

import androidx.annotation.Nullable;

import net.fred.lua.PathConstants;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.core.ForeignString;
import net.fred.lua.foreign.core.StringPool;
import net.fred.lua.foreign.proxy.LibraryProxy;

public class Lua54LibraryProxy extends Lua {
    static LuaLib lib = LibraryProxy.create(PathConstants.NATIVE_LIBRARY_DIR + "liblua.so"
            , LuaLib.class);
    private final StringPool stringPool;

    protected Lua54LibraryProxy(@Nullable Pointer pointer) {
        super(pointer);
        this.stringPool = new StringPool();
        addChild(stringPool);
    }

    @Override
    public void onFree() throws NativeMethodException {
        super.onFree();
        lib.lua_close(getPointer());
    }

    protected static Pointer newState() {
        return lib.luaL_newstate();
    }

    public static Lua54LibraryProxy create() {
        return new Lua54LibraryProxy(newState());
    }

    @Override
    public void openlibs() throws NativeMethodException {
        lib.luaL_openlibs(getPointer());
    }

    @Override
    public void dofile(String file) throws NativeMethodException {
        lib.J_luaL_dofile(getPointer(), stringPool.get(file));
    }

    public interface LuaLib {
        void lua_close(Pointer ptr);

        void luaL_openlibs(Pointer ptr);

        void J_luaL_dofile(Pointer ptr, ForeignString file);

        Pointer luaL_newstate();
    }


}
