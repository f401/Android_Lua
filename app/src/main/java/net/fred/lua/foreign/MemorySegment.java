package net.fred.lua.foreign;

import androidx.annotation.NonNull;

import net.fred.lua.foreign.allocator.DefaultAllocator;
import net.fred.lua.foreign.allocator.IAllocator;
import net.fred.lua.foreign.types.Type;

public class MemorySegment extends ResourceWrapper {
    private final CheckedMemoryAccessor checkedMemoryAccessor;

    /**
     * See {@link MemorySegment#create}
     */
    public MemorySegment(Resource wrapper) {
        super(wrapper);
        checkedMemoryAccessor = new CheckedMemoryAccessor(wrapper.getBasePointer(), wrapper.size());
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
        return new MemorySegment(allocator.allocateMemory(size));
    }

    @NonNull
    public static MemorySegment create(long size) throws NativeMethodException {
        return create(DefaultAllocator.INSTANCE, size);
    }

    public void put(long off, Type<?> type, Object obj) throws NativeMethodException {
        type.write(checkedMemoryAccessor, getBasePointer().plus(off), obj);
    }

    public void put(long off, Pointer src) {
        checkedMemoryAccessor.putPointer(getBasePointer().plus(off), src);
    }

    public static native Pointer alloc(long size) throws NativeMethodException;

    public static native void free(Pointer ptr);

    public static native void memcpy(Pointer dest, Pointer src, long length);

}
