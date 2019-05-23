package com.maple.maplexpose.mqtt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.maple.maplexpose.R;
import com.maple.maplexpose.util.FixLinesStr;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

@SuppressWarnings("all")
public class MqttActivity extends AppCompatActivity {
    public static final String TAG = "MqttActivity";
    private TextView mTv;
    MqttAndroidClient mClient;
    final String serverUri = "tcp://192.168.168.149:1883";

    String clientId = "ExampleAndroidClient";
    final String subscriptionTopic = "/device/+/upward";
    private FixLinesStr mFixLInesStr = new FixLinesStr();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);
        mTv = findViewById(R.id.tv_content);
        mClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) Log.i(TAG, "reconnectComplete: ");
                else Log.i(TAG, "connectComplete: ");
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connectionLost: ");
            }

            @Override
            @SuppressWarnings("all")
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "messageArrived: " + new String(message.getPayload()));
                show(new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        try {
            mClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mClient.setBufferOpts(disconnectedBufferOptions);
                    Log.i(TAG, "onSuccess: ");
                    sub();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "onFailure: " +
                            exception.getLocalizedMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        // publish
        findViewById(R.id.btn_publish).setOnClickListener(v -> {
            String data = "当前时间戳: " + System.currentTimeMillis() / 1000;
            try {
                mClient.publish("AndroidPush", new MqttMessage(data.getBytes()));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
    }

    private void sub() {
        try {
            mClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
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
            Log.i(TAG, e.getMessage());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void show(String s) {
        runOnUiThread(() -> mTv.setText(mFixLInesStr.put(s)));

    }

    @Override
    protected void onDestroy() {
        if (mClient != null) {
            try {
                mClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
