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

public class FaGodWhisper {
    private static final String TAG = "Fa";
    public static final String EVENT_CREATE_PRAY_CLASS= "create_pray_class";
    public static final String EVENT_SELECT_PRAY_CLASS= "select_pray_class";
    public static final String EVENT_SELECTONE_PRAY_CLASS= "selectone_pray_class";
    public static final String EVENT_UPDATE_PRAY_CLASS= "update_pray_class";
    public static final String EVENT_DELETE_PRAY_CLASS= "delete_pray_class";
//    public static final String EVENT_SAVE_SETTING= "save_app_info";

    public static final String EVENT_ADD_PARTY_PRAY_CLASS= "add_party_pray_class";
    public static final String EVENT_REMOVE_PARTY_PRAY_CLASS= "remove_party_pray_class";
    
    public static final String EVENT_DONE_PARTY_PRAY_CLASS= "done_party_pray_class";
    public static final String EVENT_NOT_PARTY_PRAY_CLASS= "not_party_pray_class";

    public static final String EVENT_ERROR_PRAY_CLASS = "error_pray_class";
    public static final String EVENT_ERROR_PRAY_COMMENT = "error_pray_comment";
    public static final String EVENT_ERROR_PRAY_MEMO = "error_pray_memo";
    public static final String EVENT_ADD_PRAY_COMMENT= "add_pray_comment";
    public static final String EVENT_UPDATE_PRAY_COMMENT= "update_pray_comment";
    public static final String EVENT_DELETE_PRAY_COMMENT= "delete_pray_comment";
    public static final String EVENT_SELECT_PRAY_COMMENT= "select_pray_comment";

    public static final String EVENT_ADD_PRAY_MEMO= "add_pray_memo";
    public static final String EVENT_UPDATE_PRAY_MEMO= "update_pray_memo";
    public static final String EVENT_SELECT_PRAY_MEMO= "select_pray_memo";

    public static final String EVENT_OVERLAY_VIEW_CLICK_HOME= "overlayview_click_home";
    public static final String EVENT_OVERLAY_VIEW_CLICK_PRAY = "overlayview_click_pray";
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
        Log.i("EVENT_" + TAG, getMsg(key, FaGodWhisper.genValue(value)));
        return msg;
    }
    public static String i(Context ctx, String TAG, String key, Throwable value) {
        return i(ctx,TAG,key,L.getStackTraceString(value));
    }
    public static String d(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, key, value);
        Log.d("EVENT_" + TAG, getMsg(key, FaGodWhisper.genValue(value)));
        return msg;
    }
    public static String d(Context ctx, String TAG, String key, Throwable value) {
        return d(ctx,TAG,key,L.getStackTraceString(value));
    }
    public static String w(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, key, value);
        Log.w("EVENT_" + TAG, getMsg(key, FaGodWhisper.genValue(value)));
        return msg;
    }
    public static String w(Context ctx, String TAG, String key, Throwable value) {
        return w(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String e(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, key, value);
        Log.e("EVENT_" + TAG, getMsg(key, FaGodWhisper.genValue(value)));
        return msg;
    }
    public static String e(Context ctx, String TAG, String key, Throwable value) {
        return e(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String v(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, key, value);
        Log.v("EVENT_" + TAG, getMsg(key, FaGodWhisper.genValue(value)));
        return msg;
    }
    public static String v(Context ctx, String TAG, String key, Throwable value) {
        return v(ctx,TAG,key,L.getStackTraceString(value));
    }

    public static String wtf(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, key, value);
        Log.wtf("EVENT_" + TAG, getMsg(key, FaGodWhisper.genValue(value)));
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