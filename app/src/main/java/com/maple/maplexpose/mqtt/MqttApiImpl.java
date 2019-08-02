package com.maple.maplexpose.mqtt;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.amap.location.demo.rpc.Ap;
import com.maple.maplexpose.APList;
import com.maple.maplexpose.Api;
import com.maple.maplexpose.LocService;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author maple on 2019/5/23 16:35.
 * @version v1.0
 * @see 1040441325@qq.com
 */
public class MqttApiImpl implements Api {
    private static final String TAG = "MqttApiImpl";
    private static final String CLIENT_ID = getMac();
    private int errorLog;//记录断线状态发送的拉消息数量

    public static String SUB;
    public static String PUB;
    public static String REG;

    //自定义通配符前缀,用于个人测试
    public static void setTopicPrefix(String s) {
        SUB = s + "loc_req/" + getMac();
        PUB = s + "loc_res/" + getMac();
        REG = s + "register/" + getMac();
    }

    public static void resetTopic() {
        SUB = "loc_req/" + getMac();
        PUB = "loc_res/" + getMac();
        REG = "register/" + getMac();
    }

    public static final int QOS = 0;
    private MqttAndroidClient mClient;
    private LinkedBlockingDeque<APList> queue = new LinkedBlockingDeque<>();
    private ListenerState mListenerState;

    public void setListenerState(ListenerState mListenerState) {
        this.mListenerState = mListenerState;
    }

    public MqttApiImpl init(Context context, String serverUri) {
        return this.init(context, serverUri, true);

    }

    /**
     *
     * @return 当前缓存任务量
     */
    public int getCacheTaskSize(){
        return queue==null?0:queue.size();
    }
    public int getErrorLog(){
        return errorLog;
    }
    public MqttApiImpl init(Context context, String serverUri, boolean isFirst) {
        if (isFirst) Log.e(TAG, "init: 初次连接");
        else Log.e(TAG, "init: 重新连接");
        mClient = new MqttAndroidClient(context, serverUri, CLIENT_ID);
        mClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (mListenerState!=null)mListenerState.onStateChange(1);
                if (reconnect) {
                    Log.i(TAG, "connectComplete: ReConnect " + mClient.isConnected());
                } else {
                    sub();
                    try {
                        mClient.publish(REG, "online".getBytes(), QOS, true);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "connectComplete: firstConnect");
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                if (mListenerState!=null)mListenerState.onStateChange(0);
                Log.i(TAG, "connectionLost: ");
            }

            @Override
            @SuppressWarnings("all")
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                try {
                    String json = new String(message.getPayload());
                    APList apList = JSON.parseObject(json, APList.class);
                    queue.offer(apList);
                } catch (JSONException e) {
                    Log.e(TAG, "messageArrived: " + e.getMessage());
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setWill(REG, "offline".getBytes(), QOS, false);
        try {
            Log.e(TAG, "init: " + Thread.currentThread().getName());
            mClient.connect(mqttConnectOptions, null, null);
        } catch (MqttException e) {
            e.printStackTrace();
            init(context, serverUri, false);
        }

        return this;
    }

    private void sub() {
        try {
            mClient.subscribe(SUB, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "onSuccess: sub");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "onFailure: sub");
                    throw new RuntimeException(exception.getMessage());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public Call<APList> getTask() {
        return new MqttCall<APList>() {
            @Override
            public Response<APList> execute() throws IOException {
                APList data = null;
                for (; ; ) {
                    try {
                        data = queue.poll(2, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (data != null) return Response.success(data);
                    else if (mClient.isConnected()) {
                        errorLog++;
                        //重发获取任务指令.
                        Log.e(TAG, "超时未下发数据,发送上报请求数据");
                        if (LocService.mApi == null) continue;
                        Ap ap = new Ap();
                        ap.setId(-1);//-1表示无效上报,只是为了申请新数据
                        ap.setLocationType("高德_-1");
                        // posted为返回结果,无需处理
                        APList posted = LocService.mApi.postLoc(ap).execute().body();
                    } else {
                        //断网中
                        Log.e(TAG, "断网中,等待重连");

                    }
                }

            }
        };
    }

    @Override
    public Call<APList> postLoc(Ap ap) {
        return new MqttCall<APList>() {
            @Override
            public Response<APList> execute() throws IOException {
                try {
                    mClient.publish(PUB, new MqttMessage(JSON.toJSONString(ap).getBytes()));
                    return Response.success(APList.success());
                } catch (MqttException e) {
                    e.printStackTrace();
                    return Response.success(new APList());
                }
            }
        };
    }

    static abstract class MqttCall<T> implements Call<T> {


        @Override
        public void enqueue(Callback callback) {

        }

        @Override
        public boolean isExecuted() {
            return false;
        }

        @Override
        public void cancel() {

        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public Call clone() {
            return null;
        }

        @Override
        public Request request() {
            return null;
        }
    }

    /**
     * 获取本机mac
     *
     * @return string类型mac地址
     */
    public static String getMac() {
        String mac = null;
        StringBuffer buf = new StringBuffer();
        NetworkInterface networkInterface = null;
        try {
            networkInterface = NetworkInterface.getByName("eth1");
            if (networkInterface == null) networkInterface = NetworkInterface.getByName("wlan0");
            if (networkInterface == null) return "02:00:00:00:00:00";
            byte[] addr = networkInterface.getHardwareAddress();
            for (byte b : addr) {
                buf.append(String.format("%02X-", b));
            }
            if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
            mac = buf.toString();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return mac;
    }

    public void onDestroy() {
        if (mClient != null) {
            try {
                mClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

   public interface  ListenerState{
        void onStateChange(int i);
    }
}
