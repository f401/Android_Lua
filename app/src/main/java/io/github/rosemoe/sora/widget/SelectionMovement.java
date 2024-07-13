package io.github.rosemoe.sora.widget;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.text.CharPosition;
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
            long newPos = editor.getLayout().getLeftOf(position.getLine(), position.getColumn());
            return editor.getText().getIndexer().getCharPosition(IntPair.getFirst(newPos), IntPair.getSecond(newPos));
        }
    }, MovingBasePosition.LEFT_SELECTION),
    /**
     * Move Right
     */
    RIGHT(new SelectionMovementComputer() {
        @Override
        public CharPosition compute(CodeEditor editor, CharPosition position) {
            long newPos = editor.getLayout().getRightOf(position.getLine(), position.getColumn());
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


    public enum MovingBasePosition {
        LEFT_SELECTION,
        RIGHT_SELECTION,
        SELECTION_ANCHOR
    }

    public interface SelectionMovementComputer {
        CharPosition compute(CodeEditor editor, CharPosition position);
    }
}
