package net.fred.lua.foreign.child;

public interface IChildPolicy {
    void addChild(AutoCloseable child);

    void closeAllChild();

    /**
     * Remove {@code child} from the current object.
     * If successful, the {@code detachParent} of {@code child} will be automatically called.
     *
     * @param child The object you want to delete.
     */
    void removeChild(AutoCloseable child);

    boolean hasChild();

    AutoCloseable childAt(int idx);
}
