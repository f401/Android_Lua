package net.fred.lua.foreign;

public class Pointer {
    private long addr;

    public Pointer(long addr) {
        this.addr = addr;
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
}
