package net.fred.lua.io;

import androidx.annotation.NonNull;

import net.fred.lua.common.utils.ThrowableUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * 该类用来扫描 Log 类输出的日志并存储到文件中
 */
public final class LogScanner {

    private static LogScanner instance;

    private LogScanner() {
    }

    @NonNull
    public static LogScanner getInstance() {
        if (instance == null) {
            instance = new LogScanner();
        }
        return instance;
    }

    public static void cleanBuffer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new ProcessBuilder("logcat", "-c").start().waitFor();
                } catch (InterruptedException | IOException ignored) {
                    Logger.e("Clean Logcat Buffer failed!");
                }
            }
        }, "Log buffer cleaner.").start();
    }

    public void start() {
        new Thread(new DoScan()).start();
    }

    private static class DoScan implements Runnable {

        @Override
        public void run() {
            PrintStream outputStream = null;
            try {
                outputStream = new PrintStream(LogFileManager.getInstance().
                        getLogScannerFile());
                while (!Thread.currentThread().isInterrupted()) {
                    Process process = new ProcessBuilder("logcat").redirectErrorStream(true).start();
                    InputStream is = process.getInputStream();
                    int len;
                    byte[] buffer = new byte[1024];
                    while (!Thread.currentThread().isInterrupted() && (len = is.read(buffer)) > -1) {
                        outputStream.print(new String(buffer, 0, len));
                        outputStream.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logger.e("Failed to start log scanner;" + e.getMessage());
            } finally {
                ThrowableUtils.closeAll(outputStream);
            }
        }

    }

}
