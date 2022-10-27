package net.fred.lua.common;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.fred.lua.App;
import net.fred.lua.common.activity.CrashActivity;
import net.fred.lua.common.utils.FileUtils;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String EXTRA_ERROR_CONTENT = "ErrorContent";
    
    
    public File errorSavePath;
    private Context ctx;
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private static CrashHandler instance;

    public void install(Context ctx) {
        this.ctx = ctx;
        this.defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        errorSavePath = ctx.getExternalFilesDir("errors");
    }

    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    private String getThrowableMessages(Throwable ta) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ta.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
    
    private void startCrashActivity(String content) {
        Intent intent = new Intent(this.ctx, CrashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
        | Intent.FLAG_ACTIVITY_CLEAR_TOP | 
        Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EXTRA_ERROR_CONTENT, content);
        ctx.startActivity(intent);
        App.forceKillSelf();
    }
    
    @Override
    public void uncaughtException(Thread p1, Throwable p2) {
        try {
            StringBuilder sb = new StringBuilder();

            String versionName = "Unknow";
            long versionCode = 0;
            try {
                PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
                versionName = packageInfo.packageName;
                versionCode = Build.VERSION.SDK_INT >= 28 ? 
                    packageInfo.getLongVersionCode() :
                    packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException ignored) {}

            String time = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date());

            sb.append("************* Crash Head ****************\n");
            sb.append("Time Of Crash      : ").append(time).append("\n");
            sb.append("Device Manufacturer: ").append(Build.MANUFACTURER).append("\n");
            sb.append("Device Model       : ").append(Build.MODEL).append("\n");
            sb.append("Android Version    : ").append(Build.VERSION.RELEASE).append("\n");
            sb.append("Android SDK        : ").append(Build.VERSION.SDK_INT).append("\n");
            sb.append("App VersionName    : ").append(versionName).append("\n");
            sb.append("App VersionCode    : ").append(versionCode).append("\n");
            sb.append("Crash Thread       : ").append(p1.getName()).append("\n");
            sb.append("************* Crash Head ****************\n\n");
            sb.append(getThrowableMessages(p2));

            FileUtils.writeFile(new File(errorSavePath, time + "-crash.log"), sb.toString());
            startCrashActivity(sb.toString());
        } catch (Throwable e) {
            if (this.defaultExceptionHandler != null)
                this.defaultExceptionHandler.uncaughtException(p1, p2);
        }
    }
}
