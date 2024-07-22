package io.github.rosemoe.sora.lang.completion;

public class SortedCompletionItem {
    private final CompletionItem completionItem;
    private FuzzyScore score;

    public SortedCompletionItem(CompletionItem completionItem, FuzzyScore score) {
        this.completionItem = completionItem;
        this.score = score;
    }

    public CompletionItem getCompletionItem() {
        return completionItem;
    }

    public FuzzyScore getScore() {
        return score;
    }

    public void setScore(FuzzyScore score) {
        this.score = score;
    }
}
