package net.fred.lua.foreign.scoped;

import net.fred.lua.App;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.allocator.IAllocator;
import net.fred.lua.foreign.child.IChildPolicy;

public class AutoScopedResource extends ScopedResourceImpl {

    public AutoScopedResource(IAllocator allocator) {
        super(allocator);
    }
    
    @Override
    public AutoScopedResource newScope() {
        AutoScopedResource result = new AutoScopedResource(mAllocator);
        addChild(result);
        return result;
    }

    @Override
    public void dispose(boolean _finalized) throws NativeMethodException {
        super.dispose(false);// Ignore warnings, enable finalizations.
    }
    
}
