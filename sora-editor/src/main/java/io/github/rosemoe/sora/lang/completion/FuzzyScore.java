package io.github.rosemoe.sora.lang.completion;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An array representing a fuzzy match.
 * <p>
 * 0. the score
 * 1. the offset at which matching started
 * 2. `<match_pos_N>`
 * 3. `<match_pos_1>`
 * 4. `<match_pos_0>` etc
 */
public final class FuzzyScore {
    public static final FuzzyScore DEFAULT = new FuzzyScore(-100, 0);
    private final int wordStart;
    private final List<Integer> matches;
    private int score;

    public FuzzyScore(int score, int wordStart, List<Integer> matches) {
        this.score = score;
        this.wordStart = wordStart;
        this.matches = matches;
    }

    public FuzzyScore(int score, int wordStart) {
        this(score, wordStart, new ArrayList<Integer>());
    }

    public static boolean isDefault(@Nullable FuzzyScore score) {
        return score != null && score.score == -100 && score.wordStart == 0;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getWordStart() {
        return wordStart;
    }

    public List<Integer> getMatches() {
        return matches;
    }
}
