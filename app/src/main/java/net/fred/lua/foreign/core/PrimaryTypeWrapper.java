package net.fred.lua.foreign.core;

import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_DOUBLE;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_FLOAT;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT16;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT32;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT64;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT8;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UINT16;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UINT32;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UINT64;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UINT8;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_VOID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.allocator.IAllocator;
import net.fred.lua.foreign.internal.MemoryAccessor;
import net.fred.lua.foreign.types.CommonFeatures;
import net.fred.lua.foreign.types.Type;
import net.fred.lua.foreign.types.TypeFactory;

/**
 * Packaging for basic types.
 *
 * @param <T> Basic types of packaging required.
 */
final class PrimaryTypeWrapper<T> extends Type<T> {
    private static final ImmutableMap<Class<?>, PrimaryType<?>> map;

    static {
        ImmutableMap.Builder<Class<?>, PrimaryType<?>> builder = ImmutableMap.builder();
        builder.put(byte.class, new PrimaryType<Byte>() {
            private final TypeFactory<Type<Byte>> FACTORY = new TypeFactory<Type<Byte>>() {
                @Override
                public Type<Byte> create(int feature) {
                    return (feature & CommonFeatures.UNSIGNED) > 0 ?
                            PrimaryTypes.UNSIGNED_BYTE : PrimaryTypes.BYTE;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putByte(dest, (byte) obj);
            }

            @Override
            public TypeFactory<Type<Byte>> getTypeFactory() {
                return FACTORY;
            }

            @Override
            public Pointer getSignedFFIPointer() {
                return FFI_TYPE_INT8;
            }

            @Override
            public Pointer getUnsignedFFIPointer() {
                return FFI_TYPE_UINT8;
            }

            @Override
            public int getSize() {
                return 1;
            }

            @Override
            public Byte read(MemoryAccessor accessor, @NonNull Pointer dest) {
                return accessor.peekByte(dest);
            }
        });
        builder.put(short.class, new PrimaryType<Short>() {
            private final TypeFactory<Type<Short>> FACTORY = new TypeFactory<Type<Short>>() {
                @Override
                public Type<Short> create(int feature) {
                    return (feature & CommonFeatures.UNSIGNED) > 0 ?
                            PrimaryTypes.UNSIGNED_SHORT : PrimaryTypes.SHORT;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putShort(dest, (short) obj);
            }

            @Override
            public TypeFactory<Type<Short>> getTypeFactory() {
                return FACTORY;
            }

            @Override
            public Pointer getSignedFFIPointer() {
                return FFI_TYPE_INT16;
            }

            @Override
            public Pointer getUnsignedFFIPointer() {
                return FFI_TYPE_UINT16;
            }

            @Override
            public int getSize() {
                return 2;
            }

            @Override
            public Short read(MemoryAccessor accessor, @NonNull Pointer dest) {
                return accessor.peekShort(dest);
            }
        });
        builder.put(int.class, new PrimaryType<Integer>() {
            private final TypeFactory<Type<Integer>> FACTORY = new TypeFactory<Type<Integer>>() {
                @Override
                public Type<Integer> create(int feature) {
                    return (feature & CommonFeatures.UNSIGNED) != 0 ?
                            PrimaryTypes.UNSIGNED_INT : PrimaryTypes.INT;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putInt(dest, (int) obj);
            }

            @Override
            public TypeFactory<Type<Integer>> getTypeFactory() {
                return FACTORY;
            }

            @Override
            public Pointer getSignedFFIPointer() {
                return FFI_TYPE_INT32;
            }

            @Override
            public Pointer getUnsignedFFIPointer() {
                return FFI_TYPE_UINT32;
            }

            @Override
            public int getSize() {
                return 4;
            }

            @Override
            public Integer read(MemoryAccessor accessor, @NonNull Pointer dest) {
                return accessor.peekInt(dest);
            }
        });
        builder.put(long.class, new PrimaryType<Long>() {
            private final TypeFactory<Type<Long>> FACTORY = new TypeFactory<Type<Long>>() {
                @Override
                public Type<Long> create(int feature) {
                    return (feature & CommonFeatures.UNSIGNED) > 0 ?
                            PrimaryTypes.UNSIGNED_LONG : PrimaryTypes.LONG;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putLong(dest, (long) obj);
            }

            @Override
            public TypeFactory<Type<Long>> getTypeFactory() {
                return FACTORY;
            }

            @Override
            public Pointer getSignedFFIPointer() {
                return FFI_TYPE_INT64;
            }

            @Override
            public Pointer getUnsignedFFIPointer() {
                return FFI_TYPE_UINT64;
            }

            @Override
            public int getSize() {
                return 8;
            }

            @Override
            public Long read(MemoryAccessor accessor, @NonNull Pointer dest) {
                return accessor.peekLong(dest);
            }
        });
        builder.put(void.class, new PrimaryType<Void>() {
            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                throw new UnsupportedOperationException();
            }

            @Override
            public TypeFactory<Type<Void>> getTypeFactory() {
                return null;
            }

            @Override
            public Pointer getSignedFFIPointer() {
                return FFI_TYPE_VOID;
            }

            @Override
            public Pointer getUnsignedFFIPointer() {
                return FFI_TYPE_VOID;
            }

            @Override
            public int getSize() {
                return 0;
            }

            @Override
            public Void read(MemoryAccessor accessor, @NonNull Pointer dest) {
                return null;
            }
        });
        builder.put(float.class, new PrimaryType<Float>() {
            private final TypeFactory<Type<Float>> FACTORY = new TypeFactory<Type<Float>>() {
                @Override
                public Type<Float> create(int feature) {
                    return PrimaryTypes.FLOAT;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putFloat(dest, (Float) obj);
            }

            @Override
            public TypeFactory<Type<Float>> getTypeFactory() {
                return FACTORY;
            }

            @Override
            public Pointer getSignedFFIPointer() {
                return FFI_TYPE_FLOAT;
            }

            @Override
            public Pointer getUnsignedFFIPointer() {
                return FFI_TYPE_FLOAT;
            }

            @Override
            public int getSize() {
                return 4;
            }

            @Override
            public Float read(MemoryAccessor accessor, @NonNull Pointer dest) {
                return accessor.peekFloat(dest);
            }
        });

        builder.put(double.class, new PrimaryType<Double>() {
            private final TypeFactory<Type<Double>> FACTORY = new TypeFactory<Type<Double>>() {
                @Override
                public Type<Double> create(int feature) {
                    return PrimaryTypes.DOUBLE;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putDouble(dest, (double) obj);
            }

            @Override
            public TypeFactory<Type<Double>> getTypeFactory() {
                return FACTORY;
            }

            @Override
            public Pointer getSignedFFIPointer() {
                return FFI_TYPE_DOUBLE;
            }

            @Override
            public Pointer getUnsignedFFIPointer() {
                return FFI_TYPE_DOUBLE;
            }

            @Override
            public int getSize() {
                return 8;
            }

            @Override
            public Double read(MemoryAccessor accessor, @NonNull Pointer dest) {
                return accessor.peekDouble(dest);
            }
        });

        map = builder.build();
    }

    private final PrimaryType<T> mapperAs;

    public PrimaryTypeWrapper(PrimaryType<T> mapperAs, int features) {
        super(features);
        this.mapperAs = mapperAs;
    }

    @SuppressWarnings("unchecked")
    public static <T> PrimaryTypeWrapper<T> of(Class<T> clazz, int features) {
        return new PrimaryTypeWrapper<>((PrimaryType<T>) map.get(clazz), features);
    }

    public static <T> PrimaryTypeWrapper<T> of(Class<T> clazz) {
        return of(clazz, NO_FEATURES);
    }

    @Override
    public int getSize(@Nullable Object obj) {
        return mapperAs.getSize();
    }

    @Nullable
    @Override
    public Pointer getFFIPointer() {
        return (isSigned() ? mapperAs.getSignedFFIPointer() : mapperAs.getUnsignedFFIPointer());
    }

    @Override
    public T read(IAllocator allocator, MemoryAccessor accessor, @NonNull Pointer dest) throws NativeMethodException {
        return mapperAs.read(accessor, dest);
    }

    @Override
    public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object data) throws NativeMethodException {
        mapperAs.write(accessor, dest, data);
    }

    @Override
    protected TypeFactory<Type<T>> getFactory() {
        return mapperAs.getTypeFactory();
    }

    private boolean isSigned() {
        return (getFeatures() & CommonFeatures.UNSIGNED) > 0;
    }

    abstract static class PrimaryType<T> {
        public abstract Pointer getSignedFFIPointer();

        public abstract Pointer getUnsignedFFIPointer();

        public abstract int getSize();

        public abstract T read(MemoryAccessor accessor, Pointer dest);

        public abstract void write(MemoryAccessor accessor, Pointer dest, Object data) throws NativeMethodException;

        public abstract TypeFactory<Type<T>> getTypeFactory();
    }
}
