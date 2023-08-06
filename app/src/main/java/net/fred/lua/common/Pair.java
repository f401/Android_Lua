package net.fred.lua.common;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Like @{link android.util.Pair}, but can be modified.
 * This object provides a sensible implementation of equals(), returning true if equals() is true on each of the contained objects.
 *
 * @param <F> The first type.
 * @param <S> The second type.
 */
public class Pair<F, S> {
    public F first;
    public S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> makePair(F first, S second) {
        return new Pair<>(first, second);
    }

    @NonNull
    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    public F getFirst() {
        return first;
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public S getSecond() {
        return second;
    }

    public void setSecond(S second) {
        this.second = second;
    }
}
