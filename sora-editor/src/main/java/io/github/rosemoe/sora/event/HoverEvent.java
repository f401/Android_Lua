package io.github.rosemoe.sora.event;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.TextRange;
import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Trigger when mouse hover updates
 */
public final class HoverEvent extends EditorMotionEvent {
    public HoverEvent(@NonNull CodeEditor editor, @NonNull CharPosition position, @NonNull MotionEvent event, @Nullable Span span, @Nullable TextRange spanRange) {
        super(editor, position, event, span, spanRange);
    }
}
