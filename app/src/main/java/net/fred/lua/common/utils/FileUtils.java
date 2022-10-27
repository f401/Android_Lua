package net.fred.lua.common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import net.fred.lua.common.CrashHandler;

public class FileUtils {
    
    
    public static void writeFile(String file, String content) {
        writeFile(new File(file), content);
    }
    
    public static void writeFile(File file, String content) {
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
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    CrashHandler.getInstance().uncaughtException(Thread.currentThread(), e);
                }
            }
        }
    }
    
}
