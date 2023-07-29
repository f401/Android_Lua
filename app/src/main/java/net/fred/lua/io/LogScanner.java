package net.fred.lua.io;

import android.util.Log;

import androidx.annotation.NonNull;

import net.fred.lua.PathConstants;
import net.fred.lua.common.Flag;
import net.fred.lua.common.utils.ThrowableUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * 该类用来扫描 Log 类输出的日志并存储到文件中
 */

public class LogScanner {

    private static LogScanner instance;
    private final Flag flag;

    private LogScanner() {
        flag = new Flag(true);
    }

    @NonNull
    public static LogScanner getInstance() {
        if (instance == null) {
            instance = new LogScanner();
        }
        return instance;
    }

    public void setFlag(boolean flag) {
        this.flag.setFlag(flag);
    }

    public boolean getFlag() {
        return this.flag.getFlag();
    }

    public void start() {
        new Thread(new DoScan()).start();
    }

    private class DoScan implements Runnable {

        @Override
        public void run() {
            PrintWriter outputStream = null;
            try {
                outputStream = new PrintWriter(PathConstants.LOG_FILE_PATH);
                while (flag.getFlag()) {
                    new ProcessBuilder("logcat", "-c").start().waitFor();
                    Process process = new ProcessBuilder("logcat").redirectErrorStream(true).start();
//                    Process pro = new ProcessBuilder().command("logcat", "-c").redirectErrorStream(true).start();
                    InputStream is = process.getInputStream();
                    int len;
                    byte[] buffer = new byte[1024];
                    while (flag.getFlag() && (len = is.read(buffer)) > -1) {
                        outputStream.print(new String(buffer, 0, len));
                        outputStream.flush();
                    }
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                Log.e("Scanner", ThrowableUtils.getThrowableMessage(e));
            } finally {
                ThrowableUtils.closeAll(outputStream);
            }
        }

    }

}
