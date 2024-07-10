package io.github.rosemoe.sora.lang.styling.span;

import android.graphics.Canvas;
import android.graphics.Paint;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public interface ISpanExternalRenderer extends ISpanExt {
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
    void draw(Canvas canvas, Paint paint, EditorColorScheme colorScheme, boolean preOrPost);
}
