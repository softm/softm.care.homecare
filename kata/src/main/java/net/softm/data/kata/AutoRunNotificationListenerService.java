package net.softm.data.kata;

import android.app.Notification;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.poi_item.TMapPOIItem;

import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.dto.StartStopOnOffVO;
import net.softm.data.kata.kt.Callkt;
import net.softm.data.kata.util.DeviceUtil;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.MapPoint;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.PackageUtil;
import net.softm.data.kata.util.Utils;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AutoRunNotificationListenerService extends NotificationListenerService {
    String TAG = "AutoRunNotificationListenerService";

    public AutoRunNotificationListenerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerConnectReceiver();
        Log.i(TAG, "[softm] onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "[softm] onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterConnectReceiver();
        Log.i(TAG, "[softm] onDestroy()");
    }

    static long checkTime = 0L;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "[softm] onNotificationPosted() - " + sbn.toString());
        Log.i(TAG, "[softm] PackageName:" + sbn.getPackageName());
        Log.i(TAG, "[softm] PostTime:" + sbn.getPostTime()); // notificatin.getChannelId()

        Notification notificatin = sbn.getNotification();
        Bundle extras = notificatin.extras;
        String title = "";
        String text = "";
        String subText = "";
        String infoText = "";
        String summaryText = "";
        try {
            title = extras.getCharSequence(Notification.EXTRA_TITLE,"") == null ? "" : extras.getCharSequence(Notification.EXTRA_TITLE,"").toString();
            text = extras.getCharSequence(Notification.EXTRA_TEXT,"") == null ? "" : extras.getCharSequence(Notification.EXTRA_TEXT,"").toString();
            subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT,"") == null ? "" : extras.getString(Notification.EXTRA_SUB_TEXT,"").toString();
            infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT,"") == null ? "" : extras.getCharSequence(Notification.EXTRA_INFO_TEXT,"").toString();
            summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT,"") == null ? "" : extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT,"").toString();
        } catch ( Exception ex ) { // 텍스트 입력 편집시 발생함 : java.lang.ClassCastException: android.text.SpannableString cannot be cast to java.lang.String
             ex.printStackTrace();
        }

        try {
            Log.i(TAG, "[softm] Title:" + title);
            Log.i(TAG, "[softm] Text:" + text);
            Log.i(TAG, "[softm] Sub Text:" + subText);
    //      if ( MapUtil.isPackageForTmap(sbn.getPackageName()) || MapUtil.getAppPackage().equals(sbn.getPackageName())) {
            if ( MapUtil.getAppPackage().equals(sbn.getPackageName()) ) {
    // 카카오 내비
    //            "안드로이드 오토 실행 중"
    // navi_noti_channel_navi_drive_state_view_0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if ( PackageUtil.isTmap() && notificatin.getChannelId().equals("noti_channel_car") ) return;
                }
                if ( title.contains("안드로이드 오토") || title.toLowerCase().contains("android auto") ) return;

                MapUtil.setNavingForNotiListener();
                MapUtil.setNaving();
                if ( MapUtil.isMapCategoryApp() ) {
             // if ( PackageUtil.isSDKNavi()  ) {
             // if ( PackageUtil.isTmap() || PackageUtil.isKakaoNavi()  ) {
             // if ( MapUtil.isInKorea(WApplication.mInstance) ) {
                    boolean doSetPoiRecent = false;
                    String [] routeInfo = new String[0];
                    if ( PackageUtil.isTmap() ) {
                        if (Utils.isNotEmpty(text))
                            routeInfo = text.split(">");
                    } else if ( PackageUtil.isKakaoNavi() ) {
                        if (Utils.isNotEmpty(text))
                            routeInfo = text.split(":");
//                    } else if ( PackageUtil.isGoogleMap() ) {
//                        if (Utils.isNotEmpty(text))
//                            routeInfo = text.split(":");
//                    } else if ( PackageUtil.isKakaoMap() ) {
//                        if (Utils.isNotEmpty(text))
//                            routeInfo = text.split(":");
                    }

                    MapPoint mapPoint = MapUtil.getPoiRecent();
                    String oldDestName = mapPoint!=null?mapPoint.getName():"";
                    String newDestName = routeInfo.length > 1 ? routeInfo[1].trim() : "";
                    if ( mapPoint != null ) {
                        if ( routeInfo.length > 1 ) {
                            if ( routeInfo.length == 2 && Utils.isNotEmpty(newDestName) && !oldDestName.equals(newDestName) ) {
                                doSetPoiRecent = true;
                            } else {
                                doSetPoiRecent = true;
                            }
                        }
                    } else {
                        if ( Utils.isNotEmpty(newDestName) ) doSetPoiRecent = true;
                    }

                    if ( doSetPoiRecent ) {
                        TMapData tMapData = new TMapData();
                        String[] finalRouteInfo = routeInfo;
                        for (int i=1; i<routeInfo.length; i++) {
                            if ( Utils.isNotEmpty(routeInfo[i]) ) {
                                String finalNewDestName = routeInfo[i].trim();
                                int finalI = i;
                                int finalS = routeInfo.length;
                                new Callkt("onNotificationPosted-getPoiPoint").runIO((param)->{
                                    try {
                                        if ( PackageUtil.isTmap() ||
                                             PackageUtil.isKakaoNavi() && !finalNewDestName.contains(" • ") ) {
                                            ArrayList<TMapPOIItem> poiItems = tMapData.findTitlePOI(finalNewDestName);
                                            if (poiItems.size() > 0) {
                                                TMapPOIItem poi = poiItems.get(0);
                                                TMapPoint tMapPoint = poi.getPOIPoint();
                                                if (tMapPoint.getLatitude() > -1) {
                                                    MapUtil.addPoiRecent(new MapPoint(NaviDestination.NOTI, finalNewDestName, (float) tMapPoint.getLatitude(), (float) tMapPoint.getLongitude()));
                                                    if (finalI == finalS - 1) {
                                                        MapUtil.setPoiRecent(new MapPoint(NaviDestination.NOTI, finalNewDestName, (float) tMapPoint.getLatitude(), (float) tMapPoint.getLongitude()));
                                                        OverlayViewUtil.init(WApplication.mInstance).openOverlayViewShowOnly(true);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (ParserConfigurationException e) {
                                        e.printStackTrace();
                                    } catch (SAXException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                    }
                                    return null;
                                });
                            }
                        }

                    }
                }

                if ( PackageUtil.isNaviToHomeScreen() ) {
                    StartStopOnOffVO ss = MapUtil.getStartStopOnOff();
                    boolean isStart = true;
                    boolean isStop = false;
                    if (
                            (isStart && ss.getHomeScreen().getStart().isUseYn()) ||
                                    (isStop && ss.getHomeScreen().getStop().isUseYn())
                    ) {
                        StartStopOnOffVO.StartUseYnOnOff startStop = isStart ? ss.getHomeScreen().getStart() : ss.getHomeScreen().getStop();
                        startStop = isStart ? ss.getHomeScreen().getStart() : ss.getPlayMusic().getStop();
                        String msgOnOff = startStop.getEnabled() ? this.getString(R.string.label_on) : this.getString(R.string.label_off);
                        DeviceUtil.goHomeScreen(this, true);
                        Utils.toast(this, this.getString(R.string.msg_startstop_onoff, this.getString(R.string.label_home_screen), msgOnOff));
                    }
                }
            }

            if ( sbn.getPackageName().equals(MapUtil.getMusicAppPackage()) ) {
                MapUtil.setMusicPlayTitle(String.format("%s [%s]",title,text));
                OverlayViewUtil.setMusicPlayInfo(title, text, subText, infoText, summaryText);
            }
//            if (MapUtil.isAppInstalled() && !Utils.hasPermission(this) && !this.getApplicationContext().getPackageName().equals(sbn.getPackageName()) ) {
//    //        if (MapUtil.isAppInstalled() && !Utils.hasPermissionForBasic(this) && !this.getApplicationContext().getPackageName().equals(sbn.getPackageName()) ) {
//    //            long oneDayMillis = 1000 * 60 * 60 * 24; // 1일의 밀리초 값
//                Random random = new Random();
//                /* TODO SOFTM : 주기성으로 혹시 앱의 접근성을 야기하지 않는가 해서 조치함 */
//                int randomNumber = random.nextInt(10) + 1; // 1부터 10 사이의 정수 생성
//    //          long oneDayMillis = 1000 * 60 * 60 * 2; // 2시간의 밀리초 값
//                long oneDayMillis = 1000 * 60 * (6*randomNumber) * 2; // 2시간의 밀리초 값
//                long currentTime = System.currentTimeMillis();
//                if (System.currentTimeMillis() - checkTime >= oneDayMillis) {
//                    if ( !PermissionUtil.hasAccessibilityPermissions() || !PermissionUtil.isAccessibilityServiceRunning(this) ) {
//    //                if ( !Utils.hasPermissionForBasic(this) ) {
//    //                    Utils.goPermission(this,"accessibility_reset",null);
//    //                    Utils.goPermission(this,"",null);
//    //                    Fa.i(this,TAG,"onNotificationPosted","!Utils.hasPermission(this)-goPermission");
//                    } else {
//    //                    Utils.goMain(this);
//    //                    Fa.i(this,TAG,"onNotificationPosted","!Utils.hasPermission(this)-goMain");
//                    }
//                    checkTime = System.currentTimeMillis();
//                } else {
//                    L.i(TAG,"onNotificationPosted","!Utils.hasPermission(this)");
//                }
//            }
        } catch ( Exception ex ) { // 텍스트 입력 편집시 발생함 : java.lang.ClassCastException: android.text.SpannableString cannot be cast to java.lang.String
             ex.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "[softm] onNotificationRemoved() - " + sbn.toString());
        Notification notificatin = sbn.getNotification();
        Bundle extras = notificatin.extras;
        String title = "";
        String text = "";
        String subText = "";
        String infoText = "";
        String summaryText = "";
        try {
            title = extras.getCharSequence(Notification.EXTRA_TITLE,"") == null ? "" : extras.getCharSequence(Notification.EXTRA_TITLE,"").toString();
            text = extras.getCharSequence(Notification.EXTRA_TEXT,"") == null ? "" : extras.getCharSequence(Notification.EXTRA_TEXT,"").toString();
            subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT,"") == null ? "" : extras.getCharSequence(Notification.EXTRA_SUB_TEXT,"").toString();
            infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT,"") == null ? "" : extras.getCharSequence(Notification.EXTRA_INFO_TEXT,"").toString();
            summaryText = extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT,"") == null ? "" : extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT,"").toString();
        } catch ( Exception ex ) { // 텍스트 입력 편집시 발생함 : java.lang.ClassCastException: android.text.SpannableString cannot be cast to java.lang.String
             ex.printStackTrace();
        }
        try {
            //      if ( MapUtil.isPackageForTmap(sbn.getPackageName()) || MapUtil.getAppPackage().equals(sbn.getPackageName())) {
            if (MapUtil.getAppPackage().equals(sbn.getPackageName())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (PackageUtil.isTmap() && notificatin.getChannelId().equals("noti_channel_car"))
                        return;
                }
                if (title.contains("안드로이드 오토") || title.toLowerCase().contains("android auto"))
                    return;

                MapUtil.setNotNavingForNotiListener();
                MapUtil.setNotNaving();

                if (Utils.hasPermission(this)) {
                    if (PackageUtil.isNaviToHomeScreen()) {
                        StartStopOnOffVO ss = MapUtil.getStartStopOnOff();
                        boolean isStart = false;
                        boolean isStop = true;
                        if (
                                (isStart && ss.getHomeScreen().getStart().isUseYn()) ||
                                        (isStop && ss.getHomeScreen().getStop().isUseYn())
                        ) {
                            StartStopOnOffVO.StartUseYnOnOff startStop = isStart ? ss.getHomeScreen().getStart() : ss.getHomeScreen().getStop();
                            startStop = isStart ? ss.getHomeScreen().getStart() : ss.getPlayMusic().getStop();
                            String msgOnOff = startStop.getEnabled() ? this.getString(R.string.label_on) : this.getString(R.string.label_off);
                            DeviceUtil.goHomeScreen(this, true);
                            Utils.toast(this, this.getString(R.string.msg_startstop_onoff, this.getString(R.string.label_home_screen), msgOnOff));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    ScreenReceiver screenReceiver = new ScreenReceiver();

    private void registerConnectReceiver() {
        L.d(TAG,"ConnectService","registerConnectReceiver");

        IntentFilter localIntentFilterScreen = new IntentFilter();
        localIntentFilterScreen.addAction(Intent.ACTION_USER_PRESENT);
        localIntentFilterScreen.addAction(Intent.ACTION_USER_UNLOCKED);
//        localIntentFilterScreen.addAction(Intent.ACTION_SCREEN_ON);
//        localIntentFilterScreen.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(this.screenReceiver, localIntentFilterScreen);


        L.d(TAG, "registerConnectReceiver","add Receiver ConnectReceiver");

    }


    private void unregisterConnectReceiver() {
        L.d(TAG,"ConnectService","unregisterConnectReceiver");
        try{
            unregisterReceiver(this.screenReceiver);
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
        L.d(TAG, "unRegisterConnectReceiver");
    }


}
