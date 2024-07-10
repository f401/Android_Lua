package net.fred.lua.editor.text;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.collect.Lists;

import java.util.List;

public class UndoStack implements Content.OnContentChangeListener, Parcelable {

    public final static Creator<UndoStack> CREATOR = new Creator<UndoStack>() {
        @Override
        public UndoStack createFromParcel(Parcel parcel) {
            UndoStack o = new UndoStack();
            o.maxStackSize = parcel.readInt();
            o.stackPointer = parcel.readInt();
            o.undoEnabled = parcel.readInt() > 0;
            int count = parcel.readInt();
            while (count > 0) {
                o.actionStack.add(parcel.<ContentAction>readParcelable(UndoStack.class.getClassLoader()));
                count--;
            }
            return o;
        }

        @Override
        public UndoStack[] newArray(int flags) {
            return new UndoStack[flags];
        }
    };

    /**
     * The max time span limit for merging actions
     */
    private static long sMergeTimeLimit = 8000L;
    private final List<ContentAction> actionStack;
    private boolean undoEnabled;
    private int maxStackSize;
    private InsertAction insertAction;
    private DeleteAction deleteAction;
    private Content targetContent;
    private boolean replaceMark;
    private int stackPointer;
    private boolean ignoreModification;
    private boolean forceNewMultiAction;
    private TextRange[] memorizedCursorRange;

    /**
     * Create an UndoManager
     */
    public UndoStack() {
        actionStack = Lists.newLinkedList();
        replaceMark = false;
        insertAction = null;
        deleteAction = null;
        stackPointer = 0;
        ignoreModification = false;
    }

    /**
     * @see #setMergeTimeLimit(long)
     */
    public static long getMergeTimeLimit() {
        return sMergeTimeLimit;
    }

    /**
     * Set max time span limit for merging actions
     *
     * @param mergeTimeLimit Time in millisecond
     */
    public static void setMergeTimeLimit(long mergeTimeLimit) {
        UndoStack.sMergeTimeLimit = mergeTimeLimit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(maxStackSize);
        parcel.writeInt(stackPointer);
        parcel.writeInt(undoEnabled ? 1 : 0);
        parcel.writeInt(actionStack.size());
        for (ContentAction contentAction : actionStack) {
            parcel.writeParcelable(contentAction, flags);
        }
    }

    /**
     * Check whether we are currently in undo/redo operations
     */
    public boolean isModifyingContent() {
        return ignoreModification;
    }

    /**
     * Undo on the given Content
     *
     * @param content Undo Target
     */
    @Nullable
    public TextRange[] undo(Content content) {
        if (canUndo() && !isModifyingContent()) {
            ignoreModification = true;
            ContentAction action = actionStack.get(stackPointer - 1);
            action.undo(content);
            stackPointer--;
            ignoreModification = false;
            return action.cursor;
        }
        return null;
    }

    /**
     * Redo on the given Content
     *
     * @param content Redo Target
     */
    public void redo(Content content) {
        if (canRedo() && !isModifyingContent()) {
            ignoreModification = true;
            actionStack.get(stackPointer).redo(content);
            stackPointer++;
            ignoreModification = false;
        }
    }

    /**
     * Called by {@link Content}
     */
    void onExitBatchEdit() {
        forceNewMultiAction = true;
        if (!actionStack.isEmpty() && actionStack.get(actionStack.size() - 1) instanceof MultiAction) {
            MultiAction action = ((MultiAction) actionStack.get(actionStack.size() - 1));
            if (action.mActions.size() == 1) {
                actionStack.set(actionStack.size() - 1, action.mActions.get(0));
            }
        }
    }

    /**
     * Whether it can undo
     */
    public boolean canUndo() {
        return isUndoEnabled() && (stackPointer > 0);
    }

    /**
     * Whether it can redo
     */
    public boolean canRedo() {
        return isUndoEnabled() && (stackPointer < actionStack.size());
    }

    /**
     * Whether this UndoManager is enabled
     *
     * @return Whether enabled
     */
    public boolean isUndoEnabled() {
        return undoEnabled;
    }

    /**
     * Set whether enable this module
     *
     * @param enabled Enable or disable
     */
    public void setUndoEnabled(boolean enabled) {
        undoEnabled = enabled;
        if (!enabled) {
            cleanStack();
        }
    }

