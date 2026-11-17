package net.softm.data.kata;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
import static net.softm.data.kata.common.Const.APP_UPDATE_DELAY_TIME;
import static net.softm.data.kata.util.MapUtil.isStart;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ParseException;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.adroitandroid.chipcloud.ChipCloud;
import com.adroitandroid.chipcloud.ChipListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.firebase.Timestamp;

import net.softm.data.kata.common.AppEnable;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.MusicActiveLiveData;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.common.OverlayStartMode;
import net.softm.data.kata.common.ParamCallback;
import net.softm.data.kata.common.PermissionType;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.dto.StartStopOnOffVO;
import net.softm.data.kata.dto.base.Voice;
import net.softm.data.kata.god.dto.PrayClass;
import net.softm.data.kata.god.dto.PrayStatus;
import net.softm.data.kata.job.WorkQueue;
import net.softm.data.kata.kt.Callbackkt;
import net.softm.data.kata.kt.Callkt;
import net.softm.data.kata.kt.Debouncer;
import net.softm.data.kata.kt.WorkerExeckt;
import net.softm.data.kata.location.StopLocationListener;
import net.softm.data.kata.location.StopSensorListener;
import net.softm.data.kata.util.AppUtil;
import net.softm.data.kata.util.BluetoothUtil;
import net.softm.data.kata.util.DeviceUtil;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.FaGodWhisper;
import net.softm.data.kata.util.FbGodWhisper;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.MapPoint;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.NotificationUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.PackageUtil;
import net.softm.data.kata.util.PermissionUtil;
import net.softm.data.kata.util.RestUtil;
import net.softm.data.kata.util.Utils;
import net.softm.lib.Util;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;
import butterknife.Optional;
//@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class OverlayViewService extends Service implements View.OnTouchListener, StopLocationListener.StopDetectedCallback, StopSensorListener.StopDetectedCallback {

    String TAG = "OverlayViewService";
    public static final String STOP_ACTION = "STOP_ACTION";
    public static boolean isForeground = false;
    private static OverlayViewService mContext;
    MainActivity mActivity;
    private View onTopView;
    private WindowManager manager;
    private Timer mTimer;

    long startTime = 0;

    TimerTask counterTask;
    Debouncer debouncer;

    boolean P_BOOT = false;
    //    boolean P_REFENCE = false;
    boolean P_CLOSE_POPUP = true; // OVERLAY 닫기
    boolean P_EXPAND_SUB = false; // lnSub 펼쳐지게
    boolean P_NAVI_AUTO_START = false; // 내비 자동실행
    boolean P_IMMEDIATE = false;
    boolean P_HIDE = false;
    NaviDestination P_NAVI_DESTINATION = null;
    OverlayStartMode P_START_MODE = null;
    //    LinearLayout lnChip = null;
    String P_NAME = null;
    String P_MSG = null;
    float P_LAT = 0.0f;
    float P_LON = 0.0f;

    @BindView(R.id.txtCounter)@Nullable
    TextView txtCounter = null;
    @BindView(R.id.imgLogo)@Nullable
    ImageView imgLogo = null;
    @BindView(R.id.lnSpeed)@Nullable
    LinearLayout lnSpeed = null;
    @BindView(R.id.txtSpeed)@Nullable
    TextView txtSpeed = null;
    @BindView(R.id.txtSpeed2)@Nullable
    TextView txtSpeed2 = null;

    @BindView(R.id.lnOverlay)@Nullable
    LinearLayout lnOverlay = null;

    @BindView(R.id.lnSub)@Nullable
    LinearLayout lnSub = null;

    @BindView(R.id.txtAddress)@Nullable
    TextView txtAddress = null;
    @OnTouch({R.id.txtAddress})
    @Optional
    boolean onTouch_txtAddress(View v) {
        MapPoint mapPoint = MapUtil.getPoiRecent();
        if (Utils.isNotEmpty(mapPoint) && ( NaviDestination.NOTI.equals(mapPoint.getDestination()) || NaviDestination.BASIC.equals(mapPoint.getDestination()) ) ) {
            new Callkt("onTouch_txtAddress").run((param)-> {
                MapUtil.runNavi(OverlayViewService.this, mapPoint, true);
                return null;
            });

        }
        return false;
    }

    @BindView(R.id.lnChip)@Nullable
    LinearLayout lnChip = null;
    @OnTouch({R.id.lnChip})
    @Optional
    boolean onTouch_lnChip(View v) {
        Fc.i(mContext,TAG,"onTouch_lnChip");
        int visible = lnSub.getVisibility();
        if ( visible == View.VISIBLE ) {
            return false;
        } else {
            setVisibleSub(View.VISIBLE);
            return true;
        }
    }

    @BindView(R.id.btnStart)@Nullable
    Button btnStart = null;
    @OnClick({R.id.btnStart})
    @Optional
    void onClick_btnStart(View v) {

//        runNavi(P_CLOSE_POPUP);

    }

    @BindView(R.id.btnCancel)@Nullable
    Button btnCancel = null;
    @OnClick({R.id.btnCancel})
    @Optional
    void onClick_btnCancel(View v) {
        Fc.i(mContext,TAG,"onClick_btnCancel");
        manager.removeView(onTopView);
        onTopView = null;
        stopSelf();
    }

    @BindView(R.id.btnSetting)@Nullable
    ImageButton btnSetting = null;
    @OnClick({R.id.btnSetting})
    @Optional
    void onClick_btnSetting(View v) {
        Fc.i(mContext,TAG,"onClick_btnSetting");
        closeOverlay();
        Utils.goFowardPage(mContext, WUrl.ReqId.WEB_SETTING.name(), 0, null);
    }

    @BindView(R.id.btnSearch)@Nullable
    ImageButton btnSearch = null;
    @OnClick({R.id.btnSearch})
    @Optional
    void onClick_btnSearch(View v) {
        Fc.i(mContext,TAG,"onClick_btnSearch");
        closeOverlay();
        Utils.goFowardPage(mContext, WUrl.ReqId.WEB_SEARCH.name(), 1, null);
    }

    @BindView(R.id.btnSearchNative)@Nullable
    ImageButton btnSearchNative = null;
    @OnClick({R.id.btnSearchNative})
    @Optional
    void onClick_btnSearchNative(View v) {
        Fc.i(mContext,TAG,"onClick_btnSearchNative");
        setVisibleSub(View.GONE);
        if ( PackageUtil.isSDKNavi() ) {
            MapUtil.runSearch(this,"");
        }
    }

    @BindView(R.id.btnMain)@Nullable
    ImageView btnMain = null;
    @OnClick({R.id.btnMain})
    @Optional
    void onClick_btnMain(View v) {
        Fc.i(mContext,TAG,"onClick_btnMain");
        try {
            closeOverlay();
            Intent i = new Intent(this, MainActivity.class); // 메인
//            if (!MapUtil.getNaviAutoStart().equals(StartMode.START)) {
//                Utils.goFowardPage(mContext, WUrl.ReqId.WEB_SETTING.name(), 0, null);
//            } else {
            Utils.goMain(mContext);
//            }
        } catch(Exception ex) {
            Fc.i(mContext,TAG,"onClick_btnMain",ex.toString());
        }
    }

    @BindView(R.id.btnGoNavi)@Nullable
    Button btnGoNavi = null;
    @OnClick({R.id.btnGoNavi})
    @Optional
    void onClick_btnGoNavi(View v) {
//      onClick_btnClose(btnClose);
//        MapUtil mapUtil = new MapUtil(this);
//        Log.d(TAG, "Map.runTmapRouteForPackage");
//        mapUtil.runTmap(this, null, -1, -1);
    }

    @BindView(R.id.btnCloseHour)@Nullable
    Button btnCloseHour = null;
    @OnClick({R.id.btnCloseHour})
    @Optional
    void onClick_btnCloseHour(View v) {
    }

    @BindView(R.id.btnClose)@Nullable
    Button btnClose = null;
    @SuppressLint("SuspiciousIndentation")
    @OnClick({R.id.btnClose})
    @Optional
    void onClick_btnClose(View v) {
        /** 20250402 : 권한처리 로직을 index.html에서 처리하도록 수정.
        if ( PackageUtil.getAndroidVersionId() >= 13 ) {
            long cnt = Long.parseLong(MapUtil.getPostNotificationsConfirmCount());
            if ( !PermissionUtil.isPostNotificationsPermission(mContext) ) {
                if ( PermissionUtil.isPermissionDoDeniedForPostNotifications() ) {
                    Utils.goMain(mContext);
                } else {
                    if (cnt < 2) {
                        Utils.goPermission(mContext, "post_notifications", null);
                        MapUtil.increasePostNotificationsConfirmCount();
                        if ( cnt == 1 ) {  //** * 두번째에는 거부를 한것으로 간주함
                            PermissionUtil.setPermissionDoDeniedForPostNotifications(true);
                        }
                        Utils.toast(mContext, R.string.msg_post_notifications);
                    } else {
                        Utils.goMain(mContext);
                    }
                }
            }
        }
        */
        /** 기도앱 - 강제 : 닫기시 종료 안함
         * */
        closeOverlay();
//        onTopView.setVisibility(View.GONE);

        Fc.i(mContext,TAG,"onClick_btnClose");
        Fa.i(mContext,TAG,Fa.EVENT_OVERLAY_VIEW_CLICK_CLOSE);
    }

    private void closeOverlay() {
        try {
            if ( this.mTimer != null ) this.mTimer.cancel();
//              countDownAnimation.cancel();
//            if ( MapUtil.isTest() ) {
//                if (countDownTimer != null) countDownTimer.cancel();
//            } else {
            L.d(TAG,"closeOverlay");
            WorkerExeckt.cancelTimer();
//            }
            //        if ( MapUtil.isClosePopup() ) {
            if (manager != null && onTopView != null) manager.removeView(onTopView);
            onTopView = null;
            OverlayViewUtil.stopService();
            stopForeground(true); // Remove from foreground
            stopSelf();
            //stopService(new Intent(this, OverlayViewService.class));
            //        } else {
            //            btnClose.setText(getString(R.string.guide_close));
            //            setVisibleInfo(View.GONE);
            //        }
        } catch(Exception ex) {
            Fc.i(mContext,TAG,"onClick_btnClose",ex.toString());
        }
    }
    private boolean checkSelectAppNRegHomeCompany(View v) {
        boolean doRegHome = false;
        boolean doRegCompany = false;
        boolean doSelectApp = false;
        MapPoint homePOI = MapUtil.getPoiHome();
        MapPoint companyPOI = MapUtil.getPoiCompany();
        if ( MapUtil.isSelectedApp() ) {
            if ( PackageUtil.isTmap() ) {
                if (MapUtil.isUsingRegHome()) {
                    if (Utils.isEmpty(homePOI)) {
                        doRegHome = true;
                    }
                    if (Utils.isEmpty(companyPOI)) {
                        doRegCompany = true;
                    }
                }
            } else {
                if (Utils.isEmpty(homePOI)) {
                    doRegHome = true;
                }
                if (Utils.isEmpty(companyPOI)) {
                    doRegCompany = true;
                }
            }
        } else {
            doSelectApp = true;
        }

        if ( doSelectApp ) {
            Utils.toast(mContext,R.string.msg_no_app_selected);
            Utils.goFowardPage(mContext, WUrl.ReqId.WEB_SETTING.name(),0,null);
            return false;
        } else if ( doRegHome && v.getId() == R.id.btnHome ) {
            if ( v.getId() == R.id.btnHome ) {
                Utils.toast(mContext, R.string.msg_not_reg_home);
                Utils.goFowardPage(mContext, WUrl.ReqId.WEB_HOME_COMPANY.name(), 0, null);
            } else if ( v.getId() == R.id.btnCompany ) {
                Utils.toast(mContext,R.string.msg_not_reg_compmay);
                Utils.goFowardPage(mContext, WUrl.ReqId.WEB_HOME_COMPANY.name(),1,null);
            }
            return false;
        } else if ( doRegCompany && v.getId() == R.id.btnCompany ) {
            if ( v.getId() == R.id.btnHome ) {
                Utils.toast(mContext, R.string.msg_not_reg_home);
                Utils.goFowardPage(mContext, WUrl.ReqId.WEB_HOME_COMPANY.name(), 0, null);
            } else if ( v.getId() == R.id.btnCompany ) {
                Utils.toast(mContext,R.string.msg_not_reg_compmay);
                Utils.goFowardPage(mContext, WUrl.ReqId.WEB_HOME_COMPANY.name(),1,null);
            }
            return false;
        }
        return true;
    }

    @BindView(R.id.btnHome)@Nullable
    Button btnHome = null;
    @OnLongClick({R.id.btnHome})
    @Optional
    boolean onLongClick_btnHome(View v) {
        Utils.goFowardPage(mContext, WUrl.ReqId.WEB_HOME_COMPANY.name(),0,null);
        return true;
    }
    @OnClick({R.id.btnHome})
    @Optional
    void onClick_btnHome(View v) {
        Fc.i(mContext,TAG,"onClick_btnHome");
        Fa.i(mContext,TAG,Fa.EVENT_OVERLAY_VIEW_CLICK_HOME);
        if ( !checkSelectAppNRegHomeCompany(v) ) return;

        NaviDestination naviDestination = MapUtil.getNaviDestinationInfo();
        if ( NaviDestination.HOME.equals(naviDestination) ) {
            MapUtil.clearPoiRecent();
        } else {
            MapPoint mapPoint = new MapPoint(NaviDestination.HOME,getString(R.string.navi_to_home),-1,-1);
            MapUtil.setPoiRecent(mapPoint);
        }
        updateAddress();
        MapUtil.runNavi(this,NaviDestination.HOME,true);
    }

    @BindView(R.id.btnCompany)@Nullable
    Button btnCompany = null;
    @OnLongClick({R.id.btnCompany})
    @Optional
    boolean onLongClick_btnCompany(View v) {
        Utils.goFowardPage(mContext, WUrl.ReqId.WEB_HOME_COMPANY.name(),1,null);
        return true;
    }
    @OnClick({R.id.btnCompany})
    @Optional
    void onClick_btnCompany(View v) {
        if ( !checkSelectAppNRegHomeCompany(v) ) return;

        Fc.i(mContext,TAG,"onClick_btnCompany");
        Fa.i(mContext,TAG,Fa.EVENT_OVERLAY_VIEW_CLICK_COMPANY);
        NaviDestination naviDestination = MapUtil.getNaviDestinationInfo();
        if ( NaviDestination.COMPANY.equals(naviDestination) ) {
            MapUtil.clearPoiRecent();
        } else {
            MapPoint mapPoint = new MapPoint(NaviDestination.COMPANY,getString(R.string.navi_to_company),-1,-1);
            MapUtil.setPoiRecent(mapPoint);
        }
        updateAddress();
        MapUtil.runNavi(this,NaviDestination.COMPANY,true);
    }

    @BindView(R.id.btnStartNavi)
    @Nullable
    Button btnDestination = null;
    @OnClick({R.id.btnStartNavi})
    @Optional
    void onClick_btnDestination(View v) {
        if ( !checkSelectAppNRegHomeCompany(v) ) return;

        Fc.i(mContext,TAG,"onClick_btnDestination");
        Fa.i(mContext,TAG,Fa.EVENT_OVERLAY_VIEW_CLICK_START_APP);
        MapUtil.clearPoiRecent();
        updateAddress();
        setVisibleSub(View.GONE);
        MapUtil.runNavi(this,NaviDestination.APP,true);
    }

    @BindView(R.id.lnMusic)
    @Nullable
    LinearLayout lnMusic = null;

    @BindView(R.id.btnPlay)
    @Nullable
    ImageButton btnPlay = null;
    @OnClick({R.id.btnPlay})
    @Optional
    void onClick_btnPlay(View v) {
        Fc.i(mContext,TAG,"onClick_btnPlay");
        Fa.i(mContext,TAG,Fa.EVENT_MUSIC_PLAY,DeviceUtil.isMusicActive(mContext));
        if ( !MapUtil.isNoticeAlertMusicPlayer() ) {
            show(false);
            Utils.toast(mContext,R.string.msg_go_music_player);
            MapUtil.execCommandActivity(mContext, DummyActivity.COMMAND_MUSIC_PLAY_NOTICE);
            initMusic();
        } else {
            MapUtil.runApp(this, MapUtil.getMusicAppPackage(),()->{
                boolean isMusicActive = DeviceUtil.isMusicActive(mContext);
                DeviceUtil.playMusicEnable(mContext,!isMusicActive,false);
                if (isMusicActive) {
                    Fa.i(mContext,TAG,Fa.EVENT_OVERLAY_VIEW_CLICK_PAUSE);
                    btnPlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_36);
                } else {
                    Fa.i(mContext,TAG,Fa.EVENT_OVERLAY_VIEW_CLICK_PLAY);
                    btnPlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_36);
                }
                setEnable(true);
            });
        }
    }

    @BindView(R.id.btnPlayPrev)
    @Nullable
    ImageButton btnPlayPrev = null;
    @OnClick({R.id.btnPlayPrev})
    @Optional
    void onClick_btnPlayPrev(View v) {
        Fc.i(mContext,TAG,"onClick_btnPlayPrev");
        Fa.i(mContext,TAG,Fa.EVENT_MUSIC_PLAY_PREV);
        tvMusicTitle.setText("");
        btnPlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_36);
        DeviceUtil.playMusicEnable(mContext,true,false);
        (new Callbackkt(1000, "onClick_btnPlayPrev")).run((param2) -> {
            DeviceUtil.sendMediaKeyevent(mContext, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            return null;
        });
        setEnable(true);
    }
    @BindView(R.id.btnPlayNext)
    @Nullable
    ImageButton btnPlayNext = null;
    @OnClick({R.id.btnPlayNext})
    @Optional
    void onClick_btnPlayNext(View v) {
        Fc.i(mContext,TAG,"onClick_btnPlayNext");
        Fa.i(mContext,TAG,Fa.EVENT_MUSIC_PLAY_NEXT);
        tvMusicTitle.setText("");
        btnPlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_36);
        DeviceUtil.playMusicEnable(mContext,true,false);
        (new Callbackkt(1000, "onClick_btnPlayNext")).run((param2) -> {
            DeviceUtil.sendMediaKeyevent(mContext, KeyEvent.KEYCODE_MEDIA_NEXT);
            return null;
        });
        setEnable(true);
    }

    @BindView(R.id.btnArrow)
    @Nullable
    ImageButton btnArrow = null;
    @OnClick({R.id.btnArrow})
    @Optional
    void onClick_btnArrowOpen(View v) {
        if ( lnSub.getVisibility() == View.VISIBLE ) {
            setVisibleSub(View.GONE);
        } else {
            setVisibleSub(View.VISIBLE);
        }
    }

    @BindView(R.id.btnAppEnable)
    @Nullable
    Switch btnAppEnable = null;
    @OnClick({R.id.btnAppEnable})
    @Optional
    void onClick_btnAppEnable(View v) {
//        v.setSelected(!v.isSelected());
        if (!MapUtil.isAppEnable()) { //
            MapUtil.setAppEnable(AppEnable.ENABLE);
            Utils.toast(this,R.string.msg_enabled);
            Fa.i(mContext,TAG,Fa.EVENT_APP_ENABLE);
        } else {
            StartStopOnOffVO ss = MapUtil.getStartStopOnOff();
            if ( ss.getPlayMusic().getStop().isUseYn() ) {
                DeviceUtil.playMusicEnable(mContext,false,false);
            }
            MapUtil.setAppEnable(AppEnable.DISABLE);
            Utils.toast(this,R.string.msg_disabled);
            Fa.i(mContext,TAG,Fa.EVENT_APP_DISABLE);
        }


        if ( MapUtil.isAppEnable() ) {
            if ( isStart() ) {
                MapUtil.startService(this);
            }
        } else {
            MapUtil.stopService(this);
        }

        if (PermissionUtil.isPermissionForType(mContext, PermissionType.OVERLAY) && MapUtil.isOverlayOpen() ) {
            OverlayViewUtil.init(this).openOverlayViewShowOnly();
        }

    }

    @BindView(R.id.tvPrayClassTitle)@Nullable
    TextView tvPrayClassTitle = null;
    @OnClick({R.id.tvPrayClassTitle,R.id.tvPrayClassDate,R.id.tvPrayClassStatus})
    @Optional
    void onClick_tvPrayClassTitle(View v) {
        if ( Utils.isNotEmpty(curPrayClass) ) {
            Fc.i(mContext,TAG,"btnPray");
            Fa.i(mContext,TAG, FaGodWhisper.EVENT_OVERLAY_VIEW_CLICK_PRAY);
            Utils.goPray(mContext, curPrayClass.getDocId());
            closeOverlay();
        }
    }

    @BindView(R.id.tvPrayClassDate)@Nullable
    TextView tvPrayClassDate = null;
    @BindView(R.id.tvPrayClassStatus)@Nullable
    TextView tvPrayClassStatus = null;
    @BindView(R.id.tvPrayClassTitleLabel)@Nullable
    TextView tvPrayClassTitleLabel = null;
    @BindView(R.id.tvPrayClassDateLabel)@Nullable
    TextView tvPrayClassDateLabel = null;
    @BindView(R.id.tvTodayDate)@Nullable
    TextView tvTodayDate = null;
    @BindView(R.id.tvTodayTitle)@Nullable
    TextView tvTodayTitle = null;
    @BindView(R.id.tvTodayContent)@Nullable
    TextView tvTodayContent = null;


    @BindView(R.id.btnTodayPray)@Nullable
    Button btnTodayPray = null;
    @OnLongClick({R.id.btnTodayPray})
    @Optional
    boolean onLongClick_btnTodayPray(View v) {
//        Utils.goFowardPage(mContext, WUrl.ReqId.WEB_HOME_COMPANY.name(),0,null);
        return true;
    }
    @OnClick({R.id.btnTodayPray})
    @Optional
    void onClick_btnTodayPray(View v) {
        Fc.i(mContext,TAG,"btnTodayPray");
        Fa.i(mContext,TAG,FaGodWhisper.EVENT_OVERLAY_VIEW_CLICK_HOME);
        Utils.goMain(mContext);
        closeOverlay();
    }

    @BindView(R.id.btnPray)@Nullable
    Button btnPray = null;
    @OnLongClick({R.id.btnPray})
    @Optional
    boolean onLongClick_btnPray(View v) {
//        Utils.goFowardPage(mContext, WUrl.ReqId.WEB_HOME_COMPANY.name(),0,null);
        return true;
    }
    @OnClick({R.id.btnPray})
    @Optional
    void onClick_btnPray(View v) {
        Fc.i(mContext,TAG,"btnPray");
        Fa.i(mContext,TAG, FaGodWhisper.EVENT_OVERLAY_VIEW_CLICK_PRAY);
        Utils.goFowardPage(mContext, WUrl.ReqId.WEB_PRAY.name(),0,null);
        closeOverlay();
    }

    @BindView(R.id.chip_cloud)@Nullable
    public ChipCloud chip_cloud = null;

    @BindView(R.id.chip_cloud2)@Nullable
    public ChipCloud chip_cloud2 = null;

    MapPoint homePoint = null;
    MapPoint companyPoint = null;
    MapPoint favoritePoint = null;

    ArrayList<MapPoint> favoritePoints = new ArrayList<MapPoint>();
    List<MapPoint> recentPoints = new ArrayList<>();

    private ArrayList<String> naviChip = new ArrayList<String>();
    private ArrayList<String> recentNaviChip = new ArrayList<String>();

    public WindowManager.LayoutParams getLayoutParam(){
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        int LAYOUT_FLAG;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; // TYPE_PHONE
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

//        params.gravity = Gravity.CENTER;
// top 10% position
        params.gravity = Gravity.LEFT | Gravity.TOP;

        saveX = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_OVERLAY_POPUP_X, -1L);
        saveY = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_OVERLAY_POPUP_Y, -1L);
        if ( saveX == -1 ) {
            params.x = (int) (10);
            params.y = (int) (metrics.heightPixels * 0.1);
        } else {
            params.x = (int) (saveX);
            params.y = (int) (saveY);
        }
        return params;
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public OverlayViewService getService() {
            return OverlayViewService.this;
        }
    }

    private ExecutorService executorService;

    private AppUpdateManager mAppUpdateManager;

    private MusicActiveLiveData musicActiveLiveData;

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Fc.i(mContext,TAG,"onBind");
        mContext = this;
