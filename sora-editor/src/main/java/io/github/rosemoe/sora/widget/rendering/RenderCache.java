package io.github.rosemoe.sora.widget.rendering;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.MutableIntList;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class RenderCache {
    private final ReentrantLock lock;
    private final MutableIntList lines;
    private final List<MeasureCacheItem> cache;
    private final int maxCacheCount = 75;

    public RenderCache() {
        this.lock = new ReentrantLock();
        this.lines = new MutableIntList();
        this.cache = new ArrayList<>();
    }

    @NonNull
    public MeasureCacheItem getOrCreateMeasureCache(int line) {
        lock.lock();
        try {
            MeasureCacheItem result = queryMeasureCache(line);
            if (result == null) {
                result = new MeasureCacheItem(line, null, 0L);
                cache.add(result);
                while (cache.size() > maxCacheCount && !cache.isEmpty()) {
                    cache.remove(0);
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public MeasureCacheItem queryMeasureCache(int line) {
        lock.lock();
        try {
            for (int i = 0; i < cache.size(); ++i) {
                MeasureCacheItem item = cache.get(i);
                if (item != null && item.getLine() == line) {
                    Collections.swap(cache, cache.size() - 1, i);// Move to back.
                    return item;
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
                for (MeasureCacheItem it : cache) {
                    if (it.getLine() > startLine) {
                        it.setLine(it.getLine() + (endLine - startLine));
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public void updateForDeletion(final int startLine, final int endLine) {
        if (startLine != endLine) {
            lines.removeRange(startLine, endLine);
            lock.lock();
            try {
                Iterators.removeIf(cache.iterator(), new Predicate<MeasureCacheItem>() {
                    @Override
                    public boolean apply(MeasureCacheItem input) {
                        return input.getLine() >= startLine && input.getLine() <= endLine;
                    }
                });
                for (MeasureCacheItem it : cache) {
                    if (it.getLine() > endLine) {
                        it.setLine(it.getLine() - (endLine - startLine));
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public void reset(int lineCount) {
        if (lines.size() > lineCount) {
            lines.removeRange(lineCount, lines.size());
        } else if (lines.size() < lineCount) {
            for (int i = 0; i < (lineCount - lines.size()); ++i) {
                lines.add(0);
            }
        }
        for (int i = 0; i < lines.size(); ++i) {
            lines.set(i, 0);
        }
        lock.lock();
        try {
            cache.clear();
        } finally {
            lock.unlock();
        }
    }
}
