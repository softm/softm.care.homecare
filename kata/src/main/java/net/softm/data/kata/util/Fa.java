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
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import net.softm.data.kata.BuildConfig;
import net.softm.data.kata.common.Const;

import org.apache.commons.lang3.StringUtils;

public class Fa {
    public static final String EVENT_REGISTER_SUBS_LICENSE = "register_subs_license";
    public static final String EVENT_REGISTER_LIFETIME_LICENSE = "register_lifetime_license";
    public static final String EVENT_UNREGISTER_SUBS_LICENSE = "unregister_subs_license";
    public static final String EVENT_UNREGISTER_LIFETIME_LICENSE = "unregister_lifetime_license";
    public static final String EVENT_MUSIC_PLAY = "music_play";
    public static final String EVENT_MUSIC_PLAY_PREV = "music_play_prev" ;
    public static final String EVENT_MUSIC_PLAY_NEXT = "music_play_next" ;
    private static final String TAG = "Fa";

    public static final String EVENT_DESTORY_CONNECT_SERVICE = "destory_connect_service";
    public static final String EVENT_BOOT_COMPLETE= "boot_complete";
    public static final String EVENT_START_CANCEL_LOCK_TO_ON_SCREEN = "start_cancel_lock_to_on_screen";
    public static final String EVENT_START_CANCEL_UNLOCK = "start_cancel_unlock";
    public static final String EVENT_STOP_CANCEL_LOCK_TO_ON_SCREEN= "stop_cancel_lock_to_on_screen";
    public static final String EVENT_STOP_CANCEL_UNLOCK= "stop_cancel_unlock";
    public static final String EVENT_PACKAGE_REPLACED= "package_replaced";
    public static final String EVENT_APPUPDATE_FIRST_INSTALL= "appupdate_first_install";
    public static final String EVENT_APPUPDATE_SAME_INSTALL= "appupdate_same_install";
    public static final String EVENT_APPUPDATE_UPDATED= "appupdate_updated";
    public static final String EVENT_START_RECEIVER= "start_receiver";
    public static final String EVENT_STOP_RECEIVER= "stop_receiver";
    public static final String EVENT_START_RECEIVER_UNLOCK_SCREEN= "start_receiver_unlock_screen";
    public static final String EVENT_STOP_RECEIVER_UNLOCK_SCREEN= "stop_receiver_unlock_screen";

    public static final String EVENT_ACS_ENABLE= "acs_enable";
    public static final String EVENT_OVERLAY_VIEW_CLICK_HOME = "overlayview_click_home";
    public static final String EVENT_OVERLAY_VIEW_CLICK_COMPANY = "overlayview_click_company";
    public static final String EVENT_OVERLAY_VIEW_CLICK_START_APP = "overlayview_click_start_app";
    public static final String EVENT_OVERLAY_VIEW_CLICK_CLOSE = "overlayview_click_close";

    public static final String EVENT_OVERLAY_VIEW_CLICK_PLAY = "overlay_view_click_play";
    public static final String EVENT_OVERLAY_VIEW_CLICK_PAUSE = "overlay_view_click_pause";

    public static final String EVENT_APP_ENABLE = "app_enable";
    public static final String EVENT_APP_DISABLE = "app_disable";
    public static final String EVENT_POPUP_REVIEW = "popup_review";
    public static final String EVENT_POPUP_REVIEW_SUCCESS = "popup_review_success";
    public static final String EVENT_POPUP_REVIEW_FAILURE = "popup_review_failure";

    public static final String PARAM_KEY_IS_START_ACTION= "_IS_START_ACTION";
    public static final String EVENT_AD_VIEW_CLICK_REWARD = "ad_view_click_reward";
    public static final String EVENT_AD_VIEW_USER_EARNED_REWARD = "ad_view_user_earned_reward";

    public static final String EVENT_INVOKE_HOME = "invoke_home";
    public static final String EVENT_INVOKE_COMPANY = "invoke_company";
    public static final String EVENT_INVOKE_START_APP = "invoke_start_app";
    public static final String EVENT_INVOKE_ROUTE = "invoke_route";
    public static final String EVENT_INVOKE_SDK = "invoke_sdk";

    public static final String EVENT_NOTIBAR_CLICK_OVERLAY_VIEW_SHOWONLY = "notibar_click_overlay_view_showonly";
    public static final String EVENT_NOTIBAR_CLICK_HOME = "notibar_click_home";
    public static final String EVENT_NOTIBAR_CLICK_COMPANY = "notibar_click_company";
    public static final String EVENT_NOTIBAR_CLICK_START_APP = "notibar_click_start_app";

    public static final String EVENT_WAIT_OVERLAY_CONTINUE_CLICK = "wait_overlay_continue_click";

    public static final String EVENT_START_CONNECT_SERVICE_FROM_WORKER= "start_connect_service_from_worker";
    public static final String EVENT_CRASH_LOG_TO_FIRESTORE = "crash_log_to_firestore";
    public static final String EVENT_CRASH_ERROR_TO_FIRESTORE = "crash_error_to_firestore";

