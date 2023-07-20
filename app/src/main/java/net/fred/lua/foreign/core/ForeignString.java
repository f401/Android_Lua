package net.fred.lua.foreign.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.Logger;
import net.fred.lua.common.utils.StringUtils;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.internal.ForeignFunctions;
import net.fred.lua.foreign.internal.MemorySegment;

public final class ForeignString extends MemorySegment {

    public static final long DEFAULT_SIZE = 128;

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
            Logger.w(ThrowableUtils.getInvokerInfoString() + " passes null when creating a string. Using default size.");
            length = DEFAULT_SIZE;
        }
        final Pointer ptr = ForeignFunctions.alloc(length + 1);
        ForeignFunctions.duplicateStringTo(ptr.get(), str);
        return new ForeignString(ptr, length);
    }

    private ForeignString(Pointer src, long size) {
        super(src, size);
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
}
