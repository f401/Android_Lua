package net.fred.lua.ui.activities;

import net.fred.lua.common.activity.BaseActivity;
import net.fred.lua.R;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import net.fred.lua.common.lua.LuaJavaBridge;

public class MainActivity extends BaseActivity {

    private Button btn;
    private EditText editText;
    private LuaJavaBridge bridge;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button)findViewById(R.id.activity_main_Button1);
        editText = (EditText)findViewById(R.id.activity_main_EditText1);
        bridge = new LuaJavaBridge();
        
        btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View p1) {
                    bridge.dofile(editText.getText().toString());
                    System.out.println("Finish!");
                    System.out.flush();
                    System.err.flush();
                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bridge.close();
    }
    
}
