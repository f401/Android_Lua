package net.fred.lua.foreign.memorypool;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import net.fred.lua.foreign.MemoryController;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.SharedResource;
import net.fred.lua.foreign.allocator.IAllocator;
import net.fred.lua.foreign.core.ForeignString;

// TODO: Impl using page size allocator
public class StringPool extends MemoryController {
    private static final String TAG = "StringPool";

    private final MyLruCache cache;
    private final IAllocator allocator;

    public StringPool(IAllocator allocator, int size) {
        this.cache = new MyLruCache(size);
        this.allocator = allocator;
    }

    public ForeignString getOrLoad(String javaString) {
        SharedResource<ForeignString> r = cache.get(javaString);
        r.addRefCount();
        return r.getResource();
    }

    @Override
    public void dispose(boolean finalized) throws NativeMethodException {
        super.dispose(finalized);
        cache.evictAll();
    }

    private class MyLruCache extends LruCache<String, SharedResource<ForeignString>> {
        /**
         * @param maxSize for caches that do not override {@link #sizeOf}, this is
         *                the maximum number of entries in the cache. For all other caches,
         *                this is the maximum sum of the sizes of the entries in this cache.
         */
        public MyLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected void entryRemoved(boolean evicted, @NonNull String key, @NonNull SharedResource<ForeignString> oldValue, @Nullable SharedResource<ForeignString> newValue) {
            Log.d(TAG, "Released 1 entry " + oldValue);
            oldValue.decreaseRefCount();
        }

        @Nullable
        @Override
        protected SharedResource<ForeignString> create(@NonNull String key) {
            try {
                return SharedResource.create(ForeignString.from(allocator, key));
            } catch (NativeMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
