package net.fred.lua.editor;

import net.fred.lua.common.Pair;

public class Document extends TextLine {
    private int cursor;
    private boolean blockMode;
    private Pair<Integer, Integer> blocked;
}
