package net.fred.lua.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.fred.lua.PathConstants;
import net.fred.lua.R;
import net.fred.lua.common.CrashHandler;
import net.fred.lua.common.Logger;
import net.fred.lua.common.activity.BaseActivity;
import net.fred.lua.foreign.NativeMethodException;
import net.fred.lua.foreign.core.DynamicLoadingLibrary;

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

        try {
            luaDll = DynamicLoadingLibrary.open(PathConstants.NATIVE_LIBRARY_DIR + "liblua.so");
        } catch (NativeMethodException e) {
            throw new RuntimeException(e);
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
                luaDll.close();
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // try {
            //luaDll.close();
       // } catch (Exception e) {
          //  CrashHandler.fastHandleException(e);
      //  }
    }

}