//        init(intent,true);
        return binder;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Fc.i(mContext,TAG,"onStartCommand");
        Notification notification = NotificationUtil.init(this).sendNoti(null,null);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(1, notification); // point
            Fc.i(mContext,TAG,"onStartCommand","Step-02");
        } else {
            startForeground(1, notification,FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK); // point
            Fc.i(mContext,TAG,"onStartCommand","Step-03");
        }
        isForeground = true;
        mContext = this;

        // ExecutorService를 이용해 백그라운드에서 작업 실행
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                handleComplexLogic(intent);
//            }
//        });
     /** 블루투스 연결 & 뮤직 앱에서 이용 */
     // BluetoothUtil.init(mContext);
        debouncer = (new Debouncer(300));

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                init(intent,false);
                Fc.i(mContext,TAG,"onStartCommand","Step-04");
    /** 블루투스 연결 & 뮤직 앱에서 이용 */
//                if (!OverlayStartMode.STOP.equals(P_START_MODE)) {
//                    if (!DeviceUtil.isBluetoothEnable(mContext)) {
//                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//                            DeviceUtil.bluetoothEnable(mContext, true);
//                            BluetoothUtil.connectToDevice();
//                        } else {
//                            MapUtil.execCommandActivity(mContext, DummyActivity.COMMAND_BLUETOOTH_ENABLE);
//                        }
//                    }
//                    toggleAudioOutput();
//                }
                /** 펼쳐져서 유지되도록 */
             // hide(P_CLOSE_POPUP, Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
                setVisibleSub(P_EXPAND_SUB?View.VISIBLE:View.GONE);
                if ( Utils.isNotEmpty(P_MSG) ) {
                    displayMsg(Utils.fromHtml("<font color='#D84315'><b>" + P_MSG + "</b></font>"));
                }

                if ( PackageUtil.isSDKNavi() ) {
//                    btnSearch.setVisibility(View.GONE);
                } else {
                    if ( btnSearchNative != null ) btnSearchNative.setVisibility(View.GONE);
                }

                Timestamp timestamp = Timestamp.now();
                Date currentDate = timestamp.toDate();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 EEEE", Utils.getLocale(mContext) );
                String formattedDate = simpleDateFormat.format(currentDate);
                if ( tvTodayDate != null ) tvTodayDate.setText(formattedDate);
                if ( tvTodayTitle != null ) tvTodayTitle.setText("");
                if ( tvTodayContent != null ) tvTodayContent.setText("");
                RestUtil.getBasicSettings(mContext,()-> {
                    if (WApplication.basicSettings != null && Utils.isNotEmpty(WApplication.basicSettings.voices) ) {
                        if ( WApplication.basicSettings.voices.size() > 0 ) {
                            new Callkt("set Today Data").run((param)-> {
                                Voice voice = WApplication.basicSettings.voices.get(0);
                                tvTodayDate.setText(formattedDate);
                                tvTodayContent.setText(voice.getContent());
                                tvTodayTitle.setText(voice.getVerse());
                                L.d(TAG,"RestUtil.getBasicSettings", voice);
                                return null;
                            });
                        } else {
                            tvTodayDate.setText(formattedDate);
                            tvTodayTitle.setText("");
                            tvTodayContent.setText("");
                        }
                    } else {
                        tvTodayDate.setText(formattedDate);
                        tvTodayTitle.setText("");
                        tvTodayContent.setText("");
                    }
                });

                Timestamp currentTimestamp = Timestamp.now();

                // 600초 이전의 Timestamp 계산
                long currentMillis = currentTimestamp.getSeconds() * 1000; // 초를 밀리초로 변환
                long previousMillis = currentMillis - (WApplication.basicSettings.prayEndIntervalTimeSec * 1000); // 600초를 밀리초로 변환하여 빼기

                // 새로운 Timestamp 객체 생성 (600초 이전)
                Timestamp previousTimestamp = new Timestamp(previousMillis / 1000, currentTimestamp.getNanoseconds());

                // 현재 시간에서 1일 추가 (to 값)
                // long oneDayInSec = 24 * 60 * 60 + ( 12 * 60 * 60 ) ;  // 1일 + 12시간 을 밀리초로 계산
                long oneDayInSec = WApplication.basicSettings.prayClassSelectTimeSec;  // 1일 + 12시간 을 밀리초로 계산
                Timestamp toTimestamp = new Timestamp(currentTimestamp.getSeconds() + oneDayInSec, currentTimestamp.getNanoseconds());
