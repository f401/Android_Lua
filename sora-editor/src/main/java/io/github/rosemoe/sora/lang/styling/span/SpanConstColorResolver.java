package io.github.rosemoe.sora.lang.styling.span;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.color.ConstColor;
import io.github.rosemoe.sora.lang.styling.color.ResolvableColor;

public final class SpanConstColorResolver implements SpanColorResolver {
    @ColorInt
    private final int foregroundColor, backgroundColor;

    /**
     * Override span's foreground and background with constant color.
     *
     * @param foreground Override foreground color, `0` for no override
     * @param background Override background color, `0` for no override
     * @author Rosemoe
     */
    public SpanConstColorResolver(int foreground, int background) {
        this.foregroundColor = foreground;
        this.backgroundColor = background;
    }

    @Nullable
    @Override
    public ResolvableColor getForegroundColor(@NonNull Span span) {
        if (foregroundColor == 0) {
            return null;
        }
        return new ConstColor(foregroundColor);
    }

    @Nullable
    @Override
    public ResolvableColor getBackgroundColor(@NonNull Span span) {
        if (backgroundColor == 0) {
            return null;
        }
        return new ConstColor(backgroundColor);
    }

}
