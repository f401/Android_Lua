package net.fred.lua.foreign;

import net.fred.lua.common.Flag;

public class MemorySegment implements AutoCloseable {
    private Pointer src;
    private Flag freed;
    private long size;

    private MemorySegment(Pointer src, long size) {
        this.src = src;
        this.size = size;
        this.freed = new Flag(false);
    }

    public static MemorySegment create(long size) {
        return new MemorySegment(new Pointer(ForeignFunctions.alloc(size)), size);
    }

    /**
     * Get the size of the segment
     * @return the size of the segment
     */
    public long size() {
        return size;
    }

    public Pointer getPointer() {
        return src;
    }

    @Override
    public void close() throws Exception {
        if (!this.freed.getFlag()) {
            ForeignFunctions.free(src.get());
            freed.setFlag(true);
        } else {
            throw new RuntimeException("Pointer freed twice!");
        }
    }
}
