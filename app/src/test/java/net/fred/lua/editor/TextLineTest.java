package net.fred.lua.editor;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TextLineTest {
    private TextLine textLine;

    @Before
    public void setup() {
        textLine = new TextLine();
        char[] buff = "Hello".toCharArray();
        textLine.setBuffer(buff, 1, buff.length);
        System.out.println("Before: " + textLine.toString());
    }

    @Test
    public void testInsert() {
        textLine.insert("ae".toCharArray(), 2);
        System.out.println("After insert: " + textLine.toString());
        assertEquals("Heaello", textLine.toString());
    }

    @Test
    public void testReplace() {
        setup();
        textLine.replace(0, 2, "H".toCharArray());
        System.out.println("After replace: " + textLine.toString());
        assertEquals("Hllo", textLine.toString());
    }

    @Test
    public void testRowToOff() {
        char[] src = "aa\nbb\ncc".toCharArray();
        textLine.setBuffer(src, 3, src.length);
        assertEquals(textLine.getRowToOffBelong(6), 3);
        assertEquals(textLine.getRowToOffBelong(4), 2);
        assertEquals(textLine.getRowToOffBelong(0), 1);
        assertEquals(textLine.getRowToOffBelong(3), 2);
        textLine.replace(0, 3, "cc".toCharArray());
        assertEquals(textLine.toString(), "ccbb\ncc");
        assertEquals(textLine.getRowToOffBelong(0), 1);
        assertEquals(textLine.getRowToOffBelong(5), 2);
        //test cache
        assertEquals(textLine.getRowToOffBelong(4), 2);
        assertEquals(textLine.getRowToOffBelong(3), 1);
    }
}
