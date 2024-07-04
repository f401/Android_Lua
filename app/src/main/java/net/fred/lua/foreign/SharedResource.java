package net.fred.lua.foreign;

import android.util.Log;

import net.fred.lua.foreign.child.SingleChildHolder;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Using reference counting to manage resources.
 */
public class SharedResource<T extends AutoCloseable> extends MemoryController {

    private final AtomicInteger mRefCount;

    protected SharedResource() {
        this.mRefCount = new AtomicInteger(1);
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
        return mRefCount.get() == 0 || isClosed();
    }

    public final void addRefCount() {
        mRefCount.getAndIncrement();
    }

    public final void decreaseRefCount() {
        if (mRefCount.decrementAndGet() == 0) {
            try {
                close(); // force close
            } catch (NativeMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final int getRefCount() {
        return mRefCount.get();
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
