package net.fred.lua.lua;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import net.fred.lua.PathConstants;
import net.fred.lua.common.CrashHandler;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.allocator.DefaultAllocator;
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

    @Override
    public void dispose(boolean finalized) throws NativeMethodException {
        super.dispose(finalized);
        getOrCreateFromCache("lua_close", new Creator() {
            @Override
            public FunctionCaller create(String symbol) throws NativeMethodException {
                return FunctionCaller.of(dll.lookupSymbol(symbol), PrimaryTypes.VOID, PrimaryTypes.POINTER);
            }
        }).call(getPointer());
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
    public void openlibs() throws NativeMethodException {
        getOrCreateFromCache("luaL_openlibs", new Creator() {
            @Override
            public FunctionCaller create(String symbol) throws NativeMethodException {
                return FunctionCaller.of(dll.lookupSymbol(symbol),
                        PrimaryTypes.VOID, PrimaryTypes.POINTER);
            }
        }).call(getPointer());
    }

    @Override
    public void dofile(String file) throws NativeMethodException {
        getOrCreateFromCache("J_luaL_dofile", new Creator() {
            @Override
            public FunctionCaller create(String symbol) throws NativeMethodException {
                return FunctionCaller.of(false, dll.lookupSymbol(symbol),
                        PrimaryTypes.INT,
                        PrimaryTypes.POINTER, PrimaryTypes.STRING);
            }
        }).call(getPointer(), ForeignString.from(DefaultAllocator.INSTANCE, file));
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
            super(16);
        }

        @Override
        protected void entryRemoved(boolean evicted, @NonNull String key, @NonNull FunctionCaller oldValue, @Nullable FunctionCaller newValue) {
            try {
                oldValue.close();
            } catch (NativeMethodException e) {
                CrashHandler.fastHandleException(e);
            }
        }
    }

}
