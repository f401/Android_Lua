package net.fred.lua.foreign;

import android.util.Log;

import net.fred.lua.foreign.child.OneChildHolder;
import net.fred.lua.foreign.internal.MemoryController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Using reference counting to manage resources.
 */
public class SharedResource<T extends AutoCloseable> extends MemoryController {

    private final AtomicInteger refCount;

    protected SharedResource() {
        this.refCount = new AtomicInteger(1);
        setChildPolicy(new OneChildHolder());
    }

    public static <T extends AutoCloseable> SharedResource<T> create(T resource) {
        SharedResource<T> result = new SharedResource<>();
        result.addChild(resource);
        return result;
    }

    @Override
    protected boolean askParentToAllowChildRelease() {
        decreaseRefCount();
        return refCount.get() == 0 || isClosed();
    }

    public final void addRefCount() {
        refCount.getAndIncrement();
    }

    public final void decreaseRefCount() {
        refCount.decrementAndGet();
    }

    @SuppressWarnings("unchecked")
    public final T getResource() {
        return (T) getChildPolicy().childAt(0);
    }

    @Override
    protected void onDetachParent() {
        try {
            close();
        } catch (NativeMethodException e) {
            Log.e("SharedResouce", "Exception happened when releasing ", e);
        }
    }
}
