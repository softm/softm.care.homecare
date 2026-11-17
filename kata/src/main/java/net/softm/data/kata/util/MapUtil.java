package net.softm.data.kata.util;

import static net.softm.data.kata.util.PackageUtil.encodeIcon;

import android.Manifest;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.kakao.sdk.navi.NaviClient;
import com.kakao.sdk.navi.model.CoordType;
import com.kakao.sdk.navi.model.Location;
import com.kakao.sdk.navi.model.NaviOption;
import com.skt.Tmap.TMapTapi;

import net.softm.lib.common.Connectivity;
import net.softm.data.kata.DummyActivity;
import net.softm.data.kata.DummyService;
import net.softm.data.kata.ExecActivity;
import net.softm.data.kata.GpsTracker;
import net.softm.data.kata.MainActivity;
import net.softm.data.kata.PowerConnectRealService;
import net.softm.data.kata.R;
import net.softm.data.kata.RealService;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.WorkerActivity;
import net.softm.data.kata.common.AppEnable;
import net.softm.data.kata.common.AppType;
import net.softm.data.kata.common.ChargingMode;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.MapMode;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.common.NaviMode;
import net.softm.data.kata.common.NaviStatus;
import net.softm.data.kata.common.Package;
import net.softm.data.kata.common.PackageMusic;
import net.softm.data.kata.common.RunStatusScreenMode;
import net.softm.data.kata.common.StartMode;
import net.softm.data.kata.common.StopMode;
import net.softm.data.kata.common.UseStatusMode;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.dto.AppInfoDTO;
import net.softm.data.kata.dto.StartStopOnOffVO;
import net.softm.data.kata.engine.SAPService;
import net.softm.data.kata.geofence.GeofenceConstants;
import net.softm.data.kata.kt.Callbackkt;
import net.softm.data.kata.kt.TestKt;
import net.softm.data.kata.web.AlertDialog;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapUtil {
    private static final String TAG = "MapUtil";
    private Context mContext;
    private static TMapTapi tmaptapi;
    public static String mApiKey = "l7xx2c3bc6b282ed46c9805c99db13f56149"; // 발급받은 SKT AppKey

//    startActivity(
//            NaviClient.instance.shareDestinationIntent(
//            Location("카카오 판교오피스", "127.108640", "37.402111"),
//    NaviOption(coordType = CoordType.WGS84)
//        )
//                )
    // 조회 method
    public static TMapTapi getInstanceForTmap(Context context) {
        if (tmaptapi == null)
            tmaptapi = new TMapTapi(context);
//        printMessage("TEst");

        return tmaptapi;
    }

    public MapUtil(Context context)
    {
        getInstanceForTmap(context);
        tmaptapi.setSKTMapAuthentication (mApiKey);
        mContext = context;
    }

    protected static AlertDialog dialog = null;

    public static void confirm(Context context, String msg, View.OnClickListener onClick, View.OnClickListener onCancel ) {
//        String pBtnText, String nBtnText, String msg, int msgGravity;
        dialog = new AlertDialog(context,null,null,null,msg, 0 );

        dialog.mPListener = onClick;
        dialog.mNListener = onCancel;
        dialog.show();

    }

    public static String getConnectedWifiSSID() {
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_CONNECTED_WIFI_SSID, "");
        return v;
    }

    public static void setConnectedWifiSSID(String ssid) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_CONNECTED_WIFI_SSID, ssid);
    }

    public static HashMap getConnectedBluetooth() {
        return (new Gson()).fromJson(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_CONNECTED_BLUETOOTH,""), HashMap.class);
    }

    public static void setConnectedBluetooth(String bluetooth) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_CONNECTED_BLUETOOTH, bluetooth);
    }

    public static int getConnectedBattery() {
        return Long.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_CONNECTED_BATTERY,"-1")).intValue();
    }

    public static void setConnectedBattery(int plugged) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_CONNECTED_BATTERY, String.valueOf(plugged));
    }

    Handler runHandler;
    private int recallCount = 0;
    private final int limitRecallCount = 5;

    public void invokeHome() throws Exception {

        Fc.i(mContext,TAG,"invokeHome");
        try {
            if ( PackageUtil.isTmap() ) {
                if (!tmaptapi.invokeGoHome())
                    throw new Exception(mContext.getString(R.string.tmap_install_problem));
//                          throw new Exception("티맵설치에 문제가 있어, 호출 할 수 없습니다.");

            } else if ( PackageUtil.isKakaoNavi() ){
                Fc.i(mContext,TAG,"invokeHome","kakao-invokeHome");
                mContext.startActivity(
//                    "My Home", "Company"
                        NaviClient.getInstance().navigateIntent(
                                new Location("My Home", null, null),
                                new NaviOption(CoordType.WGS84)
                        )
                );
            }
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"invokeHome",ex.toString());
            throw ex;
        } finally {
            Fa.i(mContext,TAG,Fa.EVENT_INVOKE_HOME);
        }

    }

    public void runToHome() {
        if ( !Connectivity.isConnected(mContext) ) {
            startNaviForExecActivity(mContext, NaviDestination.HOME);
        } else {
            if ( PackageUtil.isTmap() ) {
                getInstanceForTmap(mContext);
            }
            recallCount = 0;
            runHandler = new Handler();
            runHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        invokeHome();
                     // MapUtil.setNaving();
                    } catch (Exception ex) {
//                        toast(ex.getMessage());
                        if ( recallCount < limitRecallCount ) {
                            Fc.e(mContext,TAG,"runToHome","Run Recall Home");
                            runHandler.postDelayed(this, 500);// TODO SOFTM : ANR 2024.11.28 : ANR 조치 1000 --> 500
                            ex.printStackTrace();
                        } else {
                            toast(ex.getMessage());
                            runHandler.removeCallbacksAndMessages(null);
                        }
                        recallCount++;
                    }
                }
            },1000);
        }
    }

    public void invokeCompany() throws Exception {
        
        Fc.i(mContext,TAG,"invokeCompany");
        try {
            if ( PackageUtil.isTmap() ) {
                if (!tmaptapi.invokeGoCompany())
                    throw new Exception(mContext.getString(R.string.tmap_install_problem));
//                          throw new Exception("티맵설치에 문제가 있어, 호출 할 수 없습니다.");

            } else if ( PackageUtil.isKakaoNavi() ){
                Fc.i(mContext,TAG,"invokeCompany","kakao-invokeCompany");
                mContext.startActivity(

//                    "My Home", "Company"
                        NaviClient.getInstance().navigateIntent(
                                new Location("Company", null, null),
                                new NaviOption(CoordType.WGS84)
                        )
                );
            }
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"invokeCompany",ex.toString());
            throw ex;
        } finally {
            Fa.i(mContext,TAG,Fa.EVENT_INVOKE_COMPANY);
        }

    }

    public void runToCompany() {
        if ( !Connectivity.isConnected(mContext) ) {
            startNaviForExecActivity(mContext, NaviDestination.COMPANY);
        } else {
            getInstanceForTmap(mContext);
            recallCount = 0;
            runHandler = new Handler();
            runHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        invokeCompany();
                    } catch (Exception ex) {
//                        toast(ex.getMessage());
                        if ( recallCount < limitRecallCount ) {
                            Fc.e(mContext,TAG,"runToHome","Run Recall Company");
                            runHandler.postDelayed(this, 500);// TODO SOFTM : ANR 2024.11.28 : ANR 조치 1000 --> 500
                            ex.printStackTrace();
                        } else {
                            toast(ex.getMessage());
                            runHandler.removeCallbacksAndMessages(null);
                        }
                        recallCount++;
                    }
                }
            },1000);
        }
    }

    public void runToDrive() {
        TestKt kt = new TestKt();
        kt.printMessage("test");
        if ( !Connectivity.isConnected(mContext) ) {
            startNaviForExecActivity(mContext, NaviDestination.DRIVE);
        } else {
            getInstanceForTmap(mContext);
            recallCount = 0;
            runHandler = new Handler();
            runHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        invokeSDK();
                    } catch (Exception ex) {
//                        toast(ex.getMessage());
                        if ( recallCount < limitRecallCount ) {
                            Fc.e(mContext,TAG,"runToDrive","Run Recall Drive");
                            runHandler.postDelayed(this, 1000);
                            ex.printStackTrace();
                        } else {
                            toast(ex.getMessage());
                            runHandler.removeCallbacksAndMessages(null);
                        }
                        recallCount++;
                    }
                }
            },1000);
        }
    }

    public void invokeRoute(String szDestName, float lat, float lon) throws Exception {
        
        Fc.i(mContext,TAG,"invokeRoute");
        try {
            if ( PackageUtil.isTmap() ) {
                if (!tmaptapi.invokeRoute(szDestName, lon, lat))
                    throw new Exception(mContext.getString(R.string.tmap_install_problem));
//                          throw new Exception("티맵설치에 문제가 있어, 호출 할 수 없습니다.");
                // MapUtil.setNaving();
            } else if ( PackageUtil.isKakaoNavi() ){
                Fc.i(mContext,TAG,"invokeRoute",szDestName + " / " + String.valueOf(lon)+ " / " + String.valueOf(lat));

//                invokeRoute : 예원아파트 / 126.81401 / 37.49071
//                latitude : 위도
                mContext.startActivity(
//                    "My Home", "Company"
                        NaviClient.getInstance().navigateIntent(
                                new Location(szDestName, String.valueOf(lon), String.valueOf(lat)),
                                new NaviOption(CoordType.WGS84)
                        )
                );
            }
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"invokeRoute",ex.toString());
            throw ex;
        } finally {
            Fa.i(mContext,TAG,Fa.EVENT_INVOKE_ROUTE);
        }

    }

