package net.fred.lua.foreign.types;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.ForeignFunctions;
import net.fred.lua.foreign.internal.ForeignValues;

import java.util.concurrent.ConcurrentHashMap;

public class TypesRegistry {
    public static final int SIZE_UNKNOWN = -1;
    private static final ConcurrentHashMap<Class<?>, Type<?>> typeMap;

    static {
        typeMap = new ConcurrentHashMap<>(6);
        typeMap.put(byte.class, Type.of(1, ForeignValues.FFI_TYPE_INT8,
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
        typeMap.put(short.class, Type.of(2, ForeignValues.FFI_TYPE_INT16,
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
        typeMap.put(int.class, Type.of(4, ForeignValues.FFI_TYPE_INT32,
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
        typeMap.put(long.class, Type.of(8, ForeignValues.FFI_TYPE_INT64,
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
        typeMap.put(Pointer.class, Type.of(8, ForeignValues.FFI_TYPE_POINTER,
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

        typeMap.put(String.class, Type.of(SIZE_UNKNOWN, ForeignValues.FFI_TYPE_POINTER, null));
        typeMap.put(void.class, Type.of(0, ForeignValues.FFI_TYPE_VOID, null));

    }

    /**
     * @param clazz The key.
     * @param <T>   the type of the class modeled by this Type.Types object. For example, the type of String.class is Type<String>. Use Type<?> if the class being modeled is unknown.
     * @return The result obtained, if unknown, returns @{code null}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> get(@NonNull Class<T> clazz) {
        ArgumentsChecker.check(typeMap.containsKey(clazz), "Class has not been registered yet. (" + clazz + ").");
        return (Type<T>) typeMap.get(clazz);
    }

    public static void register(@NonNull Class<?> clazz, @NonNull Type<?> ptr) {
        ArgumentsChecker.checkOrWarning(typeMap.containsKey(clazz), "The object has already been registered. Attempting to replace.");
        typeMap.put(clazz, ptr);
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
