package net.fred.lua.foreign.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.common.Logger;
import net.fred.lua.common.utils.StringUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.foreign.internal.MemoryAccessor;
import net.fred.lua.foreign.internal.MemorySegment;
import net.fred.lua.foreign.types.Type;

/**
 * Array of native layers.
 *
 * @param <T> This type must be sized.
 */
public class Array<T> extends MemorySegment {

    private final Type<T> mType;

    protected Array(Pointer src, long len, Type<T> type) {
        super(src, len);
        this.mType = type;
    }

    public static <T> Array<T> create(Type<T> type, long length) throws NativeMethodException {
        // Must be sized
        long totalSize = length * type.getSize(null);
        Pointer ptr = MemorySegment.allocate(totalSize);
        Logger.i(StringUtils.templateOf("Create {} size of Segment at {}.", totalSize, ptr));
        return new Array<>(ptr, length, type);
    }

    /**
     * Create type of array.
     * Pointer in the native layer.
     *
     * @param type Null when it does not need to be read.
     * @param size 0 when it does not need to be read.
     * @param <T>  the type.
     * @return The ArrayType.
     */
    public static <T> ArrayType<T> ofType(@Nullable Type<T> type, long size) {
        return new ArrayType<>(type, size);
    }

    public Type<T> getType() {
        return this.mType;
    }

    private Pointer evalDataOff(int idx) {
        return pointer.plus((long) idx * mType.getSize(null));
    }

    public int evalTotalSize() {
        return (int) this.size() * this.mType.getSize(null);
    }

    public void write(int idx, T data) throws NativeMethodException {
        Pointer off = evalDataOff(idx);
        mType.write(off, data);
        Logger.i(StringUtils.templateOf("Write {}, at {}.", data, off));
    }

    public T get(int idx) {
        return mType.read(evalDataOff(idx));
    }

    public static class ArrayType<T> implements Type<Array<T>> {
        private final Type<T> type;
        private final long size;

        protected ArrayType(Type<T> type, long size) {
            this.size = size;
            this.type = type;
        }

        @Override
        public int getSize(@Nullable Object obj) {
            return (int) ForeignValues.SIZE_OF_POINTER;
        }

        @Nullable
        @Override
        public Pointer getFFIPointer() {
            return ForeignValues.FFI_TYPE_POINTER;
        }

        @Override
        public Array<T> read(@NonNull Pointer dest) {
            ArgumentsChecker.checkNotNull(type, "Cannot read when type is null.");
            return new Array<>(dest, size, type);
        }

        @Override
        public void write(@NonNull Pointer dest, @NonNull Object data) throws NativeMethodException {
            MemoryAccessor.putPointerUnchecked(dest, ((Array<?>) data).getPointer());
        }
    }
}