// http://tmapapi.sktelecom.com/main.html#android/docs/androidDoc.TMapTapi_invokeRoute_2
    public void runToRoute(final String szDestName, final float lat, final float lon) {
        Fc.i(mContext,TAG,"runToRoute");
        if ( !Connectivity.isConnected(mContext) ) {
            startNaviForExecActivity(mContext, NaviDestination.BASIC,true,szDestName,lat,lon);
        } else {
            if ( PackageUtil.isTmap() ) {
                getInstanceForTmap(mContext);
            }

            recallCount = 0;
            runHandler = new Handler();
            runHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
//                        Boolean invokeRoute(String szDestName, float fX, float fY)
//                        szDestName - 목적지 명칭
//                        fX - 경도
//                        fY - 위도
//                        KakaoSdk.INSTANCE.getKeyHash();
//                        Fc.i(mContext,TAG,"KakaoSdk.INSTANCE.getKeyHash()",KakaoSdk.INSTANCE.getKeyHash());
                        Fc.i(mContext,TAG,"runToRoute",szDestName + " / " + String.valueOf(lon)+ " / " + String.valueOf(lat));
                        invokeRoute(szDestName,lat,lon);
                    } catch (Exception ex) {
//                        toast(ex.getMessage());
                        if ( recallCount < limitRecallCount ) {
                            Fc.e(mContext,TAG,"runToRoute","Run Recall Route");
                            runHandler.postDelayed(this, 1000);
                            ex.printStackTrace();
                        } else {
                            toast(ex.getMessage());
                            runHandler.removeCallbacksAndMessages(null);
                        }
                        recallCount++;
                    }
                }
            },1000);

        }
    }

    public void invokeSearch(String szDestName) throws Exception {

        Fc.i(mContext,TAG,"invokeSearch");
        try {
            if ( PackageUtil.isTmap() ) {
                if (!tmaptapi.invokeSearchPortal(szDestName))
                    throw new Exception(mContext.getString(R.string.tmap_install_problem));
//                          throw new Exception("티맵설치에 문제가 있어, 호출 할 수 없습니다.");
                // MapUtil.setNaving();
            } else if ( PackageUtil.isKakaoNavi() ){
                Fc.i(mContext,TAG,"invokeSearch",szDestName);
//                runSafeDrive(mContext);
                runApp(mContext,getAppPackage());
//                invokeRoute : 예원아파트 / 126.81401 / 37.49071
//                latitude : 위도
//                mContext.startActivity(
////                    "My Home", "Company"
//                        NaviClient.getInstance().navigateIntent(
//                                new Location(szDestName, String.valueOf(-1), String.valueOf(-1)),
//                                new NaviOption(CoordType.WGS84)
//                        )
//                );
            }
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"invokeSearch",ex.toString());
            throw ex;
        } finally {
            Fa.i(mContext,TAG,Fa.EVENT_INVOKE_ROUTE);
        }

    }

    public void runToSearch(final String szDestName) {
        Fc.i(mContext,TAG,"runToSearch");
        if ( !Connectivity.isConnected(mContext) ) {
            startNaviForExecActivity(mContext, NaviDestination.BASIC,true,szDestName,-1,-1);
        } else {
            if ( PackageUtil.isTmap() ) {
                getInstanceForTmap(mContext);
            }

            recallCount = 0;
            runHandler = new Handler();
            runHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
//                        Boolean invokeRoute(String szDestName, float fX, float fY)
//                        szDestName - 목적지 명칭
//                        fX - 경도
//                        fY - 위도
//                        KakaoSdk.INSTANCE.getKeyHash()
                        Fc.i(mContext,TAG,"runToSearch",szDestName );
                        invokeSearch(szDestName);
                    } catch (Exception ex) {
//                        toast(ex.getMessage());
                        if ( recallCount < limitRecallCount ) {
                            Fc.e(mContext,TAG,"runToSearch","Run Recall Route");
                            runHandler.postDelayed(this, 1000);
                            ex.printStackTrace();
                        } else {
                            toast(ex.getMessage());
                            runHandler.removeCallbacksAndMessages(null);
                        }
                        recallCount++;
                    }
                }
            },1000);

        }
    }
    public static void addGeofences(String szDestName, float lat, float lon, boolean refence) {
        WApplication.mInstance.addGeofences(szDestName + "##" + lat + "##" + lon + (refence ? "##" + "REFENCE" : ""), lat, lon);
    }

    public void invokeSDK() throws Exception {
        
        Fc.i(mContext,TAG,"invokeSDK");
        try {
            if ( PackageUtil.isTmap() ) {
                if (!tmaptapi.invokeTmap())
                    throw new Exception(mContext.getString(R.string.tmap_install_problem));
//                        throw new Exception("티맵설치에 문제가 있어, 호출 할 수 없습니다.");
//                        MapUtil.setNaving();
            } else if ( PackageUtil.isKakaoNavi() ){
                Fc.i(mContext,TAG,"invokeSDK","kakao-invokeSDK");
                mContext.startActivity(
                    NaviClient.getInstance().navigateIntent(
                            new Location("", null, null),
                            new NaviOption(CoordType.WGS84)
                    )
                );


            }
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"invokeSDK",ex.toString());
            throw ex;
        } finally {
            Fa.i(mContext,TAG,Fa.EVENT_INVOKE_SDK);
        }

   }
    public void runToSDK() {
        runToSDK(null);
    }
    public void runToSDK(Runnable runnable) {
        new Thread() {
            @Override
            public void run() {
//                try {
////                    Thread.sleep(1000); // 1초간 Thread를 잠재운다
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                if ( PackageUtil.isTmap() ) {
                    getInstanceForTmap(mContext);
                }
                try {
                    invokeSDK();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if ( runnable != null ) {
                        Utils.runExecutorService(runnable);
                    }
                }
            }
        }.start();

    }

    public static void runNavi(Context ctx,MapPoint mapPoint, boolean refence) {
//        startApp(
//                context,
//                "com.skt.tmap.ku",
//                "tmap://route?goalx=" + lon + "&goaly=" + lat + "&goalname=" + szDestName,
//                szDestName,
//                lon,
//                lat
//        );
        // 티맵 (전용맵) : "com.skt.skaf.l001mtm091"
        // "tmap://route?goalx=$longitude&goaly=$latitude&goalname=$title"
        String uri= null;
        String destName = null;
        float lon = -1;
        float lat = -1;
        if ( mapPoint != null ) {
            destName = mapPoint.getName();
            lon = mapPoint.getLon();
            lat = mapPoint.getLat();
            FirebaseCrashlytics.getInstance().log(String.format("destName/lon/lat : %s / %s / %s", destName, lon, lat));
        }
        String shemName = null;

            if ( PackageUtil.isTmap() ) {
                    // tmap://route?rGoName=목적지&rGoX=\(lng)&rGoY=\(lat)"
                uri = "tmap://route?rGoX=" + lon + "&rGoY=" + lat + "&rGoName=" + mapPoint.getName();
            } else if ( PackageUtil.isNaverMap() ) {
//                    // navermaps://?menu=location&pinType=place&lat=$latitude&lng=$longitude&title=$title
//                uri = "navermaps://?menu=location&pinType=place&lat=" + lat + "&lng=" + lon + "&title=" + mapPoint.getName();
                    // nmap://map?lat=37.4979502&lng=127.0276368&zoom=20&appname=com.example.myapp
                    // nmap://route/car?dlat=\(lat)&dlng=\(lng)&appname=com.example.myApp"
                uri = "nmap://route/car?dlat=" + lat + "&dlng=" + lon + "&appname=" + ctx.getPackageName()+"&dname=" + mapPoint.getName();
            } else if ( PackageUtil.isKakaoMap() ) {
                    // kakaomap://search?q=맛집&p=37.537229,127.005515
//                uri = "kakaomap://search?q=" + mapPoint.getName() + "&p=" + lat + "," + lon + "";
                uri = "kakaomap://look?p=" + lat + "," + lon + "";
                uri = "kakaomap://route?ep=" + lat + "," + lon + "&by=CAR";
            } else if ( PackageUtil.isKakaoNavi() ) {
                    // kakaonavi://destination?name=목적지명&x=경도&y=위도
//                uri = "kakaonavi://destination?name=" + destName + "&x=" + lon + "&y=" + lat + "&by=CAR";
                uri = "kakaonavi://route?ep=" + lat + "," + lon + "&by=CAR";

            } else if ( PackageUtil.isGoogleMap() ) {
                    // http://maps.google.com/maps?saddr="+시작위도+","+시작경도+"&daddr="+도착위도+","+도착경도
                uri = "http://maps.google.com/maps?daddr=" + lat + "," + lon + "";

            } else if ( PackageUtil.isWaze() ) {
                // https://waze.com/ul?q=66%20Acacia%20Avenue&ll=45.6906304,-120.810983&navigate=yes
                uri = "https://waze.com/ul?q=" + destName + "&ll=" + lat + "," + lon + "&navigate=yes";

            } else if ( PackageUtil.isSygic() ) {
                // com.sygic.aura://coordinateaddr|15.06591|47.73341|Customer John Smith|drive
                // com.sygic.aura://coordinate|lon|lat|name
                uri = "com.sygic.aura://coordinate|" + lon + "|" + lat + "|" + destName + "|drive" ;
            } else if ( PackageUtil.isOsmAnd() || PackageUtil.isOsmAndPlus() ) {
                // osmand://?lat=위도&lon=경도&title=장소_이름
                // osmandmaps://?lat=45.6313&lon=34.9955&z=8&title=New+York
                // osmandmaps://navigate?lat=45.6313&lon=34.9955&z=8&title=New+York
                // http://osmand.net/go?lat=51.4404&lon=4.3294&z=16
                uri = "http://osmand.net/go?lat=" + lat + "&lon=" + lon + "&z=8&title=" + destName;
//            } else if ( PackageUtil.isMapsMe() ) {
//                // mapsme://map?q=latitude,longitude
////                uri = "mapsme://map?lat=" + lat + "&lon=" + lon;
//                uri = Package.MAPSME.getId();   // uri가 있는 조건에서 putExtra하기위해 할당.
            } else if ( PackageUtil.isGoDuck() ) {
                // androidamap://navi?sourceApplication=appname&poiname=fangheng&lat=36.547901&lon=104.258354&dev=1&style=2
                uri = "androidamap://navi?sourceApplication="+ ctx.getResources().getString(R.string.app_name)+ "&poiname=" + destName + "&lat=" + lat + "&lon=" + lon + "&dev=1&style=2=";
            } else if ( PackageUtil.isBaidu() ) {
                // baidumap://map/navi
                // baidumap://map/navi?query=Forbidden City&src=andr.baidu.openAPIdemo&viaPoints={"viaPoints":[{"name":"University of Foreign Trade","lng" :116 ,"lat":40}]}
                uri = "baidumap://map/navi?query=" + destName + "&src=andr.baidu.openAPIdemo&viaPoints={\"viaPoints\":[{\"name\":\"" + destName + "\",\"lng\":" + lon + ",\"lat\":" + lat + "}]}"+ ctx.getResources().getString(R.string.app_name)+ "&poiname=" + destName + "&lat=" + lat + "&lon=" + lon + "&dev=1&style=2=";
//                uri = "baidumap://map/navi?query=故宫&src=andr.baidu.openAPIdemo&viaPoints={\"viaPoints\":[{\"name\":\"对外外贸大学\",\"lng\":116,\"lat\":40}]}";
            } else {
//              if ( PackageUtil.isOneNavi() || PackageUtil.isMappy() || PackageUtil.isAltan() || isNaviApp() ) {
                if ( PackageUtil.isOneNavi() || PackageUtil.isMappy() || PackageUtil.isAltan()  ) {
                    uri = "geo:" + lat + "," + lon + "?q=" + destName;
                }
            }


//     Intent(Intent.ACTION_VIEW, Uri.parse("navermaps://?menu=location&pinType=place&lat=$latitude&lng=$longitude&title=$title")).apply {
//    `package` = "com.nhn.android.nmap"
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        }

        if ( MapUtil.isGeofence() ) {
            if ( !PermissionUtil.isBackgroundLocationPermission(ctx)) {
                Utils.goPermission(ctx, "background_location", null);
                OverlayViewUtil.hide(false);
                Utils.toast(ctx,ctx.getString(R.string.msg_permission_background_location));
                return;
            }
            if (mapPoint != null) {
                /**
                 * Store MAP POINT
                 */
                MapUtil.setNaviMapPoint(destName, lat, lon);

                /**
                 * add Fences
                 */
                MapUtil.addGeofences(destName, lat, lon, refence);
            }
        }

        if( PackageUtil.isSDKNavi() ) {
            Fc.setCustomKey(Fc.CUSTOM_KEY_RUNNAVI_RUN_TO_ROUTE_URI,uri);
            MapUtil mapUtil = new MapUtil(ctx);
            if ( lat > -1 ) {
                mapUtil.runToRoute(destName, lat, lon);
            } else {
                mapUtil.runToSearch(destName);
            }
        } else {
            if ( uri != null ) {
                PackageManager pm = ctx.getPackageManager();
                Intent it = null;
                if (PackageUtil.isBaidu()) {
                    it = new Intent();
                    it.setData(Uri.parse(uri));
                } else {
                    it = pm.getLaunchIntentForPackage(getAppPackage()); // "com.skt.skaf.l001mtm091"
                    it  = new Intent(Intent.ACTION_VIEW,Uri.parse(uri));
                }

                Fc.setCustomKey(Fc.CUSTOM_KEY_RUNNAVI_URI,uri);

                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if ( PackageUtil.isGoogleMap() ) {
                    it.addCategory(Intent.CATEGORY_LAUNCHER);
                    it.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
//                } else if ( PackageUtil.isMapsMe() ) {
//                    it = new Intent("com.mapswithme.maps.pro.action.BUILD_ROUTE");
//                    it.setPackage("com.mapswithme.maps.pro");
//                    it.setPackage(Package.MAPSME.getId());
////                    it.putExtra("lat_from", 55.800800);
////                    it.putExtra("lon_from", 37.532754);
////                    it.putExtra("saddr", "Start point");
//                    it.putExtra("lat_to", lat);
//                    it.putExtra("lon_to", lon);
//                    it.putExtra("daddr", destName);
//                    it.putExtra("router", "vehicle");

                } else {
                    it.addCategory(Intent.CATEGORY_BROWSABLE);
                }
                try {
                    ctx.startActivity(it);
                } catch(Exception ex) {
                    Fc.e(ctx,TAG,"runNavi",ex.toString());
                }
            } else {
                runApp(ctx);
            }
        }
    }

    public static void runApp(Context ctx) {
        runApp(ctx, getAppPackage(), null);
    }

    public static void runApp(Context ctx, String pkg, Runnable runnable) {
        PackageManager pm = ctx.getPackageManager();
        Intent it = pm.getLaunchIntentForPackage(pkg); // "com.skt.skaf.l001mtm091"
        if (it == null) {
            L.d(TAG, "runApp","No available activity to launch for package: " + pkg);
            return;
        }
//        it.setData(Uri.parse(uri));
//        Intent it  = new Intent(Intent.ACTION_VIEW,Uri.parse(uri));
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(it);
        Fa.i(ctx,TAG,Fa.EVENT_INVOKE_START_APP);
        if ( runnable != null ) {
            Utils.runExecutorService(runnable);
        }
    }

    public static void runApp(Context ctx, String pkg) {
        runApp(ctx, pkg, null);
    }

    public static void runApp(Context ctx, Runnable runnable) {
        runApp(ctx, getAppPackage(), runnable);
    }

    public static void runSearch(Context ctx,String destName) {
        if( PackageUtil.isSDKNavi() ) {
            Fc.setCustomKey(Fc.CUSTOM_KEY_RUNNAVI_RUN_SEARCH_TO_DESTINATION,destName);
            MapUtil mapUtil = new MapUtil(ctx);
            mapUtil.runToSearch(destName);
        }
    }

    public static void runNavi(Context ctx,NaviDestination naviDestination, boolean refence) {
        Fc.i(ctx,TAG,"MapUtil.runNavi");
        /**
        // TODO SOFTM : 20241209
        //  AutoRunNotificationListenerService.onNotificationRemoved 에서 MapUtil.setNotNaving(); 을 처리하여 실행중 상태를 해제하고 있으나
        //  이 부분이 클리어 되지 않는 상황이 존재하므로 앱을 실행시 클리어
        */
        MapUtil.setNotNaving();
        MapUtil mapUtil = new MapUtil(ctx);
//        if (!cancel) { // 팝업시 취소로 호출되면 네비 실행안하게
        //W
//            MapUtil.isNaviApp()
//            MapUtil.isUsingRegHome()

        if ( MapUtil.isUsingRecentPoiNavi() ) {
            MapPoint mapPoint = MapUtil.getPoiRecent();
            if ( mapPoint != null ) {
                if (NaviDestination.BASIC.equals(mapPoint.getDestination())) {
                    runNavi(ctx, mapPoint, refence);
                    return;
                } else if (NaviDestination.NOTI.equals(mapPoint.getDestination())) {
                    runNavi(ctx, mapPoint, refence);
                    return;
                } else {
                    naviDestination = mapPoint.getDestination();
                }
            }
        }

        if (NaviDestination.HOME.equals(naviDestination)) { // 집
            if ( MapUtil.isUsingRegHome() ) {
                if ( MapUtil.getPoiHome() == null ) {
                    Utils.toast(ctx,R.string.msg_not_reg_home);
                    Utils.goFowardPage(ctx, WUrl.ReqId.WEB_HOME_COMPANY.name(),0,null);
                } else {
                    runNavi(ctx,MapUtil.getPoiHome(),refence);
                }
            } else {
                if (PackageUtil.isSDKHomeCompanyNavi()) {
                    Log.d(TAG, "Map.runToHome");
                    mapUtil.runToHome();
                } else {
                    mapUtil.runApp(ctx);
                }
            }
        } else if (NaviDestination.COMPANY.equals(naviDestination)) { // 회사
            if ( MapUtil.isUsingRegHome() ) {
                if ( MapUtil.getPoiCompany() == null ) {
                    Utils.toast(ctx,R.string.msg_not_reg_compmay);
                    Utils.goFowardPage(ctx, WUrl.ReqId.WEB_HOME_COMPANY.name(),1,null);
                } else {
                    runNavi(ctx,MapUtil.getPoiCompany(),refence);
                }
            } else {
                if (PackageUtil.isSDKHomeCompanyNavi()) {
                    Log.d(TAG, "Map.runToCompany");
                    mapUtil.runToCompany();
                } else {
                    mapUtil.runApp(ctx);
                }
            }
        } else if (NaviDestination.DRIVE.equals(naviDestination)) { // 운전
            if (PackageUtil.isSDKHomeCompanyNavi()) {
                Log.d(TAG, "Map.runToSdk");
                mapUtil.runToSDK(()-> {
                    runSafeDrive(ctx);
                });
            } else {
                mapUtil.runApp(ctx,()-> {
                    runSafeDrive(ctx);
                });
            }
        } else if (NaviDestination.APP.equals(naviDestination)) { // App
            Log.d(TAG, "Map.runApp");
            mapUtil.runApp(ctx,()-> {
                runSafeDrive(ctx);
            });
        }
//        }
    }

    public static void runSafeDrive(Context ctx) {

        if ( MapUtil.isNaviApp() ) {
            int execLimit = 5; // 실행 횟수
            Callbackkt callbackkt = new Callbackkt(5000, "safeDrive.Check;"); /** Delay 타임 설정 가능하게 하자. */
            callbackkt.run((param) -> {
                if (PackageUtil.isTmap()) {/** 안전운전 모드로 진입 */
                    (new AcsUtil(ctx)).safeDrive(ctx, "안전운행", "안전운행", execLimit, false);
                } else if (PackageUtil.isKakaoNavi()) {
                    (new AcsUtil(ctx)).safeDrive(ctx, "안전운전", "안전운전", execLimit, false);
                } else if (PackageUtil.isNaverMap()) {
                    (new AcsUtil(ctx)).safeDrive(ctx, "내비게이션", "내비게이션 탭", execLimit, false);
                    (new Callbackkt(2000, "safeDrive.Check2;")).run((param2) -> {
                        (new AcsUtil(ctx)).nodeClick(ctx, "안전운행", "",  false);
                        return null;
                    });
                } else if (PackageUtil.isGoogleMap()) {
                    List<String> okInfos = StringsXmlValueUtil.getValuesNEnValues(ctx, "directions");
                    if (okInfos.size() == 0) {
                        okInfos.add("Directions");
                        okInfos.add("Route");
                    }
                    for (String btnStr : okInfos) {
                        (new AcsUtil(ctx)).safeDrive(ctx, btnStr, btnStr, execLimit, false);
                    }
                }
                return null;
            });
        }
    }
    public boolean isTmapInstall() {
        getInstanceForTmap(mContext);
        return tmaptapi.isTmapApplicationInstalled();
    }

    public void tmapInstall() {
        new Thread() {
            @Override
            public void run() {
                getInstanceForTmap(mContext);
                Uri uri = Uri.parse(tmaptapi.getTMapDownUrl().get(0));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                mContext.startActivity(intent);
            }

        }.start();
    }

    public static boolean isInstallApp( Context context, String pakageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(pakageName);

        if(intent==null){
            //미설치
            return false;
        }else{
            //설치
            return true;
        }
    }

    public static boolean isEnableLocationServicesStatus(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static boolean isFilePermission(Context context) {
        int hasReadPermission   = ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE);
        int hasWritePermission = ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return hasReadPermission   == PackageManager.PERMISSION_GRANTED &&
                hasWritePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static String getTopPackageName(@NonNull Context context) {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && PermissionUtil.hasUsageStatsPermission(context)) {
            UsageStatsManager usageStatsManager = null;
            usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

            long lastRunAppTimeStamp = 0L;

            final long INTERVAL = 1000 * 60 * 5;
            final long end = System.currentTimeMillis();
            // 1 minute ago
            final long begin = end - INTERVAL;
            LongSparseArray packageNameMap = new LongSparseArray<>();
            final UsageEvents usageEvents = usageStatsManager.queryEvents(begin, end);
            while (usageEvents.hasNextEvent()) {
                UsageEvents.Event event = new UsageEvents.Event();
                usageEvents.getNextEvent(event);

                if (isForeGroundEvent(event)) {
                    packageNameMap.put(event.getTimeStamp(), event.getPackageName());
                    if (event.getTimeStamp() > lastRunAppTimeStamp) {
                        lastRunAppTimeStamp = event.getTimeStamp();
                    }
                }
            }
            return String.valueOf(packageNameMap.get(lastRunAppTimeStamp, ""));
        } else {
            return "";
        }
    }

    public static boolean isFirstStart() {
        String _first = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_FIRST_START, "0");
        return  _first.equals("0")?true:false;
    }

    public static void completeFirstStart() {
        setFirstStart(true);
    }
    public static void setFirstStart(boolean firstStart) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_FIRST_START, firstStart?"1":"0");
    }

    public static boolean isAppInstalled() {
        String _grantedBasicPermission = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_APP_INSTALLED, "0");
        return  _grantedBasicPermission.equals("0")?false:true;
    }

    public static void completeAppInstalled() {
        setAppInstalled(true);
    }
    public static void setAppInstalled(boolean appInstalled) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_APP_INSTALLED, appInstalled?"1":"0");
    }

    public static boolean isSkipAcsPermission() {
        String skipAcsPermission = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_SKIP_ACS_PERMISSION, "0");
        return  skipAcsPermission.equals("0")?false:true;
    }

    public static void setSkipAcsPermission(boolean skipAcsPermission) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_SKIP_ACS_PERMISSION, skipAcsPermission?"1":"0");
    }

    public static boolean isAppUpdated() {
        String _grantedBasicPermission = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_APP_UPDATED, "0");
        return  _grantedBasicPermission.equals("0")?false:true;
    }

    public static void completeAppUpdated() {
        setAppUpdated(true);
    }
    public static void setAppUpdated(boolean appUpdated) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_APP_UPDATED, appUpdated?"1":"0");
    }

    public static boolean isAppUpdatedNotiConfirm() {
        String _grantedBasicPermission = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_APP_UPDATED_NOTI_CONFIRM, "0");
        return  _grantedBasicPermission.equals("0")?false:true;
    }

    public static void completeAppUpdatedNotiConfirm() {
        setAppUpdatedNotiConfirm(true);
    }
    public static void setAppUpdatedNotiConfirm(boolean v) {
        WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_APP_UPDATED_NOTI_CONFIRM, v?"1":"0");
    }

    /**
     * 앱 업데이트 시간
     * @return
     */
    public static long getAppUpdatedTime() {
        long v  = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_APP_UPDATED_TIME, 0L);
        return v;
    }
    public static void setAppUpdatedTime() {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_APP_UPDATED_TIME,System.currentTimeMillis());
    }

    public static String getAccessibilityServiceEnableCount() { // _accessibility_service_enable_count
        String _cnt = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_ACCESSIBILITY_SERVICE_ENABLE_COUNT, "0");  // 0
        return _cnt;
    }

    public static void increaseAccessibilityServiceEnableCount() {
        long _cnt = Long.parseLong(getAccessibilityServiceEnableCount());
        setAccessibilityServiceEnableCount(String.valueOf(_cnt + 1));
    }

    public static void setAccessibilityServiceEnableCount(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_ACCESSIBILITY_SERVICE_ENABLE_COUNT, v);
    }

    public static String getPostNotificationsConfirmCount() { // _accessibility_service_enable_count
        String _cnt = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POST_NOTIFICATIONS_CONFIRM_COUNT, "0");  // 0
        return _cnt;
    }

    public static void increasePostNotificationsConfirmCount() {
        long _cnt = Long.parseLong(getPostNotificationsConfirmCount());
        setPostNotificationsConfirmCount(String.valueOf(_cnt + 1));
    }

    public static void setPostNotificationsConfirmCount(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_POST_NOTIFICATIONS_CONFIRM_COUNT, v);
    }

    public static String getInitToCount() { // _accessibility_service_enable_count
        String _cnt = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_INIT_TO_COUNT, "0");  // 0
        return _cnt;
    }

    public static void increaseInitToCount() {
        long _cnt = Long.parseLong(getPostNotificationsConfirmCount());
        setPostNotificationsConfirmCount(String.valueOf(_cnt + 1));
    }

    public static void setInitToCount(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_INIT_TO_COUNT, v);
    }

    public static long getAppUpdateCheckTime() { //
        String _v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_APP_UPDATE_CHECK_TIME, "0");  // 0
        _v = StringUtils.defaultString(_v,"0");
        return Long.valueOf(_v);
    }

    public static void setAppUpdateCheckTime(long v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_APP_UPDATE_CHECK_TIME, String.valueOf(v));
    }

    public static long getAppUpdateDelayTime() { //
        String _v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_APP_UPDATE_DELAY_TIME, String.valueOf(Const.APP_UPDATE_DELAY_TIME));  // 0
        return Long.valueOf(_v);
    }

    public static void setAppUpdateDelayTime(long v) {
        Const.APP_UPDATE_DELAY_TIME = v;
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_APP_UPDATE_DELAY_TIME, String.valueOf(v));
    }

    public static String getActonStartLockCancelCount() { // _acton_start_lock_cancel_count
        String _cnt = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_ACTON_START_LOCK_CANCEL_COUNT, "0");  // 0
        return _cnt;
    }

    public static void increaseActonStartLockCancelCount() {
        long _cnt = Long.parseLong(getActonStartLockCancelCount());
        setAccessibilityServiceEnableCount(String.valueOf(_cnt + 1));
    }

    public static void setActonStartLockCancelCount(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_ACTON_START_LOCK_CANCEL_COUNT, v);
    }

    public static String getActonStopLockCancelCount() { // _acton_stop_lock_cancel_count
        String _cnt = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_ACTON_STOP_LOCK_CANCEL_COUNT, "0");  // 0
        return _cnt;
    }

    public static void increaseActonStopLockCancelCount() {
        long _cnt = Long.parseLong(getActonStopLockCancelCount());
        setAccessibilityServiceEnableCount(String.valueOf(_cnt + 1));
    }

    public static void setActonStopLockCancelCount(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_ACTON_STOP_LOCK_CANCEL_COUNT, v);
    }

    public static String getActonStartUnLockCancelCount() { // _acton_start_UNlock_cancel_count
        String _cnt = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_ACTON_START_UNLOCK_CANCEL_COUNT, "0");  // 0
        return _cnt;
    }

    public static void increaseActonStartUnLockCancelCount() {
        long _cnt = Long.parseLong(getActonStartUnLockCancelCount());
        setAccessibilityServiceEnableCount(String.valueOf(_cnt + 1));
    }

    public static void setActonStartUnLockCancelCount(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_ACTON_START_UNLOCK_CANCEL_COUNT, v);
    }

    public static String getActonStopUnLockCancelCount() { // _acton_stop_UNlock_cancel_count
        String _cnt = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_ACTON_STOP_UNLOCK_CANCEL_COUNT, "0");  // 0
        return _cnt;
    }

    public static void increaseActonStopUnLockCancelCount() {
        long _cnt = Long.parseLong(getActonStopUnLockCancelCount());
        setAccessibilityServiceEnableCount(String.valueOf(_cnt + 1));
    }

    public static void setActonStopUnLockCancelCount(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_ACTON_STOP_UNLOCK_CANCEL_COUNT, v);
    }

    public static boolean isStart() {
        return isStartPower() || isStartBluetooth() || isStartWifi();
    }

    public static boolean isIndependenceCondition() {
        String _independenceCondition = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_INDEPENDENCE_CONDITION, "1"); // 독립조건
        return _independenceCondition.equals("1")?true:false;
    }

    public static void setIndependenceCondition(boolean independenceCondition) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_INDEPENDENCE_CONDITION, independenceCondition?"1":"0");
    }

    public static boolean isStartPower() {
        String _start = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_START_POWER, "1"); // 시작
        return  _start.equals("1")?true:false;
    }

    public static void setStart(boolean start) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_START_POWER, start?"1":"0");
    }

    public static boolean isStartBluetooth() {
        String _start = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_START_BLUETOOTH, "0"); // 종료
        return  _start.equals("1")?true:false;
    }

    public static void setStartBluetooth(boolean start) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_START_BLUETOOTH, start?"1":"0");
    }


    public static boolean isStartWifi() {
        String _start = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_START_WIFI, "0"); // 종료
        return  _start.equals("1")?true:false;
    }

    public static void setStartWifi(boolean start) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_START_WIFI, start?"1":"0");
    }

    public static RunStatusScreenMode getRunStatusScreenStartMode() {
        String runStatusScreenMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RUN_STATUS_SCREEN_START_MODE, RunStatusScreenMode.NONE.getId()); // 사용안함.
        return RunStatusScreenMode.value(runStatusScreenMode);
    }

    public static void setRunStatusScreenStartMode(RunStatusScreenMode v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_RUN_STATUS_SCREEN_START_MODE, v.getId());
    }

    public static RunStatusScreenMode getRunStatusScreenStopMode() {
        String runStatusScreenMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RUN_STATUS_SCREEN_STOP_MODE, RunStatusScreenMode.ON.getId()); // 사용안함.
        return RunStatusScreenMode.value(runStatusScreenMode);
    }

    public static void setRunStatusScreenStopMode(RunStatusScreenMode v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_RUN_STATUS_SCREEN_STOP_MODE, v.getId());
    }

    public static void setRunStatusScreenTimeForStart(String value) {
        int v = Integer.parseInt(value);
        Const.RUN_STATUS_SCREEN_TIME_FOR_START = v*1000;
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_START, String.valueOf(v));
    }

    public static float getRunStatusScreenTimeStart() {
        return Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_START,String.valueOf(Const.RUN_STATUS_SCREEN_TIME_FOR_START /1000))).intValue();
    }

    public static void setRunStatusScreenTimeForStop(String value) {
        int v = Integer.parseInt(value);
        Const.RUN_STATUS_SCREEN_TIME_FOR_START = v*1000;
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_STOP, String.valueOf(v));
    }

    public static float getRunStatusScreenTimeStop() {
        return Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_STOP,String.valueOf(Const.RUN_STATUS_SCREEN_TIME_FOR_START /1000))).intValue();
    }

    public static boolean isOnlyStopAfterNaving() {
        String _v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_ONLY_STOP_AFTER_NAVING, "0"); // Off
        return  _v.equals("1")?true:false;
    }

    public static void setOnlyStopAfterNaving(boolean _v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_ONLY_STOP_AFTER_NAVING, _v?"1":"0");
    }

    public static ChargingMode getChargingMode() {
        String chargingMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_CHARGING_MODE, String.valueOf(ChargingMode.ALL_CHARGING.getId())); // 유선
        return ChargingMode.value(chargingMode);
    }

    public static void setChargingMode(ChargingMode v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_CHARGING_MODE, v.getId());
    }

    public static NaviDestination getNaviDestination() {
        String naviDestination = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_DESTINATION, NaviDestination.HOME.getId()); // 집
        return NaviDestination.value(naviDestination);
    }

    public static void setNaviDestination(NaviDestination v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_DESTINATION, v.getId());
    }

    public static NaviMode getNaviMode() {
        String naviMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_MODE, NaviMode.DRIVE.getId()); // 집<>회사
        return NaviMode.value(naviMode);
    }

    public static void setNaviMode(NaviMode v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_MODE, v.getId());
    }

    public static boolean isNaving() { // _NAVING -->_NAVING
//        String naviCallAble = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVING,"NO");
//        return "YES".equals(naviCallAble)?true:false;
        Log.d(TAG,"isNaving() : " +  !isNotNaving());
        return !isNotNaving();
    }

    public static boolean isNotNaving() {
        String naviCallAble = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVING,"NO");
        long  currentTimeMillis = System.currentTimeMillis();
        long  naviStartTime     = getNaviStartTime() ;
        long  naviEndTime       = getNaviEndTime() ; /** setNotNaving시 endTime이 저장됨 : 마지막 내비실행종료가 실행된후 30분 경과 되었다면 ture 즉 notNaving 상태 */
              naviEndTime       = naviEndTime   == 0 ? currentTimeMillis : naviEndTime;
              naviStartTime     = naviStartTime == 0 ? currentTimeMillis : naviStartTime;
        long  elapsedEndTime    = currentTimeMillis - naviEndTime;
        long  elapsedStartTime  = currentTimeMillis - naviStartTime;
        float elapsedEndMin     = elapsedEndTime / 1000f / 60f;
        float elapsedStartMin   = elapsedStartTime / 1000f / 60f;

        boolean isNaviCallAble  = "NO".equals(naviCallAble);
        boolean isStartPass3Hour = elapsedStartMin>=180;
        boolean isEndPass30Min  = elapsedEndMin>=30;

        /** 시작후 5분이 지났거나, 종료후 30분이 지났다면 notNaving 상태로 간주한다. */
        boolean rtn = isNaviCallAble || isStartPass3Hour || isEndPass30Min ? true : false;
        L.d(TAG,String.format("isNotNaving[%s], %s, %s, %s",rtn, isNaviCallAble,isStartPass3Hour,isEndPass30Min));
        Log.d(TAG,"isNotNaving() : " +  rtn);
        return rtn;
    }

    public static String getNaving() {
        return WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVING,"NO");
    }

    public static void initNaving() {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVING,"NO");
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_START_TIME,0l);
        setNaviEndTime(0l);
    }

    public static void setNaving() {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVING,"YES");
        long t = System.currentTimeMillis();
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_START_TIME,t);
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_LAST_START_TIME,t);
        setNaviEndTime(0l);
    }

    public static void setNotNaving() {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVING,"NO");
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_START_TIME,0l);
        setNaviEndTime();
    }

    public static String getAppPackage() {
        /** 기도앱 - 강제 : 숨김 안함
        return WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_APP_PACKAGE, Package.SKT_MAP.getId());
         * */
        return WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_APP_PACKAGE, "");
    }

    public static void setAppPackage(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_APP_PACKAGE,v);

        MapUtil.setNotNavingForNotiListener();
        MapUtil.setNotNaving();

        loadAppPackage(getAppPackage(),AppType.NAVI);
    }

    public static String getMusicAppPackage() {
        /** 기도앱 - 강제 : 기능 해제
        return WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_MUSIC_APP_PACKAGE, PackageMusic.SAMSUNG_MUSIC.getId());
         * */
        return WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_MUSIC_APP_PACKAGE, "");
    }

    public static void setMusicAppPackage(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_MUSIC_APP_PACKAGE,v);
        loadAppPackage(MapUtil.getMusicAppPackage(), AppType.MUSIC);
    }

    public static String getMusicPlayTitle() {
        return WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_MUSIC_PLAY_TITLE, PackageMusic.SAMSUNG_MUSIC.getId());
    }

    public static void setMusicPlayTitle(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_MUSIC_PLAY_TITLE,v);
    }


    public static boolean isAllOffForStop() {
        StartStopOnOffVO ss = MapUtil.getStartStopOnOff();
        return  !ss.getBluetooth().getStop().isUseYn() &&
                !ss.getWifi().getStop().isUseYn() &&
                !ss.getTethering().getStop().isUseYn() &&
                !ss.getAirplaneMode().getStop().isUseYn() &&
                !ss.getScreenOn().getStop().isUseYn() &&
                !ss.getLock().getStop().isUseYn() &&
                !ss.getCompletedLock().getStop().isUseYn() &&
                !ss.getPlayMusic().getStop().isUseYn() &&
                !ss.getHomeScreen().getStop().isUseYn()
        ;
    }

    public static StartStopOnOffVO getStartStopOnOff() {
        return (new Gson()).fromJson(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_START_STOP_ON_OFF,
                "{" +
                        "bluetooth:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
                        "wifi:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
                        "tethering:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
                        "airplane_mode:{start:{use_yn:false,onoff:'off'},stop:{use_yn:false,onoff:'on'}},"+
                        "screen_on:{start:{use_yn:true,onoff:'on'},stop:{use_yn:true,onoff:'on'}},"+
                        "lock:{start:{use_yn:true,onoff:'off'},stop:{use_yn:true,onoff:'off'}},"+
                        "completed_lock:{start:{use_yn:false,onoff:'off'},stop:{use_yn:true,onoff:'on'}},"+
                        "play_music:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'off'}},"+
                        "home_screen:{start:{use_yn:false,onoff:'on'},stop:{use_yn:false,onoff:'on'}},"+
                        "power_off:{start:{use_yn:false,onoff:'off'},stop:{use_yn:false,onoff:'on'}}"+
                "}"

        ), StartStopOnOffVO.class);
    }

    public static void setStartStopOnOff(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_START_STOP_ON_OFF,v);
    }

