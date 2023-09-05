package net.fred.lua.common;

import android.util.Log;

import androidx.annotation.NonNull;

import net.fred.lua.App;
import net.fred.lua.common.utils.DateUtils;
import net.fred.lua.common.utils.StringUtils;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.io.CacheDirectoryManager;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public final class Logger implements AutoCloseable {

    private static Logger logger;
    private PrintStream stream;

    private Logger() {
        File logFile = CacheDirectoryManager.getInstance().getLoggerFile();

        if (logFile.exists()) {
            FileUtils.deleteQuietly(logFile);
        }

        try {
            logFile.createNewFile();
            stream = new PrintStream(new FileOutputStream(logFile, true));
        } catch (IOException e) {
            CrashHandler.fastHandleException(e);
        }
    }

    @NonNull
    public static Logger getInstance() {
        if (logger != null) {
            return logger;
        }
        synchronized (Logger.class) {
            if (logger == null) {
                logger = new Logger();
            }
        }
        return logger;
    }

    public static PrintStream stream() {
        return getInstance().stream;
    }

    public static void i(String msg) {
        String sb = "INFO " + getOtherInfo() + ThrowableUtils.getCallerString() +
                " :" + msg;
        writeLine(sb);
    }

    /**
     * {@link StringUtils#templateOf}
     */
    public static void i(String base, Object... fmt) {
        i(StringUtils.templateOf(base, fmt));
    }

    public static void e(String msg) {
        String sb = "ERROR " + getOtherInfo() + ThrowableUtils.getCallerString() +
                " :" + msg;
        writeLine(sb);
    }

    public static void w(String msg) {
        String sb = "WARN " + getOtherInfo() + ThrowableUtils.getCallerString() +
                " :" + msg;
        writeLine(sb);
    }

    @NonNull
    private static String getOtherInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(DateUtils.getCurrentTimeString("yyyy-MM-dd HH:mm:ss"))
                .append(" ").append(Thread.currentThread().getName())
                .append(" ").append(Thread.currentThread().getId())
                .append("/").append(android.os.Process.myPid());
        App instance = App.getInstance();
        sb.append("(").append(instance != null ? instance.getPackageName() : "Unknown")
                .append(") ");
        return sb.toString();
    }

    public static void write(String msg) {
        Log.i("logger", msg);
        stream().print(msg);
    }

    public static void write(int i) {
        Log.i("logger", String.valueOf(i));
        stream().write(i);
    }

    public static void writeLine(String msg) {
        write(msg + "\n");
    }

    @Override
    public void close() {
        if (stream != null)
            stream.close();
    }

}
