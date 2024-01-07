package net.fred.lua.io;

import android.util.Log;

import androidx.annotation.NonNull;

import net.fred.lua.common.utils.ThrowableUtils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 该类用来扫描 Log 类输出的日志并存储到文件中
 */
public final class LogScanner {
    private static final String TAG = "LogScanner";
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
                    Log.i(TAG, "Clean logcat finished");
                } catch (InterruptedException | IOException ignored) {
                    Log.e(TAG, "Clean Logcat Buffer failed!");
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
            BufferedOutputStream writer = null;
            try {
                // Wait for compression to complete before writing data to the file
                LogFileManager.getInstance().waitForCompressionFinish();
                Log.i(TAG, "Log Scanner started");
                writer = new BufferedOutputStream(new FileOutputStream(
                        LogFileManager.getInstance().getLogScannerFile()));
                while (!Thread.currentThread().isInterrupted()) {
                    Process process = new ProcessBuilder("logcat").redirectErrorStream(true).start();
                    InputStream is = process.getInputStream();
                    int len;
                    byte[] buffer = new byte[1024];
                    while (!Thread.currentThread().isInterrupted() && (len = is.read(buffer)) > -1) {
                        if (len != 0) {
                            writer.write(buffer, 0, len);
                        }
                        writer.flush();
                    }
                }
                Log.i(TAG, "Log Scanner died.");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to start log scanner;" + e.getMessage());
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread interrupted", e);
            } finally {
                ThrowableUtils.closeAll(writer);
            }
        }

    }

}