//    public static StartMode getStartMode() {
//        String startMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_START_MODE, StartMode.START.getId()); // 기본시작.
//        return StartMode.value(startMode);
//    }
//
//    public static void setStartMode(StartMode v) {
//        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_START_MODE, v.getId());
//    }

    public static StopMode getStopMode() {
        String stopMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_STOP_MODE, StopMode.FORCE.getId()); // 강제종료.
        return StopMode.value(stopMode);
    }

    public static void setStopMode(StopMode v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_STOP_MODE, v.getId());
    }

    public static AppEnable getAppEnable() {
        String appEnable = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_APP_ENABLE, AppEnable.ENABLE.getId()); // 기본 Enable.
        return AppEnable.value(appEnable);
    }

    public static boolean isAppEnable() {
        return getAppEnable().equals(AppEnable.ENABLE)?true:false;
    }

    public static void setAppEnable(AppEnable v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_APP_ENABLE, v.getId());
        /**
        // TODO SOFTM : 20241209
        //  AutoRunNotificationListenerService.onNotificationRemoved 에서 MapUtil.setNotNaving(); 을 처리하여 실행중 상태를 해제하고 있으나
        //  이 부분이 클리어 되지 않는 상황이 존재하므로 앱을 실행시 클리어
        */
        MapUtil.setNotNaving();
    }

    public static HashMap getBluetooth() {
        return (new Gson()).fromJson(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_BLUETOOTH,""), HashMap.class);
