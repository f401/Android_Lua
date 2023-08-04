package net.fred.lua.common.utils;

import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.Logger;

import java.io.File;
import java.io.IOException;

public final class FileUtils {

    public static boolean exists(String file) {
        return !StringUtils.isEmpty(file) && new File(file).exists();
    }

    public static void makeDirs(String dir) {
        if (!StringUtils.isEmpty(dir)) {
            try {
                org.apache.commons.io.FileUtils.forceMkdir(new File(dir));
            } catch (IOException e) {
                CrashHandler.fastHandleException(e);
            }
        }
    }

    public static void writeFile(File file, String content, boolean append) {
        if (file == null || content == null) return;
        try {
            org.apache.commons.io.FileUtils.forceMkdirParent(file);
            org.apache.commons.io.FileUtils.writeStringToFile(file, content, "UTF-8", append);
        } catch (IOException e) {
            CrashHandler.fastHandleException(e);
        }
    }

    public static void deleteDirectory(File directory) {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            Logger.e("Failed to delete directory " + directory + ", because: " + e.getMessage());
        }
    }
}
