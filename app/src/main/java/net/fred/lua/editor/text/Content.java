package net.fred.lua.editor.text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.errorprone.annotations.concurrent.LockMethod;
import com.google.errorprone.annotations.concurrent.UnlockMethod;

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
    private int mTextLength;

    public Content(boolean threadSafe) {
        this.mLock = threadSafe ? new ReentrantReadWriteLock() : null;
        this.mTextLength = 0;
        this.mLines = Lists.newArrayList();
        this.mIndexer = new CachedIndexerImpl(this, 50);
        this.mDocumentVersion = new AtomicLong(0);
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

    @NonNull
    @Override
    public CharSequence subSequence(int start, int end) {
        Preconditions.checkPositionIndexes(start, end, mTextLength);
        return null;
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

    private void dispatchAfterInsert(InsertContext ctx) {
        // Todo: IMPL it
        mIndexer.afterInsert(ctx);
    }

    protected enum LockType {
        READ_LOCK, WRITE_LOCK
    }

    public interface OnContentChangeListener {
        void afterBatchInsert(@NonNull List<Content.InsertContext> operates);

        void afterBatchDelete(@NonNull List<Content.InsertContext> operates);

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