//        return (new Gson()).fromJson(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_BLUETOOTH,""), Object.class);
//        {"bondState":12,"address":"FC:58:FA:63:1C:A7","name":"MINISO D9","type":1,"diassembled":"MINISO D9"}
    }

    public static HashMap getWifi() {
        return (new Gson()).fromJson(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_WIFI,"{}"), HashMap.class);
    }

    public static void setBluetooth(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_BLUETOOTH,v);
        MapUtil.setNotNavingForNotiListener();
        MapUtil.setNotNaving();
    }

    public static void setWifi(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_WIFI,v);

        MapUtil.setNotNavingForNotiListener();
        MapUtil.setNotNaving();
    }

    public static List<HashMap> getBluetooths() {
        Type listType = new TypeToken<List<HashMap>>() {}.getType();
        return (new Gson()).fromJson(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_BLUETOOTHS,"[]"), listType);
    }

    public static void setBluetooths(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_BLUETOOTHS,v);
        MapUtil.setNotNavingForNotiListener();
        MapUtil.setNotNaving();
    }

    public static String appendAppNameForPackage(String packageName, String appName) {
        String v = "";

        if ( Package.SKT_MAP.equals(packageName) && appName.equals("TMAP") ) {
            v = appName + "(티맵)";
        } else {
            v = appName;
        }
        return v;
    }

    public static void loadAppPackage(String pkg, AppType appType) {
//        String v = getAppPackage();

        PackageManager pm = WApplication.mInstance.getPackageManager();

        ApplicationInfo app = null;
        Drawable drawable = null;
        AppInfoDTO appInfo =  new AppInfoDTO();
        String name = null;
        try {
            app = pm.getApplicationInfo( pkg, PackageManager.GET_META_DATA);
            name = app.loadLabel(pm).toString().replaceAll(System.getProperty("line.separator"), " ");
            drawable = app.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
        } finally {
            if (AppType.NAVI.equals(appType) ) {
                appInfo.setName(appendAppNameForPackage(pkg, name == null ? "TMAP" : name));
            } else {
                appInfo.setName(appendAppNameForPackage(pkg, name == null ? "" : name));
            }
            appInfo.setPackageName(pkg);
            appInfo.setIcon(drawable);
            if ( drawable != null ) appInfo.setEnCodeicon(encodeIcon(drawable));
            appInfo.setDetail(app);
            if (AppType.NAVI.equals(appType) ) {
                WApplication.appInfo = appInfo;
            } else if (AppType.MUSIC.equals(appType) ) {
                WApplication.musicAppInfo = appInfo;
            }
        }
    }

    public static boolean isOverlayOpen() {
        return !isOverlayClose();
    }

    public static boolean isOverlayClose() {
        String _defaultNaviMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_POPUP_MODE, MapUtil.isUISupport()?"1":"1"); // 1 : 팝업유지, 0 : 팝업닫기
        return _defaultNaviMode.equals("0")?true:false;
    }

    public static String getPopupMode() {
        String _defaultNaviMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_POPUP_MODE, "0");  // 1 : 팝업유지, 0 : 팝업닫기
        return _defaultNaviMode;
    }

    public static void setPopupMode(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_POPUP_MODE, v);
    }

