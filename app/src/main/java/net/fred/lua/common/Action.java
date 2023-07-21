package net.fred.lua.common;


/**
 * Required for Functional programming.
 *
 * @param <R> Return type.
 * @param <P> Param type.
 */
public interface Action<R, P> {
    R action(P param);
}
