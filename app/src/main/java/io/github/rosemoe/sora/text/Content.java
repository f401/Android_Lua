package io.github.rosemoe.sora.text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.concurrent.LockMethod;
import com.google.errorprone.annotations.concurrent.UnlockMethod;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnegative;

public class Content implements CharSequence {

    public final static int DEFAULT_MAX_UNDO_STACK_SIZE = 500;
    public final static int DEFAULT_LIST_CAPACITY = 1000;

    @Nullable
    private final ReadWriteLock mLock;
    @NonNull
    private final List<ContentLine> mLines;
    private final AtomicLong mDocumentVersion;
    @NonNull
    private final IIndexer mIndexer;
    private final UndoStack mUndoStack;
    private Cursor mCursor;
    private int mTextLength, mNestedBatchEdit;

    public Content(boolean threadSafe) {
        this.mLock = threadSafe ? new ReentrantReadWriteLock() : null;
        this.mTextLength = 0;
        this.mLines = Lists.newArrayList();
        this.mIndexer = new CachedIndexerImpl(this, 50);
        this.mDocumentVersion = new AtomicLong(0);
        this.mUndoStack = new UndoStack();
        this.mNestedBatchEdit = 0;
    }

    @LockMethod("mLock")
    protected void lock(@NonNull LockType type) {
        if (mLock != null) {
            if (type == LockType.READ_LOCK) {
                this.mLock.readLock().lock();
            } else if (type == LockType.WRITE_LOCK) {
                this.mLock.writeLock().lock();
            }
        }
    }

    @UnlockMethod("mLock")
    protected void unlock(@NonNull LockType type) {
        if (mLock != null) {
            if (type == LockType.READ_LOCK) {
                this.mLock.readLock().unlock();
            } else if (type == LockType.WRITE_LOCK) {
                this.mLock.writeLock().unlock();
            }
        }
    }

    @Override
    @CheckReturnValue
    public int length() {
        return mTextLength;
    }

    @Override
    @CheckReturnValue
    public char charAt(int index) {
        Preconditions.checkElementIndex(index, mTextLength);
        lock(LockType.READ_LOCK);
        try {
            CharPosition position = mIndexer.getCharPosition(index);
            return mLines.get(position.getLine()).charAt(position.getColumn());
        } finally {
            unlock(LockType.READ_LOCK);
        }
    }


    /**
     * Get the character at the given position
     *
     * @param line   The line position of character
     * @param column The column position of character
     * @return The character at the given position
     */
    public char charAt(int line, int column) {
        lock(LockType.READ_LOCK);
        try {
            return mLines.get(line).charAt(column);
        } finally {
            unlock(LockType.READ_LOCK);
        }
    }

    @NonNull
    @Override
    public CharSequence subSequence(int start, int end) {
        Preconditions.checkPositionIndexes(start, end, mTextLength);
        lock(LockType.READ_LOCK);
        try {
            CharPosition startPos = mIndexer.getCharPosition(start);
            CharPosition endPos = mIndexer.getCharPosition(end);
            return doSubContent(startPos.getLine(), startPos.getColumn(),
                    endPos.getLine(), endPos.getColumn());
        } finally {
            unlock(LockType.READ_LOCK);
        }
    }

    /**
     * Get raw data of line.
     * The result should not be modified by code out of editor framework.
     *
     * @param line Line
     * @return Raw ContentLine used by Content
     */
    @NonNull
    @CheckReturnValue
    public ContentLine getLine(int line) {
        lock(LockType.READ_LOCK);
        try {
            return mLines.get(line);
        } finally {
            unlock(LockType.READ_LOCK);
        }
    }

    /**
     * Get how many lines there are
     *
     * @return Line count
     */
    @Nonnegative
    @CheckReturnValue
    public int getLineCount() {
        return mLines.size();
    }

    /**
     * Get how many characters is on the given line
     * If (line < 0 or line >= getLineCount()),it will throw a IndexOutOfBoundsException
     *
     * @param line The line to get
     * @return Character count on line
     */
    @Nonnegative
    @CheckReturnValue
    public int getColumnCount(int line) {
        return getLine(line).length();
    }

    @NonNull
    @CheckReturnValue
    protected LineSeparator getLineSeparator(int line) {
        return getLine(line).getLineSeparator();
    }

    @NonNull
    @CheckReturnValue
    public IIndexer getIndexer() {
        return this.mIndexer;
    }

    @NonNull
    @CheckReturnValue
    public Cursor getCursor() {
        return this.mCursor;
    }

    public long getDocumentVersion() {
        return mDocumentVersion.get();
    }

