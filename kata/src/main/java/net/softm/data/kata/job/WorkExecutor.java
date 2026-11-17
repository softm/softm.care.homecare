package net.softm.data.kata.job;


import android.content.Context;
import android.os.Build;

import net.softm.data.kata.MainConnectReceiver;
import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.common.StartMode;
import net.softm.data.kata.common.StopMode;
import net.softm.data.kata.dto.StartStopOnOffVO;
import net.softm.data.kata.kt.Callkt;
import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.DeviceUtil;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.PermissionUtil;
import net.softm.data.kata.util.Utils;

import java.util.concurrent.BlockingQueue;

public class WorkExecutor {

    public static final String TAG = WorkExecutor.class.getSimpleName();

    private static Context mContext = null;

    public static BlockingQueue<Runnable> queue = null;
    private static AcsUtil acsUtil;

    private static int TASK_INTERVAL_SEC = 1;

    public WorkExecutor() {
    }
    public static boolean isExecuting = false;

    public static void init(Context context) {
        Fc.i(context,TAG,"init");
        mContext = context;
        acsUtil = new AcsUtil(mContext);
        isExecuting = true;
        try {
            if ( !MapUtil.isTest() ) {
                if (WApplication.mainActivity != null) {
                    WApplication.mainActivity.finishAffinity();
                }
            }
        } catch (Exception ex) {
            Fc.i(context,TAG,"init","ex", " : WApplication.mainActivity.finishAffinity");
        }
    }