    public static final String EVENT_CRASH_LOG_TO_FIRESTORE_NOT_SEND = "crash_log_to_firestore_not_send";
    public static final String EVENT_CRASH_ERROR_TO_FIRESTORE_NOT_SEND = "crash_error_to_firestore_not_send";
    public static final String EVENT_CRASH_LOG_START = "crash_log_start";

    public static final String EVENT_SERVICE_CHECKER_START = "service_checker_start";
    public static final String EVENT_SERVICE_CHECKER_DO_WORK = "service_checker_do_work";
    public static final String EVENT_SERVICE_CHECKER_RESCUE_SERVICE = "service_checker_rescue_service";
    public static final String EVENT_SERVICE_CHECKER_ALIVE = "service_checker_alive";

    public static final String EVENT_SERVICE_CHECK_LOG_TO_FIRESTORE = "service_check_log_to_firestore";
    public static final String EVENT_SERVICE_CHECK_LOG_TO_FIRESTORE_NOT_SEND = "service_check_log_to_firestore_not_send";
    public static final String EVENT_SERVICE_CHECK_NOTI = "service_check_noti";
    public static final String EVENT_SERVICE_CHECK_LOG_SEND_NOTI = "service_check_log_send_noti";

    public static final String EVENT_USER_REGISTER = "user_register";
    public static final String EVENT_USER_EXISTS = "user_exists";
    public static final String EVENT_USER_REGISTER_ERROR = "user_register_error";

    public static final String EVENT_LIVE_USER_EVERYDAY = "live_user_everyday";
    public static final String EVENT_LIVE_USER_FIRST_REGISTER = "live_user_first_register";
    public static final String EVENT_LIVE_USER_ERROR = "live_user_error";

    public static final String EVENT_PURCHASE = "purchase";
    public static final String EVENT_PURCHASE_UNREGISTER = "purchase_unregister";
    public static final String EVENT_COLLECTION_ERROR = "collection_error";

    public static final String EVENT_SAVE_SETTING= "save_setting";
//    public static final String EVENT_SAVE_SETTING= "save_app_info";

    public static String getMsg(String key, String value) {
        String msg = key;
        if ( Utils.isNotEmpty(value) ) {
            msg += " : " + value;
        }
        return msg;
    }

    public static String genValue(Object ... value) {
        String message = "";
        if (value != null && value.length == 1) {
            // handle this common case without the extra cost of creating a stringbuffer:
            if ( value[0] != null ) message = value[0].toString();
        } else {
            StringBuilder sb = new StringBuilder();
            if (value != null) for (Object m : value) {
                sb.append(m+(" , " ));
            }
            message = sb.toString();
        }
        return message;
    }

