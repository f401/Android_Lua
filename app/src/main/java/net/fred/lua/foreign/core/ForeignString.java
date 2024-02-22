package net.fred.lua.foreign.core;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.utils.StringUtils;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.Pointer;
import net.fred.lua.foreign.Resource;
import net.fred.lua.foreign.allocator.IAllocator;
import net.fred.lua.foreign.internal.ForeignValues;
import net.fred.lua.foreign.internal.MemoryAccessor;
import net.fred.lua.foreign.internal.MemoryController;
import net.fred.lua.foreign.internal.MemorySegment;
import net.fred.lua.foreign.types.Type;
import net.fred.lua.foreign.types.TypeFactory;
import net.fred.lua.foreign.types.TypeRegistry;

public class ForeignString extends MemorySegment {
    private static final String TAG = "ForeignString";
    private final String refer;

    ForeignString(Resource wrapper, String refer) {
        super(wrapper);
        this.refer = refer;
    }

    /**
     * Constructing a native string using Java {@code String}.
     *
     * @see ForeignString#from(IAllocator, MemoryController, String)
     */
    public static ForeignString from(IAllocator allocator, final @Nullable String str) throws NativeMethodException {
        return from(allocator, null, str);
    }

    /**
     * Constructing a native string using Java {@code String}.
     * Then set the constructed string to the {@code parent}'s son.
     *
     * @param parent The parent that needs to be set can be empty.
     * @param str    The {@code String} you want to construct.
     * @return A native string object.
     * @throws NativeMethodException    When allocating space for it fails.
     * @throws IllegalArgumentException Attempting to construct a string with a length of 0.
     */
    @NonNull
    public static ForeignString from(IAllocator allocator, @Nullable MemoryController parent, final @Nullable String str)
            throws NativeMethodException {
        long length;
        if (StringUtils.isEmpty(str) || (length = str.length()) == 0) {
            final String err = ThrowableUtils.getCallerString() + " passes null when creating a string.";
            Log.w(TAG, err);
            throw new IllegalArgumentException(err);
        }

        final Resource resource = allocator.allocateMemory(length + 1);
        MemoryAccessor.putStringUnchecked(resource.getBasePointer(), str);

        final ForeignString result = new ForeignString(resource, str);
        if (parent != null) {
            parent.addChild(result);
        }
        return result;
    }

    /**
     * Obtain the length of the {@code String}.
     *
     * @return The length of the {@code String}.
     * @see MemorySegment#size()
     */
    public long length() {
        return size();
    }

    public String getRefer() {
        return this.refer;
    }

    public static ForeignStringType ofType() {
        return new ForeignStringType();
    }

    public static class ForeignStringType extends Type<ForeignString> {
        public static final TypeFactory<ForeignStringType> FACTORY = new TypeFactory<ForeignStringType>() {
            @Override
            public ForeignStringType create(int feature) {
                return new ForeignStringType();
            }
        };
        public static final int TYPE_INDEX = TypeRegistry.increaseAndGetTypeIdx();

        protected ForeignStringType() {
            super(NO_FEATURES);
        }

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
        public ForeignString read(IAllocator allocator, MemoryAccessor accessor, @NonNull Pointer dest) throws NativeMethodException {
            dest = MemoryAccessor.peekPointerUnchecked(dest);
            return ForeignString.from(allocator, accessor.peekString(dest));
        }

        @Override
        public void write(MemoryAccessor accessor, @NonNull Pointer dest, @NonNull Object data) {
            MemoryAccessor.putPointerUnchecked(dest, ((ForeignString) data).getBasePointer());
        }

        @Override
        public int getTypeIndex() {
            return TYPE_INDEX;
        }
    }
}
