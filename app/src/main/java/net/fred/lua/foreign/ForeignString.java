package net.fred.lua.foreign;

import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.util.Pointer;

public final class ForeignString extends MemorySegment {

    /**
     * Obtain the length of the @{code String}.
     * @see MemorySegment#size().
     * @return The length of the @{code String}.
     */
    public long length() {
        return size();
    }

    private ForeignString(Pointer src, long size) {
        super(src, size);
    }

    /**
     * Constructing a native string (@{code char *}) using Java {@code String}.
     * @param str The {@code String} you want to construct
     * @return A native string object.
     * @throws NativeMethodException When allocating space for it fails.
     * @throws IllegalArgumentException Attempting to construct a string with a length of 0.
     */
    public static ForeignString from(final String str) throws NativeMethodException {
        long length = 0;
        if (str == null || (length = str.length()) == 0) {
            throw new IllegalArgumentException(
                    length == 0 ? "Attempting to construct a string with a length of 0." :
                    ThrowableUtils.getInvokerInfoString()
                    + " passes null when constructing a native string.");
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
}
