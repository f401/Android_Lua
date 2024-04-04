package net.fred.lua.common;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;

/**
 * A class specifically designed to accept outgoing broadcasts.
 * Used in {@link net.fred.lua.common.activity.BaseActivity}
 */
public final class ExitReceiver extends BroadcastReceiver {

    private final WeakReference<Activity> activity;

    public ExitReceiver(Activity activity) {
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Activity target = activity.get();
        if (target != null) {
            target.finish();
        }
    }

}
