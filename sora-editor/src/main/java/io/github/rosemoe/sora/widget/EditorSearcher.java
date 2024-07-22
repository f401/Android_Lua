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
package io.github.rosemoe.sora.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.rosemoe.sora.I18nConfig;
import io.github.rosemoe.sora.R;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.EventReceiver;
import io.github.rosemoe.sora.event.PublishSearchResultEvent;
import io.github.rosemoe.sora.event.SelectionChangeEvent;
import io.github.rosemoe.sora.event.Unsubscribe;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.Cursor;
import io.github.rosemoe.sora.text.TextUtils;
import io.github.rosemoe.sora.util.IntPair;
import io.github.rosemoe.sora.util.LongArrayList;

/**
 * Search text in editor.
 * Note that editor searches text in another thread, so results may not be available immediately. Also,
 * the searcher does not match empty text. For example, you will never match a single empty
 * line by regex '^.*$'. What's more, zero-length pattern is not permitted.
 * The searcher updates its search results automatically when editor text is changed, even after {@link CodeEditor#setText(CharSequence)}
 * is invoked. So be careful that the search result is changing and {@link PublishSearchResultEvent} is
 * re-triggered when search result is available for changed text.
 *
 * @author Rosemoe
 * @see PublishSearchResultEvent
 * @see SearchOptions
 */
public class EditorSearcher {

    private final CodeEditor editor;
    protected String currentPattern;
    protected SearchOptions searchOptions;
    protected Thread currentThread;
    /**
     * Search results. Note that it is naturally sorted by start index (and also end index).
     * No overlapping region is permitted.
     */
    protected LongArrayList lastResults;
    private boolean cyclicJumping = true;

    EditorSearcher(@NonNull CodeEditor editor) {
        this.editor = editor;
        this.editor.subscribeEvent(ContentChangeEvent.class, new EventReceiver<ContentChangeEvent>() {
            @Override
            public void onReceive(@NonNull ContentChangeEvent event, @NonNull Unsubscribe unsubscribe) {
                if (hasQuery()) {
                    executeMatch();
                }
            }
        });
    }

    /**
     * @see #setCyclicJumping(boolean)
     */
    public boolean isCyclicJumping() {
        return cyclicJumping;
    }

    /**
     * Jump cyclically when calling {@link #gotoNext()} and {@link #gotoPrevious()}
     *
     * @see #isCyclicJumping()
     */
    public void setCyclicJumping(boolean cyclicJumping) {
        this.cyclicJumping = cyclicJumping;
    }

