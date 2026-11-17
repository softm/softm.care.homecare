package net.softm.data.kata.job;


import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.getAction;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import net.softm.data.kata.MainConnectReceiver;
import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.common.StartMode;
import net.softm.data.kata.dto.StartStopOnOffVO;
import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.DeviceUtil;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.Utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkExecutorService {

    public static final String TAG = WorkExecutorService.class.getSimpleName();

    private static Context mContext = null;

    public static BlockingQueue<Runnable> queue = null;
    private static AcsUtil acsUtil;

    public WorkExecutorService() {
    }

    public static void init(Context context) {
        mContext = context;
        acsUtil = new AcsUtil(mContext);
    }


    protected static void execOnOff() {
        boolean isStart = getAction().equals(ACTION_SOFTM_POWER_CONNECTED) ||
                getAction().equals(ACTION_SOFTM_ACL_CONNECTED) ||
                getAction().equals(ACTION_SOFTM_WIFI_CONNECTED);
        boolean isStop = !isStart;

        StartStopOnOffVO ss = MapUtil.getStartStopOnOff();
        String msgOnOff =  isStart?mContext.getString(R.string.label_on):mContext.getString(R.string.label_off);

        StartStopOnOffVO.StartUseYnOnOff startStop = null;

        if (
                ( isStart && ss.getBluetooth().getStart().isUseYn() ) ||
                        ( isStop  && ss.getBluetooth().getStop().isUseYn() )
        ) {
            startStop = isStart?ss.getBluetooth().getStart():ss.getBluetooth().getStop();
            DeviceUtil.bluetoothEnable(mContext,startStop.getEnabled());
            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_bluetooth),msgOnOff));
        }

        if (
                ( isStart && ss.getWifi().getStart().isUseYn() ) ||
                        ( isStop  && ss.getWifi().getStop().isUseYn() )
        ) {
            startStop = isStart?ss.getWifi().getStart():ss.getWifi().getStop();
            DeviceUtil.wifiEnable(mContext,startStop.getEnabled());
            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_wifi),msgOnOff));
        }