/**
 * check서비스가 동작시. 호출한다. ( 동작주기는 1일, 동작주기 15분 ? )
 * 현재시간 ~ 현재시간 + 1일 범위로 조회된 기도 모임이 있으면,
 * 알람을 생성하고 , 프리퍼런스에 documentId를 Array로 저장한다.
 * 호출시 프리퍼런스에 documentId가 존재하면 알람을 생성하지 않는다...
 */
                curPrayClass = null;
                FbGodWhisper.selectTodayToOneDayPrayClass(mContext,previousTimestamp, toTimestamp , new ParamCallback<List<PrayClass>>() {
                    @Override
                    public void onResult(List<PrayClass> result) {
                        if ( result != null ) {
                            for (PrayClass prayClass : result) {
                                curPrayClass = prayClass;
                                prayClass.getDocId();
                                prayClass.getTitle();
                                prayClass.getPrayDt();
                                prayClass.getUuid();
                                Timestamp timestamp = prayClass.getPrayDt();
                                Date currentDate = timestamp.toDate();
                             // SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM월 dd일 EEEE HH:mm", Utils.getLocale(mContext) );
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM월 dd일 HH:mm", Utils.getLocale(mContext) );
                                String formattedDate = simpleDateFormat.format(currentDate);

                                tvPrayClassTitleLabel.setVisibility(View.VISIBLE);
                                try {
                                    tvPrayClassTitle.setText(Html.fromHtml(String.format("<u>%s</u>~",
                                            java.net.URLDecoder.decode(prayClass.getTitle(),"utf-8")
                                    )));
                                } catch (UnsupportedEncodingException e) {
//                                    throw new RuntimeException(e);
                                }
                                tvPrayClassTitle.setClickable(true);
                                tvPrayClassDate.setText(String.format("%s", formattedDate ));
                                tvPrayClassDate.setVisibility(View.VISIBLE);
                                tvPrayClassStatus.setVisibility(View.VISIBLE);
                                tvPrayClassDateLabel.setVisibility(View.VISIBLE);
                                if (PrayStatus.BEFORE.equals(prayClass.getPrayStatus()) ) {
                                    tvPrayClassStatus.setText(String.format("%s", "대기중" ));
                                    tvPrayClassStatus.setTextColor(getResources().getColor(R.color.green_900));
                                } else if (PrayStatus.ING.equals(prayClass.getPrayStatus()) ) {
                                    tvPrayClassStatus.setText(String.format("%s", "예배중" ));
                                    tvPrayClassStatus.setTextColor(getResources().getColor(R.color.skyblue));
                                } else if (PrayStatus.ING.equals(prayClass.getPrayStatus()) ) {
                                    tvPrayClassStatus.setText(String.format("%s", "예배종료" ));
                                    tvPrayClassStatus.setTextColor(getResources().getColor(R.color.red_900));
                                }
                                break;
                            }
                        }
                        if ( result == null || result.size() == 0 ) {
                            tvPrayClassTitleLabel.setVisibility(View.GONE);
                            tvPrayClassTitle.setVisibility(View.VISIBLE);
                            tvPrayClassTitle.setClickable(false);
                            tvPrayClassTitle.setText(String.format("%s","기도 모임이 없습니다."));
                            tvPrayClassTitle.setTextColor(getResources().getColor(R.color.gray_700));
                            tvPrayClassDate.setVisibility(View.GONE);
                            tvPrayClassStatus.setVisibility(View.GONE);
                            tvPrayClassDateLabel.setVisibility(View.GONE);
                            curPrayClass = null;
                        }
                    }

                });

            }
        }, 0);

        unregisterConnectReceiver();
        registerConnectReceiver();

        return super.onStartCommand(intent, flags, startId);
    }

    private void displayMsg(CharSequence msg) {
        txtAddress.setText(msg);
        (new Callbackkt(2000, "OverlayViewService.P_MSG")).run((param2) -> {
            updateAddress();
            return null;
        });
    }
    private LocationManager locationManager;
    private StopLocationListener stopLocationListener;
    private StopSensorListener stopSensorListener;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float speedKmh;
    private long checkTime;

    @BindView(R.id.btnMusicLogo)
    @Nullable
    ImageButton btnMusicLogo = null;
    @OnClick({R.id.btnMusicLogo})
    @Optional
    void onClick_btnMusicLogo(View v) {
        if ( !MapUtil.isNoticeAlertMusicPlayer() ) {
            show(false);
            Utils.toast(mContext,R.string.msg_go_music_player);
            MapUtil.execCommandActivity(mContext, DummyActivity.COMMAND_MUSIC_PLAY_NOTICE);
            initMusic();
        } else {
            MapUtil.runApp(this, MapUtil.getMusicAppPackage());
            show(true);
            initMusic();
//            AppUtil.bringAppToFront(mContext,MapUtil.getMusicAppPackage());
//            AppUtil.isAppRunning(mContext, mContext.getPackageName());
//            AppUtil.isAppRunning(mContext, MapUtil.getMusicAppPackage());
//            AppUtil.isActivityRunning(mContext, MainActivity.class);
//            if ( AppUtil.isAppRunning(mContext, MapUtil.getMusicAppPackage()) ) {
//                AppUtil.bringAppToFront(mContext, MapUtil.getMusicAppPackage());
//            } else {
//                MapUtil.runApp(this, MapUtil.getMusicAppPackage());
//            }
        }
    }
    PrayClass curPrayClass;
    @BindView(R.id.btnFav)
    @Nullable
    ImageButton btnFav = null;
    @OnClick({R.id.btnFav})
    @Optional
    void onClick_btnFav(View v) {
        closeOverlay();
        MapUtil.execCommandActivity(WApplication.mInstance, DummyActivity.COMMAND_FAV_LOCATION);
//        Utils.toast(mContext,R.string.msg_go_music_player);

    }

    @SuppressLint("MissingPermission")
    private void init(Intent intent, Boolean isBind) {
        manager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Fc.i(mContext,TAG,"init","Step-0001");
//            MapUtil.setNaviStartCallTime();

        /// softm 2024-06-18 serviceForeground
//            String notificationDetails = null;
//            NaviStatus naviStatus = null;
//            Boolean registerReceiver = null;
//            if ( intent != null ) {
//                notificationDetails = intent.getStringExtra("P_NAVI_DETAILS");
//                naviStatus = (NaviStatus) intent.getSerializableExtra("P_NAVI_STATUS");
//                registerReceiver = intent.getBooleanExtra("P_REGISTER_RECEIVER",false);
//            }
//            Notification notification = NotificationUtil.init(mContext).sendNoti(notificationDetails,naviStatus);
//            startForeground(2, notification); // point

        mAppUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        Task<AppUpdateInfo> appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
//                            && // 유연한 업데이트 사용 시 (AppUpdateType.FLEXIBLE) 사용
//                            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    // long APP_UPDATE_DELAY_TIME = 1000 * 60 * 60 * 24; // 1일의 밀리초 값
                    // long APP_UPDATE_DELAY_TIME = 1000 * 60 * 5; // 5분
                    long currentTime = System.currentTimeMillis();
                    checkTime = MapUtil.getAppUpdateCheckTime();
                    if (System.currentTimeMillis() - checkTime >= APP_UPDATE_DELAY_TIME) {
                        WApplication.appUpdateAvailable = true;
                        onClick_btnMain(null);
                    }
                } else {

                }
            }
        });

        appUpdateInfoTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                WApplication.appUpdateAvailable = false;
            }
        });
