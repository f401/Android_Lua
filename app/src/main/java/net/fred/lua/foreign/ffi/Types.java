package net.fred.lua.foreign.ffi;

import net.fred.lua.foreign.ForeignValues;
import net.fred.lua.foreign.util.Pointer;

import java.util.concurrent.ConcurrentHashMap;

public class Types {
    private static final ConcurrentHashMap<Class<?>, Pointer> primaryTypes;
    private static ConcurrentHashMap<Class<?>, Pointer> customTypes;

    public static Pointer get(Class<?> clazz) {
        if (!primaryTypes.containsKey(clazz)) {
            return primaryTypes.get(clazz);
        }
        return customTypes.get(clazz);
    }

    public static void put(Class<?> clazz, Pointer ptr) {
        customTypes.put(clazz, ptr);
    }

    static {
        primaryTypes = new ConcurrentHashMap<>(6);
        primaryTypes.put(byte.class, Pointer.from(ForeignValues.FFI_TYPE_INT8));
        primaryTypes.put(short.class, Pointer.from(ForeignValues.FFI_TYPE_INT16));
        primaryTypes.put(int.class, Pointer.from(ForeignValues.FFI_TYPE_INT32));
        primaryTypes.put(long.class, Pointer.from(ForeignValues.FFI_TYPE_INT64));
        primaryTypes.put(Pointer.class, Pointer.from(ForeignValues.FFI_TYPE_POINTER));
        primaryTypes.put(String.class, Pointer.from(ForeignValues.FFI_TYPE_POINTER));

        customTypes = new ConcurrentHashMap<>();
    }
}
