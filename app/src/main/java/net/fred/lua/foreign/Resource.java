package net.fred.lua.foreign;

import net.fred.lua.foreign.child.RejectAllHolder;
import net.fred.lua.foreign.internal.MemoryController;

public abstract class Resource extends MemoryController {

    protected Resource() {
        setChildPolicy(new RejectAllHolder());
    }

    public abstract Pointer getBasePointer();

    /**
     * Get the size of the segment.
     *
     * @return the size of the segment
     */
    public abstract long size();
}