//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0002");
        if (intent == null) {
            if (manager != null && onTopView != null) manager.removeView(onTopView);
            onTopView = null;
            stopSelf();
            return;
        }

//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0003");

        WApplication.mInstance.mGeofencingClient = LocationServices.getGeofencingClient(this);

        P_BOOT = intent.getBooleanExtra("P_BOOT", false);

        P_EXPAND_SUB = intent.getBooleanExtra("P_EXPAND_SUB", false);

        P_CLOSE_POPUP = intent.getBooleanExtra("P_CLOSE_POPUP", false);
        P_NAVI_AUTO_START = intent.getBooleanExtra("P_NAVI_AUTO_START", false);
        P_IMMEDIATE = intent.getBooleanExtra("P_IMMEDIATE", false);
        P_NAVI_DESTINATION = (NaviDestination) intent.getSerializableExtra("P_NAVI_DESTINATION");
        P_NAME = intent.getStringExtra("P_NAME");
        P_MSG = intent.getStringExtra("P_MSG");
        P_LAT = intent.getFloatExtra("P_LAT", 0.0f);
        P_LON = intent.getFloatExtra("P_LON", 0.0f);

        /** add Extra */
        P_START_MODE = (OverlayStartMode) intent.getSerializableExtra("P_START_MODE");
        P_HIDE = intent.getBooleanExtra("P_HIDE", false);
        P_HIDE = false; /** 기도앱 - 강제 : 숨김 안함 */

        Fc.i(mContext,TAG,"init", "==================================================================================");
        Fc.i(mContext,TAG,"init","P_BOOT : " + P_BOOT);
        Fc.i(mContext,TAG,"init","P_CLOSE_POPUP : " + P_CLOSE_POPUP);
        Fc.i(mContext,TAG,"init","P_NAVI_AUTO_START : " + P_NAVI_AUTO_START);
        Fc.i(mContext,TAG,"init","P_IMMEDIATE : " + P_IMMEDIATE);
        Fc.i(mContext,TAG,"init","P_NAVI_DESTINATION : " + P_NAVI_DESTINATION);
        Fc.i(mContext,TAG,"init","P_NAME : " + P_NAME);
        Fc.i(mContext,TAG,"init","P_MSG : " + P_MSG);
        Fc.i(mContext,TAG,"init","P_LAT : " + P_LAT);
        Fc.i(mContext,TAG,"init","P_LON : " + P_LON);
        Fc.i(mContext,TAG,"init","P_EXPAND_SUB : " + P_EXPAND_SUB);
        Fc.i(mContext,TAG,"init","P_START_MODE : " + P_START_MODE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //        P_CLOSE_POPUP = true;
        //        if ( !P_CLOSE_POPUP) {
        if ( onTopView != null ) { // MidPad 오류로 remvoeView 로직 추가
            manager.removeView(onTopView);
        }
//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0004");
        onTopView = inflater.inflate(R.layout.always_on_top_layout, null);

        //        } else {
        //            onTopView = inflater.inflate(R.layout.always_on_top_cancel_layout, null);
        //        }

        //        if (P_REFENCE) {
        //            LinearLayout lnGuide = onTopView.findViewById(R.id.lnGuide);
        //            lnGuide.setVisibility(View.VISIBLE);
        //            TextView txtGuide = onTopView.findViewById(R.id.txtGuide);
        //            txtGuide.setText(R.string.overlay_guide_5);
        //        } else {
        LinearLayout lnGuide = onTopView.findViewById(R.id.lnGuide);
        if (!P_CLOSE_POPUP) {
            lnGuide.setVisibility(View.GONE);
        }
//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0005");
        //        }
        ImageView btnLogo = onTopView.findViewById(R.id.btnLogo);
        if ( MapUtil.isSelectedApp() ) {
            btnLogo.setImageDrawable(WApplication.appInfo.icon);
        } else {
            btnLogo.setImageResource(R.drawable.baseline_block_skyblue_24);
        }
        btnLogo.requestLayout();
//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0006");
        onTopView.setOnTouchListener(this);
//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0007");
        manager.addView(onTopView, getLayoutParam());
        ButterKnife.bind(this, onTopView);
        //        btnMain.setOnTouchListener(this);
//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0008");
        if (!P_CLOSE_POPUP) {
            Util.setBackground(this, btnStart, R.drawable.button_default);
            Util.setBackground(this, btnCancel, R.drawable.button_gray);
            btnStart.setTextColor(getResources().getColor(R.color.white));
            btnCancel.setTextColor(getResources().getColor(R.color.black));
//            Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0009");
//        } else {
            //            Util.setBackground(this,btnStart,R.drawable.button_gray);
            //            Util.setBackground(this,btnCancel,R.drawable.button_default);
            //            btnStart.setTextColor(getResources().getColor(R.color.black));
            //            btnCancel.setTextColor(getResources().getColor(R.color.white));
        }
//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0010");
        String homePOI = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POI_HOME, "");
        String companyPOI = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POI_COMPANY, "");
        String favoritePOI = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POI_FAVORITE, "");
        String favoritePOIs = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POIS_FAVORITE, "");
        String recentPOIs = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_POIS_RECENT, "");
//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0011");
        naviChip = new ArrayList<String>();
        recentNaviChip = new ArrayList<String>();
        //        naviChip.add(getString(R.string.home)); // index 0
        //        naviChip.add(getString(R.string.company)); // index 1

//            lnChip = onTopView.findViewById(R.id.lnChip);

        Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
                0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        int counter = (Const.NAVI_OVERLAY_COUNTDOWN_TIME / 1000);

        if (StringUtils.isNotEmpty(homePOI))
            homePoint = MapUtil.getPointFromString(homePOI);
        if (StringUtils.isNotEmpty(companyPOI))
            companyPoint = MapUtil.getPointFromString(companyPOI);
        if (StringUtils.isNotEmpty(favoritePOI))
            favoritePoint = MapUtil.getPointFromString(favoritePOI);
//            recentPoints = recentPoints.stream().filter(h -> h.getLat() != 0 ).collect(Collectors.toList());

        if ( MapUtil.isSelectedApp() ) {
            if ( PackageUtil.isTmap() ) {
                if (MapUtil.isUsingRegHome()) {
                    if (StringUtils.isEmpty(homePOI)) {
                        // btnHome.setEnabled(false);
                        btnHome.setText(Utils.fromHtml(getString(R.string.home_reg)));
                        btnHome.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_pink_disable));
                    }
                    if (StringUtils.isEmpty(companyPOI)) {
                        // btnCompany.setEnabled(false);
                        btnCompany.setText(Utils.fromHtml(getString(R.string.company_reg)));
                        btnCompany.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_yellowgreen_disable));
                    }
                }
            } else {
                if (StringUtils.isEmpty(homePOI)) {
                    // btnHome.setEnabled(false);
                    btnHome.setText(Utils.fromHtml(getString(R.string.home_reg)));
                    btnHome.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_pink_disable));
                }
                if (StringUtils.isEmpty(companyPOI)) {
                    // btnCompany.setEnabled(false);
                    btnCompany.setText(Utils.fromHtml(getString(R.string.company_reg)));
                    btnCompany.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_yellowgreen_disable));
                }
                // if (StringUtils.isEmpty(favoritePOI)) btnDestination.setEnabled(false);
            }
        } else {
//            btnHome.setEnabled(false);
//            btnCompany.setEnabled(false);
//            btnDestination.setEnabled(false);
//            btnHome.setBackground(ContextCompat.getDrawable(this, R.drawable.button_gray));
//            btnCompany.setBackground(ContextCompat.getDrawable(this, R.drawable.button_gray));
//            btnDestination.setBackground(ContextCompat.getDrawable(this, R.drawable.button_gray));
            btnHome.setTextColor(getResources().getColor(R.color.gray_500));
            btnCompany.setTextColor(getResources().getColor(R.color.gray_500));
            btnDestination.setTextColor(getResources().getColor(R.color.gray_500));
            btnHome.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_pink_disable));
            btnCompany.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_yellowgreen_disable));
            btnDestination.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_disable));
        }

        redrawFavoritePois();
        redrawRecentPois();

