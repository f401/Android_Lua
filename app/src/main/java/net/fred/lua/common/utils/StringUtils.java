package net.fred.lua.common.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StringUtils {

    public static boolean isEmpty(@Nullable String str) {
        return str == null || str.isEmpty();
    }

    public static String fixLastSeparator(@NonNull String str) {
        return str.endsWith("/") ? str : str + "/";
    }

}
