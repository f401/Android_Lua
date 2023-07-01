package net.fred.lua.foreign;

import net.fred.lua.foreign.util.ForeignCloseable;
import net.fred.lua.foreign.util.Pointer;

public class MemorySegment extends ForeignCloseable {
    private long size;

    /**
     * See {@link MemorySegment#create}
     */
    private MemorySegment(Pointer src, long size) {
        super(src);
        this.size = size;
    }

    /**
     * Create a memory segment of size {@code size}.
     *
     * @param size The size of the memory segment needs to be created.
     * @return This object.
     * @throws NativeMethodException When creation fails
     */
    public static MemorySegment create(long size) throws NativeMethodException {
        long ptr = ForeignFunctions.alloc(size);
        if (ptr == ForeignValues.NULL) {
            throw new NativeMethodException(
                    "Failed to alloc size: " + size + ".Reason: " +
                            ForeignFunctions.strerror());
        }
        return new MemorySegment(new Pointer(ptr), size);
    }

    /**
     * Get the size of the segment.
     *
     * @return the size of the segment
     */
    public long size() {
        return size;
    }

}
