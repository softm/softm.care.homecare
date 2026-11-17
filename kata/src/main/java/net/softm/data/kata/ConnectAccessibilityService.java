package net.softm.data.kata;

import static android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED;
import static android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED;
import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_DISCONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_DISCONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_WIFI_DISCONNECTED;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_ACCESSIBILITY_BUTTON;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_ACCESSIBILITY_BUTTON_CHOOSER;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_BACK;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_HOME;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_KEYCODE_HEADSETHOOK;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_LOCK_SCREEN;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_NOTIFICATIONS;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_POWER_DIALOG;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_QUICK_SETTINGS;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_RECENTS;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_TAKE_SCREENSHOT;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN;

import android.accessibilityservice.AccessibilityGestureEvent;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
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
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.skyfishjy.library.RippleBackground;

import net.softm.data.kata.common.AdLoadType;
import net.softm.data.kata.common.AdType;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.AdViewOPType;
import net.softm.data.kata.common.OrientationType;
import net.softm.data.kata.common.ReceiverType;
import net.softm.data.kata.common.RunnableArg;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.kt.Callbackkt;
import net.softm.data.kata.kt.Timerkt;
import net.softm.data.kata.kt.WorkerExeckt;
import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.LicenseUtil;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.Utils;
import net.softm.data.kata.util.WaitOverlayViewUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectAccessibilityService extends android.accessibilityservice.AccessibilityService  implements View.OnTouchListener {
    static String TAG = "ConnectAccessibilityService";
//    public static final String TAG_RESTART = "ConnectAccessibilityService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    MainConnectReceiver mainConnectReceiver = new MainConnectReceiver();
    ScreenReceiver screenReceiver = new ScreenReceiver();

    BluetoothReceiver bluetoothReceiver = new BluetoothReceiver();
    BatteryReceiver batteryReceiver = new BatteryReceiver();
//    UsbReceiver usbReceiver = new UsbReceiver();
    WIFIReceiver WIFIReceiver = new WIFIReceiver();
//    private Thread mainThread;
    public static ConnectAccessibilityService mContext = null;
//    AccessibilityNodeInfo eventSource = null;

//    private static boolean isAvaliableClick = true;

//    private static boolean isControlSettingsCompleted = true;
//    private static boolean isControlSettingsAvaliableClick = true;
    static int waitTimeSec = 5;
    static int execTimeSec = 0;
    static AcsUtil acsUtil = null;

    private void performGlobalActions(String eventGubun) {
        if      ( KEY_GLOBAL_ACTION_BACK.equals(eventGubun)                         ) performGlobalAction(GLOBAL_ACTION_BACK);
        else if ( KEY_GLOBAL_ACTION_HOME.equals(eventGubun)                         ) performGlobalAction(GLOBAL_ACTION_HOME);
        else if ( KEY_GLOBAL_ACTION_RECENTS.equals(eventGubun)                      ) performGlobalAction(GLOBAL_ACTION_RECENTS);
        else if ( KEY_GLOBAL_ACTION_NOTIFICATIONS.equals(eventGubun)                ) performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
        else if ( KEY_GLOBAL_ACTION_QUICK_SETTINGS.equals(eventGubun)               ) performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS);
        else if ( KEY_GLOBAL_ACTION_POWER_DIALOG.equals(eventGubun)                 ) performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
        else if ( KEY_GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN.equals(eventGubun)          ) performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
        else if ( KEY_GLOBAL_ACTION_LOCK_SCREEN.equals(eventGubun)                  ) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ) {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
//            } else {
//            }
        }
        else if ( KEY_GLOBAL_ACTION_TAKE_SCREENSHOT.equals(eventGubun)              ) performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
        else if ( KEY_GLOBAL_ACTION_KEYCODE_HEADSETHOOK.equals(eventGubun)          ) performGlobalAction(GLOBAL_ACTION_KEYCODE_HEADSETHOOK);
        else if ( KEY_GLOBAL_ACTION_ACCESSIBILITY_BUTTON.equals(eventGubun)         ) performGlobalAction(GLOBAL_ACTION_ACCESSIBILITY_BUTTON);
        else if ( KEY_GLOBAL_ACTION_ACCESSIBILITY_BUTTON_CHOOSER.equals(eventGubun) ) performGlobalAction(GLOBAL_ACTION_ACCESSIBILITY_BUTTON_CHOOSER);
        else if ( KEY_GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT.equals(eventGubun)       ) performGlobalAction(GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT);
        else if ( KEY_GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS.equals(eventGubun)       ) performGlobalAction(GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS);
        else if ( KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE.equals(eventGubun)   ) {
//            performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // Check if the API level is supported
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ) {
                performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
            } else {
                try {
                    @SuppressLint("WrongConstant")
                    Object statusBarService = getSystemService(Context.STATUS_BAR_SERVICE); // Get the StatusBarManager service
                    if (statusBarService != null) {
                        Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
                        Method collapsePanels = statusBarManager.getMethod("collapsePanels"); // Get the collapsePanels method
                        collapsePanels.invoke(statusBarService); // Invoke the method to close the Quick Settings panel
                    }
                } catch (Exception e) {
                    Fc.i(mContext, TAG, KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
                    performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
                }
            }
//            }

        }
    }
    static Handler countDownHandler = null;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        mContext = this;
        String eventKey = "";
        String eventGubun = "";
        try { // AcsUtil.findNodesByText(this.getRootInActiveWindow(),"음악")
            eventGubun = event.getContentDescription() == null ? "" : event.getContentDescription().toString();
        } catch (Exception ex) {
        }
