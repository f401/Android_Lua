package net.fred.lua.foreign.ffi;

import androidx.annotation.NonNull;

import net.fred.lua.foreign.ForeignFunctions;
import net.fred.lua.foreign.ForeignValues;
import net.fred.lua.foreign.MemorySegment;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.types.Array;
import net.fred.lua.foreign.util.ForeignCloseable;
import net.fred.lua.foreign.util.Pointer;

import java.util.List;

public class FunctionDescriber extends ForeignCloseable {
    private final List<Types.Type<?>> params;
    private final Types.Type<?> returnType;

    private final Array<Pointer> paramsNativeArray;

    public FunctionDescriber(@NonNull List<Types.Type<?>> params, @NonNull Types.Type<?> returnType) throws NativeMethodException {
        super(null);
        this.params = params;
        this.returnType = returnType;
        this.paramsNativeArray = Array.create(params.size(), Pointer.class);

        for (int curr = 0; curr < params.size(); ++curr) {
            this.paramsNativeArray.insert(curr, params.get(curr).pointer);
        }
    }

    @NonNull
    public List<Types.Type<?>> getParams() {
        return params;
    }

    @NonNull
    public Types.Type<?> getReturnType() {
        return returnType;
    }

    public long evalParamsTotalSize() {
        long result = 0;
        for (Types.Type<?> type : params) {
            result += type.size;
        }
        return result;
    }

    /**
     * Run @{code ffi_prep_cif}. Create a function description for the native layer (remember @{code close}).
     *
     * @return Pointer to the created @{code ffi_cif}
     * @throws NativeMethodException When @{code ffi_cif} failed to allocate space
     */
    @NonNull
    public MemorySegment prepare() throws NativeMethodException {
        MemorySegment cif = MemorySegment.create(ForeignValues.SIZE_OF_FFI_CIF);
        ForeignFunctions.ffi_prep_cif(cif.getPointer().get(),
                params.size(), returnType.pointer.get(), paramsNativeArray.getPointer().get());
        return cif;
    }

    @Override
    protected void onFree() {
        this.paramsNativeArray.close();
    }
}
