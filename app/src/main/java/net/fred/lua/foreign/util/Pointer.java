package net.fred.lua.foreign.util;

import androidx.annotation.NonNull;

public class Pointer {
    private long addr;

    public Pointer(long addr) {
        if (addr < 0) {
            throw new RuntimeException(
                    "Cannot create a pointer with an address less than 0. (" + addr + ")");
        }
        this.addr = addr;
    }

    /**
     * Another method for creating @{code Pointer}.
     */
    public static Pointer from(long address) {
        return new Pointer(address);
    }

    public final long get() {
        return addr;
    }

    public final void set(long addr) {
        this.addr = addr;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pointer)) return false;

        Pointer pointer = (Pointer) o;

        return addr == pointer.addr;
    }

    @Override
    public final int hashCode() {
        return (int) (addr ^ (addr >>> 32));
    }

    @NonNull
    @Override
    public final String toString() {
        return "0x" + Integer.toHexString((int) addr);
    }
}