//    public static boolean isNaviAutoStart() {
//        String _naviAutoStart = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_AUTO_START, MapUtil.isUISupport()?"1":"1"); // 1 : 실행, 0 : 안함
//        return _naviAutoStart.equals("0")?false:true;
//    }

    public static StartMode getNaviAutoStart() {
        String naviAutoStart = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_AUTO_START, StartMode.START.getId()); // 1 : 실행, 2 : 대기, 0 : 안함
        return StartMode.value(naviAutoStart);
    }

    public static void setNaviAutoStart(StartMode v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_AUTO_START, v.getId());
    }

    public static boolean isUsingRegHome() {
        String _enable = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_USING_REG_HOME, "N");
        return  _enable.equals("Y")?true:false;
    }

    public static String getUsingRegHome() {
        String _enable = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_USING_REG_HOME, "N");
        return  _enable;
    }

    public static void setUsingRegHome(boolean v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_USING_REG_HOME, v?"Y":"N");
    }

    /**
     * 안내 종료 시간
     * @return
     */
    public static long getNaviEndTime() {
        long v  = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_END_TIME, 0L);
        return v;
    }
    public static void setNaviEndTime(long naviEndTime) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_END_TIME,naviEndTime);
    }
    public static void setNaviEndTime() {
        long naviEndTime= System.currentTimeMillis();
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_END_TIME,naviEndTime);
    }

    /**
     * 안내 시작 시간
     * @return
     */
    public static long getNaviStartTime() {
        long naviEndTime = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_START_TIME, 0l);
        return Long.valueOf(naviEndTime).longValue();
    }

    /**
     * 안내 시작 시간 - 마지막
     * @return
     */
    public static long getNaviLastStartTime() {
        long t = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_LAST_START_TIME, 0l);
        return Long.valueOf(t).longValue();
    }

    /**
     * 안내 시작 시간 - 마지막
     * @return
     */
    public static long getNaviStartCallTime() {
        long t = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_START_CALL_TIME, 0l);
        return Long.valueOf(t).longValue();
    }

    public static void setNaviStartCallTime() {
        long t = System.currentTimeMillis();
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_START_CALL_TIME,t);
    }

    /**
     * store 안내 information
     * @return
     */
    public static void setNaviMapPoint(String name
                                         , float lat
                                         , float lon
    ) {
        MapPoint v = new MapPoint(name,lat,lon);
        Gson gson = new Gson();
        WApplication.mInstance.setSharedPreferences("_NAVI_MAP_POINT", gson.toJson(v));
    }

    /**
     * clear 안내 information
     * @return
     */
    public static void clearNaviMapPoint() {
        WApplication.mInstance.setSharedPreferences("_NAVI_MAP_POINT", "");
    }

    /**
     * get store 안내 information
     * @return
     */
    public static MapPoint getNaviMapPoint() {
        Gson gson = new Gson();
        String v = WApplication.mInstance.getSharedPreferences("_NAVI_MAP_POINT", "");
        MapPoint rtn = null;
        try {
            rtn = gson.fromJson(v,MapPoint.class);
        } catch (Exception ex){
        }

        return rtn;
    }

    public static MapPoint getPoiHome() {
        String poi = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POI_HOME, "");
        MapPoint mapPoint = MapUtil.getPointFromString(poi);
        return mapPoint;
    }

    public static MapPoint getPoiCompany() {
        String poi = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POI_COMPANY, "");
        MapPoint mapPoint = MapUtil.getPointFromString(poi);
        return mapPoint;
    }

    public static ArrayList<MapPoint> getPoisFavorite() {
        String pois = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POIS_FAVORITE, "");
        ArrayList<MapPoint> mapPoints = MapUtil.getPointsFromString(pois);
        return mapPoints;
    }

    public static void removePoiFavoriteForIndex(int index) {
        List<MapPoint> favoritePoints = getPoisFavorite();
        if ( favoritePoints.size() > index ) {
            favoritePoints.remove(index);
            WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_POIS_FAVORITE, (new Gson()).toJson(favoritePoints));
        }
    }

    public static void clearPoiRecent() {
        clearPoiNaviExpireTime();
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_POI_RECENT, "");
    }

    public static void setPoiRecent(String value) {
        updatePoiNaviExpireTime();
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_POI_RECENT, value);
    }

    public static void setPoiRecent(MapPoint mapPoint) {
        updatePoiNaviExpireTime();
        WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_POI_RECENT, (new Gson()).toJson(new ArrayList<>(Arrays.asList(mapPoint))));
    }

    public static MapPoint getPoiRecent() {
        String pois = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POI_RECENT, "");
        MapPoint mapPoint = MapUtil.getPointFromString(pois);
        return mapPoint;
    }
