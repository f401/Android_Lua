package net.fred.lua.foreign.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.fred.lua.foreign.NativeMethodException;

import org.junit.Test;

public class MemoryControllerTest {
    @Test
    public void testAddChild() throws NativeMethodException {
        MemoryController base = new MemoryController();
        MemoryController child = new MemoryController();

        base.addChild(child);
        assertTrue(base.hasChild());
        base.freeChildren();
        assertFalse(base.hasChild());
        base.close();
        assertTrue(base.isClosed());
        assertTrue(child.isClosed());

        System.out.println("addChild passed!");
    }

    @Test
    public void testChildFree() throws NativeMethodException {
        MemoryController base = new MemoryController();
        MemoryController child = new MemoryController();
        base.addChild(child);
        base.close();

        assertTrue(base.isClosed());
        assertTrue(base.isClosed());

        System.out.println("Free together passed!");
    }

    @Test
    public void testChildFree2() throws NativeMethodException {
        MemoryController base = new MemoryController();
        MemoryController child = new MemoryController();
        base.addChild(child);

        child.close();
        assertFalse(base.hasChild());
        assertTrue(child.isClosed());

        base.close();
    }
}
