package io.github.rosemoe.sora.lang.completion;

import androidx.annotation.NonNull;

import com.google.common.base.Objects;

import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;

public class SnippetDescription {
    private final int selectedLength;
    private final CodeSnippet snippet;
    private final boolean deleteSelected;

    public SnippetDescription(int selectedLength, CodeSnippet snippet, boolean deleteSelected) {
        this.selectedLength = selectedLength;
        this.snippet = snippet;
        this.deleteSelected = deleteSelected;
    }

    public SnippetDescription(int selectedLength, CodeSnippet snippet) {
        this(selectedLength, snippet, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SnippetDescription)) return false;
        SnippetDescription that = (SnippetDescription) o;
        return selectedLength == that.selectedLength && deleteSelected == that.deleteSelected && Objects.equal(snippet, that.snippet);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(selectedLength, snippet, deleteSelected);
    }

    public int getSelectedLength() {
        return selectedLength;
    }

    public CodeSnippet getSnippet() {
        return snippet;
    }

    public boolean getDeleteSelected() {
        return deleteSelected;
    }

    @NonNull
    @Override
    public String toString() {
        String sb = "SnippetDescription{" + "selectedLength=" + selectedLength +
                ", snippet=" + snippet +
                ", deleteSelected=" + deleteSelected +
                '}';
        return sb;
    }
}
