package io.github.rosemoe.sora.lang.styling.span;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.color.ResolvableColor;

/**
 * Override the foreground/background color resolving process. This class can provide service for
 * multiple spans, at your option.
 */
public interface SpanColorResolver extends SpanExt {
    /**
     * Get override foreground color of the given [Span]. The given [Span] is readonly.
     * Return null if the color is not overrode by this resolver.
     */
    @Nullable
    ResolvableColor getForegroundColor(@NonNull Span span);

    /**
     * Get override background color of the given [Span]. The given [Span] is readonly.
     * Return null if the color is not overrode by this resolver.
     */
    @Nullable
    ResolvableColor getBackgroundColor(@NonNull Span span);
}
