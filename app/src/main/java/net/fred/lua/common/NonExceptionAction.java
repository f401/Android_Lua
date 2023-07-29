package net.fred.lua.common;

public interface NonExceptionAction<R, P> extends Action<R, P> {
    R action(P param);
}
