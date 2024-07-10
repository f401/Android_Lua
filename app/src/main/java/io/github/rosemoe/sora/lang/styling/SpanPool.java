package io.github.rosemoe.sora.lang.styling;

import androidx.annotation.NonNull;

import com.google.common.base.MoreObjects;

import java.util.concurrent.ArrayBlockingQueue;

public class SpanPool<T extends ISpan> {

    /**
     * Small capacity (8192 objects). This should be used for spans that will not
     * be used too frequently (for example, [StaticColorSpan]).
     */
    public static final int CAPACITY_SMALL = 8192;

    /**
     * Small capacity ([CAPACITY_SMALL] * 2 objects). This should be used for spans
     * that will be used frequently (for example, [Span]).
     */
    public static final int CAPACITY_LARGE = CAPACITY_SMALL * 2;

    /**
     * The default pool capacity. Same as [CAPACITY_LARGE].
     */
    public static final int DEFAULT_CAPACITY = CAPACITY_LARGE;

    private final ArrayBlockingQueue<T> cacheQueue;
    private final Factory<T> factory;

    public SpanPool(Factory<T> factory) {
        this(DEFAULT_CAPACITY, factory);
    }

    public SpanPool(int capacity, Factory<T> factory) {
        this.cacheQueue = new ArrayBlockingQueue<>(capacity);
        this.factory = factory;
    }

    /**
     * Return the given span to the pool. This method should not be called directly.
     * Instead, call [Span.recycle] and it should automatically return itself to the
     * pool.
     *
     * @param span The [SpanT] to recycle.
     * @return Whether the span was recycled successfully.
     */
    public boolean offer(T span) {
        return cacheQueue.offer(span);
    }

    /**
     * Returns a recycled span or creates a new one if the pool is empty.
     *
     * @param column The new column index for the span.
     * @param style  The new style for the span.
     * @return The recycled [SpanT], or a new instance of [SpanT] if the pool is empty.
     */
    public T obtain(int column, long style) {
        T target = MoreObjects.firstNonNull(cacheQueue.poll(), factory.create(column, style));
        target.setColumn(column);
        target.setStyle(style);
        return target;
    }

    public interface Factory<T extends ISpan> {
        @NonNull
        T create(int column, long style);
    }
}
