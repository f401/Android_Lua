package io.github.rosemoe.sora.text;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CachedIndexerImpl implements IIndexer {
    private static final CharPosition START = new CharPosition(0, 0, 0);
    private final List<CharPosition> mCache;
    private final CharPosition mEndPosition;
    private final Content mContent;
    private final int mCacheSize;
    private final int mThresholdIndex;
    private final int mThresholdLine;

    public CachedIndexerImpl(Content content, int cacheSize) {
        this.mCache = Lists.newArrayListWithExpectedSize(cacheSize);
        this.mEndPosition = new CharPosition(0, 0, 0);
        this.mCacheSize = cacheSize;
        this.mContent = content;
        this.mThresholdIndex = 30;
        this.mThresholdLine = 30;

        updateEnd();
    }

    /**
     * Update the end position
     */
    private void updateEnd() {
        if (mContent.length() != 0) {
            mEndPosition.setIndex(mContent.length());
            mEndPosition.setLine(mContent.getLineCount() - 1);
            mEndPosition.setColumn(mContent.getColumnCount(mEndPosition.getLine()));
        }
    }

    /**
     * Get the nearest cache for the given index
     *
     * @param index Querying index
     * @return Nearest cache
     */
    @NonNull
    private synchronized CharPosition findNearestByIndex(int index) {
        int min = index, dis = index;
        CharPosition nearestCharPosition = START;
        int targetIndex = 0;
        for (int i = 0; i < mCache.size(); i++) {
            CharPosition pos = mCache.get(i);
            dis = Math.abs(pos.getIndex() - index);
            if (dis < min) {
                min = dis;
                nearestCharPosition = pos;
                targetIndex = i;
            }
            if (dis <= mThresholdIndex) {
                break;
            }
        }
        if (Math.abs(mEndPosition.getIndex() - index) < dis) {
            nearestCharPosition = mEndPosition;
        }
        if (nearestCharPosition != START && nearestCharPosition != mEndPosition) {
            Collections.swap(mCache, targetIndex, 0);
        }
        return nearestCharPosition;
    }

    /**
     * Get the nearest cache for the given line
     *
     * @param line Querying line
     * @return Nearest cache
     */
    @NonNull
    private synchronized CharPosition findNearestByLine(int line) {
        int min = line, dis = line;
        CharPosition nearestCharPosition = START;
        int targetIndex = 0;
        for (int i = 0; i < mCache.size(); i++) {
            CharPosition pos = mCache.get(i);
            dis = Math.abs(pos.getLine() - line);
            if (dis < min) {
                min = dis;
                nearestCharPosition = pos;
                targetIndex = i;
            }
            if (min <= mThresholdLine) {
                break;
            }
        }
        if (Math.abs(mEndPosition.getLine() - line) < dis) {
            nearestCharPosition = mEndPosition;
        }
        if (nearestCharPosition != START && nearestCharPosition != mEndPosition) {
            Collections.swap(mCache, 0, targetIndex);
        }
        return nearestCharPosition;
    }

    /**
     * From the given position to find forward in text
     *
     * @param start Given position
     * @param index Querying index
     */
     private void findIndexForward(@NonNull CharPosition start, int index, @NonNull CharPosition dest) {
         Preconditions.checkArgument(start.getIndex() <= index, "Unable to find backward from method findIndexForward()");
         int workLine = start.getLine();
        int workColumn = start.getColumn();
        int workIndex = start.getIndex();
        //Move the column to the line end
        {
            int addition = Math.max(mContent.getLineSeparator(workLine).length() - 1, 0);
            int column = mContent.getColumnCount(workLine) + addition;
            workIndex += column - workColumn;
            workColumn = column;
        }
        while (workIndex < index) {
            workLine++;
            int addition = Math.max(mContent.getLineSeparator(workLine).length() - 1, 0);
            workColumn = mContent.getColumnCount(workLine) + addition;
            workIndex += workColumn + 1;
        }
        if (workIndex > index) {
            workColumn -= workIndex - index;
        }
        dest.setColumn(workColumn);
        dest.setLine(workLine);
        dest.setIndex(index);
    }

    /**
     * From the given position to find backward in text
     *
     * @param start Given position
     * @param index Querying index
     */
    private void findIndexBackward(@NonNull CharPosition start, int index, @NonNull CharPosition dest) {
        Preconditions.checkArgument(start.getIndex() >= index, "Unable to find forward from method findIndexBackward()");
        int workLine = start.getLine();
        int workColumn = start.getColumn();
        int workIndex = start.getIndex();
        while (workIndex > index) {
            workIndex -= workColumn + 1;
            workLine--;
            if (workLine != -1) {
                int addition = Math.max(mContent.getLineSeparator(workLine).length() - 1, 0);
                workColumn = mContent.getColumnCount(workLine) + addition;
            } else {
                // Reached the start of text,we have to use findIndexForward() as this method can not handle it
                findIndexForward(START, index, dest);
                return;
            }
        }
        int dColumn = index - workIndex;
        if (dColumn > 0) {
            workLine++;
            workColumn = dColumn - 1;
        }
        dest.setColumn(workColumn);
        dest.setLine(workLine);
        dest.setIndex(index);
    }


    /**
     * From the given position to find forward in text
     *
     * @param start  Given position
     * @param line   Querying line
     * @param column Querying column
     */
    private void findLiCoForward(@NonNull CharPosition start, int line, int column, @NonNull CharPosition dest) {
        Preconditions.checkArgument(start.getLine() <= line, "can not find backward from findLiCoForward()");
        int workLine = start.getLine();
        int workIndex = start.getIndex();
        {
            //Make index to left of line
            workIndex = workIndex - start.getColumn();
        }
        while (workLine < line) {
            workIndex += mContent.getColumnCount(workLine) +
                         mContent.getLineSeparator(workLine).length();
            workLine++;
        }
        dest.setColumn(0);
        dest.setLine(workLine);
        dest.setIndex(workIndex);
        findInLine(dest, line, column);
    }

    /**
     * From the given position to find backward in text
     *
     * @param start  Given position
     * @param line   Querying line
     * @param column Querying column
     */
    private void findLiCoBackward(@NonNull CharPosition start, int line, int column, @NonNull CharPosition dest) {
        Preconditions.checkArgument(start.getLine() >= line, "can not find forward from findLiCoBackward()");
        int workLine = start.getLine();
        int workIndex = start.getIndex();
        {
            //Make index to the left of line
            workIndex = workIndex - start.getColumn();
        }
        while (workLine > line) {
            workIndex -= mContent.getColumnCount(workLine - 1) +
                         mContent.getLineSeparator(workLine - 1).length();
            workLine--;
        }
        dest.setColumn(0);
        dest.setLine(workLine);
        dest.setIndex(workIndex);
        findInLine(dest, line, column);
    }

    /**
     * From the given position to find in this line
     *
     * @param pos    Given position
     * @param line   Querying line
     * @param column Querying column
     */
    private void findInLine(@NonNull CharPosition pos, int line, int column) {
        Preconditions.checkArgument(line == pos.getLine(), "can not find other lines with findInLine()");
        pos.setIndex(pos.getIndex() - pos.getColumn() + column);
        pos.setColumn(column);
    }

    /**
     * Add new cache
     *
     * @param pos New cache
     */
    private synchronized void push(@NonNull CharPosition pos) {
        if (mCacheSize <= 0) {
            return;
        }
        mCache.add(pos);
        if (mCache.size() > mCacheSize) {
            mCache.remove(0);
        }
    }

    @Override
    public int getCharIndex(int line, int column) {
        return getCharPosition(line, column).getIndex();
    }

    @Override
    public int getCharLine(int index) {
        return getCharPosition(index).getLine();
    }

    @Override
    public int getCharColumn(int index) {
        return getCharPosition(index).getIndex();
    }

    @NonNull
    @Override
    public CharPosition getCharPosition(int index) {
        CharPosition result = new CharPosition(0, 0, 0);
        getCharPosition(index, result);
        return result;
    }

    @NonNull
    @Override
    public CharPosition getCharPosition(int line, int column) {
        CharPosition result = new CharPosition(0, 0, 0);
        getCharPosition(line, column, result);
        return result;
    }

    @Override
    public void getCharPosition(int index, CharPosition dest) {
        mContent.lock(Content.LockType.READ_LOCK);
        try {
            CharPosition pos = findNearestByIndex(index);
            if (pos.getIndex() == index) {
                dest.set(pos);
            } else if (pos.getIndex() < index) {
                findIndexForward(pos, index, dest);
            } else {
                findIndexBackward(pos, index, dest);
            }
            if (Math.abs(index - pos.getIndex()) >= mThresholdIndex) {
                push(dest.copy());
            }
        } finally {
            mContent.unlock(Content.LockType.READ_LOCK);
        }
    }

    @Override
    public void getCharPosition(int line, int column, CharPosition dest) {
        mContent.lock(Content.LockType.READ_LOCK);
        try {
            CharPosition pos = findNearestByLine(line);
            if (pos.getLine() == line) {
                dest.set(pos);
                if (pos.getColumn() == column) {
                    return;
                }
                findInLine(dest, line, column);
            } else if (pos.getLine() < line) {
                findLiCoForward(pos, line, column, dest);
            } else {
                findLiCoBackward(pos, line, column, dest);
            }
            if (Math.abs(pos.getLine() - line) > mThresholdLine) {
                push(dest.copy());
            }
        } finally {
            mContent.unlock(Content.LockType.READ_LOCK);
        }
    }

    @Override
    public void beforeReplace(@NonNull Content content) {
        // Do nothing
    }

    @Override
    public void afterBatchInsert(@NonNull List<Content.InsertContext> operates) {
        int minStartLine = Integer.MAX_VALUE, minStartColumn = Integer.MAX_VALUE,
                maxEndLine = Integer.MIN_VALUE, maxEndColumn = Integer.MIN_VALUE,
                addedIndex = 0, addedLine = 0;
        for (Content.InsertContext ctx: operates) {
            addedIndex += ctx.text.length();
            addedLine += (ctx.endLine - ctx.startLine);
            if (minStartLine > ctx.startLine && minStartColumn > ctx.startColumn) {
                minStartLine = ctx.startLine; minStartColumn = ctx.startColumn;
            }

            if (maxEndLine < ctx.endLine && maxEndColumn < ctx.endColumn) {
                maxEndLine = ctx.endLine; maxEndColumn = ctx.endColumn;
            }
        }
        final ArrayList<CharPosition> garbage = Lists.newArrayList();
        for (CharPosition pos : mCache) {
            if (pos.getLine() == minStartLine) {
                if (pos.getColumn() >= minStartColumn) {
                    garbage.add(pos);
                }
            } else if (pos.getLine() == maxEndLine) {
                if (pos.getColumn() <= maxEndColumn) {
                    garbage.add(pos);
                }
            } else if (pos.getLine() > minStartLine && pos.getLine() < maxEndLine) { // In the range
                garbage.add(pos);
            } else if (pos.getLine() > maxEndLine) { // Out of range.
                pos.setIndex(pos.getIndex() + addedIndex);
                pos.setLine(pos.getLine() + addedLine);
            }
        }
        mCache.removeAll(garbage);
        updateEnd();
    }

    @Override
    public void afterBatchDelete(@NonNull List<Content.DeleteContext> operates) {
        int minStartLine = Integer.MAX_VALUE, minStartColumn = Integer.MAX_VALUE,
                maxEndLine = Integer.MIN_VALUE, maxEndColumn = Integer.MIN_VALUE,
                reduceIndex = 0, reducedLine = 0;
        for (Content.DeleteContext ctx : operates) {
            reduceIndex += ctx.text.length();
            reducedLine += (ctx.endLine - ctx.startLine);
            if (minStartLine > ctx.startLine && minStartColumn > ctx.startColumn) {
                minStartLine = ctx.startLine; minStartColumn = ctx.startColumn;
            }

            if (maxEndLine < ctx.endLine && maxEndColumn < ctx.endColumn) {
                maxEndLine = ctx.endLine; maxEndColumn = ctx.endColumn;
            }
        }
        final ArrayList<CharPosition> garbage = Lists.newArrayList();
        for (CharPosition pos : mCache) {
            if (pos.getLine() == minStartLine) {
                if (pos.getColumn() >= minStartColumn) {
                    garbage.add(pos);
                }
            } else if (pos.getLine() == maxEndLine) {
                if (pos.getColumn() <= maxEndColumn) {
                    garbage.add(pos);
                }
            } else if (pos.getLine() > minStartLine && pos.getLine() < maxEndLine) { // In the range
                garbage.add(pos);
            } else if (pos.getLine() > maxEndLine) { // Out of range.
                pos.setIndex(pos.getIndex() - reduceIndex);
                pos.setLine(pos.getLine() - reducedLine);
            }
        }
        mCache.removeAll(garbage);
        updateEnd();
    }

    @Override
    public void afterInsert(@NonNull Content.InsertContext ctx) {
        for (CharPosition pos : mCache) {
            if (pos.getLine() == ctx.startLine) {
                if (pos.getColumn() >= ctx.startColumn) {
                    pos.setIndex(pos.getIndex() + ctx.text.length());
                    pos.setLine(pos.getLine() + (ctx.endLine - ctx.startLine));
                    pos.setColumn(ctx.endColumn + pos.getColumn() - ctx.startColumn);
                }
            } else if (pos.getLine() > ctx.startLine) {
                pos.setIndex(pos.getIndex() + ctx.text.length());
                pos.setLine(pos.getLine() + (ctx.endLine - ctx.startLine));
            }
        }
    }

    @Override
    public void afterDelete(@NonNull Content.DeleteContext ctx) {
        List<CharPosition> garbage = Lists.newArrayList();
        for (CharPosition pos : mCache) {
            if (pos.getLine() == ctx.startLine) {
                if (pos.getColumn() >= ctx.startColumn) {
                    garbage.add(pos);
                }
            } else if (pos.getLine() > ctx.startLine) {
                if (pos.getLine() <= ctx.endLine) {
                    garbage.add(pos);
                } else {
                    pos.setIndex(pos.getIndex() - ctx.text.length());
                    pos.setLine(pos.getLine() - (ctx.endLine - ctx.startLine));
                }
            }
        }
        mCache.removeAll(garbage);
        updateEnd();
    }
}
