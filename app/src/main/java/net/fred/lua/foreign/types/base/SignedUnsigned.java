package net.fred.lua.foreign.types.base;

public interface SignedUnsigned<T> extends Type<T> {
    SignedUnsigned<T> setSigned(boolean signed);
}
