package net.softm.data.kata;

import static net.softm.data.kata.job.MainConnectWorker.WORK_TAG;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.view.OrientationEventListener;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.ads.AdSize;

import net.softm.data.kata.common.AdLoadType;
import net.softm.data.kata.common.AdType;
import net.softm.data.kata.common.ChargingMode;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.AdViewOPType;
import net.softm.data.kata.common.OrientationType;
import net.softm.data.kata.common.ReceiverType;
import net.softm.data.kata.common.RunStatusScreenMode;
import net.softm.data.kata.common.StartMode;
import net.softm.data.kata.common.StopMode;
import net.softm.data.kata.dto.StartStopOnOffVO;
import net.softm.data.kata.job.MainConnectWorker;
import net.softm.data.kata.job.WorkExecutor;
import net.softm.data.kata.kt.Callbackkt;
import net.softm.data.kata.kt.Callkt;
import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.DeviceUtil;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.Fb;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.LicenseUtil;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OverlayObserverViewUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.PermissionUtil;
import net.softm.data.kata.util.RestUtil;
import net.softm.data.kata.util.ReviewUtil;
import net.softm.data.kata.util.Utils;
import net.softm.data.kata.util.WaitOverlayViewUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainConnectReceiver extends BroadcastReceiver {
    public static final String ACTION_SOFTM_POWER_CONNECTED = "net.softm.data.kata.ACTION_SOFTM_POWER_CONNECTED";
    public static final String ACTION_SOFTM_POWER_DISCONNECTED = "net.softm.data.kata.ACTION_SOFTM_POWER_DISCONNECTED";
    public static final String ACTION_SOFTM_ACL_CONNECTED = "net.softm.data.kata.ACTION_SOFTM_ACL_CONNECTED";
    public static final String ACTION_SOFTM_ACL_DISCONNECTED = "net.softm.data.kata.ACTION_SOFTM_ACL_DISCONNECTED";

    public static final String ACTION_SOFTM_WIFI_CONNECTED = "net.softm.data.kata.ACTION_SOFTM_WIFI_CONNECTED";
    public static final String ACTION_SOFTM_WIFI_DISCONNECTED = "net.softm.data.kata.ACTION_SOFTM_WIFI_DISCONNECTED";

    public static final String TAG = "MainConnectReceiver";
    public static final String ACTION_RESTART_SERVICE = "ACTION_POWER_CONNECTED";

    public static final String INTENT_EXTRA_RUN_SPEED = "_EXTRA_RUN_SPEED";
    public static final String INTENT_EXTRA_RUN_SPEED_CLICK_START = "_EXTRA_RUN_SPEED_CLICK_START";

    public static final String INTENT_EXTRA_ADVIEW_USER_EARNED_REWARD = "_EXTRA_ADVIEW_USER_EARNED_REWARD";
    public static final String INTENT_EXTRA_ADVIEW_USER_EARNED_REWARD_CALLBACK = "_EXTRA_ADVIEW_USER_EARNED_REWARD_CALLBACK";

    public static final String INTENT_EXTRA_APP_REVIEW = "_EXTRA_APP_REVIEW";
    public static final String INTENT_EXTRA_APP_REVIEW_CALLBACK = "_EXTRA_APP_REVIEW_CALLBACK";

    private static Context mContext = null;
    private static Intent mIntent = null;

    public static boolean threadStarted = false;
    private static boolean unlockStarted = false;
    private static Long unlockTimeMillis = 0L;
    public static Long startTimeMillis = 0L;
    private static Thread execReceiverThread;
    private AcsUtil acsUtil;

    public static void screenOn() {
        unlockStarted = false;
    }

    public static String getAction(Intent intent) {
        String action = intent != null ? intent.getAction() : "";
        if (ACTION_SOFTM_POWER_CONNECTED.equals(action)) {
            action = ACTION_SOFTM_POWER_CONNECTED;
        } else if (ACTION_SOFTM_POWER_DISCONNECTED.equals(action)) {
            action = ACTION_SOFTM_POWER_DISCONNECTED;
        } else if (ACTION_SOFTM_ACL_CONNECTED.equals(action)) {
            action = ACTION_SOFTM_ACL_CONNECTED;
        } else if (ACTION_SOFTM_ACL_DISCONNECTED.equals(action)) {
            action = ACTION_SOFTM_ACL_DISCONNECTED;
        } else if (ACTION_SOFTM_WIFI_CONNECTED.equals(action)) {
            action = ACTION_SOFTM_WIFI_CONNECTED;
        } else if (ACTION_SOFTM_WIFI_DISCONNECTED.equals(action)) {
            action = ACTION_SOFTM_WIFI_DISCONNECTED;
        }

        return action;
    }

    public static boolean isExistIntent() {
        return mIntent != null;
    }

    public static String getAction() {
        return getAction(mIntent);
    }

    public static boolean isStartAction() {
        return isStartAction(mIntent);
    }

    public static boolean isStopAction() {
        return !isStartAction(mIntent);
    }

    public static boolean isStartAction(Intent intent) {
        return getAction(intent).equals(ACTION_SOFTM_POWER_CONNECTED) ||
                getAction(intent).equals(ACTION_SOFTM_ACL_CONNECTED) ||
                getAction(intent).equals(ACTION_SOFTM_WIFI_CONNECTED);
    }

    public static boolean isLockToOnScreenForStart() {
        return System.currentTimeMillis()-unlockTimeMillis <= Const.RUN_STATUS_SCREEN_TIME_FOR_START; /** unLock 해제후 화면접근 10초 이내는 Unclock후 onScreen으로 판단 */
    }
    public static boolean isLockToOnScreenForStop() {
        return System.currentTimeMillis()-unlockTimeMillis <= Const.RUN_STATUS_SCREEN_TIME_FOR_STOP; /** unLock 해제후 화면접근 10초 이내는 Unclock후 onScreen으로 판단 */
    }

    public static boolean isRunSpeed() {
        Fc.i(mContext,TAG,"isRunSpeed", System.currentTimeMillis()-startTimeMillis, " / ", Const.RUN_SPEED_LIMIT_TIME*1000);
//        return System.currentTimeMillis()-startTimeMillis <= Const.RUN_SPEED_LIMIT_TIME*1000;
        return System.currentTimeMillis()-startTimeMillis <= Const.RUN_SPEED_LIMIT_TIME*60*1000;
    }

    public static ReceiverType getReceiverType() {
        return getReceiverType(mIntent);
    }

    public static ReceiverType getReceiverType(Intent intent) {
        String action = getAction(intent);
        if (action.equals(ACTION_SOFTM_POWER_CONNECTED) || action.equals(ACTION_SOFTM_POWER_DISCONNECTED)) {
            return ReceiverType.POWER;
        } else if (action.equals(ACTION_SOFTM_ACL_CONNECTED) || action.equals(ACTION_SOFTM_ACL_DISCONNECTED)) {
            return ReceiverType.BLUETOOTH;
        } else if (action.equals(ACTION_SOFTM_WIFI_CONNECTED) || action.equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
            return ReceiverType.WIFI;
        }
        return ReceiverType.NONE;

    }

    public static boolean isWaitOverlayVisible() {
        Fc.i(mContext,TAG, "isWaitOverlayVisible");
        if ( !newWait ) {
            return ConnectAccessibilityService.getLayout() != null && ConnectAccessibilityService.getLayout().getWindowToken() != null;
        } else {
            return WaitOverlayViewService.getLayout() != null && WaitOverlayViewService.getLayout().getWindowToken() != null;
        }
    }

    public static boolean isStart() {
        return threadStarted;
    }

    private boolean isValidPower(boolean start, Intent intent) {
        Intent btIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = btIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        if (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB) { // 유선
            if (!MapUtil.getChargingMode().equals(ChargingMode.WIRED_CHARGING) && !MapUtil.getChargingMode().equals(ChargingMode.ALL_CHARGING)) {
                return false;
            }
        } else if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) { // 무선
            if (!MapUtil.getChargingMode().equals(ChargingMode.WIRELESS_CHARGING) && !MapUtil.getChargingMode().equals(ChargingMode.ALL_CHARGING))
                return false;
        }
        return true;
    }

    private boolean isValidWifi(boolean start, Intent intent) {
        boolean rtn = false;
        if (start) {
            rtn = MapUtil.getCurrentSsid(mContext).equals(MapUtil.getWifi().get("ssid"));
        }
        {
            rtn = true;
        }
        return rtn;
    }

    private boolean isValidBluetooth(boolean start, Intent intent) {
        HashMap<String, ?> bluetooth = MapUtil.getBluetooth();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        bluetooth.get("address");
        return bluetooth.get("address").equals(device.getAddress());
    }

    @SuppressLint("StringFormatInvalid")
    public void onReceive(Context context, Intent intent) {

        acsUtil = new AcsUtil(context);
        if (!MapUtil.isAppEnable()) {
            Utils.toastDebug(context, "비활성화되어 실행하지 않음.");
            return;
        }
        if (!MapUtil.isStart()) {
            Utils.toastDebug(context, "서비스가 중지되어 실행하지 않음.");
            return;
        }

        Calendar c = Calendar.getInstance();
        Date date = new Date();
        int ampm = c.get(Calendar.AM_PM);
        long currentTime = date.getTime();
        boolean limitTime = false;
        if (MapUtil.isEnableWorkGotoOffTime()) {
            if (ampm == 0) { // 오전
                long fromTime = MapUtil.getWorkGotoTimeFromForLong();
                long toTime = MapUtil.getWorkGotoTimeToForLong();
                if (currentTime >= fromTime && currentTime <= toTime) {
                    limitTime = true;
                }
            } else if (ampm == 1) { // 오후
                long fromTime = MapUtil.getWorkOffTimeFromForLong();
                long toTime = MapUtil.getWorkOffTimeToForLong();
                if (currentTime >= fromTime && currentTime <= toTime) {
                    limitTime = true;
                }
            }
        } else {
            limitTime = true;
        }

        if (!limitTime) {
            Fc.i(context,TAG, "onReceive", context.getString(R.string.limit_work_goto));
            Utils.toast(context, context.getString(R.string.limit_work_goto));
            //                    MapUtil.execCommandActivity(context,"toast",context.getString(R.string.limit_work_goto));
            //                    Toast.makeText(context, "출/퇴근 시간 제한으로 실행안함", Toast.LENGTH_LONG).show();
            //                    WApplication.mInstance.toast(context.getString(R.string.limit_work_goto));
            return;
        }

        {
            if (MapUtil.isStart()) {
                mContext = context;

                Fc.i(context,TAG,"onReceive","RECEIVER - action : " + getAction(intent));

                boolean isStart = getAction(intent).equals(ACTION_SOFTM_POWER_CONNECTED) ||
                        getAction(intent).equals(ACTION_SOFTM_ACL_CONNECTED) ||
                        getAction(intent).equals(ACTION_SOFTM_WIFI_CONNECTED);
                boolean isIndependenceCondition = MapUtil.isIndependenceCondition();
                boolean exec = false;
                if (!isIndependenceCondition) { /** 종속조건 */
                    exec = (!MapUtil.isStartPower() || (MapUtil.isStartPower() && (MapUtil.getConnectedBattery() != -1) == isStart)) &&
                            (!MapUtil.isStartBluetooth() || (MapUtil.getBluetooth() != null && MapUtil.isStartBluetooth() && Utils.isNotEmpty(MapUtil.getConnectedBluetooth()) == isStart)) &&
                            (!MapUtil.isStartWifi() || (MapUtil.getWifi() != null && MapUtil.isStartWifi() && Utils.isNotEmpty(MapUtil.getConnectedWifiSSID()) == isStart))
                    ;
                } else { /** 독립조건 */
/** TODO SOFTM : Test후 아래 로직이 비정상 : 테스트만 이용 */
//                    exec = (!MapUtil.isStartPower() || (MapUtil.isStartPower() && (MapUtil.getConnectedBattery() != -1) == isStart)) ||
//                            (!MapUtil.isStartBluetooth() || (MapUtil.isStartBluetooth() && Utils.isNotEmpty(MapUtil.getConnectedBluetooth()) == isStart)) ||
//                            (!MapUtil.isStartWifi() || (MapUtil.isStartWifi() && Utils.isNotEmpty(MapUtil.getConnectedWifiSSID()) == isStart))
/** TODO SOFTM : Test후 아래 로직이 정상 */
                    exec = (MapUtil.isStartPower() && (MapUtil.getConnectedBattery() != -1) == isStart) ||
                            (MapUtil.isStartBluetooth() && Utils.isNotEmpty(MapUtil.getConnectedBluetooth()) == isStart) ||
                            (MapUtil.isStartWifi() && Utils.isNotEmpty(MapUtil.getConnectedWifiSSID()) == isStart)
                    ;
                }

                if ( MapUtil.getStopMode().equals(StopMode.NONE) && MapUtil.isAllOffForStop() ) {
                    exec = false;
                    Utils.toastDebug(mContext, "Debug[" + "종료 설정이 모두 꺼져있어 동작 안함" + "]");
                }
                if (exec) {
                    if (
                        ( isStopAction() && MapUtil.getStopMode().equals(StopMode.RECENT) )  /** 종료 액션 & 최근앱 종료 일경우만 */
//                         ||
//                         android.provider.Settings.System.getInt(mContext.getContentResolver(),android.provider.Settings.System.ACCELEROMETER_ROTATION, 0) == 1
                    ) {
                        getOrientation(context);
                    }
                    Fc.i(context,TAG,"onReceive", "exec true" , " isIndependenceCondition : ",isIndependenceCondition);
                    start(intent);
                } else {
                    Fc.i(context,TAG,"onReceive", "exec false");
                }
            }
        }
    }

    public boolean start(Intent intent) {
        Fc.i(mContext,TAG,"start");
//        waitOverlayStop();
        /** TODO SOFTM : ANR 2025.05.30 : ANR 조치 : IO사용 :*/
        new Callkt("execReceiverStop").runIO((params)->{
            execReceiverStop();
            return null;
        });
      /**
       * // TODO SOFTM : threadStarted == false 인 경우에만 RunThread가 실행되는데 오류가 발생해
       * RunThread threadStarted 값이 ture에서 복귀 못한 상태가 지속적으로 이루어질 경우에대한 방어 코드
       * 접근성서비스의 WaitOverlay가 출력되고 있는지 여부로 threadStarted 상태값을 초기화함
       */
        if( PermissionUtil.hasAccessibilityPermissions() ) {
            if (isWaitOverlayVisible()) {
                Fc.i(mContext,TAG,"Routine","mLayout : ok ");
                threadStarted = true;
                unlockStarted = false;
            } else {
                Fc.i(mContext,TAG,"Routine", "mLayout : null ");
                threadStarted = false;
            }
        }

        /** 화면잠김 */
        if (MapUtil.isLockScreen()) {
            mIntent = intent;
            L.i(TAG,"Routine","화면잠김");
            Fc.i(mContext,TAG,"Routine","화면잠김" , "isScreenOn : " + (MapUtil.isScreenOn(mContext) ? "켜짐" : "꺼짐"));
            /** 화면(시작루틴) */
            if ( isStartAction(intent) ) {
                // TODO SOFTM : 화면켜짐 설정인데 화면이 꺼진경우
                if ( MapUtil.getRunStatusScreenStartMode().equals(RunStatusScreenMode.ON) && !MapUtil.isScreenOn(mContext) ) {
                    Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_off_not_running,"Start")); // [실행 안함] : "화면 꺼져 있음"
                    L.i(TAG,"Routine","Start-[ON]","[실행 안함] : \"화면 꺼져 있음\"");
                    stopToShowOverlay();
                    return false;
                } else if ( MapUtil.getRunStatusScreenStartMode().equals(RunStatusScreenMode.OFF) && MapUtil.isScreenOn(mContext) ) {
                    Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_on_not_running,"Start")); // [실행 안함] : "화면 켜져 있음"
                    L.i(TAG,"Routine","Start-[OFF]","[실행 안함] : \"화면 꺼져 있음\"");
                    stopToShowOverlay();
                    return false;
                } else if ( MapUtil.getRunStatusScreenStartMode().equals(RunStatusScreenMode.LOCK) && !MapUtil.isLockScreen()) {
                    Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_unlock_not_running,"Start")); // [실행 안함] : "화면 잠김 해제"
                    L.i(TAG,"Routine","Start-[LOCK]","[실행 안함] : \"화면 잠김 해제\"");
                    stopToShowOverlay();
                    return false;
                }
            }

            /** 화면(종료루틴) */
            if ( !isStartAction(intent) ) {
                // TODO SOFTM : 화면켜짐 설정인데 화면이 꺼진경우
                /** 전원리시버로 동작시 전원이 끈어지면 무조건 화면이 켜진상태가 되어  꺼졌던 경우라도 켜진상태가되어 무조건 동작하게됨 : MapUtil.isScreenOn(mContext)
                 * 그래서, 전원리시버로 동작했으면서, 켜짐은 화면의 켜짐이 아닌 락이해제되고 켜짐으로 판단해야함.
                 * 그래서, 전원리시버만 단독으로 체크할경우 경우의 수가 많아 모든 경우에 락이 해제된 상태 화면 열림 상태에서만 동작하도록 한다.*/
//              if ( MapUtil.getRunStatusScreenStopMode().equals(RunStatusScreenMode.ON) && !MapUtil.isScreenOn(mContext) ) {
                if ( MapUtil.getRunStatusScreenStopMode().equals(RunStatusScreenMode.ON) && MapUtil.isLockScreen() ) {
//                    Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_off_not_running,"Stop")); // [실행 안함] : "화면 꺼져 있음"
                    Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_lock_not_running,"Stop")); // [실행 안함] : "화면 꺼져 있음"
                    L.i(TAG,"Routine","Stop-[ON]","[실행 안함] : \"화면 잠겨 있음\"");
                    stopToShowOverlay();
                    return false;
                /** 종료루틴의 경우 화면이 켜진 경우에만 동작해야므로 아래 조건 없어짐
                } else if ( MapUtil.getRunStatusScreenStopMode().equals(RunStatusScreenMode.OFF) && MapUtil.isScreenOn(mContext) ) {
                    Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_on_not_running,"Stop")); // [실행 안함] : "화면 켜져 있음"
                    L.i(TAG,"Routine","Stop-[OFF]","[실행 안함] : \"화면 켜져 있음\"");
                    stopToShowOverlay();
                    return false;
                */
                } else if ( MapUtil.getRunStatusScreenStopMode().equals(RunStatusScreenMode.LOCK) && !MapUtil.isLockScreen()) {
                    Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_unlock_not_running,"Stop")); // [실행 안함] : "화면 잠김 해제"
                    L.i(TAG,"Routine","Stop-[LOCK]","[실행 안함] : \"화면 잠김 해제\"");
                    stopToShowOverlay();
                    return false;
                }
            }

            try {
                StartStopOnOffVO ss = MapUtil.getStartStopOnOff();
                StartStopOnOffVO.StartUseYnOnOff startStopScreenOn = MainConnectReceiver.isStartAction() ? ss.getScreenOn().getStart() : ss.getScreenOn().getStop();
                StartStopOnOffVO.StartUseYnOnOff startStopLock = MainConnectReceiver.isStartAction() ? ss.getLock().getStart() : ss.getLock().getStop();

                if (startStopScreenOn.isUseYn() && startStopScreenOn.getEnabled()) { // 화면을 켜는 동작만 싫행, 화면 끄는 동작은 스킵
                    DeviceUtil.screenOn(mContext, false, () -> {
                        // ConnectAccessibilityService.showWaitingOverlay(getAction(), getReceiverType(), (long) (0));
                        if (startStopLock.isUseYn() && !startStopLock.getEnabled()) { // 락을 푸는(off) 동작만 싫행, 락을 켜는(on) 동작은 스킵
                            DeviceUtil.unlockScreen(mContext, false);
                        }
                    });
                }

                unlockStarted = true;
                Thread.sleep(100);
                unlockTimeMillis = System.currentTimeMillis();
            } catch (Exception e) {
                Fc.e(mContext,TAG,"start",e.toString());
            }

