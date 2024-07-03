package net.fred.lua.foreign.child;

/**
 * A class specifically responsible for managing the sub objects of
 * the {@link net.fred.lua.foreign.MemoryController}.
 * Child must be subclasses of {@linkplain AutoCloseable}.
 *
 * @see net.fred.lua.foreign.MemoryController
 * @see RejectAllHolder
 * @see SimpleChildHolder
 * @see SingleChildHolder
 */
public interface IChildPolicy {
    /**
     * Add a new child to the current object.
     * The index will increase as the number of children increases, starting from {@code 0}.
     *
     * @param child The child you want to add.
     */
    void addChild(AutoCloseable child);

    /**
     * Delete all child objects by calling their {@linkplain AutoCloseable#close()} method.
     */
    void closeAllChild();

    /**
     * Remove {@code child} from the current object.
     * If successful, the {@code detachParent} of {@code child} will be automatically called.
     *
     * @param child The object you want to delete.
     */
    void removeChild(AutoCloseable child);

    /**
     * Returns whether the current object has children.
     * @return Whether the current object has children.
     */
    boolean hasChild();

    /**
     * Obtain the son based on the provided index.
     * @param idx The index.
     * @return The son.
     * @exception IndexOutOfBoundsException Throw when the provided quantity is greater than the maximum number of sub items.
     */
    AutoCloseable childAt(int idx);
}
