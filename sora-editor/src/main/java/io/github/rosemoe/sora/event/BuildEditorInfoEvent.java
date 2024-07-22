package io.github.rosemoe.sora.event;

import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * This event is triggered when editor is building its [EditorInfo] object for IPC.
 * You can customize the info to add extra information for the ime, due to implement specific
 * features between this editor and your own IME. But note that [EditorInfo.inputType], [EditorInfo.initialSelStart],
 * [EditorInfo.initialSelEnd] and [EditorInfo.initialCapsMode] should not be modified. They are
 * managed by editor self.
 *
 * @author Rosemoe
 */
public class BuildEditorInfoEvent extends Event {
    @NonNull
    private final EditorInfo editorInfo;

    public BuildEditorInfoEvent(@NonNull CodeEditor editor, @NonNull EditorInfo editorInfo) {
        super(editor);
        this.editorInfo = editorInfo;
    }

    @NonNull
    public EditorInfo getEditorInfo() {
        return editorInfo;
    }
}
