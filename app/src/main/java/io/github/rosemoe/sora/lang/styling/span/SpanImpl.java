package io.github.rosemoe.sora.lang.styling.span;

import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import io.github.rosemoe.sora.lang.styling.ISpan;
import io.github.rosemoe.sora.lang.styling.SpanPool;
import io.github.rosemoe.sora.lang.styling.color.IResolvableColor;

public class SpanImpl implements ISpan {
    private static final SpanPool<SpanImpl> POOL = new SpanPool<>(new SpanPool.Factory<SpanImpl>() {
        @NonNull
        @Override
        public SpanImpl create(int column, long style) {
            return new SpanImpl(column, style);
        }
    });
    private int column;
    private long style;
    private Object extra;
    private ArrayMap<Integer, ISpanExt> extMap;
    public SpanImpl(int column, long style) {
        this.column = column;
        this.style = style;
    }

    public static SpanImpl obtain(int column, long style) {
        return POOL.obtain(column, style);
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public void setColumn(int column) {
        this.column = column;
    }

    @Override
    public long getStyle() {
        return this.style;
    }

    @Override
    public void setStyle(long style) {
        this.style = style;
    }

    @Nullable
    @Override
    public IResolvableColor getUnderlineColor() {
        return getSpanExt(SpanExtAttrs.EXT_UNDERLINE_COLOR);
    }

    @Override
    public void setUnderlineColor(@Nullable IResolvableColor color) {
        setSpanExt(SpanExtAttrs.EXT_UNDERLINE_COLOR, color);
    }

    @Override
    public Object getExtra() {
        return extra;
    }

    @Override
    public void setExtra(Object extraData) {
        this.extra = extraData;
    }

    @Override
    public void setSpanExt(int extType, @Nullable ISpanExt ext) {
        Preconditions.checkArgument(SpanExtAttrs.checkType(extType, ext), "type mismatch: extType " + extType + " and extObj " + ext);
        if (ext == null) {
            if (extMap != null) {
                extMap.remove(extType);
            }
            return;
        }
        if (extMap == null) {
            extMap = new ArrayMap<>();
        }
        extMap.put(extType, ext);
    }

    @Override
    public boolean hasSpanExt(int extType) {
        return getSpanExt(extType) != null;
    }

    @Nullable
    public <T extends ISpanExt> T getSpanExt(int extType) {
        return extMap == null ? null : (T) extMap.get(extType);
    }

    @Override
    public void removeAllSpanExt() {
        if (extMap != null) {
            extMap.clear();
        }
    }

    @Override
    public void reset() {
        setColumn(0);
        setStyle(0L);
        removeAllSpanExt();
    }

    @NonNull
    @Override
    public ISpan copy() {
        SpanImpl copy = new SpanImpl(column, style);
        copy.extMap = new ArrayMap<>();
        copy.extMap.putAll(this.extMap);
        return copy;
    }

    @Override
    public boolean recycle() {
        reset();
        return POOL.offer(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpanImpl)) return false;
        SpanImpl span = (SpanImpl) o;
        return column == span.column && style == span.style
                && Objects.equal(extra, span.extra) && Objects.equal(extMap, span.extMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(column, style, extra, extMap);
    }
}
