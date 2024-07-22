package io.github.rosemoe.sora.widget;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.util.IntPair;

public class RegionResolver {
    public static final int REGION_OUTBOUND = 0;
    public static final int REGION_LINE_NUMBER = 1;
    public static final int REGION_SIDE_ICON = 2;
    public static final int REGION_DIVIDER_MARGIN = 3;
    public static final int REGION_DIVIDER = 4;
    public static final int REGION_TEXT = 5;
    public static final int IN_BOUND = 0;
    public static final int OUT_BOUND = 1;

    public static long resolveTouchRegion(@NonNull CodeEditor editor, @NonNull MotionEvent event) {
        return resolveTouchRegion(editor, event, -1);
    }

    public static long resolveTouchRegion(@NonNull CodeEditor editor, @NonNull MotionEvent event, int pointerIndex) {
        final float x = (pointerIndex == -1 ? event.getX() : event.getX(pointerIndex)) + editor.getOffsetX();
        final float y = (pointerIndex == -1 ? event.getY() : event.getY(pointerIndex)) + editor.getOffsetY();

        float lineNumberWidth = editor.measureLineNumber();
        float iconWidth = editor.getRenderer().hasSideHintIcons() ? editor.getRowHeight() : 0;
        float textOffset = editor.measureTextRegionOffset();
        float dividerMarginLeft = editor.getDividerMarginLeft();
        float dividerMarginRight = editor.getDividerMarginRight();
        float dividerWidth = editor.getDividerWidth();

        int region;
        if (x < 0f) {
            region = REGION_OUTBOUND;
        } else if (x >= 0f && x <= lineNumberWidth) {
            region = REGION_LINE_NUMBER;
        } else if (x >= lineNumberWidth && x <= lineNumberWidth + iconWidth) {
            region = REGION_SIDE_ICON;
        } else if ((x >= lineNumberWidth + iconWidth && x <= lineNumberWidth + iconWidth + dividerMarginLeft) ||
                (x >= lineNumberWidth + iconWidth + dividerMarginLeft + dividerWidth
                        && x <= lineNumberWidth + iconWidth + dividerMarginLeft + dividerMarginRight + dividerWidth)) {
            region = REGION_DIVIDER_MARGIN;
        } else if (x >= lineNumberWidth + iconWidth + dividerMarginLeft && x <= lineNumberWidth + iconWidth + dividerMarginLeft + dividerWidth) {
            region = REGION_DIVIDER;
        } else if (x >= textOffset && x <= editor.getScrollMaxX() + editor.getWidth()) {
            region = REGION_TEXT;
        } else {
            if (editor.isWordwrap() && x >= 0f && x <= editor.getWidth()) {
                region = REGION_TEXT;
            } else {
                region = REGION_OUTBOUND;
            }
        }

        int bound = y >= 0 && y <= editor.getScrollMaxY() + (float) editor.getHeight() / 2 ? IN_BOUND : OUT_BOUND;
        return IntPair.pack(region, bound);
    }
}
