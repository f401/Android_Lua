package net.fred.lua.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.fred.lua.R;
import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.Logger;
import net.fred.lua.common.activity.BaseActivity;
import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.foreign.Breakpad;
import net.fred.lua.io.CStandardOutputInput;
import net.fred.lua.lua.Lua5_4;

import java.util.Objects;

public class MainActivity extends BaseActivity {
    private Button btn, throwException, runCif;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.activity_main_Button1);
        editText = findViewById(R.id.activity_main_EditText1);
        throwException = findViewById(R.id.activity_main_throw);
        runCif = findViewById(R.id.activity_main_run_cif_btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {

            }
        });

        throwException.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                Logger.e("Making exception");
                //throw new RuntimeException();
                Breakpad.SEND_SIGNAL_SEGV();
            }

        });

        runCif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i("Running cif");
                try {
                    Lua5_4 lua54 = new Lua5_4();
                    CStandardOutputInput.getInstance().redirectStandardOutTo(
                            getExternalCacheDir() + "/lua_out.txt"
                    );
                    lua54.openlibs();
                    lua54.dofile("/sdcard/l.lua");
                    lua54.close();
                } catch (Throwable e) {
                    CrashHandler.fastHandleException(e, MainActivity.this);
                }
            }
        });

        ((Button) findViewById(R.id.activity_main_start_view_test)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ViewTestActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.activity_main_menu_clean_cache) {
            Toast.makeText(this, getString(R.string.cache_directory_size,
                    FileUtils.shrinkToBestDisplay(
                            FileUtils.evalDirectoryTotalSize(
                                    Objects.requireNonNull(getExternalCacheDir())))), Toast.LENGTH_SHORT).show();
            Logger.i("Removing cache directory.");
            FileUtils.removeDirectory(getExternalCacheDir());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
