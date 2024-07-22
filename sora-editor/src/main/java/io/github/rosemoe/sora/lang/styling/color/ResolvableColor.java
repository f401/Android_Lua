package io.github.rosemoe.sora.lang.styling.color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import io.github.rosemoe.sora.lang.styling.span.SpanExt;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * Describe a color that can be resolved when rendering.
 *
 * @author Rosemoe
 */
public interface ResolvableColor extends SpanExt {

    /**
     * Resolve this color
     *
     * @return Color int
     */
    @ColorInt
    int resolve(@NonNull EditorColorScheme colorScheme);
}
