package net.fred.lua.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.App;
import net.fred.lua.R;
import net.fred.lua.common.activity.CrashActivity;
import net.fred.lua.common.utils.DateUtils;
import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.common.utils.ThrowableUtils;
import net.fred.lua.io.LogFileManager;

import java.io.File;

public final class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    public static final String EXTRA_ERROR_CONTENT = "ErrorContent";
    private static CrashHandler instance;

    public File crashFile;
    private volatile Context ctx;
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private boolean showError;

    @NonNull
    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    public static void fastHandleException(@NonNull Throwable exception) {
        fastHandleException(exception, null);
    }

    /**
     * Usually use in try-catch
     *
     * @param exception The exception you want to deal.
     * @param ctx       If present, a dialog box will be used to remind users instead of creating a new activity. If you need to create a new activity, pass it null.
     *                  Also see @{see #fastHandleException(Throwable)}
     */
    public static void fastHandleException(@NonNull Throwable exception, @Nullable Context ctx) {
        StringBuilder sb = getInstance().writeInfoToSdCard(Thread.currentThread(), exception);
        if (ctx != null) {
            try {
                new AlertDialog.Builder(ctx)
                        .setTitle(ctx.getString(R.string.unknown_exception_happened))
                        .setMessage(sb.toString())
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                return;
            } catch (Throwable e) {
                Log.e(TAG, "Exception when building dialog.", e);
            }
        }
        getInstance().uncaughtException(Thread.currentThread(), exception);
    }

    public void install(@NonNull Context ctx) {
        if (this.ctx == null) {
            synchronized (this) {
                if (this.ctx == null) {
                    this.ctx = ctx;
                    this.defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
                    Thread.setDefaultUncaughtExceptionHandler(this);
                    crashFile = LogFileManager.getInstance().getCrashFile();
                    showError = true;
                    Log.i(TAG, "Crash handler installed in package: " + ctx.getPackageName());
                }
            }
        }
    }

    private void startCrashActivity(String content) {
        Log.i(TAG, "Launching crash activity");
        Intent intent = new Intent(this.ctx, CrashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EXTRA_ERROR_CONTENT, content);
        ctx.startActivity(intent);
    }

    /**
     * @return Information writes to sdcard.
     */
    @NonNull
    private StringBuilder writeInfoToSdCard(Thread p1, Throwable p2) {
        Log.i(TAG, "Obtain Messages");
        StringBuilder sb = new StringBuilder();
        String versionName = "Unknown";
        long versionCode = 0;
        if (ctx != null) { //可能在没有install时调用
            try {
                PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
                versionName = packageInfo.packageName;
                versionCode = Build.VERSION.SDK_INT >= 28 ?
                        packageInfo.getLongVersionCode() :
                        packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        String time = DateUtils.getCurrentTimeString();

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
        sb.append(ThrowableUtils.getThrowableMessage(p2));

        FileUtils.writeFile(crashFile, sb.toString(), true);
        return sb;
    }

    @Override
    public void uncaughtException(@NonNull Thread p1, @NonNull Throwable p2) {
        try {
            Log.e(TAG, "------------------Making Crash----------------");
            StringBuilder sb = writeInfoToSdCard(p1, p2);
            if (showError && ctx != null) {
                Log.i(TAG, "Starting a new activity");
                startCrashActivity(sb.toString());
                showError = false;
            }
            Log.i(TAG, "Killing self");
            // Kill crashed process.
            App.forceKillSelf();
        } catch (Throwable e) {
            Log.e(TAG, "CrashHandler cannot deal with exception, spreading to default handler.");
            if (this.defaultExceptionHandler != null) {
                this.defaultExceptionHandler.uncaughtException(p1, p2);
            } else {
                Log.e(TAG, "Cannot find default exception handler, exiting...");
                App.forceKillSelf();
            }
        }
    }
}
