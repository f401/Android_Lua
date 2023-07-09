package net.fred.lua.foreign.ffi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.ForeignFunctions;
import net.fred.lua.foreign.ForeignValues;
import net.fred.lua.foreign.util.Pointer;

import java.util.concurrent.ConcurrentHashMap;

public class Types {
    public static final int SIZE_UNKNOWN = -1;
    private static final ConcurrentHashMap<Class<?>, Type<?>> primaryTypes;
    private static final ConcurrentHashMap<Class<?>, Type<?>> customTypes;

    static {
        primaryTypes = new ConcurrentHashMap<>(6);
        primaryTypes.put(byte.class, Type.of(1, ForeignValues.FFI_TYPE_INT8,
                new AssignableReadable<Byte>() {
                    @Override
                    public void assign(Pointer pointer, Object value) {
                        ForeignFunctions.putByte(pointer.get(), (Byte) value);
                    }

                    @Override
                    public Byte read(Pointer pointer) {
                        return ForeignFunctions.peekByte(pointer.get());
                    }
                }));
        primaryTypes.put(short.class, Type.of(2, ForeignValues.FFI_TYPE_INT16,
                new AssignableReadable<Short>() {
                    @Override
                    public void assign(Pointer pointer, Object value) {
                        ForeignFunctions.putShort(pointer.get(), (Short) value);
                    }

                    @Override
                    public Short read(Pointer pointer) {
                        return ForeignFunctions.peekShort(pointer.get());
                    }
                }));
        primaryTypes.put(int.class, Type.of(4, ForeignValues.FFI_TYPE_INT32,
                new AssignableReadable<Integer>() {
                    @Override
                    public void assign(Pointer pointer, Object value) {
                        ForeignFunctions.putInt(pointer.get(), (Integer) value);
                    }

                    @Override
                    public Integer read(Pointer pointer) {
                        return ForeignFunctions.peekInt(pointer.get());
                    }
                }));
        primaryTypes.put(long.class, Type.of(8, ForeignValues.FFI_TYPE_INT64,
                new AssignableReadable<Long>() {
                    @Override
                    public void assign(Pointer pointer, Object value) {
                        ForeignFunctions.putLong(pointer.get(), (Long) value);
                    }

                    @Override
                    public Long read(Pointer pointer) {
                        return ForeignFunctions.peekLong(pointer.get());
                    }
                }));
        primaryTypes.put(Pointer.class, Type.of(8, ForeignValues.FFI_TYPE_POINTER,
                new AssignableReadable<Pointer>() {
                    @Override
                    public void assign(Pointer pointer, Object value) {
                        ForeignFunctions.putLong(pointer.get(), ((Pointer) value).get());
                    }

                    @Override
                    public Pointer read(Pointer pointer) {
                        return Pointer.from(ForeignFunctions.peekLong(pointer.get()));
                    }
                }));

        primaryTypes.put(String.class, Type.of(SIZE_UNKNOWN, ForeignValues.FFI_TYPE_POINTER, null));
        primaryTypes.put(void.class, Type.of(0, ForeignValues.FFI_TYPE_VOID, null));

        customTypes = new ConcurrentHashMap<>();
    }

    public static Type<?> get(@NonNull Class<?> clazz) {
        if (!primaryTypes.containsKey(clazz)) {
            return primaryTypes.get(clazz);
        }
        return customTypes.get(clazz);
    }

    public static void put(@NonNull Class<?> clazz, @NonNull Type<?> ptr) {
        customTypes.put(clazz, ptr);
    }

    public interface AssignableReadable<T> {
        void assign(Pointer pointer, Object value);

        T read(Pointer pointer);
    }

    public static class Type<T> {
        public final int size;
        public final Pointer pointer;
        public final AssignableReadable<T> assignableReadable;

        public Type(int size, @NonNull Pointer pointer, @Nullable AssignableReadable<T> assignableReadable) {
            this.size = size;
            this.pointer = pointer;
            this.assignableReadable = assignableReadable;
        }

        @NonNull
        public static <T> Type<T> of(int size, long address, @Nullable AssignableReadable<T> assignableReadable) {
            return new Type<>(size, Pointer.from(address), assignableReadable);
        }
    }
}
