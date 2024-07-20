package io.github.rosemoe.sora.util;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentLine;
import io.github.rosemoe.sora.text.ICUUtils;
import io.github.rosemoe.sora.text.TextRange;

/**
 * Utility class for working with characters and indexes.
 *
 * @author Akash Yadav
 */
public final class Chars {

    /**
     * Find the previous word and get its start position.
     */
    @NonNull
    public static CharPosition prevWordStart(@NonNull CharPosition position, @NonNull Content text) {
        return findWord(position, text, true).getStart();
    }

    /**
     * Find the next word and get its end position.
     */
    @NonNull
    public static CharPosition nextWordEnd(@NonNull CharPosition position, @NonNull Content text) {
        return findWord(position, text, false).getEnd();
    }

    /**
     * Find the previous/next word from the given [character position][position] in the given [text].
     *
     * @param reverse Whether to search for word in reverse or not.
     */
    @NonNull
    public static TextRange findWord(@NonNull CharPosition position, @NonNull Content text, boolean reverse) {
        if (reverse) {
            position.column -= 1;
        }
        if (position.column <= 0 && position.line > 0 && reverse) {
            int l = position.line - 1;
            CharPosition pos = new CharPosition(l, text.getLine(l).length());
            return new TextRange(pos, pos);
        }

        if (text.getColumnCount(position.line) == position.column &&
                position.line < text.getLineCount() - 1 && !reverse) {
            CharPosition pos = new CharPosition(position.line + 1, 0);
            return new TextRange(pos, pos);
        }

        int column = skipWs(text.getLine(position.line), position.column, reverse);
        return getWordRange(text, position.line, column, false);
    }

    /**
     * Get the range of the word at given character position.
     *
     * @param line   The line.
     * @param column The column.
     * @param useIcu Whether to use the ICU library to get word edges.
     * @return The word range.
     */
    @NonNull
    public static TextRange getWordRange(@NonNull Content text, int line, int column, boolean useIcu) {
        // Find word edges
        int startLine = line;
        int endLine = line;
        ContentLine lineObj = text.getLine(line);
        long edges = ICUUtils.getWordRange(lineObj, column, useIcu);
        int startOffset = IntPair.getFirst(edges);
        int endOffset = IntPair.getSecond(edges);
        int startColumn = startOffset;
        int endColumn = endOffset;
        if (startColumn == endColumn) {
            if (endColumn < lineObj.length()) {
                endColumn++;
            } else if (startColumn > 0) {
                startColumn--;
            } else {
                if (line > 0) {
                    int lastColumn = text.getColumnCount(line - 1);
                    startLine = line - 1;
                    startColumn = lastColumn;
                } else if (line < text.getLineCount() - 1) {
                    endLine = line + 1;
                    endColumn = 0;
                }
            }
        }
        return new TextRange(
                new CharPosition(startLine, startColumn, startOffset),
                new CharPosition(endLine, endColumn, endOffset)
        );
    }

    /**
     * Find the next/previous offset after/before [offset] skipping all the whitespaces.
     *
     * @param text    The text.
     * @param offset  The offset to start from.
     * @param reverse Whether to skip whitespaces towards index 0 or `text.length`.
     */
    public static int skipWs(@NonNull CharSequence text, int offset, boolean reverse) {
        int i = offset;
        while (true) {
            if ((reverse && i < 0) || (!reverse && i == text.length())) {
                break;
            }

            char c = text.charAt(i);
            if (!Character.isWhitespace(c) || (i == 0 && reverse))
                break;
            else {
                i += reverse ? -1 : 1;
            }
        }
        return i;
    }
}
