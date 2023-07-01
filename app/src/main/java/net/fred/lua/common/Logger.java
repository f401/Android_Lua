package net.fred.lua.common;

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
    private final File logFile;
    private PrintStream stream;

    private Logger() {
        String fileName = DateUtils.getCurrentTimeString("yyyy_MM_dd-HH_mm_ss")
                + ".log";

        logFile = new File(StringUtils.fixLastSeparator(PathConstants.LOGGER_FILE_SAVE_DIR) + fileName);

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

    public static Logger getInstance() {
        if (logger == null) {
            synchronized (Logger.class) {
                if (logger == null) {
                    logger = new Logger();
                }
            }
        }
        return logger;
    }

    public static PrintStream stream() {
        return getInstance().stream;
    }

    public static void i(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("INFO ").append(getOtherInfo()).append(ThrowableUtils.getInvokerInfoString())
                .append(" :").append(msg);
        write(sb.toString());
    }

    public static void e(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("ERROR ").append(getOtherInfo()).append(ThrowableUtils.getInvokerInfoString())
                .append(" :").append(msg);
        write(sb.toString());
    }

    public static void w(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("WARN ").append(getOtherInfo()).append(ThrowableUtils.getInvokerInfoString())
                .append(" :").append(msg);
        write(sb.toString());
    }

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

    private static void write(String msg) {
        PrintStream ps = getInstance().stream;
        ps.println(msg);
    }

    @Override
    public void close() {
        if (stream != null)
            stream.close();
    }

}
