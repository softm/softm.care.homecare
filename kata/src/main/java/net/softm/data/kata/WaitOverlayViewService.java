package net.softm.data.kata;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_DISCONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_DISCONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_WIFI_DISCONNECTED;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.skyfishjy.library.RippleBackground;

import net.softm.data.kata.common.AdLoadType;
import net.softm.data.kata.common.AdViewOPType;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.ReceiverType;
import net.softm.data.kata.kt.Callbackkt;
import net.softm.data.kata.kt.Timerkt;
import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.LicenseUtil;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.NotificationUtil;
import net.softm.data.kata.util.Utils;
import net.softm.data.kata.util.WaitOverlayViewUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.ButterKnife;

public class WaitOverlayViewService extends Service implements View.OnTouchListener {
    static String TAG = "WaitOverlayViewService";
    public static boolean isForeground = false;
    private static WaitOverlayViewService mContext;
    MainActivity mActivity;

    static ACSFrameLayout mLayout;
    static ACSFrameLayout mLayoutUnlock;

    static long saveX;
    static long saveY;
    static WindowManager manager;

//    public static WindowManager.LayoutParams getLayoutParam(){
//        DisplayMetrics metrics = new DisplayMetrics();
//        manager.getDefaultDisplay().getMetrics(metrics);
//        int LAYOUT_FLAG;
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        }else{
//            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; // TYPE_PHONE
//        }
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                LAYOUT_FLAG,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
//                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
//                PixelFormat.TRANSLUCENT);
//
////        params.gravity = Gravity.CENTER;
//// top 10% position
//        params.gravity = Gravity.LEFT | Gravity.TOP;
//
//        saveX = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_OVERLAY_POPUP_X, -1L);
//        saveY = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_OVERLAY_POPUP_Y, -1L);
//        if ( saveX == -1 ) {
//            params.x = (int) (10);
//            params.y = (int) (metrics.heightPixels * 0.1);
//        } else {
//            params.x = (int) (saveX);
//            params.y = (int) (saveY);
//        }
//        return params;
//    }

    public static WindowManager.LayoutParams getLayoutParam2(){

        DisplayMetrics metrics = new DisplayMetrics();

        manager.getDefaultDisplay().getMetrics(metrics);
        int LAYOUT_FLAG;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = TYPE_APPLICATION_OVERLAY;
        }else{
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; // TYPE_PHONE
        }
//        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT);
        return params;
    }

    public static WindowManager.LayoutParams getLayoutParamForWaitingOverlay(){
//        WindowManager.LayoutParams.MATCH_PARENT, // Overlay must be full screen otherwise my trick will not work
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, // Allow another windows to receive touch events

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
//                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
//                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
//                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

//        WindowManager.LayoutParams params = getLayoutParam();
        WindowManager.LayoutParams params = getLayoutParam2();

        params.gravity = Gravity.CENTER;
// top 10% position
        params.gravity = Gravity.LEFT | Gravity.TOP;

        saveX = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_ASS_OVERLAY_POPUP_X, -1L);
        saveY = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_ASS_OVERLAY_POPUP_Y, -1L);

        if ( saveX == -1 ) {
            DisplayMetrics metrics = new DisplayMetrics();
            params.x = (int) (10);
            params.y = (int) (metrics.heightPixels * 0.1);
        } else {
            params.x = (int) (saveX);
            params.y = (int) (saveY);
        }