//            Bundle pBundle = new Bundle();
//            Fa.setBundleForString(pBundle,Const.PREF_KEY_CHARGING_MODE,MapUtil.getChargingMode().name());
            Fa.i(mContext,TAG,isStartAction(intent)?Fa.EVENT_START_RECEIVER_UNLOCK_SCREEN:Fa.EVENT_STOP_RECEIVER_UNLOCK_SCREEN);

            L.i(TAG,"Routine","화면잠김", isStartAction(intent)?"start":"stop","UnLock 실행함");
        } else { /** 화면풀림 */
            Fc.i(mContext,TAG,"Routine","화면풀림" , "isScreenOn : " + (MapUtil.isScreenOn(mContext) ? "켜짐" : "꺼짐"));

            // 화면(시작루틴)
            if ( isStartAction(intent) ) {
                if (!isLockToOnScreenForStart() )   { // TODO SOFTM : 락을 풀어 화면에 접근을 했을 경우도 있고, 종료루틴의 경우는 제외
                    // TODO SOFTM : 화면켜짐 설정인데 화면이 꺼진경우
                    if ( MapUtil.getRunStatusScreenStartMode().equals(RunStatusScreenMode.ON) && !MapUtil.isScreenOn(mContext)) {
                        Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_off_not_running,"Start")); // [실행제외] : '화면 꺼진 상태'
                        L.i(TAG,"Routine","Start-[ON]","[실행 안함] : \"화면 꺼져 있음\"");
                        return false;
                    } else if ( MapUtil.getRunStatusScreenStartMode().equals(RunStatusScreenMode.OFF) && MapUtil.isScreenOn(mContext)) {
                        Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_on_not_running,"Start")); // [실행제외] : '화면 켜진 상태'
                        L.i(TAG,"Routine","Start-[OFF]","[실행 안함] : \"화면 켜진 상태\"");
                        return false;
                    } else if ( MapUtil.getRunStatusScreenStartMode().equals(RunStatusScreenMode.LOCK) && !MapUtil.isLockScreen()) {
                        Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_unlock_not_running,"Start")); // [실행제외] : '화면 잠김 해제'
                        L.i(TAG,"Routine","Start-[LOCK]","[실행 안함] : \"화면 잠김 해제\"");
                        return false;
                    }
                }
            }
            // 화면(종료루틴)
            if ( !isStartAction(intent) ) {
                if (!isLockToOnScreenForStop() ) { // TODO SOFTM : 락을 풀어 화면에 접근을 했을 경우도 있고, 시작루틴의 경우는 제외
                    // TODO SOFTM : 화면켜짐 설정인데 화면이 꺼진경우
                    if ( MapUtil.getRunStatusScreenStopMode().equals(RunStatusScreenMode.ON) && !MapUtil.isScreenOn(mContext)) {
                        Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_off_not_running,"Stop")); // [실행제외] : '화면 꺼진 상태'
                        L.i(TAG,"Routine","Stop-[ON]","[실행 안함] : \"화면 꺼져 있음\"");
                        return false;
                    } else if ( MapUtil.getRunStatusScreenStopMode().equals(RunStatusScreenMode.OFF) && MapUtil.isScreenOn(mContext)) {
                        Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_on_not_running,"Stop")); // [실행제외] : '화면 켜진 상태'
                        L.i(TAG,"Routine","Stop-[OFF]","[실행 안함] : \"화면 켜진 있음\"");
                        return false;
                    } else if ( MapUtil.getRunStatusScreenStopMode().equals(RunStatusScreenMode.LOCK) && !MapUtil.isLockScreen()) {
                        Utils.toast(mContext, mContext.getString(R.string.msg_run_status_screen_unlock_not_running,"Stop")); // [실행제외] : '화면 잠김 해제'
                        L.i(TAG,"Routine","Stop-[LOCK]","[실행 안함] : \"화면 잠김 해제\"");
                        return false;
                    }
                }
            }

