package net.fred.lua.common.activity;

import android.content.IntentFilter;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import net.fred.lua.App;
import net.fred.lua.common.ExitReceiver;

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
