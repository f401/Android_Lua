package io.github.rosemoe.sora.widget.component;

import android.view.ContextMenu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.event.CreateContextMenuEvent;
import io.github.rosemoe.sora.event.EventManager;
import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Add context menu items for editor
 *
 * @author Rosemoe
 */
public class EditorContextMenuCreator implements EditorBuiltinComponent {

    @NonNull
    private final CodeEditor editor;
    private EventManager mEventManager;

    public EditorContextMenuCreator(@NonNull CodeEditor editor) {
        this.editor = editor;
        getEventManager().subscribeAlways(CreateContextMenuEvent.class, new EventManager.NoUnsubscribeReceiver<CreateContextMenuEvent>() {
            @Override
            public void onEvent(CreateContextMenuEvent event) {
                onCreateContextMenu(event);
            }
        });
    }

    protected void onCreateContextMenu(@NonNull CreateContextMenuEvent event) {
        ContextMenu menu = event.getMenu();

        MenuItem item = menu.add(0, 0, 0, android.R.string.copy);
        item.setEnabled(editor.isTextSelected());
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                editor.copyText();
                return true;
            }
        });

        item = menu.add(0, 1, 0, android.R.string.cut);
        item.setEnabled(editor.isTextSelected());
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                editor.cutText();
                return true;
            }
        });

        item = menu.add(0, 2, 0, android.R.string.copy);
        item.setEnabled(editor.hasClip());
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                editor.pasteText();
                return true;
            }
        });
    }

    @Override
    public boolean isEnabled() {
        return getEventManager().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        getEventManager().setEnabled(enabled);
    }

    @NonNull
    public EventManager getEventManager() {
        if (mEventManager == null) {
            mEventManager = editor.createSubEventManager();
        }
        return mEventManager;
    }
}
