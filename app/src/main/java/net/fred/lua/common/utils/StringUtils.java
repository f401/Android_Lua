package net.fred.lua.common.utils;

public class StringUtils {
    
    public static String fixLastSeparator(String str) {
		return str.endsWith("/") ? str : str + "/";
	}
    
}
