package net.fred.lua.editor.text;

import static org.junit.Assert.*;

import org.junit.Test;

public class ContentTest {
    @Test
    public void testInsert() {
        Content src = new Content(true);
        src.insert(0,0, "Hello\nWorld");

        assertEquals("Hello", src.getLine(0).toString());
        assertEquals("World", src.getLine(1).toString());

        assertEquals(2, src.getLineCount());
        assertEquals(5, src.getColumnCount(0));
        assertEquals(5, src.getColumnCount(1));

        assertEquals(6, src.getIndexer().getCharIndex(1, 0));
        CharPosition position = src.getIndexer().getCharPosition(6);
        assertEquals(1, position.getLine());
        assertEquals(0, position.getColumn());

        position = src.getIndexer().getCharPosition(5);
        assertEquals(0, position.getLine());
        assertEquals(5, position.getColumn());

        assertEquals(String.valueOf('o'), String.valueOf(src.charAt(4)));
        assertEquals(String.valueOf('\n'), String.valueOf(src.charAt(5)));

        src.insert(0, 5, " ");
        assertEquals("Hello ", src.getLine(0).toString());
    }
}