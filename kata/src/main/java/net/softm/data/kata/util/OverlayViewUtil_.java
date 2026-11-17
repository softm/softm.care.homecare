//package net.softm.data.kata.util;
//
//
//import android.annotation.SuppressLint;
//import android.app.usage.UsageStatsManager;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Handler;
//import android.os.IBinder;
//import android.provider.Settings;
//import android.util.Log;
//
//import com.google.firebase.crashlytics.FirebaseCrashlytics;
//
//import net.softm.data.kata.GpsTracker;
//import net.softm.data.kata.OverlayViewService;
//import net.softm.data.kata.R;
//import net.softm.data.kata.WApplication;
//import net.softm.data.kata.common.NaviDestination;
//import net.softm.data.kata.common.Package;
//import net.softm.data.kata.dto.AppInfoDTO;
//
//public class OverlayViewUtil_ {
//    private static final String TAG = "OverlayViewUtil_";
//    private static Intent mIntent;
//    @SuppressLint("StaticFieldLeak")
//    private static OverlayViewUtil_ overlayViewUtil;
//    private Context mContext;
//    public OverlayViewUtil_(Context ctx)
//    {
//        mContext = ctx;
//    }
//
//    public static OverlayViewUtil_ getInstance(Context ctx) {
//        if ( overlayViewUtil != null && mIntent != null ) {
//            return overlayViewUtil;
//        } else {
//            overlayViewUtil = new OverlayViewUtil_(ctx);
//            overlayViewUtil.mContext = ctx;
//            overlayViewUtil.initIntent();
//        }
//        return overlayViewUtil;
//    }
//
//    public static OverlayViewUtil_ init(Context ctx) {
//        if ( overlayViewUtil != null && mIntent != null ) {
//            stopService();
//// TODO SOFTM : debugging 20240731
//            unbindService();
//        }
//
//        overlayViewUtil = getInstance(ctx);
//        return overlayViewUtil;
//    }
//    private OverlayViewService overlayViewService;
//    private boolean isBound = false;
//    private ServiceConnection serviceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            OverlayViewService.LocalBinder binder = (OverlayViewService.LocalBinder) service;
//            overlayViewUtil.overlayViewService = binder.getService();
//            overlayViewUtil.isBound = true;
//            if ( bindServiceCallback != null ) {
//                try {
//                    Thread thread = new Thread(overlayViewUtil.bindServiceCallback);
//                    thread.start();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            overlayViewUtil.isBound = false;
//            overlayViewUtil.overlayViewService = null;
//        }
//    };
//
//    public OverlayViewUtil_ initIntent() {
//        mIntent = new Intent(mContext, OverlayViewService.class);
//        bindService();
//        return this;
//    }
//
//    /*
//        Intent oIntent = new Intent(context, OverlayViewService.class);
//        oIntent.putExtra("P_NAVI_DESTINATION", naviDestination);
//        oIntent.putExtra("P_IMMEDIATE", immediate);
//        oIntent.putExtra("P_NAME", name);
//        oIntent.putExtra("P_LAT", lat);
//        oIntent.putExtra("P_LON", lon);
//        oIntent.putExtra("P_CLOSE_POPUP", closePopup);
//        oIntent.putExtra("P_NAVI_AUTO_START", naviAutoStart);
//        oIntent.putExtra("P_BOOT", booted);
//        oIntent.putExtra("P_REFENCE", refence);
//        context.startService(oIntent);
//    */
//
//    public OverlayViewUtil_ addNaviDestination(NaviDestination v) {
//        mIntent.putExtra("P_NAVI_DESTINATION",v);
//        return this;
//    }
//
//    public OverlayViewUtil_ addImmediate(boolean v) {
//        mIntent.putExtra("P_IMMEDIATE",v);
//        return this;
//    }
//    public OverlayViewUtil_ addName(String v) {
//        mIntent.putExtra("P_NAME",v);
//        return this;
//    }
//    public OverlayViewUtil_ addLat(float v) {
//        mIntent.putExtra("P_LAT",v);
//        return this;
//    }
//
//    public OverlayViewUtil_ addLon(float v) {
//        mIntent.putExtra("P_LON",v);
//        return this;
//    }
//
//    public OverlayViewUtil_ addClosePopup(boolean v) {
//        mIntent.putExtra("P_CLOSE_POPUP",v);
//        return this;
//    }
//
//    public OverlayViewUtil_ addNaviAutoStart(boolean v) {
//        mIntent.putExtra("P_NAVI_AUTO_START",v);
//        return this;
//    }
//
//    public OverlayViewUtil_ addBooted(boolean v) {
//        mIntent.putExtra("P_BOOT",v);
//        return this;
//    }
//    public OverlayViewUtil_ addExpandSub(boolean v) {
//        mIntent.putExtra("P_EXPAND_SUB",v);
//        return this;
//    }
//
//    public OverlayViewUtil_ addRefence(boolean v) {
//        mIntent.putExtra("P_REFENCE",v);
//        return this;
//    }
//
//    public Intent getIntent() {
//        return mIntent;
//    }
//
//    FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
//
//    public OverlayViewUtil_ startService() {
////        Utils.sleepMicoSeconds(1000);
//        crashlytics.setCustomKey("gubun","OverlayViewUtil.startService");
//        crashlytics.log(TAG + " : OverlayViewUtil.startService");
//        Log.i(TAG,"startService");
////        mContext.startService(mIntent);
//        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O) {
//            mContext.startForegroundService(mIntent);
//        } else{
//            mContext.startService(mIntent);
//        }
//        return this;
//    }
//    private Runnable bindServiceCallback;
//    public void bindService(Runnable runnable) {
//        overlayViewUtil.bindServiceCallback = runnable;
//        overlayViewUtil.mContext.bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//    }
//    public void bindService() {
//        bindService(null);
//    }
//
//    static Handler checkHandler;
//
//    public static void stopService() {
//        if ( overlayViewUtil !=null && overlayViewUtil.mContext != null ) {
//            Log.d(TAG,"stopService");
//            overlayViewUtil.mContext.stopService(mIntent);
//        }
//        unbindService();
//    }
//
//    public static void unbindService() {
//        if (overlayViewUtil.isBound) {
//            overlayViewUtil.bindServiceCallback = null;
//            overlayViewUtil.mContext.unbindService(overlayViewUtil.serviceConnection);
//            overlayViewUtil.isBound = false;
//        }
//    }
//
//    public static void hide(Context ctx) {
//        Log.d(TAG,"hide");
//        OverlayViewUtil_.getInstance(ctx).startService().bindService(new Runnable() {
//            @Override
//            public void run() {
////                overlayViewUtil.unbindService();
//            }
//        });
//        overlayViewUtil.overlayViewService.hide(true);
//    }
//
//    public static void show(Context ctx) {
////        if ( overlayViewUtil !=null && overlayViewUtil.mContext != null ) {
//        Log.d(TAG,"show");
//        if ( !OverlayViewUtil_.getInstance(ctx).isBound) {
//            OverlayViewUtil_.getInstance(ctx).startService().bindService(new Runnable() {
//                @Override
//                public void run() {
//                    overlayViewUtil.overlayViewService.show();
//                    //                overlayViewUtil.unbindService();
//                }
//            });
//        } else {
//            overlayViewUtil.overlayViewService.show();
//        }
////        } else {
////            OverlayViewUtil.getInstance(ctx).openOverlayViewShowOnly();
////        }
//    }
//
//    public void startNavi(NaviDestination naviDestination, boolean immediate, boolean booted) {
//        startNavi(naviDestination,immediate,booted,false,"",-1,-1);
//    }
//
//    public void startNavi(NaviDestination naviDestination, boolean immediate, boolean booted,boolean refense, String name, float lat, float lon) {
//        startNavi(naviDestination,immediate,booted,refense,name,lat,lon,false);
//    }
//
//    public void startNavi(NaviDestination naviDestination, boolean immediate, boolean booted,boolean refense, String name, float lat, float lon,boolean expandSub) {
//        startNavi(naviDestination,immediate,booted,refense,MapUtil.isOverlayClose(),name,lat,lon,expandSub);
//    }
//    public void startNavi(NaviDestination naviDestination, boolean immediate, boolean booted,boolean refense, boolean closePopup, String name, float lat, float lon,boolean expandSub) {
//        GpsTracker gpsTracker = new GpsTracker(mContext);
//        double myLat = gpsTracker.getLatitude();
//        double myLon = gpsTracker.getLongitude();
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                UsageStatsManager usageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
//                usageStatsManager.isAppInactive("");
//            }
//        }
//
////        getAppPackage().equals(PACKAGE_SKT_MAP)
//        if ( MapUtil.isInstallApp(mContext, MapUtil.getAppPackage()) || !immediate ) {
////            boolean closePopup = MapUtil.isClosePopup();
//            boolean naviAutoStart = MapUtil.isNaviAutoStart();
////            if ( !isTopForTmap(context) ) {
//            if ( MapUtil.isNaving() ) { // navi 실행중 아님 - cancel : false
//                if (!naviAutoStart) {
//                    if (naviDestination.equals(NaviDestination.DRIVE)) naviAutoStart = true;
//                }
//            }
//            if ( hasOverlayPermission() ) {
//                        initIntent()
//                        .addNaviDestination(naviDestination)
//                        .addImmediate(immediate)
//                        .addName(name)
//                        .addLat(lat)
//                        .addLon(lon)
//                        .addClosePopup(closePopup)
//                        .addNaviAutoStart(naviAutoStart)
//                        .addBooted(booted)
//                        .addRefence(refense)
//                        .addExpandSub(expandSub)
//                        .startService()
//                ;
////                    openOverlayView(context, naviDestination, immediate, closepopup, naviAutoStart, booted);
//            } else {
//                if (!naviDestination.equals(NaviDestination.NONE) && ( immediate || naviAutoStart ) ) {
//                    MapUtil.startNaviForExecActivity(mContext,naviDestination);
//                }
//            }
////            } else {
////                if ( isNaviApp() ) Toast.makeText(context, "내비 안내중.", Toast.LENGTH_SHORT).show();
////                else               Toast.makeText(context, "실행중", Toast.LENGTH_SHORT).show();
////            }
//        } else {
////            Toast.makeText(context, "티맵이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
////            getAppPackage().equals(PACKAGE_SKT_MAP);
//            AppInfoDTO appInfo = WApplication.appInfo;
//            if ( MapUtil.getAppPackage().equals(Package.SKT_MAP.getId()) ) {
//                // Utils.toast(context, "티맵이 설치되어 있지 않습니다.");
//                Utils.toast(mContext, R.string.msg_tmap_not_install);
//            } else {
//                // Utils.toast(context, appInfo.name + " 앱이 설치되어 있지 않습니다.");
//                Utils.toast(mContext, mContext.getString(R.string.msg_app_not_install,appInfo.name));
//            }
//            new Thread() {
//                @Override
//                public void run() {
//                    // Uri uri = Uri.parse(tmaptapi.getTMapDownUrl().get(0));
//                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + appInfo.packageName);
//                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    mContext.startActivity(intent);
//                }
//            }.start();
//        }
//    }
//
//
//    public void openOverlayViewShowOnly(boolean expandSub) {
//        startNavi(NaviDestination.NONE,false,false,false,null, -1,-1,expandSub);
//
//    }
//
//    public  void openOverlayViewShowOnlyNotClose() {
//        startNavi(NaviDestination.NONE,false,false,false,false,null, -1,-1,true);
//    }
//    public void openOverlayViewShowOnly() {
//        openOverlayViewShowOnly(false);
//    }
//
//    public boolean hasOverlayPermission() {
//        boolean exec = false;
//        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
//            if (Settings.canDrawOverlays(mContext)) exec = true;
//        } else {
//            exec = true;
//        }
//        return exec;
//    }
//}
//
