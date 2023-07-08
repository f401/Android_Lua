package net.fred.lua.foreign;

import androidx.annotation.NonNull;

import net.fred.lua.foreign.util.ForeignCloseable;
import net.fred.lua.foreign.util.Pointer;

public class MemorySegment extends ForeignCloseable {
    private final long size;

    /**
     * See {@link MemorySegment#create}
     */
    protected MemorySegment(Pointer src, long size) {
        super(src);
        this.size = size;
    }

    /**
     * Create a memory segment of size {@code size}.
     *
     * @param size The size of the memory segment needs to be created.
     * @return This object.
     * @throws NativeMethodException    When creation fails
     * @throws IllegalArgumentException When {@code size} is less than or equal to 0.
     */
    @NonNull
    public static MemorySegment create(long size) throws NativeMethodException {
        if (size <= 0) {
            throw new IllegalArgumentException("`Size 'cannot be less than or equal to 0");
        }
        final long ptr = ForeignFunctions.alloc(size);
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
