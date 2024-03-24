package net.fred.lua.foreign.core;

import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.types.CommonFeatures;
import net.fred.lua.foreign.types.Type;

public class PrimaryTypes {
    public static final Pointer.PointerType POINTER = Pointer.ofType();
    public static final Type<Void> VOID = PrimaryTypeWrapper.of(void.class);


    public static final ForeignString.ForeignStringType STRING = new ForeignString.ForeignStringType();

    public static final Type<Byte> BYTE = PrimaryTypeWrapper.of(byte.class);
    public static final Type<Byte> UNSIGNED_BYTE = PrimaryTypeWrapper.of(byte.class, CommonFeatures.UNSIGNED);
    public static final Type<Short> SHORT = PrimaryTypeWrapper.of(short.class);
    public static final Type<Short> UNSIGNED_SHORT = PrimaryTypeWrapper.of(short.class, CommonFeatures.UNSIGNED);
    public static final Type<Integer> UNSIGNED_INT = PrimaryTypeWrapper.of(int.class, CommonFeatures.UNSIGNED);
    public static final Type<Integer> INT = PrimaryTypeWrapper.of(int.class);
    public static final Type<Long> LONG = PrimaryTypeWrapper.of(long.class);
    public static final Type<Long> UNSIGNED_LONG = PrimaryTypeWrapper.of(long.class, CommonFeatures.UNSIGNED);
    public static final Type<Float> FLOAT = PrimaryTypeWrapper.of(float.class);
    public static final Type<Double> DOUBLE = PrimaryTypeWrapper.of(double.class);
}
