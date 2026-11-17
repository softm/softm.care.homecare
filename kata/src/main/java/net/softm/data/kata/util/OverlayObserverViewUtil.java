package net.softm.data.kata.util;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import net.softm.data.kata.OverlayObserverViewService;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.kt.Callbackkt;

public class OverlayObserverViewUtil {
    public static final int NOTIFICATION_ID = 11;
    static String TAG = "OverlayObserverViewUtil";
    private Context mContext;
    private static Intent mIntent;
    private static OverlayObserverViewUtil overlayObserverViewUtil;
    public OverlayObserverViewUtil(Context ctx)
    {
        mContext = ctx;
    }

    public static OverlayObserverViewUtil init(Context ctx) {
        Fc.i(ctx,TAG,"init");
        if ( overlayObserverViewUtil != null && mIntent != null ) {
            Fc.i(ctx,TAG,"init","stopService");
            stopService();
        }

        overlayObserverViewUtil = new OverlayObserverViewUtil(ctx);
        overlayObserverViewUtil.mContext = ctx;
//        overlayObserverViewUtil.initIntent();
        return overlayObserverViewUtil;
    }
    private OverlayObserverViewService overlayObserverViewService;
    public static boolean isBound = false;

    private static Callbackkt callbackkt = new Callbackkt(TAG,true);

    public OverlayObserverViewUtil initIntent() {
        Fc.i(mContext,TAG,"initIntent");
        mIntent = new Intent(mContext, OverlayObserverViewService.class);
        Fc.i(mContext,TAG,"initIntent","End");
        return this;
    }

    public OverlayObserverViewUtil addNaviDestination(NaviDestination v) {
        mIntent.putExtra("P_NAVI_DESTINATION",v);
        return this;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public void start() {
        initIntent()
        .startService();
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
    }
    static Handler checkHandler;

    public static final String STOP_SERVICE = "STOP_SERVICE";
    public static void stopService() {
//        WorkerExeckt.delayCallback((param)->{  // TODO SOFTM : 2024.12.11 : ANR
        Fc.i(WApplication.mInstance, TAG,"stopService");
        if ( overlayObserverViewUtil !=null && overlayObserverViewUtil.mContext != null ) {
            Fc.i(overlayObserverViewUtil.mContext,TAG,"stopService","Step-1");
            if ( mIntent != null ) {
                overlayObserverViewUtil.mContext.stopService(mIntent);
//                overlayObserverViewUtil.mIntent.setAction(STOP_SERVICE);
//                if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O) {
//                    overlayObserverViewUtil.mContext.startForegroundService(mIntent);
//                } else{
//                    overlayObserverViewUtil.mContext.startService(mIntent);
//                }
            }
        }
//            return null;
//        });
    }

}

