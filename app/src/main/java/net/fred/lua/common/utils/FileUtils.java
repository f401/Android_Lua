package net.fred.lua.common.utils;

import net.fred.lua.common.CrashHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileUtils {

    public static boolean exists(String file) {
        return !StringUtils.isEmpty(file) && new File(file).exists();
    }
    public static boolean makeDirs(String dir) {
        if (dir != null) {
            File file = new File(dir);
            if (!file.exists())
                return file.mkdirs();
        }
        return false;
    }

    public static boolean makeParentDir(File dir) {
        if (dir != null) {
            File parent = dir.getParentFile();
            if (parent != null && !parent.exists())
                return parent.mkdirs();
        }
        return false;
    }

    public static void writeFile(File file, String content, boolean append) {
        if (file == null || content == null) return;
        makeParentDir(file);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, append);
            fos.write(content.getBytes());
        } catch (IOException e) {
            CrashHandler.fastHandleException(e);
        } finally {
            ThrowableUtils.closeAll(fos);
        }
    }
    
    public static void writeFile(File file, String content) {
        writeFile(file, content, false);
    }

    public static String readFile(File file) {
        if (file == null) return null;
        FileUtils.checkExistsOrThrow(file);
        FileInputStream fis = null;
        String result = "";
        try {
            fis = new FileInputStream(file);
            StringBuilder sb = new StringBuilder();
            int size;
            byte[] buffer = new byte[1024];
            while ((size = fis.read(buffer)) > 0) {
                sb.append(new String(buffer, 0, size));
            }
            result = sb.toString();
        } catch (IOException e) {
            CrashHandler.fastHandleException(e);
        } finally {
            ThrowableUtils.closeAll(fis);
        }
        return result;
    }

    public static void checkExistsOrThrow(File file) {
        if (!file.exists())
            throw new RuntimeException("File path are null.");
    }

    public static long evalDirectoryTotalSize(File file) {
        long result = 0;
        File[] sub = file.listFiles();
        if (sub != null) {
            for (File curr : sub) {
                result += curr.isDirectory() ? evalDirectoryTotalSize(curr) : curr.length();
            }
        }
        return result;
    }

    public static String shrinkToBestDisplay(long bytes) {
        return shrinkToBestDisplay((double) bytes, 0);
    }

    private static String shrinkToBestDisplay(double d, int currLevel) {
        if (d < 1024 || currLevel == 3) {
            switch (currLevel) {
                case 0:
                    return d + "bytes";
                case 1:
                    return d + "KB";
                case 2:
                    return d + "MB";
                case 3:
                    return d + "GB";
                default:
                    return "";
            }
        } else {
            return shrinkToBestDisplay(d / 1024, currLevel + 1);
        }
    }

    public static void removeDirectory(File file) {
        File[] subs = file.listFiles();
        if (subs != null) {
            for (File curr : subs) {
                if (curr.isDirectory()) {
                    removeDirectory(curr);
                }
                curr.delete();
            }
        }
    }
}
