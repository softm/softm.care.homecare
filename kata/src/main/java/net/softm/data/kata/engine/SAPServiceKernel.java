package net.softm.data.kata.engine;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import net.softm.data.kata.MainConnectReceiver;
import net.softm.data.kata.PowerConnectRealService;

public class SAPServiceKernel extends Service {
    MainConnectReceiver mainConnectReceiver = new MainConnectReceiver();

    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 21) {
//            JobScheduler jobScheduler = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
//            jobScheduler.cancelAll();
            try {
//                jobScheduler.schedule((new JobInfo.Builder(1, new ComponentName((Context)this, JobSchedulerService.class))).setPeriodic(600000L).setPersisted(true).build());
            } catch (IllegalArgumentException illegalArgumentException) {}
        }
    }
    public void onDestroy() {
        super.onDestroy();
//        if (Build.VERSION.SDK_INT >= 21)
//            ((JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancelAll();

        try{
            unregisterReceiver(this.mainConnectReceiver);
        }catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        if (paramIntent != null) {
            registerBootReceiver();
            return START_STICKY;
        }
        stopSelf();
        return START_STICKY;
    }

    private void registerBootReceiver() {
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        localIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        localIntentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        localIntentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        registerReceiver(this.mainConnectReceiver, localIntentFilter);
        Log.d(PowerConnectRealService.TAG_RESTART,"PowerConnectRealService >> add Receiver PowerConnectReceiverNew : ACTION_POWER_CONNECTED");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


