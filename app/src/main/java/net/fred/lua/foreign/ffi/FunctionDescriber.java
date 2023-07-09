package net.fred.lua.foreign.ffi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.ForeignFunctions;
import net.fred.lua.foreign.ForeignValues;
import net.fred.lua.foreign.MemorySegment;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.types.Array;
import net.fred.lua.foreign.util.ForeignCloseable;
import net.fred.lua.foreign.util.Pointer;

import java.util.List;

public class FunctionDescriber extends ForeignCloseable {
    @Nullable
    private List<Types.Type<?>> params;
    @Nullable
    private Array<Pointer> paramsNativeArray;
    private final Types.Type<?> returnType;

    public FunctionDescriber(@Nullable List<Types.Type<?>> params, @NonNull Types.Type<?> returnType) throws NativeMethodException {
        super(null);
        this.returnType = returnType;

        if (params != null) {
            this.params = params;
            this.paramsNativeArray = Array.create(params.size(), Pointer.class);
            for (int curr = 0; curr < params.size(); ++curr) {
                this.paramsNativeArray.insert(curr, params.get(curr).pointer);
            }
        }
    }

    @Nullable
    public List<Types.Type<?>> getParams() {
        return params;
    }

    @NonNull
    public Types.Type<?> getReturnType() {
        return returnType;
    }

    public long evalParamsTotalSize() {
        if (params == null) return 0;
        long result = 0;
        for (Types.Type<?> type : params) {
            result += type.size;
        }
        return result;
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
        addCloses(cif);
        ForeignFunctions.ffi_prep_cif(cif.getPointer().get(),
                params != null ? params.size() : 0,
                returnType.pointer.get(),
                paramsNativeArray != null ? paramsNativeArray.getPointer().get() : ForeignValues.NULL);
        return cif;
    }

    @Override
    protected void onFree() throws Exception {
        if (paramsNativeArray != null) {
            this.paramsNativeArray.close();
        }
    }
}
