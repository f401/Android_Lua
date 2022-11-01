package net.fred.lua;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import net.fred.lua.common.CrashHandler;

public class App extends Application {
	
	public static final String EXIT_ACTION = "net.lua.exit.all"; 
    
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
        CrashHandler.getInstance().install(this);
        CrashHandler.getInstance().showError(false);
        redirectOutAndErrStream(getExternalCacheDir() + "/out.log", getExternalCacheDir() + "/err.log");
    }
    
    public void redirectOutAndErrStream(String out, String err) {
        try {
            PrintStream outStream = new PrintStream(new File(out));
            PrintStream errStream = new PrintStream(new File(err));
            System.setOut(outStream);
            System.setErr(errStream);
        } catch (IOException e) {
            CrashHandler.getInstance().uncaughtException(Thread.currentThread(), e);
        }
    }

    
   }
   
