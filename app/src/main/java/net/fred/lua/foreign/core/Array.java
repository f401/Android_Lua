package net.fred.lua.foreign.core;

import androidx.annotation.NonNull;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.internal.MemorySegment;
import net.fred.lua.foreign.types.Type;
import net.fred.lua.foreign.types.TypesRegistry;

import java.util.RandomAccess;

public class Array<T> extends MemorySegment implements RandomAccess {

    private final Type<T> clazz;
    private final long length;

    private Array(long totalSize, long length, Type<T> clazz) throws NativeMethodException {
        super(MemorySegment.allocate(totalSize), length);
        this.clazz = clazz;
        this.length = length;
    }

    @NonNull
    public static <T> Array<T> create(long length, @NonNull Class<T> clazz) throws NativeMethodException {
        Type<T> type = TypesRegistry.get(clazz);
        ArgumentsChecker.checkNotLessZero((int) length);
        ArgumentsChecker.check(clazz != String.class, "Please use class `ForeignString` instead.");
        ArgumentsChecker.check(clazz == void.class, "Please use `Pointer` instead.");
        ArgumentsChecker.check(type.assignableReadable != null, "Types.AssignableReadable` not implemented.");
        return new Array<>(length * type.size, length, type);
    }

    public void insert(int index, T o) {
        ArgumentsChecker.checkIndex(index, length);
        clazz.assignableReadable.assign(pointer.plus(clazz, index), o);
    }

    public T get(int index) {
        ArgumentsChecker.checkIndex(index, length);
        return (T) clazz.assignableReadable.read(pointer.plus(clazz, index), clazz);
    }
}
