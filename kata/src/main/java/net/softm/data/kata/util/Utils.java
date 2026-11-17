package net.softm.data.kata.util;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.softm.data.kata.BuildConfig;
import net.softm.data.kata.MainActivity;
import net.softm.data.kata.TestActivity;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.OrientationType;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.dto.ServerStateDTOOut;
import net.softm.data.kata.web.BasicActivity;
import net.softm.lib.BaseActivity;
import net.softm.lib.Constant;
import net.softm.lib.common.AsyncHttp;
import net.softm.lib.common.AsyncHttpParam;
import net.softm.lib.common.WLog;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utils {

    static String TAG = "Utils";

    public static void goMain(Context context) {
        goMain(context,false);
    }

    public static void goMain(Context context,boolean restartService) {
        Intent i = new Intent(context,MainActivity.class); // 메인
        if ( restartService ) i.putExtra("P_RESTART_SERVICE", true);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    public static void goPartyPray(Context context,String docId) {
        Intent i = new Intent(context,MainActivity.class); // 메인
        i.putExtra("P_PARTY_DOC_ID", docId);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }
    public static void goPray(Context context,String docId) {
        Intent i = new Intent(context,MainActivity.class); // 메인
        i.putExtra("P_DOC_ID", docId);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    public static void goMainForUpdate(Context context,boolean restartService) {
        Intent i = new Intent(context,MainActivity.class); // 메인
        if ( restartService ) i.putExtra("P_RESTART_SERVICE", true);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    public static void goPermission(Context context,String checkMode, String params) {
        Intent i = new Intent(context,MainActivity.class); // 메인
        i.putExtra("P_FOWARD_PAGE",WUrl.ReqId.WEB_PERMISSION.name());
        i.putExtra("P_CHECK_MODE",checkMode);
        i.putExtra("P_PARAMS",params);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    public static void goFowardPage(Context context,String P_FOWARD_PAGE, int tIdx, String params) {
        Intent i = new Intent(context,MainActivity.class); // 메인
        i.putExtra("P_FOWARD_PAGE",P_FOWARD_PAGE);
        i.putExtra("P_PARAMS",params);
        i.putExtra("P_TIDX",tIdx);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    public static void goTest(Context context) {
        Intent i = new Intent(context, TestActivity.class); // 메인
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static Intent getNotiSettingIntent(Context context,String packageName) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", packageName);
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + packageName));
        }
        return intent;
    }

    public static Intent getNotiSettingIntent(Context context) {
        return getNotiSettingIntent(context,context.getPackageName());
    }

    public static void goAppDetailSettings(Context context, String packageName) {
        Log.d("ACS", "App Info.");

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.setIdentifier("test");
        }

        intent.setData(Uri.parse("package:" + packageName));

//        intent.putExtra("SOFTM","extra_value");
//        intent.putExtra("extra_key", "extra_value");
//        ArrayList<TmpPacelable> colors = new ArrayList<>();
        // add() method
//        colors.add(new TmpPacelable("SOFTM"));

//        intent.putParcelableArrayListExtra("SOFTM", colors);
        context.startActivity(intent);

    }
    public static void goAppDetailSettings(Context context) {
        goAppDetailSettings(context,MapUtil.getAppPackage());
    }

    public static boolean isEmpty(Object s) {
        if (s == null) {
            return true;
        }
        if ((s instanceof String) && (((String)s).trim().length() == 0)) {
            return true;
        }
        if ((s instanceof String) && ("null".equals((String)s) || "<null>".equals((String)s)) ) {
            return true;
        }
        if (s instanceof Map) {
            return ((Map<?, ?>)s).isEmpty();
        }
        if (s instanceof List) {
            return ((List<?>)s).isEmpty();
        }
        if (s instanceof Object[]) {
            return (((Object[])s).length == 0);
        }
        return false;
    }
    public static boolean isNotEmpty(Object s) {
       return !isEmpty(s);
    }

    static public int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    static public int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    static public int getRatioLength(int length, int ratioWidth, int ratioHeight) {
        return length * ratioHeight / ratioWidth;

    }

    public static int getPixelToDp(Context context, float dp) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
        return px;
    }

    public static int getStatusBarHeight(final Context context) {
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        } else {
            return (int) Math.ceil((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 24 : 25) *
                    resources.getDisplayMetrics().density);
        }
    }

    // 특정 패키지명의 앱 설치 여부 체크업
    public static boolean searchAppPackage(Context context, String packageName){
        boolean bExist = false;

        // 패키지 정보 리스트 추출
        PackageManager pkgMgr = context.getPackageManager();
        List<ResolveInfo> mAppList;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mAppList = pkgMgr.queryIntentActivities(mainIntent, 0);

        // 패키지 리스트 순회하면서 특정 패키지명 검색
        try{
            for(int i=0;i<mAppList.size();i++){
                if(mAppList.get(i).activityInfo.packageName.startsWith(packageName)){
                    bExist = true;
                    break;
                }
            }
        }catch(Exception e){
            bExist = false;
        }
        return bExist;
    }

    //특정 패키지명의 앱 실행(설치여부 확인후 실행필요)
    public static void executeLocalAppPackage(Context context, String packageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    //특정 패키지명의 앱의 설치경로(PlayStore) 이동처리
    public static void gotoStoreAppPackage(Context context, String packageName){
        try {
            String url = "market://details?id=" + packageName;
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        } catch (android.content.ActivityNotFoundException e) {
            String url = "https://play.google.com/store/apps/details?id=" + packageName;
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }

    }
    /**
     * App시작 서버상태체크
     * IntroActivity에서 호출됨 ( AsIS : ServerStateTask.java )
     * @param context
     * WUtil.startAppByServerState(this)
     */
    public static void startAppByServerState_Back(Context context) {
        final BaseActivity activity = (BaseActivity)context;

        String inParams = "";
        new AsyncHttp<ServerStateDTOOut>(activity) {
            @Override
            public void complete(ServerStateDTOOut result) {
                boolean exec = false;
                WLog.i(result.getClass().getName() +" complete : " + result);
                ServerStateDTOOut.mainDTO mainDto = result.get_msg_();
                ServerStateDTOOut.bodyDTO bodyDto = mainDto.get_body_();
                String serverFlag = bodyDto.getFlag();
                if ( Const.SERVER_TYPE_SUCCESS.equals(serverFlag) ) {
//                  activity.alert("성공");
                    exec = true;
                } else {
                    exec = false;
                    serverFlag = Const.SERVER_TYPE_ERROR;
// {"_msg_":{"_common_":{"error.code":"F0000","resCode":"00"},"_body_":{"status":"success","flag":"S"}}}
//					result = new ServerStateDTOOut();
//					result.setState(Const.SERVER_TYPE_SUCCESS);
//					exec = false;
//					activity.toast(result.getMsg());
                }

				if ( exec ) {

				}
            }
            @Override
            public void callback(ServerStateDTOOut result) {

            }
        }.execute(
// TODO SOFTM 상태체크 되면 열어야함.
//				new AsyncHttpParam(WUrl.ReqId.WEB_STATE.getReqUrl())
                new AsyncHttpParam(WUrl.ReqId.WEB_STATE.getReqUrl())
        );
    }

    /**
     * App시작 서버상태체크
     * IntroActivity에서 호출됨 ( AsIS : ServerStateTask.java )
     * @param context
     * WUtil.startAppByServerState(this)
     */
    public static void startAppByServerState(Context context, String url, final WebView view) {
        final BasicActivity activity = (BasicActivity)context;

        String inParams = "";
        new AsyncHttp<ServerStateDTOOut>(activity) {
            @Override
            public void complete(ServerStateDTOOut result) {
                WLog.i(result.getClass().getName() +" complete2 : " + result);
                if ( String.valueOf(Constant.HTTP_CODE_SUCCESS).equals(result.getMsgCode()) ) {
//                        activity.alert("성공");
                    activity.mWebview.setVisibility(View.VISIBLE);
                } else {
//					activity.toast(result.getMsg());
                    activity.showNetworkError(Integer.valueOf(result.getMsgCode()),result.getMsg());
                }
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void callback(ServerStateDTOOut result) {
                String serverState = result.getState();
                WLog.i(result.getClass().getName() +" callback : " + result);
            }
        }.execute(
                new AsyncHttpParam(WUrl.ReqId.WEB_STATE.getReqUrl())
        );
        Utils.toast(context,WUrl.ReqId.WEB_STATE.getReqUrl());
    }

    public static boolean isServerConnected(Context context) {
        return Utils.isNotEmpty(WApplication.mInstance) && Utils.isNotEmpty(WApplication.basicInfoDTO) ? RestUtil.isServerConnected() : false;
    }

    public static <T> T getJsonFromServer(String url, Class<T> clazz) throws Exception {
        OkHttpClient client = new OkHttpClient();

        // HTTP GET 요청 생성
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // JSON 데이터를 DTO로 변환
            String jsonData = response.body().string();
            return convertJsonToDto(jsonData, clazz);
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static <T> T convertJsonToDto(String jsonData, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(jsonData, clazz);
    }

    /**

     * This method converts dp unit to equivalent pixels, depending on device density.

     *

     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels

     * @param context Context to get resources and device specific display metrics

     * @return A float value to represent px equivalent to dp depending on device density

     */

    public static float convertDpToPixel(float dp, Context context){

        Resources resources = context.getResources();

        DisplayMetrics metrics = resources.getDisplayMetrics();

        float px = dp * (metrics.densityDpi / 160f);

        return px;

    }



    /**

     * This method converts device specific pixels to density independent pixels.

     *

     * @param px A value in px (pixels) unit. Which we need to convert into db

     * @param context Context to get resources and device specific display metrics

     * @return A float value to represent dp equivalent to px value

     */

    public static float convertPixelsToDp(float px, Context context){

        Resources resources = context.getResources();

        DisplayMetrics metrics = resources.getDisplayMetrics();

        float dp = px / (metrics.densityDpi / 160f);

        return dp;

    }

    public static long dateStringToTime(String strDate) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // you can change format of date
            Date date = formatter.parse(strDate);
            return date.getTime();
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            return 0l;
        }
    }
    public static boolean hasPermissionForBasic(Context context) {
        boolean isPermission = false;
//        isPermission = PermissionUtil.hasNotificationAccessPermission(context) && PermissionUtil.hasOverlayPermissions(context);
        isPermission = PermissionUtil.hasOverlayPermissions(context)
//                && PermissionUtil.hasNotificationAccessPermission(context)
                && PermissionUtil.isPostNotificationsPermission(context)
//        && PermissionUtil.hasAlarmPermissions(context)
        ;
        return isPermission;
    }

    public static boolean hasPermission(Context context) {
        boolean isPermission = false;
////        if ( isLocationPermission() ) {
////			if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ) {
//            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
//                if ( Settings.canDrawOverlays(this) ) isPermission = true;
//            } else  isPermission = true;
//            if ( !isPermission ) {
//                isPermission = isPermission && MapUtil.isEnableLocationServicesStatus(this) && MapUtil.hasUsageStatsPermission(this);
//            }
////        }
//        MapUtil.hasIgnoreBatteryPermissions();
//        MapUtil.isEnableLocationServicesStatus(context);
//        isPermission = PermissionUtil.hasNotificationAccessPermission(context) && PermissionUtil.hasAccessibilityPermissions() && PermissionUtil.hasIgnoreBatteryPermissions();
        isPermission = PermissionUtil.hasNotificationAccessPermission(context) &&
//                       PermissionUtil.hasAccessibilityPermissions() &&
//                       PermissionUtil.isAccessibilityServiceRunning(context) &&
                       PermissionUtil.hasIgnoreBatteryPermissions() &&
                       PermissionUtil.hasOverlayPermissions(context);
        return isPermission;
    }

    public static void toastDebug(Context context, String v, int duration, int gravity, int top, int left) {
        if ( BuildConfig.DEBUG ) toast(context,v,duration,gravity,top,left);
    }
    public static void toastDebug(int v) {
        if ( BuildConfig.DEBUG ) toast(WApplication.mInstance,WApplication.mInstance.getResources().getString(v));
    }
    public static void toastDebug(String v) {
        if ( BuildConfig.DEBUG ) toast(WApplication.mInstance,v);
    }
    public static void toastDebug(Context context, String v, int duration, int gravity ) {
        toastDebug(context,v,duration,gravity,0,0);
    }

    public static void toastDebug(Context context, String v, int duration) {
        toastDebug(context,v,duration,Gravity.BOTTOM,0,0);
    }

    public static void toastDebug(Context context, int v, int duration) {
        Context ctx = context != null ? context : WApplication.mInstance;
        toastDebug(context,ctx.getResources().getString(v),duration);
    }

    public static void toastDebug(Context context, String v) {
        toastDebug(context,v,Toast.LENGTH_LONG);
    }

    public static void toastDebug(Context context, int v) {
        Context ctx = context != null ? context : WApplication.mInstance;
        toastDebug(ctx,v,Toast.LENGTH_LONG);
    }
    public static void toast(Context context, int v) {
        Context ctx = context != null ? context : WApplication.mInstance;
        toast(context,ctx.getString(v),Toast.LENGTH_LONG,Gravity.BOTTOM,0,0);
    }
    public static void toast(Context context, int v, int duration) {
        Context ctx = context != null ? context : WApplication.mInstance;
        toast(context,ctx.getResources().getString(v),duration,Gravity.BOTTOM,0,0);
    }
    public static void toast(Context context, String v) {
        toast(context,v,Toast.LENGTH_LONG,Gravity.BOTTOM,0,0);
    }

    public static void toast(int v, int duration) {
        Context ctx = WApplication.mInstance;
        if ( ctx != null && Utils.isNotEmpty(ctx.getResources().getString(v))) {
            toast(null, ctx.getResources().getString(v), duration, Gravity.BOTTOM, 0, 0);
        }
    }
    public static void toast(int v) {
        Context ctx = WApplication.mInstance;
        if ( ctx != null && Utils.isNotEmpty(ctx.getResources().getString(v))) {
            toast(null, ctx.getResources().getString(v), Toast.LENGTH_LONG, Gravity.BOTTOM, 0, 0);
        }
    }
    public static void toast(String v) {
        Context ctx = WApplication.mInstance;
        if ( ctx != null ) {
            toast(ctx, v, Toast.LENGTH_LONG, Gravity.BOTTOM, 0, 0);
        }
    }

    public static void toast(Context context, String v, int duration, int gravity, int top, int left) {
        Context ctx = context != null ? context : WApplication.mInstance;
        if (ctx != null) {
            try {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        Toast tt = Toast.makeText(ctx, v, duration);
                        //                Toast tt = Toast.makeText(ConnectAccessibilityService.mContext, v, duration);
                        //                        tt.setGravity(gravity,top,left); // not working
                        //            tt.setMargin(100,100);
                        tt.show();
                    }
                }, 0);
            } catch(Exception ex) {
                Fc.e(ctx,TAG,"toast",ex.toString());
            }
        }
    /*
        if ( AppUtil.isActivityRunning(context,MainActivity.class)) {
            try {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        try {
                              Toast tt = Toast.makeText(context, v, duration);
            //                Toast tt = Toast.makeText(ConnectAccessibilityService.mContext, v, duration);
        //                        tt.setGravity(gravity,top,left); // not working
            //            tt.setMargin(100,100);
                                tt.show();
                        } catch(Exception ex) {
                            Fc.e(context,TAG,"toast.run",ex.toString());
                        }
                    }
                }, 0);
            } catch(Exception ex) {
                Fc.e(context,TAG,"toast",ex.toString());
            }
//
//            Toast tt = Toast.makeText(context, v, duration);
//            View toast_view = LayoutInflater.from(context).inflate(R.layout.toast_border, null);
//            TextView text = (TextView) toast_view.findViewById(R.id.text);
//            text.setText(v);
//            tt.setView(toast_view);
//            tt.setDuration(duration);
//            tt.setGravity(gravity, top, left);
////            tt.setText(v);
//            tt.show();

//            LayoutInflater inflater = getLayoutInflater();
//            View layout = inflater.inflate(R.layout.toast_border, (ViewGroup)findViewById(R.id.toast_layout_root));
//
//            TextView text = (TextView) layout.findViewById(R.id.text);
//            text.setText(pArg1);
//
//            Toast toast = new Toast(getApplicationContext());
//            toast.setDuration(Integer.valueOf(pArg2));
//            toast.setGravity(Integer.valueOf(pArg3),Integer.valueOf(pArg4),Integer.valueOf(pArg5));
//            toast.setView(layout);
//            toast.show();

        } else {
            MapUtil.execCommandActivity(context,"toast", v, String.valueOf(duration), String.valueOf(gravity), String.valueOf(top), String.valueOf(left));
         // MapUtil.execCommandService(context,"toast", v, String.valueOf(duration));
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run()
//                {
//                    WApplication.mInstance.toast(v);
//                }
//            }, 0);
        }
*/
    }

    public static File screenCapture(View view){
        view.setDrawingCacheEnabled(true);

        Bitmap screenBitmap = view.getDrawingCache();

        File newDir = null; //or any other folder for specific purpose
        try {
            File ext = Environment.getExternalStorageDirectory();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                newDir = new File(ext + File.separator + Environment.DIRECTORY_DOCUMENTS+ File.separator  + "tmapauto");
            } else {
                newDir = new File(ext + File.separator +  "tmapauto");
            }
            if (!newDir.exists()) {
                newDir.mkdir();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String filename = "screenshot"+String.valueOf(System.currentTimeMillis()).replaceAll(":", ".") +".png";
        File file = new File(newDir.getAbsolutePath() + File.separator + filename);

        FileOutputStream os = null;
        try{
            os = new FileOutputStream(file);
            screenBitmap.compress(Bitmap.CompressFormat.PNG, 100, os); //PNG파일로 만들기
            os.close();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        view.setDrawingCacheEnabled(false);
        return file;
    }
    public static String toCamelCase(String s) {
        Matcher m = Pattern.compile("[_|-](\\w)").matcher(s);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, m.group(1).toUpperCase());
        }
        return m.appendTail(sb).toString();
    }
    public static String toCamelCaseForMyKey(String k) {
        String key = Utils.toCamelCase(k.substring(1).toLowerCase());
        return key;
    }

    public static void turnOnScreen_() {
//        MapUtil.execCommandActivity(WApplication.mInstance,"TurnOnScreen");
        PowerManager powerManager = (PowerManager) WApplication.mInstance.getSystemService(WApplication.mInstance.POWER_SERVICE);
        PowerManager.WakeLock  wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "appname::WakeLock");

        //acquire will turn on the display
        wakeLock.acquire();


        //release will release the lock from CPU, in case of that, screen will go back to sleep mode in defined time bt device settings
        wakeLock.release();

    }

    public static void turnOnScreen() {
//        MapUtil.execCommandActivity(WApplication.mInstance,"TurnOnScreen");
        PowerManager powerManager = (PowerManager) WApplication.mInstance.getSystemService(WApplication.mInstance.POWER_SERVICE);
//        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//                "Softm::MyWakelockTag");
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "Softm::MyWakelockTag");

        wakeLock.acquire();


        //release will release the lock from CPU, in case of that, screen will go back to sleep mode in defined time bt device settings
        wakeLock.release();

    }

    public static void turnOffScreen() {
//        MapUtil.execCommandActivity(WApplication.mInstance,"TurnOnScreen");
        PowerManager powerManager = (PowerManager) WApplication.mInstance.getSystemService(WApplication.mInstance.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "Softm::MyWakelockTag");

        wakeLock.acquire();


    }

    public static void sleepForWorkExecutor() {
        sleepMicoSeconds(1);
    }
    public static void sleepMicoSeconds(long microSec) {
        try {
            Thread.sleep(microSec);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//
//        try {
//            TimeUnit.MICROSECONDS.sleep(microSec);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
    public static void sleepSeconds(int sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//
//        try {
//            TimeUnit.SECONDS.sleep(sec);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * read a text input stream
     */
    public static String readTextStream(InputStream input, String encoding) throws IOException {
        InputStreamReader in = new InputStreamReader(input, encoding);
        StringWriter out = new StringWriter();
        copyReader(in, out);
        in.close();
        out.close();

        return out.toString();
    }

    /**
     * copies the contens of a stream
     */
    public static void copyReader(Reader in, Writer out) throws IOException {
        char[] buffer = new char[4096];
        for (;;) {
            int count = in.read(buffer, 0, 4096);
            if (count < 0)
                break;
            out.write(buffer, 0, count);
        }
    }

    public static String loadAssetTextAsString(Context context, String url) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(url);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ( (str = in.readLine()) != null ) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e("TAG", "Error opening asset " + url);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("TAG", "Error closing asset " + url);
                }
            }
        }

        return null;
    }

    public static String getLanguage(Context context) {

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        }else{
            locale = context.getResources().getConfiguration().locale;
        }

        String displayCountry = locale.getDisplayCountry();
        String country = locale.getCountry();
        String language = locale.getLanguage();

        return language;
    }

    public static String getCountry(Context context) {

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        }else{
            locale = context.getResources().getConfiguration().locale;
        }

        String displayCountry = locale.getDisplayCountry();
        String country = locale.getCountry();
        String language = locale.getLanguage();

        return country;
    }
    public static String getDisplayCountry(Context context) {

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        }else{
            locale = context.getResources().getConfiguration().locale;
        }

        String displayCountry = locale.getDisplayCountry();
        String country = locale.getCountry();
        String language = locale.getLanguage();

        return displayCountry;
    }

    public static String getMethodname(){
        String methodName = Thread.currentThread().getStackTrace()[1].getMethodName(); // 현재 메소드명
//        System.out.println("현재 메소드명 : " + methodName);
        return methodName;
    }

    // 홈 화면인지 확인하는 메소드
    public static boolean isHomeScreen(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            // 상위 1개의 작업을 가져옴
            List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
            if (!tasks.isEmpty()) {
                ComponentName topActivity = tasks.get(0).topActivity;
                // 홈 화면 (런처) 앱이 최상단에 있는지 확인
                return topActivity != null && topActivity.getPackageName().equals("com.android.launcher");
            }
        }
        return false;
    }

    public static boolean isAutoRotateStatus(Context context) {
        try {
            // Check if auto-rotate is enabled (1: enabled, 0: disabled)
            int autoRotateStatus = Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION
            );

            if (autoRotateStatus == 1) {
                // Auto-rotate is enabled
                return true;
            } else {
                // Auto-rotate is disabled
                return false;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isLandscape(Context context) {
        return WApplication.mInstance.isLandscape;
    }

    public static OrientationType getOrientation(Context context) {
        return WApplication.mInstance.orientationType;
    }
    public static String ColorDark = "#B7B7B7";
    public static String ColorBasic = "#494949";
    public static boolean isDarkMode(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29 이상: 실제 다크 모드 상태 반환
            int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        } else {
            // API 28 이하: 다크 모드가 없으므로 항상 false 반환
            return false;
        }
    }
    public static Spanned fromHtml(String source) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            // noinspection deprecation
            return Html.fromHtml(source);
        }
        return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
    }

    public static String millisecondsToTime(long milliseconds) {
        // 밀리세컨드를 초로 변환
        long totalSeconds = milliseconds / 1000;

        // 일, 시간, 분, 초 계산
        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds % (24 * 3600)) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        // 포맷에 맞게 반환
        return String.format("%d일 %d시간 %d분 %d초", days, hours, minutes, seconds);
    }


    public static String convertMillisecondsToDate(long milliseconds) {
        // Date 객체로 변환
        Date date = new Date(milliseconds);

        // 원하는 날짜 포맷 설정 (예: yyyy-MM-dd HH:mm:ss)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Date 객체를 지정된 포맷의 문자열로 변환하여 반환
        return dateFormat.format(date);
    }

    public static void runExecutorService(Runnable runnable) {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(runnable);
            executor.shutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static boolean hasKeyMapArrayList(ArrayList<Map<String, Object>> list,String dataKey, String key) {
        // 특정 키가 포함된 맵 찾기
        boolean containsKey = false;
        for (Map<String, Object> map : list) {
            if (map.get(dataKey).equals(key)) {
                containsKey = true;
                break;
            }
        }
        return containsKey;
    }


    public static void clearAllPreferences(Context context) {
        File sharedPrefsDir = new File(context.getFilesDir().getParent() + "/shared_prefs/");
        if (sharedPrefsDir.exists() && sharedPrefsDir.isDirectory()) {
            for (File file : sharedPrefsDir.listFiles()) {
                file.delete(); // 각 파일 삭제
            }
        }
    }
    public static void resetAppData(Context context) {
        try {
            Runtime.getRuntime().exec("pm clear " + context.getPackageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Locale getLocale(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = WApplication.mInstance.getResources().getConfiguration().getLocales().get(0);
        }else{
            locale = WApplication.mInstance.getResources().getConfiguration().locale;
        }
        return locale;
    }

    public static String unescapeHtml(String strVal) {
        String strRtn = StringEscapeUtils.unescapeHtml4(strVal);
        strRtn = StringEscapeUtils.unescapeHtml3(strVal);
        strRtn = strRtn.replaceAll("&apos;", "'");
        return strRtn;
    }
    public static String encdodeForWeb(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            L.e(TAG,"encodePayClass", e);
        }
        return text;
    }
}

