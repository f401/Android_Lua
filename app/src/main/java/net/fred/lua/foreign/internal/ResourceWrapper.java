package net.fred.lua.foreign.internal;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.Resource;

public class ResourceWrapper extends Resource {
    private final Resource impl;

    protected ResourceWrapper(Resource impl) {
        this.impl = impl;
    }

    @Override
    public final Pointer getBasePointer() {
        return impl.getBasePointer();
    }

    @Override
    public long size() {
        return impl.size();
    }

    @Override
    public void dispose(boolean finalized) throws NativeMethodException {
        super.dispose(finalized);
        impl.dispose(finalized);
    }


}
