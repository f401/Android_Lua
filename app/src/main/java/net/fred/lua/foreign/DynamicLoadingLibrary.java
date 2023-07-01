package net.fred.lua.foreign;

import net.fred.lua.common.Logger;
import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.common.utils.StringUtils;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.util.ForeignCloseable;
import net.fred.lua.foreign.util.Pointer;

public class DynamicLoadingLibrary extends ForeignCloseable {
    private DynamicLoadingLibrary(Pointer ptr) {
        super(ptr);
    }

    public static DynamicLoadingLibrary open(String path) throws NativeMethodException {
        if (!FileUtils.exists(path)) {
            String info = ThrowableUtils.getInvokerInfoString();
            Logger.e("Invoker (" + info + "), passes null path.");
            return null;
        }
        long handle = ForeignFunctions.dlopen(path, ForeignValues.RTLD_LAZY);
        if (handle == ForeignValues.NULL) {
            throw new NativeMethodException(
                    "Failed to open dll: " + path + ", reason: " +
                            ForeignFunctions.dlerror());
        }
        return new DynamicLoadingLibrary(new Pointer(handle));
    }

    public Pointer lookupSymbol(String symbol) throws NativeMethodException {
        if (StringUtils.isEmpty(symbol)) {
            String info = ThrowableUtils.getInvokerInfoString();
            Logger.e("Invoker (" + info + "), passes null symbol.");
            return null;
        }
        long handle = ForeignFunctions.dlsym(pointer.get(), symbol);
        if (handle == ForeignValues.NULL) {
            throw new NativeMethodException("Failed to load symbol: " + symbol +
                    ".Reason: " + ForeignFunctions.dlerror());
        }
        return Pointer.from(handle);
    }

    @Override
    protected void onFree() {
        ForeignFunctions.dlclose(pointer.get());
    }
}
