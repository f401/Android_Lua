package net.fred.lua.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.fred.lua.App;
import net.fred.lua.R;
import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.activity.BaseActivity;
import net.fred.lua.foreign.Breakpad;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.allocator.DefaultAllocator;
import net.fred.lua.foreign.scoped.IScopedResource;
import net.fred.lua.foreign.scoped.ScopeFactory;
import net.fred.lua.io.CStandardOutputInput;
import net.fred.lua.io.LogFileManager;
import net.fred.lua.lua.Lua54LibraryProxy;
import net.fred.lua.lua.Lua5_4;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
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
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Lua54LibraryProxy proxy = Lua54LibraryProxy.create();
                            CStandardOutputInput.getInstance().
                                    redirectStandardOutTo(
                                            getExternalCacheDir() + "/lua_out.txt"
                                    );
                            proxy.openlibs();
                            proxy.dofile(
                                    getExternalFilesDir("") + "/l.lua");
                            proxy.close();
                        } catch (NativeMethodException exception) {
                            throw new RuntimeException(exception);
                        }
                    }
                }.start();
            }
        });

        throwException.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                Log.e(TAG, "Making exception");
                //throw new RuntimeException();
                Breakpad.SEND_SIGNAL_SEGV();
            }

        });

        runCif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Running cif");
                try {
                    IScopedResource scope = ScopeFactory.ofManual(DefaultAllocator.INSTANCE);
                    Lua5_4 lua54 = new Lua5_4(scope);
                    CStandardOutputInput.getInstance().redirectStandardOutTo(
                            getExternalCacheDir() + "/lua_out.txt"
                    );
                    lua54.openlibs();
                    lua54.dofile("/sdcard/l.lua");
                    lua54.close();
                    scope.close();
                } catch (Throwable e) {
                    CrashHandler.fastHandleException(e, MainActivity.this);
                }
            }
        });

        findViewById(R.id.activity_main_start_view_test).setOnClickListener(new View.OnClickListener() {
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
            App.getThreadPool().submit(new Runnable() {

                @Override
                public void run() {
                    Log.i(TAG, "Removing cache directory.");
                    final String size = LogFileManager.getInstance().sizeOfDirectoryString();
                    LogFileManager.getInstance().delete();
                    runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, getString(R.string.cache_directory_size,
                                    size), Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            });
            
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
