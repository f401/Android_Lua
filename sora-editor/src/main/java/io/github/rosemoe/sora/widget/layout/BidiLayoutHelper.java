package io.github.rosemoe.sora.widget.layout;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.graphics.CharPosDesc;
import io.github.rosemoe.sora.graphics.GraphicTextRow;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentLine;
import io.github.rosemoe.sora.text.bidi.Directions;
import io.github.rosemoe.sora.util.Numbers;
import io.github.rosemoe.sora.widget.CodeEditor;

public final class BidiLayoutHelper {
    private BidiLayoutHelper() {
    }

    public static float horizontalOffset(
            @NonNull CodeEditor editor,
            @NonNull AbstractLayout layout,
            @NonNull Content text,
            int line, int rowStart, int rowEnd, int targetColumn
    ) {
        Directions dirs = text.getLineDirections(line);
        ContentLine lineText = text.getLine(line);
        GraphicTextRow gtr = GraphicTextRow.obtain(editor.isBasicDisplayMode());
        gtr.set(
                text,
                line,
                0,
                lineText.length(),
                layout.getSpans(line),
                editor.getTextPaint(),
                editor.getRenderContext()
        );
        if (layout instanceof WordwrapLayout) {
            gtr.setSoftBreaks(((WordwrapLayout) layout).getSoftBreaksForLine(line));
        }
        int column = Numbers.coerceIn(targetColumn, rowStart, rowEnd);
        float offset = 0f;
        for (int i = 0; i < dirs.getRunCount(); ++i) {
            int runStart = Numbers.coerceIn(dirs.getRunStart(i), rowStart, rowEnd);
            int runEnd = Numbers.coerceIn(dirs.getRunEnd(i), rowStart, rowEnd);
            if (runStart > column || runStart > runEnd) {
                break;
            }
            if (runEnd < column) {
                offset += gtr.measureText(runStart, runEnd);
            } else { //runEnd > targetColumn
                if (dirs.isRunRtl(i)) {
                    offset += gtr.measureText(targetColumn, runEnd);
                } else {
                    offset += gtr.measureText(runStart, column);
                }
            }
        }
        gtr.recycle();
        return offset;
    }

    public static int horizontalIndex(
            @NonNull CodeEditor editor,
            @NonNull AbstractLayout layout,
            @NonNull Content text,
            int line, int rowStart, int rowEnd, float targetOffset
    ) {
        Directions dirs = text.getLineDirections(line);
        ContentLine lineText = text.getLine(line);
        GraphicTextRow gtr = GraphicTextRow.obtain(editor.isBasicDisplayMode());
        gtr.set(
                text,
                line,
                0,
                lineText.length(),
                layout.getSpans(line),
                editor.getTextPaint(),
                editor.getRenderContext()
        );
        if (layout instanceof WordwrapLayout) {
            gtr.setSoftBreaks(((WordwrapLayout) layout).getSoftBreaksForLine(line));
        }
        float offset = 0f;
        for (int i = 0; i < dirs.getRunCount(); ++i) {
            int runStart = Numbers.coerceIn(dirs.getRunStart(i), rowStart, rowEnd);
            int runEnd = Numbers.coerceIn(dirs.getRunEnd(i), rowStart, rowEnd);
            if (runEnd == rowStart) {
                continue;
            }
            if (runStart == rowEnd) {
                int j = i > 0 ? i - 1 : 0;
                if (dirs.isRunRtl(j)) {
                    return Numbers.coerceIn(dirs.getRunStart(j), rowStart, rowEnd);
                } else {
                    return Numbers.coerceIn(dirs.getRunEnd(j), rowStart, rowEnd);
                }
            }
            float width = gtr.measureText(runStart, runEnd);
            if (offset + width >= targetOffset) {
                int res;
                if (dirs.isRunRtl(i)) {
                    res = CharPosDesc.getTextOffset(
                            gtr.findOffsetByAdvance(
                                    runStart,
                                    offset + width - targetOffset
                            )
                    );
                } else {
                    res = CharPosDesc.getTextOffset(
                            gtr.findOffsetByAdvance(
                                    runStart,
                                    targetOffset - offset
                            )
                    );
                }
                gtr.recycle();
                return res;
            } else {
                offset += width;
            }
        }
        gtr.recycle();
        // Fallback
        int j = dirs.getRunCount() - 1;
        if (dirs.isRunRtl(j)) {
            return Numbers.coerceIn(dirs.getRunStart(j), rowStart, rowEnd);
        } else {
            return Numbers.coerceIn(dirs.getRunEnd(j), rowStart, rowEnd);
        }
    }
}
