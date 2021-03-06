package com.maple.maplexpose;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.maple.maplexpose.mqtt.MqttApiImpl;
import com.maple.maplexpose.util.FixLinesStr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TODO: 瑕疵,IntentService不建议使用bind
 */
public class StartActivity extends AppCompatActivity {
    public static final String TAG = "START";
    private int count = 0;
    ServiceConnection mServiceConnection;
    private LocService mService;
    private Button mBtnLoc;
    private TextView mTvContent;
    private TextView mTvCounter;
    private boolean isRunning;
    private static final int MAX_LINES = 24;
    private int mLines = 0;
    private FixLinesStr mFixLinesStr = new FixLinesStr();
    private TextView mTvStarTime;
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mBtnLoc = findViewById(R.id.btn_loc);
        mTvContent = findViewById(R.id.tv_content);
        mTvCounter = findViewById(R.id.tv_count);
        mTvStarTime = findViewById(R.id.tv_start);
        mTvStarTime.setText(format.format(new Date(MainApp.time)));
        // 初始化前缀
        SharedPreferences sharedPreferences = this.getSharedPreferences("a", MODE_PRIVATE);
        MqttApiImpl.setTopicPrefix(sharedPreferences.getString("prefix", ""));
        findViewById(R.id.btn_debug).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(TAG, "onServiceConnected: ");
                mService = ((LocService.LocBinder) service).getService();
                mService.setActionListener(new LocService.HandleListener() {//正常解绑不会触发
                    @Override
                    public void onAction(String data) {
                        runOnUiThread(() -> {
                            mTvContent.setText(mFixLinesStr.put(data));
                        });
                    }

                    @SuppressLint("ResourceAsColor")
                    @Override
                    public MqttApiImpl.ListenerState getMqttState() {
                        return i -> mTvContent.setBackgroundResource(i == 1 ? R.color.white : R.color.red);


                    }

                    @Override
                    public void onFinished(final  int cacheSize,final int errlog) {
                        runOnUiThread(() -> {
                            count++;
                            mTvCounter.setText(MqttApiImpl.getMac() + "已完成 " + count + " 个任务,缓存任务数:"+cacheSize+" errlog: "+errlog);
                        });

                    }
                });

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                Log.i(TAG, "onServiceDisconnected: ");
            }
        };
        findViewById(R.id.btn_loc).setOnClickListener(v -> {
            if (isRunning) {
                isRunning = false;
                if (mService != null) {
                    mService.mRunning = false;
                    mService.stopSelf();
                }
                mBtnLoc.setText("已暂停");
            } else {
                Intent intent = new Intent(this, LocService.class);
                bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
                startService(intent);
                isRunning = true;
                mBtnLoc.setText("运行中");
            }
        });
        findViewById(R.id.btn_preFix).setOnClickListener(v -> {
            startActivity(new Intent(StartActivity.this, DialogActivity.class));
        });
        // 测试代码
        //    new MqttTest().init(this,"tcp://192.168.168.149:1883");
    }


    @Override
    protected void onDestroy() {
        if (mService != null) {
            mService.setActionListener(null);
            mService.mRunning = false;
            mService = null;
            unbindService(mServiceConnection);
        }
        super.onDestroy();
    }

}
