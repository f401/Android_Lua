package net.fred.lua.foreign.types;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.MemoryAccessor;

// Type is immutable.
public abstract class Type<T> {

    private int features;

    /**
     * Obtain the size of a single Type.
     *
     * @param obj When the size is determined, null can be passed in. Otherwise, the object that needs to obtain the size must be passed in.
     * @return The size of this type.
     */

    public abstract int getSize(@Nullable Object obj);

    @Nullable
    public abstract Pointer getFFIPointer();

    public abstract T read(MemoryAccessor accessor, @NonNull Pointer dest) throws NativeMethodException;

    public abstract void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object data) throws NativeMethodException;

    public abstract int getTypeIndex();

    protected final int getFeatures() {
        return features;
    }

    protected final void setFeatures(int features) {
        this.features = features;
    }
}
