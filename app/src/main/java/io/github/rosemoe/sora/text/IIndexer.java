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
 *
 */

package io.github.rosemoe.sora.text;

import androidx.annotation.NonNull;

public interface IIndexer extends Content.OnContentChangeListener {
    int getCharIndex(int line, int column);

    int getCharLine(int index);

    int getCharColumn(int index);

    @NonNull
    CharPosition getCharPosition(int index);

    @NonNull
    CharPosition getCharPosition(int line, int column);

    void getCharPosition(int index, CharPosition dest);

    void getCharPosition(int line, int column, CharPosition dest);
}