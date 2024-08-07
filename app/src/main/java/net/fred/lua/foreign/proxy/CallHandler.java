package net.fred.lua.foreign.proxy;

import android.util.Log;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.core.DynamicLoadingLibrary;
import net.fred.lua.foreign.core.ffi.FunctionCaller;
import net.fred.lua.foreign.scoped.ScopeFactory;
import net.fred.lua.foreign.types.Type;
import net.fred.lua.foreign.types.TypeRegistry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class CallHandler implements InvocationHandler {
    private static final String TAG = "CallHandler";
    DynamicLoadingLibrary dll;

    public CallHandler(String libPath) {
        try {
            dll = DynamicLoadingLibrary.open(libPath);
        } catch (NativeMethodException e) {
            Log.e(TAG, "Failed to open library " + libPath + ". Exception: " + e);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Type<?>[] paramsArray = null;
        if (method.getParameterTypes().length != 0) {
            ArrayList<Type<?>> params = new ArrayList<>(args.length);
            for (Class<?> param : method.getParameterTypes()) {
                params.add(TypeRegistry.getType(param));
            }
            paramsArray = params.toArray(new Type[0]);
        }
        return FunctionCaller.of(ScopeFactory.ofGlobal(), false, dll.lookupSymbol(method.getName()),
                TypeRegistry.getType(method.getReturnType()), paramsArray
        ).call(args);
    }
}
