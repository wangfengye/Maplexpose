package com.maple.maplexpose.mqtt;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.amap.location.demo.rpc.Ap;
import com.maple.maplexpose.APList;
import com.maple.maplexpose.Api;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

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

    public static String SUB ;
    public static String PUB ;
    public static String REG ;

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

    public static final int QOS = 1;
    private MqttAndroidClient mClient;
    private BlockingQueue<APList> queue = new LinkedBlockingDeque<>();
    private int RETRY_SECOND = 1;
    private ExecutorService mPool = Executors.newFixedThreadPool(1);

    public MqttApiImpl init(Context context, String serverUri) {
        return this.init(context, serverUri, true);

    }

    public MqttApiImpl init(Context context, String serverUri, boolean isFirst) {
        if (isFirst) Log.e(TAG, "init: 初次连接");
        else Log.e(TAG, "init: 重新连接");
        mClient = new MqttAndroidClient(context, serverUri, CLIENT_ID);
        mClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
            }

            @Override
            public void connectionLost(Throwable cause) {
                init(context, serverUri, false);
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
        mqttConnectOptions.setWill(REG, "offline".getBytes(), QOS, true);
        try {
            Log.e(TAG, "init: " + Thread.currentThread().getName());
            mClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    RETRY_SECOND = 1;
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mClient.setBufferOpts(disconnectedBufferOptions);
                    Log.i(TAG, "connect-onSuccess: ");
                    sub();
                    try {
                        mClient.publish(REG, "online".getBytes(), QOS, true);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "onFailure--" + Thread.currentThread().getName() + ": " + exception.getMessage());
                    mPool.execute(() -> {
                        try {
                            Thread.sleep(RETRY_SECOND * 1000);
                            RETRY_SECOND = RETRY_SECOND << 1;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        init(context, serverUri, false);
                    });

                }
            });
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
                }
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    public Call<APList> getTask() {
        return new MqttCall<APList>() {
            @Override
            public Response<APList> execute() throws IOException {
                APList data = null;
                try {
                    data = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Response.success(data);
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

    public void ondestory() {
        if (mClient != null) {
            try {
                mClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
