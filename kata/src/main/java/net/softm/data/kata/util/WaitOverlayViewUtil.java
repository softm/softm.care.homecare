package net.softm.data.kata.util;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import net.softm.data.kata.WApplication;
import net.softm.data.kata.WaitOverlayViewService;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.common.ReceiverType;
import net.softm.data.kata.kt.Callbackkt;

public class WaitOverlayViewUtil {
    static String TAG = "WaitOverlayViewUtil";
    private Context mContext;
    private static Intent mIntent;
    @SuppressLint("StaticFieldLeak")
    private static WaitOverlayViewUtil waitOverlayViewUtil;
    public WaitOverlayViewUtil(Context ctx)
    {
        mContext = ctx;
    }

    public static WaitOverlayViewUtil init(Context ctx) {
        Fc.i(ctx,TAG,"init");
        if ( waitOverlayViewUtil != null && mIntent != null ) {
            Fc.i(ctx,TAG,"init","stopService");
            stopService();
        }

        waitOverlayViewUtil = new WaitOverlayViewUtil(ctx);
        waitOverlayViewUtil.mContext = ctx;
//        waitOverlayViewUtil.initIntent();
        return waitOverlayViewUtil;
    }
    private WaitOverlayViewService waitOverlayViewService;
    public static boolean isBound = false;
    public static ServiceConnection serviceConnection;

    public WaitOverlayViewUtil initIntent() {
        return initIntent(false);
    }
    private static Callbackkt callbackkt = new Callbackkt(TAG,true);

    public WaitOverlayViewUtil initIntent(Boolean booted) {
        Fc.i(mContext,TAG,"initIntent");
        unbindFromConnectService(mContext);
        bindToConnectService(mContext);
        mIntent = new Intent(mContext, WaitOverlayViewService.class);
        try {
            if (!booted)
                waitOverlayViewUtil.mContext.bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception ex) {
            Fc.e(waitOverlayViewUtil.mContext, TAG,"bindService",ex.toString());
        }
        Fc.i(mContext,TAG,"initIntent","End");
        return this;
    }

