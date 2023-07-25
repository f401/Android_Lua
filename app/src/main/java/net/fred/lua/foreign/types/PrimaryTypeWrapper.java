package net.fred.lua.foreign.types;

import static net.fred.lua.foreign.internal.ForeignFunctions.peekByte;
import static net.fred.lua.foreign.internal.ForeignFunctions.peekInt;
import static net.fred.lua.foreign.internal.ForeignFunctions.peekLong;
import static net.fred.lua.foreign.internal.ForeignFunctions.peekPointer;
import static net.fred.lua.foreign.internal.ForeignFunctions.peekShort;
import static net.fred.lua.foreign.internal.ForeignFunctions.putByte;
import static net.fred.lua.foreign.internal.ForeignFunctions.putInt;
import static net.fred.lua.foreign.internal.ForeignFunctions.putLong;
import static net.fred.lua.foreign.internal.ForeignFunctions.putPointer;
import static net.fred.lua.foreign.internal.ForeignFunctions.putShort;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT16;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT32;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT64;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_INT8;
import static net.fred.lua.foreign.internal.ForeignValues.FFI_TYPE_POINTER;
import static net.fred.lua.foreign.internal.ForeignValues.SIZE_OF_POINTER;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.MemoryController;
import net.fred.lua.foreign.internal.MemorySegment;

import java.util.concurrent.ConcurrentHashMap;

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
                return Pointer.from(FFI_TYPE_INT8);
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
                return Pointer.from(FFI_TYPE_INT16);
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
                return Pointer.from(FFI_TYPE_INT32);
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
                return Pointer.from(FFI_TYPE_INT64);
            }

            @Override
            public Long read(@NonNull Pointer dest) {
                return peekLong(dest);
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
        return writeAsPointer ? Pointer.from(FFI_TYPE_POINTER) : mapperAs.getFFIPointer();
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

    public interface PrimaryType<T> extends Type<T> {
        void write(@NonNull Pointer dest, @NonNull Object obj);

        T read(@NonNull Pointer dest);
    }
}
