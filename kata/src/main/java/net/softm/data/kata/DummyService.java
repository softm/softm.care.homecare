package net.softm.data.kata;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DummyService extends Service {
    String TAG = "DummyService";
    private Thread mainThread;
    public static Intent serviceIntent = null;
    private static DummyService mContext;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        android.os.Debug.waitForDebugger();  // this line is key
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
        mContext = null;
        serviceIntent = null;
    }

    private static final String STOP_ACTION = "STOP_ACTION";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;
        Log.d(TAG, "onStartCommand startId : " + startId);

        mContext = this;
        String pCommand = intent.getStringExtra("P_COMMAND");
        String pArg1 = intent.getStringExtra("P_ARG1");
        String pArg2 = intent.getStringExtra("P_ARG2");

        if ( "toast".equals(pCommand) ) {
            Handler h = new Handler(getApplication().getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {

                    //WApplication.mInstance.toast(pArg1);
                    Toast.makeText(getApplicationContext(), pArg1, Integer.valueOf(pArg2)).show();
                }
            });


//            Handler mainHandler = new Handler(getMainLooper());
//
//            mainHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    // Do your stuff here related to UI, e.g. show toast
//                    Toast.makeText(getApplicationContext(), pArg1, Integer.valueOf(pArg2)).show();
//                }
//            });

        } else {
        }
//        stopSelf();

        return START_NOT_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

}