    public static void bindToConnectService(Context context) {
        Fc.i(WApplication.mInstance,TAG,"bindToConnectService");
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                WaitOverlayViewService.LocalBinder binder = (WaitOverlayViewService.LocalBinder) service;
                waitOverlayViewUtil.waitOverlayViewService = binder.getService();
                waitOverlayViewUtil.isBound = true;
                Fc.i(WApplication.mInstance,TAG,"bindToConnectService","onServiceConnected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                waitOverlayViewUtil.isBound = false;
                Fc.i(WApplication.mInstance,TAG,"bindToConnectService","onServiceDisconnected");
            }
        };
    }

    public static void unbindFromConnectService(Context context) {
        if (waitOverlayViewUtil.isBound) {
            try {
                context.unbindService(serviceConnection);
                waitOverlayViewUtil.serviceConnection = null;
                waitOverlayViewUtil.isBound = false;
            } catch (Exception e) {
            }
        }
    }

    public WaitOverlayViewUtil addNaviDestination(NaviDestination v) {
        mIntent.putExtra("P_NAVI_DESTINATION",v);
        return this;
    }

    public WaitOverlayViewUtil addImmediate(boolean v) {
        mIntent.putExtra("P_IMMEDIATE",v);
        return this;
    }
    public WaitOverlayViewUtil addName(String v) {
        mIntent.putExtra("P_NAME",v);
        return this;
    }
    public WaitOverlayViewUtil addLat(float v) {
        mIntent.putExtra("P_LAT",v);
        return this;
    }

    public WaitOverlayViewUtil addLon(float v) {
        mIntent.putExtra("P_LON",v);
        return this;
    }

    public WaitOverlayViewUtil addClosePopup(boolean v) {
        mIntent.putExtra("P_CLOSE_POPUP",v);
        return this;
    }

    public WaitOverlayViewUtil addNaviAutoStart(boolean v) {
        mIntent.putExtra("P_NAVI_AUTO_START",v);
        return this;
    }

    public WaitOverlayViewUtil addBooted(boolean v) {
        mIntent.putExtra("P_BOOT",v);
        return this;
    }
    public WaitOverlayViewUtil addExpandSub(boolean v) {
        mIntent.putExtra("P_EXPAND_SUB",v);
        return this;
    }

    public WaitOverlayViewUtil addRefence(boolean v) {
        mIntent.putExtra("P_REFENCE",v);
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
    }
    static Handler checkHandler;

    public static void stopService() {
//        WorkerExeckt.delayCallback((param)->{  // TODO SOFTM : 2024.12.11 : ANR
        Fc.i(WApplication.mInstance, TAG,"stopService");
        if ( waitOverlayViewUtil !=null && waitOverlayViewUtil.mContext != null ) {
            Fc.i(waitOverlayViewUtil.mContext,TAG,"stopService","Step-1");
            if (waitOverlayViewUtil.isBound) { // TODO SOFTM : ANR : 2024.12.11 : ANR과 연관되었을 가능성 있음 isBound는 bindService시 비동기적으로 호출되므로 이 부분을 Enable했을 경우
                                               // TODO SOFTM : ANR : 2024.12.11 : unBindService가 호출되지 않고 다시 bindService가 호출되어 성능에 영향을 주는 것으로 추측됨
                try {
                    waitOverlayViewUtil.mContext.unbindService(waitOverlayViewUtil.serviceConnection);
                } catch (Exception e) {
                    Fc.e(waitOverlayViewUtil.mContext, TAG,"startService","ex");
                }
                waitOverlayViewUtil.isBound = false;
            }
            if ( mIntent != null ) {
                waitOverlayViewUtil.mContext.stopService(mIntent);
            }
        }
//            return null;
//        });
    }

    public static void update(String action, ReceiverType receiverType) {
        if ( waitOverlayViewUtil != null && waitOverlayViewUtil.waitOverlayViewService != null ) {
            waitOverlayViewUtil.waitOverlayViewService.handleShowWaitingOverlayUpdateStatus(action, receiverType);
        }
    }

    public void show(
            String gubun, String action, ReceiverType receiverType
    ) {
        if ( PermissionUtil.hasOverlayPermissions(mContext) ) {
            initIntent()
                    .addGubun(gubun)
                    .addAction(action)
                    .addReceiverType(receiverType)
                    .startService();
        }
    }
    public void turnOnScreen(
    ) {
        if ( PermissionUtil.hasOverlayPermissions(mContext) ) {
            initIntent()
                    .addGubun(AcsUtil.TURN_ON_SCREEN)
                    .startService();
        }
    }


    public void show(
            String gubun, String action, ReceiverType receiverType, long waitingSec, boolean adviewUserEarnedReward, boolean doAppReview
    ) {
        if ( PermissionUtil.hasOverlayPermissions(mContext) ) {
            initIntent()
                    .addGubun(gubun)
                    .addAction(action)
                    .addReceiverType(receiverType)
                    .addWaitingSec(waitingSec)
                    .addAdviewUserEarnedReward(adviewUserEarnedReward)
                    .addDoAppReview(doAppReview)
                    .startService();
        }
    }

    private WaitOverlayViewUtil addDoAppReview(boolean v) {
        mIntent.putExtra("P_DO_APP_REVIEW",v);
        return this;
    }

    private WaitOverlayViewUtil addAdviewUserEarnedReward(boolean v) {
        mIntent.putExtra("P_ADVIEW_USER_EARNED_REWARD",v);
        return this;
    }

    private WaitOverlayViewUtil addWaitingSec(long v) {
        mIntent.putExtra("P_WAITING_SEC",v);
        return this;
    }

    private WaitOverlayViewUtil addReceiverType(ReceiverType v) {
        mIntent.putExtra("P_RECEIVER_TYPE",v);
        return this;
    }

    private WaitOverlayViewUtil addAction(String v) {
        mIntent.putExtra("P_ACTION",v);
        return this;
    }
    private WaitOverlayViewUtil addGubun(String v) {
        mIntent.putExtra("P_GUBUN",v);
        return this;
    }

    /** 앱 업데이트 타임 : 서버의 basic_info.json에서 값이 할당됨, index.html에서 업데이트 시간을 표시하기 위해 이용됨 */
    public static long getWaitTimeForNotAdLicense() {
        try {
            return WApplication.basicInfoDTO.waitTimeForNotAdLicense;
        } catch (Exception ex) {
            return 3000L;
        }
    }

}

