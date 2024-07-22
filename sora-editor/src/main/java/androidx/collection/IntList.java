package androidx.collection;


import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import java.util.Arrays;
import java.util.Iterator;

/**
 * [IntList] is a [List]-like collection for [Int] values. It allows retrieving the elements without
 * boxing. [IntList] is always backed by a [MutableIntList], its [MutableList]-like subclass. The
 * purpose of this class is to avoid the performance overhead of auto-boxing due to generics since
 * [Collection] classes all operate on objects.
 * <p>
 * This implementation is not thread-safe: if multiple threads access this container concurrently,
 * and one or more threads modify the structure of the list (insertion or removal for instance), the
 * calling code must provide the appropriate synchronization. It is also not safe to mutate during
 * reentrancy -- in the middle of a [forEach], for example. However, concurrent reads are safe.
 */
public class IntList implements Iterable<Integer> {
    protected int[] content;
    /**
     * The number of elements in the [IntList].
     */
    protected int size;

    public IntList(int initialCapacity) {
        this.content = new int[initialCapacity];
        this.size = 0;
    }

    /**
     * The number of elements in the [IntList].
     */
    @IntRange(from = 0)
    public int size() {
        return size;
    }

    /**
     * Calls [block] for each element in the [IntList], in order.
     *
     * @param block will be executed for every element in the list, accepting an element from the
     *              list
     */
    public void forEach(@NonNull Consumer<Integer> block) {
        for (int i = 0; i < size; i++) {
            block.accept(content[i]);
        }
    }

    /**
     * Calls [block] for each element in the [IntList] along with its index, in order.
     *
     * @param block will be executed for every element in the list, accepting the index and the
     *              element at that index.
     */
    public void forEachIndexed(@NonNull IndexedForEachConsumer block) {
        for (int i = 0; i < size; i++) {
            block.consume(i, content[i]);
        }
    }

    /**
     * Returns the element at the given [index] or throws [IndexOutOfBoundsException] if the [index]
     * is out of bounds of this collection.
     */
    public int get(@IntRange(from = 0) int index) {
        Preconditions.checkPositionIndex(index, size);
        return content[index];
    }

    /**
     * Returns `true` if the [IntList] has no elements in it or `false` otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns `true` if the [IntList] contains [element] or `false` otherwise.
     */
    public boolean contains(int element) {
        for (int i : this) {
            if (element == i) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all elements in the [MutableIntList]. The storage isn't released.
     */
    public void clear() {
        this.size = 0;
    }


    /**
     * Returns an [IntRange] of the valid indices for this [IntList].
     */
    @NonNull
    public Range<Integer> getIndices() {
        return Range.closedOpen(0, size);
    }

    /**
     * Returns the index of [element] in the [IntList] or `-1` if [element] is not there.
     */
    public int indexOf(int element) {
        for (int i = 0; i < size; i++) {
            if (content[i] == element) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int curr = 0;

            @Override
            public boolean hasNext() {
                return curr < size;
            }

            @Override
            public Integer next() {
                return content[curr++];
            }
        };
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IntList)) return false;

        IntList intList = (IntList) o;
        return size == intList.size && Arrays.equals(content, intList.content);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(content);
        result = 31 * result + size;
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "IntList{" +
                "content=" + Arrays.toString(content) +
                ", size=" + size +
                '}';
    }

    public interface IndexedForEachConsumer {
        void consume(int index, int value);
    }
}
