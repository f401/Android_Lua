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

import com.google.common.base.Preconditions;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.MemoryAccessor;
import net.fred.lua.foreign.types.Type;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Packaging for basic types.
 *
 * @param <T> Basic types of packaging required.
 */
public final class PrimaryTypeWrapper<T> implements Type<T> {
    private static final ConcurrentHashMap<Class<?>, PrimaryType<?>> map;

    static {
        map = new ConcurrentHashMap<>(6);
        map.put(byte.class, new PrimaryType<Byte>() {

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putByte(dest, (byte) obj);
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
        map.put(short.class, new PrimaryType<Short>() {

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putShort(dest, (short) obj);
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
        map.put(int.class, new PrimaryType<Integer>() {
            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putInt(dest, (int) obj);
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
        map.put(long.class, new PrimaryType<Long>() {

            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putLong(dest, (long) obj);
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
        map.put(void.class, new PrimaryType<Void>() {
            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                throw new UnsupportedOperationException();
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
        map.put(float.class, new PrimaryType<Float>() {
            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putFloat(dest, (Float) obj);
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

        map.put(double.class, new PrimaryType<Double>() {
            @Override
            public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object obj) {
                accessor.putDouble(dest, (double) obj);
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
    }

    private final PrimaryType<T> mapperAs;
    private boolean signed;
    private final boolean mutable;

    public PrimaryTypeWrapper(boolean signed, boolean mutable, PrimaryType<T> mapperAs) {
        this.mapperAs = mapperAs;
        this.signed = signed;
        this.mutable = mutable;
    }

    @SuppressWarnings("unchecked")
    public static <T> PrimaryTypeWrapper<T> of(Class<T> clazz, boolean mutable) {
        return new PrimaryTypeWrapper<>(true, mutable, (PrimaryType<T>) map.get(clazz));
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
        return (signed ? mapperAs.getSignedFFIPointer() : mapperAs.getUnsignedFFIPointer());
    }

    @Override
    public T read(MemoryAccessor accessor, @NonNull Pointer dest) throws NativeMethodException {
        return mapperAs.read(accessor, dest);
    }

    @Override
    public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object data) throws NativeMethodException {
        mapperAs.write(accessor, dest, data);
    }

    public PrimaryTypeWrapper<T> setSigned(boolean signed) {
        Preconditions.checkState(mutable, "This is immutable.");
        this.signed = signed;
        return this;
    }

    public interface PrimaryType<T> {
        Pointer getSignedFFIPointer();

        Pointer getUnsignedFFIPointer();

        int getSize();

        T read(MemoryAccessor accessor, Pointer dest);

        void write(MemoryAccessor accessor, Pointer dest, Object data) throws NativeMethodException;
    }
}