    protected static void addTaskForType() {
    }
    protected static void execOnOff() {
        boolean isStart = MainConnectReceiver.isStartAction();
//        boolean isStop = !isStart;
        execOnOff(isStart);
    }
    protected static void execOnOff(boolean start) {
        Fc.i(mContext,TAG,"execOnOff");
        boolean isStart = start;
        boolean isStop = !start;
        StartStopOnOffVO ss = MapUtil.getStartStopOnOff();
        StartStopOnOffVO.StartUseYnOnOff startStopAirplaneMode = isStart ? ss.getAirplaneMode().getStart() : ss.getAirplaneMode().getStop();
        /** Home Screen */
        // 시작시만 execOnOffLast 에서 종료시 homeScreen처리
        if (
                ( isStart && ss.getHomeScreen().getStart().isUseYn() )
//                        || ( isStop  && ss.getHomeScreen().getStop().isUseYn() )
        ) {
            StartStopOnOffVO.StartUseYnOnOff startStop = isStart ? ss.getHomeScreen().getStart() : ss.getHomeScreen().getStop();
            String msgOnOff =  startStop.getEnabled()?mContext.getString(R.string.label_on):mContext.getString(R.string.label_off);
            String msg = mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_home_screen),msgOnOff);
            if ( startStop.isUseYn() && startStop.getEnabled() ) { // 홈화면 On 동작만 싫행
                WorkQueue.addTask(new Runnable() {
                    @Override
                    public void run() {
                        Fc.i(mContext,TAG,"execOnOff", "ACS - ", "WE : " + msg);
//                        Utils.sleepMicoSeconds(500); // TODO SOFTM ACS delay Point : 앱실행 카운트 다운과 홈으로 이동하는 타이밍에 UI 에러가 나는 듯함.
                        DeviceUtil.goHomeScreen(mContext, true);
                        toast(mContext, msg,true);
                        //
                        //                    Utils.sleepForWorkExecutor();
//                        WorkQueue.nextTask();
                    }
                });
            }
        }

        /** AirplaneMode */
        if (
                ( isStart && ss.getAirplaneMode().getStart().isUseYn() )
        ) {
            WorkQueue.addTask(new Runnable() {
                @Override
                public void run() {
//                    StartStopOnOffVO.StartUseYnOnOff startStopAirplaneMode = isStart ? ss.getAirplaneMode().getStart() : ss.getAirplaneMode().getStop();
                    String msgOnOff =  startStopAirplaneMode.getEnabled()?mContext.getString(R.string.label_on):mContext.getString(R.string.label_off);
                    String msg = mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_airplane_mode),msgOnOff);

                    Fc.i(mContext,TAG,"execOnOff", String.format("AirplaneMode.%s", msg));
                    DeviceUtil.airplaneModeEnable(mContext,startStopAirplaneMode.getEnabled(),true);
                    Fc.i(mContext,TAG,"execOnOff", "ACS - ","WE : " + msg);
                    toast(mContext, msg,true);
                }
            });
        }

        if ( (isStart && !MapUtil.isNaving()) || isStop) {
            if (
                    ( isStart && ss.getBluetooth().getStart().isUseYn() ) ||
                    ( isStop  && ss.getBluetooth().getStop().isUseYn() )
            ) {
                // Bluetooth
                StartStopOnOffVO.StartUseYnOnOff startStopBluetooth = isStart ? ss.getBluetooth().getStart() : ss.getBluetooth().getStop();
                // 비행기모드를 활성화하면서 bluetooth끄는 동작인 경우 실행안함.
                boolean bExec = !(startStopAirplaneMode.getEnabled() && ss.getAirplaneMode().getStart().isUseYn()
                        && !startStopBluetooth.getEnabled());
                bExec = true; /** 위 로직을 적용안함 */
                if ( bExec ) {
                    WorkQueue.addTask(new Runnable() {
                        @Override
                        public void run() {
                            String msgOnOff = startStopBluetooth.getEnabled() ? mContext.getString(R.string.label_on) : mContext.getString(R.string.label_off);
                            String msg = mContext.getString(R.string.msg_startstop_onoff, mContext.getString(R.string.label_bluetooth), msgOnOff);
                            Fc.i(mContext,TAG,"execOnOff", String.format("Bluetooth.%s", msg));
                            DeviceUtil.bluetoothEnable(mContext,startStopBluetooth.getEnabled(),true);
                            Fc.i(mContext,TAG,"execOnOff", "ACS - ", "WE : " + msg);
                            toast(mContext, msg,true);
                        }
                    });
                }
            }

            /** Tethering */
            if (
                    ( isStart && ss.getTethering().getStart().isUseYn() ) ||
                    ( isStop  && ss.getTethering().getStop().isUseYn() )
            ) {

                StartStopOnOffVO.StartUseYnOnOff startStopTethering = isStart ? ss.getTethering().getStart() : ss.getTethering().getStop();

                // 비행기모드를 활성화하면서 tethering끄는 동작인 경우 실행안함.
                if ( !(startStopAirplaneMode.getEnabled() && ss.getAirplaneMode().getStart().isUseYn() && !startStopTethering.getEnabled()) ) {
                    WorkQueue.addTask(new Runnable() {
                        @Override
                        public void run() {
                            String msgOnOff =  startStopTethering.getEnabled()?mContext.getString(R.string.label_on):mContext.getString(R.string.label_off);
                            String msg = mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_tethering),msgOnOff);
                            Fc.i(mContext,TAG,"execOnOff",String.format("Tethering.%s", msg));
                            DeviceUtil.tetheringEnable(mContext,startStopTethering.getEnabled(),true);
                            Fc.i(mContext,TAG,"execOnOff", "ACS - ","WE : " + msg);
                            toast(mContext, msg,true);
                        }
                    });
                }
            }

            /** Wifi */
            if (
                    ( isStart && ss.getWifi().getStart().isUseYn() ) ||
                            ( isStop  && ss.getWifi().getStop().isUseYn() )
            ) {
                StartStopOnOffVO.StartUseYnOnOff startStopWifi = isStart ? ss.getWifi().getStart() : ss.getWifi().getStop();

                // 비행기모드를 활성화하면서 wifi끄는 동작인 경우 실행안함.
                if ( !(startStopAirplaneMode.getEnabled() && ss.getAirplaneMode().getStart().isUseYn() && !startStopWifi.getEnabled()) ) {
                    WorkQueue.addTask(new Runnable() {
                        @Override
                        public void run() {
                            String msgOnOff = startStopWifi.getEnabled() ? mContext.getString(R.string.label_on) : mContext.getString(R.string.label_off);
                            String msg = mContext.getString(R.string.msg_startstop_onoff, mContext.getString(R.string.label_wifi), msgOnOff);
                            Fc.i(mContext,TAG,"execOnOff",String.format("Wifi.%s", msg));
                            DeviceUtil.wifiEnable(mContext, startStopWifi.getEnabled(), true);
                            Fc.i(mContext,TAG,"execOnOff", "ACS - ", "WE : " + msg);
                            toast(mContext, msg,true);
                        }
                    });
                }
            }
        }

