package net.fred.lua.foreign.core.ffi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.Constants;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.allocator.IAllocator;
import net.fred.lua.foreign.types.Type;

public class FunctionDescriptor {

    @Nullable
    private final Type<?>[] params;
    private final Type<?> returnType;

    public FunctionDescriptor(@Nullable Type<?>[] params, Type<?> returnType) {
        this.params = params;
        this.returnType = returnType;
    }

    /**
     * Create a new function describer.
     *
     * @param returnType The return type of the function in the native layer. Null for void.
     * @param params     The parameter type of the function in the native layer. Nullable.
     * @return function describer.
     */
    public static FunctionDescriptor of(Type<?> returnType, @Nullable Type<?>... params) {
        return new FunctionDescriptor(params, returnType);
    }

    @Nullable
    public Type<?>[] getParams() {
        return params;
    }

    public Type<?> getReturnType() {
        return returnType;
    }

    /**
     * The method of truly generating {@code ffi_cif}. At libffi.cpp.
     *
     * @param cif        Pointer to the generated result storage.
     * @param returnType Return Type
     * @param params     parameter type.
     * @return The generated results. 0 is normal.
     * @throws NativeMethodException etc...
     */
    public native int prep_cif(Pointer cif, IAllocator scope, @NonNull Type<?> returnType, Type<?>[] params) throws NativeMethodException;

    public Pointer doPrepare(@NonNull IAllocator parent) throws NativeMethodException {
        Pointer cif = parent.allocateMemory(Constants.SIZE_OF_FFI_CIF).getBasePointer();
        prep_cif(cif, parent, returnType, params);
        return cif;
    }
}
