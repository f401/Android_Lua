package io.github.rosemoe.sora.lang.completion;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Comparator;
import java.util.List;

import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentLine;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.CharCode;

public class Comparators {

    public static int defaultComparator(CompletionItem a, CompletionItem b) {
        int p1Score = 0, p2Score = 0;
        if (a.extra instanceof SortedCompletionItem) {
            p1Score = ((SortedCompletionItem) a.extra).getScore().getScore();
        }

        if (b.extra instanceof SortedCompletionItem) {
            p2Score = ((SortedCompletionItem) b.extra).getScore().getScore();
        }

        // if score biggest, it better similar to input text
        if (p1Score < p2Score) {
            return 1;
        } else if (p1Score > p2Score) {
            return -1;
        }
        String p1 = toString(a.sortText), p2 = toString(b.sortText);
        // check with 'sortText'
        int result = p1.compareTo(p2);
        if (result != 0) {
            return result;
        }

        // check with 'label'
        p1 = toString(a.label);
        p2 = toString(b.label);
        result = p1.compareTo(p2);
        if (result != 0) {
            return result;
        }

        // check with 'kind'
        // if kind biggest, it better important
        int bKindVar = 0, aKindVar = 0;
        if (b.kind != null) {
            bKindVar = b.kind.getValue();
        }

        if (a.kind != null) {
            aKindVar = a.kind.getValue();
        }

        return bKindVar - aKindVar;
    }

    public static int snippetUpComparator(CompletionItem a, CompletionItem b) {
        if (a.kind != b.kind) {
            if (a.kind == CompletionItemKind.Snippet) {
                return 1;
            } else if (b.kind == CompletionItemKind.Snippet) {
                return -1;
            }
        }
        return defaultComparator(a, b);
    }

    private static String toString(CharSequence src) {
        if (src instanceof String) {
            return (String) src;
        } else if (src == null) {
            return " ";
        } else {
            return src.toString();
        }
    }

    public static Comparator<CompletionItem> getCompletionItemComparator(ContentReference source,
                                                                         CharPosition cursorPosition,
                                                                         List<CompletionItem> completionItemList) {
        source.validateAccess();
        final ContentLine sourceLine = source.getReference().getLine(cursorPosition.line);
        String word = "";
        String wordLow = "";

        // picks a score function based on the number of
        // items that we have to score/filter and based on the
        // user-configuration
        FuzzyScorer scoreFn = new FuzzyScorer() {
            @Nullable
            @Override
            public FuzzyScore calculateScore(@NonNull String pattern, @NonNull String lowPattern, int patternPos, @NonNull String word, @NonNull String lowWord, int wordPos, @Nullable FuzzyScoreOptions options) {
                if (sourceLine.length() > 2000) {
                    return Filters.fuzzyScore(pattern, lowPattern, patternPos, word, lowWord, wordPos, options);
                } else {
                    return Filters.fuzzyScoreGracefulAggressive(
                            pattern,
                            lowPattern,
                            patternPos,
                            word,
                            lowWord,
                            wordPos,
                            options
                    );
                }
            }
        };

        for (CompletionItem originItem : completionItemList) {
            source.validateAccess();
            int overwriteBefore = originItem.prefixLength;

            if (word.length() != overwriteBefore) {
                word = overwriteBefore == 0 ? "" : String.valueOf(sourceLine.subSequence(
                        sourceLine.length() - overwriteBefore, sourceLine.length()
                ));
                wordLow = word.toLowerCase();
            }


            SortedCompletionItem item = new SortedCompletionItem(originItem, FuzzyScore.DEFAULT);
            // when there is nothing to score against, don't
            // event try to do. Use a const rank and rely on
            // the fallback-sort using the initial sort order.
            // use a score of `-100` because that is out of the
            // bound of values `fuzzyScore` will return
            if (overwriteBefore == 0) {
                // when there is nothing to score against, don't
                // event try to do. Use a const rank and rely on
                // the fallback-sort using the initial sort order.
                // use a score of `-100` because that is out of the
                // bound of values `fuzzyScore` will return
                item.setScore(FuzzyScore.DEFAULT);
            } else {
                // skip word characters that are whitespace until
                // we have hit the replace range (overwriteBefore)
                int wordPos = 0;
                while (wordPos < overwriteBefore) {
                    char ch = word.charAt(wordPos);
                    if (ch == CharCode.Space || ch == CharCode.Tab) {
                        wordPos += 1;
                    } else {
                        break;
                    }
                }

                if (wordPos >= overwriteBefore) {
                    // the wordPos at which scoring starts is the whole word
                    // and therefore the same rules as not having a word apply
                    item.setScore(FuzzyScore.DEFAULT);
                } else if (originItem.sortText != null && !originItem.sortText.isEmpty()) {
                    // when there is a `filterText` it must match the `word`.
                    // if it matches we check with the label to compute highlights
                    // and if that doesn't yield a result we have no highlights,
                    // despite having the match
                    // by default match `word` against the `label`
                    FuzzyScore match = scoreFn.calculateScore(
                            word,
                            wordLow,
                            wordPos,
                            originItem.sortText,
                            originItem.sortText.toLowerCase(),
                            0,
                            FuzzyScoreOptions.DEFAULT
                    ); // NO match
                    if (match == null) continue;

                    // compareIgnoreCase(item.completion.filterText, item.textLabel) === 0
                    if (originItem.sortText.contentEquals(originItem.label)) {
                        // filterText and label are actually the same -> use good highlights
                        item.setScore(match);
                    } else {
                        // re-run the scorer on the label in the hope of a result BUT use the rank
                        // of the filterText-match
                        FuzzyScore labelMatch = scoreFn.calculateScore(
                                word,
                                wordLow,
                                wordPos,
                                toString(originItem.label),
                                toString(originItem.label).toLowerCase(),
                                0,
                                FuzzyScoreOptions.DEFAULT
                        );
                        if (labelMatch == null) continue; // NO match
                        item.setScore(labelMatch);
                        labelMatch.getMatches().set(0, match.getMatches().get(0));
                    }

                } else {
                    // by default match `word` against the `label`
                    FuzzyScore match = scoreFn.calculateScore(
                            word,
                            wordLow,
                            wordPos,
                            toString(originItem.label),
                            toString(originItem.label).toLowerCase(),
                            0,
                            FuzzyScoreOptions.DEFAULT
                    );
                    if (match == null) continue;// NO match
                    item.setScore(match);
                }

                originItem.extra = item;

            }
        }

        return new Comparator<CompletionItem>() {
            @Override
            public int compare(CompletionItem o1, CompletionItem o2) {
                return snippetUpComparator(o1, o2);
            }
        };

    }
}
