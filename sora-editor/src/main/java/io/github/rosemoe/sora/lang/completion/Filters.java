package io.github.rosemoe.sora.lang.completion;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.MoreObjects;

import io.github.rosemoe.sora.util.CharCode;
import io.github.rosemoe.sora.util.MyCharacter;

public class Filters {
    private static final int maxLen = 32;
    private static final int[] minWordMatchPosArray = new int[2 * maxLen];
    private static final int[] maxWordMatchPosArray = new int[2 * maxLen];
    private static final int[][] diag = new int[maxLen][maxLen];
    private static final int[][] table = new int[maxLen][maxLen];
    private static final int[][] arrows = new int[maxLen][maxLen];

    public static boolean isPatternInWord(String patternLow, int patternPos, int patternLen,
                                          String wordLow, int wordPos, int wordLen, boolean fillMinWordPosArr) {
        int patternPosMut = patternPos;
        int wordPosMut = wordPos;
        while (patternPosMut < patternLen && wordPosMut < wordLen) {
            if (patternLow.charAt(patternPosMut) == wordLow.charAt(wordPosMut)) {
                if (fillMinWordPosArr) {
                    // Remember the min word position for each pattern position
                    minWordMatchPosArray[patternPosMut] = wordPosMut;
                }
                patternPosMut += 1;
            }
            wordPosMut += 1;
        }
        return patternPosMut == patternLen; // pattern must be exhausted
    }

    protected static void fillInMaxWordMatchPos(int patternLen, int wordLen, int patternStart, int wordStart, String patternLow, String wordLow) {
        int patternPos = patternLen - 1;
        int wordPos = wordLen - 1;
        while (patternPos >= patternStart && wordPos >= wordStart) {
            if (patternLow.charAt(patternPos) == wordLow.charAt(wordPos)) {
                maxWordMatchPosArray[patternPos] = wordPos;
                patternPos--;
            }
            wordPos--;
        }
    }

    public static boolean isUpperCaseAtPos(int pos, String word, String wordLow) {
        return word.charAt(pos) != wordLow.charAt(pos);
    }

    public static boolean isSeparatorAtPos(String value, int index) {
        if (index < 0 || index >= value.length()) {
            return false;
        }
        switch (value.codePointAt(index)) {
            case CharCode.Underline:
            case CharCode.Dash:
            case CharCode.Period:
            case CharCode.Space:
            case CharCode.Slash:
            case CharCode.Backslash:
            case CharCode.SingleQuote:
            case CharCode.DoubleQuote:
            case CharCode.Colon:
            case CharCode.DollarSign:
            case CharCode.LessThan:
            case CharCode.GreaterThan:
            case CharCode.OpenParen:
            case CharCode.CloseParen:
            case CharCode.OpenSquareBracket:
            case CharCode.CloseSquareBracket:
            case CharCode.OpenCurlyBrace:
            case CharCode.CloseCurlyBrace:
                return true;
            default:
                return MyCharacter.couldBeEmoji(value.codePointAt(index));
        }
    }

    public static boolean isWhitespaceAtPos(String value, int index) {
        if (index < 0 || index >= value.length()) {
            return false;
        }
        switch (value.charAt(index)) {
            case CharCode.Space:
            case CharCode.Tab:
                return true;
            default:
                return false;
        }
    }

