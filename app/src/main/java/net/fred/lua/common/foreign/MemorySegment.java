package net.fred.lua.common.foreign;

public class MemorySegment implements AutoCloseable {
    private Pointer src;

    private MemorySegment(Pointer src) {
        this.src = src;
    }

    public static MemorySegment create(long size) {
        return new MemorySegment(new Pointer(alloc(size)));
    }

    private static native long alloc(long size);
    private static native void free(long ptr);

    @Override
    public void close() throws Exception {

    }
}
