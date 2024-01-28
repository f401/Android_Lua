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
import net.fred.lua.foreign.internal.MemoryAccessor;
import net.fred.lua.foreign.types.CommonFeatures;
import net.fred.lua.foreign.types.Type;
import net.fred.lua.foreign.types.TypeFactory;
import net.fred.lua.foreign.types.TypeRegistry;

/**
 * Packaging for basic types.
 *
 * @param <T> Basic types of packaging required.
 */
public final class PrimaryTypeWrapper<T> extends Type<T> {
    private static final ImmutableMap<Class<?>, PrimaryType<?>> map;

    static {
        ImmutableMap.Builder<Class<?>, PrimaryType<?>> builder = ImmutableMap.builder();
        builder.put(byte.class, new PrimaryType<Byte>() {
            private final TypeFactory<PrimaryTypeWrapper<Byte>> FACTORY = new TypeFactory<PrimaryTypeWrapper<Byte>>() {
                @Override
                public PrimaryTypeWrapper<Byte> create(int feature) {
                    PrimaryTypeWrapper<Byte> type = PrimaryTypeWrapper.of(byte.class);
                    type.setFeatures(feature);
                    return type;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putByte(dest, (byte) obj);
            }

            @Override
            public TypeFactory<PrimaryTypeWrapper<Byte>> getTypeFactory() {
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
            private final TypeFactory<PrimaryTypeWrapper<Short>> FACTORY = new TypeFactory<PrimaryTypeWrapper<Short>>() {
                @Override
                public PrimaryTypeWrapper<Short> create(int feature) {
                    PrimaryTypeWrapper<Short> type = PrimaryTypeWrapper.of(short.class);
                    type.setFeatures(feature);
                    return type;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putShort(dest, (short) obj);
            }

            @Override
            public TypeFactory<PrimaryTypeWrapper<Short>> getTypeFactory() {
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
            private final TypeFactory<PrimaryTypeWrapper<Integer>> FACTORY = new TypeFactory<PrimaryTypeWrapper<Integer>>() {
                @Override
                public PrimaryTypeWrapper<Integer> create(int feature) {
                    PrimaryTypeWrapper<Integer> type = PrimaryTypeWrapper.of(int.class);
                    type.setFeatures(feature);
                    return type;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putInt(dest, (int) obj);
            }

            @Override
            public TypeFactory<PrimaryTypeWrapper<Integer>> getTypeFactory() {
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
            private final TypeFactory<PrimaryTypeWrapper<Long>> FACTORY = new TypeFactory<PrimaryTypeWrapper<Long>>() {
                @Override
                public PrimaryTypeWrapper<Long> create(int feature) {
                    PrimaryTypeWrapper<Long> type = PrimaryTypeWrapper.of(long.class);
                    type.setFeatures(feature);
                    return type;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putLong(dest, (long) obj);
            }

            @Override
            public TypeFactory<PrimaryTypeWrapper<Long>> getTypeFactory() {
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
            public TypeFactory<PrimaryTypeWrapper<Void>> getTypeFactory() {
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
            private final TypeFactory<PrimaryTypeWrapper<Float>> FACTORY = new TypeFactory<PrimaryTypeWrapper<Float>>() {
                @Override
                public PrimaryTypeWrapper<Float> create(int feature) {
                    PrimaryTypeWrapper<Float> type = PrimaryTypeWrapper.of(float.class);
                    type.setFeatures(feature);
                    return type;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putFloat(dest, (Float) obj);
            }

            @Override
            public TypeFactory<PrimaryTypeWrapper<Float>> getTypeFactory() {
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
            private final TypeFactory<PrimaryTypeWrapper<Double>> FACTORY = new TypeFactory<PrimaryTypeWrapper<Double>>() {
                @Override
                public PrimaryTypeWrapper<Double> create(int feature) {
                    PrimaryTypeWrapper<Double> type = PrimaryTypeWrapper.of(double.class);
                    type.setFeatures(feature);
                    return type;
                }
            };

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putDouble(dest, (double) obj);
            }

            @Override
            public TypeFactory<PrimaryTypeWrapper<Double>> getTypeFactory() {
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
    private final boolean mutable;

    public PrimaryTypeWrapper(boolean mutable, PrimaryType<T> mapperAs) {
        this.mapperAs = mapperAs;
        this.mutable = mutable;
    }

    @SuppressWarnings("unchecked")
    public static <T> PrimaryTypeWrapper<T> of(Class<T> clazz, boolean mutable) {
        return new PrimaryTypeWrapper<>(mutable, (PrimaryType<T>) map.get(clazz));
    }

    public static <T> PrimaryTypeWrapper<T> of(Class<T> clazz) {
        return of(clazz, true);
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
    public T read(MemoryAccessor accessor, @NonNull Pointer dest) throws NativeMethodException {
        return mapperAs.read(accessor, dest);
    }

    @Override
    public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object data) throws NativeMethodException {
        mapperAs.write(accessor, dest, data);
    }

    @Override
    public int getTypeIndex() {
        return mapperAs.getTypeIndex();
    }

    public TypeFactory<PrimaryTypeWrapper<T>> getTypeFactory() {
        return mapperAs.getTypeFactory();
    }

    private boolean isSigned() {
        return (getFeatures() & CommonFeatures.UNSIGNED) > 0;
    }

    abstract static class PrimaryType<T> {
        private final int typeIndex;

        protected PrimaryType() {
            typeIndex = TypeRegistry.increaseAndGetTypeIdx();
        }

        public int getTypeIndex() {
            return typeIndex;
        }

        public abstract Pointer getSignedFFIPointer();

        public abstract Pointer getUnsignedFFIPointer();

        public abstract int getSize();

        public abstract T read(MemoryAccessor accessor, Pointer dest);

        public abstract void write(MemoryAccessor accessor, Pointer dest, Object data) throws NativeMethodException;

        public abstract TypeFactory<PrimaryTypeWrapper<T>> getTypeFactory();
    }
}
