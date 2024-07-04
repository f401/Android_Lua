package net.fred.lua.foreign.core.ffi;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.fred.lua.foreign.MemoryAccessor;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.allocator.IAllocator;
import net.fred.lua.foreign.scoped.IScopedResource;
import net.fred.lua.foreign.types.Type;

public class FunctionCaller {
    private IScopedResource scope;
    private final FunctionDescriptor descriptor;
    private final Pointer funcAddress;
    private final boolean saveCIF;
    private IScopedResource savedCIFScope;// related to saveCIF field
    private Pointer savedCIFPointer;// related to saveCIF field

    public FunctionCaller(IScopedResource scope, FunctionDescriptor descriptor, Pointer funcAddress, boolean saveCIF) {
        this.scope = scope;
        this.descriptor = descriptor;
        this.funcAddress = funcAddress;
        this.saveCIF = saveCIF;
    }

    /**
     * Create a Function Caller.
     *
     * @param address Address of the function to be called.
     * @param rt      {@link FunctionDescriptor#of}.
     * @param pt      {@link FunctionDescriptor#of}.
     * @return a Function Caller.
     */
    public static FunctionCaller of(IScopedResource scope, Pointer address, Type<?> rt, @Nullable Type<?>... pt) {
        return of(scope, true, address, rt, pt);
    }

    public static FunctionCaller of(IScopedResource scope, boolean useCache, Pointer address, Type<?> rt, @Nullable Type<?>... pt) {
        return new FunctionCaller(scope, FunctionDescriptor.of(rt, pt), address, useCache);
    }

    public Object call(Object... params) throws NativeMethodException {
        Type<?>[] typedParams = descriptor.getParams();
        // Check the consistency of parameters.
        // Specifically including null check and parameter length check.
        Preconditions.checkArgument((params == null && typedParams == null) || (params != null && typedParams != null));
        Preconditions.checkState(typedParams == null || typedParams.length == params.length, "The length of the passed in parameter does not match the descriptor");
        
        Pointer ffi = savedCIFPointer;
        IScopedResource tempScope = savedCIFScope;

        if (saveCIF && tempScope == null) {
            // For the first call to saveCIF
            tempScope = savedCIFScope = scope.newScope();
            ffi = savedCIFPointer = descriptor.doPrepare(savedCIFScope);
        } else if (!saveCIF) {
            // No need for saveCIF to directly create a new 
            tempScope = scope.newScope();
        }

        Log.d("FunctionCaller", "Call function at address " + funcAddress);

        // Real Call!
        Object result = ffi_call(MemoryAccessor.UNCHECKED, tempScope, ffi, funcAddress, descriptor.getParams(), params, descriptor.getReturnType());
        
        if (!saveCIF) {
            tempScope.close();
        }
        return result;
    }
    
    /**
     * A way to achieve resource reuse.
     * Recommended.
     */
    public void setNewScope(IScopedResource scope) {
        this.savedCIFScope = null;
        this.scope = scope;
    }

    private native Object ffi_call(MemoryAccessor accessor, // Usual for MemoryAccessor.UNCHECKED
                                   IAllocator allocator,
                                   Pointer cif, Pointer funcAddress, Type<?>[] typedParams, Object[] params, Type<?> returnType);
}
