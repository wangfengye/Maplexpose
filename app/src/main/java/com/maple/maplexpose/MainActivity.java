package com.maple.maplexpose;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.location.demo.rpc.Ap;
import com.amap.location.demo.rpc.LocManager;
import com.maple.maplexpose.mqtt.MqttActivity;
import com.maple.maplexpose.util.FixLinesStr;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    public static TextView mTv;
    APList mAps;
    private Messenger mService;
    private FixLinesStr mFixLinesStr = new FixLinesStr(12);
    @SuppressLint("HandlerLeak")
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
                XSharedPreferenceUtil.setAps(mAps, MainActivity.this);
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
                XSharedPreferenceUtil.setAps(mAps, MainActivity.this);
                showText();
            }
        });
        findViewById(R.id.btn_loc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroad();//发送广播
            }
        });
        findViewById(R.id.btn_mqtt).setOnClickListener(v -> startActivity(new Intent(this, MqttActivity.class)));
    }

    /**
     * 发送请求定位的广播
     */
    private void sendBroad() {
        Intent intent = new Intent();
        intent.setAction("com.maple.maplexpose.loc");
        sendBroadcast(intent);
    }

    /**
     * Messenger 服务启动
     */
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
    }

    /**
     * Messenger 获取定位
     */
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


    /**
     * 启动aidl
     */
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

    /**
     * 发送aidl
     */
    private void sendWithAidl() {
        try {
            Log.i(TAG, "sendWithAidl: " + mLocManger.loc());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * log 打印当前wifi列表
     */
    private void showScanResults() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        manager.setWifiEnabled(true);
        manager.startScan();
        List<ScanResult> scanResults = manager.getScanResults();
        StringBuilder builder = new StringBuilder();
        for (ScanResult s : scanResults) {
            builder.setLength(0);
            builder.append(s.SSID
                    + ',' + s.BSSID
                    + ',' + s.level
                    + ',' + s.capabilities
                    + ',' + s.frequency
                    + '.' + s.channelWidth
                    + ',' + s.centerFreq0
                    + ',' + s.centerFreq1
                    + ',' + s.timestamp);
            Log.i(TAG, "current wifi: " + builder.toString());
        }

    }

    private void showText() {
        mFixLinesStr.clear();
        mTv.setText(mFixLinesStr.put(mAps.toString()));
    }

    @Override
    protected void onDestroy() {
        mTv = null;
        super.onDestroy();
    }
}
