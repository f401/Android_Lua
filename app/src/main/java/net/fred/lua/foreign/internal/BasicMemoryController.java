package net.fred.lua.foreign.internal;

import androidx.annotation.Nullable;

import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.types.PointerType;

public class BasicMemoryController extends MemoryController implements PointerType {

    protected Pointer pointer;

    protected BasicMemoryController(@Nullable Pointer pointer) {
        this.pointer = pointer;
    }

    @Override
    public final Pointer getPointer() {
        return pointer;
    }

    @Override
    protected void onFree() {
        if (pointer != null) {
            ForeignFunctions.free(pointer);
        }
    }
}
