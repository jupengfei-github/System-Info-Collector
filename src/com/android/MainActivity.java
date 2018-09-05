package com.android;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import com.android.content.SystemErrorGlobal;
import com.android.content.SystemErrorInfo;
import com.android.content.ResolveItemInfo;

public class MainActivity extends Activity {
    private Button btn   = null;
    private Button share = null;
    private ArrayList<String> mItems = new ArrayList<String>();

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);

        share = (Button)findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent(MainActivity.this, SystemErrorService.class);
                MainActivity.this.startService(intent);
            }
        });

        btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                SystemErrorGlobal global = SystemErrorGlobal.getInstance();
                SystemErrorInfo info = global.getSystemErrorInfo();

                ResolveItemInfo ri = info.resolvePackageErrorInfo("com.jpf");
                Log.d("jpf", info.toString());
                Log.d("jpf", ri.toString());
            }
        });
    }
}
