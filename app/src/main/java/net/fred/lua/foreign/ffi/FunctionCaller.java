package net.fred.lua.foreign.ffi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.MemorySegment;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.types.ForeignString;
import net.fred.lua.foreign.util.ForeignCloseable;
import net.fred.lua.foreign.util.Pointer;

import java.util.ArrayList;
import java.util.List;

//TODO: FINISH.

public class FunctionCaller extends ForeignCloseable {

    @NonNull
    private final FunctionDescriber desc;

    @Nullable
    private MemorySegment cachedCif;

    protected FunctionCaller(@NonNull Pointer functionPointer,
                             @Nullable List<Types.Type<?>> params,
                             @NonNull Types.Type<?> returnType) throws NativeMethodException {
        super(functionPointer);
        this.desc = new FunctionDescriber(params, returnType);
    }

    public static FunctionCaller create(@NonNull Pointer address, @NonNull Class<?> returnType,
                                        @Nullable Class<?>... params) throws NativeMethodException {
        return new FunctionCaller(address,
                params != null ? classes2list(params) : null
                , Types.get(returnType));
    }

    @NonNull
    private static List<Types.Type<?>> classes2list(@NonNull Class<?>[] params) {
        List<Types.Type<?>> result = new ArrayList<>(params.length);
        for (Class<?> clazz : params) {
            result.add(Types.get(clazz));
        }
        return result;
    }

    @NonNull
    public MemorySegment getCif() throws NativeMethodException {
        if (cachedCif == null) {
            cachedCif = desc.prepare();
        }
        return cachedCif;
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
            returnSegment = MemorySegment.create(sz == Types.SIZE_UNKNOWN ? ForeignString.DEFAULT_SIZE : sz);
        }

        //TODO: FINISH!
        return null;
    }

    @Override
    protected void onFree() throws Exception {
        desc.close();
        if (cachedCif != null) {
            cachedCif.close();
        }
    }
}
