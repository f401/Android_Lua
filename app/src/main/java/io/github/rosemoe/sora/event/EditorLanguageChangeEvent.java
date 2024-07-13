package io.github.rosemoe.sora.event;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.lang.ILanguage;
import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Editor [Language] changed
 */
public class EditorLanguageChangeEvent extends Event {
    private final ILanguage newLanguage;

    public EditorLanguageChangeEvent(@NonNull CodeEditor editor, ILanguage newLanguage) {
        super(editor);
        this.newLanguage = newLanguage;
    }

    public ILanguage getNewLanguage() {
        return newLanguage;
    }
}
