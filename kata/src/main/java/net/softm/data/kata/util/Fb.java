/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.softm.data.kata.util;

import android.content.Context;

import com.android.billingclient.api.Purchase;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import net.softm.data.kata.BuildConfig;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.BillingType;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.CrashErrorType;
import net.softm.data.kata.common.InstallType;
import net.softm.data.kata.common.LogFormatType;
import net.softm.data.kata.job.CheckServiceWorker;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fb {
    private static final String TAG = "Fb";

    public static FirebaseFirestore instance() {
        return FirebaseFirestore.getInstance();
    }

    static String COLLECTION_CRASH_REPORTS = "crashReports"; /** 비정상 종료 로깅 */
    static String COLLECTION_CRASH_ERROR = "crashError"; /** 구글 광고 오류 로깅 & 적용은 안ㄴ했지만 구글 결제 오류 로깅도 구성함 */
    static String COLLECTION_CHECK_SERVICE = "checkService"; // checkService
    static String COLLECTION_USER = "user"; // checkService
    static String COLLECTION_LIVE_USER = "liveUser"; // checkService
    static String COLLECTION_PURCHASE = "purchase"; // checkService

    public static void logCrash(Context context, String crashMessage, long timestamp) {
        // 여기부터. 왜 널포인트 나지?
        if ( Fc.isCrashLog() ) {
            DeviceInfoExtractor deviceInfoExtractor = new DeviceInfoExtractor(context);
            // 기기 정보 및 OS 정보 추출
            Map<String, Object> deviceInfo = deviceInfoExtractor.getDeviceInfo();
            Map<String, Object> osInfo = deviceInfoExtractor.getOSInfo();

            // 비정상 종료 정보
//        Map<String, Object> crashInfo = new HashMap<>();
//        crashInfo.put("timestamp", timestamp);
//        crashInfo.put("versionCode", versionCode);
//        crashInfo.put("versionName", versionName);
//        crashInfo.put("crashMessage", crashMessage);

            // 통합 데이터 생성
            Map<String, Object> document = new HashMap<>();
            document.put("uuid", DeviceUtil.getUUID(context));
            document.put("wDate", DateFormatUtils.format(timestamp, "yyyy-MM-dd HH:mm:ss.ms"));
            document.put("message", crashMessage);
            getDefaultDocument(context, document, Fc.getCrashLogFormat());
            setDefaultParameters(context,document);

            /** Firestore에 데이터 추가 */
            instance().collection(COLLECTION_CRASH_REPORTS)
                    .add(document)
                    .addOnSuccessListener(documentReference -> {
                        Fa.i(context, TAG, Fa.EVENT_CRASH_LOG_TO_FIRESTORE, documentReference.getId());
                        L.d(TAG, Fa.EVENT_CRASH_LOG_TO_FIRESTORE + " : " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Fa.i(context, TAG, Fa.EVENT_CRASH_LOG_TO_FIRESTORE_NOT_SEND);
                        L.d(TAG, Fa.EVENT_CRASH_LOG_TO_FIRESTORE_NOT_SEND + " : " + e.getMessage());
                    });
        }
    }
    public static Map<String, Object> getDefaultDocument(Context context, Map<String, Object> document, LogFormatType logFormatType) {
        try {
            DeviceInfoExtractor deviceInfoExtractor = new DeviceInfoExtractor(context);
            deviceInfoExtractor.getDeviceInfo();
            deviceInfoExtractor.getOSInfo();
            document.put("brand", deviceInfoExtractor.getBrand());
            document.put("model", deviceInfoExtractor.getModel());
            if ( LogFormatType.DEFAULT.equals(logFormatType) ) {
                document.put("diskSpaceAvailable", deviceInfoExtractor.getDiskSpaceAvailable());
                document.put("ramAvailable", deviceInfoExtractor.getRamAvailable());
            }
            document.put("orientation", deviceInfoExtractor.getOrientation());
//        document.put("version", Build.VERSION.RELEASE);
            document.put("versionId", PackageUtil.getAndroidVersionId());
            document.put("sdkInt", android.os.Build.VERSION.SDK_INT);
            document.put("versionName", BuildConfig.VERSION_NAME);
            document.put("versionCode", BuildConfig.VERSION_CODE);
        } catch (Exception ex) {
        }
        return document;
    }

    /**
     * @see
     * @param type == TYPE_REPEAT : check checkServiceLogUUIDs에 지정된 것만 남김
     * @param type == TYPE_ONETIME : WApplication#UnCaughtException ->  WorkerUtil#crashLog ->  WorkerUtil.startCheckServiceImmediate10SecForCrash
     * @param context
     * @param type
     * @param timestamp
     * @param message
     */
    public static void logCheckService(Context context, String type, long timestamp, String message) {
        if (
            ( CheckServiceWorker.TYPE_REPEAT.equals(type) && CheckServiceUtil.isCheckServiceLog()  ) ||
            ( CheckServiceWorker.TYPE_ONETIME.equals(type) && CheckServiceUtil.isCheckServiceLogForCrash())
        ) {
            DeviceInfoExtractor deviceInfoExtractor = new DeviceInfoExtractor(context);
            Map<String, Object> deviceInfo = deviceInfoExtractor.getDeviceInfo();
            Map<String, Object> osInfo = deviceInfoExtractor.getOSInfo();

            // 통합 데이터 생성
            Map<String, Object> document = new HashMap<>();
            document.put("uuid", DeviceUtil.getUUID(context));
            document.put("type", type);
            document.put("wDate", DateFormatUtils.format(timestamp, "yyyy-MM-dd HH:mm:ss.ms"));
            document.put("message", message);

            if ( CheckServiceWorker.TYPE_REPEAT.equals(type) ) {
                getDefaultDocument(context, document,CheckServiceUtil.getCheckServiceLogFormat());
            }

            if ( CheckServiceWorker.TYPE_ONETIME.equals(type) ) {
                getDefaultDocument(context, document,CheckServiceUtil.getCheckServiceLogFormatForCrash());
            }

            boolean loggingExec = false;
            if ( CheckServiceWorker.TYPE_REPEAT.equals(type) ) {
//                if ( Utils.isEmpty(WApplication.basicInfoDTO.checkServiceLogUUIDs) ) {
//                    loggingExec = true;
//                } else {
                    loggingExec = CheckServiceUtil.isCheckServiceLogUUIDs(DeviceUtil.getUUID(context));
//                }
            } else { // TYPE_IMMEDIATE
                loggingExec = true;
            }
            if ( loggingExec ) {
                Map<String, Object> logInfo = new HashMap<>();
                logInfo.put("repeatInterval", CheckServiceWorker.lastRepeatInterval);
                logInfo.put("repeatIntervalTimeUnit", CheckServiceWorker.lastRepeatIntervalTimeUnit);
                logInfo.put("flexInterval", CheckServiceWorker.lastFlexInterval);
                logInfo.put("flexIntervalTimeUnit", CheckServiceWorker.lastFlexIntervalTimeUnit);
                logInfo.put("initialDelay", CheckServiceWorker.lastInitialDelay);
                logInfo.put("initialDelayTimeUnit", CheckServiceWorker.lastInitialDelayTimeUnit);

                document.put("logInfo", logInfo);
                document.put("logIntervalTime", CheckServiceUtil.getCheckServiceLogIntervalTime());

                CheckServiceUtil.updateLastCheckServiceLogTime(); /** 마지막 로깅 남긴 시간 기록 */

                setDefaultParameters(context,document);

                /** Firestore에 데이터 추가 */
                instance().collection(COLLECTION_CHECK_SERVICE)
                        .add(document)
                        .addOnSuccessListener(documentReference -> {
                            Fa.i(context, TAG, Fa.EVENT_SERVICE_CHECK_LOG_TO_FIRESTORE, documentReference.getId());
                            L.d(TAG, Fa.EVENT_SERVICE_CHECK_LOG_TO_FIRESTORE + " : " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            Fa.i(context, TAG, Fa.EVENT_SERVICE_CHECK_LOG_TO_FIRESTORE_NOT_SEND);
                            L.d(TAG, Fa.EVENT_SERVICE_CHECK_LOG_TO_FIRESTORE_NOT_SEND + " : Error " + e.getMessage());
                        });
            }
        }
    }

    private static void setDefaultParameters(Context context, Map<String, Object>  document)  {
        try {
            document.put(Utils.toCamelCase("license"), LicenseUtil.isLicense());
            document.put(Utils.toCamelCase("user_level"), LicenseUtil.getUserLevel());
            document.put(Utils.toCamelCase("user_type"), LicenseUtil.getUserType());
            document.put(Utils.toCamelCase("license_paid"), LicenseUtil.isPaidLicense());
            document.put(Utils.toCamelCase("license_subs"), LicenseUtil.isSubsLicense());
            document.put(Utils.toCamelCase("license_lifetime"), LicenseUtil.isLifeTimeLicense());
            document.put("service_connect_isLive", ServiceUtil.isLive());

            Map<String, Object> settingInfo = new HashMap<>();
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_APP_ENABLE), MapUtil.getAppEnable());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_APP_PACKAGE), MapUtil.getAppPackage());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_MUSIC_APP_PACKAGE), MapUtil.getMusicAppPackage());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_INDEPENDENCE_CONDITION), MapUtil.isIndependenceCondition());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_NAVI_MODE), MapUtil.getNaviMode().name());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_NAVI_AUTO_START), MapUtil.getNaviAutoStart().name());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_CHARGING_MODE), MapUtil.getChargingMode().name());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_START_POWER), MapUtil.isStartPower());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_START_WIFI), MapUtil.isStartWifi());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_START_BLUETOOTH), MapUtil.isStartBluetooth());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_STOP_MODE), MapUtil.getStopMode().name());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_NAVI_POPUP_MODE), MapUtil.isOverlayOpen());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_WORK_GOTO_OFF_TIME), MapUtil.isEnableWorkGotoOffTime());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_WORK_GOTO_TIME_FROM), MapUtil.getWorkGotoTimeFrom());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_WORK_GOTO_TIME_TO), MapUtil.getWorkGotoTimeTo());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_START_STOP_ON_OFF), MapUtil.getStartStopOnOff());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_RENAVI_PREVENT_TIME), MapUtil.getPreventWaitOverlayTime());
            settingInfo.put(Utils.toCamelCaseForMyKey(Const.PREF_KEY_NAVI_OVERLAY_WAIT_TIME), MapUtil.getOverlayWaitTime());
            document.put("setting", settingInfo);

            document.put("service_accessibility", PermissionUtil.isAccessibilityServiceRunning(context));
        } catch (Exception ex) {
            L.e(TAG,"setDefaultParameters", ex);
        }
    }

    /** 사용자 등록 */
    public static boolean isDoRegUserInfo() {
        try {
            return WApplication.basicInfoDTO.doRegUserInfo;
        } catch (Exception ex) {
            return false;
        }
    }

    /** 사용자 등록 로깅 포맷 */
    public static LogFormatType getUserInfoLogFormat() {
        try {
            return WApplication.basicInfoDTO.userInfoLogFormat;
        } catch (Exception ex) {
            return LogFormatType.DEFAULT;
        }
    }

    public static void userRegister(Context context, InstallType installType, String oldVersion, String newVersion ) {
        if (
            isDoRegUserInfo()
        ) {
            DeviceInfoExtractor deviceInfoExtractor = new DeviceInfoExtractor(context);
            Map<String, Object> deviceInfo = deviceInfoExtractor.getDeviceInfo();
            Map<String, Object> osInfo = deviceInfoExtractor.getOSInfo();

            // 통합 데이터 생성
            Map<String, Object> document = new HashMap<>();
            document.put("uuid", DeviceUtil.getUUID(context));
            document.put("wDate", DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.ms"));

            document.put("installType", installType.name());
            document.put("oldVersion", oldVersion);
            document.put("newVersion", newVersion);

            getDefaultDocument(context, document, Fb.getUserInfoLogFormat());
            try {
                instance().collection(COLLECTION_USER).whereEqualTo("uuid", DeviceUtil.getUUID(context)).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            /** 업데이트시는 Fb 전송 없음 */
                            List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                            for (DocumentSnapshot doc : docs) {
                                Fa.i(context, TAG, Fa.EVENT_USER_EXISTS, doc.getData());
                                Map<String, Object> updates = new HashMap<>();
                                // updates.put("userId", newUserId); // 기존 필드명과 새로운 값
                                updates.put("uDate",  DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.ms"));
                                doc.getReference().update(updates);
                            }
                        } else {
                            document.put("firstInstallType", installType.name());
                            document.put("firstOldVersion", oldVersion);
                            document.put("firstNewVersion", newVersion);
                            /** Firestore에 데이터 추가 */
                            instance().collection(COLLECTION_USER)
                                    .add(document)
                                    .addOnSuccessListener(documentReference -> {
                                        Fa.i(context, TAG, Fa.EVENT_USER_REGISTER, documentReference.getId());
                                        L.d(TAG, "user uuid to Firestore: " + documentReference.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Fa.i(context, TAG, Fa.EVENT_USER_REGISTER_ERROR);
                                        L.d(TAG, "Error user uuid : " + e.getMessage());
                                    });
                        }
                    } else {
                        L.e(TAG, "Error checking user existence : " + task.getException());
                    }
                });
            } catch (Exception ex) {
                Fc.log(context,TAG,"userRegister",ex);
            }

        }
    }

    public static void purchase(Context context, Purchase purchase, BillingType billingType, String productId, String purchaseToken, long purchaseTime) {
            DeviceInfoExtractor deviceInfoExtractor = new DeviceInfoExtractor(context);
            Map<String, Object> deviceInfo = deviceInfoExtractor.getDeviceInfo();
            Map<String, Object> osInfo = deviceInfoExtractor.getOSInfo();

            // 통합 데이터 생성
            Map<String, Object> document = new HashMap<>();
            document.put("status", "P");
            document.put("uuid", DeviceUtil.getUUID(context));
            document.put("wDate", DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.ms"));
            document.put("billingType", billingType.getId());
            document.put("productId", productId);
            document.put("purchaseToken", purchaseToken);
            document.put("purchaseTime", DateFormatUtils.format(purchaseTime, "yyyy-MM-dd HH:mm:ss.ms"));
            document.put("purchase", purchase.getOriginalJson());

            getDefaultDocument(context, document,WApplication.basicInfoDTO.purchaseLogFormat);
            setDefaultParameters(context,document);
            try {
                instance().collection(COLLECTION_PURCHASE).whereEqualTo("purchaseToken", purchaseToken).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                            for (DocumentSnapshot doc : docs) {
                                L.d(TAG, "purchase","중복입력 방지", purchaseToken);
                            }
                        } else {
            //               if ( BillingType.SUBS.equals(billingType) || BillingType.INAPP_LIFETIME.equals(billingType) )
                            /** Firestore에 데이터 추가 */
                            instance().collection(COLLECTION_PURCHASE)
                                    .add(document)
                                    .addOnSuccessListener(documentReference -> {
                                        Fa.i(context, TAG, Fa.EVENT_PURCHASE, documentReference.getId());
                                        L.d(TAG, "PURCHASE to Firestore: " + documentReference.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Fa.i(context, TAG, Fa.EVENT_COLLECTION_ERROR);
                                        L.d(TAG, "Error user uuid : " + e.getMessage());
                                    });
                        }
                    } else {
                        L.e(TAG, "Error checking user existence : " + task.getException());
                    }
                });
            } catch (Exception ex) {
                Fc.log(context,TAG,"purchase",ex);
            }

    }

    public static void purchaseUnRegister(Context context, BillingType billingType, String productId, String purchaseToken) {

        DeviceInfoExtractor deviceInfoExtractor = new DeviceInfoExtractor(context);
        Map<String, Object> deviceInfo = deviceInfoExtractor.getDeviceInfo();
        Map<String, Object> osInfo = deviceInfoExtractor.getOSInfo();

        // 통합 데이터 생성
        Map<String, Object> document = new HashMap<>();

        document.put("status", "U"); // UnRegister
        document.put("uuid", DeviceUtil.getUUID(context));
        document.put("wDate", DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.ms"));
        document.put("billingType", billingType.getId());
        document.put("productId", productId);
        document.put("purchaseToken", purchaseToken);

        getDefaultDocument(context, document,WApplication.basicInfoDTO.purchaseLogFormat);
        setDefaultParameters(context,document);

        if ( Utils.isEmpty(purchaseToken) ) return;

        try {
            /** Firestore에 데이터 추가 */
            instance().collection(COLLECTION_PURCHASE)
                    .add(document)
                    .addOnSuccessListener(documentReference -> {
                        Fa.i(context, TAG, Fa.EVENT_PURCHASE_UNREGISTER, documentReference.getId());
                        L.d(TAG, "PURCHASE to Firestore: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Fa.i(context, TAG, Fa.EVENT_COLLECTION_ERROR);
                        L.d(TAG, "Error user uuid : " + e.getMessage());
                    });
        } catch (Exception ex) {
            Fc.log(context,TAG,"purchaseUnRegister",ex);
        }
    }


    public static void liveUser(Context context) {
        if (
            isCheckLiveUser()
        ) {
            if ( !isAvailableForLiveUserLog() ) {
                String formatTime = Utils.millisecondsToTime(getCheckLiveUserLogIntervalTime() - ( System.currentTimeMillis() - getLastCheckLiveUserLogTime()));
                Utils.toastDebug(context, "다음 LiveUser 기록까지 : " + formatTime);
                L.d(TAG, "liveUser","Not Available Log For LiveUser - Time Interval / To Next Log Time : " + formatTime);
                return;
            }
            L.d(TAG, "liveUser","isAvailableForLiveUserLog");
            updateLastCheckLiveUserLogTime(); /** update last log time */

            DeviceInfoExtractor deviceInfoExtractor = new DeviceInfoExtractor(context);
            Map<String, Object> deviceInfo = deviceInfoExtractor.getDeviceInfo();
            Map<String, Object> osInfo = deviceInfoExtractor.getOSInfo();

            // 통합 데이터 생성
            Map<String, Object> document = new HashMap<>();
            document.put("uuid", DeviceUtil.getUUID(context));
            document.put("wDate", DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.ms"));

            getDefaultDocument(context, document, Fb.getLiveUserLogFormat());
            setDefaultParameters(context,document);

            try {
                 instance().collection(COLLECTION_LIVE_USER).whereEqualTo("uuid", DeviceUtil.getUUID(context)).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();
                                    for (DocumentSnapshot doc : docs) {
                                        Fa.i(context, TAG, Fa.EVENT_LIVE_USER_EVERYDAY, doc.getData());
                                        Map<String, Object> updates = new HashMap<>();
                                     // updates.put("userId", newUserId); // 기존 필드명과 새로운 값
                                        updates.put("uDate",  DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.ms"));
                                        doc.getReference().update(updates);
                                    }
                                } else {
                                    document.put("uDate",  DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss.ms"));
                                    /** Firestore에 데이터 추가 */
                                    instance().collection(COLLECTION_LIVE_USER)
                                            .add(document)
                                            .addOnSuccessListener(documentReference -> {
                                                Fa.i(context, TAG, Fa.EVENT_LIVE_USER_FIRST_REGISTER, documentReference.getId());
                                                L.d(TAG, "live user uuid to Firestore: " + documentReference.getId());
                                            })
                                            .addOnFailureListener(e -> {
                                                Fa.i(context, TAG, Fa.EVENT_USER_REGISTER_ERROR,e);
                                                L.d(TAG, "Error live user uuid : " + e.getMessage());
                                            });
                                }
                            } else {
                                L.e(TAG, "Error checking user existence : " + task.getException());
                            }
                        });
            } catch (Exception ex) {
                Fc.log(context,TAG,"liveUser",ex);
            }

        }
    }

    public static void crashError(Context context, String crashMessage, long timestamp, CrashErrorType crashErrorType, int errorCaseIndex) {
            // 여기부터. 왜 널포인트 나지?
            if ( Fc.isCrashLog() ) {
                DeviceInfoExtractor deviceInfoExtractor = new DeviceInfoExtractor(context);
                // 기기 정보 및 OS 정보 추출
                Map<String, Object> deviceInfo = deviceInfoExtractor.getDeviceInfo();
                Map<String, Object> osInfo = deviceInfoExtractor.getOSInfo();

                // 비정상 종료 정보
//        Map<String, Object> crashInfo = new HashMap<>();
//        crashInfo.put("timestamp", timestamp);
//        crashInfo.put("versionCode", versionCode);
//        crashInfo.put("versionName", versionName);
//        crashInfo.put("crashMessage", crashMessage);
                // 통합 데이터 생성
                Map<String, Object> document = new HashMap<>();
                document.put("uuid", DeviceUtil.getUUID(context));
                document.put("wDate", DateFormatUtils.format(timestamp, "yyyy-MM-dd HH:mm:ss.ms"));
                document.put("crashErrorType", crashErrorType.name());
                document.put("message", crashMessage);
                document.put("errorCaseIndex", errorCaseIndex);
                getDefaultDocument(context, document, Fc.getCrashLogFormat());
                setDefaultParameters(context,document);

                /** Firestore에 데이터 추가 */
                instance().collection(COLLECTION_CRASH_ERROR)
                        .add(document)
                        .addOnSuccessListener(documentReference -> {
                            Fa.i(context, TAG, Fa.EVENT_CRASH_ERROR_TO_FIRESTORE, documentReference.getId());
                            L.d(TAG, Fa.EVENT_CRASH_ERROR_TO_FIRESTORE + " : " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            Fa.i(context, TAG, Fa.EVENT_CRASH_ERROR_TO_FIRESTORE_NOT_SEND);
                            L.d(TAG, Fa.EVENT_CRASH_ERROR_TO_FIRESTORE_NOT_SEND + " : " + e.getMessage());
                        });
            }
    }


    /** 사용자 사용 여부 로깅 가능 */
    public static boolean isCheckLiveUser() {
        try {
            return WApplication.basicInfoDTO.checkLiveUser;
        } catch (Exception ex) {
            return false;
        }
    }
    /** 사용자 사용 여부 체크 로깅 가능 */
    public static boolean isAvailableForLiveUserLog() {
        return (
                System.currentTimeMillis() - getLastCheckLiveUserLogTime() >= getCheckLiveUserLogIntervalTime()
        );
    }

    /** 사용자 사용 여부 체크 로깅 간격 */
    public static long getCheckLiveUserLogIntervalTime() {
        try {
            return WApplication.basicInfoDTO.checkLiveUserLogIntervalTime;
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Firebase에 사용자 사용 마지막 로그 남긴 시간
     * @return
     */
    public static long getLastCheckLiveUserLogTime() {
        long v  = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_LAST_CHECK_LIVE_USER_LOGTIME, 0L);
        return v;
    }

    public static void setLastCheckLiveUserLogTime(long v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_LAST_CHECK_LIVE_USER_LOGTIME,v);
    }

    public static void updateLastCheckLiveUserLogTime() {
        setLastCheckLiveUserLogTime(System.currentTimeMillis());
    }

    /** 사용자 사용 로깅 포맷 */
    public static LogFormatType getLiveUserLogFormat() {
        try {
            return WApplication.basicInfoDTO.liveUserLogFormat;
        } catch (Exception ex) {
            return LogFormatType.DEFAULT;
        }
    }

    /** 파이어베이스 타임스탬프로 변환 */
    public static Timestamp toTimestamp(String v) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date date = sdf.parse(v);
            Timestamp timestamp = new Timestamp(date);
            return timestamp;
        } catch (Exception ex) {
            return null;
        }
    }
}