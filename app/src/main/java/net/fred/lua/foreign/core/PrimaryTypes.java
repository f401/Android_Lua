package net.fred.lua.foreign.core;

import net.fred.lua.foreign.Pointer;

public class PrimaryTypes {
    public static final Pointer.PointerType POINTER = Pointer.ofType();
    public static final PrimaryTypeWrapper<Void> VOID = PrimaryTypeWrapper.of(void.class);
    public static final PrimaryTypeWrapper<Integer> INT = PrimaryTypeWrapper.of(int.class);

    public static final ForeignString.ForeignStringType STRING = new ForeignString.ForeignStringType();
}
