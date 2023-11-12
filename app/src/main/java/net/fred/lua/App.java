package net.fred.lua;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import net.fred.lua.common.CrashHandler;
import net.fred.lua.io.LogFileManager;
import net.fred.lua.io.LogScanner;
import net.fred.lua.io.Logger;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

public class App extends Application {

    public static final String EXIT_ACTION = "net.lua.exit.all";
    private static App instance;

    public static App getInstance() {
        return instance;
    }

    /**
     * Exit app by broadcast
     *
     * @param ctx required
     */
    public static void killSelf(@NonNull Context ctx) {
        Intent intent = new Intent("net.fred.lua.common.activity.BaseActivity");
        intent.putExtra(EXIT_ACTION, 1);
        ctx.sendBroadcast(intent);
    }

    /**
     * Exit app by kill self.
     */
    public static void forceKillSelf() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private static void redirectOutAndErrStreamToLog() {
        class Injector extends PrintStream {
            public Injector(final String name) {
                super(new OutputStream() {
                    boolean printHeader = true;

                    @Override
                    public void write(int b) {
                        if (printHeader) {
                            Logger.write("[" + name + "]");
                            printHeader = false;
                        } else if (b == '\n') {
                            printHeader = true;
                        }
                        Logger.write(b);
                    }
                });
            }
        }
        System.setErr(new Injector("STDERR"));
        System.setOut(new Injector("STDOUT"));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        LogScanner.cleanBuffer();

        PathConstants.init(this);
        LogFileManager.install(this);
        Logger.i("Cache directory manager already installed.");

        if (App.isMainProcess()) {
            CrashHandler.getInstance().install(this);
        }

        Logger.i("Starting logger scanner");
        LogScanner.getInstance().start();

        redirectOutAndErrStreamToLog();
        // More work is in ui.activities.SplashActivity
    }

    /**
     * Obtain the process name in the best way.
     *
     * @return current process name.
     */
    @Nullable
    public static String getCurrentProcessName() {
        if (Build.VERSION.SDK_INT >= //Build.VERSION_CODES.P
                28) {
            return getProcessName();
        }
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager)
                instance.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = manager.getRunningAppProcesses();
        if (runningApps == null) return null;
        for (ActivityManager.RunningAppProcessInfo process : runningApps) {
            if (process.pid == pid) {
                return process.processName;
            }
        }
        return null;
    }

    /**
     * Check whether current is main process.
     *
     * @return For main process, true.
     */
    public static boolean isMainProcess() {
        try {
            return instance.getPackageName(). //prevent java.lang.NullPointerException
                    equals(getCurrentProcessName());
        } catch (Exception e) {
            return false;
        }
    }
}
   
