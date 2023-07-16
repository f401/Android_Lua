package net.fred.lua.foreign.ffi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.core.Array;
import net.fred.lua.foreign.internal.ForeignFunctions;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.foreign.internal.MemoryController;
import net.fred.lua.foreign.internal.MemorySegment;
import net.fred.lua.foreign.types.TypesRegistry;

import java.util.List;

public class FunctionDescriber extends MemoryController {
    private final TypesRegistry.Type<?> returnType;
    @Nullable
    private Array<Pointer> paramsNativeArray;
    @Nullable
    private List<TypesRegistry.Type<?>> params;

    public FunctionDescriber(@Nullable List<TypesRegistry.Type<?>> params, @NonNull TypesRegistry.Type<?> returnType) throws NativeMethodException {
        this.returnType = returnType;

        if (params != null) {
            this.params = params;
            this.paramsNativeArray = Array.create(params.size(), Pointer.class);
            for (int curr = 0; curr < params.size(); ++curr) {
                this.paramsNativeArray.insert(curr, params.get(curr).pointer);
            }
            this.addChild(this.paramsNativeArray);
        }
    }

    @Nullable
    public List<TypesRegistry.Type<?>> getParams() {
        return params;
    }

    @NonNull
    public TypesRegistry.Type<?> getReturnType() {
        return returnType;
    }

    /**
     * Run @{code ffi_prep_cif}. Create a function description for the native layer (don't @{code close}, It will automatically close.).
     *
     * @return Pointer to the created @{code ffi_cif}
     * @throws NativeMethodException When @{code ffi_cif} failed to allocate space
     */
    @NonNull
    public MemorySegment prepare() throws NativeMethodException {
        MemorySegment cif = MemorySegment.create(ForeignValues.SIZE_OF_FFI_CIF);
        this.addChild(cif);
        ForeignFunctions.ffi_prep_cif(cif.getPointer().get(),
                params != null ? params.size() : 0,
                returnType.pointer.get(),
                paramsNativeArray != null ? paramsNativeArray.getPointer().get() : ForeignValues.NULL);
        return cif;
    }

}
