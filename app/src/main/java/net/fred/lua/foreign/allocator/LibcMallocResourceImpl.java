package net.fred.lua.foreign.allocator;

import android.util.Log;

import androidx.annotation.NonNull;

import net.fred.lua.BuildConfig;
import net.fred.lua.foreign.MemorySegment;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.Resource;

public final class LibcMallocResourceImpl extends Resource {
    private final Pointer pointer;
    private final long size;
    private String stack;

    public LibcMallocResourceImpl(long size) throws NativeMethodException {
        this.pointer = MemorySegment.alloc(size);
        this.size = size;
        if (BuildConfig.DEBUG) {
            this.stack = Log.getStackTraceString(new Throwable());
        }
    }

    @Override
    public Pointer getBasePointer() {
        return pointer;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void dispose(boolean finalized) throws NativeMethodException {
        super.dispose(finalized);
        MemorySegment.free(pointer);
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + (BuildConfig.DEBUG ? "trace " + stack : "");
    }
    
}
