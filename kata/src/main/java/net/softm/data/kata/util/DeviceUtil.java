package net.softm.data.kata.util;

import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_LOCK_SCREEN;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import net.softm.lib.common.Connectivity;
import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.StartStopType;
import net.softm.data.kata.job.WorkQueue;
import net.softm.data.kata.kt.WorkerExeckt;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceUtil {

    private static final String TAG = "DeviceUtil";
    static AcsUtil acsUtil = null;

    @SuppressLint("MissingPermission")
    public static boolean isBluetoothEnable(Context context) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }

    public static boolean bluetoothEnable(Context context, boolean b) {
        return bluetoothEnable(context,b,false);
    }

    @SuppressLint("MissingPermission")
    public static boolean bluetoothEnable(Context context, boolean enabled,boolean workQueueNext) {
        boolean execNext = false;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            boolean bRtn = false;
            if (btAdapter != null) {
                if (enabled) btAdapter.enable();
                else btAdapter.disable();
                bRtn = btAdapter.isEnabled();
            } else {
                bRtn = false;
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ) {
                execNext = true;
                if ( workQueueNext ) WorkQueue.nextTask();
            } else {
                acsUtil = new AcsUtil(context);
                acsUtil.setQuickSettings(context, StartStopType.BLUETOOTH,enabled,workQueueNext);
            }
            return bRtn;
        } catch(Exception ex) {
            Fc.e(context,TAG,"bluetoothEnable",ex.toString());
            if ( !execNext ) {
                if ( workQueueNext ) WorkQueue.nextTask();
            }
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    public static boolean isWifiEnable(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    @SuppressLint("MissingPermission")
    public static boolean wifiEnable(Context context, boolean b) {
        return wifiEnable(context,b,false);
    }
    public static boolean wifiEnable(Context context, boolean b,boolean workQueueNext) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        wifiManager.setWifiEnabled(b);
//W
        acsUtil = new AcsUtil(context);
        if (!PermissionUtil.isAccessibilityServiceRunning(context) ) {
            if ( workQueueNext ) WorkQueue.nextTask();
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            wifiManager.setWifiEnabled(b);
            if ( workQueueNext ) WorkQueue.nextTask();
        } else
        {
            acsUtil.setSettingsControl(context, StartStopType.WIFI, b,workQueueNext);

//            checkHandler = new Handler();
//            checkHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if ( currV == notificationManagerCompat.areNotificationsEnabled() ) {
//                        checkHandler.postDelayed(this, Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);
//                    } else {
////                    Utils.goMain(MainActivity.this,true);
//                        JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun, notificationManagerCompat.areNotificationsEnabled()?"ok":"not");
//                    }
//                }
//            },Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);

        }

        return wifiManager.isWifiEnabled();
    }

    public static List<String> getStringsXmlValuesForStartStopType(Context context, StartStopType startStopType) {
        boolean clicked = false;
        List<String> stringsXmlValues = null;
        try {
            if (startStopType.equals(StartStopType.AIRPLANE_MODE)) {
                stringsXmlValues = StringsXmlValueUtil.getValuesNEnValues(context, "airplane_mode");
            } else if (startStopType.equals(StartStopType.WIFI)) {
                stringsXmlValues = StringsXmlValueUtil.getValuesNEnValues(context, "wifi");
            } else if (startStopType.equals(StartStopType.TETHERING)) {
                stringsXmlValues = StringsXmlValueUtil.getValuesNEnValues(context, "tether_settings_title_wifi");
            } else if (startStopType.equals(StartStopType.MOBILE_DATA)) {
                stringsXmlValues = StringsXmlValueUtil.getValuesNEnValues(context, "mobile_data_settings_title");
            } else if (startStopType.equals(StartStopType.BLUETOOTH)) {
                stringsXmlValues = StringsXmlValueUtil.getValuesNEnValues(context, "bluetooth");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return stringsXmlValues;
    }

    @SuppressLint("MissingPermission")
    public static boolean mobileDataEnable(Context context, boolean b) {
        return mobileDataEnable(context,b,false);
    }
    public static boolean mobileDataEnable(Context context, boolean b,boolean workQueueNext) {

        if (Connectivity.isMobileDataConnected(context) != b) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                try {
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    Method method = cm.getClass().getDeclaredMethod("setMobileDataEnabled", boolean.class);
                    method.setAccessible(true);
                    method.invoke(cm, b);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (workQueueNext) WorkQueue.nextTask();
            } else {
                acsUtil = new AcsUtil(context);
//                acsUtil.setQuickSettings(context, StartStopType.WIFI, b,workQueueNext);
//                acsUtil.setQuickSettings(context, StartStopType.MOBILE_DATA, b,workQueueNext);
                acsUtil.setSettingsControl(context, StartStopType.MOBILE_DATA, b, workQueueNext);
            }
        }

        return Connectivity.isMobileDataConnected(context);
    }


//    @SuppressLint("MissingPermission")
//    public static boolean tetheringEnable(Context context, boolean b) {
//        if ( b ) {
//            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//            if (wifiManager != null) {
//                // Check if Wi-Fi is enabled and disable it
//                if (wifiManager.isWifiEnabled()) {
//                    wifiManager.setWifiEnabled(false);
//                }
//
//                try {
//                    // Get the Wi-Fi hotspot setting method via reflection
//                    Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
//
//                    // Create a new Wi-Fi configuration
//                    WifiConfiguration wifiConfiguration = new WifiConfiguration();
//                    wifiConfiguration.SSID = "MyHotspot"; // Set your desired hotspot name
//                    wifiConfiguration.preSharedKey = "MyPassword"; // Set your desired hotspot password
//                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//                    // Invoke the method to enable the hotspot
//                    return (boolean) method.invoke(wifiManager, wifiConfiguration, true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            return false;
//        } else {
//
//        }
//        return false;
//    }

        //check whether wifi hotspot on or off
        public static boolean isApOn(Context context) {
            WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            try {
                Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
                method.setAccessible(true);
                return (Boolean) method.invoke(wifimanager);
            } catch (Throwable ignored) {
            }
            return false;
        }
    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }
    public static WifiManager.LocalOnlyHotspotReservation mReservation = null;

    private static ConnectivityManager.NetworkCallback mNetworkCallback;

    public static boolean tetheringEnable(Context context, boolean enabled,boolean workQueueNext) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(context. WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        acsUtil = new AcsUtil(context);
        if (!PermissionUtil.isAccessibilityServiceRunning(context) ) {
            if ( workQueueNext ) WorkQueue.nextTask();
            return false;
        }
        try {
            if ( !DeviceUtil.isDeviceSupportsMobileData(context) ) {
                Utils.toast(context, R.string.msg_nottethering_mobiledata_notsupport);
                if ( workQueueNext ) {
                    L.d(TAG,"tetheringEnable.dosenotsupport.mobiledata.nextTask");
                    WorkQueue.nextTask();
                }
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                acsUtil.setQuickSettings(context, StartStopType.TETHERING,enabled,workQueueNext);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if ( !enabled ) {
                    if (mReservation != null) {
                        mReservation.close();
                    }
                    if ( mNetworkCallback != null ) {
                        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        cm.unregisterNetworkCallback(mNetworkCallback);
                    }
                } else {
                    if(isApOn(context)) {
                        wifiManager.setWifiEnabled(false);
                    }
                    // For Android 8.0 and above
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkRequest.Builder builder = new NetworkRequest.Builder();
                    mNetworkCallback = new ConnectivityManager.NetworkCallback() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onAvailable(Network network) {
                            if (isApOn(context)) {
                                // Hotspot is already enabled, do nothing or handle as needed
                            } else {
                                try {
                                    wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
                                        @Override
                                        public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                                            // Hotspot started, reservation can be used to manage it
                                            mReservation = reservation;
                                            try{
                                                cm.unregisterNetworkCallback(mNetworkCallback);
                                            }catch (Exception e){
                                                Fc.e(context,TAG,"tetheringEnable_e",e.toString());
                                            }
                                        }

                                        @Override
                                        public void onFailed(int reason) {
                                            // Hotspot starting failed, handle the error
                                            if (mReservation != null) {
                                                mReservation.close();
                                                mReservation = null;
                                            }
                                        }
                                    }, null);
                                } catch (Exception ex) {
                                    Fc.e(context,TAG,"tetheringEnable_ex",ex.toString());
                                }
                            }
                        }
                    };

                    cm.requestNetwork(builder.build(), mNetworkCallback);
                }

                if ( workQueueNext ) {
                    L.d(TAG,"tetheringEnable.nextTask");
                    WorkQueue.nextTask();
                }
            } else {
                // For versions prior to Android 8.0
//                WifiConfiguration config = new WifiConfiguration();
//                config.SSID = "YourHotspotSSID";
//                config.preSharedKey = "YourHotspotPassword";
//                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

                try {
//                    wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class).invoke(wifiManager, config, true);
                        Method method = wifiManager.getClass().getMethod("setWifiApEnabled",
                                WifiConfiguration.class, boolean.class);
                        method.invoke(wifiManager, wificonfiguration, enabled);
                } catch (Exception e) {
                    // Handle any exceptions
                }

                if ( workQueueNext ) {
                    L.d(TAG,"tetheringEnable.nextTask");
                    WorkQueue.nextTask();
                }
            }