//        "bluetooth:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//                "wifi:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//                "tethering:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//                "airplaneMode:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//                "screen_on:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//                "unlock:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//                "play_music:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//                "home_screen:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
//                "power_off:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}}"
//

        if (
                ( isStart && ss.getTethering().getStart().isUseYn() ) ||
                        ( isStop  && ss.getTethering().getStop().isUseYn() )
        ) {
            startStop = isStart?ss.getTethering().getStart():ss.getTethering().getStop();
            DeviceUtil.tetheringEnable(mContext,startStop.getEnabled(),false);
            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_tethering),msgOnOff));
        }

        if (
                ( isStart && ss.getAirplaneMode().getStart().isUseYn() ) ||
                        ( isStop  && ss.getAirplaneMode().getStop().isUseYn() )
        ) {
            startStop = isStart?ss.getAirplaneMode().getStart():ss.getAirplaneMode().getStop();
            DeviceUtil.airplaneModeEnable(mContext,startStop.getEnabled());
            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_airplane_mode),msgOnOff));
        }

        if (
                ( isStart && ss.getScreenOn().getStart().isUseYn() ) ||
                        ( isStop  && ss.getScreenOn().getStop().isUseYn() )
        ) {
            startStop = isStart?ss.getScreenOn().getStart():ss.getScreenOn().getStop();
//            DeviceUtil.screenOnEnable(mContext,startStop.getEnabled(),false);
//            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_screen_on),msgOnOff));
        }

        if (
                ( isStart && ss.getLock().getStart().isUseYn() ) ||
                        ( isStop  && ss.getLock().getStop().isUseYn() )
        ) {
            startStop = isStart?ss.getLock().getStart():ss.getLock().getStop();
//            DeviceUtil.lockEnable(mContext,startStop.getEnabled(),false);
//            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_lock),msgOnOff));
        }

        if (
                ( isStart && ss.getPlayMusic().getStart().isUseYn() ) ||
                ( isStop  && ss.getPlayMusic().getStop().isUseYn() )
        ) {
            startStop = isStart?ss.getPlayMusic().getStart():ss.getPlayMusic().getStop();
            DeviceUtil.playMusicEnable(mContext,startStop.getEnabled(),false);
            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_play_music),msgOnOff));
        }

        if (
                ( isStart && ss.getHomeScreen().getStart().isUseYn() ) ||
                        ( isStop  && ss.getHomeScreen().getStop().isUseYn() )
        ) {
            startStop = isStart?ss.getHomeScreen().getStart():ss.getPlayMusic().getStop();
            DeviceUtil.goHomeScreen(mContext,false);
            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_home_screen),msgOnOff));
        }

        if (
//             ( isStart && ss.getPowerOff().getStart().isUseYn() ) ||
                ( isStop  && ss.getPowerOff().getStop().isUseYn() )
        ) {
            startStop = isStart?ss.getPowerOff().getStart():ss.getPlayMusic().getStop();
            DeviceUtil.powerOff(mContext,false);
            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_power_off),msgOnOff));
        }


    }

    public static void start() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            execOnOff();
            String threadName = Thread.currentThread().getName();
            Log.d(TAG,"Job - execOnOff " + threadName);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });

        if ( !MapUtil.getNaviAutoStart().equals(StartMode.NONE) ) {
            executor.submit(() -> {
                if ( !MapUtil.getNaviAutoStart().equals(StartMode.NONE)) {
                    try {
                        if (MapUtil.isNaving()) {
                            // if (MapUtil.isNavingForNotiListener()) {
                            Utils.toast(mContext, mContext.getString(R.string.msg_app_is_running, WApplication.appInfo.name));
                            // Utils.toast(context, context.getString(R.string.msg_tmap_is_running));
                        } else {
                            Handler mHandler = new Handler(Looper.getMainLooper());
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
//                                            MapUtil.setNotNaving();
                                    NaviDestination naviDestination = MapUtil.getNaviDestinationInfo();
                                    if (OverlayViewUtil.init(mContext).hasOverlayPermission()) {
                                        //                                      if ( MapUtil.isPopup() ) {
                                        OverlayViewUtil.init(mContext).startNavi(naviDestination,false,false);
                                        //MapUtil.getInstanceForTmap(context).invokeGoHome();
                                    } else {
                                        MapUtil.startNaviForExecActivity(mContext, naviDestination, true);
                                    }
                                }
                            }, Const.POWER_CONNECTED_START_NAVI_DELAY_TIME);

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            });
        }

        executor.submit(() -> {
            MainConnectReceiver.stopToShowOverlay();
        });

        executor.shutdown();
    }

    public static void stop() {
        ExecutorService executor = Executors.newSingleThreadExecutor();


        if ( !MapUtil.getNaviAutoStart().equals(StartMode.NONE) ) {
            executor.submit(() -> {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        acsUtil.forceStop();
                    }
                }, 0);
                String threadName = Thread.currentThread().getName();
                Log.d(TAG,"Job - forceStop" + threadName);
            });
        }

        executor.submit(() -> {
            execOnOff();
            String threadName = Thread.currentThread().getName();
            Log.d(TAG,"Job - execOnOff " + threadName);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        executor.submit(() -> {
            MainConnectReceiver.stopToShowOverlay();
        });
//
//            executor.submit(() -> {
//                String threadName = Thread.currentThread().getName();
//                System.out.println("Job3 " + threadName);
//            });
//            executor.submit(() -> {
//                String threadName = Thread.currentThread().getName();
//                System.out.println("Job4 " + threadName);
//            });

        executor.shutdown();
//            try {
//                executor.awaitTermination(20, TimeUnit.SECONDS);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
    }

}