package io.github.rosemoe.sora.lang.styling.span;


import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.Nullable;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * External renderer for spans.
 * <p>
 * <p>
 * Users can attach this renderer to any span so that you can draw
 * extra content inside the rectangle on canvas of the span.
 * <p>
 * <p>
 * However, any implementation of this class should not create many new objects
 * in its methods but initialize your required resources when the renderer is
 * created.
 * <p>
 * <p>
 * Meanwhile, the renderer should be consistent with editor's current
 * color scheme. Use colors defined in your color schemes if possible.
 * <p>
 * <p>
 * Also, try to create universal renderers for a certain effect.
 * <p>
 * <p>
 * Note: the [Paint] object should not be modified or used to draw objects on the canvas
 * It is provided only for measuring. Use another [Paint] created by yourself instead.
 */
public interface SpanExternalRenderer extends SpanExt {
    boolean requirePreDraw();

    boolean requirePostDraw();

    /**
     * Called when the editor draws the given region.
     *
     * @param canvas      The canvas to draw
     * @param paint       Paint for measuring
     * @param colorScheme Current color scheme
     * @param preOrPost   True for preDraw, False for postDraw
     */
    void draw(@Nullable Canvas canvas, @Nullable Paint paint, @Nullable EditorColorScheme colorScheme, boolean preOrPost);

}
