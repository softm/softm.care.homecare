//package net.softm.data.kata;
//
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE;
//
//import android.annotation.SuppressLint;
//import android.app.KeyguardManager;
//import android.bluetooth.BluetoothDevice;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.BatteryManager;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//
//import net.softm.data.kata.common.ChargingMode;
//import net.softm.data.kata.common.Const;
//import net.softm.data.kata.common.ReceiverType;
//import net.softm.data.kata.common.StartMode;
//import net.softm.data.kata.dto.StartStopOnOffVO;
//import net.softm.data.kata.job.WorkExecutor;
//import net.softm.data.kata.util.AcsUtil;
//import net.softm.data.kata.util.DeviceUtil;
//import net.softm.data.kata.util.MapUtil;
//import net.softm.data.kata.util.Utils;
//
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//
//public class PowerConnectReceiver extends BroadcastReceiver {
//    public static final String ACTION_SOFTM_POWER_CONNECTED = "net.softm.data.kata.ACTION_SOFTM_POWER_CONNECTED";
//    public static final String ACTION_SOFTM_POWER_DISCONNECTED = "net.softm.data.kata.ACTION_SOFTM_POWER_DISCONNECTED";
//    public static final String ACTION_SOFTM_ACL_CONNECTED = "net.softm.data.kata.ACTION_SOFTM_ACL_CONNECTED";
//    public static final String ACTION_SOFTM_ACL_DISCONNECTED = "net.softm.data.kata.ACTION_SOFTM_ACL_DISCONNECTED";
//
//    public static final String ACTION_SOFTM_WIFI_CONNECTED = "net.softm.data.kata.ACTION_SOFTM_WIFI_CONNECTED";
//    public static final String ACTION_SOFTM_WIFI_DISCONNECTED = "net.softm.data.kata.ACTION_SOFTM_WIFI_DISCONNECTED";
//
//    public static final String TAG = "PowerConnectReceiver";
//    public static final String ACTION_RESTART_SERVICE = "ACTION_POWER_CONNECTED";
//    static int cnt = 0;
//
//    private static boolean isCallableForConnected = true;
//    private static boolean isCallableForDisConnected = true;
//    private static Context mContext = null;
//    private static Intent mIntent = null;
//
//    private static boolean threadStarted = false;
//    private static Thread execReceiverThread;
//    private AcsUtil acsUtil;
//
//    public static String getAction(Intent intent) {
//        String action = intent!=null?intent.getAction():"";
//        if ( ACTION_SOFTM_POWER_CONNECTED.equals(action) ) {
//            action = ACTION_SOFTM_POWER_CONNECTED;
//        } else if ( ACTION_SOFTM_POWER_DISCONNECTED.equals(action) ) {
//            action = ACTION_SOFTM_POWER_DISCONNECTED;
//        } else if ( ACTION_SOFTM_ACL_CONNECTED.equals(action) ) {
//            action = ACTION_SOFTM_ACL_CONNECTED;
//        } else if ( ACTION_SOFTM_ACL_DISCONNECTED.equals(action) ) {
//            action = ACTION_SOFTM_ACL_DISCONNECTED;
//        } else if ( ACTION_SOFTM_WIFI_CONNECTED.equals(action) ) {
//            action = ACTION_SOFTM_WIFI_CONNECTED;
//        } else if ( ACTION_SOFTM_WIFI_DISCONNECTED.equals(action) ) {
//            action = ACTION_SOFTM_WIFI_DISCONNECTED;
//        }
//
//        return action;
//    }
//    public static boolean isExistIntent() {
//        return mIntent != null;
//    }
//    public static String getAction() {
//        return getAction(mIntent);
//    }
//
//    public static boolean isStartAction() {
//        return isStartAction(mIntent);
//    }
//
//    public static boolean isStartAction(Intent intent) {
//        return getAction(intent).equals(ACTION_SOFTM_POWER_CONNECTED) ||
//                getAction(intent).equals(ACTION_SOFTM_ACL_CONNECTED) ||
//                getAction(intent).equals(ACTION_SOFTM_WIFI_CONNECTED);
//    }
//
//    public static ReceiverType getReceiverType() {
//        return getReceiverType(mIntent) ;
//    }
//
//    public static ReceiverType getReceiverType(Intent intent) {
//        String action = getAction(intent);
//        if ( action.equals(ACTION_SOFTM_POWER_CONNECTED) || action.equals(ACTION_SOFTM_POWER_DISCONNECTED) ) {
//            return ReceiverType.POWER;
//        } else if ( action.equals(ACTION_SOFTM_ACL_CONNECTED) || action.equals(ACTION_SOFTM_ACL_DISCONNECTED) ) {
//            return ReceiverType.BLUETOOTH;
//        } else if ( action.equals(ACTION_SOFTM_WIFI_CONNECTED) || action.equals(ACTION_SOFTM_WIFI_DISCONNECTED) ) {
//            return ReceiverType.WIFI;
//        }
//        return ReceiverType.NONE;
//
//    }
//
//    public static boolean isOverlayVisible() {
//        Log.d(TAG,"isOverlayVisible");
//        return PowerConnectAccessibilityService.getLayout()!=null && PowerConnectAccessibilityService.getLayout().getWindowToken() != null;
//    }
//    public static boolean isStart() {
//        return threadStarted;
//    }
//
//    private boolean isValidPower(boolean start,Intent intent) {
//        Intent btIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//        int plugged = btIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//
//        if (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB) { // 유선
//            if (!MapUtil.getChargingMode().equals(ChargingMode.WIRED_CHARGING) && !MapUtil.getChargingMode().equals(ChargingMode.ALL_CHARGING)) {
//                return false;
//            }
//        } else if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) { // 무선
//            if (!MapUtil.getChargingMode().equals(ChargingMode.WIRELESS_CHARGING) && !MapUtil.getChargingMode().equals(ChargingMode.ALL_CHARGING))
//                return false;
//        }
//        return true;
//    }
//
//    private boolean isValidWifi(boolean start,Intent intent) {
//        boolean rtn = false;
//        if ( start ) {
//            rtn = MapUtil.getCurrentSsid(mContext).equals(MapUtil.getWifi().get("ssid"));
//        } {
//            rtn = true;
//        }
//        return rtn;
//    }
//
//    private boolean isValidBluetooth(boolean start,Intent intent) {
//        HashMap<String,?> bluetooth = MapUtil.getBluetooth();
//        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//        bluetooth.get("address");
//        return bluetooth.get("address").equals(device.getAddress());
//    }
//    @SuppressLint("StringFormatInvalid")
//    public void onReceive(Context context, Intent intent) {
//        acsUtil = new AcsUtil(mContext);
//        if (!MapUtil.isStart()) {
//            Utils.toastDebug(context, "서비스가 중지되어 실행하지 않음.");
//            return;
//        }
//
//        Calendar c = Calendar.getInstance();
//        Date date = new Date();
//        int ampm = c.get(Calendar.AM_PM);
//        long currentTime = date.getTime();
//        boolean limitTime = false;
//        if (MapUtil.isEnableWorkGotoOffTime()) {
//            if (ampm == 0) { // 오전
//                long fromTime = MapUtil.getWorkGotoTimeFromForLong();
//                long toTime = MapUtil.getWorkGotoTimeToForLong();
//                if (currentTime >= fromTime && currentTime <= toTime) {
//                    limitTime = true;
//                }
//            } else if (ampm == 1) { // 오후
//                long fromTime = MapUtil.getWorkOffTimeFromForLong();
//                long toTime = MapUtil.getWorkOffTimeToForLong();
//                if (currentTime >= fromTime && currentTime <= toTime) {
//                    limitTime = true;
//                }
//            }
//        } else {
//            limitTime = true;
//        }
//
//        if ( !limitTime ) {
//            Log.d(TAG, context.getString(R.string.limit_work_goto));
//            Utils.toast(context, context.getString(R.string.limit_work_goto));
//            //                    MapUtil.execCommandActivity(context,"toast",context.getString(R.string.limit_work_goto));
//            //                    Toast.makeText(context, "출/퇴근 시간 제한으로 실행안함", Toast.LENGTH_LONG).show();
//            //                    WApplication.mInstance.toast(context.getString(R.string.limit_work_goto));
//            return;
//        }
//
//        {
//            if (MapUtil.isStart()) {
//                mContext = context;
//
//                acsUtil = new AcsUtil(mContext);
//                Log.d(TAG, "RECEIVER - action : " + getAction(intent));
//
//                boolean isStart = getAction(intent).equals(ACTION_SOFTM_POWER_CONNECTED) ||
//                        getAction(intent).equals(ACTION_SOFTM_ACL_CONNECTED) ||
//                        getAction(intent).equals(ACTION_SOFTM_WIFI_CONNECTED);
//                boolean isIndependenceCondition = MapUtil.isIndependenceCondition();
//                boolean exec = false;
//                if ( !isIndependenceCondition ) {
//                    exec = (!MapUtil.isStartPower() || (MapUtil.isStartPower() && (MapUtil.getConnectedBattery() != -1) == isStart)) &&
//                            (!MapUtil.isStartBluetooth() || (MapUtil.getBluetooth() !=null && MapUtil.isStartBluetooth() && Utils.isNotEmpty(MapUtil.getConnectedBluetooth()) == isStart)) &&
//                            (!MapUtil.isStartWifi() || (MapUtil.getWifi() != null && MapUtil.isStartWifi() && Utils.isNotEmpty(MapUtil.getConnectedWifiSSID()) == isStart))
//                    ;
//                } else {
//                    exec = (!MapUtil.isStartPower() || (MapUtil.isStartPower() && (MapUtil.getConnectedBattery() != -1) == isStart)) ||
//                            (!MapUtil.isStartBluetooth() || (MapUtil.isStartBluetooth() && Utils.isNotEmpty(MapUtil.getConnectedBluetooth()) == isStart)) ||
//                            (!MapUtil.isStartWifi() || (MapUtil.isStartWifi() && Utils.isNotEmpty(MapUtil.getConnectedWifiSSID()) == isStart))
//                    ;
//                }
//                if ( exec ) {
//                    Log.d(TAG,"exec true");
//                    start(intent);
//                } else {
//                    Log.d(TAG,"exec false");
//                }
//            }
//        }
//    }
//
//    public boolean start(Intent intent) {
//        Log.d(TAG,"start()");
////        waitOverlayStop();
//        execReceiverThreadStop();
//
//        if( isOverlayVisible() ) {
//            Log.d(TAG,"mLayout : ok " );
//            threadStarted = true;
//        } else {
//            Log.d(TAG,"mLayout : null " );
//            threadStarted = false;
//        }
//
//        if ( MapUtil.isLockScreen() ) {
//            Log.d(TAG,"화면상태 : 화면잠김");
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    KeyguardManager km = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
////                    while (km.inKeyguardRestrictedInputMode()) {
//                        Log.d(TAG, "Until KeyGuard");
//                        try {
////                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
////                                @Override
////                                public void run() {
//
////                                    new Thread(new Runnable() {
////                                        @Override
////                                        public void run() {
////                                            // Create an instance of Instrumentation
////                                            Instrumentation inst = new Instrumentation();
////
////                                            // Simulate a key down event
////                                            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_SLEEP);
////
////                                            // Wait for a short time
//////                                            SystemClock.sleep(200);
////
////                                            // Simulate a key up event
//////                                            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
////                                        }
////                                    }).start();
//
//
//                                    StartStopOnOffVO ss = MapUtil.getStartStopOnOff();
//                                    StartStopOnOffVO.StartUseYnOnOff startStopScreenOn = PowerConnectReceiver.isStartAction() ? ss.getScreenOn().getStart() : ss.getScreenOn().getStop();
//                                    StartStopOnOffVO.StartUseYnOnOff startStopLock = PowerConnectReceiver.isStartAction() ? ss.getLock().getStart() : ss.getLock().getStop();
//
//                                    if ( startStopScreenOn.isUseYn() && startStopScreenOn.getEnabled() ) { // 화면을 켜는 동작만 싫행, 화면 끄는 동작은 스킵
//                                        DeviceUtil.screenOn(mContext,false);
//                                    }
//
//                                    // PowerConnectAccessibilityService.showWaitingOverlay(getAction(), getReceiverType(), (long) (0));
//                                    if ( startStopLock.isUseYn() && !startStopLock.getEnabled() ) { // 락을 푸는(off) 동작만 싫행, 락을 켜는(on) 동작은 스킵
//                                        DeviceUtil.unlockScreen(mContext,false);
//                                    }
//
////                            }, 0);
////                            if ( swipe ) {
////                                KeyguardManager.isDeviceLocked() : false
////                                KeyguardManager.isKeyguardLocked() : true
////                            } else if (pattern) {
////                                KeyguardManager.isDeviceLocked() : true
////                                KeyguardManager.isKeyguardLocked() : true
////                            }
//
//
//                            Thread.sleep(100);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
////                    } // while end
//
//                    Log.d(TAG,"UnLock");
//                }
//            }).start();
//
////            MapUtil.execCommandActivity(mContext,"TurnOnScreen");
////            Utils.turnOnScreen();
//
////            acsUtil.sendEvent(AcsUtil.TURN_ON_SCREEN,0);
////
////            acsUtil.sendEvent(AcsUtil.GESTURE_SWIPE,1000);
//
//            threadStarted = true;
////          acsUtil.sendEvent(AcsUtil.KEY_GLOBAL_ACTION_LOCK_SCREEN);
//            mIntent = intent;
//        } else {
//            Log.d(TAG,"화면상태 : 화면열림");
//            mIntent = intent;
////            acsUtil.sendEvent(AcsUtil.KEY_GLOBAL_ACTION_POWER_DIALOG);
//
//            if ( !MapUtil.getStartMode().equals(StartMode.NONE) ) {
//                if ( WApplication.appInfo.getDetail() == null ) {
//                    Utils.toast(mContext, mContext.getString(R.string.msg_no_app_selected));
//                    return false;
//                }
//
//                if (getAction().equals(ACTION_SOFTM_POWER_CONNECTED) || getAction().equals(ACTION_SOFTM_ACL_CONNECTED) || getAction().equals(ACTION_SOFTM_WIFI_CONNECTED)) {
//                    if (MapUtil.isNaving()) {
//                        // if (MapUtil.isNavingForNotiListener()) {
//                        Utils.toast(mContext, mContext.getString(R.string.msg_app_is_running, WApplication.appInfo.name));
//                        stop();
//                        return false;
//                    }
//                    // MapUtil.isStartPower();
//                    // MapUtil.isStartWifi();
//                    // MapUtil.isStartBluetooth();
//                    //                boolean isP = MapUtil.isStartPower() && ;
//                } else if (getAction().equals(ACTION_SOFTM_POWER_DISCONNECTED) || getAction().equals(ACTION_SOFTM_ACL_DISCONNECTED) || getAction().equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
//                    if (MapUtil.isNaviApp() && MapUtil.isNotNaving()) {
//                        if (MapUtil.isOnlyStopAfterNaving()) {
//                            Log.d("ACS", "실행중인 앱이 없습니다.");
//                            Utils.toast(mContext, mContext.getString(R.string.no_apps_running));
//                            stop();
//                            return false;
//                        }
//                    }
//                }
//            }
//            // if ( waitOverlayThread != null ) stop();
////                stop();
//            boolean waitReset = false;
//            long waitTime = Const.NAVI_RENAVI_PREVENT_TIME + 500; // 보정 .5초
//
//            if ( waitReset ) {
//                threadStarted = false;
//            } else {
//                if ( threadStarted ) {
//                    waitTime -= System.currentTimeMillis() - PowerConnectAccessibilityService.showWaitStartTime;
//                }
//            }
//
//            execReceiverThread = new Thread(new RunThread(waitTime));
//            execReceiverThread.start();
//
//            if ( !threadStarted) {
//                MapUtil.stopOverlayViewService(mContext);
//
//                if ( !MapUtil.isClosePopup() ) {
//                    MapUtil.openOverlayViewShowOnly(mContext);
//                }
//            } else {
//                Log.d(TAG,"threadStarted : " + threadStarted);
//            }
//            threadStarted = true;
//            acsUtil.sendEventWaitingOverlayUpdateStatus(getAction(intent),getReceiverType(intent),200);
//        }
//
//        return true;
//    }
//
//    private static void overlayViewReStart() {
//        MapUtil.stopOverlayViewService(mContext);
//
//        if ( !MapUtil.isClosePopup() ) {
//            MapUtil.openOverlayViewShowOnly(mContext);
//        }
//    }
//
//    public static void stop() {
//        waitOverlayStop();
//        overlayViewReStart();
//    }
//
//    public static void execReceiverThreadStop() {
//        if ( execReceiverThread != null ) execReceiverThread.interrupt();
//        execReceiverThread = null;
//    }
//
//    public static void waitOverlayStop() {
//        try {
//            PowerConnectAccessibilityService.stopWaitingOverlay();
//            mIntent = null;
//            Thread.sleep(100);
//
//            execReceiverThreadStop();
////            Utils.toast(mContext,"1초후 중지됨.그후 ");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } finally {
//            threadStarted = false;
//        }
//
//// TEST CODE
////        new Handler().postDelayed(new Runnable() {
////            @Override
////            public void run() {
////                stop();
////            }
////        }, 0);
//
//    }
//
//    protected void execOnOff() {
//        boolean isStart = getAction(mIntent).equals(ACTION_SOFTM_POWER_CONNECTED) ||
//                getAction(mIntent).equals(ACTION_SOFTM_ACL_CONNECTED) ||
//                getAction(mIntent).equals(ACTION_SOFTM_WIFI_CONNECTED);
//        boolean isStop = !isStart;
//
//        StartStopOnOffVO ss = MapUtil.getStartStopOnOff();
//        String msgOnOff =  isStart?mContext.getString(R.string.label_on):mContext.getString(R.string.label_off);
//
//        StartStopOnOffVO.StartUseYnOnOff startStop = null;
//
//        if (
//             ( isStart && ss.getBluetooth().getStart().isUseYn() ) ||
//             ( isStop  && ss.getBluetooth().getStop().isUseYn() )
//        ) {
//            startStop = isStart?ss.getBluetooth().getStart():ss.getBluetooth().getStop();
//            DeviceUtil.bluetoothEnable(startStop.getEnabled());
//            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_bluetooth),msgOnOff));
//        }
//
//        if (
//             ( isStart && ss.getWifi().getStart().isUseYn() ) ||
//             ( isStop  && ss.getWifi().getStop().isUseYn() )
//        ) {
//            startStop = isStart?ss.getWifi().getStart():ss.getWifi().getStop();
//            DeviceUtil.wifiEnable(mContext,startStop.getEnabled());
//            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_wifi),msgOnOff));
//        }
//
////        "bluetooth:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
////                "wifi:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
////                "tethering:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
////                "airplaneMode:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
////                "screen_on:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
////                "unlock:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
////                "play_music:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
////                "home_screen:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
////                "power_off:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}}"
////
//
//        if (
//             ( isStart && ss.getTethering().getStart().isUseYn() ) ||
//             ( isStop  && ss.getTethering().getStop().isUseYn() )
//        ) {
//            startStop = isStart?ss.getTethering().getStart():ss.getTethering().getStop();
//            DeviceUtil.tetheringEnable(mContext,startStop.getEnabled(),true);
//            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_tethering),msgOnOff));
//        }
//
//        if (
//             ( isStart && ss.getAirplaneMode().getStart().isUseYn() ) ||
//             ( isStop  && ss.getAirplaneMode().getStop().isUseYn() )
//        ) {
//            startStop = isStart?ss.getAirplaneMode().getStart():ss.getAirplaneMode().getStop();
//            DeviceUtil.airplaneModeEnable(mContext,startStop.getEnabled());
//            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_airplane_mode),msgOnOff));
//        }
//
//        if (
//             ( isStart && ss.getScreenOn().getStart().isUseYn() ) ||
//             ( isStop  && ss.getScreenOn().getStop().isUseYn() )
//        ) {
//            startStop = isStart?ss.getScreenOn().getStart():ss.getScreenOn().getStop();
////            DeviceUtil.screenOnEnable(mContext,startStop.getEnabled(),false);
//            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_screen_on),msgOnOff));
//        }
//
//        if (
//             ( isStart && ss.getLock().getStart().isUseYn() ) ||
//             ( isStop  && ss.getLock().getStop().isUseYn() )
//        ) {
//            startStop = isStart?ss.getLock().getStart():ss.getLock().getStop();
////            DeviceUtil.lockEnable(mContext,startStop.getEnabled(),false);
//            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_unlock),msgOnOff));
//        }
//
//        if (
//             ( isStart && ss.getPlayMusic().getStart().isUseYn() ) ||
//             ( isStop  && ss.getPlayMusic().getStop().isUseYn() )
//        ) {
//            startStop = isStart?ss.getPlayMusic().getStart():ss.getPlayMusic().getStop();
//            DeviceUtil.playMusicEnable(mContext,startStop.getEnabled(),false);
//            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_play_music),msgOnOff));
//        }
//
//        if (
//             ( isStart && ss.getHomeScreen().getStart().isUseYn() ) ||
//             ( isStop  && ss.getHomeScreen().getStop().isUseYn() )
//        ) {
//            startStop = isStart?ss.getHomeScreen().getStart():ss.getPlayMusic().getStop();
//            DeviceUtil.goHomeScreen(mContext,true);
//            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_home_screen),msgOnOff));
//        }
//
//        if (
////             ( isStart && ss.getPowerOff().getStart().isUseYn() ) ||
//             ( isStop  && ss.getPowerOff().getStop().isUseYn() )
//        ) {
//            startStop = isStart?ss.getPowerOff().getStart():ss.getPlayMusic().getStop();
//            DeviceUtil.powerOff(mContext,true);
//            Utils.toast(mContext, mContext.getString(R.string.msg_startstop_onoff,mContext.getString(R.string.label_power_off),msgOnOff));
//        }
//
//
//    }
//    @SuppressLint("StringFormatInvalid")
//    public void execReceiver(Context context, Intent intent) {
//        String action = getAction();
//        Log.i(TAG,"CALL execReceiver");
//        PowerConnectAccessibilityService.stopWaitingOverlay();
//
//        Log.d(TAG, action);
//
//        if (action.equals(ACTION_SOFTM_POWER_CONNECTED) || action.equals(ACTION_SOFTM_ACL_CONNECTED) || action.equals(ACTION_SOFTM_WIFI_CONNECTED) ) {
//            WorkExecutor.init(context);
//            WorkExecutor.start();
//
////            executor.submit(() -> {
////                String threadName = Thread.currentThread().getName();
////                System.out.println("Job3 " + threadName);
////            });
////            executor.submit(() -> {
////                String threadName = Thread.currentThread().getName();
////                System.out.println("Job4 " + threadName);
////            });
//
//        } else if (action.equals(ACTION_SOFTM_POWER_DISCONNECTED) || action.equals(ACTION_SOFTM_ACL_DISCONNECTED) || action.equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
//            WorkExecutor.init(context);
//            WorkExecutor.stop();
//        }
//    }
//
//    class RunThread implements Runnable {
//        long waitingTime = -1;
//        public RunThread(long waitingTime) {
//            this.waitingTime = waitingTime;
//        }
//        public RunThread() {
//            this.waitingTime = 5500;
//        }
//
//        @Override
//        public void run() {
//            try {
////                android.net.wifi.WIFI_STATE_CHANGED
//                if ( !threadStarted ) {
//                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            AcsUtil acsUtil = new AcsUtil(mContext);
//                            acsUtil.sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
//
//                            Log.i(TAG, "SHOW.WAITING.OVERLAY.call");
//                            PowerConnectAccessibilityService.showWaitingOverlay(getAction(), getReceiverType(), (long) (waitingTime / 1000));
//                        }
//                    }, 0);
//                }
//                Thread.sleep(waitingTime+1000); // 5.5초 Sleep
//
////                java.lang.RuntimeException: Can't create handler inside thread Thread[Thread-61,5,main] that has not called Looper.prepare()
//
////                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
////                    @Override
////                    public void run() {
//                        execReceiver(mContext, mIntent);
////                    }
////                }, waitingTime);
//
////                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
////                    @Override
////                    public void run() {
////                        if (finalExec) {
////                            execReceiver(mContext, mIntent);
////                            waitOverlayThread.interrupt();
////                        }
////                    }
////                }, 0);
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                System.out.println("\n종료");
//            }
//
//
//        }
//
//
////        void stop(){
////            Runnable interruptedThread = new InterruptedThread();
////            Thread thread = new Thread(interruptedThread);
////            thread.start();
////            try {
////                Thread.sleep(1000);
////            } catch(InterruptedException e) {
////                e.printStackTrace();
////            }
////            thread.interrupt(); // InterruptedException 발생
////        }
//
//
//    }
//
//}