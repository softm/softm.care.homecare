package net.softm.data.kata.job;

import android.content.Context;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.softm.data.kata.common.ParamCallback;
import net.softm.data.kata.god.dto.PrayClass;
import net.softm.data.kata.util.AlarmUtil;
import net.softm.data.kata.util.CheckServiceUtil;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.RestUtil;
import net.softm.data.kata.util.Utils;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CheckServiceWorker extends Worker {

    private static final String TAG = "CheckServiceWorker";
    Context mContext = null;

    public static String TYPE_REPEAT = "repeat";
    public static String TYPE_ONETIME = "onetime";

    public static long lastCheckTime = System.currentTimeMillis();
    public static long lastRepeatInterval;
    public static TimeUnit lastRepeatIntervalTimeUnit;
    public static long lastFlexInterval;
    public static TimeUnit lastFlexIntervalTimeUnit;
    public static long lastInitialDelay;
    public static TimeUnit lastInitialDelayTimeUnit;

    public CheckServiceWorker(Context context, WorkerParameters workerParams){
        super(context, workerParams);
        mContext = context;
    }

    /**
     * # type == TYPE_REPEAT
     *    {@link net.softm.data.kata.util.RestUtil#getBasicInfo} in {@link net.softm.data.kata.MainConnectReceiver#start} or
     *                                                                      {@link net.softm.data.kata.MainActivity#onCreate(Bundle)} 에서 호출됨
     *    {@link net.softm.data.kata.util.WorkerUtil#startCheckService} 에서 호출됨
     *    @link WorkerUtil#startCheckService 에서 호출됨
     * # type == TYPE_ONETIME :
     *    {@link net.softm.data.kata.WApplication#_unCaughtExceptionHandler} ->
     *    {@link net.softm.data.kata.util.WorkerUtil#crashLog} ->
     *    {@link net.softm.data.kata.util.WorkerUtil#startCheckServiceImmediate10SecForCrash}
     * @return
     */
    @Override
    public Result doWork() {
        L.i(TAG,"doWork","CheckServiceWorker");
        Fa.i(mContext,TAG,Fa.EVENT_SERVICE_CHECKER_DO_WORK);
        String type = "";
        try {
            Data inputData = getInputData();
            long timestamp;
            type = inputData.getString("type");
            type = Utils.isEmpty(type)?TYPE_REPEAT:type;
            if (TYPE_ONETIME.equals(type)) {
                timestamp = inputData.getLong("timestamp", System.currentTimeMillis());
            } else {
                timestamp = System.currentTimeMillis();
            }
            boolean sendLog = false;
            String notiTitle;
            String notiMsg;
// 티맵자동실행만 사용
//            if ( ServiceUtil.isDestory() ) { /** 서비스 죽은 경우 */
//                ServiceUtil.init(mContext).startServiceFromWorker();
//                Fa.i(mContext,TAG,Fa.EVENT_SERVICE_CHECKER_RESCUE_SERVICE,type);
//                L.i(TAG,"doWork",type, "Service.rescue_service[" + getRunAttemptCount()+"]");
//                /** 1. onetime : Crash시  실행됨 */
//                /** 2. repeat : 주기성 실행 WApplication.basicInfoDTO.checkServiceLogIntervalTime */
//                if ( TYPE_ONETIME.equals(type) || CheckServiceUtil.isAvailableLogForIntervalTime() ) {
//                    sendLog = true;
//                    Fb.logCheckService(mContext,type, timestamp, "rescue_service[" + getRunAttemptCount()+"]");
//                }
//                notiTitle = "CheckService.rescue passed " + ((System.currentTimeMillis() - lastCheckTime) /1000/60) + " Min";
//            } else { /** 서비스 살아 있는 경우 */
//                L.i(TAG,"doWork",type, "Service.alive");
//                if ( TYPE_ONETIME.equals(type) || CheckServiceUtil.isAvailableLogForIntervalTime() ) {
//                    sendLog = true;
//                    if ( TYPE_REPEAT.equals(type) ) {
//                        Fa.i(mContext,TAG,Fa.EVENT_SERVICE_CHECKER_ALIVE,type);
//                    }
//                    Fb.logCheckService(mContext,type, timestamp, "alive[" + getRunAttemptCount()+"]");
//                }
//                notiTitle = "CheckService.alive passed " + ((System.currentTimeMillis() - lastCheckTime) /1000/60) + " Min";
//            }

//        /** 서비스워커가 동작했는지에 대한 노티를 발생 시킬 것인가? */
//            /** 연구소 설정 :
//             *  1. "체크알림" 기본 == 안함 {@link UseStatusMode.NONE}
//             *      => basic_info.json의 유효성을 체크함
//             *          {@link net.softm.data.kata.dto.BasicInfoDTO#checkServiceCheckNoti} == true
//             *          {@link net.softm.data.kata.dto.BasicInfoDTO#checkServiceCheckNotiUUIDs} NotEmpty < 값이 할당 되어야 함, 없으면 조건 미성립 상태로 만듦.
//             *          {@link net.softm.data.kata.dto.BasicInfoDTO#checkServiceCheckNotiUUIDs} Exists DeviceUtil.getUUID(MainActivity.this)
//             * 2. "체크알림" 기본 == On  {@link UseStatusMode.ENABLE}
//             */
//            if (( MapUtil.getCheckServiceCheckNoti().equals(UseStatusMode.NONE) && /** */
//                  CheckServiceUtil.isCheckServiceCheckNoti() &&
//                  Utils.isNotEmpty(CheckServiceUtil.getCheckServiceCheckNotiUUIDs()) &&
//                  CheckServiceUtil.isCheckServiceCheckNotiUUIDs(DeviceUtil.getUUID(mContext))
//                ) || MapUtil.getCheckserviceCheckLogSendNoti().equals(UseStatusMode.ENABLE)
//            ) {
//                notiMsg = String.format("%s - %s", DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.ms"),"check");
//                Fa.i(mContext,TAG,Fa.EVENT_SERVICE_CHECK_NOTI,type,DeviceUtil.getUUID(mContext));
//                NotificationUtil.init(mContext).send(notiTitle, notiMsg, true);
//            }
//
//        /** 서비스워커가 동작 로그 발송에대한 노티를 발생 시킬 것인가? */
//            /** 연구소 설정 :
//             *  1. 체크알림 로그 == 안함 {@link UseStatusMode.NONE}
//             *      => basic_info.json의 유효성을 체크함
//             *          {@link net.softm.data.kata.dto.BasicInfoDTO#checkServiceCheckLogSendNoti} == true
//             *          {@link net.softm.data.kata.dto.BasicInfoDTO#checkServiceCheckLogSendNotiUUIDs} NotEmpty < 값이 할당 되어야 함, 없으면 조건 미성립 상태로 만듦.
//             *          {@link net.softm.data.kata.dto.BasicInfoDTO#checkServiceCheckLogSendNotiUUIDs} Exists DeviceUtil.getUUID(MainActivity.this)
//             * 2. 체크알림 로그 == On  {@link UseStatusMode.ENABLE}
//             */
//            if ( sendLog ) {
//                if (
//                    ( MapUtil.getCheckserviceCheckLogSendNoti().equals(UseStatusMode.NONE) &&
//                      CheckServiceUtil.isCheckServiceCheckLogSendNoti() &&
//                      Utils.isNotEmpty(CheckServiceUtil.getCheckServiceCheckLogSendNotiUUIDs() ) &&
//                      CheckServiceUtil.isCheckServiceCheckLogSendNotiUUIDs(DeviceUtil.getUUID(mContext))
//                    ) || MapUtil.getCheckServiceCheckNoti().equals(UseStatusMode.ENABLE)
//                ) {
//                    notiMsg = String.format("%s - %s",DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.ms"), "log");
//                    Fa.i(mContext,TAG,Fa.EVENT_SERVICE_CHECK_LOG_SEND_NOTI,type,DeviceUtil.getUUID(mContext));
//                    NotificationUtil.init(mContext).send(notiTitle, notiMsg, true);
//                }
//            }
            /**
             {@link net.softm.data.kata.WApplication.basicInfoDTO.checkServiceRepeatInterval } 주기로
             반복하면서 알람을 설정함
             */
            RestUtil.getBasicSettings(mContext,()->{
                AlarmUtil.bindAlarm(mContext, new ParamCallback<List<PrayClass>>() {
                    @Override
                    public void onResult(List<PrayClass> result) {
                        if ( result.size() > 0 ) {
                            L.d(TAG,"bindAlarm",TAG);
                            Utils.toastDebug(mContext, String.format("CheckServiceWorker 주기 : [%s]초 bindAlarm", CheckServiceUtil.getCheckServiceRepeatInterval() , CheckServiceUtil.getCheckServiceRepeatIntervalTimeUnit() ));
                        }
                    }
                });
            },false);
            lastCheckTime = System.currentTimeMillis();
        } catch (Exception ex) {
            L.e(TAG,"doWork",type, ex);
            return Result.failure();
        }
        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

}