//            Build.VERSION.SDK_INT
//                    Build.VERSION_CODES.R
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    public static void airplaneModeEnable(Context context, boolean enabled) {
        airplaneModeEnable(context, enabled,false);
    }

    @SuppressLint("MissingPermission")
    public static void airplaneModeEnable(Context context, boolean enabled,boolean workQueueNext) {
//        acsUtil = new AcsUtil(context);
//        acsUtil.setQuickSettings(context, StartStopType.AIRPLANE_MODE,enabled,workQueueNext);

//        Settings.Global.AIRPLANE_MODE_ON
//        boolean error = false;
//        try {
//            Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, enabled ? 1 : 0);
//
//        } catch (Exception ex) {
//            error = true;
//            Crashlytics.log(Log.ERROR,TAG,ex.getMessage());
//        }

//        if ( error ) {
/** call quickSettings
*/
        acsUtil = new AcsUtil(context);
        if (!PermissionUtil.isAccessibilityServiceRunning(context) ) {
            if ( workQueueNext ) WorkQueue.nextTask();
            return;
        }
        acsUtil.setQuickSettings(context, StartStopType.AIRPLANE_MODE,enabled,workQueueNext);
/** call setSettingsControl
        acsUtil = new AcsUtil(context);
        acsUtil.setSettingsControl(context, StartStopType.AIRPLANE_MODE, enabled,workQueueNext);
*/
//        } else {

//            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
//            intent.putExtra("state",enabled);
//            context.sendBroadcast(intent);
//
//            if ( workQueueNext ) {
//                WorkQueue.nextTask();
//            }
//        }

        // https://stackoverflow.com/questions/22349928/permission-denial-not-allowed-to-send-broadcast-android-intent-action-airplane
        // The below old code is now not necessary @ API Level 23, 26 ...
        //Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        //context.sendBroadcast(intent);

//        context.startActivity( new Intent( Settings.ACTION_AIRPLANE_MODE_SETTINGS ));
//        startActivity( new Intent( Settings.ACTION_WIFI_SETTINGS ));


    }

    @SuppressLint("MissingPermission")
    public static boolean screenOn(Context context,boolean workQueueNext,Runnable ... runnables) {
//        if ( MapUtil.isTest() && !MapUtil.isLockScreen() ) {
//            Utils.toast(context,"test : 더블터치 안됨");
//            return true;
//        }
        Fc.i(context,TAG,"screenOn");
        acsUtil = new AcsUtil(context);

        if ( PermissionUtil.isAccessibilityServiceRunning(context) ) {
            acsUtil.sendEvent(AcsUtil.TURN_ON_SCREEN, 0);
        } else {
            Utils.turnOnScreen(); /** 화면 켜기만 실행 */
            WaitOverlayViewUtil.init(context).initIntent().turnOnScreen();
            /** 아래의 이유로 위에 구현만 적용함
             * Service에서 unClock Overlay가 잠금화면위에 올라와야 작동하는데,
             * Overlay가 잠금화면 아래에 표시되어 동작이 불가함
             - WaitOverlayViewUtil.init(mContext).initIntent().turnOnScreen();
             * AccessibilityService에서는 LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
             * 이용하여 구현이됨
             */
            /** 접근성 제스처. 스와이프를 실행하므로 구현 불가
             if (startStopLock.isUseYn() && !startStopLock.getEnabled()) { // 락을 푸는(off) 동작만 싫행, 락을 켜는(on) 동작은 스킵
             DeviceUtil.unlockScreen(mContext, false);
             }
             */
        }
        acsUtil.sendEvent(AcsUtil.GESTURE_DOUBLE_TAP, 500);
        acsUtil.sendEvent(AcsUtil.GESTURE_DOUBLE_TAP,new Runnable(){
            @Override
            public void run() {
                Fc.i(context,TAG,"screenOn","end");
                for (Runnable runnable : runnables) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.submit(runnable);
                    executor.shutdown();
                }
                if ( workQueueNext ) WorkQueue.nextTask();
            }
        },1000);
        return true;
    }

    public static boolean screenOff(Context context, boolean workQueueNext) {
        acsUtil = new AcsUtil(context);
        acsUtil.sendEvent(AcsUtil.TURN_OFF_SCREEN,new Runnable(){
            @Override
            public void run() {
                if ( workQueueNext ) WorkQueue.nextTask();
            }
        });
        return true;
    }

    public static boolean lockScreen(Context context, boolean workQueueNext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            acsUtil = new AcsUtil(context);
            acsUtil.sendEvent(KEY_GLOBAL_ACTION_LOCK_SCREEN,()->{
                if ( workQueueNext ) {
                    L.d(TAG,"lockScreen.nextTask");
                    WorkQueue.nextTask();
                }
            });
        } else {
            if ( workQueueNext ) {
                L.d(TAG,"lockScreen.not.support.nextTask");
                WorkQueue.nextTask();
            }
        }

        return true;
    }
    @SuppressLint("MissingPermission")
    public static boolean unlockScreen(Context context, boolean workQueueNext) {
        Fc.i(context,TAG,"unlockScreen");
        acsUtil = new AcsUtil(context);
        Utils.sleepMicoSeconds(1500); // 지연 꾝 필요함.
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                acsUtil.sendEvent(AcsUtil.GESTURE_SWIPE, 500);
                //                                    acsUtil.sendEvent(AcsUtil.GESTURE_SWIPE,1000);
            }
        }, 0);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                //                if ( !threadStarted )
                acsUtil.sendEvent(AcsUtil.GESTURE_SWIPE, new Runnable() {
                    @Override
                    public void run() {
                        Fc.i(context,TAG,"unlockScreen","end");
                        if ( workQueueNext ) {
                            L.d(TAG,"unlockScreen.nextTask");
                            WorkQueue.nextTask();
                        }
                    }
                });
                //                                    acsUtil.sendEvent(AcsUtil.GESTURE_SWIPE,1000);
            }
        }, 1500);
        return true;
    }
    @SuppressLint("MissingPermission")
    public static boolean playMusicEnable(Context context, boolean enabled,boolean workQueueNext) {
        /*
        ICS:
        "com.android.music.musicservicecommand.play"
        "com.android.music.musicservicecommand.pause"
        "com.android.music.musicservicecommand.next"
        JB:
        "com.sec.android.app.music.musicservicecommand.play"
        "com.sec.android.app.music.musicservicecommand.pause"
        "com.sec.android.app.music.musicservicecommand.next"

        */
        if ( enabled ) {
            sendMediaKeyevent(context, KeyEvent.KEYCODE_MEDIA_PLAY);
//            context.sendBroadcast(new Intent("com.sec.android.app.music.musicservicecommand.play"));
//            ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
//            AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
//            am.setStreamMute(AudioManager.STREAM_MUSIC, true);
//
//            AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//
//            if (mAudioManager.isMusicActive()) {
//                Intent i = new Intent("com.android.music.musicservicecommand");
//                i.putExtra("command", "play");
//                context.sendBroadcast(i);
//            }
        } else {
            sendMediaKeyevent(context, KeyEvent.KEYCODE_MEDIA_PAUSE);
//            context.sendBroadcast(new Intent("com.sec.android.app.music.musicservicecommand.pause"));
//            ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(null);
//            AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
//            am.setStreamMute(AudioManager.STREAM_MUSIC, false);
//
//
//            AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//
//            if (mAudioManager.isMusicActive()) {
//                Intent i = new Intent("com.android.music.musicservicecommand");
//                i.putExtra("command", "pause");
//                context.sendBroadcast(i);
//            }

        }


        return true;
    }

    public static boolean isMusicActive(Context context) {
        if ( context == null ) return false;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        boolean isMusicActive = audioManager.isMusicActive();
        return isMusicActive;
    }

    @TargetApi(19)
    public static void sendMediaKeyevent(Context context,int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MUTE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_MEDIA_RECORD:
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_CLOSE:
            case KeyEvent.KEYCODE_MEDIA_EJECT:
            case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    AudioManager am = (AudioManager) context.getApplicationContext().getSystemService(
                            Context.AUDIO_SERVICE);
                    // use AudioManager.dispatchKeyEvent() from API 19 (KitKat)
                    // http://stackoverflow.com/questions/19890643/android-4-4-play-default-music-player
//                    if (am.isMusicActive()) {
                        long eventtime = SystemClock.uptimeMillis();
                        KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, keyCode, 0);
                        am.dispatchMediaKeyEvent(downEvent);
                        KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, keyCode, 0);
                        am.dispatchMediaKeyEvent(upEvent);
