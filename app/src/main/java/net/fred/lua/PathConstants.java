package net.fred.lua;

import android.content.Context;

import androidx.annotation.NonNull;

import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.common.utils.StringUtils;

public class PathConstants {
    public static String NATIVE_LIBRARY_DIR;

    public static void init(@NonNull Context ctx) {
        NATIVE_LIBRARY_DIR = StringUtils.fixLastSeparator(
                ctx.getApplicationInfo().nativeLibraryDir);
    }

    private static String makeDir(@NonNull String first, @NonNull String second) {
        String path = StringUtils.fixLastSeparator(
                first) + StringUtils.fixLastSeparator(second);
        FileUtils.makeDirs(path);
        return path;
    }

}
