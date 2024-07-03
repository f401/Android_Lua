package net.fred.lua.editor.text;

import androidx.annotation.NonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class CharPosition {
    private int mLine;
    private int mColumn;
    private int mIndex;

    public CharPosition(int line, int column, int index) {
        this.mLine = line;
        this.mColumn = column;
        this.mIndex = index;
    }

    public int getLine() {
        return mLine;
    }

    public void setLine(int line) {
        this.mLine = line;
    }

    public int getColumn() {
        return mColumn;
    }

    public void setColumn(int column) {
        this.mColumn = column;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        this.mIndex = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof CharPosition) {
            CharPosition other = (CharPosition) obj;
            return Objects.equal(other.mLine, this.mLine) &&
                    Objects.equal(other.mColumn, this.mColumn) &&
                    Objects.equal(other.mIndex, this.mIndex);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mLine, mColumn, mIndex);
    }

    @NonNull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("line", mLine)
                .add("column", mColumn)
                .add("index", mIndex).toString();
    }
   
}
