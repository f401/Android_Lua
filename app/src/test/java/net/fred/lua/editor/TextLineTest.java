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
}
