package io.github.rosemoe.sora.event;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * This event is triggered after format result is available and is applied to the editor
 */
public final class EditorFormatEvent extends Event {
    private final boolean isSuccess;

    public EditorFormatEvent(@NonNull CodeEditor editor, boolean isSuccess) {
        super(editor);
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
