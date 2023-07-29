package net.fred.lua;

import static org.junit.Assert.assertNotNull;

import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.internal.MemorySegment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MemorySegmentTest {
    @Before
    public void setUp() throws Exception {
        Log.i("test", "run setup");
    }

    @Test
    public void testAllocate() throws NativeMethodException {
        assertNotNull(InstrumentationRegistry.getInstrumentation().getTargetContext());
        MemorySegment seg = MemorySegment.create(100);
        MemorySegment seg2 = MemorySegment.create(200);
        seg.addChild(seg2);

        seg.close();

        Assert.assertTrue(seg.getFreed().getFlag());
        Assert.assertTrue(seg2.getFreed().getFlag());
        Assert.assertFalse(seg2.checkIsParent(seg));

        Log.i("test", "allocate test passed!");
        System.out.println("allocate test passed");
    }
}
