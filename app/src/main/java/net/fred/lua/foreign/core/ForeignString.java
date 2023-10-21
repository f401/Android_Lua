package net.fred.lua.foreign.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.utils.StringUtils;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.foreign.internal.MemoryAccessor;
import net.fred.lua.foreign.internal.MemorySegment;
import net.fred.lua.foreign.types.Type;
import net.fred.lua.io.Logger;

public final class ForeignString extends MemorySegment {

    private final String refer;

    public static final int DEFAULT_SIZE = 256;

    private ForeignString(Pointer src, String refer) {
        super(src, refer.length());
        this.refer = refer;
    }

    /**
     * Constructing a native string (@{code char *}) using Java {@code String}.
     *
     * @param str The {@code String} you want to construct
     * @return A native string object.
     * @throws NativeMethodException    When allocating space for it fails.
     * @throws IllegalArgumentException Attempting to construct a string with a length of 0.
     */
    @NonNull
    public static ForeignString from(final @Nullable String str) throws NativeMethodException {
        long length;
        if (StringUtils.isEmpty(str) || (length = str.length()) == 0) {
            final String err = ThrowableUtils.getCallerString() + " passes null when creating a string.";
            Logger.w(err);
            throw new IllegalArgumentException(err);
        }
        final Pointer ptr = MemorySegment.allocate(length + 1);
        MemoryAccessor.putStringUnchecked(ptr, str);

        final ForeignString result = new ForeignString(ptr, str);
        return result;
    }

    /**
     * Obtain the length of the @{code String}.
     *
     * @return The length of the @{code String}.
     * @see MemorySegment#size().
     */
    public long length() {
        return size();
    }

    public static ForeignStringType ofType() {
        return new ForeignStringType();
    }

    public static class ForeignStringType implements Type<ForeignString> {

        @Override
        public int getSize(@Nullable Object obj) {
            return (int) ForeignValues.SIZE_OF_POINTER;
        }

        @Nullable
        @Override
        public Pointer getFFIPointer() {
            return ForeignValues.FFI_TYPE_POINTER;
        }

        @Override
        public ForeignString read(MemoryAccessor accessor, @NonNull Pointer dest) throws NativeMethodException {
            dest = MemoryAccessor.peekPointerUnchecked(dest);
            return ForeignString.from(accessor.peekString(dest));
        }

        @Override
        public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object data) {
            MemoryAccessor.putPointerUnchecked(dest, ((ForeignString) data).getPointer());
        }
    }
}