//            Bundle pBundle = new Bundle();
//            Fa.setBundleForString(pBundle,Const.PREF_KEY_CHARGING_MODE,MapUtil.getChargingMode().name());
            Fa.i(mContext,TAG,isStartAction(intent)?Fa.EVENT_START_RECEIVER:Fa.EVENT_STOP_RECEIVER);

            L.i(TAG,"Routine","화면열림", isStartAction(intent)?"start":"stop","실행함");

            mIntent = intent;
            startTimeMillis = System.currentTimeMillis();
            String pRunSpeed = intent.getStringExtra(INTENT_EXTRA_RUN_SPEED);
            if ( Utils.isNotEmpty(pRunSpeed) && pRunSpeed.equals(INTENT_EXTRA_RUN_SPEED_CLICK_START) ) {
                /** OverlayObserverViewUtil에서 doWork 속도 조건을 충족했을 경우 재실행을 방지하기 위해
                 * mIntent.removeExtra(INTENT_EXTRA_RUN_SPEED); 하고 기존의 MainConnectReceiver루틴을 실행함
                 */
                mIntent.removeExtra(INTENT_EXTRA_RUN_SPEED);
            } else {
                if ( MapUtil.isRunSpeed() && isStartAction() ) {
                    Utils.toast(mContext,"Start Waiting");
                    OverlayObserverViewUtil.init(mContext).start();
                    return false;
                } else {
                    OverlayObserverViewUtil.stopService();
                }
            }

            unlockStarted = false;

