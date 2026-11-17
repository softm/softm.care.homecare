package net.softm.data.kata;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;

import net.softm.lib.Util;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.common.StartMode;
import net.softm.data.kata.util.MapUtil;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

//@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class OverlayCommanderService extends Service implements View.OnTouchListener , CountDownAnimation.CountDownListener {

    String TAG = "OverlayCommanderService";
    public static final String STOP_ACTION = "STOP_ACTION";

    private static OverlayCommanderService mContext;
    MainActivity mActivity;
    private View onTopView;
    private WindowManager manager;
    private Timer mTimer;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    long startTime = 0;

    TimerTask counterTask;

    boolean P_BOOT = false;
//    boolean P_REFENCE = false;
    boolean P_CLOSE_POPUP = true; // OVERLAY 닫기
    boolean P_NAVI_AUTO_START = false; // 내비 자동실행
    boolean P_IMMEDIATE = false;
    NaviDestination P_NAVI_DESTINATION = null;
//    String P_NAME = null;
//    float P_LAT = 0.0f;
//    float P_LON = 0.0f;

    @BindView(R.id.txtCounter)@Nullable
    TextView txtCounter = null;

    @BindView(R.id.lnOverlay)@Nullable
    LinearLayout lnOverlay = null;

    @BindView(R.id.lnSub)@Nullable
    LinearLayout lnSub = null;

    @BindView(R.id.txtAddress)@Nullable
    TextView txtAddress = null;
//    void onClick_txtAddress(View v) {
//        toast(P_NAME);
//    }
//    @OnClick({R.id.btnMain})
//    @Optional
//    void onClick_btnMain(View v) {
//        int visible = lnSub.getVisibility();
//        if ( visible == View.VISIBLE ) {
//            lnSub.setVisibility(View.GONE);
//            lnChip.setVisibility(View.GONE);
//        } else {
//            lnSub.setVisibility(View.VISIBLE);
//            lnChip.setVisibility(View.VISIBLE);
//        }
//    }

    @BindView(R.id.btnStart)@Nullable
    Button btnStart = null;
    @OnClick({R.id.btnStart})
    @Optional
    void onClick_btnStart(View v) {
        runNavi(P_CLOSE_POPUP);
    }

    @BindView(R.id.btnCancel)@Nullable
    Button btnCancel = null;
    @OnClick({R.id.btnCancel})
    @Optional
    void onClick_btnCancel(View v) {
        manager.removeView(onTopView);
        onTopView = null;
        stopSelf();
    }

    @BindView(R.id.btnSetting)@Nullable
    ImageButton btnSetting = null;
    @OnClick({R.id.btnSetting})
    @Optional
    void onClick_btnSetting(View v) {
        onClick_btnClose(btnClose);
        Intent i = new Intent(mContext,MainActivity.class); // 메인
        i.putExtra("P_FOWARD_PAGE","SETTING");
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(i);
    }

    @BindView(R.id.btnMain)@Nullable
    ImageView btnMain = null;
    @OnClick({R.id.btnMain})
    @Optional
    void onClick_btnMain(View v) {
        onClick_btnClose(btnClose);
        Intent i = new Intent(mContext,MainActivity.class); // 메인
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if ( !MapUtil.getNaviAutoStart().equals(StartMode.START) ) {
            i.putExtra("P_FOWARD_PAGE","SETTING");
        }
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(i);
    }


