package net.fred.lua.foreign;

import android.util.Log;

import net.fred.lua.foreign.child.SingleChildHolder;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Using reference counting to manage resources.
 */
public class SharedResource<T extends AutoCloseable> extends MemoryController {

    private final AtomicInteger refCount;

    protected SharedResource() {
        this.refCount = new AtomicInteger(1);
        setChildPolicy(new SingleChildHolder());
    }

    public static <T extends AutoCloseable> SharedResource<T> create(T resource) {
        SharedResource<T> result = new SharedResource<>();
        result.addChild(resource);
        return result;
    }

    @Override
    protected boolean askParentToAllowChildRelease(MemoryController ctl) {
        if (getResource() != ctl) {
            return true;
        }
        decreaseRefCount();
        return refCount.get() == 0 || isClosed();
    }

    public final void addRefCount() {
        refCount.getAndIncrement();
    }

    public final void decreaseRefCount() {
        if (refCount.decrementAndGet() == 0) {
            try {
                close(); // force close
            } catch (NativeMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final int getRefCount() {
        return refCount.get();
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
            Log.e("SharedResource", "Exception happened when releasing ", e);
        }
    }
}
