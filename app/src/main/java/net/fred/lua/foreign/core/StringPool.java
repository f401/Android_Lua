package net.fred.lua.foreign.core;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.child.IChildPolicy;
import net.fred.lua.foreign.child.RejectAllHolder;
import net.fred.lua.foreign.internal.MemoryController;

public class StringPool extends MemoryController {
    private static final String TAG = "StringPool";
    public static final StringPool GLOBAL = new StringPool(new RejectAllHolder());
    private final LoadingCache<String, StringReference> stringCache;

    public StringPool() {
        this(null);
    }

    private StringPool(@Nullable IChildPolicy policy) {
        this.stringCache = CacheBuilder.newBuilder().concurrencyLevel(8)
                .softValues()
                .maximumSize(45)
                .removalListener(new RemovalListener<String, StringReference>() {
                    @Override
                    public void onRemoval(
                            @NonNull RemovalNotification<String, StringReference> notification) {
                        removeChild(notification.getValue());
                    }
                })
                .build(new CacheLoader<String, StringReference>() {
                    @Override
                    public StringReference load(@NonNull String key) throws Exception {
                        StringReference ref = new StringReference(
                                ForeignString.from(StringPool.this, key));
                        StringPool.this.addChild(ref);
                        return ref;
                    }
                });
        if (policy != null) {
            setChildPolicy(policy);
        }
    }

    public ForeignString get(String str) {
        Preconditions.checkNotNull(str);
        return stringCache.getUnchecked(str);
    }

    @Override
    protected void onFree(boolean finalized) throws NativeMethodException {
        super.onFree(finalized);
        Log.i(TAG, "Releasing String pool contains " + stringCache.size());
        stringCache.cleanUp();
    }

    /**
     * 这个类的特别之处在于当调用close时不会立即释放,
     * 真正的释放靠垃圾回收器 finalize
     */
    public static class StringReference extends ForeignString {

        StringReference(ForeignString real) {
            super(real.getBasePointer(), real.getRefer());
        }

        @Override
        public void onFree(boolean finalized) throws NativeMethodException {
            // Do nothing here.
        }
    }
}
