package net.softm.data.kata.util;

import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.LogFormatType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CheckServiceUtil {
    private static final String TAG = CheckServiceUtil.class.getSimpleName();

//    public static boolean isAdingWaitReward() {
//        try {
//            return !AdMode.NONE.equals(CheckServiceUtil.getAdMode()) && !AdType.NONE.equals(WApplication.basicInfoDTO.adTypeForWaitReward);
//        } catch (Exception ex) {
//            return false;
//        }
//    }

    /** Service 체크 로깅 간격 */
    public static boolean isAvailableLogForIntervalTime() {
        return (
                System.currentTimeMillis() - CheckServiceUtil.getLastCheckServiceLogTime() >= CheckServiceUtil.getCheckServiceLogIntervalTime()
        );
    }

    /** Service 체크 - 전체 활성화  */
    public static boolean isCheckServiceAllEnable() {
        try {
            return WApplication.basicInfoDTO.checkServiceAllEnable;
        } catch (Exception ex) {
            return false;
        }
    }

    /** Service 체크 */
    public static boolean isCheckService() {
        try {
            return WApplication.basicInfoDTO.checkService;
        } catch (Exception ex) {
            return false;
        }
    }

    /** Service 체크 실행 대상 uuid : checkService false여도 동작 */
    public static List<String> getCheckServiceUUIDs() {
        try {
            return WApplication.basicInfoDTO.checkServiceUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
    public static boolean isCheckServiceUUIDs(String uuid) {
        try {
            return CheckServiceUtil.getCheckServiceUUIDs().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }

    /** Service 체크 로깅*/
    public static boolean isCheckServiceLog() {
        try {
            return WApplication.basicInfoDTO.checkServiceLog;
        } catch (Exception ex) {
            return false;
        }
    }

    /** Service 체크 로깅 대상 UUID */
    public static List<String> getCheckServiceLogUUIDs() {
        try {
            return WApplication.basicInfoDTO.checkServiceLogUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
    public static boolean isCheckServiceLogUUIDs(String uuid) {
        try {
            return CheckServiceUtil.getCheckServiceLogUUIDs().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }

    public static long getCheckServiceLogIntervalTime() {
        try {
            return WApplication.basicInfoDTO.checkServiceLogIntervalTime;
        } catch (Exception ex) {
            return 0;
        }
    }

    /** Service 체크 로깅 포맷 */
    public static LogFormatType getCheckServiceLogFormat() {
        try {
            return WApplication.basicInfoDTO.checkServiceLogFormat;
        } catch (Exception ex) {
            return LogFormatType.DEFAULT;
        }
    }

    /** Service 체크 : 반복 시간 */
    public static long getCheckServiceRepeatInterval() {
        try {
            return WApplication.basicInfoDTO.checkServiceRepeatInterval;
        } catch (Exception ex) {
            return 15;
        }
    }
    /** Service 체크 : 반복 시간 단위 */
    public static TimeUnit getCheckServiceRepeatIntervalTimeUnit() {
        try {
            return WApplication.basicInfoDTO.checkServiceRepeatIntervalTimeUnit;
        } catch (Exception ex) {
            return TimeUnit.MINUTES;
        }
    }

    /** Service 체크 : 유연 시간 */
    public static long getCheckServiceFlexInterval() {
        try {
            return WApplication.basicInfoDTO.checkServiceFlexInterval;
        } catch (Exception ex) {
            return 5;
        }
    }
    /** Service 체크 : 유연 시간 단위 */
    public static TimeUnit getCheckServiceFlexIntervalTimeUnit() {
        try {
            return WApplication.basicInfoDTO.checkServiceFlexIntervalTimeUnit;
        } catch (Exception ex) {
            return TimeUnit.MINUTES;
        }
    }

    /** Service 체크 : 시작 지연 시간 */
    public static long getCheckServiceInitialDelay() {
        try {
            return WApplication.basicInfoDTO.checkServiceInitialDelay;
        } catch (Exception ex) {
            return 5;
        }
    }

    /** Service 체크 : 시작 지연 시간 단위 */
    public static TimeUnit getCheckServiceInitialDelayTimeUnit() {
        try {
            return WApplication.basicInfoDTO.checkServiceInitialDelayTimeUnit;
        } catch (Exception ex) {
            return TimeUnit.MINUTES;
        }
    }

    /** 크래쉬 상황시 Service 체크 */
    public static boolean isCheckServiceForCrash() {
        try {
            return WApplication.basicInfoDTO.checkServiceForCrash;
        } catch (Exception ex) {
            return false;
        }
    }

    /** 크래쉬 상황시 Service 체크 로깅 */
    public static boolean isCheckServiceLogForCrash() {
        try {
            return WApplication.basicInfoDTO.checkServiceLogForCrash;
        } catch (Exception ex) {
            return false;
        }
    }

    /** 크래쉬 상황시 Service 체크 로깅 포맷 */
    public static LogFormatType getCheckServiceLogFormatForCrash() {
        try {
            return WApplication.basicInfoDTO.checkServiceLogFormatForCrash;
        } catch (Exception ex) {
            return LogFormatType.DEFAULT;
        }
    }

    /** 크래쉬 상황시 Service 체크 로깅 지연 시간 */
    public static int getCheckServiceDelaySecForCrash() {
        try {
            return WApplication.basicInfoDTO.checkServiceDelaySecForCrash;
        } catch (Exception ex) {
            return 5;
        }
    }

    /** Service 체크 실행시 노티 표시 여부 */
    public static boolean isCheckServiceCheckNoti() {
        try {
            return WApplication.basicInfoDTO.checkServiceCheckNoti;
        } catch (Exception ex) {
            return false;
        }
    }

    /** Service 체크 실행시 노티 표시할 uuid */
    public static List<String> getCheckServiceCheckNotiUUIDs() {
        try {
            return WApplication.basicInfoDTO.checkServiceCheckNotiUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
    public static boolean isCheckServiceCheckNotiUUIDs(String uuid) {
        try {
            return CheckServiceUtil.getCheckServiceCheckNotiUUIDs().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }


    /** Service 체크 실행시 로그를 보냈는지를 노티 표시 여부 */
    public static boolean isCheckServiceCheckLogSendNoti() {
        try {
            return WApplication.basicInfoDTO.checkServiceCheckLogSendNoti;
        } catch (Exception ex) {
            return false;
        }
    }

    /** Service 체크 실행시 로그를 보냈는지를 노티 표시할 uuid */
    public static List<String> getCheckServiceCheckLogSendNotiUUIDs() {
        try {
            return WApplication.basicInfoDTO.checkServiceCheckLogSendNotiUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
    public static boolean isCheckServiceCheckLogSendNotiUUIDs(String uuid) {
        try {
            return CheckServiceUtil.getCheckServiceCheckLogSendNotiUUIDs().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }


    /**
     * Firebase에 CheckService 마지막 로그 남긴 시간
     * @return
     */
    public static long getLastCheckServiceLogTime() {
        long v  = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_LAST_CHECK_SERVICE_LOGTIME, 0L);
        return v;
    }

    public static void updateLastCheckServiceLogTime() {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_LAST_CHECK_SERVICE_LOGTIME,System.currentTimeMillis());
    }
}