//        params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
//        params.format = PixelFormat.TRANSLUCENT;
//        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        params.gravity = Gravity.TOP;

        return params;
    }

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public WaitOverlayViewService getService() {
            return WaitOverlayViewService.this;
        }
    }

    private ExecutorService executorService;

    private AppUpdateManager mAppUpdateManager;

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
    boolean doAppReview;
    boolean adviewUserEarnedReward;
    long waitingSec;
    ReceiverType receiverType;
    String action;

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

        if (intent == null) {
            removeLayout();
            stopSelf();
            return START_NOT_STICKY;
        }

        doAppReview = intent.getBooleanExtra("P_DO_APP_REVIEW", false);
        adviewUserEarnedReward = intent.getBooleanExtra("P_ADVIEW_USER_EARNED_REWARD", false);
        waitingSec = intent.getLongExtra("P_WAITING_SEC", 0);
        receiverType = (ReceiverType) intent.getSerializableExtra("P_RECEIVER_TYPE");
        action = intent.getStringExtra("P_ACTION");
        eventGubun = intent.getStringExtra("P_GUBUN");

        if ( mLayout != null ) { // MidPad 오류로 remvoeView 로직 추가
            removeLayout();
        }

        if (AcsUtil.SHOW_WAITING_OVERLAY.equals(eventGubun)) {
            handleShowWaitingOverlay();
        } else if (AcsUtil.TURN_ON_SCREEN.equals(eventGubun)) {
            turnOnScreen();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void initManagerLayout() {
        manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = (ACSFrameLayout) inflater.inflate(R.layout.always_on_top_acs_layout, null);
        mLayoutUnlock = (ACSFrameLayout) inflater.inflate(R.layout.unlock_empty_layout, null);

        mLayout.setOnTouchListener(this);
        manager.addView(mLayout, getLayoutParamForWaitingOverlay());
        ButterKnife.bind(this, mLayout);

        RippleBackground rippleBackground=(RippleBackground)mLayout.findViewById(R.id.content);
        ImageView imageView=(ImageView)mLayout.findViewById(R.id.btnLogo);
//        acsUtil = new AcsUtil(this);
    }

    public void initManagerLayoutUnlock() {
        manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutUnlock = (ACSFrameLayout) inflater.inflate(R.layout.unlock_empty_layout, null);
    }

    private static void removeLayout() {
        try {
            if (mLayout!=null && ( mLayout.getWindowToken() != null || mLayout.getParent() != null ) ) {
                L.d(TAG,"removeLayout","mLayout");
                manager.removeView(mLayout);
            }
        } catch(Exception ex) {
        }

        try {
            if (mLayoutUnlock!=null && ( mLayoutUnlock.getWindowToken() !=null || mLayoutUnlock.getParent() != null ) ) {
                L.d(TAG,"removeLayout","mLayoutUnlock");
                manager.removeView(mLayoutUnlock);
                mLayoutUnlock = null;
            }
        } catch(Exception ex) {
        }
    }

    String eventGubun = "";
    public void handleShowWaitingOverlayUpdateStatus(String action, ReceiverType receiverType) {
        try {
//        if (AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS.equals(eventGubun)) {
            this.action = action;
            this.receiverType = receiverType;
            TextView tvStatus2 = mLayout.findViewById(R.id.statu2);
            ((TextView) mLayout.findViewById(R.id.app_name)).setText(WApplication.appInfo.name);
            if (action.equals(ACTION_SOFTM_POWER_CONNECTED) || action.equals(ACTION_SOFTM_ACL_CONNECTED) || action.equals(MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED)) {
                tvStatus2.setText(R.string.start);
                tvStatus2.setTextColor(getResources().getColor(R.color.blue_500));
                Log.i(TAG, "시작" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
            } else if (action.equals(ACTION_SOFTM_POWER_DISCONNECTED) || action.equals(ACTION_SOFTM_ACL_DISCONNECTED) || action.equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
                tvStatus2.setText(R.string.end);
                tvStatus2.setTextColor(getResources().getColor(R.color.red_500));
                Log.i(TAG, "종료" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
            }

            TextView tvReceiverStatus = mLayout.findViewById(R.id.receiver_status);
            String receiverStatus = receiverType.getDescription();
            tvReceiverStatus.setText(receiverStatus);
            TextView tvOrientation = mLayout.findViewById(R.id.tvOrientation);
            if ( BuildConfig.DEBUG ) {
                tvOrientation.setVisibility(View.VISIBLE);
                tvOrientation.setText(Utils.getOrientation(this).name());
            } else {
                tvOrientation.setVisibility(View.GONE);
            }
//        }
        } catch(Exception ex) {
            L.d(TAG,"handleShowWaitingOverlayUpdateStatus",ex);
        }
    }
    final boolean[] canStartTimer = {true};
    private void handleShowWaitingOverlay() {
        try {
            canStartTimer[0] = true;
            if (AcsUtil.SHOW_WAITING_OVERLAY.equals(eventGubun)) {
                showWaitStartTime = System.currentTimeMillis();
                removeLayout();
                initManagerLayout();

                TextView tvOrientation = mLayout.findViewById(R.id.tvOrientation);
                if ( BuildConfig.DEBUG ) {
                    tvOrientation.setVisibility(View.VISIBLE);
                    tvOrientation.setText(Utils.getOrientation(this).name());
                } else {
                    tvOrientation.setVisibility(View.GONE);
                }

                Fc.i(mContext,TAG,"handleShowWaitingOverlay",AcsUtil.SHOW_WAITING_OVERLAY);
                TextView tvInfoTimeSec = mLayout.findViewById(R.id.info_time_sec);
                tvInfoTimeSec.setText(String.valueOf(waitingSec));

                startWaitTimerkt = new Timerkt(waitingSec,(remainingTime, gubun, timer)->{
                    tvInfoTimeSec.setText(String.valueOf(remainingTime));
                    try {
                        String action = MainConnectReceiver.getAction();
                        TextView tvStatus2 = mLayout.findViewById(R.id.statu2);
                        if (action.equals(ACTION_SOFTM_POWER_CONNECTED) || action.equals(ACTION_SOFTM_ACL_CONNECTED) || action.equals(MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED)) {
                            tvStatus2.setText(R.string.start);
                            tvStatus2.setTextColor(getResources().getColor(R.color.blue_500));
                            Log.i(TAG, "시작" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
                        } else if (action.equals(ACTION_SOFTM_POWER_DISCONNECTED) || action.equals(ACTION_SOFTM_ACL_DISCONNECTED) || action.equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
                            tvStatus2.setText(R.string.end);
                            tvStatus2.setTextColor(getResources().getColor(R.color.red_500));
                            Log.i(TAG, "종료" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
                        }
                    } catch(Exception ex) {
                    }
                    return null;
                },(params)->{
                    L.log(this,TAG,"Timerkt.complete");
                    // TODO SOFTM : 광고 : 로직변경 : 접근성에서 실행하도록
                    try {
                        MainConnectReceiver.execReceiver(mContext);
                    } catch (Exception ex) {
                    }
                    stopWaitOverlay();
                    return null;
                },"startWaitStartTimer");

                Button btnExecStop = mLayout.findViewById(R.id.btnExecStop);
                btnExecStop.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stop();
                    }
                });

                TextView tvStatus2 = mLayout.findViewById(R.id.statu2);
                ((TextView) mLayout.findViewById(R.id.app_name)).setText(WApplication.appInfo.name);
                ((ImageView) mLayout.findViewById(R.id.app_logo)).setImageDrawable(WApplication.appInfo.icon);

                if (action.equals(ACTION_SOFTM_POWER_CONNECTED) || action.equals(ACTION_SOFTM_ACL_CONNECTED) || action.equals(MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED)) {
                    tvStatus2.setText(R.string.start);
                    tvStatus2.setTextColor(getResources().getColor(R.color.blue_500));
                    Log.i(TAG, "시작" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
                } else if (action.equals(ACTION_SOFTM_POWER_DISCONNECTED) || action.equals(ACTION_SOFTM_ACL_DISCONNECTED) || action.equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
                    tvStatus2.setText(R.string.end);
                    tvStatus2.setTextColor(getResources().getColor(R.color.red_500));
                    Log.i(TAG, "종료" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
                }

                TextView tvReceiverStatus = mLayout.findViewById(R.id.receiver_status);
                String receiverStatus = receiverType.getDescription();
                tvReceiverStatus.setText(receiverStatus);
                if ( LicenseUtil.isPaidLicense() ) {
                    mLayout.findViewById(R.id.imgVip).setVisibility(View.VISIBLE);
                }
                mLayout.setOnTouchListener(this);

                RippleBackground rippleBackground = (RippleBackground) mLayout.findViewById(R.id.content);
                ImageView imageView = (ImageView) mLayout.findViewById(R.id.btnLogo);
                rippleBackground.startRippleAnimation();

                ViewGroup adViewContainer = mLayout.findViewById(R.id.ad_view_container);
/**
 * 광고 설정 --------- 시작
 */
                LinearLayout lnAvdMain = mLayout.findViewById(R.id.lnAvdMain);
                if ( LicenseUtil.isPaidLicense() ) {  /** 유료회원 광고 미적용 : 2025-06-06 */
                    lnAvdMain.setVisibility(View.GONE);
                    adViewContainer.setVisibility(View.GONE);
                    if ( canStartTimer[0] ) startWaitOverlayTimer();
                } else { /** 유료회원 아님 */
                    LinearLayout lnAvd = mLayout.findViewById(R.id.lnAvd);
                    LinearLayout lnAvdMsgArea = mLayout.findViewById(R.id.lnAvdMsgArea);
                    if ( Utils.isNotEmpty(WApplication.basicInfoDTO) && WApplication.basicInfoDTO.useRewardForWaitOverlay ) {
                        lnAvdMain.setVisibility(View.VISIBLE);
                    } else {
                        lnAvdMain.setVisibility(View.GONE);
                    }
                    lnAvd.setVisibility(View.GONE);
                    lnAvdMsgArea.setVisibility(View.GONE);
//                if ( LicenseUtil.getAdViewOPType().equals(AdViewOPType.NONE) ) {
//                } else {
                    TextView tvAvdMsg = mLayout.findViewById(R.id.tvAvdMsg);
                    TextView tvAvdMsg2 = mLayout.findViewById(R.id.tvAvdMsg2);
                    TextView tvStatus3 = mLayout.findViewById(R.id.status3);
//                    LinearLayout lnAvdParent = mLayout.findViewById(R.id.lnAvdParent);

                    if (
                            !LicenseUtil.isLicense() &&
                                    (
                                            AdViewOPType.ADVIEW_STOP.equals(LicenseUtil.getAdViewOPType())
                                    )
                    )
                    {
                        tvStatus2.setText(R.string.expiration);
                        tvStatus2.setTextColor(getResources().getColor(R.color.orange_900));
                        canStartTimer[0] = false;
                    }

                    /**
                     * 하단 배너 ---- 시작
                     */
                    if ( AdUtil.isAdingWaitBanner() ) {
                        AdView adViewForWaitBanner;
                        /** {@link MainConnectReceiver}에서 loadWaitBanner 로 로드된 배너가 있으면 add  */
                        if (AdLoadType.INDIRECT.equals(AdUtil.getAdTypeForWaitBannerLoadType())) {
                            adViewForWaitBanner = AdUtil.getWaitBanner();
                            AdUtil.addLoadBanner(adViewContainer, mContext, AdUtil.getAdTypeForWaitBanner()); /** AdUtil.loadBanner가 선행 실행 되어야함 */
                        } else { /** 바로 로드 */
                            adViewForWaitBanner = AdUtil.addBanner(adViewContainer, mContext, AdUtil.getAdTypeForWaitBanner(), AdSize.MEDIUM_RECTANGLE);
                        }
                        adLoaded = false;
                        if (adViewForWaitBanner != null) {
                            tvStatus2.setText(mContext.getString(R.string.loading_ad)); // "광고 로딩중"
                            tvStatus2.setTextColor(getResources().getColor(R.color.green_500));
                            final boolean[] isStarted = {false};
                            if (action.equals(ACTION_SOFTM_POWER_CONNECTED) || action.equals(ACTION_SOFTM_ACL_CONNECTED) || action.equals(MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED)) {
                                isStarted[0] = true;
                            } else if (action.equals(ACTION_SOFTM_POWER_DISCONNECTED) || action.equals(ACTION_SOFTM_ACL_DISCONNECTED) || action.equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
                                isStarted[0] = false;
                            }

                            adViewForWaitBanner.setAdListener(new AdListener() {
                                @Override
                                public void onAdLoaded() {
                                    super.onAdLoaded();
                                    Log.i(TAG, "onAdLoaded");
                                    adLoaded = true;
                                }

                                @Override
                                public void onAdClosed() {
                                    super.onAdClosed();
                                    Log.i(TAG, "onAdClosed");
                                }

                                @Override
                                public void onAdOpened() {
                                    Log.e(TAG, "onAdOpened");
                                }

                                @Override
                                public void onAdClicked() {
                                    super.onAdClicked();
                                    Log.i(TAG, "onAdClicked");
                                }

                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                    adLoaded = true;
                                    Log.i(TAG, "onAdImpression");
                                    AdUtil.setWaitBanner(null);

                                    tvInfoTimeSec.setText("");

                                    if ( canStartTimer[0] ) startWaitOverlayTimer();

                                    if (
                                            !LicenseUtil.isLicense() &&
                                                    (
                                                            AdViewOPType.ADVIEW_STOP.equals(LicenseUtil.getAdViewOPType())
                                                    )
                                    ) {
                                        tvStatus2.setText(R.string.expiration);
                                        tvStatus2.setTextColor(getResources().getColor(R.color.orange_900));
                                    } else {
                                        if (isStarted[0]) {
                                            isStarted[0] = true;
                                            tvStatus2.setText(R.string.start);
                                            tvStatus2.setTextColor(getResources().getColor(R.color.blue_500));
                                            Log.i(TAG, "시작" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
                                        } else {
                                            isStarted[0] = false;
                                            tvStatus2.setText(R.string.end);
                                            tvStatus2.setTextColor(getResources().getColor(R.color.red_500));
                                            Log.i(TAG, "종료" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
                                        }
                                    }
                                }

                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    super.onAdFailedToLoad(loadAdError);
                                    adLoaded = true;
                                    Log.i(TAG, "onAdFailedToLoad");
                                    if ( canStartTimer[0] ) startWaitOverlayTimer();
                                }
                            });
                        } else {
                            if ( canStartTimer[0] ) startWaitOverlayTimer();
                        }
                    } else {
                        if ( canStartTimer[0] ) startWaitOverlayTimer();
                    }

                    /**
                     * 대기시간이 지났을 경우 계속 버튼 활성화
                     */
                    Button btnContinue = mLayout.findViewById(R.id.btnContinue);
                    btnContinue.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Fa.i(mContext,TAG,Fa.EVENT_WAIT_OVERLAY_CONTINUE_CLICK);
                                    // TODO SOFTM : 광고 : 로직변경 : 접근성에서 실행하도록
                                    try {
                                        MainConnectReceiver.execReceiver(mContext);
                                    } catch (Exception ex) {
                                    }
                                    stopWaitOverlay();
                                }
                            });
                    /** 대기시간 + 5초 후에 타이머 동작 안했을 경우 강제 타이머 동작 */
                    Callbackkt callbackkt = new Callbackkt((waitingSec*1000) + 5000, "startWaitStartTimer.Check;"); //
                    callbackkt.run((param) -> {
                        if (!adLoaded) {
                            if ( canStartTimer[0] ) startWaitOverlayTimer();
                        }
                        btnContinue.setVisibility(View.VISIBLE);
                        return null;
                    });

                    if (doAppReview) {
                        // showMessageForAdViewUserEarnedReward();
                        // LinearLayout lnAvdMsgArea = mLayout.findViewById(R.id.lnAvdMsgArea);
                        lnAvdMsgArea.setVisibility(View.VISIBLE);
                        tvAvdMsg.setText(mContext.getString(R.string.msg_review_thankyou1));
                        tvAvdMsg.setTextColor(getResources().getColor(R.color.red_500));
                        tvAvdMsg2.setVisibility(View.GONE);
                        return;
                    }

                    if ( AdViewOPType.NONE.equals(LicenseUtil.getAdViewOPType())) {
                        return;
                    }

                    // if ( !AdUtil.hasLicenseByAdView() || AdUtil.isAdViewAvailableForIntervalTime() ) {
                    //    lnAvdMsgArea.setVisibility(View.VISIBLE);
                    // }
                    // lnAvdParent.setVisibility(AdUtil.hasLicenseByAdView()?View.GONE:View.VISIBLE);
                    if ( !AdUtil.isAdingWaitReward() ) {
                        // lnAvdParent.setVisibility(View.GONE);
                    }
                    if (
                            !LicenseUtil.isLicense() &&
                                    (
                                            AdViewOPType.ADVIEW_STOP.equals(LicenseUtil.getAdViewOPType())
                                    )
                    ) {
                        /** 사용기간 만료 **/
                        tvAvdMsg.setText(R.string.msg_adview_license_expired);
                        tvAvdMsg2.setText(R.string.msg_adview_license_extend);
                        tvAvdMsg2.setVisibility(View.VISIBLE);
                        tvInfoTimeSec.setVisibility(View.GONE); // 초 라벨
                        tvStatus3.setVisibility(View.GONE); // 초 라벨
                        /* 만료일경우 삭제?
                         */
//                            tvStatus2.setText(mContext.getString(R.string.license_stop)); // "라이센스 중지됨"
//                            tvStatus2.setTextColor(getResources().getColor(R.color.red));
                    } else {
                        if (adviewUserEarnedReward) {
                            // showMessageForAdViewUserEarnedReward();
                            // LinearLayout lnAvdMsgArea = mLayout.findViewById(R.id.lnAvdMsgArea);
                            lnAvdMsgArea.setVisibility(View.VISIBLE);
                            tvAvdMsg.setText(mContext.getString(R.string.msg_adview_thankyou1));
                            tvAvdMsg.setTextColor(getResources().getColor(R.color.red_500));
                            tvAvdMsg2.setText(mContext.getString(R.string.msg_adview_user_earned_reward));
                            tvAvdMsg2.setTextColor(getResources().getColor(R.color.red_500));
                        } else {
                            // if (!AdUtil.hasLicenseByAdView() && !AdUtil.isAdLicense() ) {
                            // if ( !LicenseUtil.isLicense() && !LicenseUtil.getAdViewOPType().equals(AdViewOPType.NONE) ) {
                            if ( LicenseUtil.isLicense() ) {
                                tvAvdMsg.setText(mContext.getString(R.string.msg_adview_is));
                                tvAvdMsg.setTextColor(getResources().getColor(R.color.red_500));
                                tvAvdMsg2.setText(mContext.getString(R.string.msg_adview_thankyou2));
                                tvAvdMsg2.setTextColor(getResources().getColor(R.color.black_500));
                            } else {

                                if ( Utils.isNotEmpty(WApplication.basicInfoDTO) && WApplication.basicInfoDTO.useRewardButtonForWaitOverlay ) {
                                    lnAvd.setVisibility(View.VISIBLE);
                                }
                                lnAvdMsgArea.setVisibility(View.VISIBLE);
//                                tvAvdMsg.setText(mContext.getString(R.string.msg_adview_if));
                                tvAvdMsg.setText(mContext.getString(R.string.msg_free_lincense));
                                tvAvdMsg.setTextColor(getResources().getColor(R.color.red_500));
                                long waitingTime = MapUtil.getPreventWaitOverlayTime();
                                tvAvdMsg2.setText(
                                        Utils.fromHtml(
                                                mContext.getString(R.string.msg_adview_recuce_waittime,
                                                        String.valueOf(WaitOverlayViewUtil.getWaitTimeForNotAdLicense() / 1000),
                                                        String.valueOf(waitingTime / 1000))
                                        )
                                );
                            }
                        }
                    }

                    if ( ( AdViewOPType.ADVIEW.equals(LicenseUtil.getAdViewOPType())
                            || AdViewOPType.ADVIEW_STOP.equals(LicenseUtil.getAdViewOPType()) )
                            && AdUtil.isAdingWaitReward() && AdUtil.isAdViewAvailable() )
                    {
                        /**
                         * 보상형 전면 광고 버튼 클릭
                         */
                        lnAvd.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if ( startWaitTimerkt!=null) startWaitTimerkt.stop();
                                        Fa.i(mContext,TAG,Fa.EVENT_AD_VIEW_CLICK_REWARD);
                                        MapUtil.execCommandActivity(mContext, DummyActivity.COMMAND_VIEW_FULLAD);
//                                        Utils.goFowardPage(mContext, WUrl.ReqId.WEB_DEVELOPER_SUPPORT.name(),-1,null);
                                        MainConnectReceiver.pause();
                                    }
                                });
                        /**
                         * 광고가(mRewardedAd)가 로드된 경우 버튼 표시
                         */
                        new Timerkt(20,(remainingTime, gubun, timer)->{
                            if ( AdUtil.getRewardedAd() != null) {
                                if ( Utils.isNotEmpty(WApplication.basicInfoDTO) && WApplication.basicInfoDTO.useRewardButtonForWaitOverlay ) {
                                    lnAvd.setVisibility(View.VISIBLE);
                                }
                                lnAvdMsgArea.setVisibility(View.VISIBLE);
                            }
                            return null;
                        },(params)->{
                            L.log(this,TAG,"Timerkt.complete");
                            return null;
                        },"lnAvd.setVisibility(View.VISIBLE);").run();
                        /*
                         */
                        LinearLayout lnAvdTwinkle = mLayout.findViewById(R.id.lnAvd); // lnAvdParent
                        ObjectAnimator colorAnimator = ObjectAnimator.ofArgb(
                                lnAvdTwinkle,
                                "backgroundColor",
                                Color.parseColor("#FFF5DEB3"), // 시작 색상
                                Color.parseColor("#FFFFCC80")  // 끝 색상
                                // Color.parseColor("#d2f0fa"), // 시작 색상
                                // Color.parseColor("#9dd0e0")  // 끝 색상
                        );
                        colorAnimator.setDuration(1500); // 애니메이션 지속 시간
                        colorAnimator.setRepeatCount(ValueAnimator.INFINITE); // 무한 반복
                        colorAnimator.setRepeatMode(ValueAnimator.REVERSE); // 역방향 반복
                        colorAnimator.start();
                    } else {
                        if ( !AdUtil.isAdViewAvailableForIntervalTime() ) {
                            Utils.toastDebug(mContext, "다음 광고 시청시간 까지 :" + Utils.millisecondsToTime(AdUtil.getNextAdviewTime()));
                        } else if ( !AdUtil.isAdViewAvailableForAdMaxTime() ) {
                            Utils.toastDebug(mContext, "광고 시청시간 초과로 시청이 제한 되었습니다.");
                            L.d(TAG,"광고 시청시간 초과로 시청이 제한 되었습니다.");
                        }
                    }
                }  /** 유료회원 아님 - 끝 */
/**
 * 광고 설정 --------- 끝
 */
                // Simulate power button press
                int powerKeyCode = KeyEvent.KEYCODE_POWER;

                // Create a KeyEvent instance
                long eventTime = SystemClock.uptimeMillis();
                KeyEvent powerDownEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, powerKeyCode, 0);

                // Dispatch the KeyEvent
                mLayout.dispatchKeyEvent(powerDownEvent);

                // Wait for a brief moment
                try {
                    Thread.sleep(100); // Adjust the delay as per your requirement
                } catch (InterruptedException e) {
                }

                // Create the corresponding key up event
                KeyEvent powerUpEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, powerKeyCode, 0);

                // Dispatch the KeyEvent
                mLayout.dispatchKeyEvent(powerUpEvent);
                //            }
