package net.softm.data.kata.util;


import android.annotation.SuppressLint;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import net.softm.data.kata.GpsTracker;
import net.softm.data.kata.OverlayViewService;
import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.common.OverlayStartMode;
import net.softm.data.kata.common.Package;
import net.softm.data.kata.common.StartMode;
import net.softm.data.kata.dto.AppInfoDTO;
import net.softm.data.kata.kt.Callbackkt;

public class OverlayViewUtil {
    static String TAG = "OverlayViewUtil";
    private Context mContext;
    private static Intent mIntent;
    private static Intent mAddIntent;
    @SuppressLint("StaticFieldLeak")
    private static OverlayViewUtil overlayViewUtil;
    public OverlayViewUtil(Context ctx)
    {
        mContext = ctx;
    }


    public static OverlayViewUtil init(Context ctx) {
        Fc.i(ctx,TAG,"init");
        if ( overlayViewUtil != null && mIntent != null ) {
            Fc.i(ctx,TAG,"init","stopService");
            stopService();
// TODO SOFTM : debugging 20240731
        }

        overlayViewUtil = new OverlayViewUtil(ctx);
        overlayViewUtil.mContext = ctx;
//        overlayViewUtil.initIntent();
        overlayViewUtil.mAddIntent = new Intent(ctx, OverlayViewService.class);

        return overlayViewUtil;
    }
    private OverlayViewService overlayViewService;
    public static boolean isBound = false;
    public static ServiceConnection serviceConnection;

    public OverlayViewUtil initIntent() {
        return initIntent(false);
    }
    private static Callbackkt callbackkt = new Callbackkt(TAG,true);

    public OverlayViewUtil initIntent(Boolean booted) {
        Fc.i(mContext,TAG,"initIntent");
        unbindFromConnectService(mContext);
        bindToConnectService(mContext);
        mIntent = new Intent(mContext, OverlayViewService.class);
        try {
            if (!booted)
                overlayViewUtil.mContext.bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception ex) {
            Fc.e(overlayViewUtil.mContext, TAG,"bindService",ex.toString());
        }
        Fc.i(mContext,TAG,"initIntent","End");
        return this;
    }