//        StartStopOnOffVO.StartUseYnOnOff startStop = null;

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


        /** AirplaneMode */
        if (
                ( isStop  && ss.getAirplaneMode().getStop().isUseYn() )
        ) {
            WorkQueue.addTask(new Runnable() {
                @Override
                public void run() {
                    StartStopOnOffVO.StartUseYnOnOff startStop = isStart ? ss.getAirplaneMode().getStart() : ss.getAirplaneMode().getStop();
                    String msgOnOff =  startStop.getEnabled()?mContext.getString(R.string.label_on):mContext.getString(R.string.label_off);
                    String msg = mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_airplane_mode),msgOnOff);
                    Fc.i(mContext,TAG,"execOnOff",String.format("AirplaneMode.%s", msg));
                    DeviceUtil.airplaneModeEnable(mContext,startStop.getEnabled(),true);
                    Fc.i(mContext,TAG,"execOnOff", "ACS - ","WE : " + msg);
                    toast(mContext, msg,true);
                }
            });
        }

    }

    protected static void execOnOffLast(boolean start) {
        boolean isStart = start;
        boolean isStop = !start;
        Fc.i(mContext,TAG,"execOnOffLast");
        StartStopOnOffVO ss = MapUtil.getStartStopOnOff();

        /** Home Screen */
        if (
//                ( isStart && ss.getHomeScreen().getStart().isUseYn() ) ||
                ( isStop  && ss.getHomeScreen().getStop().isUseYn() )
        ) {
            StartStopOnOffVO.StartUseYnOnOff startStop = isStart ? ss.getHomeScreen().getStart() : ss.getHomeScreen().getStop();
            String msgOnOff =  startStop.getEnabled()?mContext.getString(R.string.label_on):mContext.getString(R.string.label_off);
            String msg = mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_home_screen),msgOnOff);
            if ( startStop.isUseYn() && startStop.getEnabled() ) { // 홈화면 On 동작만 싫행
                WorkQueue.addTask(new Runnable() {
                    @Override
                    public void run() {
                        Fc.i(mContext,TAG,"execOnOffLast", "ACS - ", "WE : " + msg);
//                        Utils.sleepMicoSeconds(500); // TODO SOFTM ACS delay Point : 앱실행 카운트 다운과 홈으로 이동하는 타이밍에 UI 에러가 나는 듯함.
                        DeviceUtil.goHomeScreen(mContext, true);
                        toast(mContext, msg,true);
                        //
                        //                    Utils.sleepForWorkExecutor();
//                        WorkQueue.nextTask();
                    }
                });
            }
        }

        /** Screen */
        if (
                ( isStart && ss.getScreenOn().getStart().isUseYn() ) ||
                ( isStop  && ss.getScreenOn().getStop().isUseYn() )
        ) {
            StartStopOnOffVO.StartUseYnOnOff startStop = isStart ? ss.getScreenOn().getStart() : ss.getScreenOn().getStop();
            String msgOnOff =  startStop.getEnabled()?mContext.getString(R.string.label_on):mContext.getString(R.string.label_off);
            String msg = mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_screen_on),msgOnOff);
            if ( startStop.isUseYn() && !startStop.getEnabled() ) { // 화면을 끄는 동작만 싫행, 화면 켜는 동작은 스킵 ( ConnectReceiver 수신 진입단계에서 수행 )
                WorkQueue.addTask(new Runnable() {
                    @Override
                    public void run() {
                        Fc.i(mContext,TAG,"execOnOffLast",String.format("Screen.%s", msg));
                        DeviceUtil.screenOff(mContext,true);
                        Fc.i(mContext,TAG,"execOnOffLast", "ACS - ","WE : " + msg);
                        toast(mContext, msg,true);
//                        WorkQueue.nextTask();
                    }
                });
            }
        }

        /** Lock */
        if (
                ( isStart && ss.getLock().getStart().isUseYn() ) ||
                ( isStop  && ss.getLock().getStop().isUseYn() )
        ) {
            StartStopOnOffVO.StartUseYnOnOff startStop = isStart ? ss.getLock().getStart() : ss.getLock().getStop();
            String msgOnOff =  startStop.getEnabled()?mContext.getString(R.string.label_on):mContext.getString(R.string.label_off);
            String msg = mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_lock),msgOnOff);
            if ( startStop.isUseYn() && startStop.getEnabled() ) { // 락을 켜는(on) 동작만 실행, 락을 푸는(off) 동작은 스킵 ( ConnectReceiver 수신 진입단계에서 수행 )
                WorkQueue.addTask(new Runnable() {
                    @Override
                    public void run() {
                        Fc.i(mContext,TAG,"execOnOffLast",String.format("Lock.%s", msg));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            toast(mContext, msg,true);
                        } else {
                            toast(mContext, mContext.getString(R.string.msg_limit_lower_version,mContext.getString(R.string.label_lock)),true);
                            Fc.i(mContext,TAG,"execOnOffLast", "ACS - ","lockScreen.not.support.nextTask");
                        }
                        DeviceUtil.lockScreen(mContext,true);
                        Fc.i(mContext,TAG,"execOnOffLast", "ACS - ","WE : " + msg);
                    }
                });
            }
        }

        /** Completed Lock */
        if (
                ( isStart && ss.getCompletedLock().getStart().isUseYn() ) ||
                ( isStop  && ss.getCompletedLock().getStop().isUseYn() )
        ) {
            StartStopOnOffVO.StartUseYnOnOff lockStartStop = isStart ? ss.getLock().getStart() : ss.getLock().getStop();
            StartStopOnOffVO.StartUseYnOnOff completedLockstartStop   = isStart ? ss.getCompletedLock().getStart() : ss.getCompletedLock().getStop();
            // lock 해제일경우만 --> completedLock의 잠금만을 실행하는 조건.
            if ( isStop &&  (!lockStartStop.isUseYn() || ( lockStartStop.isUseYn() && lockStartStop.getEnabled() != completedLockstartStop.getEnabled() )) ) {
                WorkQueue.addTask(new Runnable() {
                    @Override
                    public void run() {
                        String msgOnOff = completedLockstartStop.getEnabled() ? mContext.getString(R.string.label_on) : mContext.getString(R.string.label_off);
                        String msg = mContext.getString(R.string.msg_startstop_onoff, mContext.getString(R.string.label_completed_lock), msgOnOff);
                        Fc.i(mContext,TAG,"execOnOffLast",String.format("Completed Lock.%s", msg));
                        if (completedLockstartStop.getEnabled()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                toast(mContext, msg,true);
                                DeviceUtil.lockScreen(mContext, true);
                            } else {
                                toast(mContext, mContext.getString(R.string.msg_limit_lower_version,mContext.getString(R.string.label_lock)),true);
                                Fc.i(mContext,TAG,"execOnOffLast", "ACS - ","lockScreen.not.support");
                                WorkQueue.nextTask();
                            }
                        } else {
                            DeviceUtil.unlockScreen(mContext, true);
                            toast(mContext, msg,true);
                        }
                        Fc.i(mContext,TAG,"execOnOffLast", "ACS - ", "WE : " + msg);
                    }
                });
            }
        }

        /** Music **/
        if (
                ( isStart && ss.getPlayMusic().getStart().isUseYn() ) ||
                ( isStop  && ss.getPlayMusic().getStop().isUseYn() )
        ) {
            WorkQueue.addTask(new Runnable() {
                @Override
                public void run() {
                    StartStopOnOffVO.StartUseYnOnOff startStop = isStart ? ss.getPlayMusic().getStart() : ss.getPlayMusic().getStop();
                    String msgOnOff =  startStop.getEnabled()?mContext.getString(R.string.label_on):mContext.getString(R.string.label_off);
                    String msg = mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_play_music),msgOnOff);
                    if (WApplication.musicAppInfo.getDetail() != null) {
                        long musicEnableDelay = isStart?DeviceUtil.getDelayMusicEnable():0;
                        if (!MapUtil.getNaviAutoStart().equals(StartMode.NONE)) {
                            if ( isStart ) {
                                if ( !WApplication.appUpdateAvailable ) {
                                    MapUtil.runApp(mContext, WApplication.musicAppInfo.getPackageName());
                                }
                            }
                         // (new Callbackkt(musicEnableDelay,"WorkExecutor.Music", true)).run((params) -> {
                            (new Callkt("WorkExecutor.Music")).runIO((param)->{
                             // Fc.i(mContext,TAG,"execOnOff", "ACS - ","WE : " + msg);
                                Fc.i(mContext,TAG,"execOnOffLast",String.format("Music.%s", msg));
                                DeviceUtil.playMusicEnable(mContext, startStop.getEnabled(),true);
                                Fc.i(mContext,TAG,"execOnOffLast", "ACS - ","WE : " + msg);
                                toast(mContext, msg,true);
                                WorkQueue.nextTask();
                                return null;
                            });
                        }
                    } else {
                     // Fc.i(mContext,TAG,"execOnOff", "ACS - ","WE : " + msg);
                        Fc.i(mContext,TAG,"execOnOffLast",String.format("Music.%s", msg));
                        DeviceUtil.playMusicEnable(mContext, startStop.getEnabled(),true);
                        Fc.i(mContext,TAG,"execOnOffLast", "ACS - ","WE : " + msg);
                        toast(mContext, msg,true);
                        WorkQueue.nextTask();
                    }
                }
            });
        }

        /** Power Off : not implement */
        if (
             // ( isStart && ss.getPowerOff().getStart().isUseYn() ) ||
                ( isStop  && ss.getPowerOff().getStop().isUseYn() )
        ) {
            StartStopOnOffVO.StartUseYnOnOff startStop = isStart ? ss.getPowerOff().getStart() : ss.getPowerOff().getStop();
            String msgOnOff =  startStop.getEnabled()?mContext.getString(R.string.label_on):mContext.getString(R.string.label_off);
            String msg = mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_power_off),msgOnOff);
            if ( startStop.isUseYn() && startStop.getEnabled() ) { // 홈화면 On 동작만 싫행
                WorkQueue.addTask(new Runnable() {
                    @Override
                    public void run() {
                        Fc.i(mContext,TAG,"execOnOffLast",String.format("Power Off.%s", msg));
                        DeviceUtil.powerOff(mContext,true);
                        toast(mContext, msg,true);
                        Fc.i(mContext,TAG,"execOnOffLast", "ACS - ", "WE : " + msg);
                        WorkQueue.nextTask();
                    }
                });
            }
        }

    }

    public static void start() {
        start(true);
    }
    public static void start(boolean exec) {
        start(exec,null);
    }
    public static void start(boolean exec,Runnable runnable) {
        Fc.i(mContext,TAG,"execOnOff","WorkExecutor.start");
//TODO SOFTM activity를 이용해서 호출
//        if ( exec) {
//            MapUtil.execWorkerActivity(mContext,"start",null,null,null,null,null);
//            return;
//        }
        WorkQueue.init();
        execOnOff(true);

        WorkQueue.addTask(new Runnable() {
            @Override
            public void run() {
                Fc.i(mContext,TAG,"start",String.format("runNavi-%s", MapUtil.getNaviAutoStart().name()));
                if (!MapUtil.getNaviAutoStart().equals(StartMode.NONE)) {
                    try {
                        /** 종료모드가 설정이 되어있고, 접근성도 활성화된 상태여야, 종료동작을 질행해, Naving 플래그를 클리어했을 것이므로
                         * 앱이 실행중인 상태를 방어하지만, 그 외 상황에서는 runNavi 실행
                         * 위처럼 생각할 수 있지만
                         * 종료 루틴에서 setNotNaving을 하고, isNotNavig조건에 시작후 5분이상 지나면 NotNaving으로 판단하므로 기본 조건만 유지 ( MapUtil.isNaving() )
                         * */
                     // if (MapUtil.isNaving() && !StopMode.NONE.equals(MapUtil.getStopMode()) && PermissionUtil.isAccessibilityServiceRunning(mContext)) {
                        if (MapUtil.isNaving() ) {
                            // if (MapUtil.isNavingForNotiListener()) {
                            Utils.toast(mContext, mContext.getString(R.string.msg_app_is_running, WApplication.appInfo.name));
                            // Utils.toast(context, context.getString(R.string.msg_tmap_is_running));
                        } else {
//                            Handler mHandler = new Handler(Looper.getMainLooper());
//                            mHandler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                            Callbackkt callbackkt = new Callbackkt(Const.POWER_CONNECTED_START_NAVI_DELAY_TIME,"WorkQueue.addTask.OverlayViewUtil.init(mContext).startNavi(naviDestination,false,false);");
//                            callbackkt.run((param)->{
                                    NaviDestination naviDestination = MapUtil.getNaviDestinationInfo();
                                    if (OverlayViewUtil.hasOverlayPermission()) {
                                        // if ( MapUtil.isPopup() ) {
//                                        Utils.sleepMicoSeconds(200); // ANR 발생 포인트로 추축 제거함 : 2024.09.05
                                        OverlayViewUtil.init(mContext).startNavi(naviDestination,false,false,false,true);
                                        //MapUtil.getInstanceForTmap(context).invokeGoHome();
//                                        WorkQueue.nextTask();
                                        Fc.i(mContext,TAG,"start", "ACS - ","startNavi");
                                        Fc.i(mContext,TAG,"start",String.format("runNavi-%s.startNavi", MapUtil.getNaviAutoStart().name()));
//                                        LogUtil.faLog(mContext,TAG,"OverlayViewUtil.init.startNavi", String.format("MapUtil.getStartMode().name()", MapUtil.getStartMode().name()));
                                    } else {
                                        MapUtil.startNaviForExecActivity(mContext, naviDestination, true);
                                    }
//                                return null;
//                            });
//                                }
//                            }, Const.POWER_CONNECTED_START_NAVI_DELAY_TIME);

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Utils.toast(mContext,R.string.msg_run_only_onoff);
                    Fc.i(mContext,TAG,"start",String.format("runNavi-%s.OverlayViewUtil.stopService()", MapUtil.getNaviAutoStart().name()));
                    OverlayViewUtil.stopService();
                    if ( !MapUtil.isOverlayClose() ) {
                        OverlayViewUtil.init(mContext).openOverlayViewShowOnly();
                    }
                }
                WorkQueue.nextTask();
            }
        });

        execOnOffLast(true);

        WorkQueue.addTask(new Runnable() {
            @Override
            public void run() {
                Fc.i(mContext,TAG,"start",String.format("WorkQueue.addTask.run.stopWaitOverlay()"));
                MainConnectReceiver.stopReceiver();
            }
        });

        if ( runnable != null ) {
            WorkQueue.addTask(runnable);
        }

        WorkQueue.addTask(()->{
            isExecuting = false;
        });

        WorkQueue.execute();

    }
    public static void stop() {
        stop(true);
    }

    public static void stop(boolean exec) {
        stop(exec,null);
    }
    public static void stop(boolean exec,Runnable runnable) {
//TODO SOFTM activity를 이용해서 호출
//        if ( exec) {
//            MapUtil.execWorkerActivity(mContext,"stop",null,null,null,null,null);
//            return;
//        }

        WorkQueue.init();
        execOnOff(false);

        if (!MapUtil.getStopMode().equals(StopMode.NONE) && !MapUtil.getNaviAutoStart().equals(StartMode.NONE)) {
            WorkQueue.addTask(new Runnable() {
                @Override
                public void run() {
//                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
                    if (MapUtil.getStopMode().equals(StopMode.FORCE)) {
                        acsUtil.forceStop(true);
                    } else if (MapUtil.getStopMode().equals(StopMode.RECENT)) {
                        acsUtil.recentsStop(true);
                    } else if (MapUtil.getStopMode().equals(StopMode.RECENT_ALL)) {
                        acsUtil.recentsAllStop(true);
                    }
//                        }
//                    }, 0);
                    String threadName = Thread.currentThread().getName();
//                WorkQueue.nextTask();
                }
            });
        }

        execOnOffLast(false);

        WorkQueue.addTask(new Runnable() {
            @Override
            public void run() {
                MainConnectReceiver.stopReceiver();
                WorkQueue.nextTask();
            }
        });
        if ( !PermissionUtil.isAccessibilityServiceRunning(mContext) || MapUtil.isOverlayOpen() ) {
//            if ( !OverlayViewService.isForeground ) {
                WorkQueue.addTask(new Runnable() {
                    @Override
                    public void run() {
                        if ( !PermissionUtil.isAccessibilityServiceRunning(mContext) ) {
                            OverlayViewUtil.init(mContext).openOverlayViewShowOnly(true);
                            Utils.toast(mContext, String.valueOf(Utils.fromHtml(mContext.getString(R.string.msg_activate_accessibility_permission))));
                        } else if ( MapUtil.isOverlayOpen() ) {
                            OverlayViewUtil.init(mContext).openOverlayViewShowOnly();
                        }
                        WorkQueue.nextTask();
                    }
                });
//            }
        }
//        if ( !PermissionUtil.isAccessibilityServiceRunning(mContext) ) {
//            WorkQueue.addTask(()->{
//                OverlayViewUtil.init(mContext).openOverlayViewShowOnly(true);
//            });
//        }

        if ( runnable != null ) {
            WorkQueue.addTask(runnable);
        }

        WorkQueue.addTask(()->{
            MapUtil.setNotNaving();
            isExecuting = false;
        });

        WorkQueue.execute();
    }

    protected static void toast(Context context, String msg, boolean isSendEvent) {
        if ( isSendEvent ){
            if ( PermissionUtil.isAccessibilityServiceRunning(context) ) {
                Utils.toast(mContext, msg);
            } else {
                Utils.toastDebug(mContext, "Debug[" + msg + "]");
            }
        } else {
            Utils.toast(mContext, msg);
        }
    }
}
/***
 * 할일
 * 1. 오버레이에 권한없을경우 메시지 표시되게.
 * 2. Handler , Thread sleep 로직을 코루틴 main으로 변경
 * 2. 최소 대기시간 2초 < 0초 변경 불가
 * 3. 33 이상 노티 표시 권한 처리 : 설정
 * 3. 배너
 * 4. 구독
 */