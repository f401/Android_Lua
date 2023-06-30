package net.fred.lua;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PermissionInfo;
import android.os.Build;
import android.util.Log;
import androidx.multidex.MultiDex;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import net.fred.lua.common.CrashHandler;
import net.fred.lua.io.LogScanner;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class App extends Application {
	
    public static final String EXIT_ACTION = "net.lua.exit.all";
    private static App instance;
    
    public static App getInstance() {
        return instance;
    }

    /**
     * Exit app by broadcast
     * @param ctx required
     */
    public static void killSelf(Context ctx) {
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
    public void onCreate() {
        super.onCreate();
        instance = this;
	    PathConstants.init(this);
	    LogScanner.getInstance().start();
        if (isMainProcess()) {
            CrashHandler.getInstance().install(this);
        }
        Log.i("Application", "redirecting stream");
        redirectOutAndErrStream(PathConstants.STDOUT, PathConstants.STDERR);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    
    public static void redirectOutAndErrStream(String out, String err) {
        try {
            PrintStream outStream = new PrintStream(new File(out));
            PrintStream errStream = new PrintStream(new File(err));
            System.setOut(outStream);
            System.setErr(errStream);
        } catch (IOException e) {
            CrashHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        }
    }

    /**
     * Get process name in the best way.
     * @return current process name.
     */
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
   
