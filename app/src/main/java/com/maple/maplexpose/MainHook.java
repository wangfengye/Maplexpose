package com.maple.maplexpose;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author maple on 2018/11/29 15:46.
 * @version v1.0
 * @see 1040441325@qq.com
 * hook入口类
 */
public class MainHook implements IXposedHookLoadPackage {
    private static final String PACKAGE_NAME = "com.tencent.mm";
    private static String mStr = "伟大航路";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(PACKAGE_NAME)) {
            try {
                hookWeChat(lpparam);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    private void hookWeChat(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("hookWeChat: wx success");
        try {
            final ClassLoader classLoader = lpparam.classLoader;
            XposedHelpers.findAndHookMethod("com.tencent.mm.ui.tools.MultiStageCitySelectUI", classLoader, "initView",new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                }

                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) param.thisObject;
                    final EditText edit = new EditText(context);
                    new AlertDialog.Builder(context)
                            .setTitle("设置列表第一个地区名")
                            .setView(edit)
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) { ;
                                    mStr = edit.getText().toString();
                                    try {
                                        Method m = param.thisObject.getClass().getDeclaredMethod("cJa");
                                        m.invoke(param.thisObject);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).show();

                }
            });

            // 修改地区列表
            XposedHelpers.findAndHookMethod("com.tencent.mm.ui.tools.MultiStageCitySelectUI", classLoader, "cJa", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedBridge.log("hookWeChat: wx success MultiStageCitySelectUI cJa beforeHookedMethod");
                }

                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedBridge.log("hookWeChat: wx success MultiStageCitySelectUI cJa  afterHookedMethod");
                    Object wdnObj = XposedHelpers.findField(XposedHelpers.findClass("com.tencent.mm.ui.tools.MultiStageCitySelectUI", classLoader), "wdN").get(param.thisObject);
                    Class regionClazz = XposedHelpers.findClass("com.tencent.mm.storage.RegionCodeDecoder$Region", classLoader);
                    Field codeField = XposedHelpers.findField(regionClazz, "code");
                    Field nameField = XposedHelpers.findField(regionClazz, "name");
                    Object array = Array.get(wdnObj, 0);
                    codeField.set(array, mStr);
                    nameField.set(array, mStr);
                    Array.set(wdnObj, 0, array);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
