package net.fred.lua.editor.text;

import androidx.annotation.NonNull;

public interface IIndexer {
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