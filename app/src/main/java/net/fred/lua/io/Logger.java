package net.fred.lua.io;

import android.util.Log;

import androidx.annotation.NonNull;

import net.fred.lua.App;
import net.fred.lua.common.utils.DateUtils;
import net.fred.lua.common.utils.StringUtils;
import net.fred.lua.common.utils.ThrowableUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

@Deprecated
public final class Logger implements AutoCloseable {

    private static Logger logger;
    /**
     * 0 for free, 1 for using.
     */
    private final AtomicInteger streamMutex;
    private OutputStream stream;

    private Logger() {
        stream = new ByteArrayOutputStream();
        streamMutex = new AtomicInteger(0);
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

    public static OutputStream stream() {
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
        Log.i("Logger", msg);
        final AtomicInteger streamMutex = getInstance().streamMutex;
        try {
            for (; ; ) {
                if (streamMutex.compareAndSet(0, 1)) {
                    stream().write(msg.getBytes(StandardCharsets.UTF_8));
                    streamMutex.set(0);
                    break;
                }
            }
        } catch (IOException e) {
            Log.e("logger", "Logger error " + ThrowableUtils.getThrowableMessage(e));
        }
    }

    public static void write(int i) {
        final AtomicInteger streamMutex = getInstance().streamMutex;
        try {
            for (; ; ) {
                if (streamMutex.compareAndSet(0, 1)) {
                    stream().write(i);
                    streamMutex.set(0);
                    break;
                }
            }
        } catch (IOException e) {
            Log.e("logger", "Logger error " + ThrowableUtils.getThrowableMessage(e));
        }
    }

    public static void writeLine(String msg) {
        write(msg + "\n");
    }

    @Override
    public void close() throws IOException {
        if (stream != null)
            stream.close();
    }

    /**
     * Called from @{link CacheDirectoryManager#compressLatestLogs}
     * This function transfers the data from ByteArrayOutputStream to the log file and changes the stream to FileOutputStream.
     */
    void onLogfilePrepared() throws IOException {
        for (; ; ) {
            if (streamMutex.compareAndSet(0, 1)) {
                byte[] saved = ((ByteArrayOutputStream) stream).toByteArray();
                stream = new FileOutputStream(LogFileManager.getInstance().getLoggerFile());
                stream.write(saved);

                streamMutex.set(0);
                break;
            }
        }
    }

}
