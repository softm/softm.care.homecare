package net.softm.data.kata;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
import static net.softm.data.kata.util.OverlayObserverViewUtil.STOP_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.play.core.appupdate.AppUpdateManager;
import com.skyfishjy.library.RippleBackground;

import net.softm.data.kata.common.Const;
import net.softm.data.kata.kt.Timerkt;
import net.softm.data.kata.location.StopLocationListener;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.NotificationUtil;
import net.softm.data.kata.util.OverlayObserverViewUtil;
import net.softm.data.kata.util.PermissionUtil;
import net.softm.data.kata.util.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Optional;

//@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class OverlayObserverViewService extends Service implements View.OnTouchListener, StopLocationListener.StopDetectedCallback {

    String TAG = "OverlayObserverViewService";
    private static OverlayObserverViewService mContext;
    MainActivity mActivity;
    private View onTopView;
    private WindowManager manager;
    boolean P_CLOSE_POPUP = true; // OVERLAY 닫기

    @BindView(R.id.lnOverlay)@Nullable
    ACSFrameLayout lnOverlay = null;

    @BindView(R.id.lnSub)@Nullable
    LinearLayout lnSub = null;
    @BindView(R.id.btnClose)@Nullable
    Button btnClose = null;
    @SuppressLint("SuspiciousIndentation")
    @OnClick({R.id.btnClose})
    @Optional
    void onClick_btnClose(View v) {
        try {
            if (manager != null && onTopView != null) manager.removeView(onTopView);
            onTopView = null;
            OverlayObserverViewUtil.stopService();
            stopForeground(true); // Remove from foreground
            stopSelf();
        } catch(Exception ex) {
            Fc.i(mContext,TAG,"onClick_btnClose",ex.toString());
        }

    }

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

        saveX = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_OVERLAY_OBSERVER_POPUP_X, -1L);
        saveY = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_OVERLAY_OBSERVER_POPUP_Y, -1L);
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


    @Override
    public void onStopDetected() {
        speedKmh = 0;
        txtInfoRunSpeedLimit.setText(String.valueOf((int) speedKmh));
    }
    @Override
    public void onLocationChanged(Location location) {
        float speed = location.getSpeed(); // m/s 단위로 반환
        // 속도를 km/h로 변환
        speedKmh = speed * 3.6f;
        txtInfoRunSpeedLimit.setText(String.valueOf((int) speedKmh));
        L.d(TAG,"현재 속도: " + speedKmh + " km/h");
        if ( MapUtil.getRunSpeedLimit() <= speedKmh ) {
            doWork();
        }
    }

    public class LocalBinder extends Binder {
        public OverlayObserverViewService getService() {
            return OverlayObserverViewService.this;
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
    Notification notification = null;
    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        L.d(TAG,"onStartCommand");
        mContext = this;
        if (intent != null && intent.getAction() != null && intent.getAction().equals(STOP_SERVICE)) {
            L.d( TAG, "onStartCommand", " -> STOP");
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        String notificationDetails = mContext.getString(R.string.title_noti_runnig_title);

        notification = NotificationUtil.init(this).getObserverNotification(notificationDetails);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(OverlayObserverViewUtil.NOTIFICATION_ID, notification); // point
            L.i(TAG,"onStartCommand","Step-02");
        } else {
            startForeground(OverlayObserverViewUtil.NOTIFICATION_ID, notification,FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK); // point
            L.i(TAG,"onStartCommand","Step-03");
        }

        init(intent,false);
        L.i(TAG,"onStartCommand","Step-04");
        return super.onStartCommand(intent, flags, startId);
    }

    private void doWork() {
        String intentStr = MainConnectReceiver.getAction();
        if ( MainConnectReceiver.isRunSpeed() && Utils.isNotEmpty(intentStr)) {
            Intent intent = new Intent(intentStr);
            intent.putExtra(MainConnectReceiver.INTENT_EXTRA_RUN_SPEED, MainConnectReceiver.INTENT_EXTRA_RUN_SPEED_CLICK_START);
            mContext.sendBroadcast(intent);
            OverlayObserverViewUtil.stopService();
        } else {
            MainConnectReceiver.stopToShowOverlay();
            OverlayObserverViewUtil.stopService();
        }
    }

    private WindowManager.LayoutParams params;
    private Timerkt timerkt = null;

    private LocationManager locationManager;
    private StopLocationListener stopLocationListener;
//    private StopSensorListener stopSensorListener;
//    private SensorManager sensorManager;
//    private Sensor accelerometer;
    private float speedKmh;
    @SuppressLint("MissingPermission")
    private void init(Intent intent, Boolean isBind) {
        manager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Fc.i(mContext,TAG,"init","Step-0001");

//        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.0002");
        if (intent == null) {
            if (manager != null && onTopView != null) manager.removeView(onTopView);
            onTopView = null;
            stopSelf();
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if ( onTopView != null ) { // MidPad 오류로 remvoeView 로직 추가
            manager.removeView(onTopView);
        }

        onTopView = inflater.inflate(R.layout.always_on_top_run_observer_layout, null);

        LinearLayout lnGuide = onTopView.findViewById(R.id.lnGuide);

        onTopView.setOnTouchListener(this);
        manager.addView(onTopView, getLayoutParam());
        ButterKnife.bind(this, onTopView);

        RippleBackground rippleBackground = (RippleBackground) onTopView.findViewById(R.id.content);
        ImageView imageView = (ImageView) onTopView.findViewById(R.id.btnLogo);
        rippleBackground.startRippleAnimation();

        long spendTime = ( System.currentTimeMillis() - MainConnectReceiver.startTimeMillis ) / 1000;
        long rTime = (MapUtil.getRunSpeedLimitTime()*60) - spendTime;
//             rTime = (MapUtil.getRunSpeedLimitTime()) - spendTime;
//        rTime = 3;

        ((TextView) onTopView.findViewById(R.id.app_name)).setText(WApplication.appInfo.name);
        ((ImageView) onTopView.findViewById(R.id.app_logo)).setImageDrawable(WApplication.appInfo.icon);

        timerkt = new Timerkt(rTime,(remainingTime, gubun, timer)->{
            long minutes = remainingTime / 60;
            long seconds = remainingTime % 60;
            txtInfoRunSpeedLimitTime.setText(String.format("%02d:%02d", minutes, seconds));
            txtInfoRunSpeedLimit.setText(String.valueOf((int)speedKmh));
            txtRunSpeedLimitTime.setText(String.valueOf(MapUtil.getRunSpeedLimitTime()));
            txtRunSpeedLimit.setText(String.valueOf(MapUtil.getRunSpeedLimit()));

            String notificationDetails = mContext.getString(R.string.title_noti_runnig_title);
            NotificationManager notiManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notification = NotificationUtil.init(this)
                    .getObserverNotification("A",
                            String.format("%02d:%02d", minutes, seconds),String.valueOf((int) speedKmh),
                            String.valueOf(MapUtil.getRunSpeedLimitTime()),String.valueOf(MapUtil.getRunSpeedLimit())
                    );
            notiManager.notify(OverlayObserverViewUtil.NOTIFICATION_ID, notification);
            if ( MapUtil.getRunSpeedLimit() <= speedKmh ) {
                doWork();
            }
            L.log(mContext,TAG,"Timerkt.tick",String.format("%02d:%02d", minutes, seconds));
            return null;
        },(params)->{
            L.log(mContext,TAG,"Timerkt.complete");
            if ( MapUtil.getRunSpeedLimit() <= speedKmh ) {
                doWork();
            } else {
                MainConnectReceiver.stopToShowOverlay();
                OverlayObserverViewUtil.stopService();
            }
            return null;
        },"ObserverViewService");

        timerkt.run();

        initLocation();

        Fc.i(mContext,TAG,"OverlayViewService.onStartCommand.init.End");
    }

    @SuppressLint("MissingPermission")
    private void initLocation() {
        if ( MapUtil.isRunSpeed() ) { // TODO SOFTM : TEST : 2024.12.18 : 속도 표시
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            stopLocationListener = new StopLocationListener(this);

//            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//            stopSensorListener = new StopSensorListener(this);

            // 위치 업데이트 요청
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, // 업데이트 주기 (1초)
                    1, // 최소 거리 (1미터)
                    stopLocationListener
            );

//            if (accelerometer != null) {
//                sensorManager.registerListener(stopSensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//            }

        } else {
        }
    }

    @BindView(R.id.txtInfoRunSpeedLimitTime)@Nullable
    TextView txtInfoRunSpeedLimitTime = null;

    @BindView(R.id.txtRunSpeedLimitTime)@Nullable
    TextView txtRunSpeedLimitTime = null;

    @BindView(R.id.txtInfoRunSpeedLimit)@Nullable
    TextView txtInfoRunSpeedLimit = null;

    @BindView(R.id.txtRunSpeedLimit)@Nullable
    TextView txtRunSpeedLimit = null;

    @BindView(R.id.btnCancel)@Nullable
    Button btnCancel = null;
    @OnClick({R.id.btnCancel})
    @Optional
    void onClick_btnCancel(View v) {
        Fc.i(mContext,TAG,"onClick_btnCancel");
//        timerkt.stop();
        MainConnectReceiver.stopToShowOverlay();
        OverlayObserverViewUtil.stopService();
        Utils.toast(mContext,R.string.msg_canceled);
    }

    @BindView(R.id.btnStart)@Nullable
    Button btnStart = null;
    @OnClick({R.id.btnStart})
    @Optional
    void onClick_btnStart(View v) {
        Fc.i(mContext,TAG,"onClick_btnStart");
        doWork();
    }

    @BindView(R.id.btnBackGround)@Nullable
    Button btnBackGround = null;
    @OnClick({R.id.btnBackGround})
    @Optional
    void onClick_btnBackGround(View v) {
        Fc.i(mContext,TAG,"onClick_btnBackGround");
        // TODO SOFT :
        manager.removeView(onTopView);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Fc.i(mContext,TAG,"onDestroy");
        mContext = null;
        if (onTopView != null) {
            try {
                manager.removeView(onTopView);
            } catch (Exception ex) {
            }
            onTopView = null;
        }
        if ( timerkt != null ) timerkt.stop();

        if (locationManager != null && stopLocationListener != null) {
            locationManager.removeUpdates(stopLocationListener);
        }
//        if (sensorManager != null) {
//            sensorManager.unregisterListener(stopSensorListener);
//        }

        stopForeground(true);
    }

    float xpos = 0;
    float ypos = 0;
    private float downX;
    private float downY;
    private final float SCROLL_THRESHOLD = 100;
    private boolean isOnClick;

    public void setVisibleSub(int visible) {
        if ( lnSub != null ) lnSub.setVisibility(visible);
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
                    WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_OVERLAY_OBSERVER_POPUP_X, saveX);
                    WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_OVERLAY_OBSERVER_POPUP_Y, saveY);
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

                    return true;
                }
                break;
        }
        return false;
    }
    long saveX,saveY;

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
        onClick_btnClose(btnClose);
        if ( !PermissionUtil.hasAccessibilityPermissions() || !PermissionUtil.isAccessibilityServiceRunning(mContext) ) {
            Utils.goPermission(mContext,"accessibility_reset",null);
        } else {
            Utils.goMain(mContext);
        }
        return true;
    }
}

