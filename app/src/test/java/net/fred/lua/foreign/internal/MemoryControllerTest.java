package net.fred.lua.foreign.internal;

import org.junit.Test;

import static org.junit.Assert.*;

public class MemoryControllerTest {
    @Test
    public void testAddChild() {
        MemoryController base = new MemoryController();
        MemoryController child = new MemoryController();

        base.addChild(child);
        assertTrue(base.hasChild());
        base.freeChildren();
        assertFalse(base.hasChild());
        base.close();
        assertTrue(base.getFreed().getFlag());
        assertTrue(child.getFreed().getFlag());

        System.out.println("addChild passed!");
    }

    @Test
    public void testChildFree() {
        MemoryController base = new MemoryController();
        MemoryController child = new MemoryController();
        base.addChild(child);
        base.close();

        assertTrue(base.getFreed().getFlag());
        assertTrue(base.getFreed().getFlag());

        System.out.println("Free together passed!");
    }

    @Test
    public void testChildFree2() {
        MemoryController base = new MemoryController();
        MemoryController child = new MemoryController();
        base.addChild(child);

        child.close();
        assertFalse(base.hasChild());
        assertTrue(child.getFreed().getFlag());

        base.close();
    }
}
