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
package io.github.rosemoe.sora.widget.component;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;

import java.lang.ref.WeakReference;
import java.util.List;

import io.github.rosemoe.sora.event.ClickEvent;
import io.github.rosemoe.sora.event.ColorSchemeUpdateEvent;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.EditorFocusChangeEvent;
import io.github.rosemoe.sora.event.EditorFormatEvent;
import io.github.rosemoe.sora.event.EditorKeyEvent;
import io.github.rosemoe.sora.event.EditorLanguageChangeEvent;
import io.github.rosemoe.sora.event.EditorReleaseEvent;
import io.github.rosemoe.sora.event.Event;
import io.github.rosemoe.sora.event.EventManager;
import io.github.rosemoe.sora.event.EventReceiver;
import io.github.rosemoe.sora.event.ScrollEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.event.SnippetEvent;
import io.github.rosemoe.sora.event.Unsubscribe;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lang.completion.CompletionCancelledException;
import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.lang.styling.StylesUtils;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.text.Cursor;
import io.github.rosemoe.sora.text.TextReference;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.base.EditorPopupWindow;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * Auto complete window for editing code quicker
 *
 * @author Rosemoe
 */
public class EditorAutoCompletion extends EditorPopupWindow implements EditorBuiltinComponent {

    /**
     * Adjust the completion window's position scheme according to the device's screen size.
     */
    public static final int WINDOW_POS_MODE_AUTO = 0;
    /**
     * Completion window always follow the cursor
     */
    public static final int WINDOW_POS_MODE_FOLLOW_CURSOR_ALWAYS = 1;
    /**
     * Completion window always stay at the bottom of view and occupies the
     * horizontal viewport
     */
    public static final int WINDOW_POS_MODE_FULL_WIDTH_ALWAYS = 2;

    private final static long SHOW_PROGRESS_BAR_DELAY = 50;
    private final CodeEditor editor;
    protected boolean cancelShowUp = false;
    protected long requestTime;
    protected int maxHeight;
    protected CompletionThread completionThread;
    protected CompletionPublisher publisher;
    protected WeakReference<List<CompletionItem>> lastAttachedItems;
    protected int currentSelection = -1;
    protected EditorCompletionAdapter adapter;
    protected CompletionLayout layout;
    protected EventManager eventManager;
    private int completionWndPosMode = WINDOW_POS_MODE_AUTO;
    private CharPosition previousSelection;
    private long requestShow = 0;
    private long requestHide = -1;
    private boolean enabled = true;
    private boolean loading = false;

    /**
     * Create a panel instance for the given editor
     *
     * @param editor Target editor
     */
    public EditorAutoCompletion(@NonNull CodeEditor editor) {
        super(editor, FEATURE_HIDE_WHEN_FAST_SCROLL);
        this.editor = editor;
        adapter = new DefaultCompletionItemAdapter();
        setLayout(new DefaultCompletionLayout());
        eventManager = editor.createSubEventManager();
        eventManager.subscribeEvent(ColorSchemeUpdateEvent.class, new EventReceiver<ColorSchemeUpdateEvent>() {
            @Override
            public void onReceive(@NonNull ColorSchemeUpdateEvent event, @NonNull Unsubscribe unsubscribe) {
                onColorSchemeUpdate(event, unsubscribe);
            }
        });

        eventManager.subscribeEvent(ContentChangeEvent.class, new EventReceiver<ContentChangeEvent>() {
            @Override
            public void onReceive(@NonNull ContentChangeEvent event, @NonNull Unsubscribe unsubscribe) {
                onContentChange(event, unsubscribe);
            }
        });
        eventManager.subscribeEvent(ScrollEvent.class, new EventReceiver<ScrollEvent>() {
            @Override
            public void onReceive(@NonNull ScrollEvent event, @NonNull Unsubscribe unsubscribe) {
                onEditorScroll(event, unsubscribe);
            }
        });
        eventManager.subscribeEvent(EditorKeyEvent.class, new EventReceiver<EditorKeyEvent>() {
            @Override
            public void onReceive(@NonNull EditorKeyEvent event, @NonNull Unsubscribe unsubscribe) {
                onKeyEvent(event, unsubscribe);
            }
        });
        eventManager.subscribeEvent(SelectionChangeEvent.class, new EventReceiver<SelectionChangeEvent>() {
            @Override
            public void onReceive(@NonNull SelectionChangeEvent event, @NonNull Unsubscribe unsubscribe) {
                onSelectionChange(event, unsubscribe);
            }
        });
        eventManager.subscribeEvent(EditorReleaseEvent.class, new EventReceiver<EditorReleaseEvent>() {
            @Override
            public void onReceive(@NonNull EditorReleaseEvent event, @NonNull Unsubscribe unsubscribe) {
                setEnabled(false);
            }
        });
        subscribeEventForHide(EditorFormatEvent.class, new Function<EditorFormatEvent, Boolean>() {
            @Override
            public Boolean apply(EditorFormatEvent input) {
                return input.isSuccess();
            }
        });
        subscribeEventForHide(ClickEvent.class, null);
        subscribeEventForHide(EditorLanguageChangeEvent.class, null);
        subscribeEventForHide(EditorFocusChangeEvent.class, new Function<EditorFocusChangeEvent, Boolean>() {
            @Override
            public Boolean apply(EditorFocusChangeEvent input) {
                return !input.isGainFocus();
            }
        });
        subscribeEventForHide(SnippetEvent.class, new Function<SnippetEvent, Boolean>() {
            @Override
            public Boolean apply(SnippetEvent e) {
                return e.getAction() == SnippetEvent.ACTION_SHIFT;
            }
        });
    }

