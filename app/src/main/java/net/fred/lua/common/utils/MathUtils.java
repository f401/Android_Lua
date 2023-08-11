package net.fred.lua.common.utils;

import android.content.res.Resources;

public final class MathUtils {

    public static int dp2px(float dp) {
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int square(int src) {
        return src * src;
    }

    public static float square(float src) {
        return src * src;
    }

}
