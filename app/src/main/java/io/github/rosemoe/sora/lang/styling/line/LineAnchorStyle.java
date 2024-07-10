package io.github.rosemoe.sora.lang.styling.line;

import androidx.annotation.Nullable;

import com.google.common.primitives.Ints;

/**
 * The super class of all styles that are based on lines.
 * It is expected to be a sealed class.
 *
 * @author Rosemoe
 */
public abstract class LineAnchorStyle implements Comparable<LineAnchorStyle> {
    @Nullable
    private Object customData;
    private int line;

    public LineAnchorStyle(int line) {
        this.line = line;
    }

    @Nullable
    public Object getCustomData() {
        return customData;
    }

    public void setCustomData(@Nullable Object customData) {
        this.customData = customData;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    @Override
    public int compareTo(LineAnchorStyle o) {
        return Ints.compare(line, o.line);
    }
}
