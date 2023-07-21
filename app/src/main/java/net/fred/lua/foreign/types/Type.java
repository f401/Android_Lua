package net.fred.lua.foreign.types;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.Pointer;

public class Type<T> {
    public final int size;
    public final Pointer pointer;
    public final TypesRegistry.AssignableReadable<T> assignableReadable;

    public Type(int size, @NonNull Pointer pointer, @Nullable TypesRegistry.AssignableReadable<T> assignableReadable) {
        this.size = size;
        this.pointer = pointer;
        this.assignableReadable = assignableReadable;
    }

    @NonNull
    public static <T> Type<T> of(int size, long address, @Nullable TypesRegistry.AssignableReadable<T> assignableReadable) {
        return new Type<>(size, Pointer.from(address), assignableReadable);
    }
}