    protected <T extends Event> void subscribeEventForHide(Class<T> clazz, final Function<T, Boolean> predicate) {
        eventManager.subscribeEvent(clazz, new EventReceiver<T>() {
            @Override
            public void onReceive(@NonNull T event, @NonNull Unsubscribe unsubscribe) {
                if (predicate == null || predicate.apply(event)) {
                    hide();
                }
            }
        });
    }

    protected void onColorSchemeUpdate(@NonNull ColorSchemeUpdateEvent event, @NonNull Unsubscribe unsubscribe) {
        applyColorScheme();
    }

    protected void onContentChange(@NonNull ContentChangeEvent event, @NonNull Unsubscribe unsubscribe) {
        if (event.isCausedByUndoManager() || !isEnabled()
                || event.getAction() == ContentChangeEvent.ACTION_SET_NEW_TEXT) {
            hide();
            return;
        }
        CharPosition start = event.getChangeStart();
        CharPosition end = event.getChangeEnd();
        boolean needCompletion = false;
        switch (event.getAction()) {
            case ContentChangeEvent.ACTION_INSERT: {
                if ((!editor.hasComposingText() || editor.getProps().autoCompletionOnComposing) &&
                        end.column != 0 && start.line == end.line) {
                    needCompletion = true;
                } else {
                    hide();
                }
                updateCompletionWindowPosition(isShowing());
                break;
            }
            case ContentChangeEvent.ACTION_DELETE: {
                if (!editor.hasComposingText() && isShowing()) {
                    if (start.line != end.line || start.column != end.column - 1) {
                        hide();
                    } else {
                        needCompletion = true;
                        updateCompletionWindowPosition();
                    }
                }
            }
        }
        if (needCompletion) {
            requireCompletion();
        }
    }

    protected void onSelectionChange(@NonNull SelectionChangeEvent event, @NonNull Unsubscribe unsubscribe) {
        if (event.isSelected() || event.getCause() == SelectionChangeEvent.CAUSE_IME
                || event.getCause() == SelectionChangeEvent.CAUSE_SELECTION_HANDLE || event.getCause() == SelectionChangeEvent.CAUSE_TAP
                || event.getCause() == SelectionChangeEvent.CAUSE_SEARCH || event.getCause() == SelectionChangeEvent.CAUSE_UNKNOWN) {
            hide();
            return;
        }
        if (previousSelection == null) {
            previousSelection = event.getLeft().fromThis();
            return;
        }
        if (event.getCause() == SelectionChangeEvent.CAUSE_KEYBOARD_OR_CODE) {
            if (previousSelection.line != event.getLeft().line) {
                hide();
                return;
            }
            if (isShowing() && Math.abs(previousSelection.column - event.getLeft().column) <= 1) {
                if (event.getLeft().column > 0)
                    requireCompletion();
                else
                    hide();
            }
        }
    }

