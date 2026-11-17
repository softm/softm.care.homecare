package net.softm.data.kata.engine;

import android.content.Intent;
import android.os.IBinder;

public class SAPService extends SAPServiceKernel {
    public IBinder onBind(Intent paramIntent) {
        return super.onBind(paramIntent);
    }

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        return super.onStartCommand(paramIntent, paramInt1, paramInt2);
    }
}


