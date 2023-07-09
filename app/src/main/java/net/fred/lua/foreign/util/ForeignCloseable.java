package net.fred.lua.foreign.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.Flag;
import net.fred.lua.common.Logger;
import net.fred.lua.foreign.ForeignFunctions;

import java.util.ArrayList;
import java.util.List;

public class ForeignCloseable implements AutoCloseable {

    private Flag freed;
    protected Pointer pointer;

    /**
     * Contains objects that need to be released together when this object is released.
     */
    protected List<AutoCloseable> subCloses;

    protected ForeignCloseable(@Nullable Pointer pointer) {
        this.pointer = pointer;
    }

    public final Pointer getPointer() {
        return pointer;
    }

    @NonNull
    public final Flag getFreed() {
        return freed;
    }

    public final void setFreed(@NonNull Flag freed) {
        this.freed = freed;
    }

    public void addCloses(@Nullable AutoCloseable segment) {
        if (segment != this && segment != null) {
            if (subCloses == null) {
                subCloses = new ArrayList<>(2);
            }
            subCloses.add(segment);
        }
    }

    @Override
    public final void close() throws Exception {
        if (!this.freed.getFlag()) {
            onFree();
            freeSubCloses();
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
    protected void onFree() throws Exception {
        if (pointer != null) {
            ForeignFunctions.free(pointer.get());
        }
    }

    protected void freeSubCloses() throws Exception {
        if (subCloses != null) {
            for (AutoCloseable mem : subCloses) {
                mem.close();
            }
        }
    }

}
