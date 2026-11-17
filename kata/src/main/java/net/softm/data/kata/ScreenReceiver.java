package net.softm.data.kata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.Utils;

public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenReceiver";
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
        start();
    }

    public void start() {
//        stop();
        AcsUtil acsUtil = new AcsUtil(mContext);
        Fc.i(mContext,TAG,"start",MapUtil.isLockScreen()?"화면잠김":"화면잠김 해제");

        receiverAction = MainConnectReceiver.getAction();

        if ( Utils.isNotEmpty(receiverAction) && MainConnectReceiver.isExistIntent() ) {
            OverlayViewUtil.stopService();
        }

        if ( !threadStarted) {
//            if ( waitOverlayThread != null ) stop();
            mThread = new Thread(new RunThread(0 + 500)); // 보정 .5초
            mThread.start();
            threadStarted = true;
        } else {
            //
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

// TEST CODE
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                stop();
//            }
//        }, 0);

    }
    String receiverAction = null;
    public void execReceiver(final Context context, Intent intent) {
        String action = intent.getAction();
//        android.os.Debug.waitForDebugger();  // this line is key

        if ( action.equals(Intent.ACTION_USER_UNLOCKED) || action.equals(Intent.ACTION_USER_PRESENT)) {
            try {
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            receiverAction = MainConnectReceiver.getAction();
            MainConnectReceiver.screenOn();
//            if ( ConnectReceiver.getAction()==Intent.ACTION_POWER_CONNECTED ){
//                intentStr = ConnectReceiver.ACTION_SOFTM_POWER_CONNECTED;
//            } else if ( ConnectReceiver.getAction()==Intent.ACTION_POWER_DISCONNECTED ){
//                intentStr = ConnectReceiver.ACTION_SOFTM_POWER_DISCONNECTED;
//            } else if ( ConnectReceiver.getAction()==ACTION_ACL_CONNECTED ){
//                intentStr = ConnectReceiver.ACTION_SOFTM_ACL_CONNECTED;
//            } else if ( ConnectReceiver.getAction()==ACTION_ACL_DISCONNECTED ){
//                intentStr = ConnectReceiver.ACTION_SOFTM_ACL_DISCONNECTED;
//            }
            Log.d(MainConnectReceiver.TAG,"Screen.on.off.sendBroadcast : " + receiverAction);
            if ( Utils.isNotEmpty(receiverAction) && MainConnectReceiver.isExistIntent() ) {
                mContext.sendBroadcast(new Intent(receiverAction));
                cnt++;
            }
        } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
        }

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
                        execReceiver(mContext, mIntent);
                    }
                }, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("\n종료"   );
            }

        }
    }


}