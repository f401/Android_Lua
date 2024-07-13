package io.github.rosemoe.sora.widget.rendering;

import androidx.annotation.Nullable;

/**
 * Measure cache item.
 *
 * @author Rosemoe
 * @property line The line index for this cache
 * @property widths Measured widths of characters in this line
 * @property updateTimestamp Last updated timestamp of this cache
 */
public class MeasureCacheItem {
    private int line;
    @Nullable
    private float[] widths;
    private long updateTimestamp;

    public MeasureCacheItem(int line, @Nullable float[] widths, long updateTimestamp) {
        this.line = line;
        this.widths = widths;
        this.updateTimestamp = updateTimestamp;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    @Nullable
    public float[] getWidths() {
        return widths;
    }

    public void setWidths(@Nullable float[] widths) {
        this.widths = widths;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }
}
