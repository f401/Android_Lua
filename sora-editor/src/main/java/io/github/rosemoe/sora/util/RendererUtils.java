/*******************************************************************************
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
 ******************************************************************************/

package io.github.rosemoe.sora.util;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.color.ResolvableColor;
import io.github.rosemoe.sora.lang.styling.span.SpanColorResolver;
import io.github.rosemoe.sora.lang.styling.span.SpanExtAttrs;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * Utility methods for use in editor renderer.
 *
 * @author Akash Yadav
 */
public final class RendererUtils {

    /**
     * Get the background color for the given span.
     */
    public static int getBackgroundColor(@NonNull Span span, @NonNull EditorColorScheme colorScheme) {
        SpanColorResolver resolver = span.<SpanColorResolver>getSpanExt(SpanExtAttrs.EXT_COLOR_RESOLVER);
        if (resolver == null)
            return colorScheme.getColor(span.getBackgroundColorId());

        ResolvableColor color = resolver.getBackgroundColor(span);
        if (color == null)
            return colorScheme.getColor(span.getBackgroundColorId());

        return color.resolve(colorScheme);
    }

    /**
     * Get the foreground color for the given span.
     */
    public static int getForegroundColor(@NonNull Span span, @NonNull EditorColorScheme colorScheme) {
        SpanColorResolver resolver = span.<SpanColorResolver>getSpanExt(SpanExtAttrs.EXT_COLOR_RESOLVER);
        if (resolver == null)
            return colorScheme.getColor(span.getForegroundColorId());

        ResolvableColor color = resolver.getForegroundColor(span);
        if (color == null)
            return colorScheme.getColor(span.getForegroundColorId());

        return color.resolve(colorScheme);
    }
}