//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0016");
        if (NaviDestination.DRIVE.equals(P_NAVI_DESTINATION) ) {
            txtAddress.setText(getString(R.string.drive) + " " + getString(R.string.mode));
        } else {
            txtAddress.setText(P_NAME);
        }
//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0017");
        btnSetting.setVisibility(View.GONE);
//            if ( MapUtil.isNaviAutoStart() ) {
        redrawPermission();

        if ( MapUtil.isAppEnable() ) {
            txtAddress.setTextColor(getResources().getColor(R.color.black));
            if ( MapUtil.isSelectedApp() ) {
                updateAddress();
            } else {
                txtAddress.setText(getString(R.string.please_select_app));
            }
        } else {
            txtAddress.setText(getString(R.string.msg_disabled));
            txtAddress.setTextColor(getResources().getColor(R.color.red_400));
        }

        tvMusicTitle.setVisibility(View.GONE);
        if ( MapUtil.isSelectedMusicApp() ) {
            btnMusicLogo.setImageDrawable(WApplication.musicAppInfo.icon);
            btnMusicLogo.setVisibility(View.VISIBLE);
            lnMusic.setVisibility(View.VISIBLE);
        } else {
            btnMusicLogo.setVisibility(View.GONE);
            lnMusic.setVisibility(View.GONE);
        }

//            } else {
//                txtAddress.setText(getString(R.string.navi_to_start));
//                txtAddress.setText(getString(R.string.navi_auto_run_goto_setting) + " ▶︎︎"); // navi_auto_run_goto_setting
//            }
//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0020");
        startTime = System.currentTimeMillis();
//            setVisibleInfo(View.GONE);
        setVisibleSub(View.VISIBLE);

//        Log.d(TAG,"onStartCommand.0005");Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0021");
//            PackageUtil.isMapCategoryApp(appInfo.packageName)
/**
        if ( PackageUtil.isTmap() || ( MapUtil.isMapCategoryApp() && MapUtil.isUsingRegHome() ) ) {
            btnHome.setVisibility(View.VISIBLE);
            btnCompany.setVisibility(View.VISIBLE);
        } else {
            btnHome.setVisibility(View.GONE);
            btnCompany.setVisibility(View.GONE);
        }
*/
        btnAppEnable.setChecked(MapUtil.isAppEnable());

