package net.fred.lua.foreign.internal;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.types.Type;

public class MemorySegment extends BasicMemoryController {
    private final long size;
    private final CheckedMemoryAccessor checkedMemoryAccessor;

    /**
     * See {@link MemorySegment#create}
     */
    public MemorySegment(Pointer src, long size) {
        super(src);
        this.size = size;
        checkedMemoryAccessor = new CheckedMemoryAccessor(src, size);
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
        Preconditions.checkPositionIndex((int) size, Integer.MAX_VALUE);
        return alloc(size);
    }

    /**
     * Get the size of the segment.
     *
     * @return the size of the segment
     */
    public long size() {
        return size;
    }

    public void put(long off, Type<?> type, Object obj) throws NativeMethodException {
        type.write(checkedMemoryAccessor, getPointer().plus(off), obj);
    }

    public void put(long off, Pointer src) {
        checkedMemoryAccessor.putPointer(getPointer().plus(off), src);
    }

    @Override
    public void onFree() throws NativeMethodException {
        super.onFree();
        free(getPointer());
    }

    protected static native Pointer alloc(long size) throws NativeMethodException;

    public static native void free(Pointer ptr);

    public static native void memcpy(Pointer dest, Pointer src, long length);

}
