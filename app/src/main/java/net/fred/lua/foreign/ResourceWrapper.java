package net.fred.lua.foreign;

// 装饰器设计模式
public class ResourceWrapper extends Resource {
    private final Resource impl;

    protected ResourceWrapper(Resource impl) {
        // 不将其设置为我们的Child, 并将使用权归于我们
        impl.detachParent();
        this.impl = impl;
    }

    /**
     * {@inheritDoc}
     * Now, you can use {@link net.fred.lua.foreign.allocator.LibcMallocResourceImpl}
     */
    @Override
    public final Pointer getBasePointer() {
        return impl.getBasePointer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long size() {
        return impl.size();
    }

    @Override
    public void dispose(boolean finalized) throws NativeMethodException {
        super.dispose(finalized);
        impl.close();
    }


}
