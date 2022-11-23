package net.fred.lua.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    
    public static String getCurrentTimeString() {
		return new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date());
	}
    
}
