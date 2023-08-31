package net.fred.lua.foreign.types;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.MemoryAccessor;

public interface Type<T> {

    /**
     * Obtain the size of a single Type.
     *
     * @param obj When the size is determined, null can be passed in. Otherwise, the object that needs to obtain the size must be passed in.
     * @return The size of this type.
     */
    int getSize(@Nullable Object obj);

    @Nullable
    Pointer getFFIPointer();

    T read(MemoryAccessor accessor, @NonNull Pointer dest);

    void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object data) throws NativeMethodException;
}
