package net.fred.lua.foreign.proxy;

import net.fred.lua.common.cleaner.Cleaner;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.core.DynamicLoadingLibrary;
import net.fred.lua.foreign.ffi.FunctionCaller;
import net.fred.lua.io.Logger;

import java.lang.reflect.Proxy;

public class LibraryProxy {
    @SuppressWarnings("unchecked")
    public static <T> T create(String libPath, Class<T> type) {
        CallHandler callHandler = new CallHandler(libPath);
        T proxyInstance = (T) Proxy.newProxyInstance(FunctionCaller.class.getClassLoader(),
                new Class[]{type},
                callHandler);
        Cleaner.createPhantom(proxyInstance, new Closer(callHandler.dll));
        return proxyInstance;
    }

    private static class Closer implements Cleaner.Cleanable {
        private final DynamicLoadingLibrary dll;

        private Closer(DynamicLoadingLibrary dll) {
            this.dll = dll;
        }

        @Override
        public void clean() {
            try {
                dll.close();
            } catch (NativeMethodException e) {
                Logger.e("Failed to close library at" + dll.getPointer() + ". Exception: " + e);
            }
        }
    }
}
