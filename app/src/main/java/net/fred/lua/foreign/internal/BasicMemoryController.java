package net.fred.lua.foreign.internal;

import androidx.annotation.Nullable;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;

public class BasicMemoryController extends MemoryController {

    protected Pointer pointer;

    protected BasicMemoryController(@Nullable Pointer pointer) {
        this.pointer = pointer;
    }

    public final Pointer getPointer() {
        return pointer;
    }

    @Override
    protected void onFree() throws NativeMethodException {
        if (pointer != null) {
            MemorySegment.free(pointer);
        }
    }
}
