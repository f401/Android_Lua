package io.github.rosemoe.sora.lang;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import io.github.rosemoe.sora.lang.analysis.AnalyzeManager;
import io.github.rosemoe.sora.lang.completion.CompletionCancelledException;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.format.IFormatter;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.widget.SymbolPairMatch;

public interface ILanguage {

    /**
     * Get {@link AnalyzeManager} of the language.
     * This is called from time to time by the editor. Cache your instance please.
     */
    @NonNull
    AnalyzeManager getAnalyzeManager();

    /**
     * Get the interruption level for auto-completion.
     *
     * @see InterruptionLevel#INTERRUPTION_LEVEL_STRONG
     * @see InterruptionLevel#INTERRUPTION_LEVEL_SLIGHT
     * @see InterruptionLevel#INTERRUPTION_LEVEL_NONE
     */
    int getInterruptionLevel();

    /**
     * Request to auto-complete the code at the given {@code position}.
     * This is called in a worker thread other than UI thread.
     *
     * @param content        Read-only reference of content
     * @param position       The position for auto-complete
     * @param publisher      The publisher used to update items
     * @param extraArguments Arguments set by {@link CodeEditor#setText(CharSequence, Bundle)}
     * @throws io.github.rosemoe.sora.lang.completion.CompletionCancelledException This thread can be abandoned
     *                                                                             by the editor framework because the auto-completion items of
     *                                                                             this invocation are no longer needed by the user. This can either be thrown
     *                                                                             by {@link ContentReference} or {@link CompletionPublisher}.
     *                                                                             How the exceptions will be thrown is according to
     *                                                                             your settings: {@link #getInterruptionLevel()}
     * @see ContentReference
     * @see CompletionPublisher
     * @see #getInterruptionLevel()
     */
    @WorkerThread
    void requireAutoComplete(@NonNull ContentReference content, @NonNull CharPosition position,
                             @NonNull CompletionPublisher publisher,
                             @NonNull Bundle extraArguments) throws CompletionCancelledException;

    /**
     * Get delta indent spaces count.
     *
     * @param content Content of given line.
     * @param line    0-indexed line number. The indentation is applied on line index: {@code line + 1}.
     * @param column  Column on the given line, where a line separator is inserted.
     * @return Delta count of indent spaces. It can be a negative/positive number or zero.
     */
    @UiThread
    int getIndentAdvance(@NonNull ContentReference content, int line, int column);

    /**
     * Use tab to format
     */
    @UiThread
    boolean useTab();

    /**
     * Get the code formatter for the current language.
     * The formatter is expected to be the same one during the lifecycle of a language instance.
     *
     * @return The code formatter for the current language.
     */
    @UiThread
    @NonNull
    IFormatter getFormatter();

    /**
     * Returns language specified symbol pairs.
     * The method is called only once when the language is applied.
     */
    @UiThread
    SymbolPairMatch getSymbolPairs();

    /**
     * Get newline handlers of this language.
     * This method is called each time the user presses ENTER key.
     * <p>
     * Pay attention to the performance as this method is called frequently
     *
     * @return NewlineHandlers , maybe null
     */
    @UiThread
    @Nullable
    NewlineHandler[] getNewlineHandlers();

    /**
     * Destroy this {@link ILanguage} object.
     * <p>
     * When called, you should stop your resource-taking actions and remove any reference
     * of editor or other objects related to editor (such as references to text in editor) to avoid
     * memory leaks and resource waste.
     */
    @UiThread
    void destroy();

    class InterruptionLevel {
        /**
         * Set the thread's interrupted flag by calling {@link Thread#interrupt()}.
         * <p>
         * Throw {@link CompletionCancelledException} exceptions
         * from {@link ContentReference} and {@link CompletionPublisher}.
         * <p>
         * Set thread's flag for abortion.
         */
        public static final int INTERRUPTION_LEVEL_STRONG = 0;
        /**
         * Throw {@link CompletionCancelledException} exceptions
         * from {@link ContentReference} and {@link CompletionPublisher}.
         * <p>
         * Set thread's flag for abortion.
         */
        public static final int INTERRUPTION_LEVEL_SLIGHT = 1;
        /**
         * Throw {@link CompletionCancelledException} exceptions
         * from {@link ContentReference}
         * <p>
         * Set thread's flag for abortion.
         */
        public static final int INTERRUPTION_LEVEL_NONE = 2;
    }
}
