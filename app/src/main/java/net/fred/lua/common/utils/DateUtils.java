package net.fred.lua.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    
    public static String getCurrentTimeString() {
		return getCurrentTimeString("yyyy_MM_dd-HH_mm_ss");
    }

    public static String getCurrentTimeString(String fmt) {
		return new SimpleDateFormat(fmt).format(new Date());
    }
    
}
