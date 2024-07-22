package io.github.rosemoe.sora.widget.rendering;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.rosemoe.sora.lang.analysis.StyleUpdateRange;
import io.github.rosemoe.sora.widget.CodeEditor;

public class RenderContext {
    @NonNull
    private final CodeEditor editor;
    @NonNull
    private final RenderCache cache;
    @Nullable
    private final RenderNodeHolder renderNodeHolder;
    private int tabWidth;

    public RenderContext(@NonNull CodeEditor editor) {
        this.editor = editor;
        this.cache = new RenderCache();
        this.renderNodeHolder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? new RenderNodeHolder(editor) : null;
        this.tabWidth = 4;
    }

    public void updateForRange(@NonNull StyleUpdateRange range) {
        if (renderNodeHolder != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            renderNodeHolder.invalidateInRegion(range);
        }
    }

    public void invalidateRenderNodes() {
        if (renderNodeHolder != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            renderNodeHolder.invalidate();
        }
    }

    public void updateForInsertion(int startLine, int endLine) {
        cache.updateForInsertion(startLine, endLine);
        if (renderNodeHolder != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            renderNodeHolder.afterInsert(startLine, endLine);
        }
    }

    public void updateForDeletion(int startLine, int endLine) {
        cache.updateForDeletion(startLine, endLine);
        if (renderNodeHolder != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            renderNodeHolder.afterDelete(startLine, endLine);
        }
    }

    public void reset(int lineCount) {
        cache.reset(lineCount);
    }

    @NonNull
    public CodeEditor getEditor() {
        return editor;
    }

    @NonNull
    public RenderCache getCache() {
        return cache;
    }

    @Nullable
    public RenderNodeHolder getRenderNodeHolder() {
        return renderNodeHolder;
    }

    public int getTabWidth() {
        return tabWidth;
    }

    public void setTabWidth(int tabWidth) {
        this.tabWidth = tabWidth;
    }
}