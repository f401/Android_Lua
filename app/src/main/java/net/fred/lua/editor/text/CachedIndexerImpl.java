package net.fred.lua.editor.text;

import androidx.annotation.NonNull;

public class CachedIndexerImpl implements IIndexer {


    @Override
    public int getCharIndex(int line, int column) {
        return 0;
    }

    @Override
    public int getCharLine(int index) {
        return 0;
    }

    @Override
    public int getCharColumn(int index) {
        return 0;
    }

    @NonNull
    @Override
    public CharPosition getCharPosition(int index) {
        return null;
    }

    @NonNull
    @Override
    public CharPosition getCharPosition(int line, int column) {
        return null;
    }

    @Override
    public void getCharPosition(int index, CharPosition dest) {

    }

    @Override
    public void getCharPosition(int line, int column, CharPosition dest) {

    }
}
