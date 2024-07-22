package io.github.rosemoe.sora.lang.completion;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface FuzzyScorer {
    @Nullable
    FuzzyScore calculateScore(@NonNull String pattern, @NonNull String lowPattern, int patternPos,
                              @NonNull String word, @NonNull String lowWord, int wordPos, @Nullable FuzzyScoreOptions options);
}
