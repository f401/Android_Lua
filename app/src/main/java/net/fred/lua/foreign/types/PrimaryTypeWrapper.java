package net.fred.lua.foreign.types;

import static net.fred.lua.foreign.internal.ForeignFunctions.peekByte;
import static net.fred.lua.foreign.internal.ForeignFunctions.peekDouble;
import static net.fred.lua.foreign.internal.ForeignFunctions.peekFloat;
import static net.fred.lua.foreign.internal.ForeignFunctions.peekInt;
import static net.fred.lua.foreign.internal.ForeignFunctions.peekLong;
import static net.fred.lua.foreign.internal.ForeignFunctions.peekPointer;
import static net.fred.lua.foreign.internal.ForeignFunctions.peekShort;
import static net.fred.lua.foreign.internal.ForeignFunctions.putByte;
import static net.fred.lua.foreign.internal.ForeignFunctions.putDouble;
import static net.fred.lua.foreign.internal.ForeignFunctions.putFloat;
import static net.fred.lua.foreign.internal.ForeignFunctions.putInt;
import static net.fred.lua.foreign.internal.ForeignFunctions.putLong;
import static net.fred.lua.foreign.internal.ForeignFunctions.putPointer;
import static net.fred.lua.foreign.internal.ForeignFunctions.putShort;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_DOUBLE;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_FLOAT;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT16;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT32;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT64;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT8;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_POINTER;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UIN64;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UINT16;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UINT32;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_UINT8;
import static net.fred.lua.foreign.internal.ForeignValues.SIZE_OF_POINTER;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.ForeignValues;
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
            public int getSize(Object obj) {
                return 1;
            }

            @NonNull
            @Override
            public Pointer getFFIPointer() {
                return signed ? FFI_TYPE_INT8 : FFI_TYPE_UINT8;
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
            public int getSize(Object obj) {
                return 2;
            }

            @NonNull
            @Override
            public Pointer getFFIPointer() {
                return signed ? FFI_TYPE_INT16 : FFI_TYPE_UINT16;
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
            public int getSize(Object obj) {
                return 4;
            }

            @NonNull
            @Override
            public Pointer getFFIPointer() {
                return signed ? FFI_TYPE_INT32 : FFI_TYPE_UINT32;
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
            public int getSize(Object obj) {
                return 8;
            }

            @NonNull
            @Override
            public Pointer getFFIPointer() {
                return signed ? FFI_TYPE_INT64 : FFI_TYPE_UIN64;
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
            public int getSize(@Nullable Object obj) {
                return 0;
            }

            @NonNull
            @Override
            public Pointer getFFIPointer() {
                return ForeignValues.FFI_TYPE_VOID;
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
            public int getSize(@Nullable Object obj) {
                return 4;
            }

            @NonNull
            @Override
            public Pointer getFFIPointer() {
                return FFI_TYPE_FLOAT;
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
            public int getSize(@Nullable Object obj) {
                return 8;
            }

            @NonNull
            @Override
            public Pointer getFFIPointer() {
                return FFI_TYPE_DOUBLE;
            }

            @Override
            public Double read(@NonNull Pointer dest) {
                return peekDouble(dest);
            }
        });
    }

    private final PrimaryType<T> mapperAs;
    private boolean writeAsPointer;

    public PrimaryTypeWrapper(boolean writeAsPointer, PrimaryType<T> mapperAs) {
        this.writeAsPointer = writeAsPointer;
        this.mapperAs = mapperAs;
    }

    @SuppressWarnings("unchecked")
    public static <T> PrimaryTypeWrapper<T> of(Class<T> clazz) {
        return new PrimaryTypeWrapper<>(false, (PrimaryType<T>) map.get(clazz));
    }

    @Override
    public PrimaryTypeWrapper<T> setWriteAsPointer(boolean writeAsPointer) {
        this.writeAsPointer = writeAsPointer;
        return this;
    }

    @Override
    public int getSize(Object obj) {
        return writeAsPointer ? (int) SIZE_OF_POINTER : mapperAs.getSize(obj);
    }

    @Nullable
    @Override
    public Pointer getFFIPointer() {
        return writeAsPointer ? FFI_TYPE_POINTER : mapperAs.getFFIPointer();
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

    public abstract static class PrimaryType<T> implements SignedUnsigned<T> {
        protected boolean signed = false;

        public abstract void write(@NonNull Pointer dest, @NonNull Object obj);

        public abstract T read(@NonNull Pointer dest);

        @Override
        public SignedUnsigned<T> setSigned(boolean signed) {
            this.signed = signed;
            return this;
        }
    }
}
