package net.fred.lua.editor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.common.ArgumentsChecker;
import net.fred.lua.common.Logger;
import net.fred.lua.common.Pair;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.editor.lang.Language;

public class TextLine implements Text {
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

    @Override
    public final void insert(char[] src, int off) {
        if (src == null) {
            Logger.e("Source is null." + ThrowableUtils.getCallerString());
            return;
        }
        ArgumentsChecker.checkSize(off);

        onBeginInsert(src, off);
        synchronized (this) {
            setupSpace(off, src.length);
            for (int i = 0; i < src.length; ++i) {
                char curr = src[i];
                if (curr == '\n') {
                    lineCount++;
                }
                contents[i + off] = curr;
            }
            contentLength += src.length;
        }
        onAfterInsert(src, off);
    }

    @Override
    public void replace(int from, int to, @Nullable char[] replacement) {
        ArgumentsChecker.checkSize(from);
        ArgumentsChecker.checkSize(to);

        if (replacement == null) {
            synchronized (this) {
                setupSpace(from, to, 0);
            }
            return;
        }

        synchronized (this) {
            setupSpace(from, to, replacement.length);
            for (int i = 0; i < replacement.length; ++i) {
                char curr = replacement[i];
                if (curr == '\n') {
                    lineCount++;
                }
                contents[i + from] = curr;
            }
            contentLength -= to - from - replacement.length;
        }
    }

    /**
     * Opening up space for @{code needle} on @{code off}.
     */
    private void setupSpace(int off, int needle) {
        char[] tmp = contents;
        if (needle + contentLength > contents.length) {
            tmp = new char[needle + contentLength + DEFAULT_SIZE];
            System.arraycopy(contents, 0, tmp, 0, off);
        }
        System.arraycopy(contents, off, tmp, off + needle, contentLength - off);
        contents = tmp;
        lineCache.invalidateCache(off);
    }

    /**
     * Remove data from @{code off}, to @{code to},
     * and opening up @{code needle} space at @{code off}.
     */
    public void setupSpace(int off, int to, int needle) {
        for (int i = off; i < to; ++i) {
            if (contents[i] == '\n') {
                lineCount--;
            }
        }

        System.arraycopy(contents, to, contents, off + needle, contentLength - to);
        lineCache.invalidateCache(off);
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(contents, 0, contentLength);
    }

    public int getRowToOffBelong(int off) {
        ArgumentsChecker.checkSize(off);
        Pair<Integer, Integer> cachedLine = lineCache.getNearestLineByOffset(off);
        if (cachedLine.second == off) {
            return cachedLine.first;
        }
        synchronized (this) {
            if (cachedLine.second > off) {
                return findLineBackward(off, cachedLine.getFirst(), cachedLine.getSecond());
            }
            return findLineForward(off, cachedLine.getFirst(), cachedLine.getSecond());
        }
    }

    private int findLineForward(int needleOff, int cachedLine, int cachedOff) {
        int workingOff = cachedOff;
        int nearestMatchLine = cachedLine;
        while (workingOff <= needleOff) {
            if (contents[workingOff] == Language.NEWLINE) {
                lineCache.put(++nearestMatchLine, workingOff);
            }
            workingOff++;
        }
        return nearestMatchLine;
    }

    private int findLineBackward(int needleOff, int cachedLine, int cachedOff) {
        int workingOff = cachedOff;
        int nearestMatchLine = cachedLine;
        while (workingOff >= needleOff) {
            if (contents[workingOff] == Language.NEWLINE) {
                lineCache.put(--nearestMatchLine, workingOff);
            }
            workingOff--;
        }
        return nearestMatchLine;
    }

    public int getRowCount() {
        return lineCount;
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
