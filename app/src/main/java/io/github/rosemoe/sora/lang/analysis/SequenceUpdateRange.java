package io.github.rosemoe.sora.lang.analysis;

import java.util.Iterator;

public final class SequenceUpdateRange implements StyleUpdateRange {
    private final int startLine, endLine;

    public SequenceUpdateRange(int startLine) {
        this(startLine, Integer.MAX_VALUE);
    }

    public SequenceUpdateRange(int startLine, int endLine) {
        this.startLine = startLine;
        this.endLine = endLine;
    }

    @Override
    public boolean isInRange(int line) {
        return line >= startLine && line <= endLine;
    }

    @Override
    public Iterator<Integer> lineIndexIterator(int maxLineIndex) {
        return new Iterator<Integer>() {
            private int currLine = startLine;
            @Override
            public boolean hasNext() {
                return currLine <= Math.min(endLine, maxLineIndex);
            }

            @Override
            public Integer next() {
                return currLine++;
            }
        };
    }
}
