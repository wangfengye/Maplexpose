package com.maple.maplexpose;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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

import com.alibaba.fastjson.JSON;
import com.amap.location.demo.rpc.Ap;
import com.amap.location.demo.rpc.LocManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public static TextView mTv;
    APList mAps;
    private Messenger mService;
    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage: " + msg.getData().get("loc"));
            super.handleMessage(msg);
        }
    });


    private LocManager mLocManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv = findViewById(R.id.log);
        final EditText etBssid = findViewById(R.id.et_bssid);
        final EditText etSsid = findViewById(R.id.et_ssid);
        final EditText etLevel = findViewById(R.id.et_level);

        mAps = XSharedPreferenceUtil.getAps(this);
        if (mAps == null) mAps = new APList();
        showText();
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
                XSharedPreferenceUtil.setAps( mAps,MainActivity.this);
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
                XSharedPreferenceUtil.setAps(mAps,MainActivity.this);
                showText();
            }
        });
        findViewById(R.id.btn_loc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sendBroad();
                // sendWithMEssenger();
                //sendWithAidl();
                startLocService();
            }
        });
        findViewById(R.id.btn_loc_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroad();
            }
        });
//        bindService();
    }

    private void startLocService() {
        Intent intent = new Intent(this, LocService.class);
        startService(intent);
    }

    private void sendBroad() {
        Intent intent = new Intent();
        intent.setAction("com.maple.maplexpose.loc");
        sendBroadcast(intent);
    }

    private void showText() {
        mTv.setText(mAps.toString());
    }

    private void bindService() {
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = new Messenger(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }
        };
        Intent intent = new Intent();
        intent.setAction("android.intent.action.HOOKMESSENGER");
        intent.setPackage("com.amap.location.demo");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        bindAidl();
    }

    private void sendWithMEssenger() {
        Message msgFromClient = new Message();
        msgFromClient.what = 1;
        msgFromClient.replyTo = mMessenger;
        try {
            mService.send(msgFromClient);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        mTv = null;

        super.onDestroy();
    }

    private void bindAidl() {
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mLocManger = LocManager.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mLocManger = null;
            }
        };
        Intent intent = new Intent();
        intent.setAction("com.maple.aidl");
        intent.setPackage("com.amap.location.demo");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void sendWithAidl() {
        try {
            Log.i(TAG, "sendWithAidl: " + mLocManger.loc());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


}