//        localStorageRecent.delPoi("key", poi.id + "_" + poi.getName(), function () {
//        var mapPoint = objectToMapPoint(poi);
//        localStorageRecent.addPoi(mapPoint, function () {
//
//        }
    public static void addPoiRecent(MapPoint mapPoint) {
        List<MapPoint> recentPoints = getPoisRecent();
/**     for( MapPoint poi : recentPoints ) {  java.util.ConcurrentModificationException */
        for(int i=0; i< recentPoints.size(); i++) {
            MapPoint poi = recentPoints.get(i);
            if ( poi.getName().equals(mapPoint.getName()) ) {
                recentPoints.remove(poi);
            }
        }
        mapPoint.setId(mapPoint.getLat()+"_"+mapPoint.getLon());
        mapPoint.setId(mapPoint.getLat()+"_"+mapPoint.getLon()+"_"+mapPoint.getName());
        mapPoint.setCreated(System.currentTimeMillis());
        recentPoints.add(mapPoint);
        Collections.sort(recentPoints, new Comparator<MapPoint>() {
            @Override
            public int compare(MapPoint p1, MapPoint p2) {
                return Long.compare(p2.getCreated(), p1.getCreated()); // 내림
            }
        });

        WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_POIS_RECENT, (new Gson()).toJson(recentPoints));
    }

    public static void removePoiRecentForIndex(int index) {
        List<MapPoint> recentPoints = getPoisRecent();
        if ( recentPoints.size() > index ) {
            recentPoints.remove(index);
            WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_POIS_RECENT, (new Gson()).toJson(recentPoints));
        }
    }

    public static ArrayList<MapPoint> getPoisRecent() {
        String pois = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POIS_RECENT, "");
        ArrayList<MapPoint> mapPoints = MapUtil.getPointsFromString(pois);
        return mapPoints;
    }

    public static boolean isUsingRecentPoiNavi() {
        return getPoiNaviExpireTime() - System.currentTimeMillis() > 0;
    }

    public static long getPoiNaviExpireTime() { //
        String _v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POI_NAVI_EXPIRE_TIME, "0");  // 0
        _v = StringUtils.defaultString(_v,"0");
        return Long.valueOf(_v);
    }

    public static void updatePoiNaviExpireTime() {
//        long fiveMin = System.currentTimeMillis() + (1000L * 60 * 3);
        long fiveMin = System.currentTimeMillis() + (Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME); // 1000L * 30
        WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_POI_NAVI_EXPIRE_TIME, String.valueOf(fiveMin));
    }

    public static void clearPoiNaviExpireTime() {
        WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_POI_NAVI_EXPIRE_TIME, String.valueOf(0));
    }

    public static boolean isEnableWorkGotoOffTime() {
        String _enable = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_WORK_GOTO_OFF_TIME, "0"); // 사용
        return  _enable.equals("1")?true:false;
    }

    public static void setWorkGotoOffTime(boolean enable) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_WORK_GOTO_OFF_TIME, enable?"1":"0");
    }

    public static void setWorkGotoTimeFrom(String value) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_WORK_GOTO_TIME_FROM, value);
    }

    public static long getWorkGotoTimeFromForLong() {
        String v = getWorkGotoTimeFrom();
        long rtn = Utils.dateStringToTime((new SimpleDateFormat("yyyy-MM-dd")).format(new Date()) + " " + v + ":00");
        return rtn;
    }

    public static String getWorkGotoTimeFrom() {
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_WORK_GOTO_TIME_FROM, "06:00");
        return v;
    }

    public static void setWorkGotoTimeTo(String value) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_WORK_GOTO_TIME_TO, value);
    }

    public static long getWorkGotoTimeToForLong() {
        String v = getWorkGotoTimeTo();
        long rtn = Utils.dateStringToTime((new SimpleDateFormat("yyyy-MM-dd")).format(new Date()) + " " + v + ":59");
        return rtn;
    }

    public static String getWorkGotoTimeTo() {
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_WORK_GOTO_TIME_TO, "08:50");
        return v;
    }


    public static void setWorkOffTimeFrom(String value) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_WORK_OFF_TIME_FROM, value);
    }

    public static long getWorkOffTimeFromForLong() {
        String v = getWorkOffTimeFrom();
        long rtn = Utils.dateStringToTime((new SimpleDateFormat("yyyy-MM-dd")).format(new Date()) + " " + v + ":00");
        return rtn;
    }

    public static String getWorkOffTimeFrom() {
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_WORK_OFF_TIME_FROM, "17:30");
        return v;
    }

    public static void setWorkOffTimeTo(String value) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_WORK_OFF_TIME_TO, value);
    }

    public static long getWorkOffTimeToForLong() {
        String v = getWorkOffTimeTo();
        long rtn = Utils.dateStringToTime((new SimpleDateFormat("yyyy-MM-dd")).format(new Date()) + " " + v + ":59");
        return rtn;
    }

    public static String getWorkOffTimeTo() {
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_WORK_OFF_TIME_TO, "19:00");
        return v;
    }
    public static boolean isNavingForNotiListener() { // _NAVING -->_NAVING
        String naviCallAble = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVING,"NO");
        return "YES".equals(naviCallAble)?true:false;
    }

    public static String getNavingForNotiListener() {
        return WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVING,"NO");
    }
    public static void initNavingForNotiListener() {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVING,"NO");
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVI_START_TIME,0l);
        setNaviEndTimeForNotiListener(0l);
    }

    public static void setNavingForNotiListener() {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVING,"YES");
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVI_START_TIME,System.currentTimeMillis());
        setNaviEndTimeForNotiListener(0l);
    }

    public static void setNotNavingForNotiListener() {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVING,"NO");
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVI_START_TIME,0l);
        setNaviEndTimeForNotiListener();
    }

    /**
     * 안내 종료 시간 : 노티를 통해 판단
     * @return
     */
    public static long getNaviEndTimeForNotiListener() {
        long v  = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVI_END_TIME, 0L);
        return v;
    }

    public static void setNaviEndTimeForNotiListener() {
        long naviEndTime = System.currentTimeMillis();
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVI_END_TIME,naviEndTime);
    }

    public static void setNaviEndTimeForNotiListener(long naviEndTime) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVI_END_TIME,naviEndTime);
    }

    /**
     * 안내 시작 시간 : 노티를 통해 판단
     * @return
     */
    public static long getNaviStartTimeForNotiListener() {
        long naviEndTime = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NOTI_LISTENER_NAVI_START_TIME, 0l);
        return Long.valueOf(naviEndTime).longValue();
    }

    public static boolean isOpenPopup() {
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_OPEN_POPUP,"0");
        return "1".equals(v)?true:false;
    }

    public static void setOpenPopup(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_OPEN_POPUP, v);
    }
    public static void setNotiIcon(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NOTI_ICON, v);
    }
    public static void setTest(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_TEST, v);
    }

    public static boolean isCheckServiceForUser() {
        String _independenceCondition = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_CHECK_SERVICE, "1");
        return _independenceCondition.equals("1")?true:false;
    }

    public static void setCheckService(boolean checkService) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_CHECK_SERVICE, checkService ? "1" : "0");
    }

    public static UseStatusMode getCheckServiceCheckNoti() {
        String useStatusMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_CHECK_SERVICE_CHECK_NOTI, UseStatusMode.NONE.getId()); // 사용안함.
        return UseStatusMode.value(useStatusMode);
    }

    public static void setCheckServiceCheckNoti(UseStatusMode v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_CHECK_SERVICE_CHECK_NOTI, v.getId());
    }

    public static UseStatusMode getCheckserviceCheckLogSendNoti() {
        String useStatusMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_CHECK_SERVICE_CHECK_LOG_SEND_NOTI, UseStatusMode.NONE.getId()); // 사용안함.
        return UseStatusMode.value(useStatusMode);
    }

    public static void setCheckserviceCheckLogSendNoti(UseStatusMode v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_CHECK_SERVICE_CHECK_LOG_SEND_NOTI, v.getId());
    }

    public static boolean isNotiIcon() {
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NOTI_ICON,"1");
        return "1".equals(v)?true:false;
    }

    public static boolean isTest() {
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_TEST,"0");
        return "1".equals(v)?true:false;
    }

    public static void setEnhancedExecution(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_ENHANCED_EXECUTION, v);
    }

    public static boolean isEnhancedExecution() {
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_ENHANCED_EXECUTION,"0");
        return "1".equals(v)?true:false;
    }

