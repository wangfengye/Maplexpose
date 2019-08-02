package com.maple.maplexpose;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

public class MainApp extends Application{
    public static long time;
    @Override
    public void onCreate() {
        super.onCreate();
        time=System.currentTimeMillis();
        CrashReport.initCrashReport(getApplicationContext(), "fbaa0d6407", true);
    }
}
