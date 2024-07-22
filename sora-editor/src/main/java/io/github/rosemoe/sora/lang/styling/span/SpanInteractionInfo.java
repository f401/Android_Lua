package io.github.rosemoe.sora.lang.styling.span;

import androidx.annotation.Nullable;

/**
 * Interaction attributes for a [Span].
 */
public interface SpanInteractionInfo extends SpanExt {
    boolean isClickable();

    boolean isLongClickable();

    boolean isDoubleClickable();

    @Nullable
    Object getData();
}
