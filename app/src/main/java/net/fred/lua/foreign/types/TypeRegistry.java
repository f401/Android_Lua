package net.fred.lua.foreign.types;

import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.core.ForeignString;
import net.fred.lua.foreign.core.PrimaryTypes;

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

    private static final ConcurrentHashMap<Class<?>, TypeFactory<?>> typeFactoryMap =
            new ConcurrentHashMap<>();

    static {
        typeFactoryMap.put(short.class, PrimaryTypes.SHORT.getFactory());
        typeFactoryMap.put(int.class, PrimaryTypes.INT.getFactory());
        typeFactoryMap.put(long.class, PrimaryTypes.LONG.getFactory());
        typeFactoryMap.put(float.class, PrimaryTypes.FLOAT.getFactory());
        typeFactoryMap.put(double.class, PrimaryTypes.DOUBLE.getFactory());

        typeFactoryMap.put(ForeignString.class, ForeignString.ForeignStringType.FACTORY);
        typeFactoryMap.put(Pointer.class, Pointer.PointerType.FACTORY);
    }

    /**
     * Obtain the type corresponding to the class.
     * Note: The obtained type is immutable.
     * Return NULL for void.
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> getType(Class<T> target) {
        TypeFactory<?> typeFactory = typeFactoryMap.get(target);
        if (typeFactory == null) {
            // The type factory is null, we couldn't create.
            // Such as `void` type;
            return null;
        }

        return (Type<T>) typeFactory.create(0);
    }
}
