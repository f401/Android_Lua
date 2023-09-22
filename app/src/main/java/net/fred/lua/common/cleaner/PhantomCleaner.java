package net.fred.lua.common.cleaner;

import net.fred.lua.common.Logger;

import java.lang.ref.PhantomReference;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhantomCleaner extends PhantomReference<Object> implements Cleaner.Cleanable {
    private static final ConcurrentLinkedQueue<PhantomCleaner> workers = new ConcurrentLinkedQueue<>();
    private final Runnable thunk;

    public PhantomCleaner(Object obj, Runnable thunk) {
        super(obj, Cleaner.getQueue());
        this.thunk = thunk;
        workers.add(this);
    }

    @Override
    public void clean() {
        try {
            if (workers.remove(this)) {
                thunk.run();
            }
        } catch (Throwable e) {
            Logger.e("Running cleaner ");
        }
    }
}
