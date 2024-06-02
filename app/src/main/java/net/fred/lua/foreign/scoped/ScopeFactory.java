package net.fred.lua.foreign.scoped;

import net.fred.lua.foreign.allocator.DefaultAllocator;
import net.fred.lua.foreign.allocator.IAllocator;

import java.lang.ref.SoftReference;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class ScopeFactory {
    private static volatile SoftReference<AutoScopedResource> global = null;

    public static ManualScopedResource ofManual(IAllocator allocator) {
        return new ManualScopedResource(allocator);
    }

    public static AutoScopedResource ofAuto(IAllocator allocator) {
        return new AutoScopedResource(allocator);
    }

    public static AutoScopedResource ofGlobal() {
        AutoScopedResource result = null;
        if (global != null && (result = global.get()) != null) {
            return result;
        }
        synchronized (AutoScopedResource.class) {
            if (global == null || global.get() == null) {
                result = ofAuto(DefaultAllocator.INSTANCE);
                global = new SoftReference<>(result);
            }
        }
        return result;
    }
}