//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0022");
        final Handler handlerCounter = new Handler() {
            public void handleMessage(Message msg) {
                double periodSec = Math.ceil((System.currentTimeMillis() - startTime) / 1000);
                double remaindSec = ((Const.NAVI_OVERLAY_COUNTDOWN_TIME / 1000) - periodSec);
                String v = "";
                NumberFormat nf = NumberFormat.getInstance();
//                    if (!P_CLOSE_POPUP) { // 실행케이스 없음.
//                        try {
//                            v = getString(R.string.guide_start) + "(" + (remaindSec < 10 ? "" : "") + nf.parse(String.valueOf(remaindSec)).intValue() + ")";
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        if (btnStart != null) btnStart.setText(v);
//                    } else {
                try {
                    try {
                        v = getString(R.string.guide_close) + "(" + (remaindSec < 10 ? "" : "") + nf.parse(String.valueOf(remaindSec)).intValue() + ")";
                    } catch (java.text.ParseException e) {
                        throw new RuntimeException(e);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (btnClose != null) btnClose.setText(v);
//                    }
            }
        };
//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0010");
        final Handler handlerCounterComplete = new Handler() {
            public void handleMessage(Message msg) {
                setVisibleSub(View.GONE);
                completeCounting(P_CLOSE_POPUP, Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
                btnClose.setText(getString(R.string.guide_close));
            }
        };

        counterTask = new TimerTask() {
            public void run() {
                try {
                    if ((System.currentTimeMillis() - startTime) > Const.NAVI_OVERLAY_COUNTDOWN_TIME) {
                        if (mTimer != null) mTimer.cancel();
                        mTimer = null;
                        new Thread() {
                            public void run() {
                                Message msg = handlerCounterComplete.obtainMessage();
                                handlerCounterComplete.sendMessage(msg);
                            }
                        }.start();
                    } else {
                        Message msg = handlerCounter.obtainMessage();
                        handlerCounter.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0011");
        if ( !isBind ) {
            if (!NaviDestination.NONE.equals(P_NAVI_DESTINATION)) {
                if (!P_IMMEDIATE) {
                    if (P_NAVI_AUTO_START) {
                        //              txtCounter.setVisibility(View.VISIBLE);
//                        countDownAnimation.start();
//                        if ( MapUtil.isTest() ) {
//                            countDownTimer.start();
//                        } else {
                            L.i(TAG,"WorkerExeckt","startTimer-0");
                         // WorkerExeckt.startTimer(txtCounter, (counter == 0 ? 2 : counter), true,(params) -> {
                            WorkerExeckt.startTimer(txtCounter, counter, true,(params) -> {
                                L.i(TAG,"WorkerExeckt","startTimer-1");
                                txtCounter.setVisibility(View.GONE);
                                txtCounter.setText("");
                                if (P_NAVI_AUTO_START) {
                                    runNavi(P_NAVI_DESTINATION, P_CLOSE_POPUP);
                                } else {
                                    completeCounting(P_CLOSE_POPUP, Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
                                }

                                WorkQueue.nextTask();

                                L.i(TAG,"WorkerExeckt","startTimer-2");
                                return null;
                            });
//                        }
                    } else {
                        txtCounter.setVisibility(View.GONE);
                        this.mTimer = new Timer();
                        this.mTimer.schedule(counterTask, 0L, 1000L);
                        enabled = true;
                    }
                } else {
                    hide(P_CLOSE_POPUP, Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
                    closeOverlay();
                    runNavi(P_NAVI_DESTINATION, P_CLOSE_POPUP);
                }
            } else {
                setVisibleSub(View.GONE);
                txtCounter.setVisibility(View.GONE);
                hide(P_CLOSE_POPUP, Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
                enabled = true;
            }

            if (P_EXPAND_SUB) {
                if ( btnMusicLogo != null ) {
                    if ( MapUtil.isSelectedMusicApp() ) {
                        initMusic();
                    }
                }
                setVisibleSub(View.VISIBLE);
            }

            initLocation();

            initHoldNavi();

        } else {

        }
        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.End");
    }

    public void redrawRecentPois() {
        recentNaviChip = new ArrayList<String>();
        recentPoints = MapUtil.getPoisRecent();

        chip_cloud2.removeAllViews();
        chip_cloud2.invalidate();
        chip_cloud2.requestLayout();

        if (recentPoints.size() > 0) {
            chip_cloud2.setVisibility(View.VISIBLE);
        } else {
            chip_cloud2.setVisibility(View.GONE);
        }

        int loop = 0;
        if ( recentPoints.size() > 0 ) {
         // recentPoints = recentPoints.stream().filter(h -> h.getLat() != 0).collect(Collectors.toList());
            List<MapPoint> filteredPoints = new ArrayList<>();

            for (MapPoint poi : recentPoints) {
                String name = poi.getName();
                if (poi != null && poi.getLat() != 0) {
                    loop++;
                    filteredPoints.add(poi);
                    if (loop <= 15) {
                        //  && poi.getLat() != null
                        recentNaviChip.add(name); // index 2~~
                    }
                }
            }
            recentPoints = filteredPoints;
        }

//          cc.labels(new String[0]).build();

//            chip_cloud2.removeAllViews();
//            for (String c : recentNaviChip ) {
//                chip_cloud2.addChip(c);
//            }
//            cc.update();
        ChipCloud.Configure cc = new ChipCloud.Configure();
        cc.chipCloud(chip_cloud2)
                .selectedFontColor(getResources().getColor(R.color.chip2_select_font_color))
                .deselectedFontColor(getResources().getColor(R.color.chip2_deselect_font_color))
                .backgroundDrawble(R.drawable.chip_yellowgreen)
                // .backgroundDrawble(R.drawable.chip_deeporange)
                .chipGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL)
                .typefaceStyle(Typeface.NORMAL)
                .selectTransitionMS(500)
                .deselectTransitionMS(250)
//            .labels(Util.getStringArray(recentNaviChip))
                .mode(ChipCloud.Mode.REQUIRED)
                .allCaps(false)
                .gravity(ChipCloud.Gravity.LEFT)
                .textSize(getResources().getDimensionPixelSize(R.dimen.default_textsize))
                .verticalSpacing(getResources().getDimensionPixelSize(R.dimen.vertical_spacing))
                .minHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.min_horizontal_spacing))
                .chipListener(new ChipListener() {
                    @Override
                    public void chipSelected(int index) {
                        Fc.i(mContext,TAG,"chipSelected");
                        MapPoint mapPoint = recentPoints.get(index);
                        MapUtil.setPoiRecent(mapPoint);
                        MapUtil.addPoiRecent(mapPoint);
                        updateAddress();
                        setVisibleSub(View.GONE);
                        new Callkt("chip_cloud2-chipSelected").run((param)-> {
                            MapUtil.runNavi(OverlayViewService.this, mapPoint, true);
                            return null;
                        });
                    }

                    @Override
                    public void chipDeselected(int index) {
                        //...
                    }

                    @Override
                    public void chipLongSelected(int index) {
                        L.d(TAG,"chipLongSelected");
                        closeOverlay();
                        MapPoint mapPoint = recentPoints.get(index);
                        MapUtil.execCommandActivity(mContext, DummyActivity.COMMAND_REMOVE_RECENT_CHIP, String.valueOf(index),null,null,null,null);
                    }
                })

        ;
        cc.labels(Util.getStringArray(recentNaviChip));
//        cc.update();
        cc.build();

        if (recentNaviChip.size() == 0) {
            chip_cloud2.setVisibility(View.GONE);
        } else {
            chip_cloud2.setVisibility(View.VISIBLE);
        }
    }

    public void redrawFavoritePois() {
        favoritePoints = MapUtil.getPoisFavorite();

        if (favoritePoints.size() > 0) {
            lnChip.setVisibility(View.VISIBLE);
        } else {
            lnChip.setVisibility(View.GONE);
        }

        for (MapPoint poi : favoritePoints) {
         // if (loop >= 3) break;
            String name = poi.getName();
            naviChip.add(name); // index 2
        }

        new ChipCloud.Configure()
            .chipCloud(chip_cloud)
            .selectedFontColor(getResources().getColor(R.color.chip_select_font_color))
            .deselectedFontColor(getResources().getColor(R.color.chip_deselect_font_color))
            .backgroundDrawble(R.drawable.chip_skyblue)
         // .backgroundDrawble(R.drawable.chip_deepblue)
            .chipGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL)
        //  .typefaceStyle(Typeface.BOLD)
            .typefaceStyle(Typeface.NORMAL)
            .selectTransitionMS(500)
            .deselectTransitionMS(250)
            .labels(Util.getStringArray(naviChip))
            .mode(ChipCloud.Mode.REQUIRED)
         // .mode(ChipCloud.Mode.SINGLE)
         // .mode(ChipCloud.Mode.NONE)
            .allCaps(false)
            .gravity(ChipCloud.Gravity.LEFT)
            .textSize(getResources().getDimensionPixelSize(R.dimen.default_textsize))
            .verticalSpacing(getResources().getDimensionPixelSize(R.dimen.vertical_spacing))
            .minHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.min_horizontal_spacing))
         // .typeface(Typeface.createFromAsset(getContext().getAssets(), "RobotoSlab-Regular.ttf"))
            .chipListener(new ChipListener() {
                @Override
                public void chipSelected(int index) {
                    Fc.i(mContext,TAG,"chipSelected");
                    MapPoint mapPoint = favoritePoints.get(index);
                    MapUtil.setPoiRecent(mapPoint);
                    MapUtil.addPoiRecent(mapPoint);
                    updateAddress();
                    setVisibleSub(View.GONE);
                    new Callkt("chip_cloud-chipSelected").run((param)-> {
                        MapUtil.runNavi(OverlayViewService.this, mapPoint, true);
                        return null;
                    });
                }

                @Override
                public void chipDeselected(int index) {
                    //...
                }

                @Override
                public void chipLongSelected(int index) {
                    L.d(TAG,"chipLongSelected");
                    closeOverlay();
                    MapPoint mapPoint = favoritePoints.get(index);
                    MapUtil.execCommandActivity(mContext, DummyActivity.COMMAND_REMOVE_FAVORITE_CHIP, String.valueOf(index),null,null,null,null);
                }
            })
            .build();

        if (naviChip.size() == 0) {
            chip_cloud.setVisibility(View.GONE);
        } else {
            chip_cloud.setVisibility(View.VISIBLE);
        }

        chip_cloud.invalidate();
    }

    private void updateAddress() {
        if ( lnHoldNavi2 != null ) lnHoldNavi2.setVisibility(View.GONE);
        if ( !MapUtil.isSelectedApp() ) {
            if ( txtAddress != null ) txtAddress.setText(getString(R.string.msg_no_app_selected));
        } else {
            if(MapUtil.isUsingRecentPoiNavi()) {
                if ( lnHoldNavi2 != null ) lnHoldNavi2.setVisibility(View.VISIBLE);
                MapPoint mapPoint = MapUtil.getPoiRecent();
                if ( Utils.isNotEmpty(mapPoint) ) {
                    if ( txtAddress != null ) {
                        txtAddress.setText(Utils.fromHtml(getString(R.string.msg_to_navigate, mapPoint.getName())));
                    }
                    if ( lnHoldNavi != null ) lnHoldNavi.setVisibility(View.VISIBLE);
                        if ( btnHoldNaviCancel != null ) btnHoldNaviCancel.setVisibility(View.VISIBLE);
                }
            } else {
                if ( lnHoldNavi != null ) lnHoldNavi.setVisibility(View.GONE);
                if ( btnHoldNaviCancel != null ) btnHoldNaviCancel.setVisibility(View.GONE);
                NaviDestination naviDestination = P_NAVI_DESTINATION;
                if (NaviDestination.NONE.equals(P_NAVI_DESTINATION)) {
                    naviDestination = MapUtil.getNaviDestinationInfo();
                }
                // Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0018");
                if ( txtAddress != null ) {
                    if (MapUtil.isNaviApp()) {
                        if (PackageUtil.isTmap() || MapUtil.isUsingRegHome()) {
                            if (NaviDestination.HOME.equals(naviDestination)) {
                                txtAddress.setText(getString(R.string.navi_to_home));
                            } else if (NaviDestination.COMPANY.equals(naviDestination)) {
                                txtAddress.setText(getString(R.string.navi_to_company));
                            } else if (NaviDestination.DRIVE.equals(naviDestination)) {
                                txtAddress.setText(getString(R.string.navi_to_start));
                                // txtAddress.setText(getString(R.string.drive) + " " + getString(R.string.mode));
                            }
                        } else {
                            txtAddress.setText(getString(R.string.run_to_start));
                        }
                    } else {
                        txtAddress.setText(getString(R.string.run_to_start));
                    }
                }
            }
        }
    }

    PermissionType curPermissionType;
    public void redrawPermission() {
        lnPermission.setVisibility(View.GONE);
        tvPermission1.setVisibility(View.GONE);
        tvPermission2.setVisibility(View.GONE);
        tvPermission3.setVisibility(View.GONE);

        lnMusicMsg.setVisibility(View.GONE);

        if ( !Utils.hasPermission(mContext)
/** 20250403 : 필수 권한 배제 : 위치 || !PermissionUtil.isLocationPermission(mContext) */
        ) {
            lnPermission.setVisibility(View.VISIBLE);
            tvPermission1.setVisibility(View.VISIBLE);
                String grantStr = "";
                if ( Utils.isNotEmpty(AppUtil.getCheckGrantInfo()) ) {
                    for (String info : AppUtil.getCheckGrantInfo()) {
                        if (info.equals("notification_access") && !PermissionUtil.hasNotificationAccessPermission(mContext)) {
                            grantStr = getString(R.string.msg_activate_notification_access_permission);
                            curPermissionType = PermissionType.NOTIFICAION_ACCESS;
                            break;
                        } else if (info.equals("ignore_battery") && !PermissionUtil.hasIgnoreBatteryPermissions()) {
                            grantStr = getString(R.string.msg_activate_ignore_battery_permission);
                            curPermissionType = PermissionType.IGNORE_BATTERY;
                            break;
                        } else if (info.equals("accessibility") && ( !PermissionUtil.hasAccessibilityPermissions() || !PermissionUtil.isAccessibilityServiceRunning(mContext) ) ) {
                            tvPermission2.setVisibility(View.VISIBLE);
                            tvPermission3.setVisibility(View.VISIBLE);
                            grantStr = getString(R.string.msg_activate_accessibility_permission2);
                            tvPermission2.setText(Utils.fromHtml(getString(R.string.msg_activate_accessibility_permission3)));
                            tvPermission3.setText(Utils.fromHtml(getString(R.string.msg_activate_accessibility_permission)));
                            curPermissionType = PermissionType.ACCESSIBILITY;
                            break;
                        } else if (info.equals("location") && !PermissionUtil.isLocationPermission(mContext)) {
                            grantStr = getString(R.string.msg_activate_location_permission);
                            curPermissionType = PermissionType.LOCATION;
                            break;
                        }
                    }
                    //                tvPermission1.setText(R.string.msg_activate_permission);
                    if ( Utils.isNotEmpty(grantStr) ) {
                        tvPermission1.setText(Utils.fromHtml(grantStr));
                    } else {
                        tvPermission1.setVisibility(View.GONE);
                        lnPermission.setVisibility(View.GONE);
                    }
                }
        } else {
            lnPermission.setVisibility(View.GONE);
        }

        StartStopOnOffVO.StartStop playMusicStartStop = MapUtil.getStartStopOnOff().getPlayMusic();
        if ( playMusicStartStop != null
                && playMusicStartStop.getStart() != null
                && !playMusicStartStop.getStart().isUseYn()
        ) {
            lnMusicMsg.setVisibility(View.VISIBLE);
            tvMusicMsg.setText(Utils.fromHtml(getString(R.string.msg_music_play_setting)));
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocation() {
        if ( MapUtil.isRunSpeed() ) { // TODO SOFTM : TEST : 2024.12.18 : 속도 표시
            lnSpeed.setVisibility(View.VISIBLE);
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            stopLocationListener = new StopLocationListener(this);

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            stopSensorListener = new StopSensorListener(this);
            if (accelerometer != null) {
                sensorManager.registerListener(stopSensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }

            // 위치 업데이트 요청
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, // 업데이트 주기 (1초)
                    1, // 최소 거리 (1미터)
                    stopLocationListener
            );
        } else {
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Fc.i(mContext,TAG,"onDestroy");
//        if ( countDownAnimation != null) countDownAnimation.cancel();
        // NOT : TODO SOFTM : ANR : 2024.12.11 : ANR과 연관되었을 가능성 있음 isBound는 bindService시 비동기적으로 호출되므로 이 부분을 Enable했을 경우 unBindService가 호출되지 않고 다시 bindService가 호출되어 성능에 영향을 주는 것으로 추측됨
//        if (OverlayViewUtil.serviceConnection !=null ) {
            OverlayViewUtil.unbindFromConnectService(this);
//        }

        MapUtil.setMusicPlayTitle("");

//        if ( MapUtil.isTest() ) {
//            if (countDownTimer != null) countDownTimer.cancel();
//        } else {
        Fc.i(mContext,TAG,"onDestroy","cancelTimer");
            WorkerExeckt.cancelTimer();
//        }
        mContext = null;
        if ( this.mTimer != null ) this.mTimer.cancel();
        if (onTopView != null) {
            manager.removeView(onTopView);
            onTopView = null;
        }
        isForeground = false;

        if (locationManager != null && stopLocationListener != null) {
            locationManager.removeUpdates(stopLocationListener);
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(stopSensorListener);
        }

        /** 블루투스 연결 & 뮤직 앱에서 이용 */
        // BluetoothUtil.stop(mContext);
        try{
            unregisterConnectReceiver();
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        stopForeground(true); // Remove from foreground
        stopSelf();
    }

    public static boolean isInstanceCreated() {
        return mContext != null;
    }

    float xpos = 0;
    float ypos = 0;
    private float downX;
    private float downY;
    private final float SCROLL_THRESHOLD = 100;
    private boolean isOnClick;

    public void setVisibleSub(int visible) {
        updateAddress();
        if ( lnSub != null ) lnSub.setVisibility(visible);
        if ( lnChip != null ) lnChip.setVisibility(visible);
        if ( btnArrow != null ) {
            if (visible == View.VISIBLE) {
                btnArrow.setVisibility(View.VISIBLE);
                btnArrow.setImageResource(R.drawable.ic_baseline_keyboard_double_arrow_left_24);
            } else {
                btnArrow.setVisibility(View.GONE);
                btnArrow.setImageResource(R.drawable.ic_baseline_keyboard_double_arrow_right_24);
            }
        }
    }

    private void initMusic() {
        Fc.i(mContext,TAG,"initMusic");
        boolean isMusicActive = DeviceUtil.isMusicActive(mContext);
        // 음악 재생 상태에 따라 UI 업데이트
        if (isMusicActive) {
            String playTitle = MapUtil.getMusicPlayTitle();
            if ( Utils.isNotEmpty(playTitle) ) {
                tvMusicTitle.setVisibility(View.VISIBLE);
                tvMusicTitle.setText(playTitle);
            }
            btnPlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_36);
//            Utils.toast(mContext,"재생");
        } else {
            btnPlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_36);
//            Utils.toast(mContext,"중지");
        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int action = motionEvent.getAction();
        int pointerCount = motionEvent.getPointerCount();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (pointerCount == 1) {
                    xpos = motionEvent.getRawX();
                    ypos = motionEvent.getRawY();
                    downX = motionEvent.getX();
                    downY = motionEvent.getY();
                    isOnClick = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (pointerCount == 1) {
                    WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_OVERLAY_POPUP_X, saveX);
                    WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_OVERLAY_POPUP_Y, saveY);
                    if (isOnClick) {
                        Log.d(TAG, "onClick");
//                        if ( !this.animing && this.enabled ) {
                        if ( this.enabled ) {
                            //TODO onClick code
                            int visible = lnSub.getVisibility();
                            if ( visible == View.VISIBLE ) {
                                setVisibleSub(View.GONE);
                            } else {
                                if ( btnMusicLogo != null ) {
                                    if ( MapUtil.isSelectedMusicApp() ) {
                                        initMusic();
                                    }
                                }
                                setVisibleSub(View.VISIBLE);
                            }
                        } else {
                            setEnable(true);
                        }
                    } else {
                        hide(P_CLOSE_POPUP,Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerCount == 1) {
                    WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
                    float dx = xpos - motionEvent.getRawX();
                    float dy = ypos - motionEvent.getRawY();
                    xpos = motionEvent.getRawX();
                    ypos = motionEvent.getRawY();

//                    Log.d(TAG,(Math.abs(downX - motionEvent.getX())) + " / " + (Math.abs(downY - motionEvent.getY()))+ " / lp.x : " + lp.x + ", dx : " + dx + "lp.y : " + lp.y + ", dy : " + dy);

                    saveX = (long) (lp.x - dx);
                    saveY = (long) (lp.y - dy);
                    lp.x = (int)saveX;
                    lp.y = (int)saveY;
                    manager.updateViewLayout(view,lp);

                    if (isOnClick && (Math.abs(downX - motionEvent.getX()) > SCROLL_THRESHOLD || Math.abs(downY - motionEvent.getY()) > SCROLL_THRESHOLD)) {
//                        Log.i(TAG, "movement detected");
                        isOnClick = false;
                    }

                    setEnable(true);
                    return true;
                }
                break;
        }
        return false;
    }
    long saveX,saveY;
    // toast
//    public void toast( String value ) {
//        Toast.makeText(this, value, Toast.LENGTH_LONG).show();
//    }

    private void runNavi(NaviDestination naviDestination, boolean closePopup) {
//        countDownAnimation.cancel();
//        if ( MapUtil.isTest() ) {
//            if (countDownTimer != null) countDownTimer.cancel();
//        } else {
        Fc.i(mContext,TAG,"runNavi");
            WorkerExeckt.cancelTimer();
//        }
        if ( this.mTimer != null ) this.mTimer.cancel();
        mTimer = null;
        if( NaviDestination.BASIC.equals(naviDestination)) {
            MapPoint mapPoint = new MapPoint(P_NAME,P_LAT,P_LON);
            Fc.i(mContext,TAG,"runNavi","Step-01");
            MapUtil.runNavi(this, mapPoint, true);
        } else {
            Fc.i(mContext,TAG,"runNavi","Step-02");
            MapUtil.runNavi(this, naviDestination, true);
        }
        btnClose.setText(getString(R.string.guide_close));
        /**
         * call disabled
         */
//        MapUtil.setCallDisableForNavi();
        if (closePopup) {
            hide(closePopup);
        } else {
            setVisibleSub(View.GONE);
        }
//        onTopView.requestLayout(); // onMeasure
//        onTopView.invalidate(); // onDraw
//        manager.updateViewLayout(onTopView, getLayoutParam());
    }

    public void completeCounting(boolean closePopup,int time) {
        setEnable(false);
        if ( closePopup ) {
            hide(true,time);
        } else {
            setVisibleSub(View.GONE);
        }

    }

    public void startAlpha(View view,int anim) {
        startAlpha(view,anim,Const.NAVI_OVERLAY_BLUR_DURATION_TIME);
    }

    public void startAlpha(View view,int anim,int duration) {
        try {
            if (view instanceof ViewGroup) {
                //ViewGroup
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    startAlpha(viewGroup.getChildAt(i), anim,duration);
                }
            } else {
//            startAlpha(view,anim);
                Animation animation = AnimationUtils.loadAnimation(this.getApplicationContext(), anim);
                animation.setDuration(duration);
                if ( view != null ) view.startAnimation(animation);
            }
        } catch (Exception ex) {
        }
    }

    private static boolean enabled = false;
    private static boolean animing = false;
    Handler mHideHandler = new Handler(Looper.getMainLooper());
    Handler mEnableHandler = new Handler(Looper.getMainLooper());
    private void setEnable(boolean enabled) {
        animing = true;
        if ( true ) return ; /** 기도앱 - 강제 : 숨김 안함 */

        OverlayViewService.enabled = enabled;
        if ( lnOverlay != null ) {
            if (enabled) {
                Util.setBackground(this, lnOverlay, R.drawable.overlay_white);
            } else {
                Util.setBackground(this, lnOverlay, R.drawable.overlay_translate);
            }

            if (enabled) {
                startAlpha(lnOverlay, R.anim.alpha_10_to_100, 0);
            } else {
                startAlpha(lnOverlay, R.anim.alpha_100_to_10);
            }

            if (mEnableHandler != null) {
                mEnableHandler.removeCallbacksAndMessages(null);
                mEnableHandler = null;
            }

            if (mHideHandler != null) {
                mHideHandler.removeCallbacksAndMessages(null);
                mHideHandler = null;
            }

            if (enabled) {
                mEnableHandler = new Handler(Looper.getMainLooper());
                mEnableHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //                OverlayViewService.enabled = enabled;
                        animing = false;
                        if (enabled) {
                            //                    Util.setBackground(mContext,lnOverlay,R.drawable.overlay_white);
                        } else {
                            //                    Util.setBackground(mContext,lnOverlay,R.drawable.overlay_translate);
                        }
                        if (enabled) {
                            hide(P_CLOSE_POPUP, Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
                        }
                    }
                }, 3000);// 60000
            }
        }
    }

    public void show(boolean expandSub) {
        setEnable(true);
        try {
            if ( txtCounter != null ) txtCounter.setVisibility(View.GONE);
            setVisibleSub(expandSub ? View.VISIBLE : View.GONE);
        } catch(Exception ex) {
        }
    }
    public void show(CharSequence msg) {
        setEnable(true);
        try {
            if ( txtCounter != null ) txtCounter.setVisibility(View.GONE);
            setVisibleSub(Utils.isNotEmpty(msg) ? View.VISIBLE : View.GONE);
            if (Utils.isNotEmpty(msg)) {
                displayMsg(msg);
            }
        } catch(Exception ex) {
        }
    }

    public void show() {
        show(false);
    }

    public void hide() {
        hide(false,0);
    }

    public void hide(boolean closePopup) {
        hide(closePopup,0);
    }

    private void hide(boolean closePopup,int time) {
        /** P_HIDE 이면
         * 보이지만 않게 처리만 함.
         */
        if ( P_HIDE ) {
            setVisibleSub(View.GONE);
            setEnable(false);
            onTopView.setVisibility(View.GONE);
            return;
        }

        if ( mEnableHandler != null ) {
            mEnableHandler.removeCallbacksAndMessages(null);
            mEnableHandler = null;
        }

        if ( mHideHandler != null ) {
            mHideHandler.removeCallbacksAndMessages(null);
        }
        mHideHandler = new Handler(Looper.getMainLooper());
        mHideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if ( countDownAnimation != null) countDownAnimation.cancel();
//                if ( MapUtil.isTest() ) {
//                    if (countDownTimer != null) countDownTimer.cancel();
//                } else {

//                    WorkerExeckt.cancelTimer();
//                }
                /** 기도앱 - 강제 : 숨김 안함
                setVisibleSub(View.GONE);
                 * */
                setEnable(false);

                if(closePopup) closeOverlay();
            }
        }, time);
    }

    @BindView(R.id.tvMusicTitle)@Nullable
    TextView tvMusicTitle = null;
    public void setMusicPlayInfo(String title, String text, String subText, String infoText, String summaryText) {
        new Callkt("setMusicPlayInfo").run((param)-> {
            tvMusicTitle.setVisibility(View.VISIBLE);
            tvMusicTitle.setText(title);
            return null;
        });
//        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) tvMusicTitle.getLayoutParams();
//        manager.updateViewLayout(tvMusicTitle,lp);
    }

    @BindView(R.id.tvMusicMsg)@Nullable
    TextView tvMusicMsg = null;

    @BindView(R.id.lnMusicMsg)@Nullable
    LinearLayout lnMusicMsg = null;
    @OnTouch({R.id.lnMusicMsg})
    @Optional
    boolean onTouch_lnMusicMsg(View v) {
        closeOverlay();
        Utils.goFowardPage(mContext, WUrl.ReqId.WEB_ONOFF.name(), 0, null);
        return true;
    }
    @BindView(R.id.tvPermission1)@Nullable
    TextView tvPermission1 = null;
    @BindView(R.id.tvPermission2)@Nullable
    TextView tvPermission2 = null;
    @BindView(R.id.tvPermission3)@Nullable
    TextView tvPermission3 = null;

    @BindView(R.id.lnPermission)@Nullable
    LinearLayout lnPermission = null;
    @OnTouch({R.id.lnPermission})
    @Optional
    boolean onTouch_lnPermission(View v) {
        debouncer.debounce(()->{
/** 20250403 : 필수 권한 배제 : 접근성 */
//            if ( !PermissionUtil.hasAccessibilityPermissions() || !PermissionUtil.isAccessibilityServiceRunning(mContext) ) {
//                closeOverlay();
//    //            Utils.goPermission(mContext,"accessibility_reset",null);
//                Utils.goPermission(WApplication.mInstance,"accessibility",null);
//            } else {
                if ( PermissionType.IGNORE_BATTERY.equals(curPermissionType) ) {
                    PermissionUtil.requestIgnoreBatteryOptimizations(WApplication.mInstance, () -> {
                        redrawPermission();
                    });
                } else if ( PermissionType.LOCATION.equals(curPermissionType) ) {
                    MapUtil.execCommandActivity(WApplication.mInstance, DummyActivity.COMMAND_REQUEST_LOCATION_PERMISSION);
                } else {
                    closeOverlay();
                    Utils.goMain(WApplication.mInstance);
                }
//            }
        });
        return true;
    }

    @Override
    public void onStopDetected() {
        speedKmh = 0;
        lnSpeed.setVisibility(View.GONE);
        imgLogo.setVisibility(View.VISIBLE);
        txtSpeed.setText(String.valueOf((int) speedKmh));
    }
    @Override
    public void onLocationChanged(Location location) {
        float speed = location.getSpeed(); // m/s 단위로 반환
        // 속도를 km/h로 변환
        speedKmh = speed * 3.6f;
        L.d(TAG,"현재 속도: " + speedKmh + " km/h");
        // TODO SOFTM : TEST : 2024.12.18 : 특성속성 이상이면서 블루투스가 연결된게 어떤건지 체크하여 운전중으로 적용가능한지 조사
        lnSpeed.setVisibility(View.VISIBLE);
        imgLogo.setVisibility(View.GONE);
        txtSpeed.setText(String.valueOf((int) speedKmh));
    }

    @BindView(R.id.lnHoldNavi)
    @Nullable
    LinearLayout lnHoldNavi = null;

    @BindView(R.id.lnHoldNavi2)
    @Nullable
    LinearLayout lnHoldNavi2 = null;

    @BindView(R.id.btnHoldNaviCancel)
    @Nullable
    View btnHoldNaviCancel = null;

    @BindView(R.id.btnHoldNavi0Min)
    @Nullable
    Button btnHoldNavi0Min = null;

    @BindView(R.id.btnHoldNavi1Min)
    @Nullable
    Button btnHoldNavi1Min = null;

    @BindView(R.id.btnHoldNavi5Min)
    @Nullable
    Button btnHoldNavi5Min = null;

    @BindView(R.id.btnHoldNavi10Min)
    @Nullable
    Button btnHoldNavi10Min = null;

    @BindView(R.id.btnHoldNavi30Min)
    @Nullable
    Button btnHoldNavi30Min = null;

    @BindView(R.id.btnHoldNavi60Min)
    @Nullable
    Button btnHoldNavi60Min = null;

    @BindView(R.id.btnHoldNavi120Min)
    @Nullable
    Button btnHoldNavi120Min = null;

    @OnClick({R.id.btnHoldNaviCancel, R.id.btnHoldNavi0Min, R.id.btnHoldNavi1Min, R.id.btnHoldNavi5Min, R.id.btnHoldNavi10Min, R.id.btnHoldNavi30Min, R.id.btnHoldNavi60Min, R.id.btnHoldNavi120Min})
    @Optional
    void onClick_btnHoldNavi(View v) {
        switch (v.getId()) {
            case R.id.btnHoldNaviCancel:
                MapUtil.clearPoiRecent();
                updateAddress();
                btnHoldNaviCancel.setVisibility(View.GONE);
                break;
            case R.id.btnHoldNavi0Min:
                MapUtil.clearPoiRecent();
                btnHoldNavi0Min.setTextColor(getResources().getColor(R.color.white));
                btnHoldNavi0Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_deep));
                btnHoldNavi1Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi5Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi10Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi30Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi60Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi120Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                MapUtil.setDestinationExecutionHoldTime(String.valueOf(0));
                MapUtil.updatePoiNaviExpireTime();
                updateAddress();
                break;
            case R.id.btnHoldNavi1Min:
                btnHoldNavi0Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi1Min.setTextColor(getResources().getColor(R.color.white));
                btnHoldNavi1Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_deep));
                btnHoldNavi5Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi10Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi30Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi60Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi120Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                MapUtil.setDestinationExecutionHoldTime(String.valueOf(1000*60*1));
                MapUtil.updatePoiNaviExpireTime();
                updateAddress();
                break;
            case R.id.btnHoldNavi5Min:
                btnHoldNavi0Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi1Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi5Min.setTextColor(getResources().getColor(R.color.white));
                btnHoldNavi5Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_deep));
                btnHoldNavi10Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi30Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi60Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi120Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                MapUtil.setDestinationExecutionHoldTime(String.valueOf(1000*60*5));
                MapUtil.updatePoiNaviExpireTime();
                updateAddress();
                break;
            case R.id.btnHoldNavi10Min:
                btnHoldNavi0Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi1Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi5Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi10Min.setTextColor(getResources().getColor(R.color.white));
                btnHoldNavi10Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_deep));
                btnHoldNavi30Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi60Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi120Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                MapUtil.setDestinationExecutionHoldTime(String.valueOf(1000*60*10));
                MapUtil.updatePoiNaviExpireTime();
                updateAddress();
                break;
            case R.id.btnHoldNavi30Min:
                btnHoldNavi0Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi1Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi5Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi10Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi30Min.setTextColor(getResources().getColor(R.color.white));
                btnHoldNavi30Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_deep));
                btnHoldNavi60Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi120Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                MapUtil.setDestinationExecutionHoldTime(String.valueOf(1000*60*30));
                MapUtil.updatePoiNaviExpireTime();
                updateAddress();
                break;
            case R.id.btnHoldNavi60Min:
                btnHoldNavi0Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi1Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi5Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi10Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi30Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi60Min.setTextColor(getResources().getColor(R.color.white));
                btnHoldNavi60Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_deep));
                btnHoldNavi120Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                MapUtil.setDestinationExecutionHoldTime(String.valueOf(1000*60*60*1));
                MapUtil.updatePoiNaviExpireTime();
                updateAddress();
                break;
            case R.id.btnHoldNavi120Min:
                btnHoldNavi0Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi1Min.setBackground(ContextCompat.getDrawable(this,R.drawable.chip_skyblue_alpha));
                btnHoldNavi5Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi10Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi30Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi60Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_alpha));
                btnHoldNavi120Min.setTextColor(getResources().getColor(R.color.white));
                btnHoldNavi120Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_deep));
                MapUtil.setDestinationExecutionHoldTime(String.valueOf(1000*60*60*2));
                MapUtil.updatePoiNaviExpireTime();
                updateAddress();
                break;
            default:
        }
    }

    @SuppressLint("MissingPermission")
    private void initHoldNavi() {
        if(MapUtil.isUsingRecentPoiNavi()) {
            lnHoldNavi.setVisibility(View.VISIBLE);
            btnHoldNaviCancel.setVisibility(View.VISIBLE);
        } else {
            lnHoldNavi.setVisibility(View.GONE);
            btnHoldNaviCancel.setVisibility(View.GONE);
        }
        if (Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME == 0) { // 0
            btnHoldNavi0Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_deep));
            btnHoldNavi0Min.setTextColor(getResources().getColor(R.color.white));
        } else if (Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME == 1000 * 60 * 1) { // 60000
            btnHoldNavi1Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_deep));
            btnHoldNavi1Min.setTextColor(getResources().getColor(R.color.white));
        } else if (Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME == 1000 * 60 * 5) { // 300000
            btnHoldNavi5Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_deep));
            btnHoldNavi5Min.setTextColor(getResources().getColor(R.color.white));
        } else if (Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME == 1000 * 60 * 10) { // 600000
            btnHoldNavi10Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_deep));
            btnHoldNavi10Min.setTextColor(getResources().getColor(R.color.white));
        } else if (Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME == 1000 * 60 * 30) { // 1800000
            btnHoldNavi30Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_deep));
            btnHoldNavi30Min.setTextColor(getResources().getColor(R.color.white));
        } else if (Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME == 1000 * 60 * 60) { // 3600000
            btnHoldNavi60Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_deep));
            btnHoldNavi60Min.setTextColor(getResources().getColor(R.color.white));
        } else if (Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME == 1000 * 60 * 60 * 2 ) { // 7200000
            btnHoldNavi120Min.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_skyblue_deep));
            btnHoldNavi120Min.setTextColor(getResources().getColor(R.color.white));
        }
    }

    @BindView(R.id.btnBluetooth)
    @Nullable
    ImageButton btnBluetooth = null;
    @OnClick({R.id.btnBluetooth})
    @Optional
    void onClick_btnBluetooth(View v) {
        toggleAudioOutput();
    }

    private void toggleAudioOutput() {
        if ( !DeviceUtil.isBluetoothEnable(mContext) ) {
            btnBluetooth.setImageResource(R.drawable.ic_baseline_bluetooth_disable_24);
            BluetoothUtil.isBluetoothOn = false;
            Utils.toast(mContext,R.string.msg_turn_on_the_bluetooth);
            return;
        }

//        isBluetoothOn = checkBluetoothAudioConnected();
        L.d(TAG, "Bluetooth SCO enabled: " + BluetoothUtil.audioManager.isBluetoothScoOn());
//        BluetoothUtil.checkAudioOutput(this);
        show(false);
//        AudioDeviceInfo deviceInfo = audioManager.getCommunicationDevice();

//      if (!BluetoothUtil.checkAudioOutput(this).equals("Bluetooth")) {
        if (!BluetoothUtil.isBluetoothOn) {
            L.d(TAG,"toggleAudioOutput", "btnBluetooth is" + (btnBluetooth==null?"null":"not null"));
            L.d(TAG,"toggleAudioOutput", btnBluetooth);
            if ( btnBluetooth != null ) btnBluetooth.setImageResource(R.drawable.ic_baseline_bluetooth_24);
            // 현재 블루투스 스피커 사용 중일 때 기본 스피커로 전환
//            audioManager.setSpeakerphoneOn(true);
//            audioManager.setBluetoothScoOn(false);
//            audioManager.stopBluetoothSco();
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            BluetoothUtil.isBluetoothOn = true;
//            HashMap bt = MapUtil.getConnectedBluetooth();
//            if ( bt != null ) BluetoothUtil.disconnect(bt.get("address").toString());
//            HashMap bt = MapUtil.getConnectedBluetooth();
            HashMap bt = BluetoothUtil.getMusicBluetooth();
            if ( bt != null && BluetoothUtil.mBluetoothA2dp != null) {
//                        BluetoothUtil.connect(bt.get("address").toString());
                try {
                    BluetoothUtil.connect(BluetoothUtil.mBluetoothA2dp.getClass(), BluetoothUtil.mBluetoothA2dp,  bt.get("address").toString());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        } else {
            btnBluetooth.setImageResource(R.drawable.ic_baseline_bluetooth_disable_24);
            // 기본 스피커 사용 중일 때 블루투스 스피커로 전환
//            audioManager.setSpeakerphoneOn(false);
//            audioManager.setBluetoothScoOn(true);
//            audioManager.startBluetoothSco();
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            BluetoothUtil.isBluetoothOn = false;
//          HashMap bt = MapUtil.getConnectedBluetooth();
            HashMap bt = BluetoothUtil.getMusicBluetooth();
//            if ( bt != null ) BluetoothUtil.connect(bt.get("address").toString());

            if ( bt != null && BluetoothUtil.mBluetoothA2dp != null) {
                try {
                    boolean isMusicActive = DeviceUtil.isMusicActive(mContext);
                    BluetoothUtil.disconnect(BluetoothUtil.mBluetoothA2dp.getClass(), BluetoothUtil.mBluetoothA2dp,  bt.get("address").toString());

                    if ( isMusicActive ) {
                        (new Callbackkt(1000, "toggleAudioOutput.phone")).run((param2) -> {
                            DeviceUtil.playMusicEnable(mContext, isMusicActive, false);
                            return null;
                        });
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
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

