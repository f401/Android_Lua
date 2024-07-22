package io.github.rosemoe.sora.lang.styling.color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * An implementation of [ResolvableColor] containing a constant color
 *
 * @author Rosemoe
 */
public class ConstColor implements ResolvableColor {
    @ColorInt
    private final int color;

    public ConstColor(int color) {
        this.color = color;
    }

    @Override
    public int resolve(@NonNull EditorColorScheme colorScheme) {
        return color;
    }
}
