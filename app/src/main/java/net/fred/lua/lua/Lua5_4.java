package net.fred.lua.lua;

import net.fred.lua.PathConstants;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.core.DynamicLoadingLibrary;
import net.fred.lua.foreign.core.ForeignString;
import net.fred.lua.foreign.ffi.FunctionCaller;
import net.fred.lua.foreign.types.PrimaryTypeWrapper;

public class Lua5_4 extends Lua {
    protected static DynamicLoadingLibrary dll;

    public Lua5_4() throws NativeMethodException {
        super(new_state());
    }

    @Override
    protected void luaL_close() throws NativeMethodException {
        FunctionCaller.of
                (dll.lookupSymbol("lua_close"), PrimaryTypeWrapper.of(void.class), Pointer.ofType())
                .callAndClose(pointer);
    }

    @Override
    public void openlibs() throws NativeMethodException {
        FunctionCaller.of(dll.lookupSymbol("luaL_openlibs"),
                PrimaryTypeWrapper.of(void.class), Pointer.ofType())
                .callAndClose(pointer);
    }

    @Override
    public void dofile(String file) throws NativeMethodException {
        FunctionCaller.of(dll.lookupSymbol("J_luaL_dofile"),
                        PrimaryTypeWrapper.of(int.class),
                Pointer.ofType(), ForeignString.ofType())
                .callAndClose(pointer, ForeignString.from(file));
    }

    protected static Pointer new_state() throws NativeMethodException {
        if (dll == null) {
            dll = DynamicLoadingLibrary.open(PathConstants.NATIVE_LIBRARY_DIR + "liblua.so");
        }
        FunctionCaller caller = FunctionCaller.of(dll.lookupSymbol("luaL_newstate"), Pointer.ofType());
        Pointer result = (Pointer) caller.call();
        caller.close();
        return result;
    }

}
