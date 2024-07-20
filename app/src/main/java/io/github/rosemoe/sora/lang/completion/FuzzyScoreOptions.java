package io.github.rosemoe.sora.lang.completion;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FuzzyScoreOptions{");
        sb.append("firstMatchCanBeWeak=").append(firstMatchCanBeWeak);
        sb.append(", boostFullMatch=").append(boostFullMatch);
        sb.append('}');
        return sb.toString();
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
