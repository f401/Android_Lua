package net.fred.lua.ui.view;

import org.junit.Assert;
import org.junit.Test;

public class FreeScrollViewTest {

    @Test
    public void testEvalCircleR() {
        Assert.assertEquals(FreeScrollView.evalCircleRadius(10), 185 / 12);
    }
}
