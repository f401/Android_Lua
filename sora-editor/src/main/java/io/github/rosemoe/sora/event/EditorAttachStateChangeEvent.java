package io.github.rosemoe.sora.event;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Trigger when the editor is attached to window/detached from window
 */
public final class EditorAttachStateChangeEvent extends Event {
    private final boolean isAttachedToWindow;

    public EditorAttachStateChangeEvent(@NonNull CodeEditor editor, boolean isAttachedToWindow) {
        super(editor);
        this.isAttachedToWindow = isAttachedToWindow;
    }

    public boolean isAttachedToWindow() {
        return isAttachedToWindow;
    }
}
