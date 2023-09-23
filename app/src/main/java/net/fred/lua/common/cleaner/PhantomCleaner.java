package net.fred.lua.common.cleaner;

import net.fred.lua.common.Logger;

import java.lang.ref.PhantomReference;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhantomCleaner extends PhantomReference<Object> implements Cleaner.ReferenceCleanable {
    private static final ConcurrentLinkedQueue<PhantomCleaner> workers = new ConcurrentLinkedQueue<>();
    private final Cleaner.Cleanable thunk;

    public PhantomCleaner(Object obj, Cleaner.Cleanable thunk) {
        super(obj, Cleaner.getQueue());
        this.thunk = thunk;
        workers.add(this);
    }

    @Override
    public void performCleanup() {
        try {
            if (workers.remove(this)) {
                thunk.clean();
            }
        } catch (Throwable e) {
            Logger.e("Running cleaner ");
        }
    }
}
