package net.fred.lua.editor;

import net.fred.lua.common.Pair;

public class Document extends TextLine {
    private int cursor;
    private boolean blockMode;
    private Pair<Integer, Integer> blocked;

    public static Document open() {
        char[] buffer = "print(\"Hello\")l \n;".toCharArray();
        Document tl = new Document();
        tl.setBuffer(buffer, 2, buffer.length);
        return tl;
    }
}
