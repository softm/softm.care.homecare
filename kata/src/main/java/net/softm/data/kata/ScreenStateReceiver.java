package net.softm.data.kata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // 화면이 켜졌을 때 수행할 작업
                Log.d("ScreenStateReceiver", "Screen ON detected");
                // 예: 화면 켜졌을 때 즉시 작업 실행
                scheduleWork(context);
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // 화면이 꺼졌을 때 수행할 작업
                Log.d("ScreenStateReceiver", "Screen OFF detected");
                // 예: 화면 꺼졌을 때 작업 실행
            }
        }
    }

    private void scheduleWork(Context context) {
        // 화면이 켜졌을 때 작업을 예약하거나 즉시 실행
//        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
//                .setInitialDelay(0, TimeUnit.MILLISECONDS)  // 즉시 실행
//                .build();
//        WorkManager.getInstance(context).enqueue(workRequest);
    }
}
