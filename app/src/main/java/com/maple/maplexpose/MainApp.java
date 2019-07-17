package com.maple.maplexpose;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

public class MainApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "fbaa0d6407", true);
    }
}
