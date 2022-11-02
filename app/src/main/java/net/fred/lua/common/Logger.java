package net.fred.lua.common;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import java.io.File;
import java.util.Date;
import net.fred.lua.App;
import android.content.Context;
import java.io.IOException;
import java.io.PrintStream;

public class Logger implements AutoCloseable {

    private static Logger logger;
    private final File logFile;
    private PrintStream stream;
    
    private Logger() {
        String fileName = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date())
        + ".log";
        
        logFile = new File(App.getInstance()
        .getExternalFilesDir("logs") + File.separator + fileName);
        
        if (logFile.exists()) {
            logFile.delete();
        }
        
        try {
            logFile.createNewFile();
            stream = new PrintStream(logFile);
        } catch (IOException e) {
            CrashHandler.getInstance().uncaughtException(
            Thread.currentThread(), e);
        }
    }
    
    public static Logger getInstance() {
        if (logger == null) {
            synchronized(Logger.class) {
                if (logger == null ) {
                    logger = new Logger();
                }
            }
        }
        return logger;
    }
    
    public static PrintStream stream() {return getInstance().stream;}
    
    public static void i(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("INFO ").append(getOtherInfos()).append(getInvokerClassInfo())
        .append(" :").append(msg);
        write(sb.toString());
    }
    
    public static void e(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("ERROR ").append(getOtherInfos()).append(getInvokerClassInfo())
            .append(" :").append(msg);
        write(sb.toString());
    }
    
    public static void w(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("WARN ").append(getOtherInfos()).append(getInvokerClassInfo())
            .append(" :").append(msg);
        write(sb.toString());
    }
    
    private static String getInvokerClassInfo() {
        StackTraceElement info = //0-1 system 2 current, 3 上一级(Logger#i/e)
            Thread.currentThread().getStackTrace()[4];
        String fileName = info.getFileName();
        StringBuilder sb = new StringBuilder();
        sb.append("[")
            .append(info.getClassName())
            .append("] ").append(info.getMethodName())
            .append("(").append(fileName == null ? "unknow" : fileName)
            .append(": ").append(info.isNativeMethod() ? "native method" : info.getLineNumber())
            .append(")");
        return sb.toString();
    }

    private static String getOtherInfos() {
        StringBuilder sb = new StringBuilder();
        sb.append(new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date()))
            .append(" ").append(Thread.currentThread().getName())
            .append(" ").append(Thread.currentThread().getId())
            .append("/").append(android.os.Process.myPid());
        App instance = App.getInstance();
        sb.append("(").append(instance != null ? instance.getPackageName():"Unknow")
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
