package net.fred.lua.editor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.common.Logger;
import net.fred.lua.common.utils.ThrowableUtils;

public class TextLine {
    public static final int DEFAULT_SIZE = 32;
    public static final int DEFAULT_CACHE_SIZE = 16;

    private char[] contents;
    private final TextLineCache lineCache;
    private int lineCount, contentLength;

    public TextLine() {
        this(null, 1, 0);
    }

    public TextLine(@Nullable char[] buff, int lineCount, int contentLength) {
        this.contents = buff == null ? new char[DEFAULT_SIZE] : buff;
        this.lineCache = new TextLineCache(DEFAULT_CACHE_SIZE);
        this.lineCount = lineCount;
        this.contentLength = contentLength;
    }

    synchronized public void setBuffer(char[] newBuffer, int lineCount, int contentLength) {
        this.contents = newBuffer;
        this.lineCount = lineCount;
        this.contentLength = contentLength;
        lineCache.cleanAll();
    }

    public final void insert(char[] src, int off) {
        if (src == null) {
            Logger.e("Source is null." + ThrowableUtils.getCallerString());
            return;
        }
        ArgumentsChecker.checkSize(off);

        synchronized (this) {
            onBeginInsert(src, off);
            setupSpace(off, src.length);
            for (int i = 0; i < src.length; ++i) {
                char curr = src[i];
                if (curr == '\n') {
                    lineCount++;
                }
                contents[i + off] = curr;
            }
            contentLength += src.length;
            onAfterInsert(src, off);
        }
    }

    /**
     * Opening up space for @{code needle} on @{code off}.
     */
    private void setupSpace(int off, int needle) {
        char[] tmp = contents;
        if (needle + contentLength > contents.length) {
            tmp = new char[needle + contentLength + DEFAULT_SIZE];
        }
        System.arraycopy(contents, 0, tmp, 0, off);
        System.arraycopy(contents, off, tmp, off + needle, contentLength - off);
        contents = tmp;
        lineCache.invalidateCache(off);
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(contents, 0, contentLength);
    }

    /**
     * Called before insertion, no default implementation.
     * Called by @{link #insert};
     *
     * @param src Data to be inserted.
     * @param off Offset of inserted data.
     */
    protected void onBeginInsert(char[] src, int off) {
    }

    /**
     * Called after insertion, no default implementation.
     * Called by @{link #insert};
     *
     * @param src Data to be inserted.
     * @param off Offset of inserted data.
     */
    protected void onAfterInsert(char[] src, int off) {

    }
}
