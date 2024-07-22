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
package io.github.rosemoe.sora.widget.layout;

import android.view.View;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.graphics.Paint;
import io.github.rosemoe.sora.graphics.SingleCharacterWidths;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentLine;
import io.github.rosemoe.sora.text.bidi.Directions;
import io.github.rosemoe.sora.util.IntPair;
import io.github.rosemoe.sora.util.MutableInt;

public class ViewMeasureHelper {

    /**
     * Get desired view size for the given arguments
     */
    public static long getDesiredSize(int widthMeasureSpec, int heightMeasureSpec, float gutterSize, float rowHeight, boolean wordwrap, int tabSize, @NonNull Content text, @NonNull final Paint paint) {
        int maxWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        final SingleCharacterWidths measurer = new SingleCharacterWidths(tabSize);
        if (wordwrap) {
            if (View.MeasureSpec.getMode(widthMeasureSpec) != View.MeasureSpec.EXACTLY) {
                final int[] lines = View.MeasureSpec.getMode(heightMeasureSpec) != View.MeasureSpec.EXACTLY ?
                        new int[text.getLineCount()] : null;
                final MutableInt lineMaxSize = new MutableInt(0);
                text.runReadActionsOnLines(0, text.getLineCount() - 1, new Content.ContentLineConsumer() {
                    @Override
                    public void accept(int lineIndex, @NonNull ContentLine line, @NonNull Directions dirs) {
                        int measured = (int) Math.ceil(measurer.measureText(line.getBackingCharArray(), 0, line.length(), paint));
                        if (measured > lineMaxSize.value) {
                            lineMaxSize.value = measured;
                        }
                        if (lines != null) {
                            lines[lineIndex] = measured;
                        }
                    }
                });
                int width = (int) Math.min(maxWidth, lineMaxSize.value + gutterSize);
                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
                if (lines != null) {
                    MutableInt rowCount = new MutableInt(0);
                    int availableSize = (int) (width - gutterSize);
                    if (availableSize <= 0) {
                        rowCount.value = text.length();
                    } else {
                        for (int line : lines) {
                            rowCount.value += (int) Math.max(1, Math.ceil(1.0 * line / availableSize));
                        }
                    }
                    int height = Math.min((int) (rowHeight * rowCount.value), maxHeight);
                    heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                }
            } else {
                if (View.MeasureSpec.getMode(heightMeasureSpec) != View.MeasureSpec.EXACTLY) {
                    final MutableInt rowCount = new MutableInt(0);
                    final int availableSize = (int) (maxWidth - gutterSize);
                    if (availableSize <= 0) {
                        rowCount.value = text.length();
                    } else {
                        text.runReadActionsOnLines(0, text.getLineCount() - 1, new Content.ContentLineConsumer() {
                            @Override
                            public void accept(int lineIndex, @NonNull ContentLine line, @NonNull Directions dirs) {
                                int measured = (int) Math.ceil(measurer.measureText(line.getBackingCharArray(), 0, line.length(), paint));
                                rowCount.value += (int) Math.max(1, Math.ceil(1.0 * measured / availableSize));
                            }
                        });
                    }
                    int height = Math.min((int) (rowHeight * rowCount.value), maxHeight);
                    heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                }
            }
        } else {
            if (View.MeasureSpec.getMode(widthMeasureSpec) != View.MeasureSpec.EXACTLY) {
                final MutableInt lineMaxSize = new MutableInt(0);
                text.runReadActionsOnLines(0, text.getLineCount() - 1, new Content.ContentLineConsumer() {
                    @Override
                    public void accept(int lineIndex, @NonNull ContentLine line, @NonNull Directions dirs) {
                        int measured = (int) Math.ceil(measurer.measureText(line.getBackingCharArray(), 0, line.length(), paint));
                        if (measured > lineMaxSize.value) {
                            lineMaxSize.value = measured;
                        }
                    }
                });
                int width = (int) Math.min(lineMaxSize.value + gutterSize, maxWidth);
                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            }
            if (View.MeasureSpec.getSize(heightMeasureSpec) != View.MeasureSpec.EXACTLY) {
                int height = Math.min(maxHeight, (int) (rowHeight * text.getLineCount()));
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            }
        }
        return IntPair.pack(widthMeasureSpec, heightMeasureSpec);
    }

}
