package androidx.collection;

import androidx.annotation.IntRange;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;

/**
 * [MutableIntList] is a [MutableList]-like collection for [Int] values. It allows storing and
 * retrieving the elements without boxing. Immutable access is available through its base class
 * [IntList], which has a [List]-like interface.
 * <p>
 * This implementation is not thread-safe: if multiple threads access this container concurrently,
 * and one or more threads modify the structure of the list (insertion or removal for instance), the
 * calling code must provide the appropriate synchronization. It is also not safe to mutate during
 * reentrancy -- in the middle of a [forEach], for example. However, concurrent reads are safe.
 *
 * @constructor Creates a [MutableIntList] with a [capacity] of `initialCapacity`.
 */
public class MutableIntList extends IntList {
    public MutableIntList(int initialCapacity) {
        super(initialCapacity);
    }

    public MutableIntList() {
        this(16);
    }


    /**
     * Reduces the internal storage. If [capacity] is greater than [minCapacity] and [size], the
     * internal storage is reduced to the maximum of [size] and [minCapacity].
     *
     * @see #ensureCapacity
     */
    public void trim(int minCapacity) {
        int minSize = Math.max(minCapacity, size);
        if (content.length > minSize) {
            content = Arrays.copyOf(content, minSize);
        }
    }

    /**
     * Ensures that there is enough space to store [capacity] elements in the [MutableIntList].
     *
     * @see #trim
     */
    public void ensureCapacity(int capacity) {
        int[] oldContent = content;
        if (oldContent.length < capacity) {
            int newSize = Math.max(capacity, oldContent.length * 3 / 2);
            content = Arrays.copyOf(oldContent, newSize);
        }
    }

    /**
     * Removes the element at the given [index] and returns it.
     *
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [lastIndex], inclusive
     */
    public int removeAt(@IntRange(from = 0) int index) {
        Preconditions.checkPositionIndex(index, size);
        int[] content = this.content;
        int item = content[index];
        if (index != size - 1) {
            System.arraycopy(content, index + 1, content, index, size - index - 1);
        }
        size--;
        return item;
    }

    /**
     * Sets the value at [index] to [element].
     *
     * @return the previous value set at [index]
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [lastIndex], inclusive
     */
    public int set(@IntRange(from = 0) int index, int element) {
        Preconditions.checkPositionIndex(index, size);
        int old = content[index];
        content[index] = element;
        return old;
    }

    /**
     * Removes items from index [start] (inclusive) to [end] (exclusive).
     *
     * @throws IndexOutOfBoundsException if [start] or [end] isn't between 0 and [size], inclusive
     * @throws IllegalArgumentException  if [start] is greater than [end]
     */
    public void removeRange(@IntRange(from = 0) int start, @IntRange(from = 0) int end) {
        Preconditions.checkPositionIndexes(start, end, size);
        if (end != start) {
            if (end < size) {
                System.arraycopy(content, end, content, start, size - end);
            }
            size -= (end - start);
        }
    }

    /**
     * Removes [element] from the [MutableIntList]. If [element] was in the [MutableIntList] and was
     * removed, `true` will be returned, or `false` will be returned if the element was not found.
     */
    public boolean remove(int element) {
        int index = indexOf(element);
        if (index >= 0) {
            removeAt(index);
            return true;
        }
        return false;
    }

    /**
     * Adds [element] to the [MutableIntList] and returns `true`.
     */
    public boolean add(int element) {
        ensureCapacity(size + 1);
        content[size] = element;
        size++;
        return true;
    }

    /**
     * Adds [element] to the [MutableIntList] at the given [index], shifting over any elements at
     * [index] and after, if any.
     *
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [size], inclusive
     */
    public void add(@IntRange(from = 0) int index, int element) {
        Preconditions.checkPositionIndex(index, size);
        ensureCapacity(size + 1);
        if (index != size) {
            System.arraycopy(content, index, content, index + 1, size - index);
        }
        content[index] = element;
        size++;
    }

    /**
     * Adds all [elements] to the [MutableIntList] at the given [index], shifting over any elements
     * at [index] and after, if any.
     *
     * @return `true` if the [MutableIntList] was changed or `false` if [elements] was empty
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [size], inclusive.
     */
    public boolean addAll(@IntRange(from = 0) int index, int[] elements) {
        Preconditions.checkPositionIndex(index, size);
        if (elements.length == 0)
            return false;
        ensureCapacity(size + elements.length);
        if (index != size) {
            System.arraycopy(content, index, content, index + elements.length, size - index);
        }
        System.arraycopy(elements, 0, content, index, elements.length);
        size += elements.length;
        return true;
    }

    /**
     * Adds all [elements] to the [MutableIntList] at the given [index], shifting over any elements
     * at [index] and after, if any.
     *
     * @return `true` if the [MutableIntList] was changed or `false` if [elements] was empty
     * @throws IndexOutOfBoundsException if [index] isn't between 0 and [size], inclusive.
     */
    public boolean addAll(@IntRange(from = 0) int index, List<Integer> elements) {
        Preconditions.checkPositionIndex(index, size);
        if (elements.isEmpty())
            return false;
        ensureCapacity(size + elements.size());
        if (index != size) {
            System.arraycopy(content, index, content, index + elements.size(), size - index);
        }
        for (int i = 0; i < elements.size(); ++i) {
            content[index + i] = elements.get(i);
        }
        size += elements.size();
        return true;
    }

}
