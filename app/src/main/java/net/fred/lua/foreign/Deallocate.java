package net.fred.lua.foreign;

import net.fred.lua.common.Flag;
import net.fred.lua.common.functional.Consumer;
import net.fred.lua.foreign.internal.MemorySegment;

public class Deallocate implements Runnable {
    public static final Consumer<Pointer> FREE = new Consumer<Pointer>() {
        @Override
        public void accept(Pointer pointer) {
            MemorySegment.free(pointer);
        }
    };
    private final Flag freed;
    private final Pointer pointer;
    private final Consumer<Pointer> cleaner;

    /**
     * @param freed   Reference to flag indicating whether to release or not.
     * @param pointer Pointer that needs to be released.
     * @param cleaner Execute on release.For example, Deallocate#FREE
     */
    public Deallocate(Flag freed, Pointer pointer, Consumer<Pointer> cleaner) {
        this.freed = freed;
        this.pointer = pointer;
        this.cleaner = cleaner;
    }

    @Override
    public void run() {
        if (!freed.getFlag()) {
            cleaner.accept(pointer);
        }
    }
}