//    public static boolean isNotiIcon() {
////        NotificationManager notiManager = (NotificationManager) WApplication.mInstance.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
////        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
////            boolean v = notiManager.areNotificationsEnabled();
////            return v;
////        }
//
//        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(WApplication.mInstance.getApplicationContext());
//        boolean v = notificationManagerCompat.areNotificationsEnabled();
//
//        return v;
//    }

    public static boolean isUISupport() {
        return Const.UNSUPPORT_UI_SDK_INT < Build.VERSION.SDK_INT;
    }

    public static NaviDestination getNaviDestinationForTimeOld() {
        Calendar c = Calendar.getInstance();
        int ampm = c.get(Calendar.AM_PM);
        if ( c.get(Calendar.AM_PM) == 0 ) {

        }
        NaviDestination naviDestination = null;
        switch (ampm) {
            case 0: // 오전
                if (MapUtil.getNaviMode().equals(NaviMode.HOMETOCOMPANY)) { // 집↔회사
                    naviDestination = NaviDestination.COMPANY;
                } else if (MapUtil.getNaviMode().equals(NaviMode.HOMETOFAVORITE)) { // 집↔목적지
                    naviDestination = NaviDestination.HOME;
                } else if (MapUtil.getNaviMode().equals(NaviMode.DRIVE)) { // 운전
                    naviDestination = NaviDestination.DRIVE;
                }
                break;
            case 1: // 오후
                if (MapUtil.getNaviMode().equals(NaviMode.HOMETOCOMPANY)) { // 집↔회사
                    naviDestination = NaviDestination.HOME;
                } else if (MapUtil.getNaviMode().equals(NaviMode.HOMETOFAVORITE)) { // 집↔목적지
                    naviDestination = NaviDestination.FAVORITE;
                } else if (MapUtil.getNaviMode().equals(NaviMode.DRIVE)) { // 운전
                    naviDestination = NaviDestination.DRIVE;
                }
                break;
            default:
        }
        return naviDestination;

    }
    public static NaviDestination getNaviDestinationInfo() {
        NaviDestination naviDestination = null;
//        if ( MapUtil.isTmapApp() ) {
            naviDestination = MapUtil.getNaviDestinationForTime();
//        } else {
//            naviDestination = NaviDestination.APP;
//        }
        return naviDestination;
    }

    public static NaviDestination getNaviDestinationForTime() {
        Calendar c = Calendar.getInstance();
        int ampm = c.get(Calendar.AM_PM);
        NaviDestination naviDestination = null;
        switch (ampm) {
            case 0: // 오전
                if( MapUtil.isUISupport()) {
                    if (MapUtil.getNaviMode().equals(NaviMode.HOMETOCOMPANY)) { // 집↔회사
                        naviDestination = NaviDestination.COMPANY;
                    } else if (MapUtil.getNaviMode().equals(NaviMode.DRIVE)) { // 운전
                        naviDestination = NaviDestination.DRIVE;
                    }
                } else {
                    naviDestination = NaviDestination.COMPANY;
                }
                break;
            case 1: // 오후
                if( MapUtil.isUISupport()) {
                    if (MapUtil.getNaviMode().equals(NaviMode.HOMETOCOMPANY)) { // 집↔회사
                        naviDestination = NaviDestination.HOME;
                    } else if (MapUtil.getNaviMode().equals(NaviMode.DRIVE)) { // 운전
                        naviDestination = NaviDestination.DRIVE;
                    }
                } else {
                    naviDestination = NaviDestination.HOME;
                }
                break;
            default:
        }
        return naviDestination;
    }

    public static boolean isTopForTmap(@NonNull final Context context) {
        String topPackeName = MapUtil.getTopPackageName(context);
        return Package.SKT_MAP.equals(topPackeName);
    }

    public static boolean isPackageForTmap(String packageName) {
        return Package.SKT_MAP_RED.equals(packageName) ||
                Package.SKT_MAP.equals(packageName);
    }


    public static boolean isNoticeAlertMusicPlayer() {
        String _v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NOTICE_ALERT_MUSIC_PLAYER, "0"); // Off
        return  _v.equals("1")?true:false;
    }

    public static void setNoticeAlertMusicPlayer(boolean _v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NOTICE_ALERT_MUSIC_PLAYER, _v?"1":"0");
    }

    public static void startNaviForExecActivity(Context context, NaviDestination naviDestination) {
        startNaviForExecActivity(context,naviDestination,true);
    }

    public static void startNaviForExecActivity(Context context, NaviDestination naviDestination, boolean newTask) {
        startNaviForExecActivity(context,naviDestination, newTask, null, -1,-1);
    }

    public static void startNaviForExecActivity(Context context, NaviDestination naviDestination, boolean newTask ,String name, float lat, float lon) {
        Intent intentActivityHome = new Intent(context,ExecActivity.class); // 메인
        intentActivityHome.putExtra("P_START_NAVI", "YES");
        intentActivityHome.putExtra("P_NAVI_DESTINATION", naviDestination);
        intentActivityHome.putExtra("P_NAME", name);
        intentActivityHome.putExtra("P_LAT", lat);
        intentActivityHome.putExtra("P_LON", lon);
//        intentActivityHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if ( newTask ) intentActivityHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intentActivityHome);
    }

    public static void execCommandActivity(Context context, String command)  {
        execCommandActivity(context,command,null,null,null,null,null);
    }

    public static void execCommandActivity(Context context, String command, String argu1, String argu2, String argu3, String argu4, String argu5)  {
        Intent intentActivity = new Intent(context, DummyActivity.class); // 메인
        intentActivity.putExtra("P_COMMAND", command);
        intentActivity.putExtra("P_ARG1", argu1);
        intentActivity.putExtra("P_ARG2", argu2);
        intentActivity.putExtra("P_ARG3", argu3); // gravity
        intentActivity.putExtra("P_ARG4", argu4); // top
        intentActivity.putExtra("P_ARG5", argu5); // left
        intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intentActivity);
    }
    public static void execCommandService(Context context, String command, String argu1, String argu2) {
        Intent oIntent = new Intent(context, DummyService.class);
        oIntent.putExtra("P_COMMAND", command);
        oIntent.putExtra("P_ARG1", argu1);
        oIntent.putExtra("P_ARG2", argu2);
        context.startService(oIntent);
    }


    public static void execWorkerActivity(Context context, String command, String argu1, String argu2, String argu3, String argu4, String argu5)  {
        Intent intentActivity = new Intent(context, WorkerActivity.class); // 메인
        intentActivity.putExtra("P_COMMAND", command);
        intentActivity.putExtra("P_ARG1", argu1);
        intentActivity.putExtra("P_ARG2", argu2);
        intentActivity.putExtra("P_ARG3", argu3); //
        intentActivity.putExtra("P_ARG4", argu4); //
        intentActivity.putExtra("P_ARG5", argu5); //
        intentActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intentActivity);
    }

    public static boolean isMapCategoryApp() {
        return isMapCategoryApp(WApplication.appInfo.packageName);
    }

    public static boolean isMapCategoryApp(String pkg) {
        boolean rtn = false;
        for (Package cPkg : Package.values()) {
            if ( cPkg.getId().equals(pkg)) {
                rtn = true;
                break;
            }
        }
        return rtn;
    }

    public static boolean isNaviApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (Package pkg : Package.values()) {
                if ( pkg.equals(WApplication.appInfo.packageName) ) return true;
            }

            return WApplication.appInfo.getDetail()!=null && WApplication.appInfo.getDetail().category == 6;
        } else {
            return false;
        }
    }

    public static boolean isSelectedApp() {
        return WApplication.appInfo.icon != null;
    }
    public static boolean isSelectedMusicApp() {
        return WApplication.musicAppInfo.icon != null;
    }

    public static boolean isMusicCategoryApp(String pkg) {
        boolean rtn = false;
        for (PackageMusic cPkg : PackageMusic.values()) {
            if ( cPkg.getId().equals(pkg)) {
                rtn = true;
                break;
            }
        }
        return rtn;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static boolean isForeGroundEvent(UsageEvents.Event event) {
        if(event == null) {
            return false;
        }

        if (Build.VERSION_CODES.O >= 29 ) {
            return event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED;
        }

        return event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND;
    }

    public static boolean isPowerConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }
    public static boolean isPowerConnectedWired(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged != BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }
    public static boolean isPowerConnectedWireless(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

//    public static boolean isWirelessChargingSupported(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//            if (powerManager != null) {
//                WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
//                boolean isWirelessChargingSupported = wakeLock.isLatencySensitive();
//                wakeLock.release();
//                return isWirelessChargingSupported;
//            }
//        }
//        return false;
//    }

//    System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "M") + " Miles\n");
//    System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "K") + " Kilometers\n");
//    System.out.println(distance(32.9697, -96.80322, 29.46786, -98.53506, "N") + " Nautical Miles\n");
    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    private void toast(String v) {
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, v, Toast.LENGTH_SHORT).show();
            }
        }, 0);
    }

    public static ArrayList<MapPoint> getPointsFromString(String _POI) {
        String name = "";
        String id = "";
        String key = "";
        long created = 0l;
        float lat = 0;
        float lon = 0;
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(_POI);
        ArrayList<MapPoint> rtn = new ArrayList<MapPoint>();
        if ( Utils.isEmpty(_POI) ) return rtn;
        JsonArray array = element.getAsJsonArray();
        NumberFormat nf = NumberFormat.getInstance();
        int idx = 0;
        for (JsonElement element2 : array) {
            name = "";
            id = "";
            key = "";
            created = 0;
            lat = 0;
            lon = 0;
            if ( !"null".equals(element2.toString())  ) {
                name = String.valueOf(element2.getAsJsonObject().get("name")).replaceAll("\"", "");
                id = String.valueOf(element2.getAsJsonObject().get("id")).replaceAll("\"", "");
                key = String.valueOf(element2.getAsJsonObject().get("key")).replaceAll("\"", "");
                try {
                    lat = nf.parse(String.valueOf(element2.getAsJsonObject().get("lat")).replaceAll("\"", "")).floatValue();
                    lon = nf.parse(String.valueOf(element2.getAsJsonObject().get("lon")).replaceAll("\"", "")).floatValue();
                } catch (ParseException e) {
//                    e.printStackTrace();
                }
                try {
                    created = nf.parse(String.valueOf(element2.getAsJsonObject().get("created")).replaceAll("\"", "")).longValue();
                } catch (ParseException e) {
//                    e.printStackTrace();
                }
                if ( lat == 0 ) {
                    try {
                        lat = nf.parse(String.valueOf(element2.getAsJsonObject().get("frontLat")).replaceAll("\"", "")).floatValue();
                        lon = nf.parse(String.valueOf(element2.getAsJsonObject().get("frontLon")).replaceAll("\"", "")).floatValue();
                    } catch (ParseException e) {
//                        e.printStackTrace();
                    }
                }
            }
            MapPoint mapPoint = new MapPoint(name,lat,lon);
            mapPoint.setKey(key);
            mapPoint.setId(id);
            mapPoint.setCreated(created);
            rtn.add(mapPoint);
            idx++;
        }
        return rtn;
    }
    public static MapPoint getPointFromString(String _POI) {
        String name = "";
        float lat = 0;
        float lon = 0;
        NaviDestination naviDestination = null;
        try {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(_POI);
            JsonArray array = element.getAsJsonArray();
            NumberFormat nf = NumberFormat.getInstance();
            for (JsonElement element2 : array) {
                if ( !"null".equals(element2.toString())  ) {
                    name = String.valueOf(element2.getAsJsonObject().get("name")).replaceAll("\"", "");
                    try {
                        lat = nf.parse(String.valueOf(element2.getAsJsonObject().get("lat")).replaceAll("\"", "")).floatValue();
                        lon = nf.parse(String.valueOf(element2.getAsJsonObject().get("lon")).replaceAll("\"", "")).floatValue();
                    } catch (ParseException e) {
    //                    e.printStackTrace();
                    }
                    if ( lat == 0 ) {
                        try {
                            lat = nf.parse(String.valueOf(element2.getAsJsonObject().get("frontLat")).replaceAll("\"", "")).floatValue();
                            lon = nf.parse(String.valueOf(element2.getAsJsonObject().get("frontLon")).replaceAll("\"", "")).floatValue();
                        } catch (ParseException e) {
                            throw e;
//                            e.printStackTrace();
                        }
                    }
                    if ( element2.getAsJsonObject().get("destination") != null ) {
                        naviDestination = NaviDestination.valueOf(element2.getAsJsonObject().get("destination").toString().replaceAll("\"", ""));
                    }
                }
                break;
            }

        } catch (Exception ex) {
            return null;
        }

        return new MapPoint(naviDestination,name,lat,lon);
    }

    public static boolean isGeofence() {
        String _start = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_GEOFENCE, "0"); // 시작
        return  _start.equals("1")?true:false;
    }

    public static void setGeofence(boolean v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_GEOFENCE, v?"1":"0");
    }

    public static void setGeofenceRadiusInMeters(String value) {
        float v = Float.valueOf(value);
        GeofenceConstants.GEOFENCE_RADIUS_IN_METERS = v;
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_GEOFENCE_RADIUS_IN_METERS, String.valueOf(v));
    }

    public static float getGeofenceRadiusInMeters() {
        return WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_GEOFENCE_RADIUS_IN_METERS, GeofenceConstants.GEOFENCE_RADIUS_IN_METERS);
    }

    public static void setOverlayWaitTime(String value) {
        int v = Integer.parseInt(value);
        Const.NAVI_OVERLAY_COUNTDOWN_TIME = v*1000;
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_OVERLAY_WAIT_TIME, String.valueOf(v));
    }

    public static float getOverlayWaitTime() {
        return Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_OVERLAY_WAIT_TIME,String.valueOf(Const.NAVI_OVERLAY_COUNTDOWN_TIME/1000))).intValue();
    }

    public static void setRenaviPreventTime(String value) {
        int v = Integer.parseInt(value);
        Const.NAVI_RENAVI_PREVENT_TIME = v*1000;
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_RENAVI_PREVENT_TIME, String.valueOf(v));
    }

    public static long getRenaviPreventTime() {
        return Long.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RENAVI_PREVENT_TIME,String.valueOf(Const.NAVI_RENAVI_PREVENT_TIME/1000))).intValue();
    }

    public static void setDestinationExecutionHoldTime(String value) {
        int v = Integer.parseInt(value);
        Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME = v;
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_NAVI_DESTINATION_EXECUTION_HOLD_TIME, String.valueOf(v));
    }

    public static long getDestinationExecutionHoldTime() {
        return Float.valueOf(WApplication.mInstance.getSharedPreferences( Const.PREF_KEY_NAVI_DESTINATION_EXECUTION_HOLD_TIME,String.valueOf(Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME))).intValue();
    }

    public static long getPreventWaitOverlayTime() {
//        long v  = (Const.NAVI_RENAVI_PREVENT_TIME<3000?3000:Const.NAVI_RENAVI_PREVENT_TIME);
        long v  = Const.NAVI_RENAVI_PREVENT_TIME;
        return v;
    }

    public static boolean isRunSpeed() {
        String _start = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RUN_SPEED, "0"); // 시작
        return  _start.equals("1")?true:false;
    }

    public static void setRunSpeed(boolean v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_RUN_SPEED, v?"1":"0");
    }

    public static void setRunSpeedLimitTime(String value) {
        int v = Integer.parseInt(value);
        Const.RUN_STATUS_SCREEN_TIME_FOR_START = v*1000;
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_RUN_SPEED_LIMIT_TIME, String.valueOf(v));
    }

    public static long getRunSpeedLimitTime() {
        return Long.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RUN_SPEED_LIMIT_TIME,String.valueOf(Const.RUN_SPEED_LIMIT_TIME))).intValue();
    }
    public static void setRunSpeedLimit(String value) {
        int v = Integer.parseInt(value);
        Const.RUN_STATUS_SCREEN_TIME_FOR_START = v;
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_RUN_SPEED_LIMIT, String.valueOf(v));
    }

    public static float getRunSpeedLimit() {
        return Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RUN_SPEED_LIMIT,String.valueOf(Const.RUN_SPEED_LIMIT))).intValue();
    }

    public static MapMode getMapMode() {
        String naviMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_MAP_MODE, MapMode.TMAP.getId()); // 티맵
        return MapMode.value(naviMode);
    }

    public static void setMapMode(MapMode v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_MAP_MODE, v.getId());
    }

