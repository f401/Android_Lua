package net.fred.lua.common;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;

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
