package net.fred.lua.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class PermissionHelper {
    public static final int CODE_PERMISSION_REQUEST = 0x3838;
    public static final int CODE_GOTO_SETTINGS = 0x828;
    private final ArrayList<String> canRequest, prohibited;
    private final Activity ctx;

    private PermissionHelper(Activity ctx, ArrayList<String> notAllowed) {
        this.ctx = ctx;
        this.canRequest = new ArrayList<>();
        this.prohibited = new ArrayList<>();

        setProhibitedAndRequestList(notAllowed);
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static ArrayList<String> getNotAllowedPermissionList(Context context, String[] permissions) {
        ArrayList<String> notAllowed = new ArrayList<>();
        for (String curr : permissions) {
            if (!hasPermission(context, curr)) {
                notAllowed.add(curr);
            }
        }
        return notAllowed;
    }

    public static ArrayList<String> getNotAllowedPermissionList(String[] permissions, int[] grantResults) {
        ArrayList<String> notAllowed = new ArrayList<>();
        if (permissions.length != 0 && grantResults.length != 0) {
            for (int i = 0; i < grantResults.length; ++i) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    notAllowed.add(permissions[i]);
                }
            }
        }
        return notAllowed;
    }

    /**
     * 创建一个 {@code PermissionHelper}
     *
     * @param permissions 需要申请的权限.
     * @return 如果为空，则表示传入的权限当前应用已经全部被授权.
     */
    public static PermissionHelper create(Activity context, String... permissions) {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> notAllowed = getNotAllowedPermissionList(context, permissions);
            return notAllowed.size() == 0 ? null : new PermissionHelper(context, notAllowed);
        }
        return null;
    }

    private void setProhibitedAndRequestList(ArrayList<String> notAllowed) {
        canRequest.clear();
        prohibited.clear();

        for (String curr : notAllowed) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ctx, curr)) {
                canRequest.add(curr);
            } else {
                prohibited.add(curr);
            }
        }
    }

    public void gotoSettingsActivity() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", ctx.getPackageName(), null));
        ctx.startActivityForResult(intent, CODE_GOTO_SETTINGS);
    }

    public void tryShowRequestDialog() {
        ActivityCompat.requestPermissions(ctx, canRequest.toArray(new String[canRequest.size()]), CODE_PERMISSION_REQUEST);
    }

    public boolean hasCanRequestPermissions() {
        return canRequest.size() != 0;
    }

    public boolean hasProhibitedPermissions() {
        return prohibited.size() != 0;
    }

    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CODE_PERMISSION_REQUEST) {
            canRequest.clear();
            prohibited.clear();
            ArrayList<String> notAllowed = getNotAllowedPermissionList(permissions, grantResults);
            setProhibitedAndRequestList(notAllowed);
            return true;
        }
        return false;
    }

}
