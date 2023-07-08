package net.fred.lua.ui.activities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.fred.lua.R;
import net.fred.lua.common.Flag;
import net.fred.lua.common.activity.BaseActivity;
import net.fred.lua.common.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends BaseActivity {

    public static final int PERMISSION_REQUEST_CODE = 10101;
    public static final int GOTO_SETTINGS_ACTIVITY = 368;
    public static final int START_TIME = 2000;

    private TextView tv;
    private Handler startMainHandler;
    private final Flag isPermissionRequestFinished = new Flag(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        tv = findViewById(R.id.activity_splash_tv);
        startMainHandler = new Handler();

        hideActionBar();

        tv.setHeight(MathUtils.dp2px(getWindowManager().getDefaultDisplay().getWidth() / 2));
        tv.setWidth(MathUtils.dp2px(getWindowManager().getDefaultDisplay().getHeight() / 2));

        handleRWPermission();
        startMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPermissionRequestFinished.getFlag()) {
                    startMainHandler.removeCallbacks(this);
                    Intent realMain = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(realMain);
                    finish();
                } else {
                    startMainHandler.postDelayed(this, START_TIME);
                }
            }
        }, START_TIME);
    }

    private void hideActionBar() {
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
    }

    private boolean hasRWPermission() {
        int r = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
        int w = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        return r == PackageManager.PERMISSION_GRANTED && w == PackageManager.PERMISSION_GRANTED;
    }

    private void handleRWPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !hasRWPermission()) {
            requestRWPermission();
        } else {
            isPermissionRequestFinished.setFlag(true);
        }
    }

    private void gotoSettingsActivity() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivityForResult(intent, GOTO_SETTINGS_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (GOTO_SETTINGS_ACTIVITY == requestCode)
            isPermissionRequestFinished.setFlag(true);
    }

    private List<String> getNotAllowedPermissionList(String[] permissions, int[] grantResults) {
        List<String> notAllowed = new ArrayList<>();
        if (permissions.length != 0 && grantResults.length != 0) {
            for (int i = 0; i < grantResults.length; ++i) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    notAllowed.add(permissions[i]);
                }
            }
        }
        return notAllowed;
    }

    private void requestRWPermission() {
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> notAllowed;
        if (requestCode == PERMISSION_REQUEST_CODE && (notAllowed =
                getNotAllowedPermissionList(permissions, grantResults))
                .size() != 0) {
            List<String> ban = new ArrayList<>();
            List<String> can = new ArrayList<>();
            for (String permission : notAllowed) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                    can.add(permission);
                else
                    ban.add(permission);
            }
            if (can.size() != 0)
                ActivityCompat.requestPermissions(this, can.toArray(new String[can.size()]), PERMISSION_REQUEST_CODE);
            if (ban.size() != 0) {
                AlertDialog alert = new AlertDialog.Builder(this)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface p1, int p2) {
                                gotoSettingsActivity();
                                p1.dismiss();
                            }
                        })
                        .setTitle(R.string.warning)
                        .setMessage(R.string.request_permission_failed_content)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface p1, int p2) {
                                p1.dismiss();
                                isPermissionRequestFinished.setFlag(true);
                            }
                        }).create();
                alert.show();
            }
        } else {
            isPermissionRequestFinished.setFlag(true);
        }
    }
}