    public static void bindToConnectService(Context context) {
        Fc.i(WApplication.mInstance,TAG,"bindToConnectService");
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                OverlayViewService.LocalBinder binder = (OverlayViewService.LocalBinder) service;
                overlayViewUtil.overlayViewService = binder.getService();
                overlayViewUtil.isBound = true;
                Fc.i(WApplication.mInstance,TAG,"bindToConnectService","onServiceConnected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                overlayViewUtil.isBound = false;
                Fc.i(WApplication.mInstance,TAG,"bindToConnectService","onServiceDisconnected");
            }
        };
    }

    public static void unbindFromConnectService(Context context) {
        if (overlayViewUtil.isBound) {
            try {
                context.unbindService(serviceConnection);
                overlayViewUtil.serviceConnection = null;
                overlayViewUtil.isBound = false;
            } catch (Exception e) {
            }
        }
    }

    /*
        Intent oIntent = new Intent(context, OverlayViewService.class);
        oIntent.putExtra("P_NAVI_DESTINATION", naviDestination);
        oIntent.putExtra("P_IMMEDIATE", immediate);
        oIntent.putExtra("P_NAME", name);
        oIntent.putExtra("P_LAT", lat);
        oIntent.putExtra("P_LON", lon);
        oIntent.putExtra("P_CLOSE_POPUP", closePopup);
        oIntent.putExtra("P_NAVI_AUTO_START", naviAutoStart);
        oIntent.putExtra("P_BOOT", booted);
        oIntent.putExtra("P_REFENCE", refence);
        context.startService(oIntent);
    */

    public OverlayViewUtil addNaviDestination(NaviDestination v) {
        mIntent.putExtra("P_NAVI_DESTINATION",v);
        return this;
    }

    public OverlayViewUtil addImmediate(boolean v) {
        mIntent.putExtra("P_IMMEDIATE",v);
        return this;
    }
    public OverlayViewUtil addName(String v) {
        mIntent.putExtra("P_NAME",v);
        return this;
    }
    public OverlayViewUtil addLat(float v) {
        mIntent.putExtra("P_LAT",v);
        return this;
    }

    public OverlayViewUtil addLon(float v) {
        mIntent.putExtra("P_LON",v);
        return this;
    }

    public OverlayViewUtil addClosePopup(boolean v) {
        mIntent.putExtra("P_CLOSE_POPUP",v);
        return this;
    }

    public OverlayViewUtil addNaviAutoStart(boolean v) {
        mIntent.putExtra("P_NAVI_AUTO_START",v);
        return this;
    }

    public OverlayViewUtil addBooted(boolean v) {
        mIntent.putExtra("P_BOOT",v);
        return this;
    }
    public OverlayViewUtil addExpandSub(boolean v) {
        mIntent.putExtra("P_EXPAND_SUB",v);
        return this;
    }

    public OverlayViewUtil addRefence(boolean v) {
        mIntent.putExtra("P_REFENCE",v);
        return this;
    }

    public OverlayViewUtil addStartMode(OverlayStartMode v) {
        mAddIntent.putExtra("P_START_MODE",v);
        return this;
    }
    public OverlayViewUtil addHide(boolean v) {
        mAddIntent.putExtra("P_HIDE",v);
        return this;
    }
    public OverlayViewUtil addMsg(String v) {
        mAddIntent.putExtra("P_MSG",v);
        return this;
    }
    public OverlayViewUtil addExtras() {
        mIntent.putExtras(mAddIntent);
        return this;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public void startService() {
//        Utils.sleepMicoSeconds(1000);
        Fc.i(mContext,TAG,"startService");
//        mContext.startService(mIntent);
        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O) {
            mContext.startForegroundService(mIntent);
        } else{
            mContext.startService(mIntent);
        }

// TODO SOFTM : debugging 20240731
/*
        checkHandler = new Handler();
        checkHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (OverlayViewService.isForeground) {
//                    if (Utils.isActivityRunning(mContext,OverlayViewService.class)) {
                    checkHandler.postDelayed(this, 500);
                    Log.d(TAG,"wating startService");
                } else {
                    if ( mOverlayViewUtil !=null && mOverlayViewUtil.mContext != null ) {
                        Log.d(TAG,"startService");
                        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O) {
                            mContext.startForegroundService(mIntent);
                        } else{
                            mContext.startService(mIntent);
                        }
                    }
                    checkHandler.removeCallbacksAndMessages(null);
                }
            }
        },0);
*/
    }
    static Handler checkHandler;

    public static void stopService() {
//        WorkerExeckt.delayCallback((param)->{  // TODO SOFTM : 2024.12.11 : ANR
            Fc.i(WApplication.mInstance, TAG,"stopService");
            if ( overlayViewUtil !=null && overlayViewUtil.mContext != null ) {
                Fc.i(overlayViewUtil.mContext,TAG,"stopService","Step-1");
                if (overlayViewUtil.isBound) { // TODO SOFTM : ANR : 2024.12.11 : ANR과 연관되었을 가능성 있음 isBound는 bindService시 비동기적으로 호출되므로 이 부분을 Enable했을 경우
                                               // TODO SOFTM : ANR : 2024.12.11 : unBindService가 호출되지 않고 다시 bindService가 호출되어 성능에 영향을 주는 것으로 추측됨
                    try {
                        overlayViewUtil.mContext.unbindService(overlayViewUtil.serviceConnection);
                    } catch (Exception e) {
                        Fc.e(overlayViewUtil.mContext, TAG,"startService","ex");
                    }
                    overlayViewUtil.isBound = false;
                }
                if ( mIntent != null ) {
                    overlayViewUtil.mContext.stopService(mIntent);
                }
            }
//            return null;
//        });
    }

    public static void show(boolean exp) {
        if ( overlayViewUtil !=null && overlayViewUtil.mContext != null && overlayViewUtil.overlayViewService != null ) {
            Log.d(TAG,"show");
            overlayViewUtil.overlayViewService.show(exp);
        }
    }
    public static void show() {
        if ( overlayViewUtil !=null && overlayViewUtil.mContext != null && overlayViewUtil.overlayViewService != null ) {
            Log.d(TAG,"show");
            overlayViewUtil.overlayViewService.show();
        }
    }

    public static void show(String msg) {
        if ( overlayViewUtil !=null && overlayViewUtil.mContext != null && overlayViewUtil.overlayViewService != null ) {
            Log.d(TAG,"show");
            overlayViewUtil.overlayViewService.show(msg);
        }
    }

    public static void hide(boolean closePopup) {
        if ( overlayViewUtil !=null && overlayViewUtil.mContext != null && overlayViewUtil.overlayViewService != null ) {
            Log.d(TAG,"hide");
            overlayViewUtil.overlayViewService.hide(closePopup);
        }
    }

    public static void hide() {
        hide(true);
    }

    public static void redrawPermission() {
        if ( overlayViewUtil !=null && overlayViewUtil.mContext != null && overlayViewUtil.overlayViewService != null ) {
            L.d(TAG,"redrawPermission");
            overlayViewUtil.overlayViewService.redrawPermission();
        }
    }

    public static void redrawRecentPois() {
        if ( overlayViewUtil !=null && overlayViewUtil.mContext != null && overlayViewUtil.overlayViewService != null ) {
            L.d(TAG,"redrawRecentPoi");
            overlayViewUtil.overlayViewService.redrawRecentPois();
        }
    }

    public static void redrawFavoritePois() {
        if ( overlayViewUtil !=null && overlayViewUtil.mContext != null && overlayViewUtil.overlayViewService != null ) {
            L.d(TAG,"redrawFavoritePois");
            overlayViewUtil.overlayViewService.redrawFavoritePois();
        }
    }

    public static void setMusicPlayInfo(String title, String text, String subText, String infoText, String summaryText) {
        if ( overlayViewUtil !=null && overlayViewUtil.mContext != null && overlayViewUtil.overlayViewService != null ) {
            L.d(TAG,"setMusicPlayInfo");
            overlayViewUtil.overlayViewService.setMusicPlayInfo(title, text, subText, infoText, summaryText);
        }
    }

    public void startNavi(NaviDestination naviDestination, boolean immediate, boolean booted) {
        startNavi(naviDestination,immediate,booted,false,"",-1,-1);
    }

    public void startNavi(NaviDestination naviDestination, boolean immediate, boolean booted,boolean refense, String name, float lat, float lon) {
        startNavi(naviDestination,immediate,booted,refense,name,lat,lon,false);
    }

    public void startNavi(NaviDestination naviDestination, boolean immediate, boolean booted,boolean refense, boolean expandSub) {
        startNavi(naviDestination,immediate,booted,refense,MapUtil.isOverlayClose(),"",-1,-1,expandSub);
    }

    public void startNavi(NaviDestination naviDestination, boolean immediate, boolean booted,boolean refense, String name, float lat, float lon,boolean expandSub) {
        startNavi(naviDestination,immediate,booted,refense,MapUtil.isOverlayClose(),name,lat,lon,expandSub);
    }
    public void startNavi(NaviDestination naviDestination, boolean immediate, boolean booted,boolean refense, boolean closePopup, String name, float lat, float lon,boolean expandSub) {
        GpsTracker gpsTracker = new GpsTracker(mContext);
        double myLat = gpsTracker.getLatitude();
        double myLon = gpsTracker.getLongitude();
        Fc.i(mContext, TAG,"startNavi");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                UsageStatsManager usageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
                usageStatsManager.isAppInactive("");
            }
        }

