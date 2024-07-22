package io.github.rosemoe.sora.lang.completion;

import androidx.annotation.NonNull;

import com.google.common.base.Objects;

public final class FuzzyScoreOptions {
    public static final FuzzyScoreOptions DEFAULT = new FuzzyScoreOptions(true, true);
    private final boolean firstMatchCanBeWeak, boostFullMatch;

    public FuzzyScoreOptions(boolean firstMatchCanBeWeak, boolean boostFullMatch) {
        this.firstMatchCanBeWeak = firstMatchCanBeWeak;
        this.boostFullMatch = boostFullMatch;
    }

    public static FuzzyScoreOptions getDefault() {
        return DEFAULT;
    }

    @NonNull
    @Override
    public String toString() {
        return "FuzzyScoreOptions{firstMatchCanBeWeak=" + firstMatchCanBeWeak +
                ", boostFullMatch=" + boostFullMatch +
                '}';
    }

    public boolean isFirstMatchCanBeWeak() {
        return firstMatchCanBeWeak;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FuzzyScoreOptions)) return false;
        FuzzyScoreOptions that = (FuzzyScoreOptions) o;
        return firstMatchCanBeWeak == that.firstMatchCanBeWeak && boostFullMatch == that.boostFullMatch;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(firstMatchCanBeWeak, boostFullMatch);
    }

    public boolean isBoostFullMatch() {
        return boostFullMatch;
    }
}
