package net.fred.lua.foreign.ffi;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.core.Array;
import net.fred.lua.foreign.internal.ForeignFunctions;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.foreign.internal.MemoryController;
import net.fred.lua.foreign.internal.MemorySegment;
import net.fred.lua.foreign.types.Type;

public class FunctionDescriber extends MemoryController {

    private final Type<?>[] params;

    private final Type<?> returnType;

    public FunctionDescriber(Type<?> returnType, Type<?>[] params) {
        this.returnType = returnType;
        this.params = params;
    }

    public static FunctionDescriber of(Type<?> returnType, Type<?>... params) {
        return new FunctionDescriber(returnType, params);
    }

    public MemorySegment prepareCIF() throws NativeMethodException {
        freeChildren();
        int result;
        MemorySegment cif = MemorySegment.create(ForeignValues.SIZE_OF_FFI_CIF);
        addChild(cif);
        if (params != null) {
            Array<Pointer> params = Array.create(Pointer.ofType(), this.params.length);
            addChild(params);
            for (int i = 0; i < this.params.length; ++i) {
                params.write(i, this.params[i].getFFIPointer());
            }
            result = ForeignFunctions.ffi_prep_cif(cif.getPointer(), this.params.length, returnType.getFFIPointer(), params.getPointer());
        } else {
            result = ForeignFunctions.ffi_prep_cif(cif.getPointer(), 0, returnType.getFFIPointer(), Pointer.from(ForeignValues.NULL));
        }
        if (result != ForeignValues.FFI_STATUS_OK) {
            throw new NativeMethodException("Result: " + result);
        }
        return cif;
    }
}
