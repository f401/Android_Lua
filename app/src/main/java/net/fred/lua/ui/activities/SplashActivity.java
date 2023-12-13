package net.fred.lua.ui.activities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.fred.lua.App;
import net.fred.lua.R;
import net.fred.lua.common.PermissionHelper;
import net.fred.lua.common.TaskExecutor;
import net.fred.lua.common.activity.BaseActivity;
import net.fred.lua.foreign.Breakpad;
import net.fred.lua.io.LogFileManager;
import net.fred.lua.io.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {
    private PermissionHelper mPermissionHelper;

    private CountDownLatch counter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView tv = findViewById(R.id.activity_splash_tv);

        hideActionBar();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        tv.setWidth(metrics.widthPixels / 2);
        tv.setHeight(metrics.heightPixels / 2);



        TaskExecutor executor = new TaskExecutor.Builder()
                .addTask(new Runnable() {
                    @Override
                    public void run() {
                        if (App.isMainProcess()) {
                            LogFileManager.getInstance().compressLatestLogs();
                            Logger.i("Old logs compress finished!");
                            countDownTask();
                        }
                    }
                })
                .addTask(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleRWPermission();
                            }
                        });
                    }
                })
                .addTask(new Runnable() {
                    @Override
                    public void run() {
                        Breakpad.init(LogFileManager.getInstance().getNativeCrashDirectory().toString());
                        Logger.i("Breakpad already initialization.");
                        countDownTask();
                    }
                }).build();
        counter = new CountDownLatch(executor.getTotalTaskCount());
        // Must execute after initiation
        final ExecutorService service = executor.executeTasks();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    try {
                        counter.await();
                        Intent realMain = new Intent(SplashActivity.this, MainActivity.class);
						Logger.i("Launching MainActivity");
                        startActivity(realMain);
                        finish();
                        service.shutdownNow();
                        break;
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }, "Main Starter").start();
    }

    /**
     * Make counter decrease.
     * Called when the task is completed.
     */
    private void countDownTask() {
        counter.countDown();
    }

    private void hideActionBar() {
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
    }

    private void handleRWPermission() {
        this.mPermissionHelper = PermissionHelper.create(this, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);
        if (mPermissionHelper != null) {
            Logger.i("Trying request permission");
            mPermissionHelper.tryShowRequestDialog();
        } else {
            countDownTask();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mPermissionHelper != null &&
                mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)) {

            if (mPermissionHelper.hasCanRequestPermissions()) {
                // Here we will request twice.
                mPermissionHelper.tryShowRequestDialog();
            } else if (!mPermissionHelper.hasProhibitedPermissions()) {
                // Here all permissions are granted.
                // We can start
                countDownTask();
                return;
            }

            if (mPermissionHelper.hasProhibitedPermissions()) {
                Logger.w("Write or reading permission has been prohibited");
                AlertDialog alert = new AlertDialog.Builder(this)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface p1, int p2) {
                                mPermissionHelper.gotoSettingsActivity();
                                p1.dismiss();
                            }
                        })
                        .setTitle(R.string.warning)
                        .setMessage(R.string.request_permission_failed_content)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface p1, int p2) {
                                p1.dismiss();
                                countDownTask();
                            }
                        }).create();
                alert.show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionHelper.CODE_GOTO_SETTINGS) {
            countDownTask();
        }
    }
}
