package net.fred.lua.lua;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import net.fred.lua.PathConstants;
import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.Logger;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.core.DynamicLoadingLibrary;
import net.fred.lua.foreign.core.ForeignString;
import net.fred.lua.foreign.core.PrimaryTypes;
import net.fred.lua.foreign.ffi.FunctionCaller;

public class Lua5_4 extends Lua {
    protected static DynamicLoadingLibrary dll;
    private static final FunctionCallerCache cache = new FunctionCallerCache();

    public Lua5_4() throws NativeMethodException {
        super(new_state());
    }

    protected static Pointer new_state() throws NativeMethodException {
        if (dll == null) {
            dll = DynamicLoadingLibrary.open(PathConstants.NATIVE_LIBRARY_DIR + "liblua.so");
        }
        return (Pointer) getOrCreateFromCache("luaL_newstate", new Creator() {
            @Override
            public FunctionCaller create(String symbol) throws NativeMethodException {
                return FunctionCaller.of(dll.lookupSymbol(symbol), PrimaryTypes.POINTER);
            }
        }).call();
    }

    private static FunctionCaller getOrCreateFromCache(String symbol, Creator creator) throws NativeMethodException {
        FunctionCaller entry = cache.get(symbol);
        if (entry == null) {
            FunctionCaller result = creator.create(symbol);
            cache.put(symbol, result);
            return result;
        }
        return entry;
    }

    @Override
    protected void luaL_close() throws NativeMethodException {
        getOrCreateFromCache("lua_close", new Creator() {
            @Override
            public FunctionCaller create(String symbol) throws NativeMethodException {
                return FunctionCaller.of(dll.lookupSymbol(symbol),
                        PrimaryTypes.VOID, PrimaryTypes.POINTER);
            }
        }).call(pointer);
    }

    @Override
    public void openlibs() throws NativeMethodException {
        getOrCreateFromCache("luaL_openlibs", new Creator() {
            @Override
            public FunctionCaller create(String symbol) throws NativeMethodException {
                return FunctionCaller.of(dll.lookupSymbol(symbol),
                        PrimaryTypes.VOID, PrimaryTypes.POINTER);
            }
        }).call(pointer);
    }

    @Override
    public void dofile(String file) throws NativeMethodException {
        getOrCreateFromCache("J_luaL_dofile", new Creator() {
            @Override
            public FunctionCaller create(String symbol) throws NativeMethodException {
                return FunctionCaller.of(dll.lookupSymbol(symbol),
                        PrimaryTypes.INT,
                        PrimaryTypes.POINTER, PrimaryTypes.STRING);
            }
                }
        ).call(pointer, ForeignString.from(file));
    }

    public interface Creator {

        /**
         * Called when the corresponding @{code FunctionCaller} is not found,
         * To create a new @{code FunctionCaller},
         * <p>
         * The lifecycle of @{code FunctionCaller} will be automatically managed by Cache
         *
         * @param symbol To create symbols corresponding to native layer functions.
         * @return a new @{code FunctionCaller}
         * @throws NativeMethodException cause by @{link FunctionCaller#of}
         */
        FunctionCaller create(String symbol) throws NativeMethodException;
    }

    private static class FunctionCallerCache extends LruCache<String, FunctionCaller> {
        public FunctionCallerCache() {
            super(256);
        }

        @Override
        protected int sizeOf(@NonNull String key, @NonNull FunctionCaller value) {
            return value.getDescriber().obtainFFISize();
        }

        @Override
        protected void entryRemoved(boolean evicted, @NonNull String key, @NonNull FunctionCaller oldValue, @Nullable FunctionCaller newValue) {
            try {
                Logger.i("Removing caller. key: " + key + ": " + oldValue);
                oldValue.close();
            } catch (NativeMethodException e) {
                CrashHandler.fastHandleException(e);
            }
        }
    }

}
