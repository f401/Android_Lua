package net.fred.lua.editor.text;

import javax.annotation.concurrent.Immutable;
import com.google.common.base.Objects;
import com.google.common.base.MoreObjects;

@Immutable
public final class CharPosition {
    
    private final int line;
    private final int colume;
    private final int index;

    public CharPosition(int line, int colume, int index) {
        this.line = line;
        this.colume = colume;
        this.index = index;
    }

    public int getLine() {
        return line;
    }

    public int getColume() {
        return colume;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof CharPosition) {
            CharPosition other = (CharPosition) obj;
            return Objects.equal(other.line, this.line) &&
                Objects.equal(other.colume, this.colume) &&
                Objects.equal(other.index, this.index);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(line, colume, index);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("line", line)
            .add("colume", colume)
            .add("index", index).toString();
    }
   
}
