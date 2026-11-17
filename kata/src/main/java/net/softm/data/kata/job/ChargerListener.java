/*
package net.softm.data.kata.job;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.softm.data.kata.old.PowerConnectReceiverOld;


public class ChargerListener extends Worker {

    private static final String TAG = "ChargerListenerTag";
    Context mContext = null;
    public ChargerListener(Context context, WorkerParameters workerParams){
        super(context, workerParams);
        mContext = context;
    }

    private PowerConnectReceiverOld powerConnectReceiver;

    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: "+"Charger Connected");
        // do some work when connected
        //BroadCastReceiver �� Action ���
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerConnectReceiver = new PowerConnectReceiverOld();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            mContext.getApplicationContext().registerReceiver(powerConnectReceiver,intentFilter);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,"충전 리시버 호출",Toast.LENGTH_SHORT).show();
                }
            });
        }


        return Result.success();
    }
}
*/
