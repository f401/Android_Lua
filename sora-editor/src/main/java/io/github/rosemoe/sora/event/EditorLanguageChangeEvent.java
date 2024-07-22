package io.github.rosemoe.sora.event;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Editor [Language] changed
 */
public class EditorLanguageChangeEvent extends Event {
    @NonNull
    private final Language newLanguage;

    public EditorLanguageChangeEvent(@NonNull CodeEditor editor, @NonNull Language newLanguage) {
        super(editor);
        this.newLanguage = newLanguage;
    }

    @NonNull
    public Language getNewLanguage() {
        return newLanguage;
    }
}