    @Nullable
    public static FuzzyScore fuzzyScore(@NonNull String pattern, @NonNull String patternLow, int patternStart,
                                        @NonNull String word, @NonNull String wordLow, int wordStart, FuzzyScoreOptions options) {
        int patternLen = Math.min(maxLen, pattern.length());
        int wordLen = Math.min(maxLen - 1, word.length());
        if (patternStart >= patternLen || wordStart >= wordLen || (patternLen - patternStart) > (wordLen - wordStart)) {
            return null;
        }

        // Run a simple check if the characters of pattern occur
        // (in order) at all in word. If that isn't the case we
        // stop because no match will be possible
        if (!isPatternInWord(patternLow, patternStart, patternLen, wordLow, wordStart, wordLen, true)) {
            return null;
        }

        // Find the max matching word position for each pattern position
        // NOTE: the min matching word position was filled in above, in the `isPatternInWord` call
        fillInMaxWordMatchPos(patternLen, wordLen, patternStart, wordStart, patternLow, wordLow);

        int row = 1;
        int column = 1;
        int patternPos = patternStart;
        int wordPos = 0;

        boolean[] hasStrongFirstMatch = new boolean[]{false};

        // There will be a match, fill in tables
        while (patternPos < patternLen) {

            // Reduce search space to possible matching word positions and to possible access from next row
            int minWordMatchPos = minWordMatchPosArray[patternPos];
            int maxWordMatchPos = maxWordMatchPosArray[patternPos];
            int nextMaxWordMatchPos = (patternPos + 1 < patternLen) ? maxWordMatchPosArray[patternPos + 1] : wordLen;

            column = minWordMatchPos - wordStart + 1;
            wordPos = minWordMatchPos;

            while (wordPos < nextMaxWordMatchPos) {

                int score = Integer.MIN_VALUE;
                boolean canComeDiag = false;

                if (wordPos <= maxWordMatchPos) {
                    score = doScore(
                            pattern, patternLow, patternPos, patternStart,
                            word, wordLow, wordPos, wordLen, wordStart,
                            diag[row - 1][column - 1] == 0,
                            hasStrongFirstMatch
                    );
                }

                int diagScore = 0;
                if (score != Integer.MAX_VALUE) {
                    canComeDiag = true;
                    diagScore = score + table[row - 1][column - 1];
                }

                boolean canComeLeft = wordPos > minWordMatchPos;
                int leftScore = canComeLeft ? table[row][column - 1] + (diag[row][column - 1] > 0 ? -5 : 0) : 0; // penalty for a gap start

                boolean canComeLeftLeft = wordPos > minWordMatchPos + 1 && diag[row][column - 1] > 0;
                int leftLeftScore =
                        canComeLeftLeft ? table[row][column - 2] + (diag[row][column - 2] > 0 ? -5 : 0) : 0; // penalty for a gap start

                if (canComeLeftLeft && (!canComeLeft || leftLeftScore >= leftScore) && (!canComeDiag || leftLeftScore >= diagScore)) {
                    // always prefer choosing left left to jump over a diagonal because that means a match is earlier in the word
                    table[row][column] = leftLeftScore;
                    arrows[row][column] = Arrow.LeftLeft;
                    diag[row][column] = 0;
                } else if (canComeLeft && (!canComeDiag || leftScore >= diagScore)) {
                    // always prefer choosing left since that means a match is earlier in the word
                    table[row][column] = leftScore;
                    arrows[row][column] = Arrow.Left;
                    diag[row][column] = 0;
                } else if (canComeDiag) {
                    table[row][column] = diagScore;
                    arrows[row][column] = Arrow.Diag;
                    diag[row][column] = diag[row - 1][column - 1] + 1;
                }
                column++;
                wordPos++;
            }
            row++;
            patternPos++;
        }


        if (!hasStrongFirstMatch[0] && options != null && !options.isFirstMatchCanBeWeak()) {
            return null;
        }

        row--;
        column--;

        FuzzyScore result = new FuzzyScore(table[row][column], wordStart);

        int backwardsDiagLength = 0;
        int maxMatchColumn = 0;

        while (row >= 1) {
            // Find the column where we go diagonally up
            int diagColumn = column;
            do {
                int arrow = arrows[row][diagColumn];
                if (arrow == Arrow.LeftLeft) {
                    diagColumn -= 2;
                } else if (arrow == Arrow.Left) {
                    diagColumn -= 1;
                } else {
                    // found the diagonal
                    break;
                }
            } while (diagColumn >= 1);

            // Overturn the "forwards" decision if keeping the "backwards" diagonal would give a better match
            if (
                    backwardsDiagLength > 1 // only if we would have a contiguous match of 3 characters
                            && patternLow.charAt(patternStart + row - 1) ==
                            wordLow.charAt(wordStart + column - 1) // only if we can do a contiguous match diagonally
                            && !isUpperCaseAtPos(
                            diagColumn + wordStart - 1,
                            word,
                            wordLow
                    ) // only if the forwards chose diagonal is not an uppercase
                            && backwardsDiagLength + 1 > diag[row][diagColumn] // only if our contiguous match would be longer than the "forwards" contiguous match
            ) {
                diagColumn = column;
            }

            if (diagColumn == column) {
                // this is a contiguous match
                backwardsDiagLength++;
            } else {
                backwardsDiagLength = 1;
            }

            if (maxMatchColumn == 0) {
                // remember the last matched column
                maxMatchColumn = diagColumn;
            }

            row--;
            column = diagColumn - 1;
            result.getMatches().add(column);
        }

        if (wordLen == patternLen && options != null && options.isBoostFullMatch()) {
            // the word matches the pattern with all characters!
            // giving the score a total match boost (to come up ahead other words)
            result.setScore(result.getScore() + 2);
        }

        // Add 1 penalty for each skipped character in the word
        int skippedCharsCount = maxMatchColumn - patternLen;
        result.setScore(result.getScore() - skippedCharsCount);
        return result;
    }

