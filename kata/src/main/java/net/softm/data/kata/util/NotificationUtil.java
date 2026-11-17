package net.softm.data.kata.util;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import net.softm.data.kata.DummyActivity;
import net.softm.data.kata.ExecActivity;
import net.softm.data.kata.MainActivity;
import net.softm.data.kata.OverlayObserverViewService;
import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.common.NaviStatus;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.god.dto.PrayClass;

public class NotificationUtil {
    private static final String TAG = "NotificationUtil";
    private Context mContext;

    public static int DEFAULT_NOTI_ID = 1234;

    private static NotificationUtil instance;
    public NotificationUtil(Context ctx)
    {
        mContext = ctx;
    }

    public static String restoreNotificationDetails= null;
    public static NaviStatus restoreNaviStatus = NaviStatus.NONE;

    public static final String CHANNEL_ID1 = "channel_01";
    public static final String CHANNEL_ID2 = "channel_02";
    public static final String CHANNEL_ID3 = "channel_03";

    public static NotificationUtil init(Context ctx) {
        instance = new NotificationUtil(ctx);
        instance.mContext = ctx;
        return instance;
    }

    private RemoteViews getNotificationLayout() {
        return getNotificationLayout(false);
    }

    private RemoteViews getNotificationLayout(boolean big) {
        RemoteViews notificationLayout = new RemoteViews(mContext.getPackageName(), big?R.layout.noti_big:R.layout.noti);
/*
		Intent intentSeviceHome = new Intent(this, OverlayViewServiceNew.class);
		intentSeviceHome.putExtra("P_NAVI_DESTINATION", NaviDestination.HOME);
		intentSeviceHome.putExtra("P_IMMEDIATE", true);
		intentSeviceHome.putExtra("P_CLOSE_POPUP", true);
		intentSeviceHome.putExtra("P_NAVI_AUTO_START", true);
		intentSeviceHome.putExtra("P_BOOT", false);
		PendingIntent pendingIntentHome = PendingIntent.getService(this, 1, intentSeviceHome,PendingIntent.FLAG_UPDATE_CURRENT);
		notificationLayout.setOnClickPendingIntent(R.id.btnHome, pendingIntentHome);

		Intent intentSeviceCompany = new Intent(this, OverlayViewServiceNew.class);
		intentSeviceCompany.putExtra("P_NAVI_DESTINATION", NaviDestination.COMPANY);
		intentSeviceCompany.putExtra("P_IMMEDIATE", true);
		intentSeviceCompany.putExtra("P_CLOSE_POPUP", true);
		intentSeviceCompany.putExtra("P_NAVI_AUTO_START", true);
		intentSeviceCompany.putExtra("P_BOOT", false);
		PendingIntent pendingIntentCompany = PendingIntent.getService(this, 2, intentSeviceCompany,PendingIntent.FLAG_UPDATE_CURRENT);
		notificationLayout.setOnClickPendingIntent(R.id.btnCompany, pendingIntentCompany);

		Intent intentSeviceDrive = new Intent(this, OverlayViewServiceNew.class);
		intentSeviceDrive.putExtra("P_NAVI_DESTINATION", NaviDestination.DRIVE);
		intentSeviceDrive.putExtra("P_IMMEDIATE", true);
		intentSeviceDrive.putExtra("P_CLOSE_POPUP", true);
		intentSeviceDrive.putExtra("P_NAVI_AUTO_START", true);
		intentSeviceDrive.putExtra("P_BOOT", false);
		PendingIntent pendingIntentDrive = PendingIntent.getService(this, 3, intentSeviceDrive,PendingIntent.FLAG_UPDATE_CURRENT);
		notificationLayout.setOnClickPendingIntent(R.id.btnStartNavi, pendingIntentDrive);
*/

        if ( MapUtil.isUsingRegHome() && MapUtil.getPoiHome() == null ) {
            Intent intentActivityHome = new Intent(mContext, MainActivity.class); // 메인
            intentActivityHome.putExtra("P_FOWARD_PAGE", WUrl.ReqId.WEB_REG_POI.name());
            intentActivityHome.putExtra("P_TIDX",0);
            PendingIntent pendingIntentCompany = PendingIntent.getActivity(mContext, 1, intentActivityHome,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notificationLayout.setOnClickPendingIntent(R.id.btnCompany, pendingIntentCompany);
        } else {
            Intent intentActivityHome = new Intent(mContext, ExecActivity.class); // 메인
            intentActivityHome.putExtra("P_START_NAVI", "YES");
            intentActivityHome.putExtra("P_NAVI_DESTINATION", NaviDestination.HOME);
//		intentActivityHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		PendingIntent pendingIntentHome = PendingIntent.getActivity(this, 1, intentActivityHome,PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pendingIntentHome = PendingIntent.getActivity(mContext, 1, intentActivityHome,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notificationLayout.setOnClickPendingIntent(R.id.btnHome, pendingIntentHome);
        }

        if ( MapUtil.isUsingRegHome() && MapUtil.getPoiCompany() == null ) {
            Intent intentActivityCompany = new Intent(mContext,MainActivity.class); // 메인
            intentActivityCompany.putExtra("P_FOWARD_PAGE",WUrl.ReqId.WEB_REG_POI.name());
            intentActivityCompany.putExtra("P_TIDX",1);
            PendingIntent pendingIntentCompany = PendingIntent.getActivity(mContext, 2, intentActivityCompany,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notificationLayout.setOnClickPendingIntent(R.id.btnCompany, pendingIntentCompany);
//			notificationLayout.setImageViewResource(R.id.btnCompany, R.drawable.button_gray);

        } else {
            Intent intentActivityCompany = new Intent(mContext,ExecActivity.class); // 메인
            intentActivityCompany.putExtra("P_START_NAVI", "YES");
            intentActivityCompany.putExtra("P_NAVI_DESTINATION", NaviDestination.COMPANY);
//		intentActivityCompany.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntentCompany = PendingIntent.getActivity(mContext, 2, intentActivityCompany,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notificationLayout.setOnClickPendingIntent(R.id.btnCompany, pendingIntentCompany);
        }

        Intent intentActivityDrive = new Intent(mContext,ExecActivity.class); // 메인
        intentActivityDrive.putExtra("P_START_NAVI", "YES");
        intentActivityDrive.putExtra("P_NAVI_DESTINATION", NaviDestination.DRIVE);
//		intentActivityDrive.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentDrive = PendingIntent.getActivity(mContext, 3, intentActivityDrive,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.btnStartNavi, pendingIntentDrive);

        Intent intentActivityMain = new Intent(mContext,MainActivity.class); // 메인
        intentActivityMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String grantStr = "";
        if ( !Utils.hasPermission(mContext.getApplicationContext()) || !PermissionUtil.isAccessibilityServiceRunning(mContext.getApplicationContext()) ) {
            if ( Utils.isNotEmpty(AppUtil.getCheckGrantInfo()) ) {
                for (String info : AppUtil.getCheckGrantInfo()) {
                    if (info.equals("notification_access") && !PermissionUtil.hasNotificationAccessPermission(mContext)) {
                        grantStr = mContext.getString(R.string.msg_activate_notification_access_permission);
                        break;
                    } else if (info.equals("ignore_battery") && !PermissionUtil.hasIgnoreBatteryPermissions()) {
                        grantStr = mContext.getString(R.string.msg_activate_ignore_battery_permission);
                        break;
                    } else if (info.equals("accessibility") && ( !PermissionUtil.hasAccessibilityPermissions() || !PermissionUtil.isAccessibilityServiceRunning(mContext) ) ) {
//                        grantStr = mContext.getString(R.string.msg_activate_permission);
//                        grantStr = mContext.getString(R.string.msg_activate_accessibility_permission2);
//                        grantStr = mContext.getString(R.string.msg_activate_accessibility_permission);
                        grantStr = mContext.getString(R.string.msg_activate_accessibility_permission4);
                        intentActivityMain.putExtra("P_FOWARD_PAGE",WUrl.ReqId.WEB_ONOFF.name());
                        break;
                    } else if (info.equals("location") && !PermissionUtil.isLocationPermission(mContext)) {
                        grantStr = mContext.getString(R.string.msg_activate_location_permission);
                        break;
                    }
                }
            } else {
            }
            if ( Utils.isNotEmpty(grantStr) ) {
                notificationLayout.setViewVisibility(R.id.lnPermission, View.VISIBLE);
                notificationLayout.setTextViewText(R.id.tvPermission,Utils.fromHtml(grantStr));
            } else {
                notificationLayout.setViewVisibility(R.id.lnPermission, View.GONE);
            }
        } else {
            notificationLayout.setViewVisibility(R.id.lnPermission, View.GONE);
        }

        if ( big ) {
            notificationLayout.setTextColor(R.id.tvHome, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
            notificationLayout.setTextColor(R.id.tvCompany, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
            notificationLayout.setTextColor(R.id.tvStartNavi, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
            notificationLayout.setTextColor(R.id.tvMain, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
            notificationLayout.setTextColor(R.id.tvSearch, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
            notificationLayout.setTextColor(R.id.tvSetting, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
        }
        PendingIntent pendingIntentMain = PendingIntent.getActivity(mContext, 1, intentActivityMain,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//		notificationLayout.setOnClickPendingIntent(R.id.btnMain, pendingIntentMain);
        notificationLayout.setOnClickPendingIntent(R.id.lnPermission, pendingIntentMain);
//		notificationLayout.setOnClickPendingIntent(R.id.lnMain, pendingIntentMain);

        Intent intentOverlay = new Intent(mContext, DummyActivity.class); // 메인
        intentOverlay.putExtra("P_COMMAND", "openOverlayViewShowOnly");
        intentOverlay.putExtra("P_ARG1", String.valueOf(true));
        intentOverlay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentOverlay = PendingIntent.getActivity(mContext, 5, intentOverlay,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.btnMain, pendingIntentOverlay);

        Intent intentActivitySearch = new Intent(mContext,MainActivity.class); // 메인
        intentActivitySearch.putExtra("P_FOWARD_PAGE",WUrl.ReqId.WEB_SEARCH.name());
        intentActivitySearch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentSearch = PendingIntent.getActivity(mContext, 4, intentActivitySearch,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.btnSearch, pendingIntentSearch);

        Intent intentActivitySetting = new Intent(mContext,MainActivity.class); // 메인
        intentActivitySetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentSetting = PendingIntent.getActivity(mContext, 8, intentActivitySetting,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.btnSetting, pendingIntentSetting);

//        Intent intentActivitySetting = new Intent(mContext,MainActivity.class); // 메인
//        intentActivitySetting.putExtra("P_FOWARD_PAGE","SETTING");
//        intentActivitySetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntentSetting = PendingIntent.getActivity(mContext, 4, intentActivitySetting,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//        notificationLayout.setOnClickPendingIntent(R.id.btnSetting, pendingIntentSetting);

        Intent intentAppEnable = new Intent(mContext, DummyActivity.class); // AppEnable
        intentAppEnable.putExtra("P_COMMAND", "AppEnable");
        intentAppEnable.putExtra("P_ARG1", String.valueOf(!MapUtil.isAppEnable()));
        intentAppEnable.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentAppEnable = PendingIntent.getActivity(mContext, 7, intentAppEnable,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        /** TODO SOFTM : Error inflating class android.widget.Switch 발생으로 제거함
         * 추후 : 이미지를 이용해서 변경예정
        */
            notificationLayout.setOnClickPendingIntent(R.id.btnAppEnable, pendingIntentAppEnable);
            notificationLayout.setOnClickPendingIntent(R.id.btnAppDisable, pendingIntentAppEnable);
    //        notificationLayout.setBoolean(R.id.btnAppEnable, "setChecked", MapUtil.isAppEnable());
            if (MapUtil.isAppEnable()) {
                notificationLayout.setViewVisibility(R.id.btnAppEnable, View.VISIBLE);
                notificationLayout.setViewVisibility(R.id.btnAppDisable, View.GONE);
            } else {
                notificationLayout.setViewVisibility(R.id.btnAppDisable, View.VISIBLE);
                notificationLayout.setViewVisibility(R.id.btnAppEnable, View.GONE);
            }
        return notificationLayout;
    }

    public void sendNotification(String notificationDetails, NaviStatus naviStatus, int notiId) {
        getNotification(notificationDetails, naviStatus,true,notiId);
    }
    public Notification getNotification(String notificationDetails, NaviStatus naviStatus, int notiId) {
        return getNotification(notificationDetails, naviStatus,true,notiId);
    }

    public Notification getNotification(String notificationDetails, NaviStatus naviStatus) {
        return getNotification(notificationDetails, naviStatus, false,-1);
    }

    @SuppressLint("WrongConstant")
    public Notification getNotification(String notificationDetails, NaviStatus naviStatus,boolean send,int notiId) {
        restoreNotificationDetails= notificationDetails;
        restoreNaviStatus = naviStatus;

        String contentTitle="";
        if( NaviStatus.NONE.equals(naviStatus) ) {
            contentTitle = mContext.getString(R.string.title_noti_none);
        } else if(NaviStatus.START.equals(naviStatus) ) {
            contentTitle = mContext.getString(R.string.title_noti_start);
        } else if( NaviStatus.REFENCE.equals(naviStatus) ) {
            contentTitle = mContext.getString(R.string.title_noti_refence);
        } else if( NaviStatus.END.equals(naviStatus) ) {
            contentTitle = mContext.getString(R.string.title_noti_end);
        } else if( NaviStatus.RUNNING.equals(naviStatus) ) {
            contentTitle = mContext.getString(R.string.title_noti_runnig);
        }

        NotificationManager notiManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = mContext.getString(R.string.app_name);
/*TODO softm Remove : Notification에 Service걸린 상태에서 채널 삭제시 SecurityException 발생해서 제거함
//			try {
//				notiManager.deleteNotificationChannel(CHANNEL_ID1);
//				notiManager.deleteNotificationChannel(CHANNEL_ID2);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
*/
            if ( notiManager.getNotificationChannel(CHANNEL_ID1) == null ) {
                //			NotificationChannel mChannel =new NotificationChannel(CHANNEL_ID1, name+"-일반", NotificationManager.IMPORTANCE_NONE);
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID1, name + "-일반", NotificationManager.IMPORTANCE_DEFAULT); //IMPORTANCE_DEFAULT
                mChannel.setSound(null, null);
                mChannel.setShowBadge(false);
                notiManager.createNotificationChannel(mChannel);
                NotificationChannel mChanne2 = new NotificationChannel(CHANNEL_ID2, name + "-대기", NotificationManager.IMPORTANCE_LOW);
                mChanne2.setSound(null, null);
                mChanne2.setShowBadge(false);
                notiManager.createNotificationChannel(mChanne2);
                NotificationChannel mChanne3 = new NotificationChannel(CHANNEL_ID3, name + "-알림", NotificationManager.IMPORTANCE_LOW);
                mChanne3.setSound(null, null);
                mChanne3.setShowBadge(false);
                notiManager.createNotificationChannel(mChanne3);
            }
        }

//		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(mContext,!send?CHANNEL_ID2:CHANNEL_ID1); // point
        } else {
            builder = new Notification.Builder(mContext);
        }

        Intent intentActivityMain = new Intent(mContext,MainActivity.class); // 메인
        intentActivityMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(mContext, 5, intentActivityMain,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntentMain);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setStyle(new Notification.DecoratedCustomViewStyle())
                    .setCustomContentView(getNotificationLayout())
                    .setCustomBigContentView(getNotificationLayout(true))
            ;
        } else {

        }

        builder.setContentTitle(contentTitle);
        builder.setContentText(notificationDetails);
        builder.setSmallIcon(R.drawable.logo);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
//		builder.setPriority(Notification.PRIORITY_MIN);
        if ( !send ) {
            builder.setPriority(Notification.PRIORITY_MIN);
        } else {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        builder.setPriority(Notification.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if( NaviStatus.NONE.equals(naviStatus) ) {
                builder.setChannelId(CHANNEL_ID2); // Channel ID - 2
            } else if( NaviStatus.START.equals(naviStatus) ) {
                builder.setChannelId(CHANNEL_ID1); // Channel ID - 1
            } else if( NaviStatus.REFENCE.equals(naviStatus) ) {
                builder.setChannelId(CHANNEL_ID1); // Channel ID - 1
            } else if( NaviStatus.END.equals(naviStatus) ) {
                builder.setChannelId(CHANNEL_ID1); // Channel ID - 1
            } else if( NaviStatus.RUNNING.equals(naviStatus) ) {
//				builder.setChannelId(CHANNEL_ID2); // Channel ID - 2
                builder.setChannelId(CHANNEL_ID1); // Channel ID - 2
            }
        }

        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            notification.contentView = getNotificationLayout(true);
            notification.bigContentView = getNotificationLayout(true);
        }

        if ( send ){
            notiManager.notify(notiId, notification);
        }
        return notification;
    }

    public Notification sendNoti(String notificationDetails, NaviStatus naviStatus) {

        NotificationManager nm = (NotificationManager) WApplication.mInstance.getSystemService(Context.NOTIFICATION_SERVICE);

        if ( NaviStatus.NONE.equals(naviStatus) ) {
            notificationDetails = restoreNotificationDetails;
            naviStatus          = restoreNaviStatus;
        }

        notificationDetails = Utils.isEmpty(notificationDetails)?mContext.getString(R.string.title_noti_runnig_title):notificationDetails;
        naviStatus = naviStatus == null || NaviStatus.NONE.equals(naviStatus)?NaviStatus.RUNNING:naviStatus;
        Notification notification = getNotification(notificationDetails, naviStatus ); // New
        return notification;
    }

    @SuppressLint("WrongConstant")
    public Notification getObserverNotification(String notificationDetails) {
        return getObserverNotification(notificationDetails,"00:00","-","-","-");
    }
    @SuppressLint("WrongConstant")
    public Notification getObserverNotification(String notificationDetails,String infoRunSpeedLimitTime,String infoRunSpeedLimit, String runSpeedLimitTime,String runSpeedLimit)  {
        restoreNotificationDetails= notificationDetails;

        String contentTitle="";
            contentTitle = mContext.getString(R.string.title_noti_none);

        NotificationManager notiManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = mContext.getString(R.string.app_name);
/*TODO softm Remove : Notification에 Service걸린 상태에서 채널 삭제시 SecurityException 발생해서 제거함
//			try {
//				notiManager.deleteNotificationChannel(CHANNEL_ID1);
//				notiManager.deleteNotificationChannel(CHANNEL_ID2);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
*/
            if ( notiManager.getNotificationChannel(CHANNEL_ID1) == null ) {
                //			NotificationChannel mChannel =new NotificationChannel(CHANNEL_ID1, name+"-일반", NotificationManager.IMPORTANCE_NONE);
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID1, name + "-일반", NotificationManager.IMPORTANCE_DEFAULT); //IMPORTANCE_DEFAULT
                mChannel.setSound(null, null);
                mChannel.setShowBadge(false);
                notiManager.createNotificationChannel(mChannel);
                NotificationChannel mChanne2 = new NotificationChannel(CHANNEL_ID2, name + "-대기", NotificationManager.IMPORTANCE_LOW);
                mChanne2.setSound(null, null);
                mChanne2.setShowBadge(false);
                notiManager.createNotificationChannel(mChanne2);
            }
        }

//		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(mContext,CHANNEL_ID1); // point
        } else {
            builder = new Notification.Builder(mContext);
        }

        // 서비스 종료용 PendingIntent 생성
        Intent stopServiceIntent = new Intent(mContext, OverlayObserverViewService.class);
        stopServiceIntent.setAction("STOP_SERVICE"); // 액션 설정
        PendingIntent stopServicePendingIntent = PendingIntent.getService(
                mContext,
                1,
                stopServiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, // 버튼 아이콘
                mContext.getString(R.string.stops_touch), // 버튼 텍스트
                stopServicePendingIntent);// 버튼 동작

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setStyle(new Notification.DecoratedCustomViewStyle())
//            builder.setStyle(new Notification.BigTextStyle().bigText("Notification이 확장되면 이 메시지와 추가 버튼이 표시됩니다."))
                    .setCustomContentView(getObserverNotificationLayout(false, infoRunSpeedLimitTime, infoRunSpeedLimit, runSpeedLimitTime, runSpeedLimit))
                    .setCustomBigContentView(getObserverNotificationLayout(true,infoRunSpeedLimitTime, infoRunSpeedLimit, runSpeedLimitTime, runSpeedLimit))
            ;
        } else {

        }

        builder.setContentTitle(contentTitle);
        builder.setContentText(notificationDetails);
        builder.setSmallIcon(R.drawable.logo);
        builder.setAutoCancel(true);
        builder.setOngoing(true);
//		builder.setPriority(Notification.PRIORITY_MIN);
//        builder.setPriority(Notification.PRIORITY_HIGH);

//        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setPriority(Notification.PRIORITY_DEFAULT);
//        builder.setStyle(new Notification.BigTextStyle().bigText("Notification이 확장되면 이 메시지와 추가 버튼이 표시됩니다."));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID1); // Channel ID - 2
        }

        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            notification.contentView = getObserverNotificationLayout(false, infoRunSpeedLimitTime, infoRunSpeedLimit, runSpeedLimitTime, runSpeedLimit);
            notification.bigContentView = getObserverNotificationLayout(true, infoRunSpeedLimitTime, infoRunSpeedLimit, runSpeedLimitTime, runSpeedLimit);
        }

//        notiManager.notify(1234, notification);
        return notification;
    }


