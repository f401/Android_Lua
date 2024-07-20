package io.github.rosemoe.sora.event;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Trigger when the editor text size changed
 * @property oldTextSize text size before changed
 * @property newTextSize new text size after changed
 */
public class TextSizeChangeEvent extends Event {
    private final float oldTextSize, newTextSize;
    public TextSizeChangeEvent(@NonNull CodeEditor editor, float oldTextSize, float newTextSize) {
        super(editor);
        this.oldTextSize = oldTextSize;
        this.newTextSize = newTextSize;
    }

    public float getOldTextSize() {
        return oldTextSize;
    }

    public float getNewTextSize() {
        return newTextSize;
    }
}
