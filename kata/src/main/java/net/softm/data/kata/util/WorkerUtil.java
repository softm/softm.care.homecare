package net.softm.data.kata.util;

import android.content.Context;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;

import net.softm.data.kata.job.CheckServiceWorker;
import net.softm.data.kata.job.CrashLogWorker;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class WorkerUtil {
    private static final String TAG = "WorkerUtil";
    static PeriodicWorkRequest checkServiceWorkRequest;

    static String WORK_NAME_CHECK_SERVICE = "WORK_NAME_CHECK_SERVICE";

    public static boolean ifStopStartCheckService(Context context) {
        if ( WorkerUtil.isStopCheckService(context) ) {
            WorkerUtil.cancelCheckService(context); /** 위에서 체크해서 이미 stop 이지만 안전하게 한번더 죽이고 */
            WorkerUtil.startCheckService(context);
            return true;
        }
        return false;
    }

    public static boolean isStopCheckService(Context context) {
        WorkInfo.State wState = WorkerUtil.getCheckServiceWorkInfoState(context);
        return WorkInfo.State.CANCELLED.equals(wState);
    }

    /**
     * REPEAT
     * @param context
     */
    public static void startCheckService(Context context) {
        long repeatInterval = CheckServiceUtil.getCheckServiceRepeatInterval();
        TimeUnit repeatIntervalTimeUnit = CheckServiceUtil.getCheckServiceRepeatIntervalTimeUnit();
        long flexInterval = CheckServiceUtil.getCheckServiceFlexInterval();
        TimeUnit flexIntervalTimeUnit = CheckServiceUtil.getCheckServiceFlexIntervalTimeUnit();
        long initialDelay = CheckServiceUtil.getCheckServiceInitialDelay();
        TimeUnit initialDelayTimeUnit = CheckServiceUtil.getCheckServiceInitialDelayTimeUnit();

        CheckServiceWorker.lastRepeatInterval = repeatInterval;
        CheckServiceWorker.lastRepeatIntervalTimeUnit = repeatIntervalTimeUnit;
        CheckServiceWorker.lastFlexInterval = flexInterval;
        CheckServiceWorker.lastFlexIntervalTimeUnit = flexIntervalTimeUnit;
        CheckServiceWorker.lastInitialDelay = initialDelay;
        CheckServiceWorker.lastInitialDelayTimeUnit = initialDelayTimeUnit;

        Data inputData = new Data.Builder()
                .putString("type", CheckServiceWorker.TYPE_REPEAT)
                .putLong("timestamp", System.currentTimeMillis())
                .putLong("repeatInterval", repeatInterval)
                .putString("repeatIntervalTimeUnit", repeatIntervalTimeUnit.toString())
                .putLong("flexInterval", flexInterval)
                .putString("flexIntervalTimeUnit", flexIntervalTimeUnit.toString())
                .putLong("initialDelay", initialDelay)
                .putString("initialDelayTimeUnit", initialDelayTimeUnit.toString())
                .build();

        Fa.i(context, TAG, Fa.EVENT_SERVICE_CHECKER_START,String.format("%s : repeatInterval : %s/%s",CheckServiceWorker.TYPE_REPEAT,repeatInterval,repeatIntervalTimeUnit), String.format("flexInterval : %s/%s",flexInterval,flexIntervalTimeUnit),String.format("initialDelay : %s/%s",initialDelay,initialDelayTimeUnit));
        checkServiceWorkRequest = new PeriodicWorkRequest.
            Builder(CheckServiceWorker.class,
                repeatInterval, repeatIntervalTimeUnit,
                flexInterval, flexIntervalTimeUnit
            )
            .setInputData(inputData)
            .setInitialDelay(initialDelay, initialDelayTimeUnit) // 즉시 실행
            .build();
//            repeatInterval, repeatIntervalTimeUnit,
//            flexInterval, flexIntervalTimeUnit
        //                1, TimeUnit.HOURS,
        //                15, TimeUnit.MINUTES)
        //                15, TimeUnit.MINUTES,
        //                5, TimeUnit.MINUTES)
        /**
         반복 간격은
         PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS;
         이상이어야 하고 가변 간격은
         PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS;
         이상이어야 합니다.
         */

        //		WorkManager.getInstance(mInstance).enqueue(workRequest);
        //		WorkManager.getInstance(context).cancelAllWork();
        //        WorkManager.getInstance(context).cancelUniqueWork(checkServiceWorkName);
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_CHECK_SERVICE,
                ExistingPeriodicWorkPolicy.REPLACE,
                //        ExistingPeriodicWorkPolicy.KEEP,
                checkServiceWorkRequest);

        //        WorkManager.getInstance(context).getWorkInfosForUniqueWork("sync");
    }

    public static void getCheckServiceLiveData(Context context) {
//        ListenableFuture<List<WorkInfo>> WorkManager.getInstance(context).getWorkInfosForUniqueWork("checkService");
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(checkServiceWorkRequest.getId()).observe((LifecycleOwner) context, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null) {
                    // 작업 상태 업데이트 로직을 여기에 추가합니다.
                    WorkInfo.State state = workInfo.getState();
                    L.d(TAG,"Current work state: " + state);
                }
            }
        });
    }

    public static String getCheckServiceState(Context context) {
        StringBuffer sb = new StringBuffer();
        try {
            List<WorkInfo> workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(WORK_NAME_CHECK_SERVICE).get();
            for (WorkInfo workInfo : workInfos) {
                WorkInfo.State state = workInfo.getState();
                sb.append(workInfo.getId() + "," + state + "\n");
//                L.d(TAG,"Work ID: " + workInfo.getId() + " State: " + state);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    public static WorkInfo.State getCheckServiceWorkInfoState(Context context) {
        WorkInfo.State state = WorkInfo.State.CANCELLED;
        StringBuffer sb = new StringBuffer();
        try {
            List<WorkInfo> workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(WORK_NAME_CHECK_SERVICE).get();
            for (WorkInfo workInfo : workInfos) {
                state = workInfo.getState();
                sb.append(workInfo.getId() + "," + state + "\n");
//                L.d(TAG,"Work ID: " + workInfo.getId() + " State: " + state);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return state;
    }

    public static String getCheckServiceWorkInfoId(Context context) {
        String id = "";
        StringBuffer sb = new StringBuffer();
        try {
            List<WorkInfo> workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(WORK_NAME_CHECK_SERVICE).get();
            for (WorkInfo workInfo : workInfos) {
                WorkInfo.State state = workInfo.getState();
                id = workInfo.getId().toString();
//                L.d(TAG,"Work ID: " + workInfo.getId() + " State: " + state);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static String getAllState(Context context) {
        StringBuffer sb = new StringBuffer();
        try {
            WorkQuery workQuery = WorkQuery.Builder
                    .fromStates(Arrays.asList(WorkInfo.State.values()))
//                    .fromTags(listOf("syncTag")) // 특정 태그가 있는 작업을 찾습니다.
//                    .addStates(listOf(WorkInfo.State.FAILED, WorkInfo.State.CANCELLED)) // FAILED 또는 CANCELLED 상태의 작업을 찾습니다.
//                    .addUniqueWorkNames(listOf("preProcess", "sync")) // "preProcess" 또는 "sync"라는 이름의 유니크 작업을 찾습니다.
                    .build();
            List<WorkInfo> workInfos = WorkManager.getInstance(context).getWorkInfos(workQuery).get();
            for (WorkInfo workInfo : workInfos) {
                WorkInfo.State state = workInfo.getState();
                sb.append(workInfo.getId() + "," + state + "\n");
//                L.d(TAG,"Work ID: " + workInfo.getId() + " State: " + state);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void cancelCheckService(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_CHECK_SERVICE);
    }
    public static void cancelAll(Context context) {
        WorkManager.getInstance(context).cancelAllWork();
        WorkManager.getInstance(context).pruneWork();
    }
    public static void pruneWork(Context context) {
        WorkManager.getInstance(context).pruneWork();
    }

    public static void startCheckServiceImmediate10SecForCrash(Context context) {
        if ( CheckServiceUtil.isCheckServiceForCrash() ) {
            Data inputData = new Data.Builder()
                    .putString("type", CheckServiceWorker.TYPE_ONETIME)
                    .putString("gubun", "CRASH")
                    .putLong("timestamp", System.currentTimeMillis())
                    .build();
            //        Constraints constraints = new Constraints.Builder()
            Fa.i(context, TAG, Fa.EVENT_SERVICE_CHECKER_START, String.format("Crash %s : %s/%s",CheckServiceWorker.TYPE_ONETIME, CheckServiceUtil.getCheckServiceDelaySecForCrash(),TimeUnit.SECONDS));
            OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest
                    .Builder(CheckServiceWorker.class)
                    .setInputData(inputData)
                    .setInitialDelay(CheckServiceUtil.getCheckServiceDelaySecForCrash(), TimeUnit.SECONDS)
                    //                .setConstraints(constraints)
                    //                .addTag(WORK_TAG_CRASH_LOG)
                    .build();
            // oneTimeWorkRequest.getId();
            WorkManager.getInstance(context).enqueue(oneTimeWorkRequest);
        }
    }

    private static String WORK_TAG_CRASH_LOG = "WORK_TAG_CRASH_LOG";
    public static void crashLog(Context context,String crashMessage, long timestamp) {
        Data inputData = new Data.Builder()
                .putString("crashMessage", crashMessage)
                .putLong("timestamp", timestamp)
                .build();
        Fc.e(context,TAG,"crashLog",crashMessage);
        //        Constraints constraints = new Constraints.Builder()
        Fa.i(context, TAG, Fa.EVENT_CRASH_LOG_START);
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest
                .Builder(CrashLogWorker.class)
                .setInputData(inputData)
                //.setConstraints(constraints)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .addTag(WORK_TAG_CRASH_LOG)
                .build();

        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest);
    }

}