//            } // end else

            } // End If AcsUtil.SHOW_WAITING_OVERLAY
        } catch (Exception ex) {
            ex.printStackTrace();
//            if (AcsUtil.SHOW_WAITING_OVERLAY.equals(eventGubun)) {
//                // 대기오버레이 구동중 에러가 발생했다면
//                // 대기오버레이 타이머를 중단하고 이어서 리시버 동작 실행
//                if (startWaitTimerkt != null) startWaitTimerkt.stop();
//                //            MainConnectReceiver.stop();
//                try {
//                    MainConnectReceiver.execReceiver(mContext);
//                } catch (Exception ex2) {
//                }
//            }
//            Fc.e(mContext,TAG,"handleShowWaitingOverlay",ex.toString());
        }
    }

    public void turnOnScreen() {

        Fc.i(mContext,TAG,"turnOnScreen");
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                removeLayout();
                initManagerLayoutUnlock();
                manager.addView(mLayoutUnlock, getLayoutParam2());
                Fc.i(mContext,TAG,"turnOnScreen","Step-1");
                try {
//                    if (!MapUtil.isTest()) {
//                        mLayoutUnlock.setOnTouchListener(ConnectAccessibilityService.this);
//                    } else { // TODO SOFTM : TEST : 2024.12.18 : unlock 비정상적으로 실행루틴 종료
                    mLayoutUnlock.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            try {
                                removeLayout();
                                Utils.toast(mContext, "UnLock..");
                            } catch (Exception ex) {
                                Fc.e(mContext, TAG, "removeLayout", "mLayoutUnlock-1");
                            }
                            return true;
                        }
                    });
