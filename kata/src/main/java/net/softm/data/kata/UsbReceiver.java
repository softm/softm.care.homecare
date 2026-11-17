package net.softm.data.kata;

import static android.content.Intent.ACTION_POWER_CONNECTED;
import static android.content.Intent.ACTION_POWER_DISCONNECTED;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import net.softm.data.kata.common.ChargingMode;
import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.MapUtil;

public class UsbReceiver extends BroadcastReceiver {
    private static final String TAG = "UsbReceiver";
    static int cnt = 0;

    private static boolean isCallableForConnected = true;
    private static boolean isCallableForDisConnected = true;
    private static Context mContext = null;
    private static Intent mIntent = null;

    private static boolean threadStarted = false;
    private static Thread mThread;
    private static Thread execReceiverThread;
    private AcsUtil acsUtil;
// Alle Apps schließen
    public void onReceive(final Context context, Intent intent) {
        Fc.i(mContext,TAG,"onReceive");
        mContext = context;
        mIntent = intent;
        acsUtil = new AcsUtil(mContext);
//        Log.d(TAG,"onReceive");
//        Utils.toast(mContext,"WIFI");
        start();
    }

    public void start() {
//        stop();
        AcsUtil acsUtil = new AcsUtil(mContext);
        try {
            execReceiver(mContext,mIntent);
        } catch (InterruptedException e) {
        }
    }

    public static void stop() {
        try {
            Thread.sleep(100);
            if ( mThread != null ) mThread.interrupt();
//            Utils.toast(mContext,"1초후 중지됨.그후 ");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            threadStarted = false;
        }
    }

    @SuppressLint("MissingPermission")
    public void execReceiver(final Context context, Intent intent) throws InterruptedException {
        String action = intent.getAction();
//        android.os.Debug.waitForDebugger();  // this line is key
        Log.d(TAG,"execReceiver");
        String intentStr = null;
        if ( MapUtil.isStartPower() ) {
            Intent btIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            boolean exec = true;
            int plugged = btIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int ratio = level * 100 / scale;
            String sPlug = null;
            String sStatus = null;
            if (plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB) { // 유선
                if (!MapUtil.getChargingMode().equals(ChargingMode.WIRED_CHARGING) && !MapUtil.getChargingMode().equals(ChargingMode.ALL_CHARGING)) {
                    exec = false;
                }
            } else if (plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS) { // 무선
                if (!MapUtil.getChargingMode().equals(ChargingMode.WIRELESS_CHARGING) && !MapUtil.getChargingMode().equals(ChargingMode.ALL_CHARGING))
                    exec = false;
            }
//            ACTION_BATTERY_CHANGED
            switch (plugged){
                case BatteryManager.BATTERY_PLUGGED_AC:
                    sPlug = "AC";
                    break;
                case BatteryManager.BATTERY_PLUGGED_USB:
                    sPlug = "USB";
                    break;
                default:
                    sPlug = "BATTERY";
                    break;
            }

            switch (status){
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    sStatus = "Charging";
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    sStatus = "not charging";
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    sStatus = "discharging";
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    sStatus = "fully charged";
                    break;
                default:
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    sStatus = "Unknwon status";
                    break;
            }

            Log.d(TAG, "Battery.connected : BatteryManager[" + plugged + "] : " + sPlug + " / "  + sStatus + " / " + MapUtil.getConnectedBattery());

            if (action.equals(ACTION_POWER_DISCONNECTED)) {
                if (exec) {
                    if (MapUtil.getChargingMode().getPlugged() >= MapUtil.getConnectedBattery()) {
                        exec = true;
                    } else {
                        exec = false;
                    }
                }
            }

            if ( exec ) {
                if (action.equals(ACTION_POWER_CONNECTED)) {
                    Log.d(TAG, "Battery.connected : BatteryManager[" + plugged + "] /" + MapUtil.getConnectedBattery());
                    MapUtil.setConnectedBattery(plugged);
//                        Utils.toast(mContext, "Battery.연결됨");
                    Log.d(TAG, "Battery.연결됨");
                    intentStr = MainConnectReceiver.ACTION_SOFTM_POWER_CONNECTED;
                } else if (intent.getAction().equals(ACTION_POWER_DISCONNECTED)) {
//                    if ( !MapUtil.getStopMode().equals(StopMode.NONE) ) {
                        Log.d(TAG, "Battery.disconnected : BatteryManager[" + plugged + "] /" + MapUtil.getConnectedBattery());
                        MapUtil.setConnectedBattery(-1);
//                        Utils.toast(mContext, "Battery.해제됨");
                        Log.d(TAG, "Battery.해제됨");
                        intentStr = MainConnectReceiver.ACTION_SOFTM_POWER_DISCONNECTED;
//                    }
                } else {
                    Log.d(TAG, "Battery.ELSE~~~~~~~");
                }
            }
            if ( intentStr != null ) {
                Log.d(TAG,"Battery.on.off.sendBroadcast");
//                MapUtil.stopOverlayViewService(mContext);
                mContext.sendBroadcast(new Intent(intentStr));
            }
        }
            cnt++;
            stop();
    }

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
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        AcsUtil acsUtil = new AcsUtil(mContext);
//                        acsUtil.sendEventWaitingOverlay(mIntent.getAction(),(long)(waitingTime/1000));
//                    }
//                }, 0);

                Thread.sleep(waitingTime); // 5.5초 Sleep
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            execReceiver(mContext, mIntent);
                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
                        }
                    }
                }, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("\n종료");
            }

        }
    }


}