package net.fred.lua.foreign.internal;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;

public class BasicMemoryController extends MemoryController {

    protected BasicMemoryController(SinglePointerHolder holder) {
        super(holder);
    }

    public final Pointer getPointer() {
        return getPointerHolder().pointer;
    }

    @Override
    public SinglePointerHolder getPointerHolder() {
        return (SinglePointerHolder) super.getPointerHolder();
    }

    public static class SinglePointerHolder implements MemoryController.PointerHolder {
        protected Pointer pointer;

        public SinglePointerHolder(Pointer pointer) {
            this.pointer = pointer;
        }

        @Override
        public void onFree() throws NativeMethodException {
            if (pointer != null) {
                MemorySegment.free(pointer);
            }
        }
    }
}
