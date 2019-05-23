package com.maple.maplexpose.mqtt;

import com.amap.location.demo.rpc.Ap;
import com.maple.maplexpose.APList;
import com.maple.maplexpose.Api;

import org.eclipse.paho.client.mqttv3.MqttCallback;

import java.io.IOException;

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
    @Override
    public Call<APList> getTask() {
        return new MqttCall<APList>();
    }

    @Override
    public Call<APList> postLoc(Ap ap) {
        return new MqttCall<APList>();
    }

    static class MqttCall<T> implements Call<T> {

        @Override
        public Response execute() throws IOException {
            //todo: mqtt处理数据
            T t;
            APList data = new APList();
            Ap ap =new Ap();
            ap.setSsid("mqtt");
            data.add(ap);
            t = (T) data;

            return Response.success(t);
        }

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
}
