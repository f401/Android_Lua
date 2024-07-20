package io.github.rosemoe.sora.lang.styling.line;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.lang.styling.color.ResolvableColor;

public final class LineBackground extends LineAnchorStyle {
    @NonNull
    private ResolvableColor color;

    /**
     * Set custom line background color for the given line
     *
     * @param line  the line index
     * @param color the background color of this line
     * @author Rosemoe
     */
    public LineBackground(int line, @NonNull ResolvableColor color) {
        super(line);
        this.color = color;
    }

    @NonNull
    public ResolvableColor getColor() {
        return color;
    }

    public void setColor(@NonNull ResolvableColor color) {
        this.color = color;
    }
}
