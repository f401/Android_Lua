package net.fred.lua.common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import net.fred.lua.common.CrashHandler;
import java.io.FileInputStream;

public final class FileUtils {
    
    
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
    
    public static String readFile(File file) {
        FileInputStream fis = null;
        String result = "";
        try {
            fis = new FileInputStream(file);
            StringBuilder sb = new StringBuilder();
            int size;
            byte[] buffer = new byte[1024];
            while((size = fis.read(buffer)) > 0) {
                sb.append(new String(buffer, 0, size));
            }
            result = sb.toString();
        } catch(IOException e) {
            CrashHandler.getInstance().uncaughtException(
            Thread.currentThread(), e);
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                CrashHandler.getInstance().uncaughtException(
           
                Thread.currentThread(), e);
            }
        }
        return result;
    }
    
}
