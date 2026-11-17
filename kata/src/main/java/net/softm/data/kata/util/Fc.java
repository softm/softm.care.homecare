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
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.LogFormatType;

import org.apache.commons.lang3.StringUtils;


public class Fc {
    private static final String TAG = "Fc";
    public static String CUSTOM_KEY_WORK_TYPE = "WORK_TYPE";
    public static String CUSTOM_KEY_ACS_ENABLE_COUNT = "ACS_ENABLE_COUNT";
    public static String CUSTOM_KEY_ACTON_START_LOCK_CANCEL_COUNT = "ACTON_START_LOCK_CANCEL_COUNT";
    public static String CUSTOM_KEY_ACTON_STOP_LOCK_CANCEL_COUNT = "ACTON_STOP_LOCK_CANCEL_COUNT";
    public static String CUSTOM_KEY_ACTON_START_UNLOCK_CANCEL_COUNT = "ACTON_START_UNLOCK_CANCEL_COUNT";
    public static String CUSTOM_KEY_ACTON_STOP_UNLOCK_CANCEL_COUNT = "ACTON_STOP_UNLOCK_CANCEL_COUNT";

    public static String CUSTOM_KEY_RUNNAVI_RUN_TO_ROUTE_URI = "RUNNAVI_RUN_TO_ROUTE_URI";
    public static String CUSTOM_KEY_RUNNAVI_RUN_SEARCH_TO_DESTINATION = "RUNNAVI_RUN_SEARCH_TO_DESTINATION";
    public static String CUSTOM_KEY_RUNNAVI_URI = "RUNNAVI_URI";
    public static String CUSTOM_KEY_BASIC_INFO = "CUSTOM_KEY_BASIC_INFO";

    public static void setCustomKey(String key, String value) {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        int chunkSize = 1024;
        int length = value.length();
        int index = 1;
        for (int i = 0; i < length; i += chunkSize) {
            String chunk = value.substring(i, Math.min(i + chunkSize, length));
            crashlytics.setCustomKey(key + (( value.length() > 1024 )?index++:""), chunk);
        }
        L.i(TAG, "firebaseSetCustomKey",key ,value);
    }

    public static String i(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.i(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String i(Context ctx, String TAG, String key, Throwable value) {
        return i(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String d(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.d(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String d(Context ctx, String TAG, String key, Throwable value) {
        return d(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String w(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.w(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String w(Context ctx, String TAG, String key, Throwable value) {
        return w(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String e(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.e(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String e(Context ctx, String TAG, String key, Throwable value) {
        return e(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String v(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.v(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String v(Context ctx, String TAG, String key, Throwable value) {
        return v(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String wtf(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.wtf(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String wtf(Context ctx, String TAG, String key, Throwable value) {
        return wtf(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String getMsg(String key, String value) {
        String msg = key;
        if ( Utils.isNotEmpty(value) ) {
            msg += " : " + value;
        }
        return msg;
    }

    public static String log(Context ctx, String TAG, String key, Object ... value) {
        key = StringUtils.defaultString(key);
        String sValue = StringUtils.defaultString(Fa.genValue(value));
        FirebaseCrashlytics mfc = FirebaseCrashlytics.getInstance();
        String faKey = TAG + "__" + key.replaceAll("[^a-zA-Z0-9_]","_");
        faKey = faKey.substring(0,faKey.length()>40?40:faKey.length());
        String msg = getMsg(faKey,sValue);
        mfc.log(msg);
        return msg;
    }
    public static String log(Context ctx, String TAG, String key, Throwable value) {
        return log(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static FirebaseCrashlytics instance() {
        return FirebaseCrashlytics.getInstance();
    }

    public static void recordException(@NonNull Throwable throwable) {
        instance().recordException(throwable);
    }

    /** 크래시 로깅 여부 */
    public static boolean isCrashLog() {
        try {
            return WApplication.basicInfoDTO.crashLog;
        } catch (Exception ex) {
            return false;
        }
    }

    /** 크래시 로깅 포맷 */
    public static LogFormatType getCrashLogFormat() {
        try {
            return WApplication.basicInfoDTO.crashLogFormat;
        } catch (Exception ex) {
            return LogFormatType.DEFAULT;
        }
    }

/** ------------------------------------------------ */
    /** 정상적인 비정상 종료 여부 : 너무 빈번하게. 1분이내 30초이내로 15초이내, 10초이내, 5초이내 라면 정상적인 비정상 오류 발생이 아니라 무한루프 오류 상황임. */
    public static boolean isValildCrash() {
        L.d(TAG,"getLastCheckCrashTime()",getLastCheckCrashTime());
        L.d(TAG,"getCheckCrashIntervalTime()",getCheckCrashIntervalTime());
        return (
            System.currentTimeMillis() - getLastCheckCrashTime() >= getCheckCrashIntervalTime()
        );
    }

    /** 비정상 종료 체크 간격 */
    public static long getCheckCrashIntervalTime() {
        try {
            return WApplication.basicInfoDTO.checkCrashIntervalTime;
        } catch (Exception ex) {
            return 12000;
        }
    }

    /**
     * 비정상 종료 마지막 시간
     * @return
     */
    public static long getLastCheckCrashTime() {
        long v  = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_LAST_CHECK_CRASH_TIME, 0L);
        return v;
    }

    public static void setLastCheckCrashTime(long v) {
        WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_LAST_CHECK_CRASH_TIME,v);
    }

    public static void updateLastCheckCrashTime() {
        setLastCheckCrashTime(System.currentTimeMillis());
        L.d(TAG,"updateLastCheckCrashTime()",getLastCheckCrashTime());
    }

    public static boolean isCrashError() {
        String _enable = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_CRASH_ERROR, "0"); // 사용
        return  _enable.equals("1")?true:false;
    }

    public static void setCrashError(boolean enable) {
        WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_CRASH_ERROR, enable?"1":"0");
    }


}