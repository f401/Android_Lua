package net.fred.lua.common;

import android.util.Log;

import androidx.annotation.NonNull;

import net.fred.lua.App;
import net.fred.lua.PathConstants;
import net.fred.lua.common.utils.DateUtils;
import net.fred.lua.common.utils.StringUtils;
import net.fred.lua.common.utils.ThrowableUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Logger implements AutoCloseable {

    private static Logger logger;
    private PrintStream stream;

    private Logger() {
        String fileName = DateUtils.getCurrentTimeString("yyyy_MM_dd-HH_mm_ss")
                + ".log";

        File logFile =
                new File(StringUtils.fixLastSeparator(PathConstants.LOGGER_FILE_SAVE_DIR) + fileName);

        if (logFile.exists()) {
            logFile.delete();
        }

        try {
            logFile.createNewFile();
            stream = new PrintStream(logFile);
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
        sb.append(DateUtils.getCurrentTimeString("yyyy_MM_dd-HH_mm_ss"))
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
