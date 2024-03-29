package net.fred.lua.common.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.fred.lua.App;
import net.fred.lua.R;
import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.utils.ClipboardUtils;
import net.fred.lua.common.utils.MathUtils;

public final class CrashActivity extends AppCompatActivity {
    private static final String TAG = "CrashActivity";
    private String content;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        long id = item.getItemId();
        if (id == android.R.string.copy) {
            ClipboardUtils.copy(this, content);
            Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.string.restart) {
            restart();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, android.R.string.copy, 0, android.R.string.copy)
                //.  setOnMenuItemClickListener(this)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, R.string.restart, 0, R.string.restart)
                //  .setOnMenuItemClickListener(this)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


        return true;
    }

    private void restart() {
        Log.i(TAG, "Restarting process");
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        }
        this.finish();
        // Exit crash notice process. Move to main application.
        App.forceKillSelf();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        this.content = getIntent().getStringExtra(CrashHandler.EXTRA_ERROR_CONTENT);
        initViews();
    }

    private void initViews() {
        ScrollView root = new ScrollView(this);
        root.setFillViewport(true);
        HorizontalScrollView hsv = new HorizontalScrollView(this);

        TextView tv = new TextView(this);
        int padding = MathUtils.dp2px(16);
        tv.setText(this.content);
        tv.setTextIsSelectable(true);
        tv.setPadding(padding, padding, padding, padding);

        hsv.addView(tv);
        root.addView(hsv, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setContentView(root);
    }

}
