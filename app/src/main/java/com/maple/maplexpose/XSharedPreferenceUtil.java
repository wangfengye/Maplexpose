package com.maple.maplexpose;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
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
    public static String getString(Context context,String key){
        return getAppPreference(context).getString(key,"");
    }
    public static boolean setString(Context context,String key,String data){
        return getAppPreference(context).edit()
                .putString(key,data)
                .commit();
    }
    /******** ContentProvider 共享数据**********/
    private static final Uri uri_aps = Uri.parse("content://com.maple.maplexpose");
    public static APList getAps(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor data =  resolver.query(uri_aps, null, null, null, null);
        String json = data.getColumnName(0);
        APList aps = JSON.parseObject(json, APList.class);
        return aps;
    }

    public static boolean setAps(APList aps, Context context) {
        String json = JSON.toJSONString(aps);
        ContentResolver resolver = context.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put("APS", json);
        return resolver.update(uri_aps, cv, null, null) == 1;
    }
}
