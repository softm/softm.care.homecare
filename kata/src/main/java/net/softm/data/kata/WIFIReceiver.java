package net.softm.data.kata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import net.softm.data.kata.common.StopMode;
import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OverlayViewUtil;

import org.apache.commons.lang3.StringUtils;

public class WIFIReceiver extends BroadcastReceiver {
    private static final String TAG = "WIFIReceiver";
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
//        if ( !threadStarted) {
//            if ( waitOverlayThread != null ) stop();
//            mThread = new Thread(new RunThread(0 + 500)); // 보정 .5초
//            mThread.start();
        try {
            execReceiver(mContext,mIntent);
        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
        }
//            threadStarted = true;
//        } else {
            //
//        }
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

// TEST CODE
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                stop();
//            }
//        }, 0);

    }

    public void execReceiver(final Context context, Intent intent) throws InterruptedException {
        String action = intent.getAction();
//        android.os.Debug.waitForDebugger();  // this line is key
        L.d(TAG,"execReceiver");
        String intentStr = null;
        if ( MapUtil.isStartWifi() ) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            boolean connected = info!=null && info.isConnectedOrConnecting();
            String ssid = StringUtils.defaultString(MapUtil.getCurrentSsid(mContext));
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ) {
//                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    MapUtil.getCurrentSsid(mContext);
//                        NetworkInfo.DetailedState state = info.getDetailedState();
                    NetworkInfo.State state = info.getState();
                    Log.d(TAG,"WIFI.state_change[" + state +"] : " + ssid + " / " + MapUtil.getWifi().get("ssid") + " / " + MapUtil.getConnectedWifiSSID());
                    if ( state.equals(NetworkInfo.State.CONNECTED) ) {
                        Thread.sleep(500);
                        ssid = StringUtils.defaultString(MapUtil.getCurrentSsid(mContext));
                        Log.d(TAG, "WIFI.state_change[" + state + "] : " + ssid + " / " + MapUtil.getConnectedWifiSSID());
                        if (ssid.equals(MapUtil.getWifi().get("ssid")) ) {
//                                Utils.toast(mContext,"WIFI.연결됨");
                            Log.d(TAG,"WIFI.연결됨");
                            MapUtil.setConnectedWifiSSID(ssid);
                                intentStr = MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED;
                        }
                    } else if ( state.equals(NetworkInfo.State.DISCONNECTED) ) {
                        WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                        ssid = wm.getConnectionInfo().getSSID();
                        if ( MapUtil.getConnectedWifiSSID().equals(MapUtil.getWifi().get("ssid")) ) {
                            if ( !MapUtil.getStopMode().equals(StopMode.NONE) ) {
//                                Utils.toast(mContext,"WIFI.해제됨");
                                Log.d(TAG, "WIFI.해제됨");
                                intentStr = MainConnectReceiver.ACTION_SOFTM_WIFI_DISCONNECTED;
                            }
                        }
                        MapUtil.setConnectedWifiSSID("");
                    } else {
//                            Log.d(TAG,"WIFI.ELSE : [" +  state + "]");
                    }
//                    }
            } else  if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                boolean enabled = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED;
                Log.d(TAG,"WIFI.enabled.[" + enabled +"] : " + ssid + " / " + MapUtil.getConnectedWifiSSID());
                if ( !enabled ) {
//                        if ( MapUtil.getConnectedWifiSSID().equals(MapUtil.getWifi().get("ssid")) ) {
////                            Utils.toast(mContext, "WIFI.해제됨");
//                            Log.d(TAG,"WIFI.해제됨-DISABLED");
////                            MapUtil.setConnectedWifiSSID("");
//                            intentStr = ConnectReceiver.ACTION_SOFTM_WIFI_DISCONNECTED;
//                        }
                } else {
//                        Log.d(TAG,"WIFI.Enabled");
//                            MapUtil.setConnectedWifiSSID("");
//                        MapUtil.setConnectedWifiSSID(ssid);
//                        intentStr = ConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED;
                }
            } else {
                Log.d(TAG,"WIFI.ELSE~~~~~~~");
            }

            if ( intentStr != null ) {
                Log.d(TAG,"WIFI.on.off.sendBroadcast");
                OverlayViewUtil.stopService();
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