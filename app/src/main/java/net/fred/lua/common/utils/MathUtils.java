package net.fred.lua.common.utils;

import android.content.res.Resources;

import androidx.annotation.Px;
import androidx.annotation.Size;

public final class MathUtils {

    @Px
    public static int dp2px(@Size float dp) {
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
