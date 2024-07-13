package io.github.rosemoe.sora.widget.rendering;

import android.graphics.Canvas;
import android.graphics.RenderNode;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import io.github.rosemoe.sora.lang.analysis.StyleUpdateRange;
import io.github.rosemoe.sora.lang.styling.EmptyReader;
import io.github.rosemoe.sora.lang.styling.ISpans;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.widget.CodeEditor;

/**
 * Hardware accelerated text render, which manages [RenderNode]
 * to speed up rendering.
 *
 * @author Rosemoe
 */
@RequiresApi(Build.VERSION_CODES.Q)
public class RenderNodeHolder {
    @NonNull
    private final CodeEditor editor;
    @NonNull
    private final ArrayList<TextRenderNode> cache;
    @NonNull
    private final Stack<TextRenderNode> pool;

    public RenderNodeHolder(@NonNull CodeEditor editor) {
        this.editor = editor;
        this.cache = Lists.newArrayListWithCapacity(64);
        this.pool = new Stack<>();
    }

    public boolean shouldUpdateCache() {
        return !editor.isWordwrap() && editor.isHardwareAcceleratedDrawAllowed();
    }

    public boolean invalidateInRegion(@NonNull StyleUpdateRange range) {
        boolean res = false;
        ArrayList<TextRenderNode> garbage = Lists.newArrayList();
        for (TextRenderNode element : cache) {
            if (range.isInRange(element.line)) {
                garbage.add(element);
                element.renderNode.discardDisplayList();
                pool.push(element);
                res = true;
            }
        }
        cache.removeAll(garbage);
        return res;
    }

    /**
     * Called by editor when text style changes.
     * Such as text size/typeface.
     * Also called when wordwrap state changes from true to false
     */
    public void invalidate() {
        for (TextRenderNode i : cache) {
            i.isDirty = true;
        }
    }

    public TextRenderNode getNode(int line) {
        int size = cache.size();
        for (int i = 0; i < size; i++) {
            final TextRenderNode node = cache.get(i);
            if (node.line == line) {
                Collections.swap(cache, 0, i);
                return node;
            }
        }
        TextRenderNode node = pool.isEmpty() ? new TextRenderNode(line) : pool.pop();
        node.line = line;
        node.isDirty = true;
        cache.add(0, node);
        return node;
    }

    public void keepCurrentInDisplay(int start, int end) {
        ArrayList<TextRenderNode> garbage = Lists.newArrayList();
        for (TextRenderNode e : cache) {
            if (e.line < start || e.line > end) {
                garbage.add(e);
                e.renderNode.discardDisplayList();
            }
        }
        cache.removeAll(garbage);
    }

    public int drawLineHardwareAccelerated(@NonNull Canvas canvas, int line,
                                           float offsetX, float offsetY) {
        Preconditions.checkArgument(canvas.isHardwareAccelerated(), "Only hardware-accelerated canvas can be used");
        Styles styles = editor.getStyles();
        TextRenderNode node = getNode(line);
        // It's safe to use row directly because the mode is non-wordwrap
        if (node.needsRecord()) {
            ISpans spans = styles != null ? styles.getSpans() : null;
            ISpans.Reader reader = spans != null ? spans.read() : EmptyReader.getInstance();
            try {
                reader.moveToLine(line);
            } catch (Exception e) {
                reader = EmptyReader.getInstance();
            }
            editor.getRenderer().updateLineDisplayList(node.renderNode, line, reader);
            try {
                reader.moveToLine(-1);
            } catch (Exception e) {
                Log.e("RenderNodeHolder", "Exception: ", e);
            }
            node.isDirty = false;
        }
        canvas.save();
        canvas.translate(offsetX, offsetY);
        canvas.drawRenderNode(node.renderNode);
        canvas.restore();
        return node.renderNode.getWidth();
    }

    public void afterInsert(int startLine, int endLine) {
        for (TextRenderNode node : cache) {
            if (node.line == startLine) {
                node.isDirty = true;
            } else if (node.line > startLine) {
                node.line += endLine - startLine;
            }
        }
    }

    public void afterDelete(int startLine, int endLine) {
        Range<Integer> range = Range.closed(startLine + 1, endLine);
        ArrayList<TextRenderNode> garbage = Lists.newArrayList();
        for (TextRenderNode node : cache) {
            if (node.line == startLine) {
                node.isDirty = true;
            } else if (range.contains(node.line)) {
                garbage.add(node);
                node.renderNode.discardDisplayList();
            } else if (node.line > endLine) {
                node.line -= endLine - startLine;
            }
        }
        cache.removeAll(garbage);
        pool.addAll(garbage);
    }

    public static class TextRenderNode {
        /**
         * The target line of this node.
         * -1 for unavailable
         */
        private int line;
        @NonNull
        private RenderNode renderNode;
        private boolean isDirty;

        private TextRenderNode(int line) {
            this.line = line;
            this.renderNode = new RenderNode("editorRenderNode");
            this.isDirty = true;
        }

        public boolean needsRecord() {
            return isDirty || !renderNode.hasDisplayList();
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        @NonNull
        public RenderNode getRenderNode() {
            return renderNode;
        }

        public void setRenderNode(@NonNull RenderNode renderNode) {
            this.renderNode = renderNode;
        }

        public boolean isDirty() {
            return isDirty;
        }

        public void setDirty(boolean dirty) {
            isDirty = dirty;
        }
    }
}
