package net.fred.lua.common.activity;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import net.fred.lua.App;

public class BaseActivity extends AppCompatActivity {
	
    private final ExitReceiver receiver = new ExitReceiver(this);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(App.EXIT_ACTION);
        registerReceiver(this.receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.receiver);
    }
    
}