    /**
     * Get characters of line
     */
    public void getLineChars(int line, char[] dest) {
        getRegionOnLine(line, 0, getColumnCount(line), dest, 0);
    }


    /**
     * Get region of the given line
     *
     * @param dest   Destination of characters
     * @param offset Offset in dest to store the chars
     */
    public void getRegionOnLine(int line, int start, int end, char[] dest, int offset) {
        lock(LockType.READ_LOCK);
        try {
            mLines.get(line).getChars(start, end, dest, offset);
        } finally {
            unlock(LockType.READ_LOCK);
        }
    }

    /**
     * Insert content to this object
     *
     * @param line   The insertion's line position
     * @param column The insertion's column position
     * @param text   The text you want to insert at the position
     */
    public void insert(@Nonnegative int line, @Nonnegative int column, CharSequence text) {
        Preconditions.checkNotNull(text);
        lock(LockType.WRITE_LOCK);
        mDocumentVersion.getAndIncrement();
        try {
            InsertContext ctx = doInsert(line, column, text);
            dispatchAfterInsert(ctx);
        } finally {
            unlock(LockType.WRITE_LOCK);
        }
    }

    // Access from Cursor
    @CanIgnoreReturnValue
    protected InsertContext doInsert(int line, int column, @NonNull CharSequence text) {
        if (mLines.isEmpty()) {
            mLines.add(new ContentLine());// Lazy init
        }

        if (column > mLines.get(line).length()) {
            // Never insert texts between line separator characters
            column = mLines.get(line).length();
        }

        dispatchBeforeModification();
        int workLine = line;
        int workIndex = column;
        ContentLine currLine = mLines.get(workLine);
        InsertTextHelper helper = InsertTextHelper.forInsertion(text);
        int type, peekType = InsertTextHelper.TYPE_EOF;
        boolean fromPeek = false;
        LinkedList<ContentLine> newLines = Lists.newLinkedList();
        LineSeparator startSeparator = currLine.getLineSeparator();
        for (;;) {
            type = fromPeek ? peekType : helper.forward();
            fromPeek = false;
            if (type == InsertTextHelper.TYPE_EOF) {
                break;
            }
            if (type == InsertTextHelper.TYPE_LINE_CONTENT) {
                currLine.insert(workIndex, text, helper.getIndex(), helper.getIndexNext());
                workIndex += helper.getIndexNext() - helper.getIndex();
            } else {// InsertTextHelper.TYPE_NEW_LINE
                LineSeparator separator = LineSeparator.fromString(text, helper.getIndex(), helper.getIndexNext());
                currLine.setLineSeparator(separator);

                // Peek!
                peekType = helper.forward();
                fromPeek = true;

                ContentLine newLine = new ContentLine(currLine.length() - workIndex + helper.getIndexNext() - helper.getIndex() + 10);
                newLine.insert(0, currLine, workIndex, currLine.length());
                currLine.delete(workIndex, currLine.length());
                workIndex = 0;
                // Newly created lines are always mutable
                currLine = newLine;
                newLines.add(newLine);
                workLine++;
            }
        }

        currLine.setLineSeparator(startSeparator);
        mLines.addAll(line + 1, newLines);
        helper.recycle();
        mTextLength += text.length();
        return new InsertContext(this, line, column, workLine, workIndex, text);
    }

    /** NOTE: We must already lock */
    protected Content doSubContent(int startLine, int startColumn,
                                                 int endLine, int endColumn) {
        Content content = new Content(true);
        if (startLine == endLine) { // In the same line
            ContentLine line = mLines.get(startLine);
            if (endColumn == line.length() + 1 && line.getLineSeparator() == LineSeparator.CRLF) {
                if (startColumn < endColumn) { // contains line separator
                    content.insert(0, 0, line.subSequence(startColumn, line.length()));
                    content.mLines.get(0).setLineSeparator(LineSeparator.CR);
                    content.mTextLength++;
                }
            } else {
                content.insert(0, 0, line.subSequence(startColumn, endColumn));
            }
        } else if (startLine < endLine) {
            ContentLine firstLine = mLines.get(startLine);
            if (firstLine.getLineSeparator() == LineSeparator.CRLF) {
                if (startColumn <= firstLine.length()) {
                    content.insert(0, 0, firstLine.subSequence(startColumn, firstLine.length()));
                    content.mLines.get(0).setLineSeparator(firstLine.getLineSeparator());
                    content.mTextLength += firstLine.getLineSeparator().length();
                } else if (startColumn == firstLine.length() + 1) { // "\n"
                    content.mLines.get(0).setLineSeparator(LineSeparator.LF);
                    content.mTextLength += LineSeparator.LF.length();
                } else {
                    throw new IndexOutOfBoundsException();
                }
            } else {
                content.insert(0, 0, firstLine.subSequence(startColumn, firstLine.length()));
                content.mLines.get(0).setLineSeparator(firstLine.getLineSeparator());
                content.mTextLength += firstLine.getLineSeparator().length();
            }
            for (int i = startLine + 1; i < endLine; i++) {
                ContentLine line = mLines.get(i);
                content.mLines.add(new ContentLine(line));
                content.mTextLength += line.length() + line.getLineSeparator().length();
            }
            ContentLine end = mLines.get(endLine);
            if (endColumn == end.length() + 1 && end.getLineSeparator() == LineSeparator.CRLF) {
                ContentLine newLine = new ContentLine().insert(0, end, 0, endColumn - 1);
                content.mLines.add(newLine);
                newLine.setLineSeparator(LineSeparator.CR);
                content.mTextLength += endColumn + 1;
            } else {
                content.mLines.add(new ContentLine().insert(0, end, 0, endColumn));
                content.mTextLength += endColumn;
            }
        }
        return content;
    }

