package io.github.rosemoe.sora.event;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorSearcher;

/**
 * Event when search result is available in main thread.
 * Note that this event is also triggered when query is changed to null.
 *
 * @author Rosemoe
 */
public final class PublishSearchResultEvent extends Event {
    public PublishSearchResultEvent(@NonNull CodeEditor editor) {
        super(editor);
    }

    public EditorSearcher getSearcher() {
        return getEditor().getSearcher();
    }
}