package io.github.rosemoe.sora.event;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.rosemoe.sora.lang.styling.ISpan;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.TextRange;
import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Trigger when mouse right-clicked the editor
 */
public class ContextClickEvent extends EditorMotionEvent {
    @NonNull
    private final CharPosition position;
    @NonNull
    private final MotionEvent event;
    @Nullable
    private final ISpan span;
    @Nullable
    private final TextRange spanRange;

    public ContextClickEvent(@NonNull CodeEditor editor, @NonNull CharPosition position, @NonNull MotionEvent event, @Nullable ISpan span, @Nullable TextRange spanRange) {
        super(editor, position, event, span, spanRange);
        this.position = position;
        this.event = event;
        this.span = span;
        this.spanRange = spanRange;
    }

    @NonNull
    public CharPosition getPosition() {
        return position;
    }

    @NonNull
    public MotionEvent getEvent() {
        return event;
    }

    @Override
    @Nullable
    public ISpan getSpan() {
        return span;
    }

    @Override
    @Nullable
    public TextRange getSpanRange() {
        return spanRange;
    }
}
