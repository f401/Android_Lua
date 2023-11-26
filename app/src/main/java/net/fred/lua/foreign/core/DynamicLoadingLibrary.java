package net.fred.lua.foreign.core;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.foreign.internal.MemoryController;
import net.fred.lua.io.Logger;

import java.util.concurrent.TimeUnit;

public final class DynamicLoadingLibrary extends MemoryController {

    /**
     * Symbol Cache
     */
    private final LoadingCache<String, Pointer> cache;
    private final Pointer libPointer;

    private DynamicLoadingLibrary(Pointer ptr) {
        this.libPointer = ptr;
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .maximumSize(20)
                .concurrencyLevel(5)
                .build(new CacheLoader<String, Pointer>() {
                    @Override
                    public Pointer load(@NonNull String key) throws Exception {
                        Logger.i("Loading symbol " + key);
                        return dlsym(libPointer, key);
                    }
                });
    }

    public static DynamicLoadingLibrary open(String path) throws NativeMethodException {
        Preconditions.checkState(FileUtils.exists(path), "File %s doesn't exist.", path);

        Pointer handle = dlopen(path, ForeignValues.RTLD_LAZY);
        Logger.i("Loaded library " + path + ".At 0x" + Long.toHexString(handle.get()));
        return new DynamicLoadingLibrary(handle);
    }

    /**
     * Search Symbol in library
     *
     * @param symbol the symbol you want to search
     * @return The symbol address.
     * @throws NativeMethodException Symbol not found
     */
    public Pointer lookupSymbol(String symbol) throws NativeMethodException {
        Preconditions.checkNotNull(symbol, "Null symbol.");
        return cache.getIfPresent(symbol);
    }

    public static native Pointer dlopen(String path, int flags) throws NativeMethodException;

    public static native int dlclose(Pointer ptr);

    public static native Pointer dlsym(Pointer handle, String src) throws NativeMethodException;

    @Override
    public void onFree() {
        Logger.i("Release dll at " + libPointer);
        dlclose(libPointer);
    }

    static {
        System.loadLibrary("foreign");
    }
}
