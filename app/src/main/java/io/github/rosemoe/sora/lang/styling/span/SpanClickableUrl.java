package io.github.rosemoe.sora.lang.styling.span;

import androidx.annotation.NonNull;

public class SpanClickableUrl implements ISpanInteractionInfo {
    private final String link;

    public SpanClickableUrl(String link) {
        this.link = link;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @Override
    public boolean isLongClickable() {
        return false;
    }

    @Override
    public boolean isDoubleClickable() {
        return true;
    }

    /**
     * Return the URL
     */
    @NonNull
    @Override
    public String getData() {
        return link;
    }
}
