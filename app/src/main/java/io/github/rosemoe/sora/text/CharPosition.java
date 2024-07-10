/*
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */
package io.github.rosemoe.sora.text;

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

    public void set(CharPosition src) {
        this.mLine = src.mLine;
        this.mColumn = src.mColumn;
        this.mIndex = src.mIndex;
    }

    public CharPosition copy() {
        return new CharPosition(mLine, mColumn, mIndex);
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
