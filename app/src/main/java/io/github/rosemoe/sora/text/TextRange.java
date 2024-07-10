package io.github.rosemoe.sora.text;

import androidx.annotation.NonNull;

/**
 * A range made up of two {@link CharPosition} objects.
 *
 * @author Rosemoe
 */
public class TextRange {

    private CharPosition start;
    private CharPosition end;

    public TextRange(@NonNull CharPosition start, @NonNull CharPosition end) {
        this.start = start;
        this.end = end;
    }

    @NonNull
    public CharPosition getStart() {
        return start;
    }

    public void setStart(@NonNull CharPosition start) {
        this.start = start;
    }

    @NonNull
    public CharPosition getEnd() {
        return end;
    }

    public void setEnd(@NonNull CharPosition end) {
        this.end = end;
    }

    public int getStartIndex() {
        return start.getIndex();
    }

    public int getEndIndex() {
        return end.getIndex();
    }

    /**
     * Check if the given position is inside the range
     */
    public boolean isPositionInside(@NonNull CharPosition pos) {
        return pos.getIndex() >= start.getIndex() && pos.getIndex() < end.getIndex();
    }

    @NonNull
    @Override
    public String toString() {
        return "TextRange{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}