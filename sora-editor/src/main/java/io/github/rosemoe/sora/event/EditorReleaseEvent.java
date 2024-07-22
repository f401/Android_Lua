package io.github.rosemoe.sora.event;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Called when the editor is going to be released. That's when [CodeEditor.release] is
 * called. You may subscribe this event to release resources when you are holding editor-specific
 * resources.
 * <p>
 * Note that this event will only be triggered once on a certain editor.
 *
 * @author Rosemoe
 */
public final class EditorReleaseEvent extends Event {
    public EditorReleaseEvent(@NonNull CodeEditor editor) {
        super(editor);
    }
}
