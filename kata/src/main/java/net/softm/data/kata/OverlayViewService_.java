//package net.softm.data.kata;
//
//import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
//
//import android.annotation.SuppressLint;
//import android.app.Notification;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.PixelFormat;
//import android.graphics.Point;
//import android.graphics.Typeface;
//import android.net.ParseException;
//import android.os.Binder;
//import android.os.Build;
//import android.os.CountDownTimer;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Looper;
//import android.os.Message;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.view.Display;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.view.animation.AlphaAnimation;
//import android.view.animation.Animation;
//import android.view.animation.AnimationSet;
//import android.view.animation.AnimationUtils;
//import android.view.animation.ScaleAnimation;
//import android.widget.Button;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.Nullable;
//import androidx.core.content.ContextCompat;
//
//import com.adroitandroid.chipcloud.ChipCloud;
//import com.adroitandroid.chipcloud.ChipListener;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.play.core.appupdate.AppUpdateInfo;
//import com.google.android.play.core.appupdate.AppUpdateManager;
//import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
//import com.google.android.play.core.install.model.UpdateAvailability;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;
//
//import net.softm.lib.Util;
//import net.softm.data.kata.common.Const;
//import net.softm.data.kata.common.NaviDestination;
//import net.softm.data.kata.job.WorkQueue;
//import net.softm.data.kata.util.MapPoint;
//import net.softm.data.kata.util.MapUtil;
//import net.softm.data.kata.util.NotificationUtil;
//import net.softm.data.kata.util.OverlayViewUtil;
//import net.softm.data.kata.util.PackageUtil;
//import net.softm.data.kata.util.WorkerExeckt;
//
//import org.apache.commons.lang3.StringUtils;
//
//import java.text.NumberFormat;
//import java.util.ArrayList;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import butterknife.OnTouch;
//import butterknife.Optional;
//
////@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//public class OverlayViewService extends Service implements View.OnTouchListener , CountDownAnimation.CountDownListener {
//
//    String TAG = "OverlayViewService";
//    public static final String STOP_ACTION = "STOP_ACTION";
//    public static boolean isForeground = false;
//    private static OverlayViewService mContext;
//    MainActivity mActivity;
//    private View onTopView;
//    private WindowManager manager;
//    private Timer mTimer;
//
//    long startTime = 0;
//
//    TimerTask counterTask;
//
//    boolean P_BOOT = false;
////    boolean P_REFENCE = false;
//    boolean P_CLOSE_POPUP = true; // OVERLAY 닫기
//    boolean P_EXPAND_SUB = false; // lnSub 펼쳐지게
//    boolean P_NAVI_AUTO_START = false; // 내비 자동실행
//    boolean P_IMMEDIATE = false;
//    NaviDestination P_NAVI_DESTINATION = null;
////    LinearLayout lnChip = null;
//    String P_NAME = null;
//    float P_LAT = 0.0f;
//    float P_LON = 0.0f;
//
//    @BindView(R.id.txtCounter)@Nullable
//    TextView txtCounter = null;
//
//    @BindView(R.id.lnOverlay)@Nullable
//    LinearLayout lnOverlay = null;
//
//    @BindView(R.id.lnSub)@Nullable
//    LinearLayout lnSub = null;
//
//    @BindView(R.id.txtAddress)@Nullable
//    TextView txtAddress = null;
//
//    @BindView(R.id.lnChip)@Nullable
//    LinearLayout lnChip = null;
//    @OnTouch({R.id.lnChip})
//    @Optional
//    boolean onTouch_blnMain(View v) {
//        crashlytics.log("onTouch_blnMain");
//        int visible = lnSub.getVisibility();
//        if ( visible == View.VISIBLE ) {
//            return false;
//        } else {
//            setVisibleSub(View.VISIBLE);
//            return true;
//        }
//    }
//
//    @BindView(R.id.btnStart)@Nullable
//    Button btnStart = null;
//    @OnClick({R.id.btnStart})
//    @Optional
//    void onClick_btnStart(View v) {
//
////        runNavi(P_CLOSE_POPUP);
//
//    }
//
//    @BindView(R.id.btnCancel)@Nullable
//    Button btnCancel = null;
//    @OnClick({R.id.btnCancel})
//    @Optional
//    void onClick_btnCancel(View v) {
//        crashlytics.log("onClick_btnCancel");
//        manager.removeView(onTopView);
//        onTopView = null;
//        stopSelf();
//    }
//
//    @BindView(R.id.btnSetting)@Nullable
//    ImageButton btnSetting = null;
//    @OnClick({R.id.btnSetting})
//    @Optional
//    void onClick_btnSetting(View v) {
//        crashlytics.log("onClick_btnSetting");
//        onClick_btnClose(btnClose);
//        Intent i = new Intent(this,MainActivity.class); // 메인
//        i.putExtra("P_FOWARD_PAGE","SETTING");
////        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(i);
//    }
//
//    @BindView(R.id.btnSearch)@Nullable
//    ImageButton btnSearch = null;
//    @OnClick({R.id.btnSearch})
//    @Optional
//    void onClick_btnSearch(View v) {
//        crashlytics.log("onClick_btnSearch");
//        onClick_btnClose(btnClose);
//        Intent i = new Intent(this,MainActivity.class); // 메인
//        i.putExtra("P_FOWARD_PAGE","SEARCH");
//        i.putExtra("P_TIDX",1);
////        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(i);
//    }
//
//    @BindView(R.id.btnMain)@Nullable
//    ImageView btnMain = null;
//    @OnClick({R.id.btnMain})
//    @Optional
//    void onClick_btnMain(View v) {
//        crashlytics.log("onClick_btnMain");
//        try {
//            onClick_btnClose(btnClose);
//            Intent i = new Intent(this, MainActivity.class); // 메인
////        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            if (!MapUtil.isNaviAutoStart()) {
//                i.putExtra("P_FOWARD_PAGE", "SETTING");
//            }
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            this.startActivity(i);
//        } catch(Exception ex) {
//            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
//            crashlytics.setCustomKey("gubun","onClick_btnMain");
//            crashlytics.log("mContext : " + (mContext==null?"null":"not null"));
//            crashlytics.recordException(ex);
//        }
//    }
//
//
//    @BindView(R.id.btnGoNavi)@Nullable
//    Button btnGoNavi = null;
//    @OnClick({R.id.btnGoNavi})
//    @Optional
//    void onClick_btnGoNavi(View v) {
////      onClick_btnClose(btnClose);
////        MapUtil mapUtil = new MapUtil(this);
////        Log.d(TAG, "Map.runTmapRouteForPackage");
////        mapUtil.runTmap(this, null, -1, -1);
//    }
//
//    @BindView(R.id.btnCloseHour)@Nullable
//    Button btnCloseHour = null;
//    @OnClick({R.id.btnCloseHour})
//    @Optional
//    void onClick_btnCloseHour(View v) {
//    }
//
//    @BindView(R.id.btnClose)@Nullable
//    Button btnClose = null;
//    @SuppressLint("SuspiciousIndentation")
//    @OnClick({R.id.btnClose})
//    @Optional
//    void onClick_btnClose(View v) {
//        try {
//            if ( this.mTimer != null ) this.mTimer.cancel();
////              countDownAnimation.cancel();
////            if ( MapUtil.isTest() ) {
////                if (countDownTimer != null) countDownTimer.cancel();
////            } else {
//                WorkerExeckt.cancelTimer();
////            }
//    //        if ( MapUtil.isClosePopup() ) {
//            if (manager != null && onTopView != null) manager.removeView(onTopView);
//                onTopView = null;
//                OverlayViewUtil.stopService();
//                stopSelf();
//                //stopService(new Intent(this, OverlayViewService.class));
//    //        } else {
//    //            btnClose.setText(getString(R.string.guide_close));
//    //            setVisibleInfo(View.GONE);
//    //        }
//        } catch(Exception ex) {
//            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
//            crashlytics.setCustomKey("gubun","onClick_btnClose");
//            crashlytics.log("mContext : " + (mContext==null?"null":"not null"));
//            crashlytics.recordException(ex);
//        }
//
//    }
//
//    @BindView(R.id.btnHome)@Nullable
//    Button btnHome = null;
//    @OnClick({R.id.btnHome})
//    @Optional
//    void onClick_btnHome(View v) {
//        crashlytics.log("onClick_btnHome");
//        MapUtil.runNavi(this,NaviDestination.HOME,true);
//    }
//
//    @BindView(R.id.btnCompany)@Nullable
//    Button btnCompany = null;
//    @OnClick({R.id.btnCompany})
//    @Optional
//    void onClick_btnCompany(View v) {
//        crashlytics.log("onClick_btnCompany");
//        MapUtil.runNavi(this,NaviDestination.COMPANY,true);
//    }
//
//    @BindView(R.id.btnStartNavi)
//    @Nullable
//    Button btnDestination = null;
//    @OnClick({R.id.btnStartNavi})
//    @Optional
//    void onClick_btnDestination(View v) {
//        crashlytics.log("onClick_btnDestination");
//        MapUtil.runNavi(this,NaviDestination.APP,true);
//    }
//
//    @BindView(R.id.btnArrow)
//    @Nullable
//    ImageButton btnArrow = null;
//    @OnClick({R.id.btnArrow})
//    @Optional
//    void onClick_btnArrowOpen(View v) {
//        if ( lnSub.getVisibility() == View.VISIBLE ) {
//            setVisibleSub(View.GONE);
//        } else {
//            setVisibleSub(View.VISIBLE);
//        }
//    }
//
//    @BindView(R.id.chip_cloud)@Nullable
//    public ChipCloud chip_cloud = null;
//
//    MapPoint homePoint = null;
//    MapPoint companyPoint = null;
//    MapPoint favoritePoint = null;
//
//    ArrayList<MapPoint> favoritePoints = new ArrayList<MapPoint>();
//
//    private CountDownAnimation countDownAnimation;
//    private Animation mAnimation;
//    private AnimationSet mAnimationSet;
//    private CountDownTimer countDownTimer;
//    private ArrayList<String> naviChip = new ArrayList<String>();
//
//    public WindowManager.LayoutParams getLayoutParam(){
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
//
//    private final IBinder binder = new LocalBinder();
//
//    public class LocalBinder extends Binder {
//        public OverlayViewService getService() {
//            return OverlayViewService.this;
//        }
//    }
//
//    private ExecutorService executorService;
//    FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
//    private AppUpdateManager mAppUpdateManager;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        executorService = Executors.newSingleThreadExecutor();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        crashlytics.setCustomKey("gubun","OverlayViewService.onBind");
//        crashlytics.log("OverlayViewService.onBind");
//        mContext = this;
////        init(intent,true);
//        return binder;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId)
//    {
//        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
//        crashlytics.setCustomKey("OverlayViewService","onStartCommand");
//        crashlytics.log("OverlayViewService.onStartCommand.01");
//
//        Notification notification = NotificationUtil.init(this).sendNoti(null,null);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//            crashlytics.log("OverlayViewService.onStartCommand.02");
//            startForeground(1, notification); // point
//            crashlytics.log("OverlayViewService.onStartCommand.03");
//        } else {
//            crashlytics.log("OverlayViewService.onStartCommand.04");
//            startForeground(1, notification,FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK); // point
//            crashlytics.log("OverlayViewService.onStartCommand.05");
//        }
//        isForeground = true;
//        mContext = this;
//
//        // ExecutorService를 이용해 백그라운드에서 작업 실행
////        executorService.execute(new Runnable() {
////            @Override
////            public void run() {
////                handleComplexLogic(intent);
////            }
////        });
//
//        Handler mHandler = new Handler(Looper.getMainLooper());
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                crashlytics.log("OverlayViewService.onStartCommand.06");
//                init(intent,false);
//                crashlytics.log("OverlayViewService.onStartCommand.07");
//            }
//        }, 0);
//
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    private void init(Intent intent,Boolean isBind) {
//        manager = (WindowManager) getSystemService(WINDOW_SERVICE);
//
//        crashlytics.log("OverlayViewService.onStartCommand.init.0001");
////            MapUtil.setNaviStartCallTime();
//
//        /// softm 2024-06-18 serviceForeground
////            String notificationDetails = null;
////            NaviStatus naviStatus = null;
////            Boolean registerReceiver = null;
////            if ( intent != null ) {
////                notificationDetails = intent.getStringExtra("P_NAVI_DETAILS");
////                naviStatus = (NaviStatus) intent.getSerializableExtra("P_NAVI_STATUS");
////                registerReceiver = intent.getBooleanExtra("P_REGISTER_RECEIVER",false);
////            }
////            Notification notification = NotificationUtil.init(mContext).sendNoti(notificationDetails,naviStatus);
////            startForeground(2, notification); // point
//
//        mAppUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
//        Task<AppUpdateInfo> appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();
//        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
//            @Override
//            public void onSuccess(AppUpdateInfo appUpdateInfo) {
//                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
////                            && // 유연한 업데이트 사용 시 (AppUpdateType.FLEXIBLE) 사용
////                            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
//                ) {
//                    onClick_btnMain(null);
//                } else {
//
//                }
//            }
//        });
//
////        crashlytics.log("OverlayViewService.onStartCommand.init.0002");
//        if (intent == null) {
//            if (manager != null && onTopView != null) manager.removeView(onTopView);
//            onTopView = null;
//            stopSelf();
//            return;
//        }
//
////        crashlytics.log("OverlayViewService.onStartCommand.init.0003");
//
//        WApplication.mInstance.mGeofencingClient = LocationServices.getGeofencingClient(this);
//
//        P_BOOT = intent.getBooleanExtra("P_BOOT", false);
//
//        P_EXPAND_SUB = intent.getBooleanExtra("P_EXPAND_SUB", false);
//
//        P_CLOSE_POPUP = intent.getBooleanExtra("P_CLOSE_POPUP", false);
//        P_NAVI_AUTO_START = intent.getBooleanExtra("P_NAVI_AUTO_START", false);
//        P_IMMEDIATE = intent.getBooleanExtra("P_IMMEDIATE", false);
//        P_NAVI_DESTINATION = (NaviDestination) intent.getSerializableExtra("P_NAVI_DESTINATION");
//        P_NAME = intent.getStringExtra("P_NAME");
//        P_LAT = intent.getFloatExtra("P_LAT", 0.0f);
//        P_LON = intent.getFloatExtra("P_LON", 0.0f);
//
//        Log.d(TAG, "==================================================================================");
//        Log.d(TAG, "P_BOOT : " + P_BOOT);crashlytics.log("P_BOOT : " + P_BOOT);
//        Log.d(TAG, "P_CLOSE_POPUP : " + P_CLOSE_POPUP);crashlytics.log("P_CLOSE_POPUP : " + P_CLOSE_POPUP);
//        Log.d(TAG, "P_NAVI_AUTO_START : " + P_NAVI_AUTO_START);crashlytics.log("P_NAVI_AUTO_START : " + P_NAVI_AUTO_START);
//        Log.d(TAG, "P_IMMEDIATE : " + P_IMMEDIATE);crashlytics.log("P_IMMEDIATE : " + P_IMMEDIATE);
//        Log.d(TAG, "P_NAVI_DESTINATION : " + P_NAVI_DESTINATION);crashlytics.log("P_NAVI_DESTINATION : " + P_NAVI_DESTINATION);
//        Log.d(TAG, "P_NAME : " + P_NAME);crashlytics.log("P_NAME : " + P_NAME);
//        Log.d(TAG, "P_LAT : " + P_LAT);crashlytics.log("P_LAT : " + P_LAT);
//        Log.d(TAG, "P_LON : " + P_LON);crashlytics.log("P_LON : " + P_LON);
//        Log.d(TAG, "P_EXPAND_SUB : " + P_EXPAND_SUB);crashlytics.log("P_EXPAND_SUB : " + P_EXPAND_SUB);
//
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        //        P_CLOSE_POPUP = true;
//        //        if ( !P_CLOSE_POPUP) {
//        if ( onTopView != null ) { // MidPad 오류로 remvoeView 로직 추가
//            manager.removeView(onTopView);
//        }
////        crashlytics.log("OverlayViewService.onStartCommand.init.0004");
//        onTopView = inflater.inflate(R.layout.always_on_top_layout, null);
//
//        //        } else {
//        //            onTopView = inflater.inflate(R.layout.always_on_top_cancel_layout, null);
//        //        }
//
//        //        if (P_REFENCE) {
//        //            LinearLayout lnGuide = onTopView.findViewById(R.id.lnGuide);
//        //            lnGuide.setVisibility(View.VISIBLE);
//        //            TextView txtGuide = onTopView.findViewById(R.id.txtGuide);
//        //            txtGuide.setText(R.string.overlay_guide_5);
//        //        } else {
//        LinearLayout lnGuide = onTopView.findViewById(R.id.lnGuide);
//        if (!P_CLOSE_POPUP) {
//            lnGuide.setVisibility(View.GONE);
//        }
////        crashlytics.log("OverlayViewService.onStartCommand.init.0005");
//        //        }
//        ImageView btnLogo = onTopView.findViewById(R.id.btnLogo);
//        if ( MapUtil.isSelectedApp() ) {
//            btnLogo.setImageDrawable(WApplication.appInfo.icon);
//        } else {
//            btnLogo.setImageResource(R.drawable.baseline_block_24);
//        }
//        btnLogo.requestLayout();
////        crashlytics.log("OverlayViewService.onStartCommand.init.0006");
//        onTopView.setOnTouchListener(this);
////        crashlytics.log("OverlayViewService.onStartCommand.init.0007");
//        manager.addView(onTopView, getLayoutParam());
//        ButterKnife.bind(this, onTopView);
//        //        btnMain.setOnTouchListener(this);
////        crashlytics.log("OverlayViewService.onStartCommand.init.0008");
//        if (!P_CLOSE_POPUP) {
//            Util.setBackground(this, btnStart, R.drawable.button_default);
//            Util.setBackground(this, btnCancel, R.drawable.button_gray);
//            btnStart.setTextColor(getResources().getColor(R.color.white));
//            btnCancel.setTextColor(getResources().getColor(R.color.black));
////            crashlytics.log("OverlayViewService.onStartCommand.init.0009");
////        } else {
//            //            Util.setBackground(this,btnStart,R.drawable.button_gray);
//            //            Util.setBackground(this,btnCancel,R.drawable.button_default);
//            //            btnStart.setTextColor(getResources().getColor(R.color.black));
//            //            btnCancel.setTextColor(getResources().getColor(R.color.white));
//        }
////        crashlytics.log("OverlayViewService.onStartCommand.init.0010");
//        String homePOI = WApplication.mInstance.getSharedPreferences("_POI_HOME", "");
//        String companyPOI = WApplication.mInstance.getSharedPreferences("_POI_COMPANY", "");
//        String favoritePOI = WApplication.mInstance.getSharedPreferences("_POI_FAVORITE", "");
//        String favoritePOIs = WApplication.mInstance.getSharedPreferences("_POIS_FAVORITE", "");
////        crashlytics.log("OverlayViewService.onStartCommand.init.0011");
//        naviChip = new ArrayList<String>();
//        //        naviChip.add(getString(R.string.home)); // index 0
//        //        naviChip.add(getString(R.string.company)); // index 1
//
////            lnChip = onTopView.findViewById(R.id.lnChip);
//
//        countDownAnimation = new CountDownAnimation(txtCounter, (Const.NAVI_OVERLAY_COUNTDOWN_TIME / 1000));
//        countDownAnimation.setCountDownListener(this);
//        Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
//                0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
//                Animation.RELATIVE_TO_SELF, 0.5f);
//        countDownAnimation.setAnimation(scaleAnimation);
//        int counter = (Const.NAVI_OVERLAY_COUNTDOWN_TIME / 1000);
//        countDownAnimation.setStartCount(counter);
////        crashlytics.log("OverlayViewService.onStartCommand.init.0012");
//
//        countDownTimer = new CountDownTimer((counter==0?1:counter) * 1000, 1000) {
//            public void onTick(long millisUntilFinished) {
//                txtCounter.setVisibility(View.VISIBLE);
//                txtCounter.setText( String.valueOf(millisUntilFinished / 1000) );
//                txtCounter.startAnimation(mAnimationSet);
//            }
//
//            public void onFinish() {
//                txtCounter.setVisibility(View.GONE);
//                txtCounter.setText("");
//                if ( P_NAVI_AUTO_START ) {
//                    runNavi(P_NAVI_DESTINATION,P_CLOSE_POPUP);
//                }
//                else {
//                    completeCounting(P_CLOSE_POPUP,Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
//                }
//
//                WorkQueue.nextTask();
//            }
//        };
//        mAnimation = new AlphaAnimation(1.0f, 0.0f);
//        mAnimation.setDuration(1000);
////        txtCounter.setAnimation(mAnimation);
//
//        // 크기 줄이기 애니메이션
//        ScaleAnimation scaleAnimation2 = new ScaleAnimation(
//                1f, 0.5f, // X축 크기 변화
//                1f, 0.5f, // Y축 크기 변화
//                Animation.RELATIVE_TO_SELF, 0.5f, // X축 중심
//                Animation.RELATIVE_TO_SELF, 0.5f  // Y축 중심
//        );
//        scaleAnimation2.setDuration(1000);
//        scaleAnimation2.setFillAfter(true);
//
//        // 알파값 변화 애니메이션
//        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f); // 시작: 1f, 종료: 0f
//        alphaAnimation.setDuration(1000);
//        alphaAnimation.setFillAfter(true);
//
//        // 애니메이션 세트 생성
//        AnimationSet animationSet = new AnimationSet(true);
//        animationSet.addAnimation(scaleAnimation);
//        animationSet.addAnimation(alphaAnimation);
//        mAnimationSet = animationSet;
//
//        if (StringUtils.isNotEmpty(homePOI))
//            homePoint = MapUtil.getPointFromString(homePOI);
//        if (StringUtils.isNotEmpty(companyPOI))
//            companyPoint = MapUtil.getPointFromString(companyPOI);
//        if (StringUtils.isNotEmpty(favoritePOI))
//            favoritePoint = MapUtil.getPointFromString(favoritePOI);
//        if (StringUtils.isNotEmpty(favoritePOIs))
//            favoritePoints = MapUtil.getPointsFromString(favoritePOIs);
//        if ( MapUtil.isSelectedApp() ) {
//            if (MapUtil.isUsingRegHome()) {
//                if (StringUtils.isEmpty(homePOI)) {
//                    // btnHome.setEnabled(false);
//                    btnHome.setBackground(ContextCompat.getDrawable(this, R.drawable.button_gray));
//                }
//                if (StringUtils.isEmpty(companyPOI)) {
//                    // btnCompany.setEnabled(false);
//                    btnCompany.setBackground(ContextCompat.getDrawable(this, R.drawable.button_gray));
//                }
//                // if (StringUtils.isEmpty(favoritePOI)) btnDestination.setEnabled(false);
//            }
//        } else {
//            btnHome.setEnabled(false);
//            btnCompany.setEnabled(false);
//            btnDestination.setEnabled(false);
//        }
////        crashlytics.log("OverlayViewService.onStartCommand.init.0013");
//        if (favoritePoints.size() > 0) {
//            lnChip.setVisibility(View.VISIBLE);
//        } else {
//            lnChip.setVisibility(View.GONE);
//        }
//
//        //          naviChip.add(getString(R.string.destination)); // index 2
//        int loop = 0;
//        for (MapPoint poi : favoritePoints) {
////                if (loop >= 3) break;
//            loop++;
//            String name = poi.getName();
//            //                name = name.length() > 10?name.substring(0,10) + "...":name;
//            naviChip.add(name); // index 2~~
//        }
//
////        Log.d(TAG,"onStartCommand.0004");crashlytics.log("OverlayViewService.onStartCommand.init.0014");
//        new ChipCloud.Configure()
//                .chipCloud(chip_cloud)
//                //                .selectedColor( com.adroitandroid.chipcloud.R.color.dark_grey )
//                //                .selectedFontColor(Color.parseColor("#ffffff"))
//                //              .deselectedColor(Color.parseColor("#ff0000"))
//                //                .deselectedFontColor(Color.parseColor("#000000"))78D18CEBAAC3
//                //                .deselectedColor( Color.parseColor("#e1e1e1") )
//                //                .deselectedFontColor(Color.parseColor("#ffffff"))
//                //                .selectedColor(Color.parseColor("#ff00cc"))
//                //                .selectedFontColor(Color.parseColor("#ffffff"))
//                //                .deselectedColor(Color.parseColor("#e1e1e1"))
//                //                .deselectedFontColor(Color.parseColor("#333333"))
//                //                .selectedColor(Color.parseColor("#FF00000"))
//
//                //                .selectedColor(getResources().getColor(R.color.chip_select_color))
//                .selectedFontColor(getResources().getColor(R.color.chip_select_font_color))
//                //                .deselectedColor(getResources().getColor(R.color.chip_deselect_color))
//                .deselectedFontColor(getResources().getColor(R.color.chip_deselect_font_color))
//
//                //                .selectedColor(Color.parseColor("#FFFFFF"))
//                //                .selectedFontColor(Color.parseColor("#ff0000"))
//                //                .deselectedColor(Color.parseColor("#FFFFFF"))
//                //                .deselectedFontColor(Color.parseColor("#ffffff"))
//                //                .selectedColor(Color.parseColor("#FF00000"))
//                .backgroundDrawble(R.drawable.chip_skyblue)
//                .chipGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL)
//                //                .typefaceStyle(Typeface.BOLD)
//                .typefaceStyle(Typeface.NORMAL)
//                .selectTransitionMS(500)
//                .deselectTransitionMS(250)
//                //                .labels(getResources().getStringArray(R.array.demo_array))
//                .labels(Util.getStringArray(naviChip))
//                .mode(ChipCloud.Mode.REQUIRED)
//                //                .mode(ChipCloud.Mode.SINGLE)
//                //                .mode(ChipCloud.Mode.NONE)
//                .allCaps(false)
//                .gravity(ChipCloud.Gravity.LEFT)
//                .textSize(getResources().getDimensionPixelSize(R.dimen.default_textsize))
//                .verticalSpacing(getResources().getDimensionPixelSize(R.dimen.vertical_spacing))
//                .minHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.min_horizontal_spacing))
//                //                    .typeface(Typeface.createFromAsset(getContext().getAssets(), "RobotoSlab-Regular.ttf"))
//
//                .chipListener(new ChipListener() {
//
//                    @Override
//                    public void chipSelected(int index) {
//                        crashlytics.log("chipSelected");
//                        MapPoint mapPoint = favoritePoints.get(index);
//                        MapUtil.runNavi(OverlayViewService.this, mapPoint, true);
//                    }
//
//                    @Override
//                    public void chipDeselected(int index) {
//                        //...
//                    }
//                })
//                .build();
////        crashlytics.log("OverlayViewService.onStartCommand.init.0015");
//        if (naviChip.size() == 0) {
//            lnChip.setVisibility(View.GONE);
//        } else {
//            lnChip.setVisibility(View.VISIBLE);
//        }
////        crashlytics.log("OverlayViewService.onStartCommand.init.0016");
//        if (NaviDestination.DRIVE.equals(P_NAVI_DESTINATION) ) {
//            txtAddress.setText(getString(R.string.drive) + " " + getString(R.string.mode));
//        } else {
//            txtAddress.setText(P_NAME);
//        }
////        crashlytics.log("OverlayViewService.onStartCommand.init.0017");
//        btnSetting.setVisibility(View.GONE);
////            if ( MapUtil.isNaviAutoStart() ) {
//        if ( MapUtil.isSelectedApp() ) {
//            NaviDestination naviDestination = P_NAVI_DESTINATION;
//            if (NaviDestination.NONE.equals(P_NAVI_DESTINATION)) {
//                naviDestination = MapUtil.getNaviDestinationInfo();
//            }
////            crashlytics.log("OverlayViewService.onStartCommand.init.0018");
//            if ( MapUtil.isNaviApp() ) {
//                if ( PackageUtil.isTmap() || MapUtil.isUsingRegHome() ) {
//                    if (NaviDestination.HOME.equals(naviDestination)) {
//                        txtAddress.setText(getString(R.string.navi_to_home));
//                    } else if (NaviDestination.COMPANY.equals(naviDestination)) {
//                        txtAddress.setText(getString(R.string.navi_to_company));
//                    } else if (NaviDestination.DRIVE.equals(naviDestination)) {
//                        txtAddress.setText(getString(R.string.navi_to_start));
//                        // txtAddress.setText(getString(R.string.drive) + " " + getString(R.string.mode));
//                    }
//                } else {
//                    txtAddress.setText(getString(R.string.run_to_start));
//                }
//            } else {
//                txtAddress.setText(getString(R.string.run_to_start));
//            }
////            crashlytics.log("OverlayViewService.onStartCommand.init.0019");
//        } else {
//            txtAddress.setText(getString(R.string.please_select_app));
//        }
////            } else {
////                txtAddress.setText(getString(R.string.navi_to_start));
////                txtAddress.setText(getString(R.string.navi_auto_run_goto_setting) + " ▶︎︎"); // navi_auto_run_goto_setting
////            }
////        crashlytics.log("OverlayViewService.onStartCommand.init.0020");
//        startTime = System.currentTimeMillis();
////            setVisibleInfo(View.GONE);
//        setVisibleSub(View.VISIBLE);
//
////        Log.d(TAG,"onStartCommand.0005");crashlytics.log("OverlayViewService.onStartCommand.init.0021");
////            PackageUtil.isMapCategoryApp(appInfo.packageName)
//        if ( PackageUtil.isTmap() || ( MapUtil.isMapCategoryApp() && MapUtil.isUsingRegHome() ) ) {
//            btnHome.setVisibility(View.VISIBLE);
//            btnCompany.setVisibility(View.VISIBLE);
//        } else {
//            btnHome.setVisibility(View.GONE);
//            btnCompany.setVisibility(View.GONE);
//        }
////        crashlytics.log("OverlayViewService.onStartCommand.init.0022");
//        final Handler handlerCounter = new Handler() {
//            public void handleMessage(Message msg) {
//                double periodSec = Math.ceil((System.currentTimeMillis() - startTime) / 1000) + 1;
//                double remaindSec = (Const.NAVI_OVERLAY_COUNTDOWN_TIME / 1000) - periodSec;
//                String v = "";
//                NumberFormat nf = NumberFormat.getInstance();
////                    if (!P_CLOSE_POPUP) { // 실행케이스 없음.
////                        try {
////                            v = getString(R.string.guide_start) + "(" + (remaindSec < 10 ? "" : "") + nf.parse(String.valueOf(remaindSec)).intValue() + ")";
////                        } catch (ParseException e) {
////                            e.printStackTrace();
////                        }
////                        if (btnStart != null) btnStart.setText(v);
////                    } else {
//                try {
//                    try {
//                        v = getString(R.string.guide_close) + "(" + (remaindSec < 10 ? "" : "") + nf.parse(String.valueOf(remaindSec)).intValue() + ")";
//                    } catch (java.text.ParseException e) {
//                        throw new RuntimeException(e);
//                    }
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//                if (btnClose != null) btnClose.setText(v);
////                    }
//            }
//        };
////        crashlytics.log("OverlayViewService.onStartCommand.init.0010");
//        final Handler handlerCounterComplete = new Handler() {
//            public void handleMessage(Message msg) {
//                setVisibleSub(View.GONE);
//                completeCounting(P_CLOSE_POPUP, Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
//                btnClose.setText(getString(R.string.guide_close));
//            }
//        };
//
//        counterTask = new TimerTask() {
//            public void run() {
//                try {
//                    if ((System.currentTimeMillis() - startTime) > Const.NAVI_OVERLAY_COUNTDOWN_TIME) {
//                        if (mTimer != null) mTimer.cancel();
//                        mTimer = null;
//                        new Thread() {
//                            public void run() {
//                                Message msg = handlerCounterComplete.obtainMessage();
//                                handlerCounterComplete.sendMessage(msg);
//                            }
//                        }.start();
//                    } else {
//                        Message msg = handlerCounter.obtainMessage();
//                        handlerCounter.sendMessage(msg);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//
////        crashlytics.log("OverlayViewService.onStartCommand.init.0011");
//        if ( !isBind ) {
//            if (!NaviDestination.NONE.equals(P_NAVI_DESTINATION)) {
//                if (!P_IMMEDIATE) {
//                    if (P_NAVI_AUTO_START) {
//                        //              txtCounter.setVisibility(View.VISIBLE);
////                        countDownAnimation.start();
////                        if ( MapUtil.isTest() ) {
////                            countDownTimer.start();
////                        } else {
//                            WorkerExeckt.startTimer(txtCounter, (counter == 0 ? 1 : counter), true,(params) -> {
//                                txtCounter.setVisibility(View.GONE);
//                                txtCounter.setText("");
//                                if (P_NAVI_AUTO_START) {
//                                    runNavi(P_NAVI_DESTINATION, P_CLOSE_POPUP);
//                                } else {
//                                    completeCounting(P_CLOSE_POPUP, Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
//                                }
//
//                                WorkQueue.nextTask();
//                                return null;
//                            });
////                        }
//                    } else {
//                        txtCounter.setVisibility(View.GONE);
//                        this.mTimer = new Timer();
//                        this.mTimer.schedule(counterTask, 0L, 1000L);
//                        enabled = true;
//                    }
//                } else {
//                    hide(P_CLOSE_POPUP, Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
//                    onClick_btnClose(btnClose);
//                    runNavi(P_NAVI_DESTINATION, P_CLOSE_POPUP);
//                }
//            } else {
//                setVisibleSub(View.GONE);
//                txtCounter.setVisibility(View.GONE);
//                hide(P_CLOSE_POPUP, Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
//                enabled = true;
//            }
//
//            if (P_EXPAND_SUB) {
//                setVisibleSub(View.VISIBLE);
//            }
//        } else {
//
//        }
//        crashlytics.log("OverlayViewService.onStartCommand.init.End");
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
////        if ( countDownAnimation != null) countDownAnimation.cancel();
//
////        if ( MapUtil.isTest() ) {
////            if (countDownTimer != null) countDownTimer.cancel();
////        } else {
//            WorkerExeckt.cancelTimer();
////        }
//        mContext = null;
//        if ( this.mTimer != null ) this.mTimer.cancel();
//        if (onTopView != null) {
//            manager.removeView(onTopView);
//            onTopView = null;
//        }
//        isForeground = false;
//    }
//
//    public static boolean isInstanceCreated() {
//        return mContext != null;
//    }
//
//    float xpos = 0;
//    float ypos = 0;
//    private float downX;
//    private float downY;
//    private final float SCROLL_THRESHOLD = 100;
//    private boolean isOnClick;
//
//    public void setVisibleSub(int visible) {
//        if ( lnSub != null ) lnSub.setVisibility(visible);
//        if ( lnChip != null ) lnChip.setVisibility(visible);
//        if ( btnArrow != null ) {
//            if (visible == View.VISIBLE) {
//                btnArrow.setVisibility(View.VISIBLE);
//                btnArrow.setImageResource(R.drawable.ic_baseline_keyboard_double_arrow_left_24);
//            } else {
//                btnArrow.setVisibility(View.GONE);
//                btnArrow.setImageResource(R.drawable.ic_baseline_keyboard_double_arrow_right_24);
//            }
//        }
//    }
//
//    @Override
//    public boolean onTouch(View view, MotionEvent motionEvent) {
//        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//
//        int action = motionEvent.getAction();
//        int pointerCount = motionEvent.getPointerCount();
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                if (pointerCount == 1) {
//                    xpos = motionEvent.getRawX();
//                    ypos = motionEvent.getRawY();
//                    downX = motionEvent.getX();
//                    downY = motionEvent.getY();
//                    isOnClick = true;
//                }
//                break;
//            case MotionEvent.ACTION_CANCEL:
//            case MotionEvent.ACTION_UP:
//                if (pointerCount == 1) {
//                    WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_OVERLAY_POPUP_X, saveX);
//                    WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_OVERLAY_POPUP_Y, saveY);
//                    if (isOnClick) {
//                        Log.d(TAG, "onClick ");
////                        if ( !this.animing && this.enabled ) {
//                        if ( this.enabled ) {
//                            //TODO onClick code
//                            int visible = lnSub.getVisibility();
//                            if ( visible == View.VISIBLE ) {
//                                setVisibleSub(View.GONE);
//                            } else {
//                                setVisibleSub(View.VISIBLE);
//                            }
//                        } else {
//                            setEnable(true);
//                        }
//                    } else {
//                        hide(P_CLOSE_POPUP,Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
//                    }
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if (pointerCount == 1) {
//                    WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
//                    float dx = xpos - motionEvent.getRawX();
//                    float dy = ypos - motionEvent.getRawY();
//                    xpos = motionEvent.getRawX();
//                    ypos = motionEvent.getRawY();
//
////                    Log.d(TAG,(Math.abs(downX - motionEvent.getX())) + " / " + (Math.abs(downY - motionEvent.getY()))+ " / lp.x : " + lp.x + ", dx : " + dx + "lp.y : " + lp.y + ", dy : " + dy);
//
//                    saveX = (long) (lp.x - dx);
//                    saveY = (long) (lp.y - dy);
//                    lp.x = (int)saveX;
//                    lp.y = (int)saveY;
//                    manager.updateViewLayout(view,lp);
//
//                    if (isOnClick && (Math.abs(downX - motionEvent.getX()) > SCROLL_THRESHOLD || Math.abs(downY - motionEvent.getY()) > SCROLL_THRESHOLD)) {
////                        Log.i(TAG, "movement detected");
//                        isOnClick = false;
//                    }
//
//                    setEnable(true);
//                    return true;
//                }
//                break;
//        }
//        return false;
//    }
//    long saveX,saveY;
//    // toast
////    public void toast( String value ) {
////        Toast.makeText(this, value, Toast.LENGTH_LONG).show();
////    }
//
//    @Override
//    public void onCountDownEnd(CountDownAnimation animation) {
//        countDownAnimation.cancel();
//        txtCounter.setVisibility(View.GONE);
//        if ( P_NAVI_AUTO_START ) {
//            runNavi(P_NAVI_DESTINATION,P_CLOSE_POPUP);
//        }
//        else {
//            completeCounting(P_CLOSE_POPUP,Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
//        }
//
//        WorkQueue.nextTask();
//    }
//
//    private void runNavi(NaviDestination naviDestination, boolean closePopup) {
////        countDownAnimation.cancel();
////        if ( MapUtil.isTest() ) {
////            if (countDownTimer != null) countDownTimer.cancel();
////        } else {
//            WorkerExeckt.cancelTimer();
////        }
//        if ( this.mTimer != null ) this.mTimer.cancel();
//        mTimer = null;
//        if( NaviDestination.BASIC.equals(naviDestination)) {
//            MapPoint mapPoint = new MapPoint(P_NAME,P_LAT,P_LON);
//            MapUtil.runNavi(this, mapPoint, true);
//        } else {
//            MapUtil.runNavi(this, naviDestination, true);
//        }
//        btnClose.setText(getString(R.string.guide_close));
//        /**
//         * call disabled
//         */
////        MapUtil.setCallDisableForNavi();
//        if (closePopup) {
//            hide(closePopup);
//        } else {
//            setVisibleSub(View.GONE);
//        }
////        onTopView.requestLayout(); // onMeasure
////        onTopView.invalidate(); // onDraw
////        manager.updateViewLayout(onTopView, getLayoutParam());
//    }
//
//    public void completeCounting(boolean closePopup,int time) {
//        setEnable(false);
//        if ( closePopup ) {
//            hide(true,time);
//        } else {
//            setVisibleSub(View.GONE);
//        }
//
//    }
//
//    public void startAlpha(View view,int anim) {
//        startAlpha(view,anim,Const.NAVI_OVERLAY_BLUR_DURATION_TIME);
//    }
//
//    public void startAlpha(View view,int anim,int duration) {
//        try {
//            if (view instanceof ViewGroup) {
//                //ViewGroup
//                ViewGroup viewGroup = (ViewGroup) view;
//                for (int i = 0; i < viewGroup.getChildCount(); i++) {
//                    startAlpha(viewGroup.getChildAt(i), anim,duration);
//                }
//            } else {
////            startAlpha(view,anim);
//                Animation animation = AnimationUtils.loadAnimation(this.getApplicationContext(), anim);
//                animation.setDuration(duration);
//                if ( view != null ) view.startAnimation(animation);
//            }
//        } catch (Exception ex) {
//        }
//    }
//
//    private static boolean enabled = false;
//    private static boolean animing = false;
//    Handler mHideHandler = new Handler(Looper.getMainLooper());
//    Handler mEnableHandler = new Handler(Looper.getMainLooper());
//    private void setEnable(boolean enabled) {
//        animing = true;
//
//        OverlayViewService.enabled = enabled;
//        if ( lnOverlay != null ) {
//            if (enabled) {
//                Util.setBackground(this, lnOverlay, R.drawable.overlay_white);
//            } else {
//                Util.setBackground(this, lnOverlay, R.drawable.overlay_translate);
//            }
//
//            if (enabled) {
//                startAlpha(lnOverlay, R.anim.alpha_10_to_100, 0);
//            } else {
//                startAlpha(lnOverlay, R.anim.alpha_100_to_10);
//            }
//
//            if (mEnableHandler != null) {
//                mEnableHandler.removeCallbacksAndMessages(null);
//                mEnableHandler = null;
//            }
//
//            if (mHideHandler != null) {
//                mHideHandler.removeCallbacksAndMessages(null);
//                mHideHandler = null;
//            }
//
//            if (enabled) {
//                mEnableHandler = new Handler(Looper.getMainLooper());
//                mEnableHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //                OverlayViewService.enabled = enabled;
//                        animing = false;
//                        if (enabled) {
//                            //                    Util.setBackground(mContext,lnOverlay,R.drawable.overlay_white);
//                        } else {
//                            //                    Util.setBackground(mContext,lnOverlay,R.drawable.overlay_translate);
//                        }
//                        if (enabled) {
//                            hide(P_CLOSE_POPUP, Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
//                        }
//                    }
//                }, 3000);// 60000
//            }
//        }
//    }
//
//    public void show() {
//        setVisibleSub(View.VISIBLE);
//    }
//    public void hide() {
//        hide(false,0);
//    }
//    public void hide(boolean closePopup) {
//        hide(closePopup,0);
//    }
//    private void hide(boolean closePopup,int time) {
//
//        if ( mEnableHandler != null ) {
//            mEnableHandler.removeCallbacksAndMessages(null);
//            mEnableHandler = null;
//        }
//
//        if ( mHideHandler != null ) {
//            mHideHandler.removeCallbacksAndMessages(null);
//        }
//        mHideHandler = new Handler(Looper.getMainLooper());
//        mHideHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
////                if ( countDownAnimation != null) countDownAnimation.cancel();
////                if ( MapUtil.isTest() ) {
////                    if (countDownTimer != null) countDownTimer.cancel();
////                } else {
//                    WorkerExeckt.cancelTimer();
////                }
//                setVisibleSub(View.GONE);
//                setEnable(false);
//
//                if(closePopup) onClick_btnClose(btnClose);
//            }
//        }, time);
//    }
//}
//
