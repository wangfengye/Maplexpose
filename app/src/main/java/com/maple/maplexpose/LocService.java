package com.maple.maplexpose;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.amap.location.demo.rpc.Ap;
import com.amap.location.demo.rpc.LocManager;
import com.maple.maplexpose.mqtt.MqttApiImpl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**

 */
public class LocService extends IntentService {
    public static final String TAG = "LocService";
    public static final int TYPE_DEBUG = 1;
    //private Api mApi = RetrofitFactory.create().baseUrl("http://192.168.168.175:8865").build().create(Api.class);
    public static Api mApi;
    private LocManager mLocManger;
    private HandleListener mListener;
    public boolean mRunning = true;

    public LocService() {
        super("LocService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    IBinder mBinder = new LocBinder();

    public class LocBinder extends Binder {
        LocService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    void setActionListener(HandleListener listener) {
        mListener = listener;
    }

    public interface HandleListener {
        //返回执行日志
        void onAction(String data);

        //一个任务已完成
        void onFinished();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        mRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // 获取任务
        // 修改 scanResults;
        // 执行定位
        // 上报定位结果
        //private Api mApi = RetrofitFactory.create().baseUrl("http://192.168.168.175:8865").build().create(Api.class);
        if (mApi == null)
            mApi = new MqttApiImpl().init(getApplicationContext(), "tcp://192.168.168.149:1883");
        long time = 0;
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        for (; ; ) {
            if (!mRunning) break;
            time = System.currentTimeMillis();

            try {
                APList data = mApi.getTask().execute().body();
                if (data == null || data.getCode() != 200) {
                    dueGetError(data);
                    throw new Exception("任务已完成");
                }
                Log.i(TAG, "start: " + data.getData().get(0).getSsid());
                if (mListener != null)
                    mListener.onAction("******start*******" + format.format(new Date(time)) + "\n" + data.getData().get(0).getSsid() + "\n");
                XSharedPreferenceUtil.setAps(data, LocService.this);
                if (mLocManger == null) {
                    int counter = 1;
                    while (true) {
                        if (mLocManger != null) break;
                        bindAidl();
                        if (counter > 3) {
                            Log.e(TAG, "bindAidl: " + counter);
                            return;
                        }
                        counter++;
                        try {
                            Thread.sleep(2000 * counter);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.e(TAG, "aidl start: ");
                List<Ap> aps = mLocManger.loc();
                Log.e(TAG, "aidl end: ");
                for (Ap ap : aps) {
                    ap.setLevel(data.getData().get(0).getLevel());
                    ap.setSsid(data.getData().get(0).getSsid());
                    ap.setBssid(data.getData().get(0).getBssid());
                    ap.setId(data.getData().get(0).getId());
                    ap.setDeviceId(data.getData().get(0).getDeviceId());
                    ap.setArea(data.getData().get(0).getArea());
                    if (data.getType() == TYPE_DEBUG)
                        ap.setDebug(JSON.toJSONString(data.getData()));
                    APList posted = mApi.postLoc(ap).execute().body();
                    if (mListener != null) {
                        mListener.onAction(ap.getLocationType() + ": " + ap.getLatitude() + "," + ap.getLongitude() + "\n地址: " +
                                ap.getAddress() + "\n精度: " + ap.getAccuracy() + "\n");
                    }
                    if (posted.getCode() != 200) duePostError(posted);
                    Log.i(TAG, "end: " + ap.toString());
                }
                if (mListener != null) mListener.onAction("******finished*******\n");
                if (mListener != null) mListener.onFinished();
                long delay = time + 3000 - System.currentTimeMillis();
                if (delay > 0) Thread.sleep(delay);
            } catch (Exception e) {
                Log.e(TAG, "onError: " + e.getMessage());
                if (mListener != null) mListener.onAction("******执行异常*******\n" + e.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                //break;

            }
        }
    }

    private void dueGetError(APList data) {
        if (mListener != null) mListener.onAction("******无新任务*******\n");
        Log.e("hook_service", "dueGetError: " + data.getCode());
    }

    private void duePostError(APList posted) {
        Log.e("hook_service", "duePostError: ");
    }

    ServiceConnection connection;

    private void bindAidl() {
        connection = new ServiceConnection() {
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (connection != null) unbindService(connection);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy: ", e);
        }

    }
}




