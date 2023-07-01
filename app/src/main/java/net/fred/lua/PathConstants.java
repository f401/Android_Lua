package net.fred.lua;

import android.content.Context;

import net.fred.lua.common.utils.DateUtils;
import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.common.utils.StringUtils;

public class PathConstants {

    public static String LOG_FILE_PATH;
    public static String STDOUT;
    public static String STDERR;
    public static String EXTERNAL_CACHE_DIR;
    public static String LOGGER_FILE_PATH;
    public static String CRASH_FILE_SAVE_DIR;
    public static String LOGGER_FILE_SAVE_DIR;

    public static void init(Context ctx) {
        EXTERNAL_CACHE_DIR = ctx.getExternalCacheDir().toString();
        LOG_FILE_PATH = makeDir(EXTERNAL_CACHE_DIR
                , "logs/") + DateUtils.getCurrentTimeString() + "-log.log";
        STDOUT = makeDir(EXTERNAL_CACHE_DIR
                , "std/") + DateUtils.getCurrentTimeString() + "-out.log";
        STDERR = makeDir(EXTERNAL_CACHE_DIR
                , "std/") + DateUtils.getCurrentTimeString() + "-err.log";
        CRASH_FILE_SAVE_DIR = makeDir(EXTERNAL_CACHE_DIR, "crash/");
        LOGGER_FILE_SAVE_DIR = makeDir(EXTERNAL_CACHE_DIR, "loggers/");
    }

    private static String makeDir(String first, String second) {
        String path = StringUtils.fixLastSeparator(
                first) + StringUtils.fixLastSeparator(second);
        FileUtils.makeDirs(path);
        return path;
    }

    public static String getNativeLibraryPath(Context ctx) {
        return ctx.getApplicationInfo().nativeLibraryDir;
    }

}
