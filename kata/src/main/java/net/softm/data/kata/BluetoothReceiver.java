package net.softm.data.kata;

import static android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED;
import static android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED;
import static android.content.Context.RECEIVER_EXPORTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_DISCONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_DISCONNECTED;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import net.softm.data.kata.common.StopMode;
import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OverlayViewUtil;

import java.util.HashMap;
import java.util.Map;

public class BluetoothReceiver extends BroadcastReceiver {
    private static final String TAG = "BluetoothReceiver";
    static int cnt = 0;

    private static boolean isCallableForConnected = true;
    private static boolean isCallableForDisConnected = true;
    private static Context mContext = null;
    private static Intent mIntent = null;

    private static boolean threadStarted = false;
    private static Thread mThread;
    private static Thread execReceiverThread;
    private AcsUtil acsUtil;

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
        //  registerConnectReceiver(); /** 블루투스 연결 & 뮤직 앱에서 이용 */
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

/** 블루투스 연결 & 뮤직 앱에서 이용 */
    static MainConnectReceiver mainConnectReceiver = new MainConnectReceiver();

    private void registerConnectReceiver() {
        L.d(TAG, "ConnectService", "registerConnectReceiver");
        IntentFilter localIntentFilterPower = new IntentFilter();
        localIntentFilterPower.addAction(ACTION_SOFTM_POWER_CONNECTED);
        localIntentFilterPower.addAction(ACTION_SOFTM_POWER_DISCONNECTED);

        localIntentFilterPower.addAction(ACTION_SOFTM_ACL_CONNECTED);
        localIntentFilterPower.addAction(ACTION_SOFTM_ACL_DISCONNECTED);
        L.d(TAG, "registerConnectReceiver", "add Receiver ConnectReceiver");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WApplication.mInstance.registerReceiver(this.mainConnectReceiver, localIntentFilterPower,RECEIVER_EXPORTED);
        } else { // Below Android 12
            WApplication.mInstance.registerReceiver(this.mainConnectReceiver, localIntentFilterPower);
        }
    }

    private void unregisterConnectReceiver() {
        L.d(TAG,"ConnectService","unregisterConnectReceiver");
        try{
            mContext.unregisterReceiver(this.mainConnectReceiver);
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
        L.d(TAG, "unRegisterConnectReceiver");

    }

    @SuppressLint("MissingPermission")
    public void execReceiver(final Context context, Intent intent) throws InterruptedException {
        String action = intent.getAction();
//        android.os.Debug.waitForDebugger();  // this line is key
        Log.d(TAG,"execReceiver");
        String intentStr = null;
        if ( MapUtil.isStartBluetooth() ) {
            HashMap<String,?> bluetooth = MapUtil.getBluetooth();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (action.equals(ACTION_ACL_CONNECTED) ) {
                Log.d(TAG,"Bluethooth.connected : " + device.getName() + "/" + device.getAddress() + " / " + MapUtil.getConnectedBluetooth());
                if (bluetooth != null && bluetooth.get("address").equals(device.getAddress())) {
//                        Utils.toast(mContext,"Bluetooth.연결됨");
                    Log.d(TAG,"Bluetooth.연결됨");
                    Map<String, String> deviceInfo = new HashMap<>();
                    deviceInfo.put("name", device.getName());
                    deviceInfo.put("address", device.getAddress());
                    deviceInfo.put("bondState", String.valueOf(device.getBondState()));
                    deviceInfo.put("type", String.valueOf(device.getType()));
                    MapUtil.setConnectedBluetooth((new Gson()).toJson(deviceInfo));
                    intentStr = MainConnectReceiver.ACTION_SOFTM_ACL_CONNECTED;
                } else {
                     Log.d(TAG,"Bluetooth.connected.ELSE : " +  device.getName() + "");
                }
            } else if (intent.getAction().equals(ACTION_ACL_DISCONNECTED)) {
                Log.d(TAG,"Bluethooth.disconnected : " + device.getName() + "/" + device.getAddress() + " / " + MapUtil.getConnectedBluetooth());
                if (bluetooth != null && bluetooth.get("address").equals(device.getAddress())) {
                    if ( !MapUtil.getStopMode().equals(StopMode.NONE) ) {
                        //                        Utils.toast(mContext,"Bluetooth.해제됨");
                        Log.d(TAG, "Bluetooth.해제됨");
                        MapUtil.setConnectedBluetooth((new Gson()).toJson(device));
                        intentStr = MainConnectReceiver.ACTION_SOFTM_ACL_DISCONNECTED;
                    }
                } else {
                     Log.d(TAG,"Bluetooth.disconnected.ELSE : " +  device.getName() + "");
                }
                MapUtil.setConnectedBluetooth("");
            } else {
                Log.d(TAG,"BLIETOOTH.ELSE");
            }

            if ( intentStr != null ) {
                Log.d(TAG,"Bluetooth.on.off.sendBroadcast");
                OverlayViewUtil.stopService();
                mContext.sendBroadcast(new Intent(intentStr));
            }
        }
        cnt++;
/** 블루투스 연결 & 뮤직 앱에서 이용 */
//        (new Callbackkt(3000, "BluetoothReceiver.execReceiver")).run((param2) -> {
//            unregisterConnectReceiver();
//            return null;
//        });
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