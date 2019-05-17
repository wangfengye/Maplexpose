package com.maple.maplexpose;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author maple on 2019/5/16 10:55.
 * @version v1.0
 * @see 1040441325@qq.com
 */
public class ScanResultsProvider extends ContentProvider{
    public static final String AUTOHORITY = "com.maple.maplexpose";
    @Override
    public boolean onCreate() {
        // todo: create database
        XSharedPreferenceUtil.getAppPreference(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
       String aps=XSharedPreferenceUtil.getString(getContext(),"APS");
       String[] res = new String[]{aps};
        MatrixCursor cursor = new MatrixCursor(res);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        XSharedPreferenceUtil.setString(getContext(),"APS",values.getAsString("APS"));
        return 1;
    }
}