//                    }
                } catch (Exception ex) {
                    Fc.e(mContext,TAG,"turnOnScreen","Step-2");
                }

                int powerKeyCode = KeyEvent.KEYCODE_POWER;

                // Create a KeyEvent instance
                long eventTime = SystemClock.uptimeMillis();
                KeyEvent powerDownEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, powerKeyCode, 0);

                // Dispatch the KeyEvent
                mLayoutUnlock.dispatchKeyEvent(powerDownEvent);

                // Wait for a brief moment
                try {
                    Thread.sleep(100); // Adjust the delay as per your requirement
                } catch (InterruptedException e) {
                }

                // Create the corresponding key up event
                KeyEvent powerUpEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, powerKeyCode, 0);

                // Dispatch the KeyEvent
                mLayoutUnlock.dispatchKeyEvent(powerUpEvent);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            removeLayout();
                        } catch(Exception ex) {
                        }
                    }
                },5);
            }
        },0);

//                                    }
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Fc.i(mContext,TAG,"onDestroy");
        // NOT : TODO SOFTM : ANR : 2024.12.11 : ANR과 연관되었을 가능성 있음 isBound는 bindService시 비동기적으로 호출되므로 이 부분을 Enable했을 경우 unBindService가 호출되지 않고 다시 bindService가 호출되어 성능에 영향을 주는 것으로 추측됨
