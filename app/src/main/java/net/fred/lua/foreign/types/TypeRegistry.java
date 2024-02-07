package net.fred.lua.foreign.types;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.fred.lua.common.Pair;
import net.fred.lua.common.utils.ThrowableUtils;
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

    // 高32位是feature, 低32是typeIndex
    private static final LoadingCache<Long, Type<?>> cachedTypes;

    static {
        cachedTypes = CacheBuilder.newBuilder()
                .concurrencyLevel(8)
                .softValues()
                .build(new CacheLoader<Long, Type<?>>() {

                    @Override
                    public Type<?> load(@NonNull Long key) {
                        TypeFactory<?> factory = getFactory((int) (key & TYPE_MASK));
                        Log.d("TypeRegistry", "Found Factory " + factory);
                        return factory != null ?
                                factory.create((int) (key >>> 32)) : null;
                    }
                });

        typeMap.put(Pointer.class, Pair.makePair(Pointer.PointerType.TYPE_INDEX, Pointer.PointerType.FACTORY));

        PrimaryTypeWrapper<Integer> intType = PrimaryTypeWrapper.of(int.class);
        typeMap.put(int.class, Pair.makePair(intType.getTypeIndex(), intType.getTypeFactory()));
        PrimaryTypeWrapper<Void> voidType = PrimaryTypeWrapper.of(void.class);
        typeMap.put(void.class, Pair.makePair(voidType.getTypeIndex(), voidType.getTypeFactory()));

        typeMap.put(ForeignString.class, Pair.makePair(ForeignString.ForeignStringType.TYPE_INDEX, ForeignString.ForeignStringType.FACTORY));
    }

    public static long makeKey(int typeIndex, int features) {
        return ((long) (typeIndex) & TYPE_MASK) +
                ((long) (features) << 32);
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

        return (Type<T>) cachedTypes.getUnchecked((long) (typePair.first) & TYPE_MASK);
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

    static Type<?> getOrLoad(int typeIdx, int feat) {
        return cachedTypes.getUnchecked(makeKey(typeIdx, feat));
    }

    public static int increaseAndGetTypeIdx() {
        int id = IncreaseHelper.INSTANCE.increase();
        Log.i("TypeRegistry", "Register new type " +
                ThrowableUtils.getCallerString() + " with type id " + id);
        return id;
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
