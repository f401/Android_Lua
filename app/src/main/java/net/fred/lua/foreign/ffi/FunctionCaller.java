package net.fred.lua.foreign.ffi;

import androidx.annotation.Nullable;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.MemoryAccessor;
import net.fred.lua.foreign.internal.MemoryController;
import net.fred.lua.foreign.types.Type;

/**
 * The function caller of the native layer.
 * Attention: Cannot call macro functions!!!
 */
public final class FunctionCaller extends MemoryController {
    private final FunctionDescriber describer;
    private final Pointer funcAddress;

    /**
     * If this is true, close is necessary. Default is true.
     */
    private final boolean useCache;

    public FunctionCaller(FunctionDescriber describer, Pointer funcAddress, boolean useCache) {
        this.describer = describer;
        this.funcAddress = funcAddress;
        this.useCache = useCache;
        addChild(describer);
    }

    /**
     * Create a Function Caller.
     *
     * @param address Address of the function to be called.
     * @param rt      @{link FunctionDescriber#of}.
     * @param pt      @{link FunctionDescriber#of}.
     * @return a Function Caller.
     */
    public static FunctionCaller of(Pointer address, Type<?> rt, @Nullable Type<?>... pt) {
        return of(true, address, rt, pt);
    }

    public static FunctionCaller of(boolean useCache, Pointer address, Type<?> rt, @Nullable Type<?>... pt) {
        return new FunctionCaller(FunctionDescriber.of(rt, pt), address, useCache);
    }

    /**
     * Calculate the total size of parameters.
     * Called from native. jni/foreign/foreignFuncs.cpp
     *
     * @param params Parameters to be calculated.
     * @return Total size required.
     */
    @SuppressWarnings("unused")
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
        Pointer ffi = null;
        if (useCache) {
            ffi = describer.prepareCIF().getPointer();
        }
        return ffi_call(MemoryAccessor.UNCHECKED, describer, ffi, funcAddress,
                typedParams, params, describer.getReturnType());
    }

    public Object callThenClose(Object... args) throws NativeMethodException {
        Object result = call(args);
        close();
        return result;
    }

    private native Object ffi_call(MemoryAccessor accessor, // Usual for MemoryAccessor.UNCHECKED
                                   FunctionDescriber describer,
                                   Pointer cif, Pointer funcAddress, Type<?>[] typedParams, Object[] params, Type<?> returnType);
}
