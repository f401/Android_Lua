package net.fred.lua.common.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import androidx.core.util.Consumer;
import android.util.Log;

public class ThrowableUtils {
    
    private static final String TAG = "ThrowableUtils";

    /**
     * Obtain exception information for throwable and call stack.
     *
     * @param th Throwable what you want
     * @return The message
     */
    @NonNull
    public static String getThrowableMessage(Throwable th) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        pw.close();
        return sw.toString();//StringWriter doesn't need close
    }

    public static void closeAll(@Nullable AutoCloseable... target) {
        if (target != null) {
            for (AutoCloseable c : target) {
                try {
                    if (c != null) {
                        c.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error when releasing object "  + c, e);
                }
            }
        }
    }

    /**
     * Close all non empty objects in {@code t}.
     *
     * @param t      The collection of objects that need to be closed
     * @param action Actions to be performed before closing. If you don't need it, just pass it @{code null}.
     */
    public static void closeAll(@Nullable List<AutoCloseable> t, @Nullable Consumer<AutoCloseable> action) {
        if (t != null) {
            for (AutoCloseable c : t) {
                try {
                    if (c != null) {
                        if (action != null) {
                            action.accept(c);
                        }
                        c.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error when releasing object "  + c, e);
                }
            }
        }
    }

    @NonNull
    public static String getCallerString() {
        StackTraceElement info = // 0-1 system, 2 current, 3 upper
                Thread.currentThread().getStackTrace()[4];
        String fileName = info.getFileName();
        return "[" +
                info.getClassName() +
                "] " + info.getMethodName() +
                "(" + (fileName == null ? "unknown" : fileName) +
                ": " + (info.isNativeMethod() ? "native method" : info.getLineNumber()) +
                ")";
    }
}
