package net.fred.lua.foreign.core;

import android.util.Log;

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

import java.util.concurrent.TimeUnit;

public final class DynamicLoadingLibrary extends MemoryController {
    private static final String TAG = "DLL";
    private static final LoadingCache<String, DynamicLoadingLibrary> openCache = CacheBuilder.newBuilder()
            .softValues()
            .build(new CacheLoader<String, DynamicLoadingLibrary>() {
                @Override
                public DynamicLoadingLibrary load(@NonNull String key) throws Exception {
                    Pointer handle = dlopen(key, ForeignValues.RTLD_LAZY);
                    Log.i(TAG, "Loaded library " + key + ".At 0x" + Long.toHexString(handle.get()));
                    return new DynamicLoadingLibrary(handle);
                }
            });
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
                        Log.d(TAG, "Loading symbol " + key);
                        return dlsym(libPointer, key);
                    }
                });
    }

    public static DynamicLoadingLibrary open(String path) throws NativeMethodException {
        Preconditions.checkState(FileUtils.exists(path), "File %s doesn't exist.", path);
        return openCache.getUnchecked(path);
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
        return cache.getUnchecked(symbol);
    }

    public static native Pointer dlopen(String path, int flags) throws NativeMethodException;

    public static native int dlclose(Pointer ptr);

    public static native Pointer dlsym(Pointer handle, String src) throws NativeMethodException;

    @Override
    public void onFree(boolean finalized) {
        Log.i(TAG, "Release dll at " + libPointer);
        dlclose(libPointer);
    }

    static {
        System.loadLibrary("foreign");
    }
}
