package net.fred.lua.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;

public class ExitReceiver extends BroadcastReceiver {
    
	private Activity activity;

	public ExitReceiver(Activity activity) {
		this.activity = activity;
	}
	
    @Override
    public void onReceive(Context context, Intent intent) {
        this.activity.finish();
    }
    
}