    protected void onEditorScroll(@NonNull ScrollEvent event, @NonNull Unsubscribe unsubscribe) {
        if (event.getCause() == ScrollEvent.CAUSE_USER_DRAG) {
            updateCompletionWindowPosition(false);
        } else if (event.getCause() == ScrollEvent.CAUSE_USER_FLING) {
            float minVe = editor.getDpUnit() * 2000;
            float velocityX = event.getFlingVelocityX(), velocityY = event.getFlingVelocityY();
            if (Math.abs(velocityX) >= minVe || Math.abs(velocityY) >= minVe) {
                hide();
            }
        }
    }

    protected void onKeyEvent(@NonNull EditorKeyEvent event, @NonNull Unsubscribe unsubscribe) {
        if (event.getEventType() == EditorKeyEvent.Type.DOWN && !event.isAltPressed()
                && !event.isCtrlPressed() && !event.isShiftPressed() && isShowing()) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP: {
                    moveUp();
                    event.setResult(true);
                    event.intercept();
                    break;
                }
                case KeyEvent.KEYCODE_DPAD_DOWN: {
                    moveDown();
                    event.setResult(true);
                    event.intercept();
                    break;
                }
                case KeyEvent.KEYCODE_PAGE_DOWN:
                case KeyEvent.KEYCODE_PAGE_UP:
                    hide();
                    break;
                case KeyEvent.KEYCODE_TAB:
                case KeyEvent.KEYCODE_ENTER: {
                    if (select()) {
                        event.setResult(true);
                        event.intercept();
                    }
                }
            }
        }
    }

    /**
     * Update the position of completion window
     */
    public void updateCompletionWindowPosition() {
        updateCompletionWindowPosition(true);
    }

    /**
     * Apply new position of auto-completion window
     *
     * @param scrollEditor Scroll the editor if there is no enough space to display the window
     */
    public void updateCompletionWindowPosition(boolean scrollEditor) {
        float dp = editor.getDpUnit();
        Cursor cursor = editor.getCursor();
        float panelX = editor.updateCursorAnchor() + dp * 20;
        int rowHeight = editor.getRowHeight();
        float[] rightLayoutOffset = editor.getLayout().getCharLayoutOffset(cursor.getRightLine(), cursor.getRightColumn());
        float panelY = rightLayoutOffset[0] - editor.getOffsetY() + rowHeight / 2f;
        float restY = editor.getHeight() - panelY;
        if (restY > dp * 200) {
            restY = dp * 200;
        } else if (restY < dp * 100 && scrollEditor) {
            float offset = 0;
            while (restY < dp * 100 && editor.getOffsetY() + offset + rowHeight <= editor.getScrollMaxY()) {
                restY += rowHeight;
                panelY -= rowHeight;
                offset += rowHeight;
            }
            editor.getScroller().startScroll(editor.getOffsetX(), editor.getOffsetY(), 0, (int) offset, 0);
        }
        int width;
        if ((editor.getWidth() < 500 * dp && completionWndPosMode == WINDOW_POS_MODE_AUTO) || completionWndPosMode == WINDOW_POS_MODE_FULL_WIDTH_ALWAYS) {
            // center mode
            width = editor.getWidth() * 7 / 8;
            panelX = editor.getWidth() / 8f / 2f;
        } else {
            // follow cursor mode
            width = (int) Math.min(300 * dp, editor.getWidth() / 2f);
        }
        int height = getHeight();
        setMaxHeight((int) restY);
        setLocation((int) panelX + editor.getOffsetX(), (int) panelY + editor.getOffsetY());
        setSize(width, height);
    }

    /**
     * @see #setCompletionWndPositionMode(int)
     */
    public int getCompletionWndPositionMode() {
        return completionWndPosMode;
    }

    /**
     * Set how should we control the position&size of completion window
     *
     * @see #WINDOW_POS_MODE_AUTO
     * @see #WINDOW_POS_MODE_FOLLOW_CURSOR_ALWAYS
     * @see #WINDOW_POS_MODE_FULL_WIDTH_ALWAYS
     */
    public void setCompletionWndPositionMode(int mode) {
        completionWndPosMode = mode;
        updateCompletionWindowPosition();
    }

    /**
     * Replace the layout of completion window
     */
    @SuppressWarnings("unchecked")
    public void setLayout(@NonNull CompletionLayout layout) {
        this.layout = layout;
        layout.setEditorCompletion(this);
        setContentView(layout.inflate(editor.getContext()));
        applyColorScheme();
        if (adapter != null) {
            this.layout.getCompletionList().setAdapter(adapter);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        eventManager.setEnabled(enabled);
        // do cleanup if disabled
        if (!enabled) {
            cancelCompletion();
            hide();
        }
    }

    /**
     * Check if the completion background worker is running
     */
    public boolean isCompletionInProgress() {
        final CompletionThread thread = completionThread;
        return super.isShowing() || requestShow > requestHide || (thread != null && thread.isAlive());
    }

    /**
     * Some layout may support to display more animations,
     * this method provides control over the animation of the layout。
     *
     * @see CompletionLayout#setEnabledAnimation(boolean)
     */
    public void setEnabledAnimation(boolean enabledAnimation) {
        layout.setEnabledAnimation(enabledAnimation);
    }

    /**
     * Set adapter for auto-completion window
     * This will take effect next time the window updates
     *
     * @param adapter New adapter, maybe null
     */
    @SuppressWarnings("unchecked")
    public void setAdapter(@Nullable EditorCompletionAdapter adapter) {
        this.adapter = adapter;
        if (adapter == null) {
            this.adapter = new DefaultCompletionItemAdapter();
        }

        layout.getCompletionList().setAdapter(adapter);
    }

    @Override
    public void show() {
        if (cancelShowUp || !isEnabled()) {
            return;
        }
        requestShow = System.currentTimeMillis();
        final long requireRequest = requestTime;
        editor.postDelayedInLifecycle(new Runnable() {
            @Override
            public void run() {
                if (requestHide < requestShow && requestTime == requireRequest) {
                    EditorAutoCompletion.super.show();
                }
            }
        }, 70);
    }

    /**
     * Hide the completion window
     */
    public void hide() {
        super.dismiss();
        cancelCompletion();
        requestHide = System.currentTimeMillis();
    }

    public Context getContext() {
        return editor.getContext();
    }

    public int getCurrentPosition() {
        return currentSelection;
    }

    /**
     * Apply colors for self
     */
    public void applyColorScheme() {
        EditorColorScheme colors = editor.getColorScheme();
        layout.onApplyColorScheme(colors);
    }

    /**
     * Change layout to loading/idle
     *
     * @param state Whether loading
     */
    public void setLoading(boolean state) {
        loading = state;
        if (state) {
            editor.postDelayedInLifecycle(new Runnable() {
                @Override
                public void run() {
                    if (loading) {
                        layout.setLoading(true);
                    }
                }
            }, SHOW_PROGRESS_BAR_DELAY);
        } else {
            layout.setLoading(false);
        }

    }

    /**
     * Move selection down
     */
    public void moveDown() {
        AdapterView<?> adpView = layout.getCompletionList();
        if (currentSelection + 1 >= adpView.getAdapter().getCount()) {
            return;
        }
        currentSelection++;
        ((EditorCompletionAdapter) adpView.getAdapter()).notifyDataSetChanged();
        ensurePosition();
    }

    /**
     * Move selection up
     */
    public void moveUp() {
        AdapterView<?> adpView = layout.getCompletionList();
        if (currentSelection - 1 < 0) {
            return;
        }
        currentSelection--;
        ((EditorCompletionAdapter) adpView.getAdapter()).notifyDataSetChanged();
        ensurePosition();
    }

    /**
     * Make current selection visible
     */
    private void ensurePosition() {
        if (currentSelection != -1)
            layout.ensureListPositionVisible(currentSelection, adapter.getItemHeight());
    }

    /**
     * Select current position
     *
     * @return if the action is performed
     */
    public boolean select() {
        return select(currentSelection);
    }

    /**
     * Select the given position
     *
     * @param pos Index of auto complete item
     * @return if the action is performed
     */
    public boolean select(int pos) {
        if (pos == -1) {
            return false;
        }
        AdapterView<?> adpView = layout.getCompletionList();
        CompletionItem item = ((EditorCompletionAdapter) adpView.getAdapter()).getItem(pos);
        Cursor cursor = editor.getCursor();
        final CompletionThread completionThread = this.completionThread;
        if (!cursor.isSelected() && completionThread != null) {
            cancelShowUp = true;
            editor.beginComposingTextRejection();
            editor.getText().beginBatchEdit();
            editor.restartInput();
            try {
                item.performCompletion(editor, editor.getText(), completionThread.requestPosition);
                editor.updateCursor();
            } finally {
                editor.getText().endBatchEdit();
                editor.endComposingTextRejection();
                cancelShowUp = false;
            }
            editor.restartInput();
        }
        hide();
        return true;
    }

    /**
     * Stop previous completion thread
     */
    public void cancelCompletion() {
        CompletionThread previous = completionThread;
        if (previous != null && previous.isAlive()) {
            previous.cancel();
            previous.requestTimestamp = -1;
        }
        completionThread = null;
    }

    /**
     * Check cursor position's span.
     * If {@link io.github.rosemoe.sora.lang.styling.TextStyle#NO_COMPLETION_BIT} is set, true is returned.
     */
    public boolean checkNoCompletion() {
        CharPosition pos = editor.getCursor().left();
        Styles styles = editor.getStyles();
        return StylesUtils.checkNoCompletion(styles, pos);
    }

    /**
     * Start completion at current selection position
     */
    public void requireCompletion() {
        if (cancelShowUp || !isEnabled()) {
            return;
        }
        Content text = editor.getText();
        if (text.getCursor().isSelected() || checkNoCompletion()) {
            hide();
            return;
        }
        if (System.nanoTime() - requestTime < editor.getProps().cancelCompletionNs) {
            hide();
            requestTime = System.nanoTime();
            return;
        }
        cancelCompletion();
        requestTime = System.nanoTime();
        currentSelection = -1;
        publisher = new CompletionPublisher(editor.getHandler(), new Runnable() {
            @Override
            public void run() {
                List<CompletionItem> items = publisher.getItems();
                if (lastAttachedItems == null || lastAttachedItems.get() != items) {
                    adapter.attachValues(EditorAutoCompletion.this, items);
                    adapter.notifyDataSetInvalidated();
                    lastAttachedItems = new WeakReference<>(items);
                } else {
                    adapter.notifyDataSetChanged();
                }
                float newHeight = adapter.getItemHeight() * adapter.getCount();
                if (newHeight == 0) {
                    hide();
                }
                updateCompletionWindowPosition();
                setSize(getWidth(), (int) Math.min(newHeight, maxHeight));
                if (!isShowing()) {
                    show();
                }
            }
        }, editor.getEditorLanguage().getInterruptionLevel());
        completionThread = new CompletionThread(requestTime, publisher);
        setLoading(true);
        completionThread.start();
    }

    public void setMaxHeight(int height) {
        maxHeight = height;
    }

    /**
     * Auto-completion Analyzing thread
     *
     * @author Rosemoe
     */
    public class CompletionThread extends Thread implements TextReference.Validator {

        private final Bundle extraData;
        private final CharPosition requestPosition;
        private final Language targetLanguage;
        private final ContentReference contentRef;
        private final CompletionPublisher localPublisher;
        private long requestTimestamp;
        private boolean aborted;

        public CompletionThread(long requestTime, @NonNull CompletionPublisher publisher) {
            requestTimestamp = requestTime;
            requestPosition = editor.getCursor().left();
            targetLanguage = editor.getEditorLanguage();
            contentRef = new ContentReference(editor.getText());
            contentRef.setValidator(this);
            localPublisher = publisher;
            extraData = editor.getExtraArguments();
            aborted = false;
        }

        /**
         * Abort the completion thread
         */
        public void cancel() {
            aborted = true;
            int level = targetLanguage.getInterruptionLevel();
            if (level == Language.INTERRUPTION_LEVEL_STRONG) {
                interrupt();
            }
            localPublisher.cancel();
        }

        public boolean isCancelled() {
            return aborted;
        }

        @Override
        public void validate() {
            if (requestTime != requestTimestamp || aborted) {
                throw new CompletionCancelledException();
            }
        }

        @Override
        public void run() {
            try {
                targetLanguage.requireAutoComplete(contentRef, requestPosition, localPublisher, extraData);
                if (localPublisher.hasData()) {
                    if (completionThread == Thread.currentThread()) {
                        localPublisher.updateList(true);
                    }
                } else {
                    editor.postInLifecycle(new Runnable() {
                        @Override
                        public void run() {
                            EditorAutoCompletion.this.hide();
                        }
                    });
                }
                editor.postInLifecycle(new Runnable() {
                    @Override
                    public void run() {
                        setLoading(false);
                    }
                });
            } catch (Exception e) {
                if (e instanceof CompletionCancelledException) {
                    Log.v("CompletionThread", "Completion is cancelled");
                } else {
                    Log.e("CompletionThread", "Completion failed", e);
                }
            }
        }


    }

}

