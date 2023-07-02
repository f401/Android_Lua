package net.fred.lua.foreign.util;

import net.fred.lua.common.Flag;
import net.fred.lua.common.Logger;
import net.fred.lua.foreign.ForeignFunctions;

public class ForeignCloseable implements AutoCloseable {

    private Flag freed;
    protected Pointer pointer;

    protected ForeignCloseable(Pointer pointer) {
        this.pointer = pointer;
    }

    public final Pointer getPointer() {
        return pointer;
    }

    public final Flag getFreed() {
        return freed;
    }

    public final void setFreed(Flag freed) {
        this.freed = freed;
    }

    @Override
    public final void close() throws RuntimeException {
        if (!this.freed.getFlag()) {
            onFree();
            freed.setFlag(true);
        } else {
            Logger.e("Pointer freed twice: " + pointer);
            throw new RuntimeException("Pointer freed twice! " + pointer);
        }
    }

    /*
     * This function is called when the pointer is released.
     * Can ensure that the flag is only called once without modification.
     *
     * If you don't want to call the free function to release,
     * Rewrite and implement your own release function.
     */
    protected void onFree() {
        ForeignFunctions.free(pointer.get());
    }

}
