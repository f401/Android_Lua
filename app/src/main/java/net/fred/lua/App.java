package net.fred.lua;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.Logger;
import net.fred.lua.io.LogScanner;

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
        PathConstants.init(this);
        LogScanner.getInstance().start();
        if (isMainProcess()) {
            CrashHandler.getInstance().install(this);
        }
        Log.i("Application", "redirecting stream");
        redirectOutAndErrStreamToLog();
    }

    /**
     * Obtain the process name in the best way.
     *
     * @return current process name.
     */
    @Nullable
    public String autoGetProcessName() {
        if (Build.VERSION.SDK_INT >= //Build.VERSION_CODES.P
                28) {
            return getProcessName();
        } else {
            int pid = android.os.Process.myPid();
            ActivityManager manager = (ActivityManager)
                    getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningApps = manager.getRunningAppProcesses();
            if (runningApps == null) return null;
            for (ActivityManager.RunningAppProcessInfo process : runningApps) {
                if (process.pid == pid) {
                    return process.processName;
                }
            }
            return null;
        }
    }

    /**
     * Check whether current is main process.
     *
     * @return For main process, true.
     */
    public boolean isMainProcess() {
        try {
            return getPackageName(). //prevent java.lang.NullPointerException
                    equals(autoGetProcessName());
        } catch (Exception e) {
            return false;
        }
    }
}
   
