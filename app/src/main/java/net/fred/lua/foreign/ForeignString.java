package net.fred.lua.foreign;

import androidx.annotation.NonNull;

import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.util.Pointer;

public final class ForeignString extends MemorySegment {

    /**
     * Constructing a native string (@{code char *}) using Java {@code String}.
     *
     * @param str The {@code String} you want to construct
     * @return A native string object.
     * @throws NativeMethodException    When allocating space for it fails.
     * @throws IllegalArgumentException Attempting to construct a string with a length of 0.
     */
    public static ForeignString from(final @NonNull String str) throws NativeMethodException {
        long length;
        if ((length = str.length()) == 0) {
            throw new IllegalArgumentException(ThrowableUtils.getInvokerInfoString() +
                    " Attempting to construct a string with a length of 0.");
        }
        final long ptr = ForeignFunctions.alloc(length);
        if (ptr == ForeignValues.NULL) {
            throw new NativeMethodException(
                    "Failed to alloc size: " + length + ".Reason: " +
                            ForeignFunctions.strerror());
        }
        ForeignFunctions.duplicateStringTo(ptr, str);
        return new ForeignString(Pointer.from(ptr), length);
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
