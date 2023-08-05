package net.fred.lua.common.utils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static final String DEFAULT_DATE_FORMAT = "yyyy_MM_dd-HH_mm_ss";

    @NonNull
    public static String getCurrentTimeString() {
        return getCurrentTimeString(DEFAULT_DATE_FORMAT);
    }

    @NonNull
    public static String getCurrentTimeString(@NonNull String fmt) {
        return formatDate(fmt, System.currentTimeMillis());
    }

    public static String formatDate(long date) {
        return formatDate(DEFAULT_DATE_FORMAT, date);
    }

    @NonNull
    public static String formatDate(@NonNull String fmt, long date) {
        return new SimpleDateFormat(fmt, Locale.getDefault()).format(new Date(date));
    }

}
