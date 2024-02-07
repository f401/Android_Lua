package net.fred.lua.foreign.internal;

import androidx.annotation.NonNull;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.allocate.DefaultAllocator;
import net.fred.lua.foreign.allocate.IAllocator;
import net.fred.lua.foreign.types.Type;

public class MemorySegment extends MemoryController {
    private final long size;
    private final CheckedMemoryAccessor checkedMemoryAccessor;
    private final Pointer base;

    /**
     * See {@link MemorySegment#create}
     */
    public MemorySegment(Pointer src, long size) {
        this.base = src;
        this.size = size;
        checkedMemoryAccessor = new CheckedMemoryAccessor(src, size);
    }

    /**
     * Create a memory segment of size {@code size}.
     *
     * @param size      The size of the memory segment needs to be created.
     * @param allocator The memory allocator.
     * @return This object.
     * @see #alloc
     * @see IAllocator#allocateMemory
     */
    @NonNull
    public static MemorySegment create(IAllocator allocator, long size) throws NativeMethodException {
        return new MemorySegment(allocator.allocateMemory(size), size);
    }

    @NonNull
    public static MemorySegment create(long size) throws NativeMethodException {
        return create(DefaultAllocator.INSTANCE, size);
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
        type.write(checkedMemoryAccessor, getBasePointer().plus(off), obj);
    }

    public void put(long off, Pointer src) {
        checkedMemoryAccessor.putPointer(getBasePointer().plus(off), src);
    }

    @Override
    public void onFree(boolean finalized) throws NativeMethodException {
        super.onFree(finalized);
        free(getBasePointer());
    }

    public Pointer getBasePointer() {
        return base;
    }

    public static native Pointer alloc(long size) throws NativeMethodException;

    public static native void free(Pointer ptr);

    public static native void memcpy(Pointer dest, Pointer src, long length);

}
