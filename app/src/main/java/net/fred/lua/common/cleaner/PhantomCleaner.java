package net.fred.lua.common.cleaner;

import net.fred.lua.common.Logger;

import java.lang.ref.PhantomReference;

public class PhantomCleaner extends PhantomReference<Object> implements Cleanable {
    private final Runnable thunk;

    public PhantomCleaner(Object obj, Runnable thunk) {
        super(obj, Cleaner.getQueue());
        this.thunk = thunk;
    }

    @Override
    public void clean() {
        try {
            thunk.run();
        } catch (Throwable e) {
            Logger.e("Running cleaner ");
        }
    }
}
