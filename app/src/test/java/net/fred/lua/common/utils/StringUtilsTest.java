package net.fred.lua.common.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void isEmpty() {
        assertTrue(StringUtils.isEmpty(""));
        assertTrue(StringUtils.isEmpty(null));
        assertFalse(StringUtils.isEmpty("3829"));
        assertFalse(StringUtils.isEmpty("djjad"));
    }

    @Test
    public void fixLastSeparator() {
        assertEquals("/usr/bin/", StringUtils.fixLastSeparator("/usr/bin"));
        assertEquals("/usr/bin/", StringUtils.fixLastSeparator("/usr/bin/"));
    }

    @Test
    public void templateOf() {
        assertEquals("1 + 2 = 3", StringUtils.templateOf("1 + 2 = {}", 3));
        assertEquals("Hello World", StringUtils.templateOf("Hello {}", "World"));
    }

    @Test
    public void getSuffix() {
        assertEquals("java", StringUtils.getSuffix("Main.java"));
        assertEquals("cs", StringUtils.getSuffix("Program.cs"));
    }
}