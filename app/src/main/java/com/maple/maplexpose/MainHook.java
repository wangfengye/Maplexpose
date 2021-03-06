package com.maple.maplexpose;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.widget.EditText;

import com.amap.location.demo.rpc.Ap;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String PACKAGE_NAME_LOC = "com.amap.location.demo";
    private static String mStr = "伟大航路";
    private static String mtmp = "started";

    //public static String[] macs = new String[]{"d8:2d:9b:03:1c:0a", "d4:ee:07:05:d2:e6", "00:0f:e2:00:00:51"};
    // wifi列表>3 才使用wifi定位,真实wifi>1即可进行定位,但真实wifi越少,定位失败率越高
    // wifi 定位 饿了么,钉钉,高德sdk 基本一致, sdk提供的地址详情有区别
    public static String[] macs = new String[]{"00:07:bf:a0:3d:32", "00:23:cd:5f:92:2e", "06:95:73:30:6a:1e"};
    //public static String[] sdids = new String[]{"ASCEND10_5G", "ASCEND9", "HHHHHH"};
    public static String[] sdids = new String[]{"88wifi", "TP-LINK_5F922E", "aWiFi-8888"};
    private static Context applicationContext;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 微信hook
        if (lpparam.packageName.equals(PACKAGE_NAME)) {
            try {
                //hookWeChat(lpparam);
                hookWeChatNearBy(lpparam);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        // hook wifi列表
        /*try {
            if (!lpparam.packageName.equals("com.android.systemui") && !lpparam.packageName.equals("com.android.settings")) {//屏蔽系统读取
                hookLoc(lpparam);
            }
            hookContent(lpparam);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        // wifi万能钥匙hook
        // hookWifiKey(lpparam);
    }

    private void hookWifiKey(XC_LoadPackage.LoadPackageParam lpparam) {
/*        XposedHelpers.findAndHookMethod("com.wifi.connect.ui.a", lpparam.classLoader, "a", String.class, int.class, NetworkInfo.State.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String s = (String) param.args[0];
                ArrayList<Integer> a = new ArrayList<>();
                a.se
                XposedBridge.log("wifiKey" + s);
                super.afterHookedMethod(param);
            }
        });*/
        XposedHelpers.findAndHookMethod("com.wifi.connect.ui.ConnectFragment", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("wifiKey:oncreate");
                super.afterHookedMethod(param);
            }
        });
    }

    private void hookLoc(XC_LoadPackage.LoadPackageParam lpparam) {

        XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", lpparam.classLoader, "getScanResults", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getScanResults" + lpparam.packageName);

                try {
                    long timeStamp = 0L;
                    int frequency = 2437;
                    Object resultO = param.getResult();
                    List<ScanResult> data = (List<ScanResult>) resultO;
                    if (data == null) data = new ArrayList<>();
                    if (data.size() > 0) {
                        timeStamp = data.get(0).timestamp;
                        frequency = data.get(0).frequency;
                    }

                    Class<?> cls = ScanResult.class;
                    Constructor<?> constructor = cls.getConstructor();
                    APList apList;
                    if (applicationContext == null) apList = new APList();
                    else apList = XSharedPreferenceUtil.getAps(applicationContext);
                    XposedBridge.log("hook getScanResults" + apList.toString());
            /*        for (int i = data.size()-1; i >=0 ; i--) {
                        if (!data.get(i).SSID.equals("aiwifi"))
                        data.remove(i);
                    }
*/
                    data.clear();
                    for (int i = 0; i < apList.getData().size(); i++) {
                        Ap ap = apList.getData().get(i);
                        ScanResult sr = (ScanResult) constructor.newInstance();
                        sr.BSSID = ap.getBssid();
                        sr.SSID = ap.getSsid();
                        sr.frequency = frequency;
                        sr.level = ap.getLevel();
                        sr.capabilities = "[WPA-PSK-CCMP][WPA2-PSK-CCMP][ESS]";
                        sr.timestamp = timeStamp;
                        data.add(sr);
                    }
                    param.setResult(data);


                } catch (Exception e) {
                    XposedBridge.log("hook getScanResults error" + e.getMessage());
                }

            }
        });
        XposedHelpers.findAndHookMethod("android.location.Location", lpparam.classLoader, "getLatitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getLatitude" + param.getResult());
                param.setResult((double) 0d);
            }
        });
        XposedHelpers.findAndHookMethod("android.location.Location", lpparam.classLoader, "getLongitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getLatitude" + param.getResult());
                param.setResult((double) 0d);

            }
        });
        XposedHelpers.findAndHookMethod("android.location.LocationManager", lpparam.classLoader, "getLastKnownLocation", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getLastKnownLocation" + Arrays.toString(param.args));
                Location location = (Location) param.getResult();
                if (location == null) return;
                XposedBridge.log("hook getLastKnownLocation" + location.getProvider());
                if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                    param.setResult(null);
                }

            }
        });
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getCellLocation", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getCellLocation");
                param.setResult(null);
            }
        });
        XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", lpparam.classLoader, "getNeighboringCellInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getNeighboringCellInfo");
                param.setResult(null);
            }
        });
        XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", lpparam.classLoader, "onCellInfoChanged", List.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook onCellInfoChanged");
                param.setResult(null);
            }
        });
        XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", lpparam.classLoader, "onCellLocationChanged", CellLocation.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook onCellLocationChanged");
                param.setResult(null);
            }
        });
        // 屏蔽 CdmaCellLocation
        XposedHelpers.findAndHookMethod("android.telephony.cdma.CdmaCellLocation", lpparam.classLoader, "getBaseStationId", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getBaseStationId");
                param.setResult(0);
            }
        });
        XposedHelpers.findAndHookMethod("android.telephony.cdma.CdmaCellLocation", lpparam.classLoader, "getNetworkId", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getNetworkId");
                param.setResult(0);
            }
        });

        XposedHelpers.findAndHookMethod("android.telephony.cdma.CdmaCellLocation", lpparam.classLoader, "getSystemId", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getSystemId");
                param.setResult(0);
            }
        });
        XposedHelpers.findAndHookMethod(" android.telephony.gsm.GsmCellLocation", lpparam.classLoader, "getCid", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getCid");
                param.setResult(0);
            }
        });
        XposedHelpers.findAndHookMethod(" android.telephony.gsm.GsmCellLocation", lpparam.classLoader, "getLac", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getLac");
                param.setResult(0);
            }
        });
        XposedHelpers.findAndHookMethod("android.telephony.CellInfoCdma", lpparam.classLoader, "getCellIdentity", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getCellIdentity");
                param.setResult(null);
            }
        });
        XposedHelpers.findAndHookMethod("android.telephony.CellInfoCdma", lpparam.classLoader, "getCellSignalStrength", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook getCellSignalStrength");
                param.setResult(null);
            }
        });
    }

    private void hookWeChat(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("hookWeChat: wx success");
        try {
            final ClassLoader classLoader = lpparam.classLoader;
            XposedHelpers.findAndHookMethod("com.tencent.mm.ui.tools.MultiStageCitySelectUI", classLoader, "initView", new XC_MethodHook() {
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
                                public void onClick(DialogInterface dialog, int which) {
                                    mStr = edit.getText().toString();
                                    if (MainActivity.mTv != null)
                                        MainActivity.mTv.setText(MainActivity.mTv.getText() + "\n hooka at" + System.currentTimeMillis() / 1000 + "\n" + edit.getText().toString());
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

    private static Object getFromField(XC_MethodHook.MethodHookParam param, String fieldName) throws Exception {
        Field field = param.thisObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(param.thisObject);
    }
    private static void setToField(XC_MethodHook.MethodHookParam param, String fieldName,Object value) throws Exception {
        Field field = param.thisObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(param.thisObject,value);
    }
    private void hookWeChatNearBy(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("hookWeChat: wx success");
        try {
            final ClassLoader classLoader = lpparam.classLoader;
            // 控制附近的人位置
            XposedHelpers.findAndHookMethod("com.tencent.mm.modelgeo.d", classLoader, "a", Class.forName("com.tencent.mm.modelgeo.b$a",true,classLoader), boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    // 120.737859,苏州
                    setToField(param,"fWw",31.278959d);
                    setToField(param,"fWx",120.737859d);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    double fWw = (double) getFromField(param, "fWw");
                    double fWx = (double) getFromField(param, "fWx");
                    int fWy = (int) getFromField(param, "fWy");
                    double fWz = (double) getFromField(param, "fWz");
                    double fWA = (double) getFromField(param, "fWA");
                    double bGk = (double) getFromField(param, "bGk");
                    String fWB = (String) getFromField(param, "fWB");
                    String fWC = (String) getFromField(param, "fWC");
                    int fWD = (int) getFromField(param, "fWD");
                    XposedBridge.log("fWw" + fWw
                            + "\nfWx:  " + fWx
                            + "\nfWy:  " + fWy
                            + "\nfWz:  " + fWz
                            + "\nfWA:  " + fWA
                            + "\nbGk:  " + bGk
                            + "\nfWB:  " + fWB
                            + "\nfWC:  " + fWC
                            + "\nfWd:  " + fWD);
                    super.afterHookedMethod(param);
                }
            });



            // 控制返回数量
            XposedHelpers.findAndHookMethod("com.tencent.mm.plugin.nearby.a.c", classLoader, "cdL", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    XposedBridge.log("cdL-before");
                }

                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    List<Object> data = (List<Object>) (param.getResult());
                    XposedBridge.log("cdL-after" + data.size());
                    // 保留一个看看情况
                   /* while (data.size() > 1) {
                        data.remove(0);
                    }*/

                }
            });

        } catch (Exception e) {
            XposedBridge.log(e.getMessage() + "ma2");
        }
    }

    private void hookContent(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> ContextClass = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(ContextClass, "getApplicationContext", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    if (applicationContext != null)
                        return;
                    applicationContext = (Context) param.getResult();
                    XposedBridge.log("CSDN_LQR-->得到上下文");
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("CSDN_LQR-->获取上下文出错");
            XposedBridge.log(t);
        }
    }
}
