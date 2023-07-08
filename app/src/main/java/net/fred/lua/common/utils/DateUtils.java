package net.fred.lua.common.utils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    @NonNull
    public static String getCurrentTimeString() {
        return getCurrentTimeString("yyyy_MM_dd-HH_mm_ss");
    }

    @NonNull
    public static String getCurrentTimeString(@NonNull String fmt) {
        return new SimpleDateFormat(fmt, Locale.getDefault()).format(new Date());
    }

}
