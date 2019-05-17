package com.maple.maplexpose;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amap.location.demo.rpc.Ap;
import com.amap.location.demo.rpc.LocManager;
import com.maple.mapleretrofit.RetrofitFactory;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**

 */
public class LocService extends IntentService {
    public static final String TAG = "LocService";
    private Api mApi = RetrofitFactory.create().baseUrl("http://123.57.175.155:8865").build().create(Api.class);
    private LocManager mLocManger;

    public LocService() {
        super("LocService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // 获取任务
        // 修改 scanResults;
        // 执行定位
        // 上报定位结果

        for (; ; ) {
            try {
                APList data = mApi.getTask().execute().body();
                if (data == null || data.getCode() != 200) {
                    dueGetError(data);
                    return;
                }
                Log.i(TAG, "start: " + data.getData().get(0).getSsid());
                XSharedPreferenceUtil.setAps(data, LocService.this);
                if (mLocManger == null) {
                    bindAidl();
                    int counter = 0;
                    while (true) {
                        if (mLocManger != null) break;
                        if (counter > 3) {
                            Log.e(TAG, "bindAidl: " + counter);
                            return;
                        }
                        counter++;
                        try {
                            Thread.sleep(2000);
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
                    APList posted = mApi.postLoc(ap).execute().body();
                    if (posted.getCode() != 200) duePostError(posted);
                    Log.i(TAG, "end: " + ap.toString());
                }
                Thread.sleep(3000);
            } catch (Exception e) {
                Log.e(TAG, "onError: "+e.getMessage() );
            }

        }
    }

    private void dueGetError(APList data) {
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

       if (connection!=null)unbindService(connection);
    }
}




