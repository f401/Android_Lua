package io.github.rosemoe.sora.lang.styling.inlayHint;

import android.graphics.Canvas;

import androidx.annotation.NonNull;

import com.google.errorprone.annotations.ForOverride;

import io.github.rosemoe.sora.graphics.Paint;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * Base class for all inlay hints.
 *
 * @author Rosemoe
 */
public abstract class InlayHint {
    @NonNull
    private final InlayHintLayoutType type;
    private float measuredWidth;
    private long measureTimestamp;

    protected InlayHint(@NonNull InlayHintLayoutType type) {
        this.type = type;
        this.measuredWidth = 0f;
        this.measureTimestamp = 0;
    }

    public float getMeasuredWidth() {
        return measuredWidth;
    }

    public long getMeasureTimestamp() {
        return measureTimestamp;
    }

    public final float measure(@NonNull Paint paint, @NonNull Paint.FontMetricsInt textMetrics,
                               int lineHeight, float baseline) {
        measuredWidth = onMeasure(paint, textMetrics, lineHeight, baseline);
        measureTimestamp = System.nanoTime();
        return measuredWidth;
    }

    public final void render(@NonNull Canvas canvas, @NonNull Paint paint, @NonNull Paint.FontMetricsInt textMetrics,
                             @NonNull EditorColorScheme colorScheme, int lineHeight, float baseline, float measuredWidth) {
        onRender(canvas, paint, textMetrics, colorScheme, lineHeight, baseline, measuredWidth);
    }

    /**
     * Measure the width of this inlay hint so that editor can properly place all the elements.
     * Be careful that the given objects should not be modified, especially [paint] and [textMetrics]. They
     * are currently used by editor instance to measure and render.
     * [lineHeight] and [baseline] are given y offsets (considering y offset of target line top is 0). Because the
     * baseline can be different from the one computed directly from the given [textMetrics] when line spacing is set.
     * The method is called when editor measures text, for example when the text size or font is changed. So make sure do
     * that this method is fast enough to achieve good performance. If your width changes because other reasons, remember to
     * notify the editor in time.
     * [InlayHint] is only allowed to be place at span start or end. Illegal hints will be ignored.
     *
     * @param paint       the text paint currently used by editor
     * @param textMetrics the [FontMetricsInt] instance of the paint cached by editor
     * @param lineHeight  the general line height, with line spacing considered
     * @param baseline    the general baseline, with line spacing considered
     * @return the width of this inlay hint
     */
    @ForOverride
    protected abstract float onMeasure(@NonNull Paint paint, @NonNull Paint.FontMetricsInt textMetrics,
                                       int lineHeight, float baseline);

    /**
     * Render the inlay hint on the given canvas. The [Canvas.translate] is called in advance so you do
     * not need to consider the exact line index. The left of the given canvas is where you should start render
     * your content and the top of the given canvas is the top of target line.
     * Your measure width previously generated is passed to you. You are expected to make your content
     * in range, according to the [measuredWidth] and [lineHeight].
     *
     * @param canvas        the canvas to render your content
     * @param paint         the text paint currently used by editor
     * @param textMetrics   the [FontMetricsInt] instance of the paint cached by editor
     * @param colorScheme   the [EditorColorScheme] of editor
     * @param lineHeight    the general line height, with line spacing considered
     * @param baseline      the general baseline, with line spacing considered
     * @param measuredWidth the width previously measured
     */
    @ForOverride
    protected abstract void onRender(@NonNull Canvas canvas, @NonNull Paint paint, @NonNull Paint.FontMetricsInt textMetrics,
                                     @NonNull EditorColorScheme colorScheme, int lineHeight, float baseline, float measuredWidth);
}
