package net.fred.lua.foreign.types;

import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_DOUBLE;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_FLOAT;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT16;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT32;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT64;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT8;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_POINTER;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UINT16;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UINT32;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UINT64;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UINT8;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_VOID;
import static net.fred.lua.foreign.internal.ForeignValues.SIZE_OF_POINTER;
import static net.fred.lua.foreign.internal.MemoryAccessor.peekByte;
import static net.fred.lua.foreign.internal.MemoryAccessor.peekDouble;
import static net.fred.lua.foreign.internal.MemoryAccessor.peekFloat;
import static net.fred.lua.foreign.internal.MemoryAccessor.peekInt;
import static net.fred.lua.foreign.internal.MemoryAccessor.peekLong;
import static net.fred.lua.foreign.internal.MemoryAccessor.peekPointer;
import static net.fred.lua.foreign.internal.MemoryAccessor.peekShort;
import static net.fred.lua.foreign.internal.MemoryAccessor.putByte;
import static net.fred.lua.foreign.internal.MemoryAccessor.putDouble;
import static net.fred.lua.foreign.internal.MemoryAccessor.putFloat;
import static net.fred.lua.foreign.internal.MemoryAccessor.putInt;
import static net.fred.lua.foreign.internal.MemoryAccessor.putLong;
import static net.fred.lua.foreign.internal.MemoryAccessor.putPointer;
import static net.fred.lua.foreign.internal.MemoryAccessor.putShort;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.MemoryController;
import net.fred.lua.foreign.internal.MemorySegment;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Packaging for basic types.
 *
 * Note: Free is only required after using @{link PointerType#writeAsPointer}.
 *
 * @param <T> Basic types of packaging required.
 */
public class PrimaryTypeWrapper<T> extends MemoryController implements PointerType<T> {
    private static final ConcurrentHashMap<Class<?>, PrimaryType<?>> map;

    static {
        map = new ConcurrentHashMap<>(6);
        map.put(byte.class, new PrimaryType<Byte>() {

            @Override
            public void write(@NonNull Pointer dest, @NonNull Object obj) {
                putByte(dest, (byte) obj);
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
            public Byte read(@NonNull Pointer dest) {
                return peekByte(dest);
            }
        });
        map.put(short.class, new PrimaryType<Short>() {

            @Override
            public void write(@NonNull Pointer dest, @NonNull Object obj) {
                putShort(dest, (short) obj);
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
            public Short read(@NonNull Pointer dest) {
                return peekShort(dest);
            }
        });
        map.put(int.class, new PrimaryType<Integer>() {
            @Override
            public void write(@NonNull Pointer dest, @NonNull Object obj) {
                putInt(dest, (int) obj);
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
            public Integer read(@NonNull Pointer dest) {
                return peekInt(dest);
            }
        });
        map.put(long.class, new PrimaryType<Long>() {

            @Override
            public void write(@NonNull Pointer dest, @NonNull Object obj) {
                putLong(dest, (long) obj);
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
            public Long read(@NonNull Pointer dest) {
                return peekLong(dest);
            }
        });
        map.put(void.class, new PrimaryType<Void>() {
            @Override
            public void write(@NonNull Pointer dest, @NonNull Object obj) {
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
            public Void read(@NonNull Pointer dest) {
                return null;
            }
        });
        map.put(float.class, new PrimaryType<Float>() {
            @Override
            public void write(@NonNull Pointer dest, @NonNull Object obj) {
                putFloat(dest, (Float) obj);
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
            public Float read(@NonNull Pointer dest) {
                return peekFloat(dest);
            }
        });

        map.put(double.class, new PrimaryType<Double>() {
            @Override
            public void write(@NonNull Pointer dest, @NonNull Object obj) {
                putDouble(dest, (double) obj);
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
            public Double read(@NonNull Pointer dest) {
                return peekDouble(dest);
            }
        });
    }

    private final PrimaryType<T> mapperAs;
    private boolean writeAsPointer, signed;

    public PrimaryTypeWrapper(boolean writeAsPointer, PrimaryType<T> mapperAs, boolean signed) {
        this.writeAsPointer = writeAsPointer;
        this.mapperAs = mapperAs;
        this.signed = signed;
    }

    @SuppressWarnings("unchecked")
    public static <T> PrimaryTypeWrapper<T> of(Class<T> clazz) {
        return new PrimaryTypeWrapper<>(false, (PrimaryType<T>) map.get(clazz), true);
    }

    @Override
    public PrimaryTypeWrapper<T> setWriteAsPointer(boolean writeAsPointer) {
        this.writeAsPointer = writeAsPointer;
        return this;
    }

    @Override
    public int getSize(@Nullable Object obj) {
        return writeAsPointer ? (int) SIZE_OF_POINTER : mapperAs.getSize();
    }

    @Nullable
    @Override
    public Pointer getFFIPointer() {
        return writeAsPointer ? FFI_TYPE_POINTER : (signed ? mapperAs.getSignedFFIPointer() : mapperAs.getUnsignedFFIPointer());
    }

    @Override
    public T read(@NonNull Pointer dest) {
        if (writeAsPointer) {
            dest = peekPointer(dest);
        }
        return mapperAs.read(dest);
    }

    @Override
    public void write(@NonNull Pointer dest, @NonNull Object data) throws NativeMethodException {
        if (writeAsPointer) {
            MemorySegment memorySegment = MemorySegment.create(getSize(data));
            addChild(memorySegment);
            mapperAs.write(memorySegment.getPointer(), data);
            putPointer(dest, memorySegment.getPointer());
        } else {
            mapperAs.write(dest, data);
        }
    }

    public PrimaryTypeWrapper<T> setSigned(boolean signed) {
        this.signed = signed;
        return this;
    }

    public interface PrimaryType<T> {
        Pointer getSignedFFIPointer();

        Pointer getUnsignedFFIPointer();

        int getSize();

        T read(Pointer dest);

        void write(Pointer dest, Object data) throws NativeMethodException;
    }
}
