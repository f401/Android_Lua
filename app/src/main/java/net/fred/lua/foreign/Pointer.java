package net.fred.lua.foreign;

import androidx.annotation.NonNull;

import net.fred.lua.common.Logger;
import net.fred.lua.foreign.types.PointerType;
import net.fred.lua.foreign.types.Type;

public class Pointer implements PointerType {
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
    public final Pointer plus(Type<?> type, long need) {
        return new Pointer(address + (type.size * need));
    }

    @NonNull
    public final Pointer plus(long size) {
        return new Pointer(address + size);
    }

    @Override
    public Pointer getPointer() {
        return this;
    }
}
