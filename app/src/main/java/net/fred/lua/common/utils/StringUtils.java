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

    @NonNull
    public static String templateOf(@NonNull String base, Object... args) {
        try {
            for (int i = 0; base.contains("{}"); ++i) {
                base = base.replaceFirst("\\{\\}", args[i].toString());
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Base: " + base);
            e.printStackTrace();
        }
        return base;
    }

}