//        acsUtil.findNodeByDescription(getRootInActiveWindow(),"Directions")
//        acsUtil.findNodeByDescription(getRootInActiveWindow(),"안전운행")
//        acsUtil.findNodeByText(getRootInActiveWindow(),"Directions")
//        android.widget.ImageButton
        if ( Utils.isEmpty(eventGubun) ) return;
        if ( AcsUtil.UNCAUGHT_EXCEPTION.equals(eventGubun) ) {
            stopSelf();
            String nu = null;
            nu.charAt(0);
            throw new RuntimeException("This is a crash");
        }
        Fc.i(mContext,TAG, "onAccessibilityEvent", eventGubun);
        try {
//            L.d(TAG,"eventGubun", eventGubun);
//            Log.d("Catch-" + TAG, "Catch Event Package Name : " + event.getPackageName());crashlytics.log("Catch Event Package Name : " + event.getPackageName());
//            Log.d("Catch-" + TAG, "Catch Event TEXT : " + event.getText());crashlytics.log("Catch Event TEXT : " + event.getText());
//            Log.d("Catch-" + TAG, "Catch Event Event : " + eventGubun);crashlytics.log("Catch Event Event : " + eventGubun);
//            Log.d("Catch-" + TAG, "Catch Event getSource : " + event.getSource());crashlytics.log("Catch Event getSource : " + event.getSource());
//            Log.d("Catch-" + TAG, "Catch Event eventType : " + event.getEventType());crashlytics.log("Catch Event eventType : " + event.getEventType());
//            Log.d("Catch-" + TAG, "Catch Event getViewIdResourceName : " + ( event.getSource() != null ? event.getSource().getViewIdResourceName() : "null") );crashlytics.log("Catch Event getViewIdResourceName : " + ( event.getSource() != null ? event.getSource().getViewIdResourceName() : "null") );

            AccessibilityNodeInfo ani = event.getSource();
            if (ani != null) {
                ani.refresh(); // to fix issue with viewIdResName = null on Android 6+
            }

            if ( eventGubun.startsWith(AcsUtil.PREFIX_EVENT) ) {
                List<CharSequence> keys = event.getText();
                AccessibilityNodeInfo nodeInfo = event.getSource();
                eventKey = keys.size() > 0 ? keys.get(0).toString() : "";

                if (AcsUtil.REGISTER_QUEUE.equals(eventKey)) {
                    L.d(TAG, "register : " + eventGubun);
                    acsUtil.register(eventGubun, event); // eventCallbackQueues 등록
                }

                if (AcsUtil.TURN_ON_SCREEN.equals(eventGubun)) {
                    Utils.turnOnScreen();
                    turnOnScreen();
                }

                if (AcsUtil.OPEN_OVERLAY_VIEW_SHOWONLY.equals(eventGubun)) {
                    OverlayViewUtil.init(this).openOverlayViewShowOnlyNotClose();
                }

                if (AcsUtil.TURN_OFF_SCREEN.equals(eventGubun)) {
                    Utils.turnOffScreen();
                }

                performGlobalActions(eventGubun);

                handleShowWaitingOverlay(event);
                handleTab(event);
                handleTurnOnScreenSwipe(event);

                handleStopRecents(event);
                handleFastSwipeFoward(event);

                handleReceiver(event);

                performQueuesRunnable(event); // eventCallbackQueues 실행
            } else {
                if ( WUrl.devMode != WUrl.EnumDevMode.RELEASE ) {
                    try {
                        L.d(TAG, "event", event);
                    } catch (Exception ex) {
                        Fc.e(mContext, TAG, "onAccessibilityEvent", ex);
                    }
                }
            }
        } catch ( ClassCastException ex ) { // 텍스트 입력 편집시 발생함 : java.lang.ClassCastException: android.text.SpannableString cannot be cast to java.lang.String
            Fc.e(mContext,TAG,"onAccessibilityEvent",ex);
        } catch ( Exception ex ) {
            Fc.e(mContext,TAG,"onAccessibilityEvent",ex);
        }
    }

    public void performQueuesRunnable(AccessibilityEvent event){
        try {

            if ( acsUtil.execQueues.size() > 0 ) {

                int eventType = event.getEventType();

                boolean exec = false;
                for (String gubun : Set.copyOf(acsUtil.execQueues.keySet())) { // Non-fatal Exception: java.util.ConcurrentModificationException

//                    if ( EVENT_QUICK_SETTINGS_CLICK.equals(gubun) || EVENT_SETTINGS_SWITCH_CLICK.equals(gubun) || KEY_GLOBAL_ACTION_BACK.equals(gubun) || EVENT_FORCE_STOP_CLICK.equals(gubun) ) {
//                        exec = true;
//                    } else {
//
//                    }
                    if (
// (
/** EVENT_FORCE_STOP_DIALOG_OK_CLICK 이벤트 처리를 접근성서비스 내부에서 안함으로 사용안함 */
//                            AcsUtil.EVENT_FORCE_STOP_DIALOG_OK_CLICK.equals(gubun)
/** EVENT_FORCE_STOP_CLICK 이벤트 처리를 접근성서비스 내부에서 안함으로 사용안함 */
//                                    || AcsUtil.EVENT_FORCE_STOP_CLICK.equals(gubun)
//                                    || AcsUtil.KEY_GLOBAL_ACTION_QUICK_SETTINGS.equals(gubun)
// ) &&
                        eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ) {
                        exec = true;
                    } else {
                        exec = true;
                    }
                    if ( !exec ) return;
                    ExecutorService executorService = Executors.newSingleThreadExecutor();

                    BlockingQueue<RunnableArg> queue = acsUtil.execQueues.get(gubun);
                    acsUtil.execQueues.remove(gubun);
                    if (!queue.isEmpty()) {
//                        executorService.execute(new Runnable() {
//                            @Override
//                            public void run() {
                                RunnableArg task = null;
                                Runnable taskRunnable = null;
                                try {
                                    if (queue.peek() instanceof RunnableArg) {
                                        String eventGubun = event.getContentDescription() == null ? "" : event.getContentDescription().toString();
                                        task = queue.take();
                                        task.setPackageName(event.getPackageName());
                                        task.setText(event.getText());
                                        task.setContentDescription(eventGubun);
                                        task.setSource(event.getSource());
                                        task.setEventType(event.getEventType());
                                        Thread thread = new Thread(task);
                                        thread.start();
                                        Log.d(TAG, "RunnableArg.callBack(" + gubun + ")");
                                        Log.d(TAG, "RunnableArg.callBack.event.getEventType(" + event.getEventType() + ")");
                                        Log.d(TAG, "RunnableArg.callBack.event.getText(" + event.getText() + ")");
                                        Log.d(TAG, "RunnableArg.callBack.event.getContentDescription(" + eventGubun + ")");
                                        Log.d(TAG, "RunnableArg.callBack.event.getSource(" + event.getSource() + ")");
                                    } else {
                                        taskRunnable = queue.take();

                                        Thread thread = new Thread(taskRunnable);
                                        thread.start();
                                        L.d(TAG, "performQueuesRunnable","Runnable.callBack(" + gubun + ")");
                                    }
                                }
                                catch(Exception ex) {
                                    Fc.e(mContext,TAG,"performQueuesRunnable",ex.toString());
                                }
//                            }
//                        });
                    }
                }
            }
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"performQueuesRunnable",ex.toString());
        }
    }

    /**
     * 사용안하고 onServiceConnected 최초에만 리시버 등록함.
     * @param event
     */
    private void handleReceiver(AccessibilityEvent event) {
        try {
            String eventGubun = event.getContentDescription() == null ? "" : event.getContentDescription().toString();
            if (AcsUtil.EVENT_REGISTER_RECEIVER.equals(eventGubun)) {
                registerConnectReceiver();
            }

            if (AcsUtil.EVENT_UNREGISTER_RECEIVER.equals(eventGubun)) {
                unregisterConnectReceiver();
            }
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"handleReceiver",ex.toString());
        }
    }

    private void handleStopRecents(AccessibilityEvent event) {
        try {
            String eventGubun = event.getContentDescription() == null ? "" : event.getContentDescription().toString();
            List<CharSequence> eventTexts = event.getText();
            AccessibilityNodeInfo nodeInfo = event.getSource();
            if (AcsUtil.EVENT_STOP_RECENTS.equals(eventGubun)) {
                Log.d("EVENT_STOP_RECENTS", "EVENT_STOP_RECENTS");
                int orientation = getResources().getConfiguration().orientation;
/** 라인 표시되게
 */
if ( MapUtil.isTest() ) {
    removeLayout();
    manager.addView(mLayoutUnlock, getLayoutParam());
}
                Point point = new Point();
                manager.getDefaultDisplay().getRealSize(point);
                int width = point.x;
                int height = point.y;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Path swipePath = new Path();
                    float startX = 0;
                    float stopX = 0;
                    float startY = 0;
                    float stopY = 0;

/** Previous Task Swipe Gesture */
                    if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ) {
                        // 비정상동작함 : 스와이프가 정상적으로 동작안함... 버전 버그인가?
                        /** Previous task Swipe 실행이 아니라 바로 최근앱 종료 실행
                            // 9 이하 버전 ( 28 이하 ) : 앱 리스트가 수로 배열
                            //                이상은 : 수평 배열
                            // 하단 센터 40% 70% 스와이프로 앱 닫기
                         **/
//                        startX = Double.valueOf(width * Float.valueOf(WApplication.testValue1).floatValue()).floatValue();
//                        stopX = Double.valueOf(width * Float.valueOf(WApplication.testValue2).floatValue()).floatValue();
//                        startY = Double.valueOf(height * Float.valueOf(WApplication.testValue3).floatValue()).floatValue();
//                        stopY = Double.valueOf(height * Float.valueOf(WApplication.testValue4).floatValue()).floatValue();
                        startX = Double.valueOf(width * 0.7).floatValue();
                        stopX = Double.valueOf(width * 0.1).floatValue();
                        startY = Double.valueOf(height * 0.5).floatValue();
                        stopY = Double.valueOf(height * 0.5).floatValue();
                    } else {
                        if (OrientationType.PORTRAIT.equals(Utils.getOrientation(this)) || OrientationType.PORTRAIT_UPSIDEDOWN.equals(Utils.getOrientation(this))) {
                            startX = Double.valueOf(width * 0.8).floatValue();
                            stopX = Double.valueOf(width * 0.2).floatValue();
                            startY = width / 2;
                            stopY = width / 2;
                        } else if (OrientationType.LANDSCAPE_RIGHT.equals(Utils.getOrientation(this))) {
                            startY = Double.valueOf(height * 0.6).floatValue();
                            stopY = Double.valueOf(height * 0.2).floatValue();
                            startX = width / 2;
                            stopX = width / 2;
                        } else if (OrientationType.LANDSCAPE_LEFT.equals(Utils.getOrientation(this))) {
                            startY = Double.valueOf(height * 0.2).floatValue();
                            stopY = Double.valueOf(height * 0.6).floatValue();
                            startX = width / 2;
                            stopX = width / 2;
                        }
                    }
                    swipePath.moveTo(startX, startY);
                    swipePath.lineTo(stopX, stopY);

                    /** 라인 표시되게 - Previous Task Swipe Gesture */
                    if ( MapUtil.isTest() ) {
                        mLayoutUnlock.setStartX(startX);
                        mLayoutUnlock.setStartY(startY);
                        mLayoutUnlock.setEndX(stopX);
                        mLayoutUnlock.setEndY(stopY);
                        mLayoutUnlock.invalidate();
                        WorkerExeckt.testDrawLayout(mLayoutUnlock, 0,(params) -> {
//                            Utils.sleepMicoSeconds(1000);
//                            removeLayout();
                            return null;
                        });
                    }
                    if ( !MapUtil.isTest() ) {
                        /** Previous Task Swipe Gesture - Invoke */
                        GestureDescription.Builder gestureBuilder = null;
                        gestureBuilder = new GestureDescription.Builder();
                        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 300)); // Long.valueOf(WApplication.testValue).longValue()
                        GestureDescription gestureDescription = gestureBuilder.build();
                        dispatchGesture(gestureDescription, null, new Handler(Looper.getMainLooper()));
                    }

                    /** 10이상부터는 수평배열로 종료 제스쳐 구현 **/
                    if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P ) {
                        Utils.sleepMicoSeconds(350);
                        Path swipePath2 = new Path();
                        if (OrientationType.PORTRAIT.equals(Utils.getOrientation(this)) || OrientationType.PORTRAIT_UPSIDEDOWN.equals(Utils.getOrientation(this))) {
    /** Stop Gesture - PORTRAIT */
                            startX = Double.valueOf(width * 0.5).floatValue();
                            startY = Double.valueOf(height * 0.6).floatValue();
                            stopX = startX;
                            stopY = 0;
                        } else if (OrientationType.LANDSCAPE_RIGHT.equals(Utils.getOrientation(this))) {
    /** Stop Gesture - LANDSCAPE */
                            startX = Double.valueOf(width * 0.3).floatValue();
                            stopX = width;
                            startY = height / 2;
                            stopY = height / 2;
                        } else if (OrientationType.LANDSCAPE_LEFT.equals(Utils.getOrientation(this))) {
    /** Stop Gesture - LANDSCAPE */
                            startX = Double.valueOf(width * 0.7).floatValue();
                            stopX = 0;
                            startY = height / 2;
                            stopY = height / 2;
                        }
                        swipePath2.moveTo(startX, startY);
                        swipePath2.lineTo(stopX, stopY);
                        if ( MapUtil.isTest() ) {
    /** 라인 표시되게 - Stop Gesture - Invoke */
                            float finalStartX = startX;
                            float finalStartY = startY;
                            float finalStopX = stopX;
                            float finalStopY = stopY;
                            WorkerExeckt.testDrawLayout(mLayoutUnlock, 1,(params) -> {
                                mLayoutUnlock.setStartX(finalStartX);
                                mLayoutUnlock.setStartY(finalStartY);
                                mLayoutUnlock.setEndX(finalStopX);
                                mLayoutUnlock.setEndY(finalStopY);
                                mLayoutUnlock.invalidate();
//                                Utils.sleepMicoSeconds(500);
//                                removeLayout();
                                return null;
                            });
                        }
                        if ( !MapUtil.isTest() ) {
                            Utils.sleepMicoSeconds(350);
                            GestureDescription.Builder gestureBuilder2 = null;
                            gestureBuilder2 = new GestureDescription.Builder();
                            gestureBuilder2.addStroke(new GestureDescription.StrokeDescription(swipePath2, 0, 300));
                            GestureDescription gestureDescription2 = gestureBuilder2.build();
                            dispatchGesture(gestureDescription2, null, new Handler(Looper.getMainLooper()));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"handleStopRecents",ex.toString());
        }
    }
    private void handleFastSwipeFoward(AccessibilityEvent event) {
        try {
            String eventGubun = event.getContentDescription() == null ? "" : event.getContentDescription().toString();
            List<CharSequence> eventTexts = event.getText();
            AccessibilityNodeInfo nodeInfo = event.getSource();

            if (AcsUtil.GESTURE_FAST_SWIPE_FOWARD.equals(eventGubun)) {
                int orientation = getResources().getConfiguration().orientation;
/** 라인 표시되게
 */
                if ( MapUtil.isTest() ) {
                    removeLayout();
                    manager.addView(mLayoutUnlock, getLayoutParam());
                }
                Point point = new Point();
                manager.getDefaultDisplay().getRealSize(point);
                int width = point.x;
                int height = point.y;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Path swipePath = new Path();
                    float startX = 0;
                    float stopX = 0;
                    float startY = 0;
                    float stopY = 0;

/** Swipe Task Swipe Gesture */
                    if (OrientationType.PORTRAIT.equals(Utils.getOrientation(this)) || OrientationType.PORTRAIT_UPSIDEDOWN.equals(Utils.getOrientation(this))) {
                        startX = Double.valueOf(width * 0.2).floatValue();
                        stopX = Double.valueOf(width * 0.8).floatValue();
                        startY = width / 2;
                        stopY = width / 2;
                    } else if (OrientationType.LANDSCAPE_RIGHT.equals(Utils.getOrientation(this))) {
                        startY = Double.valueOf(height * 0.2).floatValue();
                        stopY = Double.valueOf(height * 0.6).floatValue();
                        startX = width / 2;
                        stopX = width / 2;
                    } else if (OrientationType.LANDSCAPE_LEFT.equals(Utils.getOrientation(this))) {
                        startY = Double.valueOf(height * 0.6).floatValue();
                        stopY = Double.valueOf(height * 0.2).floatValue();
                        startX = width / 2;
                        stopX = width / 2;
                    }
                    swipePath.moveTo(startX, startY);
                    swipePath.lineTo(stopX, stopY);
/** 라인 표시되게 - Previous Task Swipe Gesture */
                    if ( MapUtil.isTest() ) {
                        mLayoutUnlock.setStartX(startX);
                        mLayoutUnlock.setStartY(startY);
                        mLayoutUnlock.setEndX(stopX);
                        mLayoutUnlock.setEndY(stopY);
                        mLayoutUnlock.invalidate();
                        WorkerExeckt.testDrawLayout(mLayoutUnlock, 0,(params) -> {
                            mLayoutUnlock.invalidate();
                            Utils.sleepMicoSeconds(500);
                            removeLayout();
                            return null;
                        });
                    }
                    if ( !MapUtil.isTest() ) {
/** Previous Task Swipe Gesture - Invoke */
                        GestureDescription.Builder gestureBuilder = null;
                        gestureBuilder = new GestureDescription.Builder();
                        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 100));
                        GestureDescription gestureDescription = gestureBuilder.build();
                        dispatchGesture(gestureDescription, null, new Handler(Looper.getMainLooper()));
                    }
                    Utils.sleepMicoSeconds(350);


                }
            }
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"handleFastSwipeFoward",ex.toString());
        }
    }

    private void handleTurnOnScreenSwipe(AccessibilityEvent event) {
        try {
            String eventGubun = event.getContentDescription() == null ? "" : event.getContentDescription().toString();
            List<CharSequence> eventTexts = event.getText();
            AccessibilityNodeInfo nodeInfo = event.getSource();

            if (AcsUtil.GESTURE_SWIPE_TEST.equals(eventGubun)) {

                Path swipePath = new Path();
//                  swipePath.moveTo(startX, startY);
//                  swipePath.lineTo(endX, endY);
                float startX = Float.parseFloat(eventTexts.get(1).toString());
                float startY = Float.parseFloat(eventTexts.get(2).toString());
                float endX = Float.parseFloat(eventTexts.get(3).toString());
                float endY = Float.parseFloat(eventTexts.get(4).toString());
                swipePath.moveTo(startX, startY);
                swipePath.lineTo(endX, endY);

                mLayoutUnlock.setStartX(startX);
                mLayoutUnlock.setStartY(startY);
                mLayoutUnlock.setEndX(endX);
                mLayoutUnlock.setEndY(endY);
                mLayoutUnlock.invalidate();
            }

            if (AcsUtil.GESTURE_DOUBLE_TAP.equals(eventGubun)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    Point point = new Point();
                    manager.getDefaultDisplay().getRealSize(point);
                    int width = point.x;
                    int height = point.y;

                    float startX = width - 100;
                    float startY = height - 300;
                    float endX = width - 100;
                    float endY = height - 900;

                    Point position = new Point((int) startX, (int) startY);
                    GestureDescription.Builder builder = new GestureDescription.Builder();
                    Path p = new Path();
                    Path q = new Path();
//For first finger
                    p.moveTo(position.x, position.y);
                    p.lineTo(position.x, position.y);

//For second finger
                    q.moveTo(position.x, position.y);
                    q.lineTo(position.x, position.y + 300);

//Two strokes for two fingers
                    builder.addStroke(new GestureDescription.StrokeDescription(p, 100L, 50L));
//                builder.addStroke(new GestureDescription.StrokeDescription(p, 200L, 100L));
                    GestureDescription gesture = builder.build();
                    boolean isDispatched = dispatchGesture(gesture, null, null);
                }
            }
            if (AcsUtil.GESTURE_SWIPE.equals(eventGubun)) {
//                Path swipePath = new Path();
//                swipePath.moveTo(2000, 2000);
//                swipePath.lineTo(100, 2000);
//                GestureDescription.Builder gestureBuilder = null;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//                    gestureBuilder = new GestureDescription.Builder();
//                    gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 1000));
//                    dispatchGesture(gestureBuilder.build(), null, null);
//                }

//                private void performSwipeGesture(int startX, int startY, int endX, int endY, int duration) {
                Point point = new Point();
                manager.getDefaultDisplay().getRealSize(point);
                int width = point.x;
                int height = point.y;
//
//            float startX = width-100;
//            float startY = height-300;
//            float endX = width-100;
//            float endY = height-900;

                float startX = Double.valueOf(width * 0.9).floatValue();
                float startY = Double.valueOf(height * 0.7).floatValue();

                float endX = Double.valueOf(width * 0.9).floatValue();
                float endY = Double.valueOf(height * 0.2).floatValue();

                Path swipePath = new Path();
                swipePath.moveTo(startX, startY);
                swipePath.lineTo(endX, endY);
//            startX = 500;
//            startY = 1000;
//            endX = 500;
//            endY = 500;
//            swipePath.moveTo(500, 1000);
//            swipePath.lineTo(500, 500);
/** 라인 표시되게
 mLayoutUnlock.setStartX(startX);
 mLayoutUnlock.setStartY(startY);
 mLayoutUnlock.setEndX(endX);
 mLayoutUnlock.setEndY(endY);
 mLayoutUnlock.invalidate();
 */

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    GestureDescription.Builder gestureBuilder = null;
                    gestureBuilder = new GestureDescription.Builder();
                    gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
                    GestureDescription gestureDescription = gestureBuilder.build();
                    dispatchGesture(gestureDescription, null, new Handler(Looper.getMainLooper()));
                }

                if (MapUtil.isScreenOn(mContext)) {
                    MainConnectReceiver.screenOn();
                }
            }
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"handleTurnOnScreenSwipe",ex.toString());
        }
    }
    public static long showWaitStartTime = -1;

    public void showWaitingOverlay(Context ctx, String action, ReceiverType receiverType, long waitingSec) {
        showWaitStartTime = System.currentTimeMillis();
//        sendEvent(SHOW_WAITING_OVERLAY,null, new String[]{action,receiverType.getId(),String.valueOf(waitingSec)},0);
//        sendEvent(String gubun,String key,String[] texts,long delayTime) {
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
                AcsUtil acsUtil = new AcsUtil(ctx);
                AccessibilityEvent e = AccessibilityEvent.obtain();
                e.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                e.setClassName(ConnectAccessibilityService.class.getName());
                e.setPackageName(ctx.getPackageName());
                e.setContentDescription(AcsUtil.SHOW_WAITING_OVERLAY);
                e.getText().add("SOFTM ACS");
                for (String text : new String[]{action, receiverType.getId(), String.valueOf(waitingSec)}) {
                    if (text != null) e.getText().add(text);
                }
                handleShowWaitingOverlay(e);
//            }
//        },0);

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
    }

    boolean adLoaded = false;
    static Timerkt startWaitTimerkt;
    private void startWaitOverlayTimer(TextView tv) {
            if (startWaitTimerkt!=null) {
                startWaitTimerkt.stop();
            }
            L.d(TAG,"startWaitTimerkt","Start");
            startWaitTimerkt = new Timerkt(waitTimeSec,(remainingTime, gubun, timer)->{
                tv.setText(String.valueOf(remainingTime));
                return null;
            },(params)->{
                L.log(this,TAG,"Timerkt.complete");
                stopWaitOverlay();
                // TODO SOFTM : 광고 : 로직변경 : 접근성에서 실행하도록
                try {
                    MainConnectReceiver.execReceiver(mContext);
                } catch (Exception ex) {
                }
                return null;
            },"startWaitStartTimer");

            startWaitTimerkt.run();

//        }
    }

    @SuppressLint("StringFormatInvalid")
    private void handleShowWaitingOverlay(AccessibilityEvent event) {
        String eventGubun = "";
        try {
            List<CharSequence> eventTexts = null;
            
            try {
                eventGubun = event.getContentDescription() == null ? "" : event.getContentDescription().toString();
                eventTexts = event.getText();
            } catch (Exception ex) {
                Fc.e(mContext,TAG,"handleShowWaitingOverlay",ex.toString());
            } finally {
            }
            //        AccessibilityNodeInfo nodeInfo = event.getSource();

            if (AcsUtil.SHOW_WAITING_OVERLAY.equals(eventGubun)) {
                //            event.getBeforeText()
                //            performGlobalAction(GLOBAL_ACTION_BACK);
                removeLayout();
                initManagerLayout();
                addView(mLayout, getLayoutParamForWaitingOverlay());

                Fc.i(mContext,TAG,"handleShowWaitingOverlay",AcsUtil.SHOW_WAITING_OVERLAY);
                if (event.getText().size() >= 5)
                    waitTimeSec = Integer.parseInt(eventTexts.get(3).toString());
                execTimeSec = waitTimeSec;

                TextView tv = mLayout.findViewById(R.id.info_time_sec);
                tv.setText(String.valueOf(execTimeSec));

                TextView tvStatus2 = mLayout.findViewById(R.id.statu2);
                ((TextView) mLayout.findViewById(R.id.app_name)).setText(WApplication.appInfo.name);
                ((ImageView) mLayout.findViewById(R.id.app_logo)).setImageDrawable(WApplication.appInfo.icon);

                if (eventTexts.get(1).equals(ACTION_SOFTM_POWER_CONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_ACL_CONNECTED) || eventTexts.get(1).equals(MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED)) {
                    tvStatus2.setText(R.string.start);
                    tvStatus2.setTextColor(getResources().getColor(R.color.blue_500));
                    Log.i(TAG, "시작" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
                } else if (eventTexts.get(1).equals(ACTION_SOFTM_POWER_DISCONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_ACL_DISCONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
                    tvStatus2.setText(R.string.end);
                    tvStatus2.setTextColor(getResources().getColor(R.color.red_500));
                    Log.i(TAG, "종료" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
                }

                TextView tvReceiverStatus = mLayout.findViewById(R.id.receiver_status);
                String receiverStatus = ReceiverType.value(eventTexts.get(2).toString()).getDescription();
                tvReceiverStatus.setText(receiverStatus);

                mLayout.setOnTouchListener(ConnectAccessibilityService.this);

                RippleBackground rippleBackground = (RippleBackground) mLayout.findViewById(R.id.content);
                ImageView imageView = (ImageView) mLayout.findViewById(R.id.btnLogo);
                rippleBackground.startRippleAnimation();

                ViewGroup adViewContainer = mLayout.findViewById(R.id.ad_view_container);
/**
 * 광고 설정 --------- 시작
 */
                if ( AdViewOPType.NONE.equals(LicenseUtil.getAdViewOPType()) ) {

                } else {
                    TextView tvAvdMsg = mLayout.findViewById(R.id.tvAvdMsg);
                    TextView tvAvdMsg2 = mLayout.findViewById(R.id.tvAvdMsg2);
                    TextView tvStatus3 = mLayout.findViewById(R.id.status3);

                    LinearLayout lnAvd = mLayout.findViewById(R.id.lnAvd);
                    LinearLayout lnAvdMsgArea = mLayout.findViewById(R.id.lnAvdMsgArea);
//                    LinearLayout lnAvdParent = mLayout.findViewById(R.id.lnAvdParent);

                    final boolean[] canStartTimer = {true};
                    if ( AdViewOPType.ADVIEW_STOP.equals(LicenseUtil.getAdViewOPType()) ) {
                        if ( !LicenseUtil.isLicense() ) {
                            tvAvdMsg.setText(R.string.msg_adview_license_expired);
                            tvAvdMsg2.setText(R.string.msg_adview_license_extend);
                            tvAvdMsg2.setVisibility(View.VISIBLE);
                            tvStatus3.setVisibility(View.GONE); // 초 라벨
//                            tvStatus2.setText(mContext.getString(R.string.license_stop)); // "라이센스 중지됨"
//                            tvStatus2.setTextColor(getResources().getColor(R.color.red));
                            canStartTimer[0] = false;
                        }
                    }

                    /**
                     * 하단 배너 ---- 시작
                     */
                    if ( AdUtil.isAdingWaitBanner() ) {
                        AdView adViewForWaitBanner;
                        if (AdLoadType.INDIRECT.equals(AdUtil.getAdTypeForWaitBannerLoadType())) {
                            adViewForWaitBanner = AdUtil.getWaitBanner();
                            AdUtil.addLoadBanner(adViewContainer, mContext, AdType.INLINE);
                        } else {
                            adViewForWaitBanner = AdUtil.addBanner(adViewContainer, mContext, AdType.INLINE);
                        }
                        adLoaded = false;
                        if (adViewForWaitBanner != null) {
                            tvStatus2.setText(mContext.getString(R.string.loading_ad)); // "광고 로딩중"
                            tvStatus2.setTextColor(getResources().getColor(R.color.green_500));
                            final boolean[] isStarted = {false};
                            if (eventTexts.get(1).equals(ACTION_SOFTM_POWER_CONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_ACL_CONNECTED) || eventTexts.get(1).equals(MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED)) {
                                isStarted[0] = true;
                            } else if (eventTexts.get(1).equals(ACTION_SOFTM_POWER_DISCONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_ACL_DISCONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
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

                                    tv.setText("");

                                    if ( canStartTimer[0] ) startWaitOverlayTimer(tv);

                                    if ( AdViewOPType.ADVIEW_STOP.equals(LicenseUtil.getAdViewOPType()) && !LicenseUtil.isLicense() ) {
                                        tvStatus2.setText(R.string.expiration);
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
                                    if ( canStartTimer[0] ) startWaitOverlayTimer(tv);
                                }
                            });
                        } else {
                            if ( canStartTimer[0] ) startWaitOverlayTimer(tv);
                        }
                    } else {
                        if ( canStartTimer[0] ) startWaitOverlayTimer(tv);
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
                            stopWaitOverlay();
                            // TODO SOFTM : 광고 : 로직변경 : 접근성에서 실행하도록
                            try {
                                MainConnectReceiver.execReceiver(mContext);
                            } catch (Exception ex) {
                            }
                        }
                    });
                    /** 대기시간 + 5초 후에 타이머 동작 안했을 경우 강제 타이머 동작 */
                    Callbackkt callbackkt = new Callbackkt((waitTimeSec*1000) + 5000, "startWaitStartTimer.Check;"); //
                    callbackkt.run((param) -> {
                        if (!adLoaded) {
                            if ( canStartTimer[0] ) startWaitOverlayTimer(tv);
                        }
                        btnContinue.setVisibility(View.VISIBLE);
                        return null;
                    });

                    lnAvd.setVisibility(View.GONE);
                    lnAvdMsgArea.setVisibility(View.GONE);
//                    if ( !AdUtil.hasLicenseByAdView() || AdUtil.isAdViewAvailableForIntervalTime() ) {
//                        lnAvdMsgArea.setVisibility(View.VISIBLE);
//                    }
//                    lnAvdParent.setVisibility(AdUtil.hasLicenseByAdView()?View.GONE:View.VISIBLE);
                    if ( !AdUtil.isAdingWaitReward() ) {
//                        lnAvdParent.setVisibility(View.GONE);
                    }
                    if ( AdViewOPType.ADVIEW_STOP.equals(LicenseUtil.getAdViewOPType()) && !LicenseUtil.isLicense()) {
                        /** 사용기간 만료 **/
                    } else {
                        String adviewUserEarnedReward = null;
                        if (event.getText().size() >= 5)
                            adviewUserEarnedReward = eventTexts.get(4).toString();
                        String doAppReview = null;
                        if (event.getText().size() >= 6)
                            doAppReview = eventTexts.get(5).toString();

                        if (Utils.isNotEmpty(adviewUserEarnedReward)) {
                            //                        showMessageForAdViewUserEarnedReward();
                            //                        LinearLayout lnAvdMsgArea = mLayout.findViewById(R.id.lnAvdMsgArea);
                            lnAvdMsgArea.setVisibility(View.VISIBLE);
                            tvAvdMsg.setText(mContext.getString(R.string.msg_adview_thankyou1));
                            tvAvdMsg.setTextColor(getResources().getColor(R.color.red_500));
                            tvAvdMsg2.setText(mContext.getString(R.string.msg_adview_user_earned_reward));
                            tvAvdMsg2.setTextColor(getResources().getColor(R.color.red_500));
                        } else if (Utils.isNotEmpty(doAppReview)) {
                            //                        showMessageForAdViewUserEarnedReward();
                            //                        LinearLayout lnAvdMsgArea = mLayout.findViewById(R.id.lnAvdMsgArea);
                            lnAvdMsgArea.setVisibility(View.VISIBLE);
                            tvAvdMsg.setText(mContext.getString(R.string.msg_review_thankyou1));
                            tvAvdMsg.setTextColor(getResources().getColor(R.color.red_500));
                            tvAvdMsg2.setVisibility(View.GONE);
                        } else {
                            if (!LicenseUtil.isLicense()) {
                                tvAvdMsg.setText(mContext.getString(R.string.msg_adview_if));
                                tvAvdMsg.setTextColor(getResources().getColor(R.color.red_500));
                                long waitingTime = MapUtil.getPreventWaitOverlayTime();
                                tvAvdMsg2.setText(
                                        Utils.fromHtml(
                                            mContext.getString(R.string.msg_adview_recuce_waittime,
                                                    String.valueOf(WaitOverlayViewUtil.getWaitTimeForNotAdLicense() / 1000),
                                                    String.valueOf(waitingTime / 1000)
                                        )))
                                ;
//                                tvAvdMsg2.setTextColor(getResources().getColor(R.color.black));
                            } else {
                                tvAvdMsg.setText(mContext.getString(R.string.msg_adview_is));
                                tvAvdMsg.setTextColor(getResources().getColor(R.color.black_500));
                                long waitingTime = MapUtil.getPreventWaitOverlayTime();
                                tvAvdMsg2.setText(mContext.getString(R.string.msg_adview_thankyou2));
                                tvAvdMsg2.setTextColor(getResources().getColor(R.color.black_500));
                            }
                        }
                    }
                    if ( AdUtil.isAdingWaitReward() && AdUtil.isAdViewAvailable() ) {
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
                                    MainConnectReceiver.pause();
                            }
                        });
                        /**
                         * 광고가(mRewardedAd)가 로드된 경우 버튼 표시
                         */
                        new Timerkt(20,(remainingTime, gubun, timer)->{
                            if ( AdUtil.getRewardedAd() != null) {
                                lnAvd.setVisibility(View.VISIBLE);
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
//                                Color.parseColor("#d2f0fa"), // 시작 색상
//                                Color.parseColor("#9dd0e0")  // 끝 색상
                        );
                        colorAnimator.setDuration(1500); // 애니메이션 지속 시간
                        colorAnimator.setRepeatCount(ValueAnimator.INFINITE); // 무한 반복
                        colorAnimator.setRepeatMode(ValueAnimator.REVERSE); // 역방향 반복
                        colorAnimator.start();
                    } else {
                        if ( !AdUtil.isAdViewAvailableForIntervalTime() ) {
                            Utils.toastDebug(mContext, "다음 광고 시청시간 까지 :" + Utils.millisecondsToTime(AdUtil.getNextAdviewTime()));
                        } else if ( !AdUtil.isAdViewAvailableForAdMaxTime() ) {
                            Utils.toastDebug(mContext, "다음 광고 광고 시청시간까지 시청이 제한 되었습니다.");
                            L.d(TAG,"광고 시청시간 초과로 시청이 제한 되었습니다.");
                        }
                    }
                }
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
            }

            if (AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS.equals(eventGubun)) {
                TextView tvStatus2 = mLayout.findViewById(R.id.statu2);
                ((TextView) mLayout.findViewById(R.id.app_name)).setText(WApplication.appInfo.name);
                if (eventTexts.get(1).equals(ACTION_SOFTM_POWER_CONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_ACL_CONNECTED) || eventTexts.get(1).equals(MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED)) {
                    tvStatus2.setText(R.string.start);
                    tvStatus2.setTextColor(getResources().getColor(R.color.blue_500));
                    Log.i(TAG, "시작" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
                } else if (eventTexts.get(1).equals(ACTION_SOFTM_POWER_DISCONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_ACL_DISCONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
                    tvStatus2.setText(R.string.end);
                    tvStatus2.setTextColor(getResources().getColor(R.color.red_500));
                    Log.i(TAG, "종료" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
                }

                TextView tvReceiverStatus = mLayout.findViewById(R.id.receiver_status);
                String receiverStatus = ReceiverType.value(eventTexts.get(2).toString()).getDescription();
                tvReceiverStatus.setText(receiverStatus);
            }
        } catch (Exception ex) {
            if (AcsUtil.SHOW_WAITING_OVERLAY.equals(eventGubun)) {
                // 대기오버레이 구동중 에러가 발생했다면
                // 대기오버레이 타이머를 중단하고 이어서 리시버 동작 실행
                if (startWaitTimerkt != null) startWaitTimerkt.stop();
                //            MainConnectReceiver.stop();
                try {
                    MainConnectReceiver.execReceiver(mContext);
                } catch (Exception ex2) {
                }
            }
            Fc.e(mContext,TAG,"handleShowWaitingOverlay",ex.toString());
        }
    }

    public static void showMessageForAdViewUserEarnedReward() {
        if (mLayout!=null && (mLayout.getWindowToken() != null || mLayout.getParent() != null) ) {
            LinearLayout lnAvdMsgArea = mLayout.findViewById(R.id.lnAvdMsgArea);
            lnAvdMsgArea.setVisibility(View.VISIBLE);
            TextView tvAvdMsg = mLayout.findViewById(R.id.tvAvdMsg);
            tvAvdMsg.setText(mContext.getString(R.string.msg_adview_user_earned_reward));
            tvAvdMsg.setTextColor(mContext.getResources().getColor(R.color.a200_orange));
        }
    }

    public static void addView(ACSFrameLayout layout,WindowManager.LayoutParams params) {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//            final DisplayManager dm = mContext.getSystemService(DisplayManager.class);
//            final Display primaryDisplay = dm.getDisplay(DEFAULT_DISPLAY);
//            Context windowContext = mContext.createDisplayContext(primaryDisplay).createWindowContext(TYPE_APPLICATION_OVERLAY, null);
//            params.type = TYPE_APPLICATION_OVERLAY; // TYPE_ACCESSIBILITY_OVERLAY
//            params.flags = WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
//            windowContext.getSystemService(WindowManager.class).addView(layout, params);
//        } else {
            Log.d(TAG,"addView");
            manager.addView(layout, params);
//        }
    }

    public void turnOnScreen() {
        
        Fc.i(mContext,TAG,"turnOnScreen");
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    removeLayout();
                    initManagerLayoutUnlock();
                    manager.addView(mLayoutUnlock, getLayoutParam());
                    Fc.i(mContext, TAG, "turnOnScreen", "Step-1");
                    try {
//                    if (!MapUtil.isTest()) {
//                        mLayoutUnlock.setOnTouchListener(ConnectAccessibilityService.this);
//                    } else { // TODO SOFTM : TEST : 2024.12.18 : unlock 비정상적으로 실행루틴 종료
                        mLayoutUnlock.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                try {
                                    removeLayout();
                                    Utils.toast(ConnectAccessibilityService.this, "UnLock..");
                                } catch (Exception ex) {
                                    Fc.e(mContext, TAG, "removeLayout", "mLayoutUnlock-1");
                                }
                                return true;
                            }
                        });
//                    }
                    } catch (Exception ex) {
                        Fc.e(mContext, TAG, "turnOnScreen", "Step-2");
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
                            } catch (Exception ex) {
                            }
                        }
                    }, waitTimeSec);
                } catch( Exception ex) {
                    Fc.e(mContext,TAG,"turnOnScreen",ex);
                    L.e(TAG,"turnOnScreen",ex);
                }
            }
        },0);

