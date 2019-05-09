package com.maple.maplexpose;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Type;

import de.robv.android.xposed.XSharedPreferences;

/**
 * @author maple on 2019/5/9 15:32.
 * @version v1.0
 * @see 1040441325@qq.com
 */
public class XSharedPreferenceUtil {
    private static final SharedPreferences PREFERENCES=null;

    private static final String APP_PREFERENCES_KEY = "profile";

    public static SharedPreferences getAppPreference(Context context) {
        SharedPreferences pre = context.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_WORLD_READABLE);
        return pre;
    }
    public static <T> T getJsonInHook(String key, Type type){
        XSharedPreferences pre = new XSharedPreferences(XSharedPreferenceUtil.class.getPackage().getName(),APP_PREFERENCES_KEY);
        String json = pre.getString(key,"");
        return JSON.parseObject(json,type);
    }

    public static boolean setJson(Context context,String key,Object object){
        String json = JSON.toJSONString(object);
        return getAppPreference(context).edit()
                .putString(key,json)
                .commit();
    }
    public static <T> T getJson(Context context,String key, Type type){
        String json = getAppPreference(context).getString(key,"");
        return JSON.parseObject(json,type);
    }

}
