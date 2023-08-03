package net.fred.lua.foreign.core;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.Logger;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.BasicMemoryController;
import net.fred.lua.foreign.internal.ForeignFunctions;
import net.fred.lua.foreign.internal.ForeignValues;

public final class DynamicLoadingLibrary extends BasicMemoryController {

    private final PointerLruCache cache;

    private DynamicLoadingLibrary(Pointer ptr) {
        super(ptr);
        this.cache = new PointerLruCache();
    }

    public static DynamicLoadingLibrary open(String path) throws NativeMethodException {
        ArgumentsChecker.checkFileExists(path
                , "Invoker (" + ThrowableUtils.getCallerString() + "), passes null symbol.");

        Pointer handle = ForeignFunctions.dlopen(path, ForeignValues.RTLD_LAZY);
        Logger.i("Loaded library " + path + ".At 0x" + Long.toHexString(handle.get()));
        return new DynamicLoadingLibrary(handle);
    }

    public Pointer lookupSymbol(String symbol) throws NativeMethodException {
        ArgumentsChecker.checkStringNotNullOrEmpty(symbol, "Invoker (" + ThrowableUtils.getCallerString() +
                "), passes null symbol.");
        return cache.get(symbol);
    }

    @Override
    protected void onFree() {
        Logger.i("Release dll at " + pointer);
        ForeignFunctions.dlclose(pointer);
    }

    private class PointerLruCache extends LruCache<String, Pointer> {
        public PointerLruCache() {
            super(50);
        }

        @Override
        protected Pointer create(@NonNull String key) {
            try {
                Pointer ptr = ForeignFunctions.dlsym(pointer, key);
                Logger.i("Loaded symbol " + key + ".At " + ptr);
                return ptr;
            } catch (NativeMethodException e) {
                Logger.e(e.getMessage());
                CrashHandler.fastHandleException(e);
                return null;
            }
        }
    }
}