//    @BindView(R.id.btnGoNavi)@Nullable
//    Button btnGoNavi = null;
//    @OnClick({R.id.btnGoNavi})
//    @Optional
//    void onClick_btnGoNavi(View v) {
////      onClick_btnClose(btnClose);
//        MapUtil mapUtil = new MapUtil(this);
//        Log.d(TAG, "Map.runTmapRouteForPackage");
//        mapUtil.runTmap(this, null, -1, -1);
//    }

    @BindView(R.id.btnCloseHour)@Nullable
    Button btnCloseHour = null;
    @OnClick({R.id.btnCloseHour})
    @Optional
    void onClick_btnCloseHour(View v) {
    }

    @BindView(R.id.btnClose)@Nullable
    Button btnClose = null;
    @OnClick({R.id.btnClose})
    @Optional
    void onClick_btnClose(View v) {
        if ( this.mTimer != null ) this.mTimer.cancel();
        countDownAnimation.cancel();
//        if ( MapUtil.isClosePopup() ) {
            if (manager != null && onTopView != null) manager.removeView(onTopView);
            onTopView = null;
            stopSelf();
            //stopService(new Intent(this, OverlayViewService.class));
//        } else {
//            btnClose.setText(getString(R.string.guide_close));
//            setVisibleInfo(View.GONE);
//        }

    }

    @BindView(R.id.btnHome)@Nullable
    Button btnHome = null;
    @OnClick({R.id.btnHome})
    @Optional
    void onClick_btnHome(View v) {
        runNavi(false, NaviDestination.HOME ,P_CLOSE_POPUP);
    }

    @BindView(R.id.btnCompany)@Nullable
    Button btnCompany = null;
    @OnClick({R.id.btnCompany})
    @Optional
    void onClick_btnCompany(View v) {
        runNavi(false, NaviDestination.COMPANY ,P_CLOSE_POPUP);
    }

    @BindView(R.id.btnStartNavi)@Nullable
    Button btnDestination = null;
    @OnClick({R.id.btnStartNavi})
    @Optional
    void onClick_btnDestination(View v) {
        runNavi(false, NaviDestination.DRIVE ,P_CLOSE_POPUP);
    }

    private CountDownAnimation countDownAnimation;
    private ArrayList<String> naviChip = new ArrayList<String>();

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
    private AppUpdateManager mAppUpdateManager;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mContext = this;
        manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (intent != null && intent.getAction() != null && intent.getAction().equals(STOP_ACTION)) {
            Log.d(TAG, "onStartCommand -> STOP");
            if ( manager != null && onTopView != null ) manager.removeView(onTopView);
            onTopView = null;
            stopForeground(true);
            stopSelf();
        } else {
            mAppUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
            Task<AppUpdateInfo> appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();

            appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                @Override
                public void onSuccess(AppUpdateInfo appUpdateInfo) {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
//                            && // 유연한 업데이트 사용 시 (AppUpdateType.FLEXIBLE) 사용
//                            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                    ) {
                        onClick_btnMain(null);
                    } else {

                    }
                }
            });

            if (intent == null) {
                if (manager != null && onTopView != null) manager.removeView(onTopView);
                onTopView = null;
                stopSelf();
                return startId;
            }

            WApplication.mInstance.mGeofencingClient = LocationServices.getGeofencingClient(this);

            P_BOOT = intent.getBooleanExtra("P_BOOT", false);
            P_CLOSE_POPUP = intent.getBooleanExtra("P_CLOSE_POPUP", false);
            P_NAVI_AUTO_START = intent.getBooleanExtra("P_NAVI_AUTO_START", false);
            P_IMMEDIATE = intent.getBooleanExtra("P_IMMEDIATE", false);
            P_NAVI_DESTINATION = (NaviDestination) intent.getSerializableExtra("P_NAVI_DESTINATION");

            Log.d(TAG, "P_BOOT : " + P_BOOT);
            Log.d(TAG, "P_CLOSE_POPUP : " + P_CLOSE_POPUP);
            Log.d(TAG, "P_NAVI_AUTO_START : " + P_NAVI_AUTO_START);
            Log.d(TAG, "P_IMMEDIATE : " + P_IMMEDIATE);
            Log.d(TAG, "P_NAVI_DESTINATION : " + P_NAVI_DESTINATION);

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //        P_CLOSE_POPUP = true;
            //        if ( !P_CLOSE_POPUP) {
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
            //        }

            onTopView.setOnTouchListener(this);

            manager.addView(onTopView, getLayoutParam());
            ButterKnife.bind(mContext, onTopView);
            //        btnMain.setOnTouchListener(this);

            if (!P_CLOSE_POPUP) {
                Util.setBackground(this, btnStart, R.drawable.button_default);
                Util.setBackground(this, btnCancel, R.drawable.button_gray);
                btnStart.setTextColor(getResources().getColor(R.color.white));
                btnCancel.setTextColor(getResources().getColor(R.color.black));
            } else {
                //            Util.setBackground(this,btnStart,R.drawable.button_gray);
                //            Util.setBackground(this,btnCancel,R.drawable.button_default);
                //            btnStart.setTextColor(getResources().getColor(R.color.black));
                //            btnCancel.setTextColor(getResources().getColor(R.color.white));
            }

            countDownAnimation = new CountDownAnimation(txtCounter, (Const.NAVI_OVERLAY_COUNTDOWN_TIME / 1000));
            countDownAnimation.setCountDownListener(this);
            Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            countDownAnimation.setAnimation(scaleAnimation);
            countDownAnimation.setStartCount((Const.NAVI_OVERLAY_COUNTDOWN_TIME / 1000));

            btnSetting.setVisibility(View.GONE);

            startTime = System.currentTimeMillis();
