package com.maple.maplexpose;

import com.amap.location.demo.rpc.Ap;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * @author maple on 2019/5/14 9:50.
 * @version v1.0
 * @see 1040441325@qq.com
 */
public interface Api {
    @GET("ap/unlocated")
    Call<APList> getTask();
    @POST("ap/location")
    Call<APList> postLoc(@Body Ap ap);
}
