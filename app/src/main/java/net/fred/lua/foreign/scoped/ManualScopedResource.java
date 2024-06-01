package net.fred.lua.foreign.scoped;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Resource;
import net.fred.lua.foreign.allocator.IAllocator;
import net.fred.lua.foreign.internal.MemoryController;

/**
 * 这是IAllocator和MemoryAccessor的结合体, 专门用来分配受管理的内存
 * 不需要使用时，调用close即可
 */
public class ManualScopedResource extends ScopedResourceImpl {


    public static final String TAG = "ScopedResource";
    
    public ManualScopedResource(IAllocator allocator) {
        super(allocator);
    }
    
    /**
     * 新建一个受限的资源
     */
    @Override
    public ManualScopedResource newScope() {
        ManualScopedResource result = new ManualScopedResource(mAllocator);
        addChild(result);
        return result;
    }


}