    static RemoteViews notificationLayout = null;
    public static RemoteViews getObserverNotificationRemoteView() {
        return notificationLayout;
    }

    private RemoteViews getObserverNotificationLayout(boolean big,String infoRunSpeedLimitTime,String infoRunSpeedLimit,String runSpeedLimitTime,String runSpeedLimit ) {
        notificationLayout = new RemoteViews(mContext.getPackageName(), big?R.layout.noti_observer:R.layout.noti_observer);

        notificationLayout.setTextViewText(R.id.txtRunSpeedLimitTime,runSpeedLimitTime);
        notificationLayout.setTextViewText(R.id.txtRunSpeedLimit,runSpeedLimit);

        notificationLayout.setTextViewText(R.id.txtInfoRunSpeedLimitTime,infoRunSpeedLimitTime);
        notificationLayout.setTextViewText(R.id.txtInfoRunSpeedLimit,infoRunSpeedLimit);

//        Intent intentActivityMain = new Intent(mContext,MainActivity.class); // 메인
//        intentActivityMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntentMain = PendingIntent.getActivity(mContext, 8, intentActivityMain,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//		notificationLayout.setOnClickPendingIntent(R.id.btnMain, pendingIntentMain);
//        notificationLayout.setOnClickPendingIntent(R.id.lnPermission, pendingIntentMain);
//		notificationLayout.setOnClickPendingIntent(R.id.lnMain, pendingIntentMain);

        notificationLayout.setTextColor(R.id.tvTitle1, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
        notificationLayout.setTextColor(R.id.tvTitle2, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
        notificationLayout.setTextColor(R.id.txtPths1, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
        notificationLayout.setTextColor(R.id.txtPths2, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
        notificationLayout.setTextColor(R.id.txtPths3, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
        notificationLayout.setTextColor(R.id.txtPths4, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
        notificationLayout.setTextColor(R.id.txtMin, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
        notificationLayout.setTextColor(R.id.txtKmh, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
        notificationLayout.setTextColor(R.id.txtRunSpeedLimitTime, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));
        notificationLayout.setTextColor(R.id.txtRunSpeedLimit, Color.parseColor(Utils.isDarkMode(mContext)?Utils.ColorDark:Utils.ColorBasic));

//        Intent intentActivityLaunch = new Intent(mContext, DummyActivity.class); // 메인
//        intentActivityLaunch.putExtra("P_COMMAND", "runSpeedStart");
//        intentActivityLaunch.putExtra("P_ARG1", String.valueOf(true));
//        intentActivityLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntentLaunch = PendingIntent.getActivity(mContext, 1, intentActivityLaunch,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//        notificationLayout.setOnClickPendingIntent(R.id.lnOverlay, pendingIntentLaunch);

        Intent intentActivityRunSpeed = new Intent(mContext, DummyActivity.class); // 메인
        intentActivityRunSpeed.putExtra("P_COMMAND", "runSpeedStart");
        intentActivityRunSpeed.putExtra("P_ARG1", String.valueOf(true));
        intentActivityRunSpeed.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentRunSpeed = PendingIntent.getActivity(mContext, 1, intentActivityRunSpeed,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.btnStart, pendingIntentRunSpeed);

        Intent intentActivityMain = new Intent(mContext, DummyActivity.class); // 메인
        intentActivityMain.putExtra("P_COMMAND", "startOverlayObserverViewService");
        intentActivityMain.putExtra("P_ARG1", String.valueOf(true));
        intentActivityMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(mContext, 2, intentActivityMain,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.lnOverlay, pendingIntentMain);

        Intent intentOverlay = new Intent(mContext, DummyActivity.class); // 메인
        intentOverlay.putExtra("P_COMMAND", "stopOverlayObserverViewService");
        intentOverlay.putExtra("P_ARG1", String.valueOf(true));
        intentOverlay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentOverlay = PendingIntent.getActivity(mContext, 3, intentOverlay,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.btnCancel, pendingIntentOverlay);


//        Intent stopServiceIntent = new Intent(mContext, OverlayObserverViewService.class);
//        stopServiceIntent.setAction("STOP_SERVICE"); // 액션 설정
//        PendingIntent stopServicePendingIntent = PendingIntent.getService(
//                mContext,
//                4,
//                stopServiceIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//        notificationLayout.setOnClickPendingIntent(R.id.btnCancel, stopServicePendingIntent);



        // RemoteViews에 btnClose 클릭 이벤트 설정

        return notificationLayout;
    }

    private static int notificationId = 300;
    @SuppressLint("WrongConstant")
    public Notification send(String contentTitle, String message, boolean send) {
        return send(contentTitle,message,true, ++notificationId);
    }

    public Notification send(String contentTitle, String message, boolean send,int notiId) {
        NotificationManager notiManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(mContext,CHANNEL_ID3);
        } else {
            builder = new Notification.Builder(mContext);
        }

        Intent intentActivityMain = new Intent(mContext,MainActivity.class); // 메인
        intentActivityMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(mContext, 5, intentActivityMain,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntentMain);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setStyle(new Notification.DecoratedCustomViewStyle())
//                    .setCustomContentView(getNotificationLayout())
//                    .setCustomBigContentView(getNotificationLayout(true))
            ;
        }

        builder.setContentTitle(contentTitle);
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.logo);
//        builder.setAutoCancel(false);
//        builder.setOngoing(true);
//		builder.setPriority(Notification.PRIORITY_MIN);
        if ( !send ) {
            builder.setPriority(Notification.PRIORITY_MIN);
        } else {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        builder.setPriority(Notification.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID1); // Channel ID - 1
        }

        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//            notification.contentView = getNotificationLayout(true);
//            notification.bigContentView = getNotificationLayout(true);
        }

        if ( send ) {
            notiManager.notify(notiId, notification);
        }
        return notification;
    }
   public static void cancel(Context context, int notificationId) {
       NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
       notificationManager.cancel(notificationId);
   }

    public Notification sendPray(String contentTitle, String message, boolean send,int notiId, PrayClass prayClass) {
        NotificationManager notiManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(mContext,CHANNEL_ID3);
        } else {
            builder = new Notification.Builder(mContext);
        }

        Intent intentActivityMain = new Intent(mContext,MainActivity.class); // 메인
        intentActivityMain.putExtra("P_PARTY_DOC_ID", prayClass.getDocId());
        intentActivityMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntentMain = PendingIntent.getActivity(mContext, 500, intentActivityMain,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntentMain);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setStyle(new Notification.DecoratedCustomViewStyle())
//                    .setCustomContentView(getNotificationLayout())
//                    .setCustomBigContentView(getNotificationLayout(true))
            ;
        }

        builder.setContentTitle(contentTitle);
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.logo);
        builder.setAutoCancel(true);
//        builder.setOngoing(true);
//		builder.setPriority(Notification.PRIORITY_MIN);
        if ( !send ) {
            builder.setPriority(Notification.PRIORITY_MIN);
        } else {
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        builder.setPriority(Notification.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID1); // Channel ID - 1
        }

        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//            notification.contentView = getNotificationLayout(true);
//            notification.bigContentView = getNotificationLayout(true);
        }

        if ( send ) {
            notiManager.notify(notiId, notification);
        }
        return notification;
    }
}

