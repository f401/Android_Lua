package io.github.rosemoe.sora.lang.styling.inlayHint;

import android.graphics.Canvas;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.graphics.Paint;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * A general inlay hint of text. The hint text will be displayed in a round colored rect.
 *
 * @author Rosemoe
 */
public class TextInlayHint extends InlayHint {
    @NonNull
    private final String text;

    protected TextInlayHint(@NonNull String text) {
        super(InlayHintLayoutType.IN_LINE);
        this.text = text;
    }

    @Override
    protected float onMeasure(@NonNull Paint paint, @NonNull Paint.FontMetricsInt textMetrics, int lineHeight, float baseline) {
        float margin = paint.getSpaceWidth() * 0.8f;
        float textSize = paint.getTextSize();
        paint.setTextSizeWrapped(textSize * 0.75f);
        float width = paint.measureText(text) + margin * 3;
        paint.setTextSizeWrapped(textSize);
        return width;
    }

    @Override
    protected void onRender(@NonNull Canvas canvas, @NonNull Paint paint, @NonNull Paint.FontMetricsInt textMetrics, @NonNull EditorColorScheme colorScheme, int lineHeight, float baseline, float measuredWidth) {
        float margin = paint.getSpaceWidth() * 0.8f;
        float textSize = paint.getTextSize();
        paint.setTextSizeWrapped(textSize * 0.75f);

        float myLineHeight = paint.descent() - paint.ascent();
        float myBaseline = lineHeight / 2f - myLineHeight / 2f + paint.descent();
        paint.setColor(colorScheme.getColor(EditorColorScheme.TEXT_INLAY_HINT_BACKGROUND));
        canvas.drawRoundRect(
                margin,
                lineHeight / 2f - myLineHeight / 2f,
                measuredWidth - margin,
                lineHeight / 2f + myLineHeight / 2f,
                0.15f,
                0.15f,
                paint
        );
        paint.setColor(colorScheme.getColor(EditorColorScheme.TEXT_INLAY_HINT_FOREGROUND));
        canvas.drawText(text, margin * 1.5f, myBaseline, paint);

        paint.setTextSizeWrapped(textSize);
    }
}
