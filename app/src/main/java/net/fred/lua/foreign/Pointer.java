package net.fred.lua.foreign;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.Logger;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.foreign.internal.MemoryAccessor;
import net.fred.lua.foreign.types.base.Type;

public class Pointer {
    private long address;

    public Pointer(long address) {
        if (address < 0) {
            Logger.w(
                    "Trying to create a pointer with an address less than 0. (" + address + ")");
        }
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

    public final void set(long address) {
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

    public static class PointerType implements Type<Pointer> {
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
        public Pointer read(@NonNull Pointer dest) {
            return MemoryAccessor.peekPointer(dest);
        }

        @Override
        public void write(@NonNull Pointer dest, @NonNull Object data) throws NativeMethodException {
            MemoryAccessor.putPointer(dest, (Pointer) data);
        }
    }
}
