package io.github.rosemoe.sora.util;

public interface ShareableData<T> extends Cloneable {
    /**
     * Declare that the object is retained by an owner
     */
    void retain();

    /**
     * Declare that the object is no longer used by an owner
     */
    void release();

    /**
     * Check if this object can be modified directly
     */
    boolean isMutable();

    /**
     * If this object is mutable, returns itself.
     * Otherwise, the object is cloned and the cloned object is returned.
     */
    T toMutable();
}
