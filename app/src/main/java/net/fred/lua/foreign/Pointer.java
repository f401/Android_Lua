package net.fred.lua.foreign;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.allocate.IAllocator;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.foreign.internal.MemoryAccessor;
import net.fred.lua.foreign.types.Type;
import net.fred.lua.foreign.types.TypeFactory;
import net.fred.lua.foreign.types.TypeRegistry;

public class Pointer {
    private long address;

    public Pointer(long address) {
        this.address = address;
    }

    /**
     * Another method for creating @{code Pointer}.
     */
    @NonNull
    public static Pointer from(long address) {
        return new Pointer(address);
    }

    public final long get() {
        return address;
    }

    protected final void set(long address) {
        this.address = address;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pointer)) return false;

        Pointer pointer = (Pointer) o;

        return address == pointer.address;
    }

    @Override
    public final int hashCode() {
        return (int) address;
    }

    @NonNull
    @Override
    public final String toString() {
        return "0x" + Long.toHexString(address);
    }

    @NonNull
    public final Pointer plus(long size) {
        return new Pointer(address + size);
    }

    @NonNull
    public static PointerType ofType() {
        return new PointerType();
    }

    private static Pointer min(Pointer first, Pointer second) {
        return first.address < second.address ? first : second;
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
        if ((this.address > 0 && other.address > 0) ||
                (this.address < 0 && other.address < 0)) {
            return this.address > other.address;
        } else {
            return this.address < 0;
        }
    }

    public static class PointerType extends Type<Pointer> {
        public static final TypeFactory<PointerType> FACTORY = new TypeFactory<PointerType>() {
            @Override
            public PointerType create(int feature) {
                return new PointerType();
            }
        };

        public static final int TYPE_INDEX = TypeRegistry.increaseAndGetTypeIdx();

        protected PointerType() {
            super(NO_FEATURES);
        }

        @Override
        public int getSize(@Nullable Object obj) {
            return (int) ForeignValues.SIZE_OF_POINTER;
        }

        @Nullable
        @Override
        public Pointer getFFIPointer() {
            return ForeignValues.FFI_TYPE_POINTER;
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
        public int getTypeIndex() {
            return TYPE_INDEX;
        }
    }
}