//                    }
                } else {
                    // use broadcasting fake intent method
                    // http://stackoverflow.com/questions/12925896/android-emulate-send-action-media-button-intent
                    long eventtime = SystemClock.uptimeMillis();
                    Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                    KeyEvent downEvent = new KeyEvent(eventtime, eventtime,KeyEvent.ACTION_DOWN, keyCode, 0);
                    downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
                    context.sendOrderedBroadcast(downIntent, null);

                    Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, keyCode, 0);
                    upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
                    context.sendOrderedBroadcast(upIntent, null);
                }
                break;
            default:
                break;
        }
    }


    @SuppressLint("MissingPermission")
    public static boolean goHomeScreen(Context context, boolean workQueueNext) {
//        if ( MapUtil.isTest() ) {
//            acsUtil = new AcsUtil(context);
//            acsUtil.sendEvent(KEY_GLOBAL_ACTION_HOME ,()->{
//                if ( workQueueNext ) {
//                    L.d(TAG,"goHomeScreen.nextTask");
//                    WorkQueue.nextTask();
//                }
//            });
//
//        } else {
            WorkerExeckt.goHomeScreen(context,workQueueNext);
//        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public static boolean powerOff(Context context,boolean workQueueNext) {
        return true;
    }

    public static boolean isDeviceSupportsMobileData(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            // 모바일 데이터 지원 여부 확인
            return telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
        }
        return false;
    }

    /**
     * get Device ID
     */
    public static String getUUID(Context context){
        String deviceID;
        try {
//            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            final String tmDevice = "" + tm.getDeviceId();
//            final String tmSerial = "" + tm.getSimSerialNumber();
            final String androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

//            UUID uuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
//            deviceID = uuid.toString();
            deviceID = androidId;
        } catch (Exception ex) {
            deviceID = "";
        }
        return deviceID;
    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static boolean isTablet(Context context) {
        boolean bTablet = false;
        int screenSizeType = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        switch(screenSizeType) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                bTablet = true;
                break;
        }

        return bTablet;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /** 음악 플레이어를 활성화하는 지연 시간 */
    public static long getDelayMusicEnable() {
        try {
            return WApplication.basicInfoDTO.delayMusicEnable;
        } catch (Exception ex) {
            return 0;
        }
    }
}