//            setVisibleInfo(View.GONE);
            setVisibleInfo(View.VISIBLE);

            final Handler handlerCounter = new Handler() {
                public void handleMessage(Message msg) {
                    double periodSec = Math.ceil((System.currentTimeMillis() - startTime) / 1000) + 1;
                    double remaindSec = (Const.NAVI_OVERLAY_COUNTDOWN_TIME / 1000) - periodSec;
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
                        v = getString(R.string.guide_close) + "(" + (remaindSec < 10 ? "" : "") + nf.parse(String.valueOf(remaindSec)).intValue() + ")";
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (btnClose != null) btnClose.setText(v);
//                    }
                }
            };

            final Handler handlerCounterComplete = new Handler() {
                public void handleMessage(Message msg) {
                    setVisibleInfo(View.GONE);
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


        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
        if ( this.mTimer != null ) this.mTimer.cancel();
        if (onTopView != null) {
            manager.removeView(onTopView);
            onTopView = null;
        }
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
    public void setVisibleInfo(int visible) {
        lnSub.setVisibility(visible);
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
                        Log.d(TAG, "onClick ");
//                        if ( !this.animing && this.enabled ) {
                        if ( this.enabled ) {
                            //TODO onClick code
                            int visible = lnSub.getVisibility();
                            if ( visible == View.VISIBLE ) {
                                setVisibleInfo(View.GONE);
                            } else {
                                setVisibleInfo(View.VISIBLE);
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

                    Log.d(TAG,(Math.abs(downX - motionEvent.getX())) + " / " + (Math.abs(downY - motionEvent.getY()))+ " / lp.x : " + lp.x + ", dx : " + dx + "lp.y : " + lp.y + ", dy : " + dy);

                    saveX = (long) (lp.x - dx);
                    saveY = (long) (lp.y - dy);
                    lp.x = (int)saveX;
                    lp.y = (int)saveY;
                    manager.updateViewLayout(view,lp);

                    if (isOnClick && (Math.abs(downX - motionEvent.getX()) > SCROLL_THRESHOLD || Math.abs(downY - motionEvent.getY()) > SCROLL_THRESHOLD)) {
                        Log.i(TAG, "movement detected");
                        isOnClick = false;
                    }
                    return true;
                }
                break;
        }
        return false;
    }
    long saveX,saveY;
    // toast
    public void toast( String value ) {
        Toast.makeText(this, value, Toast.LENGTH_LONG).show();
    }

    void runNavi(boolean closePopup) {
        runNavi(MapUtil.isUISupport(), P_NAVI_DESTINATION, closePopup,false);
//      runNavi(MapUtil.isUISupport(), P_NAVI_DESTINATION, null, -1, -1, cancel); // uiSupport == false
    }

    void runNavi(boolean uiSupport, NaviDestination naviDestination, boolean closePopup) {
        runNavi(uiSupport,naviDestination, closePopup, false);
    }

    void runNavi(boolean uiSupport, NaviDestination naviDestination, boolean closePopup, boolean refence) {
        countDownAnimation.cancel();
        if ( this.mTimer != null ) this.mTimer.cancel();
        mTimer = null;
        MapUtil mapUtil = new MapUtil(this);
//        if (!cancel) { // 팝업시 취소로 호출되면 네비 실행안하게
            if (NaviDestination.HOME.equals(naviDestination)) { // 집
                Log.d(TAG, "Map.runTmapGoHome");
                mapUtil.runToHome();
            } else if (NaviDestination.COMPANY.equals(naviDestination)) { // 회사
                Log.d(TAG, "Map.runTmapGoCompany");
                mapUtil.runToCompany();
            } else if (NaviDestination.DRIVE.equals(naviDestination)) { // 운전
                Log.d(TAG, "Map.runTmapSafeDrive");
                mapUtil.runToDrive();
            }
            Log.d(TAG, "closePopup : " + String.valueOf(closePopup));
//        }

        btnClose.setText(getString(R.string.guide_close));
        /**
         * call disabled
         */
//        MapUtil.setCallDisableForNavi();
        if (closePopup) {
            hide(closePopup);
        } else {
            setVisibleInfo(View.GONE);
        }
//        onTopView.requestLayout(); // onMeasure
//        onTopView.invalidate(); // onDraw
//        manager.updateViewLayout(onTopView, getLayoutParam());
    }

    @Override
    public void onCountDownEnd(CountDownAnimation animation) {
        countDownAnimation.cancel();
        txtCounter.setVisibility(View.GONE);
        if ( P_NAVI_AUTO_START ) runNavi(P_CLOSE_POPUP);
        else {
            completeCounting(P_CLOSE_POPUP,Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
        }
    }

    public void completeCounting(boolean closePopup,int time) {
        setEnable(false);
        if ( closePopup ) {
            hide(true,time);
        } else {
            setVisibleInfo(View.GONE);
        }
    }


    public static void startAlpha(View view,int anim) {
        startAlpha(view,anim,Const.NAVI_OVERLAY_BLUR_DURATION_TIME);
    }

    public static void startAlpha(View view,int anim,int duration) {
        try {
            if (view instanceof ViewGroup) {
                //ViewGroup
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    startAlpha(viewGroup.getChildAt(i), anim,duration);
                }
            } else {
//            startAlpha(view,anim);
                Animation animation = AnimationUtils.loadAnimation(mContext.getApplicationContext(), anim);
                animation.setDuration(duration);
                if ( view != null ) view.startAnimation(animation);
            }
        } catch (Exception ex) {
        }
    }

    private static boolean enabled = false;
    private static boolean animing = false;
    Handler mHideHandler = new Handler(Looper.getMainLooper());
    private void setEnable(boolean enabled) {
        animing = true;
        OverlayCommanderService.enabled = enabled;
        if ( enabled ) {
            Util.setBackground(mContext,lnOverlay,R.drawable.overlay_white);
        } else {
            Util.setBackground(mContext,lnOverlay,R.drawable.overlay_translate);
        }
        if ( enabled ) {
            startAlpha(lnOverlay,R.anim.alpha_10_to_100,0);
        } else {
            startAlpha(lnOverlay,R.anim.alpha_100_to_10);
        }

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                OverlayViewService.enabled = enabled;
                animing = false;
                if ( enabled ) {
//                    Util.setBackground(mContext,lnOverlay,R.drawable.overlay_white);
                } else {
//                    Util.setBackground(mContext,lnOverlay,R.drawable.overlay_translate);
                }
                if ( enabled ) {
                    hide(P_CLOSE_POPUP,Const.NAVI_OVERLAY_CLOSE_WAIT_TIME);
                }
            }
        }, 3000);// 60000
    }

    private void hide() {
        hide(false,0);
    }
    private void hide(boolean closePopup) {
        hide(closePopup,0);
    }
    private void hide(boolean closePopup,int time) {
        mHideHandler.removeCallbacksAndMessages(null);
        mHideHandler = new Handler(Looper.getMainLooper());
        mHideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                countDownAnimation.cancel();
                setVisibleInfo(View.GONE);
                setEnable(false);
                if(closePopup) onClick_btnClose(btnClose);
            }
        }, time);
    }
}