    public static String i(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, key, value);
        Log.i("EVENT_" + TAG, getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String i(Context ctx, String TAG, String key, Throwable value) {
        return i(ctx,TAG,key,L.getStackTraceString(value));
    }
    public static String d(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, key, value);
        Log.d("EVENT_" + TAG, getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String d(Context ctx, String TAG, String key, Throwable value) {
        return d(ctx,TAG,key,L.getStackTraceString(value));
    }
    public static String w(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, key, value);
        Log.w("EVENT_" + TAG, getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String w(Context ctx, String TAG, String key, Throwable value) {
        return w(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String e(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, key, value);
        Log.e("EVENT_" + TAG, getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String e(Context ctx, String TAG, String key, Throwable value) {
        return e(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String v(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, key, value);
        Log.v("EVENT_" + TAG, getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String v(Context ctx, String TAG, String key, Throwable value) {
        return v(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String wtf(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, key, value);
        Log.wtf("EVENT_" + TAG, getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String wtf(Context ctx, String TAG, String key, Throwable value) {
        return wtf(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static void logScreenName(Context ctx, String screenName, String activityName) {
        FirebaseAnalytics mFa = FirebaseAnalytics.getInstance(ctx);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName);
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, activityName);
        mFa.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }
//
//    public static String log(Context ctx, String key) {
//        return log(ctx,key);
//    }

    public static String log(Context ctx, String key, Object ... value) {
        key = StringUtils.defaultString(key);
        String sValue = StringUtils.defaultString(genValue(value));
               sValue = sValue.substring(0,sValue.length()>100?100:sValue.length());

        FirebaseAnalytics mFa = FirebaseAnalytics.getInstance(ctx);
        String faKey = key.replaceAll("[^a-zA-Z0-9_]","_");
               faKey = faKey.substring(0,faKey.length()>40?40:faKey.length());
//        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, TAG);
        Bundle bundle = new Bundle();
        setDefaultEventParameters(ctx,bundle);

        if ( Utils.isNotEmpty(sValue) ) {
            bundle.putString("value", sValue);
        }

        mFa.logEvent(faKey,bundle);
        String msg = getMsg(key,sValue);
        return msg;
    }
    public static String log(Context ctx, String key, Throwable value) {
        return log(ctx,key,L.getStackTraceString(value));
    }

    public static String log(Context ctx, String key, Bundle newBundle) {
        key = StringUtils.defaultString(key);
        String faKey = key.replaceAll("[^a-zA-Z0-9_]","_");
        faKey = faKey.substring(0,faKey.length()>40?40:faKey.length());
        Bundle bundle = new Bundle();
        setDefaultEventParameters(ctx,bundle);
        bundle.putAll(newBundle);
        instance(ctx).logEvent(faKey,bundle);
        String msg = getMsg(key,genValue(newBundle));
        return msg;
    }

    private static void setDefaultEventParameters(Context ctx, Bundle bundle)  {
        try {
            instance(ctx).setUserProperty("licensed", String.valueOf(LicenseUtil.isLicense()));
            instance(ctx).setUserProperty("user_type", LicenseUtil.getUserType().name());
            instance(ctx).setUserProperty("user_level", LicenseUtil.getUserLevel().name()); // 확인 필요
            instance(ctx).setUserProperty("license_paid", String.valueOf(LicenseUtil.isPaidLicense())); // 확인 필요
            instance(ctx).setUserProperty("license_subs", String.valueOf(LicenseUtil.isSubsLicense())); // 확인 필요
            instance(ctx).setUserProperty("license_lifetime", String.valueOf(LicenseUtil.isLifeTimeLicense())); // 확인 필요

            setBundleForString(bundle, Const.PREF_KEY_APP_ENABLE, MapUtil.getAppEnable().name());
            setBundleForString(bundle, Const.PREF_KEY_APP_PACKAGE, MapUtil.getAppPackage());
            setBundleForString(bundle, Const.PREF_KEY_MUSIC_APP_PACKAGE, MapUtil.getMusicAppPackage());
            setBundleForBoolean(bundle, Const.PREF_KEY_INDEPENDENCE_CONDITION, MapUtil.isIndependenceCondition());
            setBundleForString(bundle, Const.PREF_KEY_NAVI_MODE, MapUtil.getNaviMode().name());
            setBundleForString(bundle, Const.PREF_KEY_NAVI_AUTO_START, MapUtil.getNaviAutoStart().name());
            setBundleForString(bundle, Const.PREF_KEY_CHARGING_MODE, MapUtil.getChargingMode().name());
            setBundleForBoolean(bundle, Const.PREF_KEY_START_POWER, MapUtil.isStartPower());
            setBundleForBoolean(bundle, Const.PREF_KEY_START_WIFI, MapUtil.isStartWifi());
            setBundleForBoolean(bundle, Const.PREF_KEY_START_BLUETOOTH, MapUtil.isStartBluetooth());
            setBundleForString(bundle, Const.PREF_KEY_STOP_MODE, MapUtil.getStopMode().name());
            setBundleForBoolean(bundle, Const.PREF_KEY_NAVI_POPUP_MODE, MapUtil.isOverlayOpen());
            setBundleForLong(bundle, Const.PREF_KEY_RENAVI_PREVENT_TIME, MapUtil.getPreventWaitOverlayTime());
            setBundleForFloat(bundle, Const.PREF_KEY_NAVI_OVERLAY_WAIT_TIME, MapUtil.getOverlayWaitTime());

            instance(ctx).setUserProperty("service_accessibility", String.valueOf(PermissionUtil.isAccessibilityServiceRunning(ctx)));
            instance(ctx).setUserProperty("service_connect_isLive", String.valueOf(ServiceUtil.isLive()));
        } catch (Exception ex) {
            L.e(TAG,"setDefaultEventParameters",ex);
        }
    }

    public static Bundle setBundleForString(Bundle bundle, String k, String v)  {
        String key = Utils.toCamelCase(k.substring(1).toLowerCase());
        bundle.putString(key,v);
        return bundle;
    }

    public static Bundle setBundleForBoolean(Bundle bundle, String k, boolean v)  {
        String key = Utils.toCamelCase(k.substring(1).toLowerCase());
        bundle.putBoolean(key,v);
        return bundle;
    }
    public static Bundle setBundleForLong(Bundle bundle, String k, long v)  {
        String key = Utils.toCamelCase(k.substring(1).toLowerCase());
        bundle.putLong(key,v);
        return bundle;
    }

    public static Bundle setBundleForFloat(Bundle bundle, String k, float v)  {
        String key = Utils.toCamelCase(k.substring(1).toLowerCase());
        bundle.putFloat(key,v);
        return bundle;
    }

    public static FirebaseAnalytics instance(Context ctx) {
        return FirebaseAnalytics.getInstance(ctx);
    }

    public static void setDefaultEventParameters(Context ctx) {
        Bundle bundle = new Bundle();
        bundle.putString("version", BuildConfig.VERSION_NAME);
        bundle.putString("version_code", String.valueOf(BuildConfig.VERSION_CODE));
        instance(ctx).setDefaultEventParameters(bundle);
    }

}