//                                    }
    }

    private static WindowManager manager;
    static long saveX;
    static long saveY;
    public static WindowManager.LayoutParams getLayoutParam(){

        DisplayMetrics metrics = new DisplayMetrics();

        manager.getDefaultDisplay().getMetrics(metrics);
        int LAYOUT_FLAG;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = TYPE_APPLICATION_OVERLAY;
        }else{
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; // TYPE_PHONE
        }
        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;

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

        WindowManager.LayoutParams params = getLayoutParam();

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

    static ACSFrameLayout mLayout;
    static ACSFrameLayout mLayoutUnlock;
    public static ACSFrameLayout getLayout() {
        return mLayout;
    }
    // 접근성 권한을 가지고, 연결이 되면 호출되는 함수
    public void onServiceConnected() {
        mContext = this;
        //Toast.makeText(getApplicationContext(), "reflash", Toast.LENGTH_SHORT).show();
        L.d(TAG, "onServiceConnected"," START");
        // 알림바가 켜져있고 접근성서비스를 이용하므로 일반서비스 Stop
        // if ( MapUtil.isNotiIcon() ) {
//        Log.d(TAG, "ACS-onServiceConnected -> STOP Service-1");
//        Intent in = new Intent(mContext, ConnectService.class);
//        mContext.stopService(in);
//        Log.d(TAG, "ACS-onServiceConnected -> STOP Service-2");

//        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
//
//        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK; // 전체 이벤트 가져오기
////        info.feedbackType = AccessibilityServiceInfo.DEFAULT | AccessibilityServiceInfo.FEEDBACK_HAPTIC;
//        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN | AccessibilityServiceInfo.FEEDBACK_HAPTIC;
//        info.notificationTimeout = 100; // millisecond
//        setServiceInfo(info);


        AccessibilityServiceInfo info   = new AccessibilityServiceInfo();
//        info.packageNames               = new String[]{getApplicationContext().getPackageName()};
        info.eventTypes                 = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType               = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout        = 100;
        info.flags                      = AccessibilityServiceInfo.DEFAULT;
//        android:accessibilityEventTypes="typeAllMask"
//        android:accessibilityFlags="flagDefault"
//        android:accessibilityFeedbackType="feedbackSpoken"
//        android:notificationTimeout="100"
//        android:canRetrieveWindowContent="true"

        setServiceInfo(info);
// 잠시 주석 풀어야함.
//        unregisterConnectReceiver();
        if ( !MapUtil.isNotiIcon() ) {
            registerConnectReceiver();
        }
        initManagerLayout();
    }

    @Override
    public boolean onGesture(@NonNull AccessibilityGestureEvent gestureEvent) {
        return super.onGesture(gestureEvent);
    }

    public void initManagerLayout() {
        manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = (ACSFrameLayout) inflater.inflate(R.layout.always_on_top_acs_layout, null);
        mLayoutUnlock = (ACSFrameLayout) inflater.inflate(R.layout.unlock_empty_layout, null);

        RippleBackground rippleBackground=(RippleBackground)mLayout.findViewById(R.id.content);
        ImageView imageView=(ImageView)mLayout.findViewById(R.id.btnLogo);
        acsUtil = new AcsUtil(this);
    }


    public void initManagerLayoutUnlock() {
        manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutUnlock = (ACSFrameLayout) inflater.inflate(R.layout.unlock_empty_layout, null);
    }

    @Override
    public void onInterrupt() {
        Fc.i(mContext,TAG,"onInterrupt");
        mContext = null;
        try{
            stopWaitOverlay();
            unregisterConnectReceiver();
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"onInterrupt",ex.toString());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        android.os.Debug.waitForDebugger();  // this line is key
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
        try{
            removeLayout();
            unregisterConnectReceiver();
        } catch (Exception ex) {
        } finally {
            Fc.i(mContext,TAG,"onDestroy");
        }
    }
    ConnectivityManager.NetworkCallback wifiCallback;
    private void unregisterConnectReceiver() {
        try{
            unregisterReceiver(this.mainConnectReceiver);
        } catch (Exception ex) {
        }
        try{
            unregisterReceiver(this.screenReceiver);
        } catch (Exception ex) {
        }
        try{
            unregisterReceiver(this.batteryReceiver);
        } catch (Exception ex) {
        }
//        try{
//            unregisterReceiver(this.usbReceiver
//            );
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        try{
            unregisterReceiver(this.bluetoothReceiver);
        } catch (Exception ex) {
        }
        try{
            unregisterReceiver(this.WIFIReceiver);
        } catch (Exception ex) {
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//            connectivityManager.unregisterNetworkCallback(wifiCallback);
//        }
    }
    @SuppressLint("WrongConstant")
    private void registerConnectReceiver() {
        IntentFilter localIntentFilterPower = new IntentFilter();
        localIntentFilterPower.addAction(ACTION_SOFTM_POWER_CONNECTED);
        localIntentFilterPower.addAction(ACTION_SOFTM_POWER_DISCONNECTED);

        localIntentFilterPower.addAction(ACTION_SOFTM_ACL_CONNECTED);
        localIntentFilterPower.addAction(ACTION_SOFTM_ACL_DISCONNECTED);

        localIntentFilterPower.addAction(MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED);
        localIntentFilterPower.addAction(ACTION_SOFTM_WIFI_DISCONNECTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(this.mainConnectReceiver, localIntentFilterPower,RECEIVER_EXPORTED);
        } else { // Below Android 12
            registerReceiver(this.mainConnectReceiver, localIntentFilterPower);
        }

        IntentFilter localIntentFilterScreen = new IntentFilter();
        localIntentFilterScreen.addAction(Intent.ACTION_USER_PRESENT);
        localIntentFilterScreen.addAction(Intent.ACTION_USER_UNLOCKED);
//        localIntentFilterScreen.addAction(Intent.ACTION_SCREEN_ON);
//        localIntentFilterScreen.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(this.screenReceiver, localIntentFilterScreen);

        IntentFilter localIntentFilterBattery = new IntentFilter();
        localIntentFilterBattery.addAction(Intent.ACTION_POWER_CONNECTED);
        localIntentFilterBattery.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(this.batteryReceiver, localIntentFilterBattery);

//        IntentFilter localIntentFilterUsb = new IntentFilter();
//        localIntentFilterUsb.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        localIntentFilterUsb.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        registerReceiver(this.usbReceiver, localIntentFilterUsb);

        IntentFilter localIntentFilterBluetooth = new IntentFilter();
        localIntentFilterBluetooth.addAction(ACTION_ACL_CONNECTED);
        localIntentFilterBluetooth.addAction(ACTION_ACL_DISCONNECTED);
        registerReceiver(this.bluetoothReceiver, localIntentFilterBluetooth);

        IntentFilter localIntentFilterWIFI = new IntentFilter();
        localIntentFilterWIFI.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        localIntentFilterWIFI.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(this.WIFIReceiver, localIntentFilterWIFI);

        Log.d(TAG,"ConnectAccessibilityService >> add Receiver ConnectReceiver");
/*
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            wifiCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
//                    Utils.toast(mContext,"WI-FI 연결됨");
                    MapUtil.getCurrentSsid(mContext);
                    Log.e("onAvailable", "WI-FI 연결됨");
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    MapUtil.getCurrentSsid(mContext);
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
//                    Utils.toast(mContext,"WI-FI 연결 끊김");
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        ConnectivityManager connectivityManager = getApplicationContext().getSystemService(ConnectivityManager.class);
                        NetworkCapabilities netCaps = connectivityManager.getNetworkCapabilities(network);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            try {
                                WifiInfo info = (WifiInfo) netCaps.getTransportInfo();
                            } catch (Exception ex) {
//                                Utils.toast(mContext, ex.toString());
                            }
                        }
//                    String ssid = wifiInfo.getSSID();

                        Log.e("onAvailable", "WI-FI 연결 끊김");
                    }
                }
            };

            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            // Builder 선언
            NetworkRequest.Builder wifiBuilder = new NetworkRequest.Builder();

            // 콜백이 호출되는 조건
            // TRANSPORT_WIFI = WIFI 상태 / TRANSPORT_CELLULAR = Mobile 네트워크 상태
            wifiBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

            // 해당 콜백 등록
            connectivityManager.registerNetworkCallback(wifiBuilder.build(), wifiCallback);

        }*/


    }

    float xpos = 0;
    float ypos = 0;
    private float downX;
    private float downY;
    private final float SCROLL_THRESHOLD = 100;
    private boolean isOnClick;
    private static boolean enabled = false;
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
                                    Utils.toast(ConnectAccessibilityService.this,R.string.msg_exec_stopped);
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

    private void handleTab(AccessibilityEvent event) {
        try {
            String eventGubun = event.getContentDescription() == null ? "" : event.getContentDescription().toString();
            List<CharSequence> eventTexts = event.getText();
            AccessibilityNodeInfo nodeInfo = event.getSource();

            if (AcsUtil.GESTURE_TAP.equals(eventGubun)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    float startX = Float.valueOf(String.valueOf(eventTexts.get(1)));
                    float startY = Float.valueOf(String.valueOf(eventTexts.get(2)));

                    Point position = new Point((int) startX, (int) startY);
                    GestureDescription.Builder builder = new GestureDescription.Builder();
                    Path p = new Path();
                    Path q = new Path();
//For first finger
                    p.moveTo(position.x, position.y);
                    builder.addStroke(new GestureDescription.StrokeDescription(p, 0L, 300L));
//                builder.addStroke(new GestureDescription.StrokeDescription(p, 200L, 100L));
                    GestureDescription gesture = builder.build();
                    boolean isDispatched = dispatchGesture(gesture, null, null);
//                    removeLayout();
//                    initManagerLayout();
//
//                    manager.addView(mLayoutUnlock, getLayoutParam());
//                    mLayoutUnlock.setVisibility(View.VISIBLE);
//                    mLayoutUnlock.setStartX(startX);
//                    mLayoutUnlock.setStartY(startY);
//                    mLayoutUnlock.setEndX(startX);
//                    mLayoutUnlock.setEndY(startY+400);
//                    mLayoutUnlock.invalidate();
//
//                    Utils.sleepSeconds(10);
//                    removeLayout();


                }
            }
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"handleTab",ex.toString());
        }
    }
}

