package net.fred.lua.foreign.types;

import androidx.annotation.NonNull;

import net.fred.lua.foreign.MemorySegment;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.ffi.Types;

import java.util.RandomAccess;

public class Array<T> extends MemorySegment implements RandomAccess {

    private final Types.Type<T> clazz;

    private Array(long totalSize, long length, Types.Type<T> clazz) throws NativeMethodException {
        super(MemorySegment.allocate(totalSize), length);
        this.clazz = clazz;
    }

    @NonNull
    public static <T> Array<T> create(long length, @NonNull Class<T> clazz) throws NativeMethodException {
        if (length <= 0) {
            throw new IllegalArgumentException("The length cannot be less than or equal to 0. (" + length + ").");
        }
        Types.Type<T> type = Types.get(clazz);
        if (clazz == String.class) {
            throw new RuntimeException("Please use class `ForeignString` instead.");
        } else if (clazz == void.class) {
            throw new RuntimeException("Please use `Pointer` instead.");
        } else if (type.assignableReadable == null) {
            throw new RuntimeException("`Types.AssignableReadable` not implemented.");
        }
        return new Array<>(length * type.size, length, type);
    }

    public void insert(int index, T o) {
        clazz.assignableReadable.assign(pointer.plus(clazz, index), o);
    }

    public T get(int index) {
        return (T) clazz.assignableReadable.read(pointer.plus(clazz, index));
    }
}