//        if (OverlayViewUtil.serviceConnection !=null ) {
        WaitOverlayViewUtil.unbindFromConnectService(this);
//        }
        Fc.i(mContext,TAG,"onDestroy");
        mContext = null;
        removeLayout();
        isForeground = false;
    }


    boolean adLoaded = false;
    static Timerkt startWaitTimerkt;
    private void startWaitOverlayTimer() {
//        if (startWaitTimerkt!=null) {
//            startWaitTimerkt.stop();
//        }
        L.d(TAG,"startWaitTimerkt","Start");

        canStartTimer[0] = false;
        startWaitTimerkt.run();
//        }
    }

    public static void stopWaitOverlay() {
        L.i(TAG,"stopWaitOverlay");
//        if ( MapUtil.isTest() ) {
//            if ( countDownHandler != null ) countDownHandler.removeCallbacksAndMessages(null);
//        } else {
//            WorkerExeckt.waitCancelTimer();
        try {
            if (startWaitTimerkt != null) {
                startWaitTimerkt.stop();
            }
        } catch(Exception ex) {
        }
//        }
        showWaitStartTime = -1;
        removeLayout();
        if ( mContext != null ) {
            mContext.stopForeground(true); // Remove from foreground
            mContext.stopSelf();
        }
    }

    public static long showWaitStartTime = -1;

    public static boolean isInstanceCreated() {
        return mContext != null;
    }
    float xpos = 0;
    float ypos = 0;
    private float downX;
    private float downY;
    private final float SCROLL_THRESHOLD = 100;
    private boolean isOnClick;

    private void stop() {
        stopWaitOverlay();
        Handler rHandler = new Handler(Looper.getMainLooper());
        rHandler.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                L.d(TAG, "Stop_Touch");
    //                                    view.performClick();
    // TODO softm : Gesture Click
    //                                    Path clickPath = new Path();
    //                                    clickPath.moveTo(xpos, ypos);
    //                                    GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
    //                                    gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 100));
    //                                    dispatchGesture(gestureBuilder.build(), null, null);
                Utils.toast(mContext,R.string.msg_exec_stopped);
    //                                    Utils.toast(mContext,"실행이 중지 되었습니다.");
    //                                    MainConnectReceiver.isStartAction()
                MainConnectReceiver.stopToShowOverlay();
                if (MainConnectReceiver.isStartAction()) {
                    if (MainConnectReceiver.isLockToOnScreenForStart()) {
                        MapUtil.increaseActonStartLockCancelCount();
                        Fa.i(mContext,TAG, Fa.EVENT_START_CANCEL_LOCK_TO_ON_SCREEN);
                        Fc.i(mContext,TAG,Fc.CUSTOM_KEY_ACTON_START_LOCK_CANCEL_COUNT,MapUtil.getActonStartLockCancelCount());
                        Fc.setCustomKey(Fc.CUSTOM_KEY_ACTON_START_LOCK_CANCEL_COUNT,MapUtil.getActonStartLockCancelCount());
                    } else {
                        MapUtil.increaseActonStartUnLockCancelCount();
                        Fa.i(mContext,TAG, Fa.EVENT_START_CANCEL_UNLOCK);
                        Fc.i(mContext,TAG,Fc.CUSTOM_KEY_ACTON_START_UNLOCK_CANCEL_COUNT,MapUtil.getActonStartUnLockCancelCount());
                        Fc.setCustomKey(Fc.CUSTOM_KEY_ACTON_START_UNLOCK_CANCEL_COUNT,MapUtil.getActonStartUnLockCancelCount());
                    }
                } else { /** STOP ACTION */
                    if (MainConnectReceiver.isLockToOnScreenForStart()) {
                        MapUtil.increaseActonStopLockCancelCount();
                        Fa.i(mContext,TAG, Fa.EVENT_STOP_CANCEL_LOCK_TO_ON_SCREEN);
                        Fc.i(mContext,TAG,Fc.CUSTOM_KEY_ACTON_STOP_LOCK_CANCEL_COUNT,MapUtil.getActonStopLockCancelCount());
                        Fc.setCustomKey(Fc.CUSTOM_KEY_ACTON_STOP_LOCK_CANCEL_COUNT,MapUtil.getActonStopLockCancelCount());
                    } else {
                        MapUtil.increaseActonStopUnLockCancelCount();
                        Fa.i(mContext,TAG, Fa.EVENT_STOP_CANCEL_UNLOCK);
                        Fc.i(mContext,TAG,Fc.CUSTOM_KEY_ACTON_STOP_UNLOCK_CANCEL_COUNT,MapUtil.getActonStopUnLockCancelCount());
                        Fc.setCustomKey(Fc.CUSTOM_KEY_ACTON_STOP_UNLOCK_CANCEL_COUNT,MapUtil.getActonStopUnLockCancelCount());
                    }
                }
            }
        }, 100);
        L.d(TAG, "Stop_onClick");
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
                    WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_ASS_OVERLAY_POPUP_X, saveX);
                    WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_ASS_OVERLAY_POPUP_Y, saveY);
                    if (isOnClick) {
////////////////////////////////////////////////////
                        stop();
                    } else {
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
//                if (pointerCount == 1) {
//                    WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
//                    float dx = xpos - motionEvent.getRawX();
//                    float dy = ypos - motionEvent.getRawY();
//                    xpos = motionEvent.getRawX();
//                    ypos = motionEvent.getRawY();
//
//                    Log.d(TAG,(Math.abs(downX - motionEvent.getX())) + " / " + (Math.abs(downY - motionEvent.getY()))+ " / lp.x : " + lp.x + ", dx : " + dx + "lp.y : " + lp.y + ", dy : " + dy);
//
//                    saveX = (long) (lp.x - dx);
//                    saveY = (long) (lp.y - dy);
//                    lp.x = (int)saveX;
//                    lp.y = (int)saveY;
//                    manager.updateViewLayout(view,lp);
//
//                    if (isOnClick && (Math.abs(downX - motionEvent.getX()) > SCROLL_THRESHOLD || Math.abs(downY - motionEvent.getY()) > SCROLL_THRESHOLD)) {
//                        Log.i(TAG, "movement detected");
//                        isOnClick = false;
//                    }
//                    return true;
//                }
                break;
        }
        return false;
    }

    public static ACSFrameLayout getLayout() {
        return mLayout;
    }
}

