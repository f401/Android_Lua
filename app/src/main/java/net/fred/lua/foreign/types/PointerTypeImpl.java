package net.fred.lua.foreign.types;

public abstract class PointerTypeImpl<T> implements PointerType<T> {

    protected boolean writeAsPointer;

    protected PointerTypeImpl(boolean writeAsPointer) {
        this.writeAsPointer = writeAsPointer;
    }

    @Override
    public PointerType<T> setWriteAsPointer(boolean writeAsPointer) {
        this.writeAsPointer = writeAsPointer;
        return this;
    }
}
