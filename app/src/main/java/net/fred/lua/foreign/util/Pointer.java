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

    public long get() {
        return addr;
    }

    public void set(long addr) {
        this.addr = addr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pointer)) return false;

        Pointer pointer = (Pointer) o;

        return addr == pointer.addr;
    }

    @Override
    public int hashCode() {
        return (int) (addr ^ (addr >>> 32));
    }

    @NonNull
    @Override
    public String toString() {
        return "0x" + Integer.toHexString((int) addr);
    }
}
