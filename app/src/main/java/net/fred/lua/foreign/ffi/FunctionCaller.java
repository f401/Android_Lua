package net.fred.lua.foreign.ffi;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.foreign.internal.MemoryController;
import net.fred.lua.foreign.internal.MemorySegment;
import net.fred.lua.foreign.types.Type;

public class FunctionCaller extends MemoryController {
    private static final Pointer.PointerType POINTER_TYPE = Pointer.ofType();
    private final FunctionDescriber describer;
    private final Pointer funcAddress;

    public FunctionCaller(FunctionDescriber describer, Pointer funcAddress) {
        this.describer = describer;
        this.funcAddress = funcAddress;
    }

    public static FunctionCaller of(Pointer address, Type<?> rt, Type<?>... pt) {
        return new FunctionCaller(FunctionDescriber.of(rt, pt), address);
    }

    private long evalTotalSize(Object... params) {
        long size = 0;
        for (int i = 0; i < params.length; i++) {
            size += describer.getParams()[i].getSize(params[i]);
        }
        return size;
    }

    /**
     * This implementation will cause excessive memory usage in the native layer heap,
     * So the new implementation will implement these functions on the stack
     */
    @Deprecated
    private MemorySegment putParamsToSegment(Object... params) throws NativeMethodException {
        long paramsOffset = 0;
        MemorySegment paramsSegment = MemorySegment.create(evalTotalSize(params));
        MemorySegment resultSegment = MemorySegment.create(params.length * ForeignValues.SIZE_OF_POINTER);
        addChild(paramsSegment);
        addChild(resultSegment);
        Type<?>[] typedParams = describer.getParams();
        for (int i = 0; i < params.length; i++) {
            Pointer dest = paramsSegment.getPointer().plus(paramsOffset);
            typedParams[i].write(dest, params[i]);
            POINTER_TYPE.write(Pointer.from(i * ForeignValues.SIZE_OF_POINTER), dest);

            paramsOffset += typedParams[i].getSize(params[i]);
        }
        return resultSegment;
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
            //paramsPointerSegment = putParamsToSegment(params);
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

    protected native void ffi_call(Pointer cif, Pointer funcAddress, Pointer returnSegment, Type<?>[] typedParams, Object[] params);
}
