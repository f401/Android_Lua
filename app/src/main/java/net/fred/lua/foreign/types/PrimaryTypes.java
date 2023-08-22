package net.fred.lua.foreign.types;

import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.types.base.Type;

public class PrimaryTypes {
    public static final Type<Pointer> POINTER = Pointer.ofType();
    public static final PrimaryTypeWrapper<Void> VOID = PrimaryTypeWrapper.of(void.class, false);
    public static final PrimaryTypeWrapper<Integer> INT = PrimaryTypeWrapper.of(int.class, false);

    public static final ForeignString.ForeignStringType STRING = new ForeignString.ForeignStringType(false);
}
