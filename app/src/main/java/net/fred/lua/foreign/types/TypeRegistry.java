package net.fred.lua.foreign.types;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import net.fred.lua.common.Pair;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.core.ForeignString;
import net.fred.lua.foreign.core.PrimaryTypeWrapper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static final long TYPE_MASK = 0xffffffffL;

    private static final ConcurrentHashMap<Class<?>, Pair<Integer, TypeFactory<?>>> typeMap =
            new ConcurrentHashMap<>();

    //
    private static final Cache<Long, Type<?>> cachedTypes;

    static {
        cachedTypes = CacheBuilder.newBuilder()
                .concurrencyLevel(8)
                .softValues()
                .build(new CacheLoader<Long, Type<?>>() {

                    @Override
                    public Type<?> load(@NonNull Long key) {
                        TypeFactory<?> factory = getFactory((int) (key & TYPE_MASK));
                        return factory != null ?
                                factory.create((int) (key << 32)) : null;
                    }
                });

        typeMap.put(Pointer.class, Pair.makePair(Pointer.PointerType.TYPE_INDEX, Pointer.PointerType.FACTORY));

        PrimaryTypeWrapper<Integer> intType = PrimaryTypeWrapper.of(int.class);
        typeMap.put(int.class, Pair.makePair(intType.getTypeIndex(), intType.getTypeFactory()));
        PrimaryTypeWrapper<Void> voidType = PrimaryTypeWrapper.of(void.class);
        typeMap.put(void.class, Pair.makePair(voidType.getTypeIndex(), voidType.getTypeFactory()));

        typeMap.put(ForeignString.class, Pair.makePair(ForeignString.ForeignStringType.TYPE_INDEX, ForeignString.ForeignStringType.FACTORY));
    }

    /**
     * Obtain the type corresponding to the class.
     * Note: The obtained type is immutable.
     * Return NULL for void.
     */
    @SuppressWarnings("unchecked")
    public static <T> Type<T> getType(Class<T> target) {
        Pair<Integer, TypeFactory<?>> typePair = typeMap.get(target);
        if (typePair == null) {
            throw new RuntimeException("Couldn't find class " + target + ". Have you registered yet?");
        }

        TypeFactory<?> typeFactory = typePair.second;
        if (typeFactory == null) {
            // The type factory is null, we couldn't create.
            // Such as `void` type;
            return null;
        }

        Type<T> cached = (Type<T>) cachedTypes.getIfPresent(Long.valueOf(typePair.first));
        if (cached == null) {
            // Here we should create needle.
            Type<T> result = (Type<T>) typeFactory.create(0);
            cachedTypes.put((long) typePair.first, result);
            return result;
        }
        // Found it , just return.
        return cached;
    }

    /**
     * NOTE: Return NULL for void !!!
     */
    @Nullable
    private static TypeFactory<?> getFactory(int typeIndex) {
        for (Pair<Integer, TypeFactory<?>> entry : typeMap.values()) {
            if (entry.first == typeIndex) {
                return entry.second;
            }
        }
        throw new RuntimeException("Can't find factory for " + typeIndex);
    }

    static Type<?> getOrLoad(long key) {
        Type<?> cached = cachedTypes.getIfPresent(key);
        if (cached != null) return cached;

        TypeFactory<?> factory = getFactory((int) (key & TYPE_MASK));
        if (factory == null) {
            // The type factory is null, we couldn't create.
            // Such as `void` type;
            return null;
        }

        Type<?> result = factory.create((int) (key << 32));
        cachedTypes.put(key, result);
        return result;
    }

    public static int increaseAndGetTypeIdx() {
        return IncreaseHelper.INSTANCE.increase();
    }

    private enum IncreaseHelper {
        INSTANCE;
        private final AtomicInteger current;

        IncreaseHelper() {
            current = new AtomicInteger();
        }

        public int increase() {
            return current.getAndIncrement();
        }
    }
}
