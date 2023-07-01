package net.fred.lua.common.utils;

import net.fred.lua.common.CrashHandler;

import java.io.Closeable;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ThrowableUtils {

    /**
     * Obtain exception information for throwable and call stack.
     *
     * @param th
     * @return The message
     */
    public static String getThrowableMessage(Throwable th) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        pw.close();
        return sw.toString();//StringWriter doesn't need close
    }

    public static void closes(Closeable... target) {
        for (Closeable c : target) {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
                CrashHandler.fastHandleException(e);
            }
        }
    }

    public static String getInvokerInfoString() {
        StackTraceElement info = // 0-1 system, 2 current, 3 supper
                Thread.currentThread().getStackTrace()[4];
        String fileName = info.getFileName();
        StringBuilder sb = new StringBuilder();
        sb.append("[")
                .append(info.getClassName())
                .append("] ").append(info.getMethodName())
                .append("(").append(fileName == null ? "unknown" : fileName)
                .append(": ").append(info.isNativeMethod() ? "native method" : info.getLineNumber())
                .append(")");
        return sb.toString();
    }
}
