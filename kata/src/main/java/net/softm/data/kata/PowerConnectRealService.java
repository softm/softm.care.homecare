package net.softm.data.kata;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.NotificationUtil;
import net.softm.data.kata.util.OverlayViewUtil;

import java.util.Calendar;
import java.util.Date;

public class PowerConnectRealService extends Service {
    String TAG = "PowerConnectRealService";
    public static final String TAG_RESTART = "PowerConnectRealService";

    MainConnectReceiver mainConnectReceiver = new MainConnectReceiver();
    private Thread mainThread;
    public static Intent serviceIntent = null;
    private static PowerConnectRealService mContext;

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
        Log.d(PowerConnectRealService.TAG_RESTART,"onDestroy");
        super.onDestroy();
        mContext = null;
        serviceIntent = null;
        setAlarmTimer();
        Thread.currentThread().interrupt();

        try{
            unregisterReceiver(this.mainConnectReceiver);
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        if (mainThread != null) {
            mainThread.interrupt();
            mainThread = null;
        }
    }

    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, PowerConnectAlarmRecever.class);
        intent.setAction(MainConnectReceiver.ACTION_RESTART_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
//        } else {
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
//        }
        Log.d(PowerConnectRealService.TAG_RESTART,"setAlarmTimer");
    }

    private static final String STOP_ACTION = "STOP_ACTION";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showToast(getApplication(), "Start PowerConnectRealService");
        serviceIntent = intent;
        //Toast.makeText(getApplicationContext(), "reflash", Toast.LENGTH_SHORT).show();
        Log.d(PowerConnectRealService.TAG_RESTART, "onStartCommand startId : " + startId);
        Log.d(PowerConnectRealService.TAG_RESTART, "onStartCommand isInstanceCreated : " +  isInstanceCreated());

        mContext = this;

        Log.d(PowerConnectRealService.TAG_RESTART, "onStartCommand -> START");
        registerBootReceiver();
        boolean bootStartNavi = serviceIntent.getBooleanExtra("P_BOOT_COMPLETED_START_NAVI", false);
        if ( bootStartNavi ) {
            NaviDestination naviDestination = MapUtil.getNaviDestinationForTime();
            OverlayViewUtil.init(mContext).startNavi(naviDestination,false, true);
        }
        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean run = true;
                while (run) {
                    try {
                        Thread.sleep(1000 * 60 * 1); // 1 minute
                        Date date = new Date();
                        NotificationUtil.init(mContext).sendNoti(null,null);
//                        sendNotification(date.toString());
                    } catch (InterruptedException e) {
                        run = false;
                        e.printStackTrace();
                    }
                }
            }
        });
        mainThread.start();

        return START_NOT_STICKY;
//        return super.onStartCommand(intent, flags, startId);
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

    public void showToast(final Application application, final String msg) {
        Handler h = new Handler(application.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "fcm_default_channel";//getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)//drawable.splash)
                        .setContentTitle("Service test")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,"Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}

