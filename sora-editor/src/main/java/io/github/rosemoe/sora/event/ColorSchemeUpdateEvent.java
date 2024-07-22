package io.github.rosemoe.sora.event;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

public class ColorSchemeUpdateEvent extends Event {
    public ColorSchemeUpdateEvent(@NonNull CodeEditor editor) {
        super(editor);
    }

    public EditorColorScheme getColorScheme() {
        return getEditor().getColorScheme();
    }
}
