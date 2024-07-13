package io.github.rosemoe.sora.event;

import android.view.ContextMenu;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Trigger when the editor needs to create context menu
 *
 * @property menu [ContextMenu] for adding menu items
 * @property position Target text position of the menu
 */
public class CreateContextMenuEvent extends Event {
    @NonNull
    private final ContextMenu menu;
    @NonNull
    private final CharPosition position;

    public CreateContextMenuEvent(@NonNull CodeEditor editor, @NonNull ContextMenu menu, @NonNull CharPosition position) {
        super(editor);
        this.menu = menu;
        this.position = position;
    }

    @NonNull
    public ContextMenu getMenu() {
        return menu;
    }

    @NonNull
    public CharPosition getPosition() {
        return position;
    }
}
