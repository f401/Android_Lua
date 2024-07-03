package net.fred.lua.foreign;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.allocator.IAllocator;
import net.fred.lua.foreign.types.Type;
import net.fred.lua.foreign.types.TypeFactory;

import javax.annotation.concurrent.Immutable;

/**
 * Encapsulation of Native Layer Pointers in Java Layer.
 */
@Immutable
public class Pointer {
    private long mAddress;

    public Pointer(long address) {
        this.mAddress = address;
    }

    /**
     * Another method for creating {@code Pointer}.
     */
    @NonNull
    public static Pointer from(long address) {
        return new Pointer(address);
    }

    private static Pointer min(Pointer first, Pointer second) {
        return first.mAddress < second.mAddress ? first : second;
    }

    /**
     * Get the pointer to the native layer.
     * NOTE: THIS METHOD IS USELESS IN JAVA LAYER.
     *
     * @return The pointer in the native layer.
     */
    public final long get() {
        return mAddress;
    }

    /**
     * Set the pointer to the native layer
     * @param address The pointer value you want to set.
     */
    protected final void set(long address) {
        this.mAddress = address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pointer)) return false;

        Pointer pointer = (Pointer) o;

        return mAddress == pointer.mAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return (int) mAddress;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public final String toString() {
        return "0x" + Long.toHexString(mAddress);
    }

    @NonNull
    public static PointerType ofType() {
        return new PointerType();
    }

    /**
     * Add the current pointer to {@code size} and return a new pointer based on the addition result.
     * @param size The size you want to add.
     * @return The new pointer.
     */
    @NonNull
    public final Pointer plus(long size) {
        return new Pointer(mAddress + size);
    }

    public final boolean biggerThan(@NonNull Pointer other) {
        // 正常来说, 计算机中有符号的整数最高位为符号位(0为正, 1为负)(二进制反码)
        // 例如:
        // 1111 1111 1111 1111 (Short) (代表-1)
        // 0111 1111 1111 1111 (Short) (Short.MAX_VALUE)
        // 但 Linux中地址的表述都是无符号整数
        // 所以上面两个数字在无符号时代表的整数就不一样
        // 1111 1111 1111 1111 (Short) (代表65535) (无符号) (unsigned short)
        // 0111 1111 1111 1111 (Short) (Short.MAX_VALUE) (无符号) (unsigned short)
        // Long同理

        if (this.equals(other)) return false;

        // 这时两者都大于0或小于0(都有符号位或没有)，可以正常比较
        if ((this.mAddress > 0 && other.mAddress > 0) ||
                (this.mAddress < 0 && other.mAddress < 0)) {
            return this.mAddress > other.mAddress;
        } else {
            return this.mAddress < 0;
        }
    }

    public static class PointerType extends Type<Pointer> {
        public static final TypeFactory<PointerType> FACTORY = new TypeFactory<PointerType>() {
            @Override
            public PointerType create(int feature) {
                return new PointerType();
            }
        };

        protected PointerType() {
            super(NO_FEATURES);
        }

        @Override
        public int getSize(@Nullable Object obj) {
            return (int) Constants.SIZE_OF_POINTER;
        }

        @Nullable
        @Override
        public Pointer getFFIPointer() {
            return Constants.FFI_TYPE_POINTER;
        }

        @Override
        public Pointer read(IAllocator allocator, MemoryAccessor accessor, @NonNull Pointer dest) throws NativeMethodException {
            return MemoryAccessor.peekPointerUnchecked(dest);
        }

        @Override
        public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object data) throws NativeMethodException {
            MemoryAccessor.putPointerUnchecked(dest, (Pointer) data);
        }

        @Override
        protected TypeFactory<PointerType> getFactory() {
            return FACTORY;
        }

    }
}
