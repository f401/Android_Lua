package net.fred.lua.foreign.core;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.BasicMemoryController;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.io.Logger;

public final class DynamicLoadingLibrary extends BasicMemoryController {

    private final PointerLruCache cache;

    private DynamicLoadingLibrary(Pointer ptr) {
        super(new DLLPointerHolder(ptr));
        this.cache = new PointerLruCache();
    }

    public Pointer lookupSymbol(String symbol) throws NativeMethodException {
        ArgumentsChecker.checkStringNotNullOrEmpty(symbol, "Invoker (" + ThrowableUtils.getCallerString() +
                "), passes null symbol.");
        return cache.get(symbol);
    }

    public static DynamicLoadingLibrary open(String path) throws NativeMethodException {
        ArgumentsChecker.checkFileExists(path
                , "Invoker (" + ThrowableUtils.getCallerString() + "), passes null symbol.");

        Pointer handle = dlopen(path, ForeignValues.RTLD_LAZY);
        Logger.i("Loaded library " + path + ".At 0x" + Long.toHexString(handle.get()));
        return new DynamicLoadingLibrary(handle);
    }
    public static native Pointer dlopen(String path, int flags) throws NativeMethodException;
    public static native int dlclose(Pointer ptr);

    public static native Pointer dlsym(Pointer handle, String src) throws NativeMethodException;

    private static class DLLPointerHolder extends BasicMemoryController.SinglePointerHolder {

        protected DLLPointerHolder(Pointer pointer) {
            super(pointer);
        }

        @Override
        public void onFree() throws NativeMethodException {
            Logger.i("Release dll at " + pointer);
            dlclose(pointer);
        }
    }

    private class PointerLruCache extends LruCache<String, Pointer> {
        public PointerLruCache() {
            super(50);
        }

        @Override
        protected Pointer create(@NonNull String key) {
            try {
                Pointer ptr = dlsym(getPointer(), key);
                Logger.i("Loaded symbol " + key + ".At " + ptr);
                return ptr;
            } catch (NativeMethodException e) {
                Logger.e(e.getMessage());
                CrashHandler.fastHandleException(e);
                return null;
            }
        }
    }

    static {
        System.loadLibrary("foreign");
    }
}
