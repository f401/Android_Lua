package net.fred.lua.common.cleaner;

import net.fred.lua.common.Logger;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.TimeUnit;

public class Cleaner {
    private static ReferenceQueue<Object> queue;

    public static void init() {
        if (queue == null) {
            queue = new ReferenceQueue<>();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (; ; ) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            Logger.e("clean thread interrupted");
                            break;
                        }
                        tryPending();
                    }
                }
            }, "Cleaner");
            thread.setDaemon(true);
            thread.setPriority(3);
            thread.start();
        }
    }

    public static PhantomCleaner createPhantom(Object obj, Runnable cleaner) {
        return new PhantomCleaner(obj, cleaner);
    }

    protected static ReferenceQueue<Object> getQueue() {
        return queue;
    }

    private static void tryPending() {
        Reference<?> obj;
        while ((obj = queue.poll()) != null) {
            if (obj instanceof Cleanable) {
                ((Cleanable) obj).clean();
            }
        }
    }
}
