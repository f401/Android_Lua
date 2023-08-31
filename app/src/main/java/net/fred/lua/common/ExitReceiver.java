package net.fred.lua.common;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class ExitReceiver extends BroadcastReceiver {

    private final Activity activity;

    public ExitReceiver(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.activity.finish();
    }

}