//            acsUtil.sendEvent(AcsUtil.KEY_GLOBAL_ACTION_POWER_DIALOG);
            // if ( waitOverlayThread != null ) stop();
//                stop();
            boolean waitReset = false;

            long waitingTime = MapUtil.getPreventWaitOverlayTime();
            long addWatingTime = 0;
//            if ( Const.NAVI_RENAVI_PREVENT_TIME < 5000 ) {
                if ( !LicenseUtil.isPaidLicense() ) {
                    if ( LicenseUtil.isLicense() ) { /** 유료사용자는 아니지만 라이센스가 있는경우 */
                        addWatingTime = waitingTime<3000?3000:waitingTime;
                    }
                    if ( AdViewOPType.ADVIEW.equals(LicenseUtil.getAdViewOPType()) ||
                         AdViewOPType.ADVIEW_STOP.equals(LicenseUtil.getAdViewOPType())
                    ) {
                        if ( !LicenseUtil.isLicense() ) { /** 라이센스 없으면 10초로 */
                            if (waitingTime < WaitOverlayViewUtil.getWaitTimeForNotAdLicense()) {
                                addWatingTime = WaitOverlayViewUtil.getWaitTimeForNotAdLicense() - waitingTime; // 10초로 만듦
                            }
                        }
                    }

                } else {
                    L.d(TAG,"라이센스 사용자 입니다.");
//                    Utils.toastDebug(R.string.msg_license_user);
                }
