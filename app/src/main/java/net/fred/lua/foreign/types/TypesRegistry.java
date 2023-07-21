package net.fred.lua.foreign.types;

import androidx.annotation.NonNull;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.ForeignFunctions;
import net.fred.lua.foreign.internal.ForeignValues;

import java.util.concurrent.ConcurrentHashMap;

public class TypesRegistry {
    public static final int SIZE_UNKNOWN = -1;
    private static final ConcurrentHashMap<Class<?>, Type<?>> classesMap;

    static {
        classesMap = new ConcurrentHashMap<>(6);
        classesMap.put(byte.class, Type.of(1, ForeignValues.FFI_TYPE_INT8,
                new AssignableReadable<Byte>() {
                    @Override
                    public void assign(Pointer pointer, Object value) {
                        ForeignFunctions.putByte(pointer, (Byte) value);
                    }

                    @Override
                    public Byte read(Pointer pointer, Type<Byte> clazz) {
                        return ForeignFunctions.peekByte(pointer);
                    }
                }));
        classesMap.put(short.class, Type.of(2, ForeignValues.FFI_TYPE_INT16,
                new AssignableReadable<Short>() {
                    @Override
                    public void assign(Pointer pointer, Object value) {
                        ForeignFunctions.putShort(pointer, (Short) value);
                    }

                    @Override
                    public Short read(Pointer pointer, Type<Short> clazz) {
                        return ForeignFunctions.peekShort(pointer);
                    }
                }));
        classesMap.put(int.class, Type.of(4, ForeignValues.FFI_TYPE_INT32,
                new AssignableReadable<Integer>() {
                    @Override
                    public void assign(Pointer pointer, Object value) {
                        ForeignFunctions.putInt(pointer, (Integer) value);
                    }

                    @Override
                    public Integer read(Pointer pointer, Type<Integer> clazz) {
                        return ForeignFunctions.peekInt(pointer);
                    }
                }));
        classesMap.put(long.class, Type.of(8, ForeignValues.FFI_TYPE_INT64,
                new AssignableReadable<Long>() {
                    @Override
                    public void assign(Pointer pointer, Object value) {
                        ForeignFunctions.putLong(pointer, (Long) value);
                    }

                    @Override
                    public Long read(Pointer pointer, Type<Long> clazz) {
                        return ForeignFunctions.peekLong(pointer);
                    }
                }));
        classesMap.put(Pointer.class, Type.of(8, ForeignValues.FFI_TYPE_POINTER,
                new AssignableReadable<Pointer>() {
                    @Override
                    public void assign(Pointer pointer, Object value) {
                        ForeignFunctions.putLong(pointer, ((Pointer) value).get());
                    }

                    @Override
                    public Pointer read(Pointer pointer, Type<Pointer> clazz) {
                        return Pointer.from(ForeignFunctions.peekLong(pointer));
                    }
                }));

        classesMap.put(String.class, Type.of(SIZE_UNKNOWN, ForeignValues.FFI_TYPE_POINTER, null));
        classesMap.put(void.class, Type.of(0, ForeignValues.FFI_TYPE_VOID, null));
    }

    /**
     * @param clazz The key.
     * @param <T>   the type of the class modeled by this Type.Types object. For example, the type of String.class is Type<String>. Use Type<?> if the class being modeled is unknown.
     * @return The result obtained, if unknown, returns @{code null}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> get(@NonNull Class<T> clazz) {
        ArgumentsChecker.check(classesMap.containsKey(clazz), "Class has not been registered yet. (" + clazz + ").");
        return (Type<T>) classesMap.get(clazz);
    }

    public static void register(@NonNull Class<?> clazz, @NonNull Type<?> ptr) {
        ArgumentsChecker.checkOrWarning(classesMap.containsKey(clazz), "The object has already been registered. Attempting to replace.");
        classesMap.put(clazz, ptr);
    }

    public interface AssignableReadable<T> {
        void assign(Pointer pointer, Object value);

        T read(Pointer pointer, Type<T> clazz);
    }

}
