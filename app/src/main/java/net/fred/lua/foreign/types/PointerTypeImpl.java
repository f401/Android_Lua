package net.fred.lua.foreign.types;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.foreign.types.base.PointerType;

public abstract class PointerTypeImpl<T> implements PointerType<T> {
    protected boolean writeAsPointer;
    protected boolean mutable;

    protected PointerTypeImpl(boolean writeAsPointer, boolean mutable) {
        this.writeAsPointer = writeAsPointer;
        this.mutable = mutable;
    }

    @Override
    public PointerType<T> setWriteAsPointer(boolean writeAsPointer) {
        ArgumentsChecker.checkState(mutable, "This is immutable.");
        this.writeAsPointer = writeAsPointer;
        return this;
    }
}
