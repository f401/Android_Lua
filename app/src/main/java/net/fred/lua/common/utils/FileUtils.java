package net.fred.lua.common.utils;

import net.fred.lua.common.CrashHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileUtils {

    public static boolean makeDirs(String dir) {
        if (dir != null) {
            File file = new File(dir);
            if (!file.exists())
                return file.mkdirs();
        }
        return false;
    }

    public static boolean makeParentDir(String dir) {
        return dir != null ? makeParentDir(new File(dir)) : false;
    }

    public static boolean makeParentDir(File dir) {
        if (dir != null) {
            File parent = dir.getParentFile();
            if (!parent.exists())
                return parent.mkdirs();
        }
        return false;
    }

    public static void writeFile(String file, String content) {
        if (file != null)
            writeFile(new File(file), content);
    }

    public static void writeFile(File file, String content) {
        if (file == null || content == null) return;
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
        } catch (IOException e) {
            CrashHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        } finally {
            ThrowableUtils.closes(fos);
        }
    }

    public static String readFile(File file) {
        if (file == null) return null;
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
            CrashHandler.getInstance().uncaughtException(
                    Thread.currentThread(), e);
        } finally {
            ThrowableUtils.closes(fis);
        }
        return result;
    }

}
