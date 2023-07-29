package net.fred.lua.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.fred.lua.PathConstants;
import net.fred.lua.R;
import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.Logger;
import net.fred.lua.common.activity.BaseActivity;
import net.fred.lua.common.utils.FileUtils;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.core.DynamicLoadingLibrary;
import net.fred.lua.io.CStandardOutputInput;
import net.fred.lua.lua.Lua5_4;

import java.util.Objects;

public class MainActivity extends BaseActivity {
    private Button btn, throwException, runCif;
    private EditText editText;
    private DynamicLoadingLibrary luaDll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.activity_main_Button1);
        editText = findViewById(R.id.activity_main_EditText1);
        throwException = findViewById(R.id.activity_main_throw);
        runCif = findViewById(R.id.activity_main_run_cif_btn);

        try {
            luaDll = DynamicLoadingLibrary.open(PathConstants.NATIVE_LIBRARY_DIR + "liblua.so");
        } catch (NativeMethodException e) {
            CrashHandler.fastHandleException(e, this);
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                System.out.println("Starting");
                try {
                    String text = editText.getText().toString();
                    Logger.i("Pointer of " + text + " at " + luaDll.lookupSymbol(text));
                } catch(Exception e) {
                    CrashHandler.fastHandleException(e);
                }
            }
        });

        throwException.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                Logger.e("Making exception");
                //throw new RuntimeException();
                try {
                    luaDll.close();
                } catch (NativeMethodException e) {
                    CrashHandler.fastHandleException(e, MainActivity.this);
                }
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
