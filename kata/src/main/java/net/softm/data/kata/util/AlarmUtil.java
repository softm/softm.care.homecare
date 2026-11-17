package net.softm.data.kata.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.google.firebase.Timestamp;
import com.google.gson.Gson;

import net.softm.data.kata.CheckAlarmReceiver;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.ParamCallback;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.god.common.AlarmType;
import net.softm.data.kata.god.PrayClassAlarmReceiver;
import net.softm.data.kata.god.dto.PrayClass;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlarmUtil {
    private static final String TAG = "AlarmUtil";
    public static final int PRAY_CLASS_ALARM_MAX_COUNT = 10;
    public static final int PRAY_CLASS_ALARM_BASE_REQUEST_CODE = 1000;

    private static AlarmManager alarmMgr;

    private static PendingIntent alarmIntent;
    public static void addAlarm(Context context) {

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CheckAlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        alarmMgr.set(alarmMgr.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        60 * 1000, alarmIntent);

        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 14);

//        alarmMgr.cancelAll();

// With setInexactRepeating(), you have to use one of the alarmMgr interval
// constants--in this case, alarmMgr.INTERVAL_DAY.
        alarmMgr.setInexactRepeating(alarmMgr.RTC_WAKEUP, calendar.getTimeInMillis(),
                alarmMgr.INTERVAL_DAY, alarmIntent);
    }

//    alarmMgr.setExact(alarmMgr.RTC_WAKEUP, triggerAtMillis3, pendingIntent3);
//    alarmMgr.setInexactRepeating(alarmMgr.RTC_WAKEUP, calendar.getTimeInMillis(),
//    alarmMgr.INTERVAL_DAY, alarmIntent);

    public static void addAlarm(Context context, PrayClass prayClass) {
        addAlarm(context,prayClass,null);
    }

    public static void addExactAlarm(Context context, PrayClass prayClass, ParamCallback<PrayClass> callback, int requestCode) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, PrayClassAlarmReceiver.class);

//        PRAY_CLASS_ALARM_BASE_REQUEST_CODE + 10 + index;;
//        prayClass.getPrayDt().getSeconds()/60
//        1747700665
//        1747700665 : 일반적인 초값의 범위
//        2147483647 : Integer.MAX_VALUE
        // int reqCode = PRAY_CLASS_ALARM_BASE_REQUEST_CODE + index + 10 ;
        /**
         * reqCode -1 이면 bindAlarm으로 1 ~ PRAY_CLASS_ALARM_MAX_COUNT 까지 생성이되며
         * 일괄적으로 취소 가능한 형태로 만듦
         */
        int reqCode = requestCode > -1?requestCode: (int) (prayClass.getPrayDt().getSeconds());
        intent.putExtra("prayClass", (new Gson()).toJson(prayClass));
        intent.putExtra("alarmId", reqCode);
        PendingIntent alarmIntent1 = null;
        PendingIntent alarmIntent2 = null;
        PendingIntent alarmIntent3 = null;
        PendingIntent alarmIntent4 = null;
        PendingIntent alarmIntent5 = null;
        if ( requestCode == -1 ) { /** 특정 알람 : PrayClass.getPrayDt().getSeconds() **/
                alarmIntent1 = PendingIntent.getBroadcast(context, reqCode + 10, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if ( doubleAlarm ) {
                //            alarmIntent2 = PendingIntent.getBroadcast(context, reqCode + 20, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                alarmIntent3 = PendingIntent.getBroadcast(context, reqCode + 30, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                //            alarmIntent4 = PendingIntent.getBroadcast(context, reqCode + 40, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                alarmIntent5 = PendingIntent.getBroadcast(context, reqCode + 50, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            }
        } else {
            alarmIntent1 = PendingIntent.getBroadcast(context, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        Timestamp prayDt = prayClass.getPrayDt();
        Date date = prayDt.toDate();

        long NOW_MINUTES = 0L;
        long ONE_MINUTES = 1 * 60 * 1000L;
        long THREE_MINUTES = 3 * 60 * 1000L;
        long FIVE_MINUTES = 5 * 60 * 1000L;
        long TEN_MINUTES = 10 * 60 * 1000L;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (alarmIntent1 != null)
                    sendAlarm(AlarmType.ExactAndAllowWhileIdle, prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds() * 1000L) - (NOW_MINUTES), alarmIntent1, reqCode + 10);
                if (alarmIntent2 != null)
                    sendAlarm(AlarmType.ExactAndAllowWhileIdle, prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds() * 1000L) - (ONE_MINUTES), alarmIntent2, reqCode + 10);
                if (alarmIntent3 != null)
                    sendAlarm(AlarmType.ExactAndAllowWhileIdle, prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds() * 1000L) - (THREE_MINUTES), alarmIntent3, reqCode + 10);
                if (alarmIntent4 != null)
                    sendAlarm(AlarmType.ExactAndAllowWhileIdle, prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds() * 1000L) - (FIVE_MINUTES), alarmIntent4, reqCode + 10);
                if (alarmIntent5 != null)
                    sendAlarm(AlarmType.ExactAndAllowWhileIdle, prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds() * 1000L) - (TEN_MINUTES), alarmIntent5, reqCode + 10);
            } else {
                if (alarmIntent1 != null)
                    sendAlarm(AlarmType.Exact, prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds() * 1000L) - (NOW_MINUTES), alarmIntent1, reqCode + 10);
                if (alarmIntent2 != null)
                    sendAlarm(AlarmType.Exact, prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds() * 1000L) - (ONE_MINUTES), alarmIntent2, reqCode + 10);
                if (alarmIntent3 != null)
                    sendAlarm(AlarmType.Exact, prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds() * 1000L) - (THREE_MINUTES), alarmIntent3, reqCode + 10);
                if (alarmIntent4 != null)
                    sendAlarm(AlarmType.Exact, prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds() * 1000L) - (FIVE_MINUTES), alarmIntent4, reqCode + 10);
                if (alarmIntent5 != null)
                    sendAlarm(AlarmType.Exact, prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds() * 1000L) - (TEN_MINUTES), alarmIntent5, reqCode + 10);
            }

        } catch (SecurityException ex) {
            L.e(TAG,"addExactAlarm", ex);
        }
        if (callback != null) callback.onResult(prayClass);
    }

    public static void addInExactAlarm(Context context, PrayClass prayClass, ParamCallback<PrayClass> callback, int requestCode) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, PrayClassAlarmReceiver.class);

