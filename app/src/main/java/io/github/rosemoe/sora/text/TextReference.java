package io.github.rosemoe.sora.text;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

/**
 * Reference of a {@link CharSequence} object, which allows
 * to access the original sequence in read-only mode, and attach a
 * {@link Validator} to validate access to check whether reject the
 * read access.
 * This can be useful when reading text in multiple threads, with the ability
 * to interrupt that thread when the actual text changes.
 *
 * @author Rosemoe
 */
public class TextReference implements CharSequence {

    private final CharSequence ref;
    private final int start, end;
    private Validator validator;

    public TextReference(@NonNull CharSequence ref) {
        this(ref, 0, ref.length());
    }

    public TextReference(@NonNull CharSequence ref, int start, int end) {
        Preconditions.checkNotNull(ref);
        this.ref = ref;
        this.start = start;
        this.end = end;
        Preconditions.checkPositionIndexes(start, end, ref.length());
    }

    /**
     * Get original text of the reference
     */
    @NonNull
    public CharSequence getReference() {
        return ref;
    }

    @Override
    public int length() {
        validateAccess();
        return end - start;
    }

    @Override
    public char charAt(int index) {
        Preconditions.checkPositionIndex(index, length());
        validateAccess();
        return ref.charAt(start + index);
    }

    @NonNull
    @Override
    public String toString() {
        return ref.subSequence(start, end).toString();
    }

    @NonNull
    @Override
    public CharSequence subSequence(int start, int end) {
        Preconditions.checkPositionIndexes(start, end, length());
        validateAccess();
        return new TextReference(ref, this.start + start, this.start + end).setValidator(validator);
    }

    public TextReference setValidator(Validator validator) {
        this.validator = validator;
        return this;
    }

    public void validateAccess() {
        if (validator != null)
            validator.validate();
    }

    public interface Validator {
        void validate();
    }
}