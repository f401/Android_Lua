package io.github.rosemoe.sora.event;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Triggered when focus state is changed
 */
public final class EditorFocusChangeEvent extends Event {
    private final boolean isGainFocus;

    public EditorFocusChangeEvent(@NonNull CodeEditor editor, boolean isGainFocus) {
        super(editor);
        this.isGainFocus = isGainFocus;
    }

    public boolean isGainFocus() {
        return isGainFocus;
    }
}
