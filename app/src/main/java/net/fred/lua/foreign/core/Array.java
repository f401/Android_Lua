package net.fred.lua.foreign.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.Logger;
import net.fred.lua.common.utils.StringUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.ForeignFunctions;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.foreign.internal.MemorySegment;
import net.fred.lua.foreign.types.PointerTypeImpl;
import net.fred.lua.foreign.types.Type;

import java.util.Objects;

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

    public static <T> ArrayType<T> ofType() {
        return new ArrayType<>();
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

    public static class ArrayType<T> extends PointerTypeImpl<Array<T>> {
        protected ArrayType() {
            super(true);
        }

        @Override
        public int getSize(@Nullable Object obj) {
            if (writeAsPointer) {
                return (int) ForeignValues.SIZE_OF_POINTER;
            }
            Objects.requireNonNull(obj);
            return evalTotalSize(obj);
        }

        @Nullable
        @Override
        public Pointer getFFIPointer() {
            return writeAsPointer ? Pointer.from(ForeignValues.FFI_TYPE_POINTER) : null;
        }

        @Override
        public Array<T> read(@NonNull Pointer dest) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(@NonNull Pointer dest, @NonNull Object data) throws NativeMethodException {
            if (writeAsPointer) {
                ForeignFunctions.putPointer(dest, ((Array<?>) data).getPointer());
            } else {
                ForeignFunctions.memcpy(dest, ((Array<?>) data).getPointer(), evalTotalSize(data));
            }
        }

        private int evalTotalSize(Object array) {
            return ((Array<?>) array).evalTotalSize();
        }
    }
}