    /**
     * Delete character in [start,end)
     *
     * @param start Start position in content
     * @param end   End position in content
     */
    public void delete(int start, int end) {
        lock(LockType.WRITE_LOCK);
        mDocumentVersion.getAndIncrement();
        try {
            CharPosition startPos = getIndexer().getCharPosition(start);
            CharPosition endPos = getIndexer().getCharPosition(end);
            if (start != end) {
                dispatchAfterDelete(
                        doDelete(startPos.getLine(), startPos.getColumn(),
                                endPos.getLine(), endPos.getColumn()));
            }
        } finally {
            unlock(LockType.WRITE_LOCK);
        }
    }

    /**
     * Delete text in the given region
     *
     * @param startLine         The start line position
     * @param columnOnStartLine The start column position
     * @param endLine           The end line position
     * @param columnOnEndLine   The end column position
     */
    public void delete(int startLine, int columnOnStartLine, int endLine, int columnOnEndLine) {
        lock(LockType.WRITE_LOCK);
        mDocumentVersion.getAndIncrement();
        try {
            dispatchAfterDelete(
                    doDelete(startLine, columnOnStartLine, endLine, columnOnEndLine)
            );
        } finally {
            unlock(LockType.WRITE_LOCK);
        }
    }

    @Nullable
    private DeleteContext doDelete(int startLine, int columnOnStartLine, int endLine, int columnOnEndLine) {
        if (startLine == endLine && columnOnStartLine == columnOnEndLine) {
            return null;
        }

        ContentLine endLineObj = mLines.get(endLine);
        if (columnOnEndLine > endLineObj.length() && endLine + 1 < getLineCount()) {
            // Expected to delete the whole newline
            return doDelete(startLine, columnOnStartLine, endLine + 1, 0);
        }
        ContentLine startLineObj = mLines.get(startLine);
        if (columnOnStartLine > startLineObj.length()) {
            // Expected to delete the whole newline
            return doDelete(startLine, startLineObj.length(), endLine, columnOnEndLine);
        }

        StringBuilder changedContent = new StringBuilder();
        if (startLine == endLine) {
            ContentLine curr = mLines.get(startLine);
            int len = curr.length();
            Preconditions.checkPositionIndexes(columnOnStartLine, columnOnEndLine, len);

            dispatchBeforeModification();

            changedContent.append(curr, columnOnStartLine, columnOnEndLine);
            curr.delete(columnOnStartLine, columnOnEndLine);
            mTextLength -= columnOnEndLine - columnOnStartLine;
        } else if (startLine < endLine) {
            dispatchBeforeModification();
            for (int i = startLine + 1; i <= endLine - 1; i++) {
                ContentLine line = mLines.get(i);
                LineSeparator separator = mLines.get(i).getLineSeparator();
                mTextLength -= line.length() + separator.length();
                line.appendTo(changedContent);
                changedContent.append(separator.getChar());
            }
            if (endLine > startLine + 1) {
                mLines.subList(startLine + 1, endLine).clear();
            }

            int currEnd = startLine + 1;
            ContentLine start = mLines.get(startLine);
            ContentLine end = mLines.get(currEnd);
            mTextLength -= start.length() - columnOnStartLine;
            changedContent.insert(0, start, columnOnStartLine, start.length())
                    .insert(start.length() - columnOnStartLine, start.getLineSeparator().getChar());
            start.delete(columnOnStartLine, start.length());
            mTextLength -= columnOnEndLine;
            changedContent.append(end, 0, columnOnEndLine);
            mTextLength -= start.getLineSeparator().length();
            mLines.remove(currEnd);
            start.append(new TextReference(end, columnOnEndLine, end.length()));
            start.setLineSeparator(end.getLineSeparator());
        } else {
            throw new IllegalArgumentException("start line > end line");
        }
        return new DeleteContext(this, startLine, columnOnStartLine,
                endLine, columnOnEndLine, changedContent);
    }

