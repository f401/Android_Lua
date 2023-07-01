package net.fred.lua.common.utils;

public class StringUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String fixLastSeparator(String str) {
        return str.endsWith("/") ? str : str + "/";
    }

}
