package net.fred.lua.common.utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class FileUtils {
    private static final String TAG = "FileUtils";

    public static boolean exists(String file) {
        return !StringUtils.isEmpty(file) && new File(file).exists();
    }

    public static void makeDirs(String dir) {
        if (!StringUtils.isEmpty(dir)) {
            makeDirs(new File(dir));
        }
    }

    public static void makeDirs(File dir) {
        try {
            org.apache.commons.io.FileUtils.forceMkdir(dir);
        } catch (IOException e) {
            Log.w(TAG, "Make directory method 1 failed. trying method 2." + e.getMessage());
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    public static void writeFile(File file, String content, boolean append) {
        if (file == null || content == null) return;
        try {
            makeDirs(file.getParentFile());
            org.apache.commons.io.FileUtils.writeStringToFile(file, content, "UTF-8", append);
        } catch (IOException e) {
            Log.w(TAG, "Write file method 1 failed. trying method 2." + e.getMessage());
            makeDirs(file.getParentFile());
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file, append);
                byte[] data = content.getBytes(StandardCharsets.UTF_8);
                fileOutputStream.write(data, 0, data.length);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                ThrowableUtils.closeAll(fileOutputStream);
            }
        }
    }

    public static void deleteDirectory(File directory) {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete directory " + directory + ", because: " + e.getMessage());
        }
    }

}