    /**
     * Replace the text in the given region
     * This action will be completed by calling {@link Content#delete(int, int, int, int)} and {@link Content#insert(int, int, CharSequence)}
     *
     * @param startLine         The start line position
     * @param columnOnStartLine The start column position
     * @param endLine           The end line position
     * @param columnOnEndLine   The end column position
     * @param text              The text to replace old text
     */
    public void replace(int startLine, int columnOnStartLine, int endLine, int columnOnEndLine, CharSequence text) {
        if (text == null) {
            throw new IllegalArgumentException("text can not be null");
        }
        lock(LockType.WRITE_LOCK);
        mDocumentVersion.getAndIncrement();
        try {
            dispatchBeforeReplace();
            dispatchAfterDelete(doDelete(startLine, columnOnStartLine, endLine, columnOnEndLine));
            dispatchAfterInsert(doInsert(startLine, columnOnStartLine, text));
            // TODO dispatch after replace
        } finally {
            unlock(LockType.WRITE_LOCK);
        }
    }

    /**
     * Replace text in the given region with the text
     */
    public void replace(int startIndex, int endIndex, @NonNull CharSequence text) {
        CharPosition start = getIndexer().getCharPosition(startIndex);
        CharPosition end = getIndexer().getCharPosition(endIndex);
        replace(start.getLine(), start.getColumn(),
                end.getLine(), end.getColumn(), text);
    }

    private void dispatchBeforeReplace() {
        // TODO: IMPL it
        mIndexer.beforeReplace(this);
        mUndoStack.beforeReplace(this);
    }

    private void dispatchAfterDelete(@Nullable DeleteContext deleteContext) {
        // TODO: IMPL it
        if (deleteContext != null) {
            mUndoStack.afterDelete(deleteContext);
            mIndexer.afterDelete(deleteContext);
        }
    }

    private void dispatchAfterInsert(@Nullable InsertContext ctx) {
        // TODO: IMPL it
        if (ctx != null) {
            mUndoStack.afterInsert(ctx);
            mIndexer.afterInsert(ctx);
        }
    }

    private void dispatchBeforeModification() {
        mUndoStack.beforeModification(this);
        // TODO: IMPL
    }

    @NonNull
    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    public StringBuilder toStringBuilder() {
        StringBuilder sb = new StringBuilder();
        appendToStringBuilder(sb);
        return sb;
    }

    /**
     * Append the content to the given {@link StringBuilder}
     */
    public void appendToStringBuilder(@NonNull StringBuilder sb) {
        sb.ensureCapacity(sb.length() + length());
        lock(LockType.READ_LOCK);
        try {
            final int lines = getLineCount();
            for (int i = 0; i < lines; i++) {
                ContentLine line = this.mLines.get(i);
                line.appendTo(sb);
                sb.append(line.getLineSeparator().getChar());
            }
        } finally {
            unlock(LockType.READ_LOCK);
        }
    }

    /**
     * Copy text in this Content object.
     * Returns a new thread-safe Content object with the same text as this object. By default, the object is
     * thread-safe and access operations are locked when accessed by multiple threads.
     */
    public Content copyText() {
        return copyText(true);
    }

    /**
     * Copy text in this Content object.
     * Returns a new Content object with the same text as this object.
     */
    public Content copyText(boolean newContentThreadSafe) {
        lock(LockType.READ_LOCK);
        try {
            Content n = new Content(newContentThreadSafe);
            n.mLines.clear();
            ((ArrayList<ContentLine>) n.mLines).ensureCapacity(getLineCount());

            for (ContentLine line : mLines) {
                n.mLines.add(new ContentLine(line));
            }
            n.mTextLength = mTextLength;
            return n;
        } finally {
            unlock(LockType.READ_LOCK);
        }
    }

    /**
     * A delegate method.
     * Notify the UndoManager to begin batch edit(enter a new layer).
     * NOTE: batch edit in Android can be nested.
     *
     * @return Whether in batch edit
     */
    public boolean beginBatchEdit() {
        mNestedBatchEdit++;
        return isInBatchEdit();
    }

