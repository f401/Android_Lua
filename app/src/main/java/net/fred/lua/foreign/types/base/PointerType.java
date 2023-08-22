package net.fred.lua.foreign.types.base;


/**
 * All classes that can serve as pointers need to be implemented.
 *
 * @param <T> The type referred to.
 */
public interface PointerType<T> extends Type<T> {
    PointerType<T> setWriteAsPointer(boolean writeAsPointer);
}
