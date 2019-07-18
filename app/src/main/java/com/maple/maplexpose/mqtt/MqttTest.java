package com.maple.maplexpose.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttTest {
    public static final String TAG = "MqttTest";
    private MqttAndroidClient mClient;

    public void init(Context context, String serverUri){
        mClient = new MqttAndroidClient(context, serverUri, "android_test_01");

        mClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                //此处处理连接成功的操作,避免初次连接失败,后续的重试成功不会调用onSuccess()接口
                Log.i(TAG, "connectComplete: reconnect--"+ reconnect+"\n serverURI---"+serverURI);
                if (!reconnect){
                    try {
                        mClient.subscribe("SUB", 0, null, new IMqttActionListener() {
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
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connectionLost: "+(cause==null?"null":cause.getMessage()));
            }

            @Override
            @SuppressWarnings("all")
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "messageArrived: topic --- "+topic +"\n message"+message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "deliveryComplete: "+token.getException().getMessage());
            }
        });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        // 遗嘱消息,retained,之后订阅者必然受到消息,但已订阅者可能收不到消息
        mqttConnectOptions.setWill("will", "offline".getBytes(), 0, false);
        try {
            mClient.connect(mqttConnectOptions, null,null);
        } catch (MqttException e) {
            e.printStackTrace();
            
        }
    }
}
