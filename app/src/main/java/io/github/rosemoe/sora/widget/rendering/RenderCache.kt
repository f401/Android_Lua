package io.github.rosemoe.sora.widget.rendering;

import androidx.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cache for editor rendering, including line-based data and measure
 * cache for recently accessed lines.
 *
 * @author Rosemoe
 */
public class RenderCache {
    private final ReentrantLock lock;
    private final ArrayList<Integer> lines;
    private final List<MeasureCacheItem> cache;
    private final int maxCacheCount = 75;

    public RenderCache() {
        this.lock = new ReentrantLock();
        this.lines = Lists.newArrayList();
        this.cache = Lists.newArrayList();
    }

    public MeasureCacheItem getOrCreateMeasureCache(int line) {
        MeasureCacheItem result = queryMeasureCache(line);
        if (result == null) {
            lock.lock();
            try {
                result = new MeasureCacheItem(line, null, 0);
                cache.add(result);
                // Clear cache
                while (cache.size() > maxCacheCount && !cache.isEmpty()) {
                    cache.remove(0);
                }
            } finally {
                lock.unlock();
            }
        }
        return result;
    }

    @Nullable
    public MeasureCacheItem queryMeasureCache(int line) {
        lock.lock();
        try {
            for (int i = 0; i < cache.size(); i++) {
                MeasureCacheItem curr = cache.get(i);
                if (curr != null && curr.getLine() == line) {
                    cache.remove(i);
                    cache.add(curr);// Move to last
                    return curr;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public int getStyleHash(int line) {
        return lines.get(line);
    }

    public void setStyleHash(int line, int hash) {
        lines.set(line, hash);
    }

    public void updateForInsertion(int startLine, int endLine) {
        if (startLine != endLine) {
            if (endLine - startLine == 1) {
                lines.add(startLine, 0);
            } else {
                lines.addAll(startLine, Collections.nCopies(endLine - startLine, 0));
            }

            lock.lock();
            try {
                for (MeasureCacheItem item : cache) {
                    if (item.getLine() > startLine) {
                        item.setLine(item.getLine() + (endLine - startLine));
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public void updateForDeletion(int startLine, int endLine) {
        if (startLine != endLine) {
            lines.subList(startLine, endLine).clear();
            final Range<Integer> range = Range.closed(startLine, endLine);
            lock.lock();
            try {
                Iterables.removeIf(cache, new Predicate<MeasureCacheItem>() {
                    @Override
                    public boolean apply(@Nullable MeasureCacheItem input) {
                        return range.contains(input.getLine());
                    }
                });
                for (MeasureCacheItem item : cache) {
                    if (item.getLine() > endLine) {
                        item.setLine(item.getLine() - (endLine - startLine));
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public void reset(int lineCount) {
        if (lines.size() >= lineCount) {
            lines.subList(lineCount, lines.size() - 1).clear();
        }
        Collections.fill(lines, 0);
        lock.lock();
        try {
            cache.clear();
        } finally {
            lock.unlock();
        }
    }
}
