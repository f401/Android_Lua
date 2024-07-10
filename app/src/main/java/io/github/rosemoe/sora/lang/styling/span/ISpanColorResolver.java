package io.github.rosemoe.sora.lang.styling.span;

import io.github.rosemoe.sora.lang.styling.ISpan;
import io.github.rosemoe.sora.lang.styling.color.IResolvableColor;

/**
 * Override the foreground/background color resolving process. This class can provide service for
 * multiple spans, at your option.
 */
public interface ISpanColorResolver extends ISpanExt {
    /**
     * Get override foreground color of the given [Span]. The given [Span] is readonly.
     * Return null if the color is not overrode by this resolver.
     */
    IResolvableColor getForegroundColor(ISpan span);

    /**
     * Get override background color of the given [Span]. The given [Span] is readonly.
     * Return null if the color is not overrode by this resolver.
     */
    IResolvableColor getBackgroundColor(ISpan span);
}