//        getAppPackage().equals(PACKAGE_SKT_MAP)
        if ( MapUtil.isInstallApp(mContext, MapUtil.getAppPackage()) || !immediate ) {
//            boolean closePopup = MapUtil.isClosePopup();
            boolean naviAutoStart = MapUtil.getNaviAutoStart().equals(StartMode.START);
//            if ( !isTopForTmap(context) ) {
            if ( MapUtil.isNaving() ) { // navi 실행중 아님 - cancel : false
                if (!naviAutoStart) {
                    if (naviDestination.equals(NaviDestination.DRIVE)) naviAutoStart = true;
                }
            }
            if ( hasOverlayPermission() ) {
                boolean finalNaviAutoStart = naviAutoStart;
//                Callbackkt.runFirst((param)-> {
//                callbackkt.run((param)-> {
// 코루틴 적용시 연속호출시 죽음
                    initIntent(booted)
                    .addNaviDestination(naviDestination)
                    .addImmediate(immediate)
                    .addName(name)
                    .addLat(lat)
                    .addLon(lon)
                    .addClosePopup(closePopup)
                    .addNaviAutoStart(finalNaviAutoStart)
                    .addBooted(booted)
                    .addRefence(refense)
                    .addExpandSub(expandSub)
                    .addExtras()
                    .startService();
//                    return null;
//                });
//                    openOverlayView(context, naviDestination, immediate, closepopup, naviAutoStart, booted);
            } else {
                if (!naviDestination.equals(NaviDestination.NONE) && ( immediate || naviAutoStart ) ) {
                    MapUtil.startNaviForExecActivity(mContext,naviDestination);
                }
            }
//            } else {
//                if ( isNaviApp() ) Toast.makeText(context, "내비 안내중.", Toast.LENGTH_SHORT).show();
//                else               Toast.makeText(context, "실행중", Toast.LENGTH_SHORT).show();
//            }
        } else {
//            Toast.makeText(context, "티맵이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
//            getAppPackage().equals(PACKAGE_SKT_MAP);
            AppInfoDTO appInfo = WApplication.appInfo;
            if ( MapUtil.getAppPackage().equals(Package.SKT_MAP.getId()) ) {
                // Utils.toast(context, "티맵이 설치되어 있지 않습니다.");
                Utils.toast(mContext, R.string.msg_tmap_not_install);
            } else {
                // Utils.toast(context, appInfo.name + " 앱이 설치되어 있지 않습니다.");
                Utils.toast(mContext, mContext.getString(R.string.msg_app_not_install,appInfo.name));
            }
            new Thread() {
                @Override
                public void run() {
                    // Uri uri = Uri.parse(tmaptapi.getTMapDownUrl().get(0));
                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + appInfo.packageName);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }.start();
        }
    }


    public void openOverlayViewShowOnly(boolean expandSub) {
        startNavi(NaviDestination.NONE,false,false,false,null, -1,-1,expandSub);

    }

    public  void openOverlayViewShowOnlyNotClose() {
        startNavi(NaviDestination.NONE,false,false,false,false,null, -1,-1,true);
    }
    public void openOverlayViewShowOnly() {
        openOverlayViewShowOnly(false);
    }

    /**
     * overlay없이 실행
     */
    public void openHide() {
        addHide(true);
        startNavi(NaviDestination.NONE,false,false,false,false,null, -1,-1,false);
    }

    public static boolean hasOverlayPermission() {
        boolean exec = false;
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if (Settings.canDrawOverlays(WApplication.mInstance.getApplicationContext())) exec = true;
        } else {
            exec = true;
        }
        return exec;
    }
}

