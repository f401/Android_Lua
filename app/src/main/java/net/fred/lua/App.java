package net.fred.lua;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import androidx.multidex.MultiDex;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.Logger;

public class App extends Application {
	
	public static final String EXIT_ACTION = "net.lua.exit.all";
    private static App instance;
    
    public static App getInstance() {
        return instance;
    }
    
    public static void killSelf(Context ctx) {
		Intent intent = new Intent("net.fred.lua.common.activity.BaseActivity");
		intent.putExtra(EXIT_ACTION, 1);
        ctx.sendBroadcast(intent);
	}
    
    public static void forceKillSelf() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
        
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (isMainProcess()) {
            CrashHandler.getInstance().install(this);
            //CrashHandler.getInstance().showError(false);
        }
        Logger.i("outputstream");
        redirectOutAndErrStream(getExternalCacheDir() + "/out.log", getExternalCacheDir() + "/err.log");
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
    
    public String getProcessName() {
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
    
    public boolean isMainProcess() {
        try {
            return getPackageName(). //prevent java.lang.NullPointerException
            equals(getProcessName());
        } catch (Exception e) {
            return false;
        }
    }
    
}
   
