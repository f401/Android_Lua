package net.fred.lua;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDex;

import net.fred.lua.common.CrashHandler;
import net.fred.lua.io.LogFileManager;
import net.fred.lua.io.LogScanner;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class App extends Application {
    public static final String EXIT_ACTION = "net.lua.exit.all";
    
    private static final String TAG = "APP";
    private static App instance;
    private static ThreadPoolExecutor threadPool;

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


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        int cpus = Runtime.getRuntime().availableProcessors();
        threadPool = new ThreadPoolExecutor(cpus / 2, cpus * 2, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(cpus));

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().permitDiskReads().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        
        if (App.isMainProcess()) {
            LogScanner.cleanBuffer();
        }

        PathConstants.init(this);
        LogFileManager.install(this);
        Log.i(TAG, "Cache directory manager already installed.");

        if (App.isMainProcess()) {
            CrashHandler.getInstance().install(this);
        }
        LogScanner.getInstance().start();
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
    
    public static ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }
    
}
   