    public static int doScore(String pattern, String patternLow, int patternPos, int patternStart,
                              String word, String wordLow, int wordPos, int wordLen, int wordStart, boolean newMatchStart,
                              boolean[] outFirstMatchStrong) {
        if (patternLow.charAt(patternPos) != wordLow.charAt(wordPos)) {
            return Integer.MIN_VALUE;
        }

        int score = 1;
        boolean isGapLocation = false;
        if (wordPos == patternPos - patternStart) {
            // common prefix: `foobar <-> foobaz`
            //                            ^^^^^
            score = pattern.charAt(patternPos) == word.charAt(wordPos) ? 7 : 5;

        } else if (isUpperCaseAtPos(wordPos, word, wordLow) && (wordPos == 0 || !isUpperCaseAtPos(
                wordPos - 1,
                word,
                wordLow
        ))
        ) {
            // hitting upper-case: `foo <-> forOthers`
            //                              ^^ ^
            score = pattern.charAt(patternPos) == word.charAt(wordPos) ? 7 : 5;
            isGapLocation = true;

        } else if (isSeparatorAtPos(wordLow, wordPos) && (wordPos == 0 || !isSeparatorAtPos(
                wordLow,
                wordPos - 1
        ))
        ) {
            // hitting a separator: `. <-> foo.bar`
            //                                ^
            score = 5;
        } else if (isSeparatorAtPos(wordLow, wordPos - 1) || isWhitespaceAtPos(wordLow, wordPos - 1)) {
            // post separator: `foo <-> bar_foo`
            //                              ^^^
            score = 5;
            isGapLocation = true;
        }

        if (score > 1 && patternPos == patternStart) {
            outFirstMatchStrong[0] = true;
        }

        if (!isGapLocation) {
            isGapLocation = isUpperCaseAtPos(wordPos, word, wordLow) || isSeparatorAtPos(
                    wordLow,
                    wordPos - 1
            ) || isWhitespaceAtPos(wordLow, wordPos - 1);
        }

        //
        if (patternPos == patternStart) { // first character in pattern
            if (wordPos > wordStart) {
                // the first pattern character would match a word character that is not at the word start
                // so introduce a penalty to account for the gap preceding this match
                score -= isGapLocation ? 3 : 5;
            }
        } else {
            if (newMatchStart) {
                // this would be the beginning of a new match (i.e. there would be a gap before this location)
                score += isGapLocation ? 2 : 0;
            } else {
                // this is part of a contiguous match, so give it a slight bonus, but do so only if it would not be a preferred gap location
                score += isGapLocation ? 0 : 1;
            }
        }

        if (wordPos + 1 == wordLen) {
            // we always penalize gaps, but this gives unfair advantages to a match that would match the last character in the word
            // so pretend there is a gap after the last character in the word to normalize things
            score -= isGapLocation ? 3 : 5;
        }

        return score;
    }

    public static FuzzyScore fuzzyScoreGracefulAggressive(String pattern, String lowPattern, int patternPos, String word,
                                                          String lowWord, int wordPos, @Nullable FuzzyScoreOptions options) {
        return fuzzyScoreWithPermutations(
                pattern,
                lowPattern,
                patternPos,
                word,
                lowWord,
                wordPos,
                true,
                options
        );
    }

    public static FuzzyScore fuzzyScoreWithPermutations(String pattern, String lowPattern,
                                                        int patternPos, String word, String lowWord,
                                                        int wordPos, boolean aggressive, @Nullable FuzzyScoreOptions options) {
        FuzzyScore top = fuzzyScore(
                pattern,
                lowPattern,
                patternPos,
                word,
                lowWord,
                wordPos,
                MoreObjects.firstNonNull(options, FuzzyScoreOptions.DEFAULT)
        );
        if (top != null && !aggressive) {
            // when using the original pattern yield a result we`
            // return it unless we are aggressive and try to find
            // a better alignment, e.g. `cno` -> `^co^ns^ole` or `^c^o^nsole`.
            return top;
        }

        if (pattern.length() >= 3) {
            // When the pattern is long enough then try a few (max 7)
            // permutations of the pattern to find a better match. The
            // permutations only swap neighbouring characters, e.g
            // `cnoso` becomes `conso`, `cnsoo`, `cnoos`.
            //int tries = 7.coerceAtMost(pattern.length() - 1);
            int tries = Math.min(7, pattern.length() - 1);

            int movingPatternPos = patternPos + 1;

            while (movingPatternPos < tries) {
                String newPattern = nextTypoPermutation(pattern, movingPatternPos);
                if (newPattern != null) {
                    FuzzyScore candidate = fuzzyScore(
                            newPattern,
                            newPattern.toLowerCase(),
                            patternPos,
                            word,
                            lowWord,
                            wordPos,
                            MoreObjects.firstNonNull(options, FuzzyScoreOptions.DEFAULT)
                    );
                    if (candidate != null) {
                        candidate.setScore(candidate.getScore() - 3); // permutation penalty
                        if (top == null || candidate.getScore() > top.getScore()) {
                            top = candidate;
                        }
                    }
                }
                movingPatternPos++;
            }
        }

        return top;
    }

    public static String nextTypoPermutation(String pattern, int patternPos) {
        if (patternPos + 1 >= pattern.length()) {
            return null;
        }

        char swap1 = pattern.charAt(patternPos);
        char swap2 = pattern.charAt(patternPos + 1);

        if (swap1 == swap2) {
            return null;
        }

        return pattern.substring(0, patternPos) + swap2 + swap1 + pattern.substring(patternPos + 2);
    }

    public static final class Arrow {
        public static final int Diag = 1;
        public static final int Left = 2;
        public static final int LeftLeft = 3;
    }

}
