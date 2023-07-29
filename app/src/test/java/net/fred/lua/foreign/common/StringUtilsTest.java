package net.fred.lua.foreign.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.fred.lua.common.utils.StringUtils;

import org.junit.Test;

public class StringUtilsTest {
    @Test
    public void testStringTemplate() {
        assertEquals(StringUtils.templateOf("{}: {}", "name", "Tome"), "name: Tome");
    }

    @Test
    public void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(""));
        assertTrue(StringUtils.isEmpty(null));
    }

    @Test
    public void testFix() {
        assertEquals(StringUtils.fixLastSeparator("/usr"), "/usr/");
        assertEquals(StringUtils.fixLastSeparator("/usr/"), "/usr/");
    }
}
