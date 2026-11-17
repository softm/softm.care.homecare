package net.softm.data.kata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceRestartingReceiver extends BroadcastReceiver {
    private static final String TAG = "ServiceRestartingReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context, "락해제", Toast.LENGTH_SHORT).show();

        String action = intent.getAction();
        if( action.equals(Intent.ACTION_MY_PACKAGE_REPLACED) ) {
            Log.d(TAG,"ACTION_MY_PACKAGE_REPLACED");
//            Toast.makeText(context, "Updated", Toast.LENGTH_LONG).show();
//            WApplication.mInstance.startPowerConnectService(context.getString(R.string.title_noti_runnig_title),NaviStatus.RUNNING, false);
        }
    }
}