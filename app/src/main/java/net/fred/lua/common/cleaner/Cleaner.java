package net.fred.lua.common.cleaner;

import net.fred.lua.io.Logger;

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
            thread.start();
        }
    }

    public static ReferenceCleanable createPhantom(Object obj, Cleanable cleaner) {
        return new PhantomCleaner(obj, cleaner);
    }

    protected static ReferenceQueue<Object> getQueue() {
        return queue;
    }

    private static void tryPending() {
        Reference<?> obj;
        while ((obj = queue.poll()) != null) {
            if (obj instanceof ReferenceCleanable) {
                ((ReferenceCleanable) obj).performCleanup();
            }
        }
    }

    public interface ReferenceCleanable {
        void performCleanup();
    }

    public interface Cleanable {
        void clean() throws Exception;
    }
}
