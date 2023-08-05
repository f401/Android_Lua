package net.fred.lua.foreign.internal;

import androidx.annotation.NonNull;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.types.Type;

public class MemorySegment extends BasicMemoryController {
    private final long size;

    /**
     * See {@link MemorySegment#create}
     */
    public MemorySegment(Pointer src, long size) {
        super(src);
        this.size = size;
    }

    /**
     * Create a memory segment of size {@code size}.
     *
     * @param size The size of the memory segment needs to be created.
     * @return This object.
     * @see MemorySegment#allocate
     */
    @NonNull
    public static MemorySegment create(long size) throws NativeMethodException {
        return new MemorySegment(allocate(size), size);
    }

    /**
     * Create a 'size' length and return a pointer to it.
     *
     * @param size The size needs to be created.
     * @return Pointer to.
     * @throws NativeMethodException    When creation fails
     * @throws IllegalArgumentException When {@code size} is less than or equal to 0.
     */
    @NonNull
    public static Pointer allocate(long size) throws NativeMethodException {
        ArgumentsChecker.checkNotLessZero((int) size);
        return ForeignFunctions.alloc(size);
    }

    /**
     * Get the size of the segment.
     *
     * @return the size of the segment
     */
    public long size() {
        return size;
    }

    public void put(long off, Pointer src) {
        ForeignFunctions.putPointer(pointer.plus(off), src);
    }

    public void put(long off, Type<?> type, Object obj) throws NativeMethodException {
        type.write(pointer.plus(off), obj);
    }

}