//            }
            if (waitReset) {
                threadStarted = false;
            } else {
                if (threadStarted) {
                    waitingTime -= System.currentTimeMillis() - ConnectAccessibilityService.showWaitStartTime;
                }
            }

            L.d(TAG, "workType",WApplication.mInstance.workType.name());
            if ( WApplication.mInstance.workType.equals(WApplication.WorkType.THREAD) ) {

                execReceiverThread = new Thread(new RunThread(waitingTime + 500 + addWatingTime)); // 보정 .5초
                execReceiverThread.start();

            } else {
                runWork(waitingTime + addWatingTime);
            }

            if (!threadStarted) {
                L.d(TAG,"start", "OverlayViewUtil.stopService");
//                OverlayViewUtil.stopService();
                OverlayViewUtil.hide(false);
            } else {
            }

            if (isWaitOverlayVisible()) {
                Fc.i(mContext, TAG, "start", "sendEventWaitingOverlayUpdateStatus");
                if (
                    (
                     !AdViewOPType.ADVIEW_STOP.equals(LicenseUtil.getAdViewOPType())
                    )
                    ||
                    (
                        ( AdViewOPType.ADVIEW_STOP.equals(LicenseUtil.getAdViewOPType()) ) && LicenseUtil.isLicense()
                    )
                )
                {
                    if ( !newWait ) {
                        acsUtil.sendEventWaitingOverlayUpdateStatus(getAction(intent), getReceiverType(intent), 200);
                    } else {
                        WaitOverlayViewUtil.update(getAction(), getReceiverType(intent));
                    }
                }
            }

            /**
             * Update UserType From 구글결제정보
             */
            LicenseUtil.syncUserLicense(mContext,null);

            /**
             * 운영 기본 정보 조회
             */
            RestUtil.getBasicInfo(mContext,()->{
                Fb.liveUser(mContext); /** Update Live User */

                if (
                        !AdViewOPType.NONE.equals(LicenseUtil.getAdViewOPType())
                ) {
                    if (AdUtil.isAdingWaitReward()) {
                        if (AdUtil.getRewardedAd() == null) {
                            new Callkt("AdUtil.getBasicInfo.callback->AdUtil.loadRewardAd").run((param) -> {
                                /**
                                 * 광고 로드
                                 */
                                AdUtil.loadRewardAd(mContext, AdUtil.getAdTypeForWaitReward(), () -> {
                                }, () -> {
                                    Utils.toastDebug(mContext, "광고 준비됨");
                                    L.d( TAG, "광고 준비됨");
                                });
                                return null;
                            });
                        }
                    }
                }

                if ( AdUtil.isAdingWaitBanner() ) {
                    if (AdLoadType.INDIRECT.equals(AdUtil.getAdTypeForWaitBannerLoadType())) {
                        if (AdUtil.getWaitBanner() == null) {
                            new Callkt("AdUtil.getBasicInfo.callback->AdUtil.loadWaitBanner").run((param) -> {
                                /**
                                 * 광고 로드
                                 */
                                AdUtil.loadBanner(mContext, AdType.BANNER, AdSize.MEDIUM_RECTANGLE);
                                return null;
                            });
                        }
                    }
                }
            });
        }

        Fc.i(mContext,TAG,"start", "threadStarted : " + threadStarted);
        return true;
    }
    static boolean newWait = true;
    private void runWork(long waitingTime) {
        // 한번만 대기 오버레이 ADD
        if (!threadStarted) {
            
            Fc.i(mContext,TAG,"runWork");
            // Create charging constraint
            Constraints constraints = new Constraints.Builder()
                    //                .setRequiresCharging(true)
                    .build();
            //        OneTimeWorkRequest chargerListen = new OneTimeWorkRequest
            //                .Builder(ChargerListener.class)
            //                .setConstraints(constraints)
            //                .build();

            String adviewUserEarnedReward = mIntent.getStringExtra(MainConnectReceiver.INTENT_EXTRA_ADVIEW_USER_EARNED_REWARD);

            Data inputData = new Data.Builder()
                    .putLong("waitingTime", waitingTime)
                    .putString("adviewUserEarnedReward", adviewUserEarnedReward)
                    .build();

            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest
                    .Builder(MainConnectWorker.class)
                    .setInputData(inputData)
                    .setConstraints(constraints)
                    .addTag(WORK_TAG)
                    .build();

            WorkManager.getInstance(mContext).enqueue(oneTimeWorkRequest);
        }
    }

    private static void overlayViewReStart() {
        if (MapUtil.isOverlayOpen()) {
            OverlayViewUtil.init(mContext).openOverlayViewShowOnly();
        }
    }

    public static void pause() {
        Fc.i(mContext,TAG,"pause");
        stopReceiver(false);
//        overlayViewReStart();
    }

    public static void stopToShowOverlay() {
        stopToShowOverlay(null);
    }
    public static void stopToShowOverlay(String msg) {
        Fc.i(mContext,TAG,"stopToShowOverlay");
        if (MapUtil.isOverlayOpen()) {
            if ( OverlayViewUtil.isBound ) {
                OverlayViewUtil.show(msg);
            } else {
                OverlayViewUtil.init(mContext).addMsg(msg).openOverlayViewShowOnly(true);
            }
        }
        stopReceiver();
//        overlayViewReStart();
    }

    public static void execReceiverStop() {
        Fc.i(mContext,TAG,"execReceiverStop");
        if ( WApplication.mInstance.workType.equals(WApplication.WorkType.THREAD) ) {
            if (execReceiverThread != null) execReceiverThread.interrupt();
            execReceiverThread = null;
        } else {
            WorkManager.getInstance(mContext).cancelAllWorkByTag(WORK_TAG);
        }
    }

    public static void stopReceiver() {
        stopReceiver(true);
    }

    public static void stopReceiver(boolean mIntentClear) {
        try {
            // 리시버 종료시 OrientationEventListener 비활성화
            if (orientationEventListener != null) {
                orientationEventListener.disable();
            }
            Fc.i(mContext,TAG,"stopWaitOverlay");
            if ( !newWait ) {
                ConnectAccessibilityService.stopWaitOverlay();
            } else {
                WaitOverlayViewService.stopWaitOverlay();
            }
            if (mIntentClear) mIntent = null;
//            Thread.sleep(100); // TODO SOFTM : ANR 2024.11.21 : ANR 조치 remove Thread.sleep(100)
            Callbackkt callbackkt = new Callbackkt(100,"MaainConnectReceiver.stopWaitOverlay()");
            callbackkt.run((param)->{
                execReceiverStop();threadStarted = false;
                return null;
            });
//            Utils.toast(mContext,"1초후 중지됨.그후 ");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadStarted = false;
        }
    }

    @SuppressLint("StringFormatInvalid")
    public static void execReceiver(Context context) {
        String action = getAction();
        Fc.i(mContext,TAG,"execReceiver");
        if ( !newWait ) {
            ConnectAccessibilityService.stopWaitOverlay();
        } else {
            WaitOverlayViewService.stopWaitOverlay();
        }
        Fc.i(mContext,TAG,"execReceiver","Step-1");
        threadStarted = false;

        if (action.equals(ACTION_SOFTM_POWER_CONNECTED) || action.equals(ACTION_SOFTM_ACL_CONNECTED) || action.equals(ACTION_SOFTM_WIFI_CONNECTED)) {
            WorkExecutor.init(context);
//            if ( MapUtil.isTest() ) {
            WorkExecutor.start();
//            } else {
//                WorkerExeckt.start();
//            }
//            executor.submit(() -> {
//                String threadName = Thread.currentThread().getName();
//                System.out.println("Job3 " + threadName);
//            });
//            executor.submit(() -> {
//                String threadName = Thread.currentThread().getName();
//                System.out.println("Job4 " + threadName);
//            });

        } else if (action.equals(ACTION_SOFTM_POWER_DISCONNECTED) || action.equals(ACTION_SOFTM_ACL_DISCONNECTED) || action.equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
            WorkExecutor.init(context);
            WorkExecutor.stop();
        }
    }
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    class RunThread implements Runnable {
        long waitingTime = -1;

        public RunThread(long waitingTime) {
            this.waitingTime = waitingTime;
        }

        public RunThread() {
            this.waitingTime = 5500;
        }

        @Override
        public void run() {
            
            try {
                // 한번만 대기 오버레이 ADD
                if (!threadStarted) {
//                    WorkerExeckt.delayCallback((param)->{
//                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
                    Callbackkt callbackkt = new Callbackkt(0L,"WorkQueue.addTask.OverlayViewUtil.init(mContext).startNavi(naviDestination,false,false);");
                    callbackkt.run((param)->{
                        Fc.i(mContext,TAG,"RunThread","run");
                        AcsUtil acsUtil = new AcsUtil(mContext);
                        acsUtil.sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
                        try {
                            if (!MapUtil.getNaviAutoStart().equals(StartMode.NONE)) {
//                                if (WApplication.appInfo.getDetail() == null) {
//                                    Utils.toast(mContext, mContext.getString(R.string.msg_no_app_selected));
//                                    return null;
//                                }
                                Fc.i(mContext,TAG,"RunThread.run");
                                if (getAction().equals(ACTION_SOFTM_POWER_CONNECTED) || getAction().equals(ACTION_SOFTM_ACL_CONNECTED) || getAction().equals(ACTION_SOFTM_WIFI_CONNECTED)) {
                                    /** 종료모드가 설정이 되어있고, 접근성도 활성화된 상태여야, 종료동작을 질행해, Naving 플래그를 클리어했을 것이므로
                                     * 앱이 실행중인 상태를 방어하지만, 그 외 상황에서는 runNavi 실행
                                     * 위처럼 생각할 수 있지만
                                     * 종료 루틴에서 setNotNaving을 하고, isNotNavig조건에 시작후 5분이상 지나면 NotNaving으로 판단하므로 기본 조건만 유지 ( MapUtil.isNaving() )
                                     * */
                                 // if (MapUtil.isNaving() && !StopMode.NONE.equals(MapUtil.getStopMode()) && PermissionUtil.isAccessibilityServiceRunning(mContext)) {
                                    if (MapUtil.isNaving() ) {
                                        // if (MapUtil.isNavingForNotiListener()) {
                                        Utils.toast(mContext, mContext.getString(R.string.msg_app_is_running, WApplication.appInfo.name));
                                        L.i(TAG,"RunThread.run","앱이 실행중입니다.");
//                                        overlayViewReStart();
                                        stopToShowOverlay(mContext.getString(R.string.msg_app_is_running, WApplication.appInfo.name));
                                        return null;
                                    }
                                    // MapUtil.isStartPower();
                                    // MapUtil.isStartWifi();
                                    // MapUtil.isStartBluetooth();
                                    //                boolean isP = MapUtil.isStartPower() && ;
                                } else if (getAction().equals(ACTION_SOFTM_POWER_DISCONNECTED) || getAction().equals(ACTION_SOFTM_ACL_DISCONNECTED) || getAction().equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
                                    if (MapUtil.isNaviApp() && MapUtil.isNotNaving()) {
                                        if (MapUtil.isOnlyStopAfterNaving()) {
                                            L.i(TAG,"RunThread.run","실행중인 앱이 없습니다.");
                                            Utils.toast(mContext, mContext.getString(R.string.no_apps_running));
//                                            overlayViewReStart();
                                            stopToShowOverlay();
                                            return null;
                                        }
                                    }
                                }
                            }

                            Fc.i(mContext,TAG,"RunThread.run","SHOW.WAITING.OVERLAY.call");
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    String adviewUserEarnedReward;
                                    try {
                                        adviewUserEarnedReward = mIntent != null?mIntent.getStringExtra(INTENT_EXTRA_ADVIEW_USER_EARNED_REWARD):"";
                                    } catch(Exception ex) {
                                        adviewUserEarnedReward = "";
                                    }
                                    String doAppReview;
                                    try {
                                        doAppReview = mIntent != null?mIntent.getStringExtra(INTENT_EXTRA_APP_REVIEW):"";
                                    } catch(Exception ex) {
                                        doAppReview = "";
                                    }

                                    if ( ReviewUtil.isReviewRequestAvailable() ) {
                                        MainConnectReceiver.pause();
                                        ReviewUtil.goReview(mContext);
                                    } else {
                                        Utils.toastDebug(mContext,"인앱리뷰 호출 조건 안됨");
                                        if ( !newWait ) {
                                            acsUtil.sendEventWaitingOverlay(getAction(), getReceiverType(), (long) (waitingTime / 1000), adviewUserEarnedReward, doAppReview, () -> {
//                                    try {
//                                        Thread.sleep(waitingTime+1000); // 5.5초 Sleep
//                                    } catch (InterruptedException e) {
//                                        throw new RuntimeException(e);
//                                    }
                                                // TODO SOFTM : 광고 : 로직변경 : 접근성에서 실행하도록
                                                if (!PermissionUtil.isAccessibilityServiceRunning(mContext)) {
                                                    execReceiver(mContext);
                                                }

                                            });
                                        } else {
                                            WaitOverlayViewUtil.init(mContext).initIntent().show(AcsUtil.SHOW_WAITING_OVERLAY,getAction(), getReceiverType(mIntent),(long) (waitingTime / 1000), Utils.isNotEmpty(adviewUserEarnedReward),Utils.isNotEmpty(doAppReview));
                                        }
                                        threadStarted = true;
                                    }


                                }
                            });


                        } catch (Exception ex) {
                            Fc.e(mContext,TAG,"RunThread.run","ex");
                        }
//                        }
                        return null;
                    });
                } else {
//                    acsUtil.sendEventWaitingOverlayUpdateStatus(getAction(mIntent),getReceiverType(mIntent),0);
                }
                /*
                 */
