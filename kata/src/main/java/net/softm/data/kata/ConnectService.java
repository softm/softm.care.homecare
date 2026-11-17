package net.softm.data.kata;

import static android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED;
import static android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_DISCONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_DISCONNECTED;

import android.app.Application;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import net.softm.data.kata.common.NaviStatus;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.NotificationUtil;
import net.softm.data.kata.util.ServiceUtil;

public class ConnectService extends Service {
    String TAG = "ConnectService";

    MainConnectReceiver mainConnectReceiver = new MainConnectReceiver();
    ScreenReceiver screenReceiver = new ScreenReceiver();
    BluetoothReceiver bluetoothReceiver = new BluetoothReceiver();
    BatteryReceiver batteryReceiver = new BatteryReceiver();
    WIFIReceiver WIFIReceiver = new WIFIReceiver();
//    UsbReceiver usbReceiver = new UsbReceiver();
    private Thread mainThread;
    public static Intent serviceIntent = null;
    private static ConnectService mContext;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        android.os.Debug.waitForDebugger();  // this line is key
    }

    public static boolean isInstanceCreated() {
        return mContext != null;
    }

    @Override
    public void onDestroy() {
        Fc.i(mContext,TAG,"onDestroy");
        super.onDestroy();
        Fa.i(mContext, TAG,Fa.EVENT_DESTORY_CONNECT_SERVICE);
        mContext = null;
        serviceIntent = null;
        try{
            unregisterConnectReceiver();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        stopForeground(true);
        ServiceUtil.destory();
    }

    private static final String STOP_ACTION = "STOP_ACTION";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.d(TAG,"onStartCommand");
        serviceIntent = intent;
        mContext = this;
        if (intent != null && intent.getAction() != null && intent.getAction().equals(STOP_ACTION)) {
            L.d(TAG, "onStartCommand"," -> STOP");
            stopForeground(true);
            stopSelf();
        } else {
            L.d(TAG, "onStartCommand"," -> START");

            String notificationDetails = null;
            NaviStatus naviStatus = null;
            Boolean registerReceiver = false;
            if ( serviceIntent != null ) {
                notificationDetails = serviceIntent.getStringExtra("P_NAVI_DETAILS");
                naviStatus = (NaviStatus) serviceIntent.getSerializableExtra("P_NAVI_STATUS");
                registerReceiver = serviceIntent.getBooleanExtra("P_REGISTER_RECEIVER",false);
            }

            Notification notification = NotificationUtil.init(this).sendNoti(notificationDetails,naviStatus);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                startForeground(1, notification); // point
            } else {
                startForeground(1, notification,FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK); // point
            }

            // TODO softm : Prevent Duplicate Receiver
//          if ( !PermissionUtil.hasAccessibilityPermissions() ) {
//            if ( registerReceiver ) {
                unregisterConnectReceiver();
                registerConnectReceiver();
//            }

//            if ( serviceIntent != null ) {
//                boolean bootStartNavi = serviceIntent.getBooleanExtra("P_BOOT_COMPLETED_START_NAVI", false);
//                if (bootStartNavi) {
//                    NaviDestination naviDestination = MapUtil.getNaviDestinationForTime();
//                    MapUtil.startNavi(mContext, naviDestination, false, true);
//                }
//            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    ConnectivityManager.NetworkCallback wifiCallback;
    private void unregisterConnectReceiver() {
        L.d(TAG,"ConnectService","unregisterConnectReceiver");
        try{
            unregisterReceiver(this.mainConnectReceiver);
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
        try{
            unregisterReceiver(this.screenReceiver);
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
        try{
            unregisterReceiver(this.batteryReceiver);
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
//        try{
//            unregisterReceiver(this.usbReceiver);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        try{
            unregisterReceiver(this.bluetoothReceiver);
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
        try{
            unregisterReceiver(this.WIFIReceiver);
        } catch (Exception ex) {
//            ex.printStackTrace();
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//            connectivityManager.unregisterNetworkCallback(wifiCallback);
//        }

        L.d(TAG, "unRegisterConnectReceiver");

    }
    private void registerConnectReceiver() {
        L.d(TAG,"ConnectService","registerConnectReceiver");
        IntentFilter localIntentFilterPower = new IntentFilter();
        localIntentFilterPower.addAction(ACTION_SOFTM_POWER_CONNECTED);
        localIntentFilterPower.addAction(ACTION_SOFTM_POWER_DISCONNECTED);

        localIntentFilterPower.addAction(ACTION_SOFTM_ACL_CONNECTED);
        localIntentFilterPower.addAction(ACTION_SOFTM_ACL_DISCONNECTED);

        localIntentFilterPower.addAction(MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED);
        localIntentFilterPower.addAction(MainConnectReceiver.ACTION_SOFTM_WIFI_DISCONNECTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(this.mainConnectReceiver, localIntentFilterPower,RECEIVER_EXPORTED);
        } else { // Below Android 12
            registerReceiver(this.mainConnectReceiver, localIntentFilterPower);
        }

        IntentFilter localIntentFilterScreen = new IntentFilter();
        localIntentFilterScreen.addAction(Intent.ACTION_USER_PRESENT);
        localIntentFilterScreen.addAction(Intent.ACTION_USER_UNLOCKED);
//        localIntentFilterScreen.addAction(Intent.ACTION_SCREEN_ON);
//        localIntentFilterScreen.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(this.screenReceiver, localIntentFilterScreen);

        IntentFilter localIntentFilterBattery = new IntentFilter();
        localIntentFilterBattery.addAction(Intent.ACTION_POWER_CONNECTED);
        localIntentFilterBattery.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(this.batteryReceiver, localIntentFilterBattery);

//        IntentFilter localIntentFilterUsb = new IntentFilter();
//        localIntentFilterUsb.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        localIntentFilterUsb.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        registerReceiver(this.usbReceiver, localIntentFilterUsb);

        IntentFilter localIntentFilterBluetooth = new IntentFilter();
        localIntentFilterBluetooth.addAction(ACTION_ACL_CONNECTED);
        localIntentFilterBluetooth.addAction(ACTION_ACL_DISCONNECTED);
        registerReceiver(this.bluetoothReceiver, localIntentFilterBluetooth);

        IntentFilter localIntentFilterWIFI = new IntentFilter();
        localIntentFilterWIFI.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        localIntentFilterWIFI.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(this.WIFIReceiver, localIntentFilterWIFI);

        L.d(TAG, "registerConnectReceiver","add Receiver ConnectReceiver");
/*
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            wifiCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
//                    Utils.toast(mContext,"WI-FI 연결됨");
                    MapUtil.getCurrentSsid(mContext);
                    Log.e("onAvailable", "WI-FI 연결됨");
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    MapUtil.getCurrentSsid(mContext);
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
//                    Utils.toast(mContext,"WI-FI 연결 끊김");
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        ConnectivityManager connectivityManager = getApplicationContext().getSystemService(ConnectivityManager.class);
                        NetworkCapabilities netCaps = connectivityManager.getNetworkCapabilities(network);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            try {
                                WifiInfo info = (WifiInfo) netCaps.getTransportInfo();
                            } catch (Exception ex) {
//                                Utils.toast(mContext, ex.toString());
                            }
                        }
//                    String ssid = wifiInfo.getSSID();

                        Log.e("onAvailable", "WI-FI 연결 끊김");
                    }
                }
            };

            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            // Builder 선언
            NetworkRequest.Builder wifiBuilder = new NetworkRequest.Builder();

            // 콜백이 호출되는 조건
            // TRANSPORT_WIFI = WIFI 상태 / TRANSPORT_CELLULAR = Mobile 네트워크 상태
            wifiBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

            // 해당 콜백 등록
            connectivityManager.registerNetworkCallback(wifiBuilder.build(), wifiCallback);

        }*/
    }

    public void toast(final Application application, final String msg) {
        Handler h = new Handler(application.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

}