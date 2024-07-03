package net.fred.lua.editor.text;

import android.os.Parcelable;

public class UndoStack {

    public static abstract class ContentAction implements Parcelable {
        public abstract void undo(Content content);

        public abstract void redo(Content content);

        public abstract boolean canMerge(ContentAction action);

        public abstract void merge(ContentAction action);
    }
}
