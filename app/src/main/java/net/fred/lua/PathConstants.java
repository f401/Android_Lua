package net.fred.lua;

import android.content.Context;

import androidx.annotation.NonNull;

import net.fred.lua.common.utils.DateUtils;
import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.common.utils.StringUtils;

public class PathConstants {

    public static String LOG_FILE_PATH;
    public static String EXTERNAL_CACHE_DIR;
    public static String CRASH_FILE_SAVE_DIR;
    public static String LOGGER_FILE_SAVE_DIR;
    public static String NATIVE_LIBRARY_DIR;
    public static String NATIVE_CRASH_DUMP_PATH;

    public static void init(@NonNull Context ctx) {
        EXTERNAL_CACHE_DIR = ctx.getExternalCacheDir().toString();
        LOG_FILE_PATH = makeDir(EXTERNAL_CACHE_DIR
                , "logs/") + DateUtils.getCurrentTimeString() + "-log.log";
        CRASH_FILE_SAVE_DIR = makeDir(EXTERNAL_CACHE_DIR, "crash/");
        LOGGER_FILE_SAVE_DIR = makeDir(EXTERNAL_CACHE_DIR, "loggers/");

        NATIVE_LIBRARY_DIR = StringUtils.fixLastSeparator(
                ctx.getApplicationInfo().nativeLibraryDir);
        NATIVE_CRASH_DUMP_PATH = makeDir(ctx.getExternalCacheDir().toString(), "crash/native/");
    }

    private static String makeDir(@NonNull String first, @NonNull String second) {
        String path = StringUtils.fixLastSeparator(
                first) + StringUtils.fixLastSeparator(second);
        FileUtils.makeDirs(path);
        return path;
    }

}