//        PRAY_CLASS_ALARM_BASE_REQUEST_CODE + 10 + index;;
//        prayClass.getPrayDt().getSeconds()/60
//        1747700665
//        1747700665 : 일반적인 초값의 범위
//        2147483647 : Integer.MAX_VALUE
        // int reqCode = PRAY_CLASS_ALARM_BASE_REQUEST_CODE + index + 10 ;
        /**
         * reqCode -1 이면 (int) (prayClass.getPrayDt().getSeconds()
         * bindAlarm 이면 1 ~ PRAY_CLASS_ALARM_MAX_COUNT 까지 생성되어 일괄적으로 취소 가능한 형태로 만듦
         */
        int reqCode = requestCode > -1?requestCode: (int) (prayClass.getPrayDt().getSeconds());
        if ( WUrl.devMode != WUrl.EnumDevMode.RELEASE ) prayClass.setTitle(prayClass.getTitle() + " : " + reqCode);

        intent.putExtra("prayClass", (new Gson()).toJson(prayClass));
        intent.putExtra("alarmId", reqCode);
        PendingIntent alarmIntent1 = null;
        PendingIntent alarmIntent2 = null;
        PendingIntent alarmIntent3 = null;
        PendingIntent alarmIntent4 = null;
        PendingIntent alarmIntent5 = null;
        if ( requestCode == -1 ) {
            alarmIntent1 = PendingIntent.getBroadcast(context, reqCode + (doubleAlarm?10:0), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if ( doubleAlarm ) {
    //          alarmIntent2 = PendingIntent.getBroadcast(context, reqCode + 20, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                alarmIntent3 = PendingIntent.getBroadcast(context, reqCode + 30, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    //            alarmIntent4 = PendingIntent.getBroadcast(context, reqCode + 40, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                alarmIntent5 = PendingIntent.getBroadcast(context, reqCode + 50, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            }
        } else {
            alarmIntent1 = PendingIntent.getBroadcast(context, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        Timestamp prayDt = prayClass.getPrayDt();
        Date date = prayDt.toDate();

        long NOW_MINUTES = 0L;
        long ONE_MINUTES = 1 * 60 * 1000L;
        long THREE_MINUTES = 3 * 60 * 1000L;
        long FIVE_MINUTES = 5 * 60 * 1000L;
        long TEN_MINUTES = 10 * 60 * 1000L;

        if ( alarmIntent1 != null ) sendAlarm(AlarmType.AndAllowWhileIdle,prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds()*1000L) - (NOW_MINUTES),alarmIntent1,reqCode + 10);
        if ( alarmIntent2 != null ) sendAlarm(AlarmType.AndAllowWhileIdle,prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds()*1000L) - (ONE_MINUTES),alarmIntent2,reqCode + 20);
        if ( alarmIntent3 != null ) sendAlarm(AlarmType.AndAllowWhileIdle,prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds()*1000L) - (THREE_MINUTES),alarmIntent3,reqCode + 30);
        if ( alarmIntent4 != null ) sendAlarm(AlarmType.AndAllowWhileIdle,prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds()*1000L) - (FIVE_MINUTES),alarmIntent4,reqCode + 40);
        if ( alarmIntent5 != null ) sendAlarm(AlarmType.AndAllowWhileIdle,prayClass, AlarmManager.RTC_WAKEUP, (prayDt.getSeconds()*1000L) - (TEN_MINUTES),alarmIntent5,reqCode + 50);

        if (callback != null) callback.onResult(prayClass);
    }

    public static void sendAlarm(AlarmType alarmType, PrayClass prayClass, int type, long triggerAtMillis, PendingIntent operation, int requestId) {
        if ( AlarmType.AndAllowWhileIdle.equals(alarmType) || AlarmType.Exact.equals(alarmType) || AlarmType.ExactAndAllowWhileIdle.equals(alarmType) ) {
            Timestamp prayDt = prayClass.getPrayDt();
            L.d(TAG, "알람 등록", requestId, DateFormatUtils.format(prayDt.toDate(), "yyyy-MM-dd HH:mm:ss"), prayClass.getPrayDt(), prayClass.getTitle(), prayClass.getDocId());
            alarmMgr.setAndAllowWhileIdle(type, triggerAtMillis, operation);
        }
    }

    public static void addAlarm(Context context, PrayClass prayClass, ParamCallback<PrayClass> callback) {
        addAlarm(context,prayClass,callback,-1);
    }
    public static void addAlarm(Context context, PrayClass prayClass, ParamCallback<PrayClass> callback,int requestId) {
        addInExactAlarm(context,prayClass,callback,requestId);
//        addExactAlarm(context,prayClass,callback);
        if (callback != null) callback.onResult(prayClass);
    }

    public static void cancelAlarm(Context context,PrayClass prayClass,ParamCallback<PrayClass> callback) {
        cancelAlarm(context,prayClass,callback,-1);
    }
    private static boolean doubleAlarm = true;
    public static void cancelAlarm(Context context,PrayClass prayClass, ParamCallback<PrayClass> callback, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // int reqCode = PRAY_CLASS_ALARM_BASE_REQUEST_CODE + 10 + index;
        /**
         * reqCode -1 이면 (int) (prayClass.getPrayDt().getSeconds()
         * bindAlarm 이면 1 ~ PRAY_CLASS_ALARM_MAX_COUNT 까지 생성되어 일괄적으로 취소 가능한 형태로 만듦
         */
        int reqCode = requestCode > -1?requestCode: (int) (prayClass.getPrayDt().getSeconds());
        PendingIntent alarmIntent1 = null;
        PendingIntent alarmIntent2 = null;
        PendingIntent alarmIntent3 = null;
        PendingIntent alarmIntent4 = null;
        PendingIntent alarmIntent5 = null;

        Intent intent = new Intent(context, PrayClassAlarmReceiver.class);
        intent.putExtra("prayClass", (new Gson()).toJson(prayClass));
        intent.putExtra("alarmId", reqCode);

        if ( requestCode == -1 ) { /** 특정 알람 : PrayClass.getPrayDt().getSeconds() **/
            alarmIntent1 = PendingIntent.getBroadcast(context, reqCode + (doubleAlarm?10:0), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if ( doubleAlarm ) {
                //          alarmIntent2 = PendingIntent.getBroadcast(context, reqCode + 20, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                alarmIntent3 = PendingIntent.getBroadcast(context, reqCode + 30, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                //          alarmIntent4 = PendingIntent.getBroadcast(context, reqCode + 40, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                alarmIntent5 = PendingIntent.getBroadcast(context, reqCode + 50, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            }
            alarmManager.cancel(alarmIntent1);
            if ( doubleAlarm ) {
                //            alarmManager.cancel(alarmIntent2);
                alarmManager.cancel(alarmIntent3);
                //            alarmManager.cancel(alarmIntent4);
                alarmManager.cancel(alarmIntent5);
            }
            Timestamp prayDt = prayClass.getPrayDt();
            L.d(TAG, "알람 취소", reqCode + 10, DateFormatUtils.format(prayDt.toDate(), "yyyy-MM-dd HH:mm:ss"), prayClass.getPrayDt(), prayClass.getTitle(), prayClass.getDocId());
            if ( doubleAlarm ) {
                L.d(TAG, "알람 취소", reqCode + 30, DateFormatUtils.format(prayDt.toDate(), "yyyy-MM-dd HH:mm:ss"), prayClass.getPrayDt(), prayClass.getTitle(), prayClass.getDocId());
                L.d(TAG, "알람 취소", reqCode + 50, DateFormatUtils.format(prayDt.toDate(), "yyyy-MM-dd HH:mm:ss"), prayClass.getPrayDt(), prayClass.getTitle(), prayClass.getDocId());
            }
        } else { /** 일괄알람 */
            alarmIntent1 = PendingIntent.getBroadcast(context, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(alarmIntent1);
            L.d(TAG, "알람 취소", reqCode);
        }

        if (callback != null) callback.onResult(prayClass);
    }

    public static void cancelBindAlarm(Context context) {
        for (int idx=0; idx < PRAY_CLASS_ALARM_MAX_COUNT; idx++) {
            AlarmUtil.cancelAlarm(context,null, null,PRAY_CLASS_ALARM_BASE_REQUEST_CODE +idx);
        }
        L.d(TAG, "BindAlarm 알람 모두 취소");
    }

    public static void bindAlarm(Context context, ParamCallback callback) {
//        final ArrayList<PrayClass>[] lists = new ArrayList[]{new ArrayList<>()};
        // Firestore Timestamp로 현재 시간 가져오기
        Timestamp fromTimestamp = Timestamp.now();  // 현재 시간

        // 현재 시간에서 1일 추가 (to 값)
     // long oneDayInSec = 24 * 60 * 60 + ( 12 * 60 * 60 ) ;  // 1일 + 12시간 을 밀리초로 계산
        long oneDayInSec = WApplication.basicSettings.prayClassSelectTimeSec;  // 1일 + 12시간 을 밀리초로 계산
        Timestamp toTimestampOneDay = new Timestamp(fromTimestamp.getSeconds() + oneDayInSec, fromTimestamp.getNanoseconds());

//        // 현재 시간에서 1일 추가 (to 값)
//        long oneDayInSec = 24 * 60 * 60;  // 1일을 밀리초로 계산
//        Timestamp toTimestampOneDay = new Timestamp(fromTimestamp.getSeconds() + oneDayInSec, fromTimestamp.getNanoseconds());

/**
 * check서비스가 동작시. 호출한다. ( 동작주기는 1일, 동작주기 15분 ? )
 * 현재시간 ~ 현재시간 + 1일 범위로 조회된 기도 모임이 있으면,
 * 알람을 생성하고 , 프리퍼런스에 documentId를 Array로 저장한다.
 * 호출시 프리퍼런스에 documentId가 존재하면 알람을 생성하지 않는다...
 */
        /** 현재시간 ~ 현재시간 + 1
        FbGodWhisper.selectTodayToOneDayPrayClass(context,Timestamp.now(), toTimestampOneDay , new ParamCallback<List<PrayClass>>() {
         * */
        FbGodWhisper.selectTodayToOneDayPrayClass(context,Timestamp.now(), toTimestampOneDay , new ParamCallback<List<PrayClass>>() {
            @Override
            public void onResult(List<PrayClass> result) {
                AlarmUtil.cancelBindAlarm(context);
                if ( result != null ) {
                    int idx=0;
                    for (PrayClass prayClass : result) {
                        prayClass.getDocId();
                        prayClass.getTitle();
                        prayClass.getPrayDt();
                        prayClass.getUuid();
                        AlarmUtil.cancelAlarm(context,prayClass, null); /** 지정알람 삭제 */
                        /** 일괄알람으로 등록 */
                        AlarmUtil.addAlarm(context,prayClass, null); /** 지정알람 등록 */
//                        AlarmUtil.addAlarm(context,prayClass, null,PRAY_CLASS_ALARM_BASE_REQUEST_CODE +idx); /** 일괄코드 삭제 */
                        idx++;
                        if ( idx > AlarmUtil.PRAY_CLASS_ALARM_MAX_COUNT - 1 ) break;
                    }
                }
                callback.onResult(result);
            }
        });


    }
}