    /**
     * A delegate method.
     * Notify the UndoManager to end batch edit(exit current layer).
     *
     * @return Whether in batch edit
     */
    public boolean endBatchEdit() {
        mNestedBatchEdit--;
        if (mNestedBatchEdit == 0) {
            mUndoStack.onExitBatchEdit();
        }
        if (mNestedBatchEdit < 0) {
            mNestedBatchEdit = 0;
        }
        return isInBatchEdit();
    }

    public int getNestedBatchEdit() {
        return mNestedBatchEdit;
    }

    public void resetBatchEdit() {
        mNestedBatchEdit = 0;
    }

    /**
     * Returns whether we are in batch edit
     *
     * @return Whether in batch edit
     */
    public boolean isInBatchEdit() {
        return mNestedBatchEdit > 0;
    }

    /**
     * Set whether enable this module
     *
     * @param enabled Enable or disable
     * @see UndoStack#setUndoEnabled(boolean)
     */
    public void setUndoEnabled(boolean enabled) {
        mUndoStack.setUndoEnabled(enabled);
    }

    /**
     * Undo on the given Content
     *
     * @see UndoStack#undo
     */
    public TextRange[] undo() {
        return mUndoStack.undo(this);
    }

    /**
     * Redo on the given Content
     *
     * @see UndoStack#redo
     */
    public void redo() {
        mUndoStack.redo(this);
    }

    /**
     * Whether it can undo
     *
     * @see UndoStack#canUndo()
     */
    public boolean canUndo() {
        return mUndoStack.canUndo();
    }

    public boolean canRedo() {
        return mUndoStack.canRedo();
    }

    /**
     * Read the lines (ordered).
     * This is for optimizing frequent lock acquiring.
     *
     * @param startLine inclusive
     * @param endLine   inclusive
     */
    public void runReadActionsOnLines(int startLine, int endLine, @NonNull ContentLineConsumer consumer) {
        lock(LockType.READ_LOCK);
        try {
            for (int i = startLine; i <= endLine; i++) {
                consumer.accept(i, mLines.get(i));
            }
        } finally {
            unlock(LockType.READ_LOCK);
        }
    }

    /**
     * Read the lines (ordered).
     * This is for optimizing frequent lock acquiring.
     *
     * @param startLine inclusive
     * @param endLine   inclusive
     */
    public void runReadActionsOnLines(int startLine, int endLine, @NonNull ContentLineConsumer2 consumer) {
        lock(LockType.READ_LOCK);
        try {
            ContentLineConsumer2.AbortFlag flag = new ContentLineConsumer2.AbortFlag();
            for (int i = startLine; i <= endLine && !flag.set; i++) {
                consumer.accept(i, mLines.get(i), flag);
            }
        } finally {
            unlock(LockType.READ_LOCK);
        }
    }


    public interface ContentLineConsumer {

        void accept(int lineIndex, @NonNull ContentLine line);

    }

    public interface ContentLineConsumer2 {

        void accept(int lineIndex, @NonNull ContentLine line, @NonNull AbortFlag flag);

        class AbortFlag {
            public boolean set = false;
        }

    }


    protected enum LockType {
        READ_LOCK, WRITE_LOCK
    }

    public interface OnContentChangeListener {
        void beforeModification(@NonNull Content content);

        void beforeReplace(@NonNull Content content);

        void afterBatchInsert(@NonNull List<Content.InsertContext> operates);

        void afterBatchDelete(@NonNull List<DeleteContext> operates);

        void afterInsert(@NonNull Content.InsertContext ctx);

        void afterDelete(@NonNull Content.DeleteContext ctx);
    }

    public static class InsertContext {
        public @NonNull Content content;
        public int startLine;
        public int startColumn;
        public int endLine;
        public int endColumn;
        public @NonNull CharSequence text;

        public InsertContext(@NonNull Content content, int startLine, int startColumn, int endLine, int endColumn, @NonNull CharSequence text) {
            this.content = content;
            this.startLine = startLine;
            this.startColumn = startColumn;
            this.endLine = endLine;
            this.endColumn = endColumn;
            this.text = text;
        }
    }

    public static class DeleteContext {
        @NonNull
        public Content content;
        public int startLine;
        public int startColumn;
        public int endLine;
        public int endColumn;
        @NonNull
        public CharSequence text;

        public DeleteContext(@NonNull Content content, int startLine, int startColumn, int endLine, int endColumn, @NonNull CharSequence text) {
            this.content = content;
            this.startLine = startLine;
            this.startColumn = startColumn;
            this.endLine = endLine;
            this.endColumn = endColumn;
            this.text = text;
        }
    }
}