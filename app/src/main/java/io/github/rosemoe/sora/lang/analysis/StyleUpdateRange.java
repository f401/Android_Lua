package io.github.rosemoe.sora.lang.analysis;

import java.util.Iterator;

/**
 * Describe the range of a style update
 *
 * @author Rosemoe
 */
public interface StyleUpdateRange {
    /**
     * Check whether the given [line] index is in range
     */
    boolean isInRange(int line);

    /**
     * Get a new iterator for line indices in range
     */
    Iterator<Integer> lineIndexIterator(int maxLineIndex);
}
