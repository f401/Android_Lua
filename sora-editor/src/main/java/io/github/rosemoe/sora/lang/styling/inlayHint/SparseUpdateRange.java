package io.github.rosemoe.sora.lang.styling.inlayHint;

import android.util.SparseBooleanArray;

import androidx.annotation.NonNull;

import java.util.Iterator;

import io.github.rosemoe.sora.lang.analysis.StyleUpdateRange;

public final class SparseUpdateRange implements StyleUpdateRange {
    @NonNull
    private final SparseBooleanArray array;

    public SparseUpdateRange() {
        this.array = new SparseBooleanArray();
    }

    public void addLine(int line) {
        array.put(line, true);
    }

    @Override
    public boolean isInRange(int line) {
        return array.get(line);
    }

    @Override
    public Iterator<Integer> lineIndexIterator(final int maxLineIndex) {
        return new Iterator<Integer>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < array.size();
            }

            @Override
            public Integer next() {
                return Math.min(maxLineIndex, array.keyAt(index++));
            }
        };
    }
}
