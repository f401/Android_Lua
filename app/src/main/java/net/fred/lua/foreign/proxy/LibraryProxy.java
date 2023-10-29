package net.fred.lua.foreign.proxy;

import net.fred.lua.foreign.ffi.FunctionCaller;

import java.lang.reflect.Proxy;

public class LibraryProxy {
    @SuppressWarnings("unchecked")
    public static <T> T create(String libPath, Class<T> type) {
        CallHandler callHandler = new CallHandler(libPath);
        return (T) Proxy.newProxyInstance(FunctionCaller.class.getClassLoader(),
                new Class[]{type},
                callHandler);
    }
}
