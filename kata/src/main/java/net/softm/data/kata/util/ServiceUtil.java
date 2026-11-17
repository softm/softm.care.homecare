package net.softm.data.kata.util;


import android.content.Context;
import android.content.Intent;
import android.os.Build;

import net.softm.data.kata.ConnectService;
import net.softm.data.kata.R;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.common.NaviStatus;

public class ServiceUtil {
    private static final String TAG = "ServiceUtil";
    private Context mContext;
    public static Intent connectServiceIntent = null;

    private static ServiceUtil serviceUtil;
    public ServiceUtil(Context ctx)
    {
        mContext = ctx;
    }

    public static void stop() {
        if ( serviceUtil != null && serviceUtil.connectServiceIntent != null ) {
            Fc.i(serviceUtil.mContext, TAG,"stop");
            serviceUtil.stopService();
        }
    }

    public static void destory() {
        serviceUtil.connectServiceIntent = null;
    }

    public static ServiceUtil init(Context ctx) {
        stop();

        serviceUtil = new ServiceUtil(ctx);
        serviceUtil.mContext = ctx;
        serviceUtil.connectServiceIntent = null;
        Fc.i(serviceUtil.mContext, TAG,"init");
        return serviceUtil;
    }
    public static Intent getConnectServiceIntent() {
        return serviceUtil.connectServiceIntent;
    }

    public void startService(String notificationDetails, NaviStatus naviStatus, boolean bootStartNavi) {
        Fc.i(mContext,TAG,"startService");

//        serviceUtil.mContext.startService(mIntent);
//        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O) {
//            mOpenOverlayView.mContext.startForegroundService(mIntent);
//        } else{
//            mOpenOverlayView.mContext.startService(mIntent);
//        }
        MapUtil.isNotiIcon();
        MapUtil.isOverlayOpen();
        if (!Utils.hasPermissionForBasic(mContext)) {
            return;
        }

        if ( bootStartNavi ) {
            Utils.toast(mContext, R.string.app_boot_start); // Boot
            NaviDestination naviDestination = MapUtil.getNaviDestinationInfo();
            OverlayViewUtil.init(mContext).startNavi(naviDestination, false, true);
        } else {
//            if ( MapUtil.isOverlayOpen() ) {
//                OverlayViewUtil.init(mContext).startService();
//            }
        }

//        if ( MapUtil.isNotiIcon() ) {
        if (MapUtil.isStart()) {
//              WApplication.mInstance.clearGeofences(); // call startConnectService()

            connectServiceIntent = new Intent(mContext, ConnectService.class);
            connectServiceIntent.putExtra("P_NAVI_DETAILS", notificationDetails);
            connectServiceIntent.putExtra("P_NAVI_STATUS" , naviStatus);
            connectServiceIntent.putExtra("P_BOOT_COMPLETED_START_NAVI", bootStartNavi);
            connectServiceIntent.putExtra("P_REGISTER_RECEIVER", true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(connectServiceIntent);
            } else {
                mContext.startService(connectServiceIntent);
            }
            Fc.i(mContext,TAG,"startService");
        }
//        }
    }
    public static boolean isStartAble() {
        return MapUtil.isStart() && MapUtil.isAppEnable() ;
    }

    public void startServiceFromWorker() {
        if ( isStartAble() ) {
            connectServiceIntent = new Intent(mContext, ConnectService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(connectServiceIntent);
            } else {
                mContext.startService(connectServiceIntent);
            }
            Fa.i(mContext,TAG, Fa.EVENT_START_CONNECT_SERVICE_FROM_WORKER);
            Fc.i(mContext,TAG,"startService");
        }
    }
    public void stopService() {
        if ( connectServiceIntent != null ) {
            serviceUtil.mContext.stopService(connectServiceIntent);
            serviceUtil.connectServiceIntent = null;
        }
    }

    public static boolean isLive() {
        return ServiceUtil.getConnectServiceIntent()!=null;
//        return ConnectService.isInstanceCreated();
    }

    public static boolean isDestory() {
        return !isLive();
//        return ConnectService.isInstanceCreated();
    }

}

