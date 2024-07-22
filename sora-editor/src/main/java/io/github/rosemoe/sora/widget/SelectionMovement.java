package io.github.rosemoe.sora.widget;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.TextUtils;
import io.github.rosemoe.sora.util.Chars;
import io.github.rosemoe.sora.util.IntPair;
import io.github.rosemoe.sora.util.Numbers;
import io.github.rosemoe.sora.widget.layout.Layout;
import io.github.rosemoe.sora.widget.layout.Row;

public enum SelectionMovement {
    /**
     * Move Up
     */
    UP(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition position) {
            long newPos = editor.getLayout().getUpPosition(position.getLine(), position.getColumn());
            return editor.getText().getIndexer().getCharPosition(IntPair.getFirst(newPos), IntPair.getSecond(newPos));
        }
    }, MovingBasePosition.LEFT_SELECTION),
    /**
     * Move Down
     */
    DOWN(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition position) {
            long newPos = editor.getLayout().getDownPosition(position.getLine(), position.getColumn());
            return editor.getText().getIndexer().getCharPosition(IntPair.getFirst(newPos), IntPair.getSecond(newPos));
        }
    }, MovingBasePosition.RIGHT_SELECTION),
    /**
     * Move Left
     */
    LEFT(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition position) {
            long newPos = editor.getCursor().getLeftOf(position.toIntPair());
            return editor.getText().getIndexer().getCharPosition(IntPair.getFirst(newPos), IntPair.getSecond(newPos));
        }
    }, MovingBasePosition.LEFT_SELECTION),
    /**
     * Move Right
     */
    RIGHT(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition position) {
            long newPos = editor.getCursor().getRightOf(position.toIntPair());
            return editor.getText().getIndexer().getCharPosition(IntPair.getFirst(newPos), IntPair.getSecond(newPos));
        }
    }, MovingBasePosition.RIGHT_SELECTION),
    /**
     * Move To Previous Word Boundary
     */
    PREVIOUS_WORD_BOUNDARY(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition position) {
            CharPosition pos = Chars.prevWordStart(position, editor.getText());
            return editor.getText().getIndexer().getCharPosition(pos.getLine(), pos.getColumn());
        }
    }),
    /**
     * Move To Next Word Boundary
     */
    NEXT_WORD_BOUNDARY(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition position) {
            CharPosition pos = Chars.nextWordEnd(position, editor.getText());
            return editor.getText().getIndexer().getCharPosition(pos.getLine(), pos.getColumn());
        }
    }),
    /**
     * Move Page Up
     */
    PAGE_UP(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition position) {
            Layout layout = editor.getLayout();
            int rowCount = (int) Math.ceil(editor.getHeight() / (float) editor.getRowHeight());
            int currIdx = layout.getRowIndexForPosition(position.getIndex());
            int afterIdx = Numbers.coerceIn(currIdx + rowCount, 0, layout.getRowCount() - 1);
            int selOffset = position.getColumn() - layout.getRowAt(currIdx).startColumn;
            Row row = layout.getRowAt(afterIdx);
            int line = row.lineIndex;
            int column =
                    row.startColumn + Numbers.coerceIn(selOffset, 0, row.endColumn - row.startColumn);
            return editor.getText().getIndexer().getCharPosition(line, column);
        }
    }),

    /**
     * Move Page Down
     */
    PAGE_DOWN(new SelectionMovementComputer() {

        @Override
        public CharPosition compute(CodeEditor editor, CharPosition pos) {
            Layout layout = editor.layout;
            int rowCount = (int) Math.ceil(editor.getHeight() / (float) editor.getRowHeight());
            int currIdx = layout.getRowIndexForPosition(pos.index);
            int afterIdx = Numbers.coerceIn(currIdx + rowCount, 0, layout.getRowCount() - 1);
            int selOffset = pos.column - layout.getRowAt(currIdx).startColumn;
            Row row = layout.getRowAt(afterIdx);
            int line = row.lineIndex;
            int column =
                    row.startColumn + Numbers.coerceIn(selOffset, 0, row.endColumn - row.startColumn);
            return editor.getText().getIndexer().getCharPosition(line, column);
        }
    }),

    /**
     * Move To Page Top
     */
    PAGE_TOP(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition pos) {
            Layout layout = editor.layout;
            int currIdx = layout.getRowIndexForPosition(pos.index);
            int selOffset = pos.column - layout.getRowAt(currIdx).startColumn;
            int afterIdx = editor.getFirstVisibleRow();
            Row row = layout.getRowAt(afterIdx);
            int line = row.lineIndex;
            int column =
                    row.startColumn + Numbers.coerceIn(selOffset, 0, row.endColumn - row.startColumn);
            return editor.getText().getIndexer().getCharPosition(line, column);
        }
    }),

    /**
     * Move To Page Bottom
     */
    PAGE_BOTTOM(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition pos) {
            Layout layout = editor.layout;
            int currIdx = layout.getRowIndexForPosition(pos.index);
            int selOffset = pos.column - layout.getRowAt(currIdx).startColumn;
            int afterIdx = editor.getLastVisibleRow();
            Row row = layout.getRowAt(afterIdx);
            int line = row.lineIndex;
            int column =
                    row.startColumn + Numbers.coerceIn(selOffset, 0, row.endColumn - row.startColumn);
            return editor.getText().getIndexer().getCharPosition(line, column);
        }
    }),

    /**
     * Move To Line Start
     */
    LINE_START(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition pos) {
            if (editor.getProps().enhancedHomeAndEnd) {
                int column = IntPair.getFirst(
                        TextUtils.findLeadingAndTrailingWhitespacePos(
                                editor.getText().getLine(pos.line)
                        )
                );
                if (pos.column != column) {
                    return editor.getText().getIndexer().getCharPosition(pos.line, column);
                } else {
                    return editor.getText().getIndexer().getCharPosition(pos.line, 0);
                }
            } else {
                return editor.getText().getIndexer().getCharPosition(pos.line, 0);
            }
        }
    }),

    /**
     * Move To Line End
     */
    LINE_END(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition pos) {
            int colNum = editor.getText().getColumnCount(pos.line);
            if (editor.getProps().enhancedHomeAndEnd) {
                int column = IntPair.getSecond(
                        TextUtils.findLeadingAndTrailingWhitespacePos(
                                editor.getText().getLine(pos.line)
                        )
                );
                if (pos.column != column) {
                    return editor.getText().getIndexer().getCharPosition(pos.line, column);
                } else {
                    return editor.getText().getIndexer().getCharPosition(pos.line, colNum);
                }
            } else {
                return editor.getText().getIndexer().getCharPosition(pos.line, colNum);
            }
        }
    }),

    /**
     * Move To Text Start
     */
    TEXT_START(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition position) {
            return new CharPosition().toBOF();
        }
    }),


    /**
     * Move To Text End
     */
    TEXT_END(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition position) {
            return editor.getText().getIndexer().getCharPosition(editor.getText().length());
        }
    });

    @NonNull
    private final SelectionMovementComputer computer;

    @NonNull
    private final MovingBasePosition basePosition;

    SelectionMovement(@NonNull SelectionMovementComputer computer) {
        this(computer, MovingBasePosition.SELECTION_ANCHOR);
    }

    SelectionMovement(@NonNull SelectionMovementComputer computer, @NonNull MovingBasePosition basePosition) {
        this.computer = computer;
        this.basePosition = basePosition;
    }

    @NonNull
    public MovingBasePosition getBasePosition() {
        return basePosition;
    }

    public CharPosition getPositionAfterMovement(CodeEditor editor, CharPosition pos) {
        return computer.compute(editor, pos);
    }

    public enum MovingBasePosition {
        LEFT_SELECTION,
        RIGHT_SELECTION,
        SELECTION_ANCHOR
    }

    public interface SelectionMovementComputer {
        CharPosition compute(CodeEditor editor, CharPosition position);
    }
}