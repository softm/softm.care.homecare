package net.softm.data.kata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class PowerConnectAlarmRecever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Log.d(ConnectService.TAG_RESTART,"PowerConnectAlarmRecever Restart");
//            Intent in = new Intent(context, PowerConnectRestartService.class);
//            context.startForegroundService(in);
//            } else {
//                Intent in = new Intent(context, PowerConnectServiceNew.class);
//                context.startService(in);
        }
    }

}
