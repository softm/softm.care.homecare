package net.softm.data.kata.util;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

public class L {
    private static final String TAG = "L";

    public static String i(String TAG, String key, Object ... value) {
        String msg = getMsg(key,Fa.genValue(value));
        Log.i(TAG , msg);
        return msg;
    }
    public static String i(String TAG, String key, Throwable value) {
        return i(TAG,key,L.getStackTraceString(value));
    }

    public static String d(String TAG, String key, Object ... value) {
        String msg = getMsg(key,Fa.genValue(value));
        Log.d(TAG , msg);
        return msg;
    }
    public static String d(String TAG, String key, Throwable value) {
        return d(TAG,key,L.getStackTraceString(value));
    }
    public static String w(String TAG, String key, Object ... value) {
        String msg = getMsg(key,Fa.genValue(value));
        Log.w(TAG , msg);
        return msg;
    }
    public static String w(String TAG, String key, Throwable value) {
        return w(TAG,key,L.getStackTraceString(value));
    }
    public static String e(String TAG, String key, Object ... value) {
        String msg = getMsg(key,Fa.genValue(value));
        Log.e(TAG , msg);
        return msg;
    }
    public static String e(String TAG, String key, Throwable value) {
        return e(TAG,key,L.getStackTraceString(value));
    }
    public static String v(String TAG, String key, Object ... value) {
        String msg = getMsg(key,Fa.genValue(value));
        Log.v(TAG , msg);
        return msg;
    }
    public static String v(String TAG, String key, Throwable value) {
        return v(TAG,key,L.getStackTraceString(value));
    }
    public static String wtf(String TAG, String key, Object ... value) {
        String msg = getMsg(key,Fa.genValue(value));
        Log.wtf(TAG , msg);
        return msg;
    }
    public static String wtf(String TAG, String key, Throwable value) {
        return wtf(TAG,key,L.getStackTraceString(value));
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
        Log.i(TAG,msg);
        return msg;
    }
    public static String log(Context ctx, String TAG, String key, Throwable value) {
        return log(ctx,TAG,key,L.getStackTraceString(value));
    }


    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to log
     */
    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

}