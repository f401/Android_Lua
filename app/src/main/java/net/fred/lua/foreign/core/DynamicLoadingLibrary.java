package net.fred.lua.foreign.core;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.common.Logger;
import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.BasicMemoryController;
import net.fred.lua.foreign.internal.ForeignFunctions;
import net.fred.lua.foreign.internal.ForeignValues;

public final class DynamicLoadingLibrary extends BasicMemoryController {
    private DynamicLoadingLibrary(Pointer ptr) {
        super(ptr);
    }

    public static DynamicLoadingLibrary open(String path) throws NativeMethodException {
        ArgumentsChecker.check(FileUtils.exists(path)
                , "Invoker (" + ThrowableUtils.getCallerString() + "), passes null symbol.");

        Pointer handle = ForeignFunctions.dlopen(path, ForeignValues.RTLD_LAZY);
        Logger.i("Loaded library " + path + ".At 0x" + Long.toHexString(handle.get()));
        return new DynamicLoadingLibrary(handle);
    }

    public Pointer lookupSymbol(String symbol) throws NativeMethodException {
        ArgumentsChecker.checkNotEmpty(symbol, "Invoker (" + ThrowableUtils.getCallerString() +
                "), passes null symbol.");

        Pointer handle = ForeignFunctions.dlsym(pointer, symbol);
        Logger.i("Loaded symbol " + symbol + ".At 0x" + Long.toHexString(handle.get()));
        return handle;
    }

    @Override
    protected void onFree() {
        ForeignFunctions.dlclose(pointer);
    }
}
