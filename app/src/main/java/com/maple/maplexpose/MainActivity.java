package com.maple.maplexpose;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static TextView mTv;
    APList mAps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv = findViewById(R.id.log);
        final EditText etBssid = findViewById(R.id.et_bssid);
        final EditText etSsid = findViewById(R.id.et_ssid);
        final EditText etLevel = findViewById(R.id.et_level);
        mAps=XSharedPreferenceUtil.getJson(this,"APS",APList.class);
        if (mAps==null)mAps=new APList();showText();
        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ap ap = new Ap();
                String bssid = etBssid.getText().toString();
                String ssid = etSsid.getText().toString();
                String levelStr = etLevel.getText().toString();
                if (bssid.isEmpty() || ssid.isEmpty() || levelStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "数据未填充完整", Toast.LENGTH_SHORT).show();
                    return;
                }
                ap.setBssid(bssid);
                ap.setSsid(ssid);
                ap.setLevel(-Integer.parseInt(levelStr));
                mAps.add(ap);
                XSharedPreferenceUtil.setJson(MainActivity.this, "APS", mAps);
                etBssid.getText().clear();
                etSsid.getText().clear();
                etLevel.getText().clear();
                showText();
            }
        });
        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAps.clear();
                XSharedPreferenceUtil.setJson(MainActivity.this,"APS",mAps);
                showText();
            }
        });
    }

    private void showText() {

        mTv.setText(mAps.toString());
    }

    @Override
    protected void onDestroy() {
        mTv = null;
        super.onDestroy();
    }
}
