package net.softm.data.kata.util;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import net.softm.data.kata.WApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AppUtil {

    static String TAG = "AppUtil";

    public static boolean isActivityRunning(Context context, Class<?> activityClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        if (runningTasks != null && runningTasks.size() > 0) {
            ActivityManager.RunningTaskInfo taskInfo = runningTasks.get(0);
            ComponentName componentName = taskInfo.topActivity;
            return componentName != null && componentName.getClassName().equals(activityClass.getName());
        }

        return false;
    }

    // 특정 앱이 실행 중인지 확인하는 메서드
    public static boolean isAppRunning(Context context, String packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long currentTime = System.currentTimeMillis();
            Map<String, UsageStats> usageStats = usageStatsManager.queryAndAggregateUsageStats(currentTime - 1000 * 60 * 60, currentTime); // 1시간 이내에 실행된 앱들

            if (usageStats != null) {
                return usageStats.get(packageName) != null;
            }
        } else {
            // 이전 버전에서는 ActivityManager를 사용
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo process : processes) {
                if (process.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 특정 앱을 포커스로 가져오는 메서드
    public static void bringAppToFront(@NonNull Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (task.topActivity.getPackageName().equals(packageName)) {
                activityManager.moveTaskToFront(task.id, ActivityManager.MOVE_TASK_WITH_HOME);
                return;
            }
        }
    }


    /** 앱 업데이트 타임 : 서버의 basic_info.json에서 값이 할당됨, index.html에서 업데이트 시간을 표시하기 위해 이용됨 */
    public static long getAppReleaseUpdtedTime() {
        try {
            return WApplication.basicInfoDTO.appReleaseUpdtedTime;
        } catch (Exception ex) {
            return 0;
        }
    }

    /** 권한 체크 순서 */
    public static List<String> getCheckGrantInfo() {
        try {
            return WApplication.basicInfoDTO.checkGrantInfo;
        } catch (Exception ex) {
//            return new ArrayList<>(Arrays.asList("notification_access","ignore_battery","accessibility","location"));
            return new ArrayList<>(Arrays.asList("notification_access","location"));
        }
    }

}

