package net.fred.lua.foreign.core;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.google.common.base.Preconditions;

import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.BasicMemoryController;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.io.Logger;

public final class DynamicLoadingLibrary extends BasicMemoryController {

    private final PointerLruCache cache;

    private DynamicLoadingLibrary(Pointer ptr) {
        super(ptr);
        this.cache = new PointerLruCache();
    }

    public static DynamicLoadingLibrary open(String path) throws NativeMethodException {
        Preconditions.checkState(FileUtils.exists(path), "File %s doesn't exist.", path);

        Pointer handle = dlopen(path, ForeignValues.RTLD_LAZY);
        Logger.i("Loaded library " + path + ".At 0x" + Long.toHexString(handle.get()));
        return new DynamicLoadingLibrary(handle);
    }

    public Pointer lookupSymbol(String symbol) throws NativeMethodException {
        Preconditions.checkNotNull(symbol, "Null symbol.");
        return cache.get(symbol);
    }

    public static native Pointer dlopen(String path, int flags) throws NativeMethodException;

    public static native int dlclose(Pointer ptr);

    public static native Pointer dlsym(Pointer handle, String src) throws NativeMethodException;

    @Override
    public void onFree() {
        Logger.i("Release dll at " + pointer);
        dlclose(pointer);
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
