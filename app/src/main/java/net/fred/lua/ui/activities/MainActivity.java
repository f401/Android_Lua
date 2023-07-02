package net.fred.lua.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.fred.lua.PathConstants;
import net.fred.lua.R;
import net.fred.lua.common.Logger;
import net.fred.lua.common.activity.BaseActivity;
import net.fred.lua.foreign.DynamicLoadingLibrary;

import java.nio.ByteBuffer;

public class MainActivity extends BaseActivity {
    private Button btn, throwException;
    private EditText editText;
    private DynamicLoadingLibrary luaDll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.activity_main_Button1);
        editText = (EditText) findViewById(R.id.activity_main_EditText1);
        throwException = (Button) findViewById(R.id.activity_main_throw);
        luaDll = DynamicLoadingLibrary.open(PathConstants.NATIVE_LIBRARY_DIR + "liblua.so");

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                System.out.println("Finish!");
            }
        });
        throwException.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                Logger.e("Making exception");
                throw new RuntimeException();
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        luaDll.close();
    }

}
