package net.fred.lua.foreign.ffi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.core.PrimaryTypes;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.foreign.internal.MemoryController;
import net.fred.lua.foreign.internal.MemorySegment;
import net.fred.lua.foreign.types.Type;

/**
 * Store CIF in a cached manner.
 */
public final class FunctionDescriber extends MemoryController {

    @Nullable
    private final Type<?>[] params;
    private final Type<?> returnType;

    public FunctionDescriber(Type<?> returnType, @Nullable Type<?>[] params) {
        if (returnType == null) {
            returnType = PrimaryTypes.VOID;
        }
        this.returnType = returnType;
        this.params = params;
    }

    /**
     * Create a new function describer.
     *
     * @param returnType The return type of the function in the native layer. Null for void.
     * @param params     The parameter type of the function in the native layer. Nullable.
     * @return function describer.
     */
    public static FunctionDescriber of(Type<?> returnType, @Nullable Type<?>... params) {
        return new FunctionDescriber(returnType, params);
    }

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
    public native int prep_cif(Pointer cif, @NonNull Type<?> returnType, Type<?>[] params) throws NativeMethodException;

    /**
     * Called from native. {@link #prep_cif} (libffi.cpp)
     */
    @SuppressWarnings("unused")
    private long requestMemory(long size) throws NativeMethodException {
        MemorySegment segment = MemorySegment.create(size);
        addChild(segment);
        return segment.getBasePointer().get();
    }

    /**
     * Running the native layer {@code ffi_prep_cif}.
     * To prepare for the first parameter of the call. ({@code ffi_call})
     *
     * @return Point to {@code ffi_cif}
     * @throws NativeMethodException etc...
     */
    public MemorySegment prepareCIF() throws NativeMethodException {
        // search in cache.
        if (hasChild()) {
            return (MemorySegment) childAt(0);
        }
        MemorySegment cif = MemorySegment.create(ForeignValues.SIZE_OF_FFI_CIF);
        addChild(cif);
        int result = prep_cif(cif.getBasePointer(), returnType, params);
        if (result != ForeignValues.FFI_STATUS_OK) {
            throw new NativeMethodException("Result: " + result);
        }
        return cif;
    }
}
