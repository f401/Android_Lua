/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 */
package io.github.rosemoe.sora.lang.styling;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.rosemoe.sora.lang.styling.color.IResolvableColor;
import io.github.rosemoe.sora.lang.styling.span.ISpanExt;
import io.github.rosemoe.sora.lang.styling.span.SpanExtAttrs;

/**
 * Span describes the appearance and other attributes of text segment
 *
 * @author Rosemoe
 */
public interface ISpan {

    /**
     * Get column of this span
     *
     * @see #setColumn(int)
     */
    int getColumn();

    /**
     * Set column of this span
     *
     * @see #getColumn()
     */
    void setColumn(int column);

    /**
     * Get style of the span
     *
     * @see #setStyle(long)
     * @see TextStyle
     */
    long getStyle();

    /**
     * Set style of the span
     *
     * @see #getStyle()
     * @see TextStyle
     */
    void setStyle(long style);

    /**
     * Get the {@link IResolvableColor} instance for resolving underline color of this span
     */
    @Nullable
    IResolvableColor getUnderlineColor();

    /**
     * Set underline color with a {@link IResolvableColor} to resolve colors when the span is rendered.
     * Null for no underline.
     */
    void setUnderlineColor(@Nullable IResolvableColor color);

    /**
     * @see #setExtra(Object)
     */
    Object getExtra();

    /**
     * Extra data for language internal use
     *
     * @see #getExtra()
     */
    void setExtra(Object extraData);

    /**
     * Set extended attribute of this span. The type of {@code ext} is checked whether it is compatible
     * with the given {@code extType}.
     *
     * @param extType Type of extension, from {@link SpanExtAttrs}
     * @param ext     The data to set. Use null to unset.
     */
    void setSpanExt(int extType, @Nullable ISpanExt ext);

    /**
     * Check if certain extended attribute is set
     */
    boolean hasSpanExt(int extType);

    /**
     * Get extended attribute of given type. If it is unset, null is returned.
     */
    @Nullable
    <T extends ISpanExt> T getSpanExt(int extType);

    /**
     * Remove all {@link ISpanExt}s
     */
    void removeAllSpanExt();

    /**
     * Reset all properties of this span, including column, style and ext.
     */
    void reset();

    /**
     * Create a new span with the same attributes. The new span can be safely modified with affecting
     * the original span.
     * <p>
     * Note that {@link ISpanExt} objects are <strong>shared</strong>s by the old span and new span instance.
     *
     * @return new span with the same attribute
     */
    @NonNull
    ISpan copy();

    /**
     * Recycle this span to pool for later use. After calling this method, you should not
     * make any access to this {@link ISpan} instance. And all attributes of this span are reset.
     * <p>
     * Note that no matter whether the span is added to pool, it will be reset.
     *
     * @return if the span is actually added to the pool.
     */
    boolean recycle();

}