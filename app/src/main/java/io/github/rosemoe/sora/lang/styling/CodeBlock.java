package io.github.rosemoe.sora.lang.styling;

import com.google.common.base.Objects;

import java.util.Comparator;
import java.util.List;

public class CodeBlock {
    public static final Comparator<CodeBlock> COMPARATOR_END = new Comparator<CodeBlock>() {
        @Override
        public int compare(CodeBlock a, CodeBlock b) {
            int res = Integer.compare(a.endLine, b.endLine);
            if (res == 0) {
                return Integer.compare(a.endColumn, b.endColumn);
            } else {
                return res;
            }
        }
    };

    public static final Comparator<CodeBlock> COMPARATOR_START = new Comparator<CodeBlock>() {
        @Override
        public int compare(CodeBlock a, CodeBlock b) {
            int res = Integer.compare(a.startLine, b.startLine);
            if (res == 0) {
                return Integer.compare(a.startColumn, b.startColumn);
            } else {
                return res;
            }
        }
    };
    /**
     * Start line of code block
     */
    public int startLine;
    /**
     * Start column of code block
     */
    public int startColumn;
    /**
     * End line of code block
     */
    public int endLine;
    /**
     * End column of code block
     */
    public int endColumn;
    /**
     * Indicates that this BlockLine should be drawn vertically until the bottom of its end line
     */
    public boolean toBottomOfEndLine;

    /**
     * Performs a binary search to find the index of the smallest code block whose end line is
     * greater than or equal to the specified line.
     * <p>
     * This implementation also handles the case where the elements in the list are
     * <code>null</code>. If a null element is encountered at <code>mid</code>, we look for the
     * first non-null element either before and after the element and set it as <code>mid</code>.
     *
     * @param line   The line number to search for.
     * @param blocks The list of code blocks to search within.
     * @return The index of the smallest code block with an end line greater than or equal to the
     * specified line. If no matching code block is found, -1 is returned.
     */
    public static int binarySearchEndBlock(int line, List<CodeBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return -1;
        }

        int left = 0, right = blocks.size() - 1, mid, row;
        int max = right;

        while (left <= right) {
            mid = left + (right - left) / 2;
            if (mid < 0 || mid > max) {
                return -1;
            }

            CodeBlock block = blocks.get(mid);
            if (block == null) {
                int nonNullLeft = mid - 1;
                int nonNullRight = mid + 1;

                while (true) {
                    if (nonNullLeft < left && nonNullRight > right) {
                        return -1;
                    } else if (nonNullLeft >= left && blocks.get(nonNullLeft) != null) {
                        mid = nonNullLeft;
                        break;
                    } else if (nonNullRight <= right && blocks.get(nonNullRight) != null) {
                        mid = nonNullRight;
                        break;
                    }
                    nonNullLeft--;
                    nonNullRight++;
                }

                block = blocks.get(mid);
            }

            row = block.endLine;
            if (row > line) {
                right = mid - 1;
            } else if (row < line) {
                left = mid + 1;
            } else {
                left = mid;
                break;
            }
        }

        if (left < 0 || left > max) {
            return -1;
        }

        return left;
    }

    public void clear() {
        startColumn = startLine = endLine = endColumn = 0;
        toBottomOfEndLine = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CodeBlock)) return false;
        CodeBlock codeBlock = (CodeBlock) o;
        return startLine == codeBlock.startLine && startColumn == codeBlock.startColumn
                && endLine == codeBlock.endLine && endColumn == codeBlock.endColumn
                && toBottomOfEndLine == codeBlock.toBottomOfEndLine;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(startLine, startColumn, endLine, endColumn, toBottomOfEndLine);
    }

    @Override
    public String toString() {
        return "CodeBlock{" +
                "startLine=" + startLine +
                ", startColumn=" + startColumn +
                ", endLine=" + endLine +
                ", endColumn=" + endColumn +
                ", toBottomOfEndLine=" + toBottomOfEndLine +
                '}';
    }
}
