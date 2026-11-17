package net.softm.data.kata.job;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import java.util.concurrent.TimeUnit;


class JobSchedulerSample {
    private static final int JOB_ID_UPDATE = 0x1000;

    static void setUpdateJob(Context context) {
        // 대용량 데이터를 업데이트하기 위한 적정 조건 설정
        JobInfo job =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            job = new JobInfo.Builder(
                    // Job에 설정할 Id 값
                    JOB_ID_UPDATE,
                    // 조건 만족 시 UpdateDataByWiFiService가 실행
                    new ComponentName(context, SyncJobService.class)
            )
                    .setPeriodic(TimeUnit.SECONDS.toMillis(5))
                    // 충분한 저장 공간이 있고,
//                    .setRequiresStorageNotLow(true)
                    // WiFi 등의 비과금 네트워크를 사용 중이며
//                    .setRequiredNetworksCapabilities(JobInfo.NETWORK_TYPE_UNMETERED)
                    // 충전 중 일 때
                    .setRequiresCharging(true)
                    .build();

            // JobScheduler 서비스
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            // Job을 등록한다.
            jobScheduler.schedule(job);

        }
    }
}
/*

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){ // >= 21
//            JobScheduler jobScheduler = (JobScheduler)getApplicationContext()
//                    .getSystemService(JOB_SCHEDULER_SERVICE);
//
//            ComponentName componentName = new ComponentName(this,
//                    SyncJobService.class);
//
//            JobInfo jobInfo = new JobInfo.Builder(1, componentName)
////                    .setRequiresDeviceIdle(true)
////                    .setRequiresCharging(true)
//                    .setPeriodic(TimeUnit.SECONDS.toMillis(5))
//                    .setPersisted(true)
////            .setPeriodic(3000)
//                    .build();
//            jobScheduler.schedule(jobInfo);
//        }

*/
/*
// 1
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // >= 21
//            JobScheduler jobScheduler = (JobScheduler) getApplicationContext()
//                    .getSystemService(JOB_SCHEDULER_SERVICE);
            JobScheduler jobScheduler = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                jobScheduler = getApplicationContext().getSystemService(JobScheduler.class);
            }

            ComponentName componentName = new ComponentName(this,
                    PowerConnectionService.class);

            JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                    .setMinimumLatency(1 * 1000) // wait at least
                    .setOverrideDeadline(3 * 1000) // maximum delay
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setRequiresDeviceIdle(true)
                    .setRequiresCharging(true)
//                    .setPeriodic(TimeUnit.SECONDS.toMillis(5))
//                    .setPersisted(true)
//            .setPeriodic(3000)
                    .build();
            jobScheduler.schedule(jobInfo);
        }
 *//*


// 0
// setupWorkRequest();

    private void setupWorkRequest() {
        mWorkManager = WorkManager.getInstance();
        Log.d(TAG, "setupWorkRequest: " + "setting request");
        // Create charging constraint
        Constraints constraints = new Constraints.Builder()
//                .setRequiresCharging(true)
                .build();
//        OneTimeWorkRequest chargerListen = new OneTimeWorkRequest
//                .Builder(ChargerListener.class)
//                .setConstraints(constraints)
//                .build();
        OneTimeWorkRequest chargerListen = new OneTimeWorkRequest
                .Builder(ChargerListener.class)
                .setConstraints(constraints)
                .build();

        mWorkManager.enqueue(chargerListen);
    }
*/
