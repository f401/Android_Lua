package net.fred.lua.foreign;

import androidx.annotation.NonNull;

import net.fred.lua.foreign.util.ForeignCloseable;
import net.fred.lua.foreign.util.Pointer;

import java.util.ArrayList;
import java.util.List;

public class MemorySegment extends ForeignCloseable {
    private final long size;

    /**
     * Contains objects that need to be released together when this object is released.
     */
    private List<MemorySegment> subMemorySegment;

    /**
     * See {@link MemorySegment#create}
     */
    protected MemorySegment(Pointer src, long size) {
        super(src);
        this.size = size;
    }

    /**
     * Create a memory segment of size {@code size}.
     *
     * @param size The size of the memory segment needs to be created.
     * @return This object.
     * @see MemorySegment#allocate
     */
    @NonNull
    public static MemorySegment create(long size) throws NativeMethodException {
        return new MemorySegment(allocate(size), size);
    }

    /**
     * Create a 'size' length and return a pointer to it.
     *
     * @param size The size needs to be created.
     * @return Pointer to.
     * @throws NativeMethodException    When creation fails
     * @throws IllegalArgumentException When {@code size} is less than or equal to 0.
     */
    @NonNull
    public static Pointer allocate(long size) throws NativeMethodException {
        if (size <= 0) {
            throw new IllegalArgumentException("`Size 'cannot be less than or equal to 0");
        }
        final long ptr = ForeignFunctions.alloc(size);
        if (ptr == ForeignValues.NULL) {
            throw new NativeMethodException(
                    "Failed to alloc size: " + size + ".Reason: " +
                            ForeignFunctions.strerror());
        }
        return Pointer.from(ptr);
    }

    public void addSubSegment(@NonNull MemorySegment segment) {
        if (segment != this) {
            if (subMemorySegment == null) {
                subMemorySegment = new ArrayList<>(2);
            }
            subMemorySegment.add(segment);
        }
    }

    /**
     * Get the size of the segment.
     *
     * @return the size of the segment
     */
    public long size() {
        return size;
    }

    /**
     * You must call this method in your own @ {code onFree} when overriding.
     */
    protected void freeSubSegments() {
        if (subMemorySegment != null) {
            for (MemorySegment mem : subMemorySegment) {
                mem.close();
            }
        }
    }

    /**
     * @see MemorySegment#freeSubSegments()
     */
    @Override
    protected void onFree() {
        super.onFree();
        freeSubSegments();
    }
}