    /**
     * Get current max stack size
     *
     * @return max stack size
     */
    public int getMaxUndoStackSize() {
        return maxStackSize;
    }

    /**
     * Set a max stack size for this UndoManager
     *
     * @param maxSize max stack size
     */
    public void setMaxUndoStackSize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException(
                    "max size can not be zero or smaller.Did you want to disable undo module by calling setUndoEnabled()?");
        }
        maxStackSize = maxSize;
        cleanStack();
    }

    /**
     * Clean stack after add or state change
     * This is to limit stack size
     */
    private void cleanStack() {
        if (!undoEnabled) {
            actionStack.clear();
            stackPointer = 0;
        } else {
            while (stackPointer > 1 && actionStack.size() > maxStackSize) {
                actionStack.remove(0);
                stackPointer--;
            }
        }
    }

    /**
     * Clean the stack before pushing
     * If we are not at the end(Undo action executed),remove those actions
     */
    private void cleanBeforePush() {
        while (stackPointer < actionStack.size()) {
            actionStack.remove(actionStack.size() - 1);
        }
    }

    /**
     * Push a new {@link ContentAction} to stack
     * It will merge actions if possible
     *
     * @param action New {@link ContentAction}
     */
    private void pushAction(Content content, ContentAction action) {
        if (!isUndoEnabled()) {
            return;
        }
        cleanBeforePush();
        if (content.isInBatchEdit()) {
            if (actionStack.isEmpty()) {
                MultiAction a = new MultiAction();
                a.addAction(action);
                a.cursor = action.cursor;
                actionStack.add(a);
                stackPointer++;
            } else {
                ContentAction a = actionStack.get(actionStack.size() - 1);
                if (a instanceof MultiAction && !forceNewMultiAction) {
                    MultiAction ac = (MultiAction) a;
                    ac.addAction(action);
                } else {
                    MultiAction ac = new MultiAction();
                    ac.addAction(action);
                    ac.cursor = action.cursor;
                    actionStack.add(ac);
                    stackPointer++;
                }
            }
        } else {
            if (actionStack.isEmpty()) {
                actionStack.add(action);
                stackPointer++;
            } else {
                ContentAction last = actionStack.get(actionStack.size() - 1);
                if (last.canMerge(action)) {
                    last.merge(action);
                } else {
                    actionStack.add(action);
                    stackPointer++;
                }
            }
        }
        forceNewMultiAction = false;
        cleanStack();
    }

    public void exitReplaceMode() {
        if (replaceMark && deleteAction != null) {
            pushAction(targetContent, deleteAction);
        }
        replaceMark = false;
        targetContent = null;
    }

    @Override
    public void beforeReplace(@NonNull Content content) {
        if (ignoreModification) {
            return;
        }
        replaceMark = true;
        targetContent = content;
        memorizedCursorRange = content.getCursor().getRanges();
    }

    @Override
    public void afterInsert(@NonNull Content.InsertContext ctx) {
        if (ignoreModification) {
            return;
        }
        insertAction = new InsertAction();
        insertAction.startLine = ctx.startLine;
        insertAction.startColumn = ctx.startColumn;
        insertAction.endLine = ctx.endLine;
        insertAction.endColumn = ctx.endColumn;
        insertAction.text = ctx.text;
        if (replaceMark && deleteAction != null) {
            ReplaceAction rep = new ReplaceAction();
            rep.delete = deleteAction;
            rep.insert = insertAction;
            rep.cursor = memorizedCursorRange;
            pushAction(ctx.content, rep);
        } else {
            insertAction.cursor = memorizedCursorRange;
            pushAction(ctx.content, insertAction);
        }
        deleteAction = null;
        insertAction = null;
        replaceMark = false;
    }

    @Override
    public void afterDelete(@NonNull Content.DeleteContext ctx) {
        if (ignoreModification) {
            return;
        }
        deleteAction = new DeleteAction();
        deleteAction.endColumn = ctx.endColumn;
        deleteAction.startColumn = ctx.startColumn;
        deleteAction.endLine = ctx.endLine;
        deleteAction.startLine = ctx.startLine;
        deleteAction.text = ctx.text;
        deleteAction.cursor = memorizedCursorRange;
        if (!replaceMark) {
            pushAction(ctx.content, deleteAction);
        }
    }

    /**
     * Base class of content actions
     *
     * @author Rosemoe
     */
    @Override
    public void afterBatchInsert(@NonNull List<Content.InsertContext> operates) {
        if (ignoreModification) {
            return;
        }
        for (Content.InsertContext ctx : operates) {
            afterInsert(ctx);
        }
    }

    @Override
    public void afterBatchDelete(@NonNull List<Content.DeleteContext> operates) {
        if (ignoreModification) {
            return;
        }
        for (Content.DeleteContext ctx : operates) {
            afterDelete(ctx);
        }
    }

    public static abstract class ContentAction implements Parcelable {
        public transient TextRange[] cursor;

        public abstract void undo(Content content);

        public abstract void redo(Content content);

        public abstract boolean canMerge(ContentAction action);

        public abstract void merge(ContentAction action);
    }

    /**
     * Insert action model for UndoStack
     *
     * @author Rosemoe
     */
    public static final class InsertAction extends ContentAction {

        public static final Creator<InsertAction> CREATOR = new Creator<InsertAction>() {
            @Override
            public InsertAction createFromParcel(Parcel parcel) {
                InsertAction o = new InsertAction();
                o.startLine = parcel.readInt();
                o.startColumn = parcel.readInt();
                o.endLine = parcel.readInt();
                o.endColumn = parcel.readInt();
                o.text = parcel.readString();
                return o;
            }

            @Override
            public InsertAction[] newArray(int size) {
                return new InsertAction[size];
            }
        };
        public int startLine, endLine, startColumn, endColumn;
        public transient long createTime = System.currentTimeMillis();
        public CharSequence text;

        @Override
        public void undo(Content content) {
            content.delete(startLine, startColumn, endLine, endColumn);
        }

        @Override
        public void redo(Content content) {
            content.insert(startLine, startColumn, text);
        }

        @Override
        public boolean canMerge(ContentAction action) {
            if (action instanceof InsertAction) {
                InsertAction ac = (InsertAction) action;
                return (ac.startColumn == endColumn && ac.startLine == endLine
                        && ac.text.length() + text.length() < 10000
                        && Math.abs(ac.createTime - createTime) < sMergeTimeLimit);
            }
            return false;
        }

        @Override
        public void merge(ContentAction action) {
            if (!canMerge(action)) {
                throw new IllegalArgumentException();
            }
            InsertAction ac = (InsertAction) action;
            this.endColumn = ac.endColumn;
            this.endLine = ac.endLine;
            StringBuilder sb;
            if (text instanceof StringBuilder) {
                sb = (StringBuilder) text;
            } else {
                sb = new StringBuilder(text);
                text = sb;
            }
            sb.append(ac.text);
        }

        @NonNull
        @Override
        public String toString() {
            return "InsertAction{" +
                    "startLine=" + startLine +
                    ", endLine=" + endLine +
                    ", startColumn=" + startColumn +
                    ", endColumn=" + endColumn +
                    ", createTime=" + createTime +
                    ", text=" + text +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(startLine);
            parcel.writeInt(startColumn);
            parcel.writeInt(endLine);
            parcel.writeInt(endColumn);
            parcel.writeString(text.toString());
        }
    }

    /**
     * MultiAction saves several actions for UndoManager
     *
     * @author Rose
     */
    public static final class MultiAction extends ContentAction {

        public final static Creator<MultiAction> CREATOR = new Creator<MultiAction>() {
            @Override
            public MultiAction createFromParcel(Parcel parcel) {
                MultiAction o = new MultiAction();
                int count = parcel.readInt();
                while (count > 0) {
                    o.mActions.add(parcel.<ContentAction>readParcelable(MultiAction.class.getClassLoader()));
                    count--;
                }
                return o;
            }

            @Override
            public MultiAction[] newArray(int size) {
                return new MultiAction[size];
            }
        };
        private final List<ContentAction> mActions = Lists.newArrayList();

        public void addAction(ContentAction action) {
            if (mActions.isEmpty()) {
                mActions.add(action);
            } else {
                ContentAction last = mActions.get(mActions.size() - 1);
                if (last.canMerge(action)) {
                    last.merge(action);
                } else {
                    mActions.add(action);
                }
            }
        }

        @Override
        public void undo(Content content) {
            for (int i = mActions.size() - 1; i >= 0; i--) {
                mActions.get(i).undo(content);
            }
        }

        @Override
        public void redo(Content content) {
            for (int i = 0; i < mActions.size(); i++) {
                mActions.get(i).redo(content);
            }
        }

        @Override
        public boolean canMerge(ContentAction action) {
            return false;
        }

        @Override
        public void merge(ContentAction action) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(mActions.size());
            for (ContentAction action : mActions) {
                parcel.writeParcelable(action, flags);
            }
        }
    }

    /**
     * Delete action model for UndoManager
     *
     * @author Rose
     */
    public static final class DeleteAction extends ContentAction {

        public final static Creator<DeleteAction> CREATOR = new Creator<DeleteAction>() {
            @Override
            public DeleteAction createFromParcel(Parcel parcel) {
                DeleteAction o = new DeleteAction();
                o.startLine = parcel.readInt();
                o.startColumn = parcel.readInt();
                o.endLine = parcel.readInt();
                o.endColumn = parcel.readInt();
                o.text = parcel.readString();
                return o;
            }

            @Override
            public DeleteAction[] newArray(int size) {
                return new DeleteAction[size];
            }
        };
        public int startLine, endLine, startColumn, endColumn;
        public transient long createTime = System.currentTimeMillis();
        public CharSequence text;

        @Override
        public void undo(Content content) {
            content.insert(startLine, startColumn, text);
        }

        @Override
        public void redo(Content content) {
            content.delete(startLine, startColumn, endLine, endColumn);
        }

        @Override
        public boolean canMerge(ContentAction action) {
            if (action instanceof DeleteAction) {
                DeleteAction ac = (DeleteAction) action;
                return (ac.endColumn == startColumn && ac.endLine == startLine
                        && ac.text.length() + text.length() < 10000
                        && Math.abs(ac.createTime - createTime) < sMergeTimeLimit);
            }
            return false;
        }

        @Override
        public void merge(ContentAction action) {
            if (!canMerge(action)) {
                throw new IllegalArgumentException();
            }
            DeleteAction ac = (DeleteAction) action;
            this.startColumn = ac.startColumn;
            this.startLine = ac.startLine;
            StringBuilder sb;
            if (text instanceof StringBuilder) {
                sb = (StringBuilder) text;
            } else {
                sb = new StringBuilder(text);
                text = sb;
            }
            sb.insert(0, ac.text);
        }

        @NonNull
        @Override
        public String toString() {
            return "DeleteAction{" +
                    "startLine=" + startLine +
                    ", endLine=" + endLine +
                    ", startColumn=" + startColumn +
                    ", endColumn=" + endColumn +
                    ", createTime=" + createTime +
                    ", text=" + text +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(startLine);
            parcel.writeInt(startColumn);
            parcel.writeInt(endLine);
            parcel.writeInt(endColumn);
            parcel.writeString(text.toString());
        }
    }

    /**
     * Replace action model for UndoManager
     *
     * @author Rose
     */
    public static final class ReplaceAction extends ContentAction {

        public final static Creator<ReplaceAction> CREATOR = new Creator<ReplaceAction>() {
            @Override
            public ReplaceAction createFromParcel(Parcel parcel) {
                ReplaceAction o = new ReplaceAction();
                o.insert = parcel.readParcelable(ReplaceAction.class.getClassLoader());
                o.delete = parcel.readParcelable(ReplaceAction.class.getClassLoader());
                return o;
            }

            @Override
            public ReplaceAction[] newArray(int size) {
                return new ReplaceAction[size];
            }
        };
        public InsertAction insert;
        public DeleteAction delete;

        @Override
        public void undo(Content content) {
            insert.undo(content);
            delete.undo(content);
        }

        @Override
        public void redo(Content content) {
            delete.redo(content);
            insert.redo(content);
        }

        @Override
        public boolean canMerge(ContentAction action) {
            return false;
        }

        @Override
        public void merge(ContentAction action) {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public String toString() {
            return "ReplaceAction{" +
                    "insert=" + insert +
                    ", delete=" + delete +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeParcelable(insert, flags);
            parcel.writeParcelable(delete, flags);
        }
    }
}
