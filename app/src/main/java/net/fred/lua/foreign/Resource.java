package net.fred.lua.foreign;

import net.fred.lua.foreign.child.RejectAllHolder;

/**
 * Decorator mode.
 * 所有需要申请内存(malloc)的类，都会间接继承于本类.
 * 直接继承于{@link ResourceWrapper}.
 */
public abstract class Resource extends MemoryController {

    protected Resource() {
        setChildPolicy(new RejectAllHolder());
    }

    /**
     * Obtain the requested memory.
     * Maybe `mmap` request or `malloc` request
     * @return Pointer header of the requested memory.
     */
    public abstract Pointer getBasePointer();

    /**
     * Get the size of the segment.
     *
     * @return the size of the segment
     */
    public abstract long size();
}