    /**
     * Search text with the given pattern and options. If you use {@link SearchOptions#TYPE_REGULAR_EXPRESSION},
     * the pattern will be your regular expression.
     * <p>
     * {@link #stopSearch()} should be called if you want to stop, instead of invoking this method with nulls.
     * <p>
     * Note that, the result is not immediately available because we search texts in another thread to
     * avoid lags in main thread. If you want to be notified when the results is available, refer to
     * {@link PublishSearchResultEvent}. Also be careful that, the event is also triggered when {@link #stopSearch()}
     * is called.
     *
     * @throws IllegalArgumentException               if pattern length is zero
     * @throws java.util.regex.PatternSyntaxException if pattern is invalid when regex is enabled.
     */
    public void search(@NonNull String pattern, @NonNull SearchOptions options) {
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("pattern length must be > 0");
        }
        if (options.type == SearchOptions.TYPE_REGULAR_EXPRESSION) {
            // Pre-check
            //noinspection ResultOfMethodCallIgnored
            Pattern.compile(pattern);
        }
        currentPattern = pattern;
        searchOptions = options;
        executeMatch();
        editor.postInvalidate();
    }

    /**
     * Execute current match task. Cancel any previous tasks.
     */
    private void executeMatch() {
        if (currentThread != null && currentThread.isAlive()) {
            currentThread.interrupt();
        }
        SearchRunnable runnable = new SearchRunnable(editor.getText(), searchOptions, currentPattern);
        currentThread = new Thread(runnable);
        currentThread.start();
    }

    /**
     * Stop searching.
     */
    public void stopSearch() {
        if (currentThread != null && currentThread.isAlive()) {
            currentThread.interrupt();
        }
        currentThread = null;
        lastResults = null;
        currentPattern = null;
        searchOptions = null;
        editor.dispatchEvent(new PublishSearchResultEvent(editor));
    }

    /**
     * Check if any search is in progress
     */
    public boolean hasQuery() {
        return currentPattern != null;
    }

    private void checkState() {
        if (!hasQuery()) {
            throw new IllegalStateException("pattern not set");
        }
    }

    /**
     * Find current selected region in search results and return the index in search result.
     * Or {@code -1} if result is not available or the current selected region is not in result.
     *
     * @throws IllegalStateException if no search is in progress
     */
    public int getCurrentMatchedPositionIndex() {
        checkState();
        Cursor cur = editor.getCursor();
        if (!cur.isSelected()) {
            return -1;
        }
        int left = cur.getLeft();
        int right = cur.getRight();

        if (isResultValid()) {
            LongArrayList res = lastResults;
            if (res == null) {
                return -1;
            }
            long packed = IntPair.pack(left, right);
            int index = res.lowerBound(packed);
            if (index < res.size() && res.get(index) == packed) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Get item count of search result. Or {@code 0} if result is not available or no item is found.
     *
     * @throws IllegalStateException if no search is in progress
     */
    public int getMatchedPositionCount() {
        checkState();
        if (!isResultValid()) {
            return 0;
        }
        LongArrayList result = lastResults;
        return result == null ? 0 : result.size();
    }

    /**
     * Goto next matched position based on cursor position.
     *
     * @return if any jumping action is performed
     * @throws IllegalStateException if no search is in progress
     * @see #setCyclicJumping(boolean)
     */
    public boolean gotoNext() {
        checkState();
        if (isResultValid()) {
            LongArrayList res = lastResults;
            if (res == null) {
                return false;
            }
            int right = editor.getCursor().getRight();
            int index = res.lowerBoundByFirst(right);
            if (index == res.size() && cyclicJumping) {
                index = 0;
            }
            if (index < res.size()) {
                long data = res.get(index);
                int start = IntPair.getFirst(data);
                CharPosition pos1 = editor.getText().getIndexer().getCharPosition(start);
                CharPosition pos2 = editor.getText().getIndexer().getCharPosition(IntPair.getSecond(data));
                editor.setSelectionRegion(pos1.line, pos1.column, pos2.line, pos2.column, SelectionChangeEvent.CAUSE_SEARCH);
                return true;
            }
        }
        return false;
    }

    /**
     * Goto last matched position based on cursor position.
     *
     * @return if any jumping action is performed
     * @throws IllegalStateException if no search is in progress
     * @see #setCyclicJumping(boolean)
     */
    public boolean gotoPrevious() {
        checkState();
        if (isResultValid()) {
            LongArrayList res = lastResults;
            if (res == null || res.size() == 0) {
                return false;
            }
            int left = editor.getCursor().getLeft();
            int index = res.lowerBoundByFirst(left);
            if (index == res.size() || IntPair.getFirst(res.get(index)) >= index) {
                index--;
            }
            if (index < 0 && cyclicJumping) {
                index = res.size() - 1;
            }
            if (index >= 0 && index < res.size()) {
                long data = res.get(index);
                int end = IntPair.getSecond(data);
                CharPosition pos1 = editor.getText().getIndexer().getCharPosition(IntPair.getFirst(data));
                CharPosition pos2 = editor.getText().getIndexer().getCharPosition(end);
                editor.setSelectionRegion(pos1.line, pos1.column, pos2.line, pos2.column, SelectionChangeEvent.CAUSE_SEARCH);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if selected region is exactly a search result
     *
     * @throws IllegalStateException if no search is in progress
     */
    public boolean isMatchedPositionSelected() {
        return getCurrentMatchedPositionIndex() > -1;
    }

    /**
     * Replace currently selected region if the region is exactly a match of searching pattern.
     * Otherwise, attempt to jump to next matched position.
     *
     * @param replacement The text for replacement
     * @throws IllegalStateException if no search is in progress
     * @deprecated Confusing method name. Use {@link #replaceCurrentMatch(String)} instead.
     */
    @Deprecated(since = "0.24.0", forRemoval = true)
    public void replaceThis(@NonNull String replacement) {
        replaceCurrentMatch(replacement);
    }

    /**
     * Replace currently selected region if the region is exactly a match of searching pattern.
     * Otherwise, attempt to jump to next matched position.
     *
     * @param replacement The text for replacement
     * @throws IllegalStateException if no search is in progress
     */
    public void replaceCurrentMatch(@NonNull String replacement) {
        if (!editor.isEditable()) {
            return;
        }
        if (isMatchedPositionSelected()) {
            if (replacement.isEmpty()) {
                editor.deleteText();
            } else {
                editor.commitText(replacement);
            }
        } else {
            gotoNext();
        }
    }

    /**
     * Replace all matched position. Note that after invoking this, a blocking {@link ProgressDialog}
     * is shown until the action is done (either succeeded or failed).
     *
     * @param replacement The text for replacement
     * @throws IllegalStateException if no search is in progress
     */
    public void replaceAll(@NonNull String replacement) {
        replaceAll(replacement, null);
    }

    /**
     * Replace all matched position. Note that after invoking this, a blocking {@link ProgressDialog}
     * is shown until the action is done (either succeeded or failed). The given callback will be executed
     * on success.
     *
     * @param replacement   The text for replacement
     * @param whenSucceeded Callback when action is succeeded
     * @throws IllegalStateException if no search is in progress
     */
    public void replaceAll(@NonNull final String replacement, @Nullable final Runnable whenSucceeded) {
        if (!editor.isEditable()) {
            return;
        }
        checkState();
        if (!isResultValid()) {
            Toast.makeText(editor.getContext(), I18nConfig.getResourceId(R.string.sora_editor_editor_search_busy), Toast.LENGTH_SHORT).show();
            return;
        }
        Context context = editor.getContext();
        final ProgressDialog dialog = ProgressDialog.show(context,
                I18nConfig.getString(context, R.string.sora_editor_replaceAll),
                I18nConfig.getString(context, R.string.sora_editor_editor_search_replacing),
                true, false);
        final LongArrayList res = lastResults;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final StringBuilder sb = editor.getText().toStringBuilder();
                    int newLength = replacement.length();
                    int delta = 0;
                    for (int i = 0; i < res.size(); i++) {
                        long region = res.get(i);
                        int start = IntPair.getFirst(region);
                        int end = IntPair.getSecond(region);
                        int oldLength = end - start;
                        sb.replace(start + delta, end + delta, replacement);
                        delta += newLength - oldLength;
                    }
                    editor.postInLifecycle(new Runnable() {
                        @Override
                        public void run() {
                            CharPosition pos = editor.getCursor().left();
                            editor.getText().replace(0, 0, editor.getLineCount() - 1, editor.getText().getColumnCount(editor.getLineCount() - 1), sb);
                            editor.setSelectionAround(pos.line, pos.column);
                            dialog.dismiss();

                            if (whenSucceeded != null) {
                                whenSucceeded.run();
                            }
                        }
                    });
                } catch (final Exception e) {
                    editor.postInLifecycle(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(editor.getContext(), "Replace failed:" + e, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                }
            }
        }).start();
    }

    protected boolean isResultValid() {
        return currentThread == null || !currentThread.isAlive();
    }

    /**
     * Search options for {@link EditorSearcher#search(String, SearchOptions)}
     */
    public static class SearchOptions {

        /**
         * Normal text searching
         */
        public final static int TYPE_NORMAL = 1;
        /**
         * Text searching by whole word
         */
        public final static int TYPE_WHOLE_WORD = 2;
        /**
         * Use regular expression for text searching
         */
        public final static int TYPE_REGULAR_EXPRESSION = 3;
        public final boolean caseInsensitive;
        @IntRange(from = 1, to = 3)
        public final int type;

        public SearchOptions(boolean caseInsensitive, boolean useRegex) {
            this(useRegex ? TYPE_REGULAR_EXPRESSION : TYPE_NORMAL, caseInsensitive);
        }

        /**
         * Create a new searching option with the given attributes.
         *
         * @param type            type of searching method
         * @param caseInsensitive Case insensitive
         * @see #TYPE_NORMAL
         * @see #TYPE_WHOLE_WORD
         * @see #TYPE_REGULAR_EXPRESSION
         */
        public SearchOptions(@IntRange(from = 1, to = 3) int type, boolean caseInsensitive) {
            if (type < 1 || type > 3) {
                throw new IllegalArgumentException("invalid type");
            }
            this.type = type;
            this.caseInsensitive = caseInsensitive;
        }

    }

    /**
     * Run for regex matching
     */
    private final class SearchRunnable implements Runnable {

        private final StringBuilder text;
        private final String pattern;
        private final SearchOptions options;
        private Thread localThread;

        public SearchRunnable(@NonNull Content content, @NonNull SearchOptions options, @NonNull String pattern) {
            this.text = content.toStringBuilder();
            this.options = options;
            this.pattern = pattern;
        }

        private boolean checkNotCancelled() {
            return currentThread == localThread && !Thread.interrupted();
        }

        @Override
        public void run() {
            localThread = Thread.currentThread();
            final LongArrayList results = new LongArrayList();
            int textLength = text.length();
            boolean ignoreCase = options.caseInsensitive;
            String pattern = this.pattern;
            switch (options.type) {
                case SearchOptions.TYPE_NORMAL: {
                    int nextStart = 0;
                    int patternLength = pattern.length();
                    while (nextStart != -1 && nextStart < textLength && checkNotCancelled()) {
                        nextStart = TextUtils.indexOf(text, pattern, ignoreCase, nextStart);
                        if (nextStart != -1) {
                            results.add(IntPair.pack(nextStart, nextStart + patternLength));
                            nextStart += patternLength;
                        }
                    }
                    break;
                }
                case SearchOptions.TYPE_WHOLE_WORD:
                    pattern = "\\b" + Pattern.quote(pattern) + "\\b";
                    // fall-through
                case SearchOptions.TYPE_REGULAR_EXPRESSION:
                    Pattern regex = Pattern.compile(pattern, (ignoreCase ? Pattern.CASE_INSENSITIVE : 0) | Pattern.MULTILINE);
                    int lastEnd = 0;
                    // Matcher will call toString() on input several times
                    String string = text.toString();
                    Matcher matcher = regex.matcher(string);
                    while (lastEnd < textLength && matcher.find(lastEnd) && checkNotCancelled()) {
                        lastEnd = matcher.end();
                        int start = matcher.start();
                        if (start == lastEnd) {
                            // Do not match empty text
                            lastEnd++;
                            continue;
                        }
                        results.add(IntPair.pack(matcher.start(), lastEnd));
                    }
            }
            if (checkNotCancelled()) {
                editor.postInLifecycle(new Runnable() {
                    @Override
                    public void run() {
                        if (currentThread == localThread) {
                            lastResults = results;
                            editor.invalidate();
                            editor.dispatchEvent(new PublishSearchResultEvent(editor));
                            currentThread = null;
                        }
                    }
                });
            }
        }
    }

}
