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

public class Fac {
    private static final String TAG = Fac.class.getSimpleName();

    public static String getMsg(String key, String value) {
        String msg = key;
        if ( Utils.isNotEmpty(value) ) {
            msg += " : " + value;
        }
        return msg;
    }

    public static String genValue(Object ... value) {
        String message;
        if (value != null && value.length == 1) {
            // handle this common case without the extra cost of creating a stringbuffer:
            message = value[0].toString();
        } else {
            StringBuilder sb = new StringBuilder();
            if (value != null) for (Object m : value) {
                sb.append(m);
            }
            message = sb.toString();
        }
        return message;
    }

    public static String i(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.i(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String d(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.d(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String w(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.w(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String e(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.e(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String v(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.v(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }
    public static String wtf(Context ctx, String TAG, String key, Object ... value) {
        String msg = log(ctx, TAG, key, value);
        Log.wtf(TAG , getMsg(key,Fa.genValue(value)));
        return msg;
    }

    public static String log(Context ctx, String TAG, String key, Object ... value) {
        String msg = Fa.log(ctx,TAG,key,value);
        Fa.log(ctx,TAG,key,value);
        return msg;
    }

}