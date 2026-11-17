package net.softm.data.kata.util;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class BluetoothUtil {

    private static final String TAG = "BluetoothUtil";
    public static boolean isBluetoothOn = false;

    public static BluetoothA2dp mBluetoothA2dp;

    public static AudioManager audioManager;
    public static BluetoothAdapter bluetoothAdapter;
    public static BluetoothProfile proxy;
    public static boolean isBluetoothConnected = false;

    public static void init( Context context ) {
        isBluetoothOn = false;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP);
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null);
//        IntentFilter localIntentFilterAudio = new IntentFilter();
//        localIntentFilterAudio.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
//        registerReceiver(mediaReceiver, localIntentFilterAudio);
    }

    public static void stop( Context context ) {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mBluetoothA2dp);
        }
    }

    private BroadcastReceiver mediaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive (Context c, Intent intent){

            if (intent.getAction().equals( AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                // Pause the playback
            }
        }
    };

    private final static android.media.AudioDeviceCallback audioDeviceCallback =
            new android.media.AudioDeviceCallback() {
                @Override
                public void onAudioDevicesAdded(
                        AudioDeviceInfo[] addedDevices) {
                    L.d(TAG , "Audio devices added");
//                    onAudioDeviceChange();  addedDevices[3].getAudioProfiles()
                    // addedDevices[1].getAddress()

                }

                @Override
                public void onAudioDevicesRemoved(
                        AudioDeviceInfo[] removedDevices) {
                    L.d(TAG, "Audio devices removed");
//                    onAudioDeviceChange();
                }
            };

    private static BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                // A2DP 프로파일이 연결되었을 때
                isBluetoothConnected = true;
                mBluetoothA2dp = (BluetoothA2dp) proxy;
                L.d(TAG, "BluetoothA2dp connected" , mBluetoothA2dp);
                try {
                    HashMap bt = MapUtil.getConnectedBluetooth();
                    if ( bt != null ) {
//                        BluetoothUtil.connect(bt.get("address").toString());
                        BluetoothUtil.connect(mBluetoothA2dp.getClass(), mBluetoothA2dp,  bt.get("address").toString());
                    }
                } catch (Exception e) {
                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, mBluetoothA2dp);
                    e.printStackTrace();
                }

            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
                // A2DP 프로파일이 연결 해제되었을 때
                isBluetoothConnected = false;
                mBluetoothA2dp = null;
            }
        }
    };

    public static boolean isValidBluetooths( BluetoothDevice device ) {
        List<HashMap> bluetooths = MapUtil.getBluetooths();
        if ( Utils.isNotEmpty(bluetooths) ) {
            for (HashMap bluetooth : bluetooths) {
                if (bluetooth != null && bluetooth.get("address").equals(device.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static HashMap getMusicBluetooth() {
        List<HashMap> bluetooths = MapUtil.getBluetooths();
        if ( Utils.isNotEmpty(bluetooths) ) {
            for (HashMap bluetooth : bluetooths) {
                if (bluetooth != null && bluetooth.get("music").equals(true)) {
                    return bluetooth;
                }
            }
        }
        return null;
    }

    public static boolean connect(Class btClass, BluetoothProfile proxy, String address) throws Exception {
        Method connectMethod = btClass.getDeclaredMethod("connect", BluetoothDevice.class);
        connectMethod.setAccessible(true);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(address);

        Boolean returnValue = (Boolean) connectMethod.invoke(proxy,btDevice);
        return returnValue.booleanValue();
    }

    public static boolean disconnect(Class btClass,BluetoothProfile proxy,String address) throws Exception {
        Method disconnectMethod = btClass.getDeclaredMethod("disconnect", BluetoothDevice.class);
        disconnectMethod.setAccessible(true);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(address);

        Boolean returnValue = (Boolean) disconnectMethod.invoke(proxy,btDevice);
        return returnValue.booleanValue();
    }


    public static String checkAudioOutput(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isBluetoothScoOn()) {
            return "Bluetooth";
        } else if (!audioManager.isBluetoothScoOn() && !audioManager.isSpeakerphoneOn()) {
            return "Wired";
        } else {
            return "Phone";
        }
    }

//
//    public static void connect(String address) {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
//        if (device == null) {
//            L.e(TAG,"BluetoothConnector", "Device not found. Unable to connect.");
//            return;
//        }
//
//        try {
//            // Using reflection to call the connect method
//            Method connectMethod = device.getClass().getDeclaredMethod("connect");
//            connectMethod.setAccessible(true);
//            connectMethod.invoke(device);
//            L.d(TAG,"BluetoothConnector", "Connection initiated.");
//        } catch (Exception e) {
//            L.e(TAG,"BluetoothConnector", "Failed to connect to device.", e);
//        }
//    }
//
//
//    public static void disconnect(String address) {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
//        if (device == null) {
//            L.e(TAG,"BluetoothConnector", "Device not found. Unable to connect.");
//            return;
//        }
//
//        try {
//            // Using reflection to call the connect method
//            Method connectMethod = device.getClass().getDeclaredMethod("disconnect");
//            connectMethod.setAccessible(true);
//            connectMethod.invoke(device);
//            L.d(TAG,"BluetoothConnector", "Connection initiated.");
//        } catch (Exception e) {
//            L.e(TAG,"BluetoothConnector", "Failed to connect to device.", e);
//        }
//    }
}

