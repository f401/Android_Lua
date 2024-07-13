package io.github.rosemoe.sora.text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.util.Arrays;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class ContentLine implements CharSequence {
    public static final float GROW_FACTOR = 1.5f;

    private LineSeparator mSeparator;
    private char[] mContent;
    private int mLength;

    ContentLine() {
        this(true, null);
    }

    ContentLine(@NonNull ContentLine other) {
        this(false, other.mSeparator);
        this.mLength = other.mLength;
        this.mContent = Arrays.copyOf(other.mContent, other.mLength);
    }

    ContentLine(int size) {
        this(false, null);
        this.mLength = 0;
        this.mContent = new char[size];
    }

    ContentLine(boolean init, @Nullable LineSeparator separator) {
        if (init) {
            this.mContent = new char[32];
            this.mLength = 0;
        }
        this.mSeparator = MoreObjects.firstNonNull(separator, LineSeparator.LF);
    }

    private void ensureCapacity(int expected) {
        if (mContent.length < expected) {
            // grow
            int newLength = GROW_FACTOR * mContent.length < expected ?
                    expected + 10 :
                    (int) (GROW_FACTOR * mContent.length);
            char[] newValue = new char[newLength];
            System.arraycopy(this.mContent, 0, newValue, 0, mLength);
            this.mContent = newValue;
        }
    }

    @Override
    public int length() {
        return mLength;
    }

    @Override
    public char charAt(int index) {
        // Check for line separator
        if (index >= this.mLength) {
            return this.mSeparator.getChar().charAt(index - this.mLength);
        }
        return mContent[index];
    }

    /**
     * Didn't include LineSeparator.
     */
    @NonNull
    @Override
    public ContentLine subSequence(int start, int end) {
        Preconditions.checkElementIndex(start, this.mLength);
        Preconditions.checkElementIndex(end, this.mLength);
        Preconditions.checkArgument(start < end, "start greater than end");

        char[] newValue = new char[end - start + 16];
        System.arraycopy(this.mContent, start, newValue, 0, end - start);
        ContentLine result = new ContentLine(false, getLineSeparator());
        result.mContent = newValue;
        result.mLength = end - start;

        return result;
    }

    public final LineSeparator getLineSeparator() {
        return this.mSeparator;
    }

    public final void setLineSeparator(LineSeparator lineSeparator) {
        this.mSeparator = lineSeparator;
    }

    @NonNull
    public ContentLine insert(int dstOff, CharSequence src, int start, int end) {
        Preconditions.checkNotNull(src);
        Preconditions.checkPositionIndex(dstOff, this.mLength);
        Preconditions.checkPositionIndexes(start, end, src.length());

        int len = end - start;
        this.ensureCapacity(len + this.mLength);
        System.arraycopy(this.mContent, dstOff, this.mContent, dstOff + len, mLength - dstOff);
        for (int i = start; i < end; i++) {
            mContent[dstOff++] = src.charAt(i);
        }
        mLength += len;

        return this;
    }

    @NonNull
    public ContentLine insert(int dst, CharSequence src) {
        return insert(dst, src, 0, src.length());
    }

    @NonNull
    public ContentLine delete(int start, int end) {
        Preconditions.checkPositionIndexes(start, end, mLength);

        System.arraycopy(this.mContent, end, this.mContent, start, mLength - end);
        mLength -= (end - start);

        return this;
    }

    @NonNull
    public ContentLine append(CharSequence src) {
        return this.insert(mLength, src);
    }

    @NonNull
    @Override
    public String toString() {
        return new String(this.mContent, 0, mLength);
    }

    /**
     * A convenient method to append text to a StringBuilder
     */
    public void appendTo(@NonNull StringBuilder sb) {
        sb.append(mContent, 0, mLength);
    }

    public void getChars(int srcBegin, int srcEnd, @NonNull char[] dst, int dstBegin) {
        Preconditions.checkPositionIndexes(srcBegin, srcEnd, length());
        System.arraycopy(mContent, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }

    /**
     * Get the backing char array of this object.
     * The result array should not be modified.
     */
    @NonNull
    public char[] getBackingCharArray() {
        return this.mContent;
    }
}