//                try {
//                    Thread.sleep(waitingTime+1000); // 5.5초 Sleep
//                } catch (InterruptedException e) {
//                }
//                if ( threadStarted ) execReceiver(mContext, mIntent);
            } catch (Exception ex2) {
                Fc.e(mContext,TAG,"RunThread.run",ex2.toString());
            } finally {
                L.d(TAG,"run","종료");
            }
        }


//        void stop(){
//            Runnable interruptedThread = new InterruptedThread();
//            Thread thread = new Thread(interruptedThread);
//            thread.start();
//            try {
//                Thread.sleep(1000);
//            } catch(InterruptedException e) {
//                e.printStackTrace();
//            }
//            thread.interrupt(); // InterruptedException 발생
//        }
    }

    static OrientationEventListener orientationEventListener;
    private static void getOrientation(Context context) {
        orientationEventListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    // 장치의 방향을 감지할 수 없는 경우
                    return;
                }
                if ( !MainConnectReceiver.isWaitOverlayVisible() ) {
                    orientationEventListener.disable();
                    return;
                }
                // 방향 변경 로그 출력
                L.d(TAG,"OrientationChange", "방향 변경 감지됨: " + orientation);

                if (orientation >= 315 || orientation < 45) {
                    WApplication.mInstance.isLandscape = false;
                    WApplication.mInstance.orientationType = OrientationType.PORTRAIT;
                    L.d(TAG,"OrientationChange", "현재 방향: 세로 모드 (Portrait)");
                } else if (orientation >= 45 && orientation < 135) {
                    WApplication.mInstance.isLandscape = true;
                    WApplication.mInstance.orientationType = OrientationType.LANDSCAPE_LEFT;
                    L.d(TAG,"OrientationChange", "현재 방향: 가로 모드 - 왼쪽 (Landscape Left)");
                } else if (orientation >= 135 && orientation < 225) {
                    WApplication.mInstance.isLandscape = false;
                    WApplication.mInstance.orientationType = OrientationType.PORTRAIT_UPSIDEDOWN;
                    L.d(TAG,"OrientationChange", "현재 방향: 세로 모드 - 거꾸로 (Upside Down)");
                } else if (orientation >= 225 && orientation < 315) {
                    WApplication.mInstance.isLandscape = true;
                    WApplication.mInstance.orientationType = OrientationType.LANDSCAPE_RIGHT;
                    L.d(TAG,"OrientationChange", "현재 방향: 가로 모드 - 오른쪽 (Landscape Right)");
                }
            }
        };

        // OrientationEventListener가 지원되는지 확인하고 활성화
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
            L.d(TAG,"OrientationChange", "OrientationEventListener 활성화됨.");
        } else {
            L.d(TAG,"OrientationChange", "OrientationEventListener를 사용할 수 없습니다.");
        }

        //
    }
}