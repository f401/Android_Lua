package io.github.rosemoe.sora.event;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Event for ime private command execution. When [android.view.inputmethod.InputConnection.performPrivateCommand]
 * is called, this event will be triggered.
 * You can subscribe to this event in order to interact with your own inputmethod and thus implement
 * specific features between this editor and your IME app.
 *
 * @author Rosemoe
 * @see android.view.inputmethod.InputConnection.performPrivateCommand
 */
public class ImePrivateCommandEvent extends Event {
    private final String action;
    @Nullable
    private final Bundle data;

    public ImePrivateCommandEvent(@NonNull CodeEditor editor, String action, @Nullable Bundle data) {
        super(editor);
        this.action = action;
        this.data = data;
    }

    public String getAction() {
        return action;
    }

    @Nullable
    public Bundle getData() {
        return data;
    }
}