//////////////////////////////////////
    public static void startConnectService(Context context, String notificationDetails, NaviStatus naviStatus, boolean bootStartNavi) {

        //TODO softm : sap : jobscheduler
        if ( false ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent in = new Intent(context, SAPService.class);
                context.startForegroundService(in);
            } else {
                Intent in = new Intent(context, SAPService.class);
                context.startService(in);
            }
        } else if ( WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.NOTIFICATION_SERVICE) ) {
            ServiceUtil.init(context).startService(notificationDetails,naviStatus,bootStartNavi);
        } else if ( WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.ACCESSIBILITY_SERVICE) ) {
// TODO SOFTM : 접근성서비스
//            Utils.toast(context,"접근성서비스");
        } else if ( WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.RESTART_SEARVICE) ) {
            if (RealService.serviceIntent==null) {
                RealService.serviceIntent = new Intent(context, PowerConnectRealService.class);
                context.startService(MainActivity.serviceIntent);
            } else {
                RealService.serviceIntent = PowerConnectRealService.serviceIntent;//getInstance().getApplication();
                Toast.makeText(context, "already", Toast.LENGTH_LONG).show();
            }
        } else if ( WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.RESTART_SEARVICE_TEST) ) {
    		if (RealService.serviceIntent==null) {
    			MainActivity.serviceIntent = new Intent(context, RealService.class);
    			context.startService(MainActivity.serviceIntent);
    		} else {
                MainActivity.serviceIntent = RealService.serviceIntent;//getInstance().getApplication();
                Toast.makeText(context, "already", Toast.LENGTH_LONG).show();
    		}

        }
    }

    public static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        // API 레벨 20 이하에서는 isScreenOn()을 사용
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return powerManager.isScreenOn();
        } else {
            // API 레벨 20 이상에서는 isInteractive()를 사용
            return powerManager.isInteractive();
        }
    }

    public static boolean isLockScreen() {
        KeyguardManager keyguardManager = (KeyguardManager) WApplication.mInstance.getSystemService(WApplication.mInstance.KEYGUARD_SERVICE);
//        return keyguardManager.isKeyguardLocked();
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    public static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        if ( Connectivity.isConnected(context) ) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !Utils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID().replaceAll("\"","");
            }
//        }
        L.d(TAG,"getCurrentSsid", ssid);
        return ssid;
    }

    public static String getCurrentBssid(Context context) {
        String bssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if ( Connectivity.isConnected(context) ) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !Utils.isEmpty(connectionInfo.getBSSID())) {
                bssid = connectionInfo.getBSSID();
            }
        }
        return bssid;
    }

    /**
     * Process가 실행중인지 여부 확인.
     * @param context, packageName
     * @return true/false
     */
    public static boolean isRunningProcess(Context context, String packageName) {

        boolean isRunning = false;
        ActivityManager actMng = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = actMng.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo rap : list)
        {
            if(rap.processName.equals(packageName))
            {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }


    public static String getCurrentAddress( Context ctx,double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
//			Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
//			Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }
        if (addresses == null || addresses.size() == 0) {
//			Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString();
    }

    public static boolean isInKorea(Context ctx) {
        double latitude;
        double longitude;
        GpsTracker gpsTracker = new GpsTracker(ctx);
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();
        return isInKorea(latitude,longitude);
    }

    public static boolean isInKorea(double latitude, double longitude) {
        return latitude >= 33.0 && latitude <= 39.6 && longitude >= 124.5 && longitude <= 131.0;
    }

    /**
     * isStart == true 일 경우 ConnectService 실행.
     * @param ctx
     * @return
     */
    public static boolean startService(Context ctx) {
        /** 티맵자동실행 로직 */
//        if ( isStart() ) {
//            if ( WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.NOTIFICATION_SERVICE) ) {
//                ServiceUtil.init(ctx).startService(null,null,false);
                WorkerUtil.ifStopStartCheckService(ctx);
//                return true;
//            } else if ( WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.ACCESSIBILITY_SERVICE) ) {
//                AcsUtil ascUtil = new AcsUtil(ctx);
//                ascUtil.registerReceiver();
//                return true;
//            }
//        }
        return false;
    }

    public static boolean stopService(Context ctx) {
        /** 티맵자동실행 로직 */
////        if ( !isStart() ) {
//            if ( WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.NOTIFICATION_SERVICE) ) {
//                ServiceUtil.stop();
//                return true;
//            } else if ( WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.ACCESSIBILITY_SERVICE) ) {
//                AcsUtil ascUtil = new AcsUtil(ctx);
//                ascUtil.unregisterReceiver();
//                return true;
//            }
////        }
        return false;
    }

}

