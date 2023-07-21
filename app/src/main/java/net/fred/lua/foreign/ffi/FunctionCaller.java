package net.fred.lua.foreign.ffi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.core.ForeignString;
import net.fred.lua.foreign.internal.BasicMemoryController;
import net.fred.lua.foreign.internal.MemorySegment;
import net.fred.lua.foreign.types.Type;
import net.fred.lua.foreign.types.TypesRegistry;

import java.util.ArrayList;
import java.util.List;

//TODO: FINISH.

public class FunctionCaller extends BasicMemoryController {

    @NonNull
    private final FunctionDescriber desc;

    protected FunctionCaller(@NonNull Pointer functionPointer,
                             @Nullable List<Type<?>> params,
                             @NonNull Type<?> returnType) throws NativeMethodException {
        super(functionPointer);
        this.desc = new FunctionDescriber(params, returnType);
        this.addChild(this.desc);
    }

    public static FunctionCaller create(@NonNull Pointer address, @NonNull Class<?> returnType,
                                        @Nullable Class<?>... params) throws NativeMethodException {
        return new FunctionCaller(address,
                params != null ? classes2list(params) : null
                , TypesRegistry.get(returnType));
    }

    @NonNull
    private static List<Type<?>> classes2list(@NonNull Class<?>[] params) {
        List<Type<?>> result = new ArrayList<>(params.length);
        for (Class<?> clazz : params) {
            result.add(TypesRegistry.get(clazz));
        }
        return result;
    }

    @NonNull
    public MemorySegment getCif() throws NativeMethodException {
        return desc.prepare();
    }

    @NonNull
    public FunctionDescriber getFunctionDesc() {
        return desc;
    }

    public Object call(@Nullable Object... params) throws NativeMethodException {
        if ((params == null && desc.getParams() != null) || (params != null && desc.getParams() == null)) {
            throw new IllegalArgumentException("Parameter or description is null.");
        }

        if (params != null && params.length != desc.getParams().size()) {
            throw new IllegalArgumentException("Argument and description lengths are not equal.(params: " + params.length + ", desc: "
                    + desc.getParams().size() + ").");
        }

        @Nullable
        MemorySegment returnSegment;
        if (desc.getReturnType().size == 0) {
            returnSegment = null;
        } else {
            final long sz = desc.getReturnType().size;
            returnSegment = MemorySegment.create(sz == TypesRegistry.SIZE_UNKNOWN ? ForeignString.DEFAULT_SIZE : sz);
        }

        //TODO: FINISH!
        return null;
    }
}
