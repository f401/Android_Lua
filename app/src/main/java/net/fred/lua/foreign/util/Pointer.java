package net.fred.lua.foreign.util;

import androidx.annotation.NonNull;

import net.fred.lua.foreign.ffi.Types;
import net.fred.lua.common.Logger;

public class Pointer {
    private long address;

    public Pointer(long address) {
        if (address < 0) {
            Logger.e(
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

    public final void set(long addr) {
        this.address = addr;
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
        return "0x" + Integer.toHexString((int) address);
    }

    @NonNull
    public final Pointer plus(Types.Type<?> type, long need) {
        return new Pointer(address + (type.size * need));
    }

    @NonNull
    public final Pointer plus(long size) {
        return new Pointer(address + size);
    }
}
