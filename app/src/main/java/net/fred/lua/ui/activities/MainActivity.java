package net.fred.lua.ui.activities;

import net.fred.lua.common.activity.BaseActivity;
import net.fred.lua.R;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import net.fred.lua.jni.lua.LuaJavaBridge;
import net.fred.lua.jni.CStandardOutputInput;

public class MainActivity extends BaseActivity {
    private Button btn;
    private EditText editText;
    private LuaJavaBridge bridge;
    private CStandardOutputInput cstdio;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button)findViewById(R.id.activity_main_Button1);
        editText = (EditText)findViewById(R.id.activity_main_EditText1);
        bridge = new LuaJavaBridge();
        cstdio = CStandardOutputInput.getInstance();
        
        btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View p1) {
                    cstdio.redirectStandardOutTo(MainActivity.this.getExternalFilesDir("") + "/luaout.txt");
                    cstdio.redirectStandardInTo(MainActivity.this.getExternalFilesDir("") + "/luain.txt");
                    bridge.dofile(editText.getText().toString());
                    System.out.println("Finish!");
                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bridge.close();
    }
    
}
