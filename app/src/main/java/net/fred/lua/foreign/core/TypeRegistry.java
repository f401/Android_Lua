package net.fred.lua.foreign.core;

import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.types.Type;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains all registered types.
 * Registered types have corresponding classes.
 * For example:
 * {@code
 * int.class -> PrimaryTypeWrapper.of(int.class);
 * Pointer.class -> Pointer.PointerType;
 * ForeignString.class -> ForeignString.ForeignStringType;
 * }
 */
public class TypeRegistry {
    private static final ConcurrentHashMap<Class<?>, Type<?>> typeMap = new ConcurrentHashMap<>();

    static {
        typeMap.put(Pointer.class, PrimaryTypes.POINTER);
        typeMap.put(int.class, PrimaryTypes.INT);
        typeMap.put(void.class, PrimaryTypes.VOID);
        typeMap.put(ForeignString.class, PrimaryTypes.STRING);
    }

    /**
     * Obtain the type corresponding to the class.
     * Note: The obtained type is immutable.
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> getType(Class<T> target) {
        Type<T> result = (Type<T>) typeMap.get(target);
        if (result == null) {
            throw new RuntimeException("Couldn't find class " + target + ". Have you registered yet?");
        }
        return result;
    }
}
