package net.fred.lua.foreign.ffi;

import androidx.annotation.Nullable;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.common.Logger;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.MemoryController;
import net.fred.lua.foreign.internal.MemorySegment;
import net.fred.lua.foreign.types.Type;

/**
 * The function caller of the native layer.
 * Attention: Cannot call macro functions!!!
 */
public class FunctionCaller extends MemoryController {
    private static final Pointer.PointerType POINTER_TYPE = Pointer.ofType();
    private final FunctionDescriber describer;
    private final Pointer funcAddress;

    public FunctionCaller(FunctionDescriber describer, Pointer funcAddress) {
        this.describer = describer;
        this.funcAddress = funcAddress;
    }

    /**
     * Create a Function Caller.
     * @param address Address of the function to be called.
     * @param rt @{link FunctionDescriber#of}.
     * @param pt @{link FunctionDescriber#of}.
     * @return a Function Caller.
     */
    public static FunctionCaller of(Pointer address, Type<?> rt, @Nullable Type<?>... pt) {
        return new FunctionCaller(FunctionDescriber.of(rt, pt), address);
    }

    /**
     * Calculate the total size of parameters.
     *
     * @param params Parameters to be calculated.
     * @return Total size required.
     */
    private long evalParamsTotalSize(Object... params) {
        long size = 0;
        for (int i = 0; i < params.length; i++) {
            size += describer.getParams()[i].getSize(params[i]);
        }
        return size;
    }

    public Object call(Object... params) throws NativeMethodException {
        Type<?>[] typedParams = describer.getParams();
        if ((params == null && typedParams != null) ||
                (params != null && typedParams == null)) {
            throw new IllegalArgumentException();
        }
        //ffi need to pass in a pointer to the parameter
        if (typedParams != null) {
            ArgumentsChecker.check(typedParams.length == params.length, "The length of the passed in parameter does not match the descriptor");
        }

        long returnSize = describer.getReturnType().getSize(null);
        Pointer returnPointer = null;
        if (returnSize != 0) {
            MemorySegment returnSegment = MemorySegment.create(returnSize);
            addChild(returnSegment);
            returnPointer = returnSegment.getPointer();
        }
        ffi_call(describer.prepareCIF().getPointer(), funcAddress, returnPointer,
                typedParams, params);
        return describer.getReturnType().read(returnPointer);
    }

    public FunctionDescriber getDescriber() {
        return describer;
    }

    protected native void ffi_call(Pointer cif, Pointer funcAddress, Pointer returnSegment, Type<?>[] typedParams, Object[] params);

    @Override
    protected void onFree() throws NativeMethodException {
        super.onFree();
        Logger.i("Releasing Caller.");
        describer.close();
    }
}
