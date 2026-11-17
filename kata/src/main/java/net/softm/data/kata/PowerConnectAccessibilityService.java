//package net.softm.data.kata;
//
//import static android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED;
//import static android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED;
//import static android.view.Display.DEFAULT_DISPLAY;
//import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//import static android.view.accessibility.AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY;
//import static net.softm.data.kata.PowerConnectReceiver.ACTION_SOFTM_ACL_CONNECTED;
//import static net.softm.data.kata.PowerConnectReceiver.ACTION_SOFTM_ACL_DISCONNECTED;
//import static net.softm.data.kata.PowerConnectReceiver.ACTION_SOFTM_POWER_CONNECTED;
//import static net.softm.data.kata.PowerConnectReceiver.ACTION_SOFTM_POWER_DISCONNECTED;
//
//import static net.softm.data.kata.util.AcsUtil.EVENT_FORCE_STOP_CLICK;
//
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_ACCESSIBILITY_BUTTON;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_ACCESSIBILITY_BUTTON_CHOOSER;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_BACK;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_HOME;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_KEYCODE_HEADSETHOOK;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_LOCK_SCREEN;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_NOTIFICATIONS;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_POWER_DIALOG;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_QUICK_SETTINGS;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_RECENTS;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_TAKE_SCREENSHOT;
//import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN;
//import static net.softm.data.kata.util.AcsUtil.SEND_EVENT_DELAY_TIME;
//import static net.softm.data.kata.util.AcsUtil.EVENT_SETTINGS_SWITCH_CLICK;
//
//import android.accessibilityservice.AccessibilityServiceInfo;
//import android.accessibilityservice.GestureDescription;
//import android.app.KeyguardManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.graphics.Path;
//import android.graphics.PixelFormat;
//import android.graphics.Point;
//import android.hardware.display.DisplayManager;
//import android.net.ConnectivityManager;
//import android.net.wifi.WifiManager;
//import android.os.Build;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.SystemClock;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.view.Display;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.WindowManager;
//import android.view.accessibility.AccessibilityEvent;
//import android.view.accessibility.AccessibilityNodeInfo;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.RequiresApi;
//
//import com.crashlytics.android.Crashlytics;
//import com.skyfishjy.library.RippleBackground;
//
//import net.softm.data.kata.common.Const;
//import net.softm.data.kata.common.ReceiverType;
//import net.softm.data.kata.common.RunnableArg;
//import net.softm.data.kata.common.StartStopType;
//import net.softm.data.kata.job.WorkQueue;
//import net.softm.data.kata.util.AcsUtil;
//import net.softm.data.kata.util.MapUtil;
//import net.softm.data.kata.util.StringsXmlValueUtil;
//import net.softm.data.kata.util.Utils;
//
//import java.util.List;
//import java.util.concurrent.BlockingQueue;
//
//public class PowerConnectAccessibilityService extends android.accessibilityservice.AccessibilityService  implements View.OnTouchListener {
//    static String TAG = "PowerConnectAccessibilityService";
//    public static final String TAG_RESTART = "PowerConnectAccessibilityService";
//
//    PowerConnectReceiver powerConnectReceiver = new PowerConnectReceiver();
//    PowerScreenReceiver powerScreenReceiver = new PowerScreenReceiver();
//
//    PowerBluetoothReceiver powerBluetoothReceiver = new PowerBluetoothReceiver();
//    PowerBatteryReceiver powerBatteryReceiver = new PowerBatteryReceiver();
//    PowerWIFIReceiver powerWIFIReceiver = new PowerWIFIReceiver();
//    private Thread mainThread;
//    public static PowerConnectAccessibilityService mContext;
//    AccessibilityNodeInfo eventSource = null;
//
//    private static boolean isAvaliableClick = true;
//
//    private static boolean isControlSettingsCompleted = true;
//    private static boolean isControlSettingsAvaliableClick = true;
//    static int waitTimeSec = 5;
//    static int execTimeSec = 0;
//    static AcsUtil acsUtil = null;
//
//    private void performGlobalActions(String eventContentDescription) {
//        if      ( KEY_GLOBAL_ACTION_BACK.equals(eventContentDescription)                         ) performGlobalAction(GLOBAL_ACTION_BACK);
//        else if ( KEY_GLOBAL_ACTION_HOME.equals(eventContentDescription)                         ) performGlobalAction(GLOBAL_ACTION_HOME);
//        else if ( KEY_GLOBAL_ACTION_RECENTS.equals(eventContentDescription)                      ) performGlobalAction(GLOBAL_ACTION_RECENTS);
//        else if ( KEY_GLOBAL_ACTION_NOTIFICATIONS.equals(eventContentDescription)                ) performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS);
//        else if ( KEY_GLOBAL_ACTION_QUICK_SETTINGS.equals(eventContentDescription)               ) performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS);
//        else if ( KEY_GLOBAL_ACTION_POWER_DIALOG.equals(eventContentDescription)                 ) performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
//        else if ( KEY_GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN.equals(eventContentDescription)          ) performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
//        else if ( KEY_GLOBAL_ACTION_LOCK_SCREEN.equals(eventContentDescription)                  ) performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
//        else if ( KEY_GLOBAL_ACTION_TAKE_SCREENSHOT.equals(eventContentDescription)              ) performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
//        else if ( KEY_GLOBAL_ACTION_KEYCODE_HEADSETHOOK.equals(eventContentDescription)          ) performGlobalAction(GLOBAL_ACTION_KEYCODE_HEADSETHOOK);
//        else if ( KEY_GLOBAL_ACTION_ACCESSIBILITY_BUTTON.equals(eventContentDescription)         ) performGlobalAction(GLOBAL_ACTION_ACCESSIBILITY_BUTTON);
//        else if ( KEY_GLOBAL_ACTION_ACCESSIBILITY_BUTTON_CHOOSER.equals(eventContentDescription) ) performGlobalAction(GLOBAL_ACTION_ACCESSIBILITY_BUTTON_CHOOSER);
//        else if ( KEY_GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT.equals(eventContentDescription)       ) performGlobalAction(GLOBAL_ACTION_ACCESSIBILITY_SHORTCUT);
//        else if ( KEY_GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS.equals(eventContentDescription)       ) performGlobalAction(GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS);
//        else if ( KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE.equals(eventContentDescription)   ) performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
//    }
//    static Handler countDownHandler = null;
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//    @Override
//    public void onAccessibilityEvent(AccessibilityEvent event) {
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
////            disableSelf();
////        }
//        try {
//            Log.d(TAG, "Catch Event Package Name : " + event.getPackageName());
//            Log.d(TAG, "Catch Event TEXT : " + event.getText());
//            Log.d(TAG, "Catch Event Event : " + event.getContentDescription());
//            Log.d(TAG, "Catch Event getSource : " + event.getSource());
//            Log.d(TAG, "Catch Event eventType : " + event.getEventType());
//            Log.d(TAG, "Catch Event getViewIdResourceName : " + ( event.getSource() != null ? event.getSource().getViewIdResourceName() : "null") );
//
//            KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
//
//            AccessibilityNodeInfo ani = event.getSource();
//            if (ani != null) {
//                ani.refresh(); // to fix issue with viewIdResName = null on Android 6+
//            }
//
///*
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                Log.d(TAG, "KeyguardManager.isDeviceSecure() : " + String.valueOf(keyguardManager.isDeviceSecure()));
//            }
//            Log.d(TAG, "KeyguardManager.isKeyguardSecure() : " + String.valueOf(keyguardManager.isKeyguardSecure()));
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//                Log.d(TAG, "KeyguardManager.isDeviceLocked() : " + String.valueOf(keyguardManager.isDeviceLocked()));
//            }
//            Log.d(TAG, "KeyguardManager.isKeyguardLocked() : " + String.valueOf(keyguardManager.isKeyguardLocked()));
//*/
//            Intent intent = new Intent(this, powerConnectReceiver.getClass());
//            PendingIntent checker = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
//            if (checker == null) {
//                Log.d(TAG, "Catch Event Recever Bind : No ");
//            } else {
//                Log.d(TAG, "Catch Event Recever Bind : Yes");
//            }
//
//            String eventGubun = (String) event.getContentDescription();
//            List<CharSequence> keys = event.getText();
//            AccessibilityNodeInfo nodeInfo = event.getSource();
////
//            String eventKey = keys.size() > 0 ? (String) keys.get(0) :"";
//
//            if (
//                    AcsUtil.REGISTER_QUEUE.equals(eventKey)
//            ) {
//                Log.d(TAG, "register : " + eventGubun);
//                acsUtil.register(eventGubun,event);
//            }
//            Log.d(TAG, "=========================================================================");
//
////            if
////                    EVENT_REGISTER_CALLBACK.equals(eventContentDescription)
////            ) {
////                acsUtil.registerCallback(eventTexts.get(1).toString(),event);
////            }
//
////            acsUtil.handleForceStopDialogOk(event);
//
////            handleEventForceStopClickToOk(event);
//
//            if ( AcsUtil.TURN_ON_SCREEN.equals(eventGubun) ) {
//                Utils.turnOnScreen();
//                PowerConnectAccessibilityService.turnOnScreen();
//            }
//
//            if ( AcsUtil.OPEN_OVERLAY_VIEW_SHOWONLY.equals(eventGubun) ) {
//                MapUtil.openOverlayViewShowOnlyNotClose(this);
//            }
//
//            if ( AcsUtil.TURN_OFF_SCREEN.equals(eventGubun) ) {
//                Utils.turnOffScreen();
//            }
//
//            performGlobalActions(eventGubun);
//
//            handleShowWaitingOverlay(event);
//
//            handleTurnOnScreenSwipe(event);
//
////            handleControlSettingsWifi(event);
//            performQueuesRunnable(event);
//
///*
////            acsUtil.performRunnable();
//*/
//
//        } catch ( Exception ex ) {
//            ex.printStackTrace();
//        }
//    }
//
//    public void performQueuesRunnable(AccessibilityEvent event){
//        try {
//
//            if ( acsUtil.execQueues.size() > 0 ) {
//
//                int eventType = event.getEventType();
//
//                boolean exec = false;
//                for (String gubun : acsUtil.execQueues.keySet()) {
//
////                    if ( EVENT_QUICK_SETTINGS_CLICK.equals(gubun) || EVENT_SETTINGS_SWITCH_CLICK.equals(gubun) || KEY_GLOBAL_ACTION_BACK.equals(gubun) || EVENT_FORCE_STOP_CLICK.equals(gubun) ) {
////                        exec = true;
////                    } else {
////
////                    }
//                    if ( (
//                            AcsUtil.EVENT_FORCE_STOP_DIALOG_OK_CLICK.equals(gubun)
//                                    || AcsUtil.EVENT_FORCE_STOP_CLICK.equals(gubun)
////                                    || AcsUtil.KEY_GLOBAL_ACTION_QUICK_SETTINGS.equals(gubun)
//                         )
//                            && eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ) {
//                        exec = true;
//                    } else {
//                        exec = true;
//                    }
//
//                    if ( !exec ) return;
//
//                    BlockingQueue<RunnableArg> queue = acsUtil.execQueues.get(gubun);
//                    acsUtil.execQueues.remove(gubun);
//                    if (!queue.isEmpty()) {
//                        RunnableArg task = null;
//                        Runnable taskRunnable = null;
//                        if ( queue.peek() instanceof RunnableArg ) {
//                            task = queue.take();
//                            task.setPackageName(event.getPackageName());
//                            task.setText(event.getText());
//                            task.setContentDescription(event.getContentDescription());
//                            task.setSource(event.getSource());
//                            task.setEventType(event.getEventType());
//                            Thread thread = new Thread(task);
//                            thread.start();
//                            Log.d(TAG, "RunnableArg.callBack(" + gubun + ")" );
//                            Log.d(TAG, "RunnableArg.callBack.event.getEventType(" + event.getEventType() + ")" );
//                            Log.d(TAG, "RunnableArg.callBack.event.getText(" + event.getText() + ")" );
//                            Log.d(TAG, "RunnableArg.callBack.event.getContentDescription(" + event.getContentDescription() + ")" );
//                            Log.d(TAG, "RunnableArg.callBack.event.getSource(" + event.getSource() + ")" );
//                        } else {
//                            taskRunnable = queue.take();
//                            Thread thread = new Thread(taskRunnable);
//                            thread.start();
//                            Log.d(TAG, "Runnable.callBack(" + gubun + ")" );
//                        }
//                    }
//                }
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void handleTurnOnScreenSwipe(AccessibilityEvent event) {
//        String eventContentDescription = (String) event.getContentDescription();
//        List<CharSequence> eventTexts = event.getText();
//        AccessibilityNodeInfo nodeInfo = event.getSource();
//
//        if ( AcsUtil.GESTURE_SWIPE_TEST.equals(eventContentDescription) ) {
//
//            Path swipePath = new Path();
////                  swipePath.moveTo(startX, startY);
////                  swipePath.lineTo(endX, endY);
//            float startX = Float.parseFloat(eventTexts.get(1).toString());
//            float startY = Float.parseFloat(eventTexts.get(2).toString());
//            float endX = Float.parseFloat(eventTexts.get(3).toString());
//            float endY = Float.parseFloat(eventTexts.get(4).toString());
//            swipePath.moveTo(startX, startY);
//            swipePath.lineTo(endX, endY);
//
//            mLayoutUnlock.setStartX(startX);
//            mLayoutUnlock.setStartY(startY);
//            mLayoutUnlock.setEndX(endX);
//            mLayoutUnlock.setEndY(endY);
//            mLayoutUnlock.invalidate();
//        }
//
//        if ( AcsUtil.GESTURE_DOUBLE_TAP.equals(eventContentDescription) ) {
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//
//                Point point = new Point();
//                manager.getDefaultDisplay().getRealSize(point);
//                int width = point.x;
//                int height = point.y;
//
//                float startX = width-100;
//                float startY = height-300;
//                float endX = width-100;
//                float endY = height-900;
//
//                Point position=new Point((int) startX, (int) startY);
//                GestureDescription.Builder builder = new GestureDescription.Builder();
//                Path p = new Path();
//                Path q = new Path();
////For first finger
//                p.moveTo(position.x, position.y);
//                p.lineTo(position.x, position.y);
//
////For second finger
//                q.moveTo(position.x, position.y);
//                q.lineTo(position.x, position.y+300);
//
////Two strokes for two fingers
//                builder.addStroke(new GestureDescription.StrokeDescription(p, 100L, 50L));
////                builder.addStroke(new GestureDescription.StrokeDescription(p, 200L, 100L));
//                GestureDescription gesture = builder.build();
//                boolean isDispatched = dispatchGesture(gesture,null,null);
//            }
//
//
//        }
//        if ( AcsUtil.GESTURE_SWIPE.equals(eventContentDescription) ) {
////                Path swipePath = new Path();
////                swipePath.moveTo(2000, 2000);
////                swipePath.lineTo(100, 2000);
////                GestureDescription.Builder gestureBuilder = null;
////                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
////                    gestureBuilder = new GestureDescription.Builder();
////                    gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 1000));
////                    dispatchGesture(gestureBuilder.build(), null, null);
////                }
//
////                private void performSwipeGesture(int startX, int startY, int endX, int endY, int duration) {
//            Point point = new Point();
//            manager.getDefaultDisplay().getRealSize(point);
//            int width = point.x;
//            int height = point.y;
//
//            float startX = width-100;
//            float startY = height-300;
//            float endX = width-100;
//            float endY = height-900;
//
////            float startX = 800;
////            float startY = 900;
////            float endX = 800;
////            float endY = 10;
//            Path swipePath = new Path();
////                  swipePath.moveTo(startX, startY);
////                  swipePath.lineTo(endX, endY);
//            swipePath.moveTo(startX, startY);
//            swipePath.lineTo(endX, endY);
//
//            mLayoutUnlock.setStartX(startX);
//            mLayoutUnlock.setStartY(startY);
//            mLayoutUnlock.setEndX(endX);
//            mLayoutUnlock.setEndY(endY);
//            mLayoutUnlock.invalidate();
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                GestureDescription.Builder gestureBuilder = null;
//                gestureBuilder = new GestureDescription.Builder();
//                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 800));
//                GestureDescription gestureDescription = gestureBuilder.build();
//                dispatchGesture(gestureDescription, null, new Handler(Looper.getMainLooper()));
//            }
//
////                KeyguardManager keyguardManager = (KeyguardManager) WApplication.mInstance.getSystemService(WApplication.mInstance.KEYGUARD_SERVICE);
////                KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
////                lock.disableKeyguard();
//
//
////                }
//        }
//    }
//    public static long showWaitStartTime = -1;
//
//    public static void showWaitingOverlay(String action, ReceiverType receiverType, long waitingSec) {
//        showWaitStartTime = System.currentTimeMillis();
////        sendEvent(SHOW_WAITING_OVERLAY,null, new String[]{action,receiverType.getId(),String.valueOf(waitingSec)},0);
////        sendEvent(String gubun,String key,String[] texts,long delayTime) {
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                AcsUtil acsUtil = new AcsUtil(mContext);
//                AccessibilityEvent e = AccessibilityEvent.obtain();
//                e.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
//                e.setClassName(PowerConnectAccessibilityService.class.getName());
//                e.setPackageName(mContext.getPackageName());
//                e.setContentDescription(AcsUtil.SHOW_WAITING_OVERLAY);
//                e.getText().add("SOFTM ACS");
//                for (String text : new String[]{action, receiverType.getId(), String.valueOf(waitingSec)}) {
//                    if (text != null) e.getText().add(text);
//                }
//                handleShowWaitingOverlay(e);
//            }
//        },0);
//    }
//    private static void removeLayout() {
//        try {
//            if (mLayout.getWindowToken() != null) {
//                manager.removeView(mLayout);
//            }
//            if (mLayout.getParent() != null) {
//                manager.removeView(mLayout);
//            }
//
//        } catch(Exception ex) {
//            Crashlytics.setString("gubun",TAG);
//            Crashlytics.log(Log.ERROR,TAG,ex.getMessage());
//            Crashlytics.log(Log.INFO,TAG,"removeLayout https://console.firebase.google.com/project/startnavifortmap/crashlytics/app/android:net.softm.data.kata/issues/d74198fce06ff4bfecd0d970b1ed5ebb?time=1696579198000:1696665598000&sessionEventKey=651E64FB01A300011425F55673CCF6CB_1865229398847716235");
//        }
//
//        try {
//            if (mLayoutUnlock.getWindowToken() !=null ) {
//                manager.removeView(mLayoutUnlock);
//            }
//            if (mLayoutUnlock.getParent() != null) {
//                manager.removeView(mLayoutUnlock);
//            }
//        } catch(Exception ex) {
//            Crashlytics.setString("gubun",TAG);
//            Crashlytics.log(Log.ERROR,TAG,ex.getMessage());
//            Crashlytics.log(Log.INFO,TAG,"removeLayout https://console.firebase.google.com/project/startnavifortmap/crashlytics/app/android:net.softm.data.kata/issues/d74198fce06ff4bfecd0d970b1ed5ebb?time=1696579198000:1696665598000&sessionEventKey=651E64FB01A300011425F55673CCF6CB_1865229398847716235");
//        }
//    }
//    public static void stopWaitingOverlay() {
//        if ( countDownHandler != null ) countDownHandler.removeCallbacksAndMessages(null);
//        showWaitStartTime = -1;
//        removeLayout();
//    }
//
//    private static void handleShowWaitingOverlay(AccessibilityEvent event) {
//        String eventContentDescription = (String) event.getContentDescription();
//        List<CharSequence> eventTexts = event.getText();
////        AccessibilityNodeInfo nodeInfo = event.getSource();
//
//        if ( AcsUtil.SHOW_WAITING_OVERLAY.equals(eventContentDescription) ) {
////            event.getBeforeText()
////            performGlobalAction(GLOBAL_ACTION_BACK);
//            removeLayout();
//            if (manager != null) {
//                if (mLayout != null) {
//                    addView(mLayout,getLayoutParamForWaitingOverlay());
//                } else {
//                    Crashlytics.log("mLayout is null");
//                }
//            } else {
//                Crashlytics.log("manger is null");
//            }
//
//            Log.i(AcsUtil.SHOW_WAITING_OVERLAY,"SHOW.WAITING.OVERLAY.add");
//            if ( event.getText().size() == 4 ) waitTimeSec = Integer.parseInt(eventTexts.get(3).toString());
//            execTimeSec = waitTimeSec;
//
//            TextView tv = mLayout.findViewById(R.id.info_time_sec);
//            tv.setText(String.valueOf(execTimeSec));
//
//            TextView tvStatus2 = mLayout.findViewById(R.id.statu2);
//            ((TextView)mLayout.findViewById(R.id.app_name)).setText(WApplication.appInfo.name);
//            ((ImageView)mLayout.findViewById(R.id.app_logo)).setImageDrawable(WApplication.appInfo.icon);
//
//            if ( eventTexts.get(1).equals(ACTION_SOFTM_POWER_CONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_ACL_CONNECTED)  || eventTexts.get(1).equals(PowerConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED)) {
//                tvStatus2.setText(R.string.start);
//                tvStatus2.setTextColor(mContext.getResources().getColor(R.color.blue_500));
//                Log.i(TAG,"시작" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
//            } else  if ( eventTexts.get(1).equals(ACTION_SOFTM_POWER_DISCONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_ACL_DISCONNECTED)  || eventTexts.get(1).equals(PowerConnectReceiver.ACTION_SOFTM_WIFI_DISCONNECTED)) {
//                tvStatus2.setText(R.string.end);
//                tvStatus2.setTextColor(mContext.getResources().getColor(R.color.red_500));
//                Log.i(TAG,"종료"+ AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
//            }
//
//            TextView tvReceiverStatus = mLayout.findViewById(R.id.receiver_status);
//            String receiverStatus = ReceiverType.value(eventTexts.get(2).toString()).getDescription();
//            tvReceiverStatus.setText(receiverStatus);
//
//            mLayout.setOnTouchListener(mContext);
////                @Override
////                protected void onDraw(Canvas canvas) {
////                    super.onDraw(canvas);
////                    canvas.drawCircle(xPos, yPos, radius, paint);
////                }
//
//            RippleBackground rippleBackground=(RippleBackground)mLayout.findViewById(R.id.content);
//            ImageView imageView=(ImageView)mLayout.findViewById(R.id.btnLogo);
//            rippleBackground.startRippleAnimation();
//            if ( countDownHandler != null ) countDownHandler.removeCallbacksAndMessages(null);
//            countDownHandler = new Handler();
//
//            countDownHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    //                int waitTimeSec = 5;
//                    //                int execTimeSec = 0;
//                    if ( execTimeSec >= 0 ) {
//                        tv.setText(String.valueOf(execTimeSec));
//                        countDownHandler.postDelayed(this,1000);
//                    } else {
//                        countDownHandler.removeCallbacksAndMessages(null);
//                        try {
////                            TODO softm removeView
//                            manager.removeView(mLayout);
////                            mLayout.setVisibility(View.GONE);
//                        } catch (Exception ex) {
//                            //
//                        }
//                    }
//                    execTimeSec--;
//                }
//            },0);
//
////            PulsatorLayout pulsator = (PulsatorLayout) mLayout.findViewById(R.id.pulsator);
////            pulsator.start();
//
////                if ( MapUtil.isLockScreen() ) {
////            acsUtil.sendEvent(AcsUtil.KEY_GLOBAL_ACTION_LOCK_SCREEN);
////            AccessibilityManager accessibilityManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
////            boolean isServiceEnabled = accessibilityManager.isEnabled();
////
////            if (isServiceEnabled) {
//                // Simulate power button press
//                int powerKeyCode = KeyEvent.KEYCODE_POWER;
//
//                // Create a KeyEvent instance
//                long eventTime = SystemClock.uptimeMillis();
//                KeyEvent powerDownEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, powerKeyCode, 0);
//
//                // Dispatch the KeyEvent
//                mLayout.dispatchKeyEvent(powerDownEvent);
//
//                // Wait for a brief moment
//                try {
//                    Thread.sleep(100); // Adjust the delay as per your requirement
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                // Create the corresponding key up event
//                KeyEvent powerUpEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, powerKeyCode, 0);
//
//                // Dispatch the KeyEvent
//                mLayout.dispatchKeyEvent(powerUpEvent);
////            }
//        }
//
//        if ( AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS.equals(eventContentDescription) ) {
//            TextView tvStatus2 = mLayout.findViewById(R.id.statu2);
//            ((TextView)mLayout.findViewById(R.id.app_name)).setText(WApplication.appInfo.name);
//            if ( eventTexts.get(1).equals(ACTION_SOFTM_POWER_CONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_ACL_CONNECTED) || eventTexts.get(1).equals(PowerConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED)) {
//                tvStatus2.setText(R.string.start);
//                tvStatus2.setTextColor(mContext.getResources().getColor(R.color.blue_500));
//                Log.i(TAG,"시작" + AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
//            } else  if ( eventTexts.get(1).equals(ACTION_SOFTM_POWER_DISCONNECTED) || eventTexts.get(1).equals(ACTION_SOFTM_ACL_DISCONNECTED) || eventTexts.get(1).equals(PowerConnectReceiver.ACTION_SOFTM_WIFI_DISCONNECTED) ) {
//                tvStatus2.setText(R.string.end);
//                tvStatus2.setTextColor(mContext.getResources().getColor(R.color.red_500));
//                Log.i(TAG,"종료"+ AcsUtil.SHOW_WAITING_OVERLAY_UPDATE_STATUS);
//            }
//
//            TextView tvReceiverStatus = mLayout.findViewById(R.id.receiver_status);
//            String receiverStatus = ReceiverType.value(eventTexts.get(2).toString()).getDescription();
//            tvReceiverStatus.setText(receiverStatus);
//        }
//
//    }
//
//    public static void addView(ACSFrameLayout layout,WindowManager.LayoutParams params) {
////        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
////            final DisplayManager dm = mContext.getSystemService(DisplayManager.class);
////            final Display primaryDisplay = dm.getDisplay(DEFAULT_DISPLAY);
////            Context windowContext = mContext.createDisplayContext(primaryDisplay).createWindowContext(TYPE_APPLICATION_OVERLAY, null);
////            params.type = TYPE_APPLICATION_OVERLAY; // TYPE_ACCESSIBILITY_OVERLAY
////            params.flags = WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
////            windowContext.getSystemService(WindowManager.class).addView(layout, params);
////        } else {
//            manager.addView(layout, params);
////        }
//    }
//
//    public static void turnOnScreen() {
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                removeLayout();
//                if (manager != null) {
//                    if (mLayoutUnlock != null) {
////                        addView(mLayoutUnlock,getLayoutParam());
//                        manager.addView(mLayoutUnlock,getLayoutParam());
//                    } else {
//                        Crashlytics.log("mLayoutUnlock is null");
//                    }
//                } else {
//                    Crashlytics.log("mLayoutUnlock is null");
//                }
//
//                mLayoutUnlock.setOnTouchListener(mContext);
//
//                int powerKeyCode = KeyEvent.KEYCODE_POWER;
//
//                // Create a KeyEvent instance
//                long eventTime = SystemClock.uptimeMillis();
//                KeyEvent powerDownEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, powerKeyCode, 0);
//
//                // Dispatch the KeyEvent
//                        mLayoutUnlock.dispatchKeyEvent(powerDownEvent);
//
//                // Wait for a brief moment
//                try {
//                    Thread.sleep(100); // Adjust the delay as per your requirement
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                // Create the corresponding key up event
//                KeyEvent powerUpEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, powerKeyCode, 0);
//
//                // Dispatch the KeyEvent
//                        mLayoutUnlock.dispatchKeyEvent(powerUpEvent);
//                (new Handler()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        manager.removeView(mLayoutUnlock);
//                    }
//                },waitTimeSec);
//            }
//        },0);
//
////                                    }
//
//
//    }
//
//    private void handleEventForceStopClickToOk(AccessibilityEvent event) {
//        String eventContentDescription = (String) event.getContentDescription();
//        List<CharSequence> eventTexts = event.getText();
//        AccessibilityNodeInfo nodeInfo = event.getSource();
//
//        if ( EVENT_FORCE_STOP_CLICK.equals(eventContentDescription) ) {
//            acsUtil.invokeForceStopClick(getRootInActiveWindow(),false);
//        }
////            if ( getPackageName() event.getPackageName() )
//        if ( AcsUtil.isValidForceButtonClicked() ) {
//            if ( "com.android.settings".equals(event.getPackageName()) && nodeInfo != null ) {
//                int eventType = event.getEventType();
//                if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                    if (isAvaliableClick) {
//                        Handler rHandler = new Handler(Looper.getMainLooper());
//                        isAvaliableClick = false;
//                        rHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.d(TAG, "ForceStopOkClick");
//                                isAvaliableClick = true;
//                            }
//                        }, AcsUtil.FORCE_STOP_OK_PREVENT_INTERVAL_TIME);
//
//                        Log.d("ACS","FORCE_STOP_OK_PREVENT_INTERVAL_TIME : " + System.currentTimeMillis());
//                        Log.d("ACS","(AcsUtil.appDetailSettinsForceClickedTime + SEND_EVENT_DELAY_TIME) + \" / \" + System.currentTimeMillis() : " + (AcsUtil.appDetailSettingsForceClickedTime + SEND_EVENT_DELAY_TIME) + " / " + System.currentTimeMillis());
//
//                        if ( AcsUtil.appDetailSettingsForceClickedTime + SEND_EVENT_DELAY_TIME + 3000 > System.currentTimeMillis()) {
//                            //                                                                  3000 보정.
//                            if ( MapUtil.isNaviApp() ) {
//                                Utils.toast(this,R.string.force_stop_navi);
//                            } else {
//                                Utils.toast(this,R.string.force_stop_app);
//                            }
//                            Log.d("ACS","Click OK");
//                            acsUtil.invokeForceStopOkClick(nodeInfo);
//                        } else {
//                            Log.d("ACS","Click NOT OK");
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private void handleForceStopClickToOk2(AccessibilityEvent event) {
//        String eventContentDescription = (String) event.getContentDescription();
//        List<CharSequence> eventTexts = event.getText();
//        AccessibilityNodeInfo nodeInfo = event.getSource();
//
//        if ( EVENT_FORCE_STOP_CLICK.equals(eventContentDescription) ) {
//            acsUtil.invokeForceStopClick(getRootInActiveWindow(),false);
//        }
////            if ( getPackageName() event.getPackageName() )
//
//        if ( "com.android.settings".equals(event.getPackageName()) && nodeInfo != null ) {
//            Log.d("ACS","ACS");
//            int eventType = event.getEventType();
//            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                if ( AcsUtil.isValidForceButtonClicked() ) {
//                    if (isAvaliableClick) {
//                        isAvaliableClick = false;
//                        Handler rHandler = new Handler(Looper.getMainLooper());
//                        rHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.d("ACS", "ForceStopOkClicked");
//                                isAvaliableClick = true;
//                            }
//                        }, AcsUtil.FORCE_STOP_OK_PREVENT_INTERVAL_TIME);
//                        Log.d("ACS","FORCE_STOP_OK_PREVENT_INTERVAL_TIME : " + System.currentTimeMillis());
//                        Log.d("ACS","(AcsUtil.appDetailSettinsForceClickedTime + SEND_EVENT_DELAY_TIME) + \" / \" + System.currentTimeMillis() : " + (AcsUtil.appDetailSettingsForceClickedTime + SEND_EVENT_DELAY_TIME) + " / " + System.currentTimeMillis());
//                        if ( AcsUtil.appDetailSettingsForceClickedTime + SEND_EVENT_DELAY_TIME + 3000 > System.currentTimeMillis()) {
//                            //                                                                  3000 보정.
//                            if ( MapUtil.isNaviApp() ) {
//                                Utils.toast(this,R.string.force_stop_navi);
//                            } else {
//                                Utils.toast(this,R.string.force_stop_app);
//                            }
//                            Log.d("ACS","Click OK");
//                            acsUtil.invokeForceStopOkClick(nodeInfo);
//                        } else {
//                            Log.d("ACS","Click NOT OK");
//                        }
//                    }
//                } else {
//                    if ( AcsUtil.appDetailSettingsForceClickedTime > System.currentTimeMillis() &&
//                            AcsUtil.appDetailSettingsForceClickedTime + SEND_EVENT_DELAY_TIME + 3000 <= System.currentTimeMillis()) {
//                        acsUtil.sendEvent(KEY_GLOBAL_ACTION_BACK);
//                        Log.d("ACS","Click NOT OK-2");
//                    }
//                    isAvaliableClick = false;
//                }
//
//            }
//        }
//
//    }
//
//    private static WindowManager manager;
//    static long saveX;
//    static long saveY;
//    public static WindowManager.LayoutParams getLayoutParam(){
//
//        DisplayMetrics metrics = new DisplayMetrics();
//
//        manager.getDefaultDisplay().getMetrics(metrics);
//        int LAYOUT_FLAG;
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        }else{
//            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; // TYPE_PHONE
//        }
//        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
//
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.MATCH_PARENT,
//                LAYOUT_FLAG,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
//                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|
//                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
//                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
//                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
//                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
//                PixelFormat.TRANSLUCENT);
//        return params;
//    }
//    public static WindowManager.LayoutParams getLayoutParamForWaitingOverlay(){
////        WindowManager.LayoutParams.MATCH_PARENT, // Overlay must be full screen otherwise my trick will not work
////                WindowManager.LayoutParams.MATCH_PARENT,
////                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
////                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, // Allow another windows to receive touch events
//
////        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
////                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
////                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
////                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//
//        WindowManager.LayoutParams params = getLayoutParam();
//
//        params.gravity = Gravity.CENTER;
//// top 10% position
//        params.gravity = Gravity.LEFT | Gravity.TOP;
//
//        saveX = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_ASS_OVERLAY_POPUP_X, -1L);
//        saveY = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_ASS_OVERLAY_POPUP_Y, -1L);
//
//        if ( saveX == -1 ) {
//            DisplayMetrics metrics = new DisplayMetrics();
//            params.x = (int) (10);
//            params.y = (int) (metrics.heightPixels * 0.1);
//        } else {
//            params.x = (int) (saveX);
//            params.y = (int) (saveY);
//        }
//
////        params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
////        params.format = PixelFormat.TRANSLUCENT;
////        params.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
////        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
////        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
////        params.gravity = Gravity.TOP;
//
//        return params;
//    }
//
//    static ACSFrameLayout mLayout;
//    static ACSFrameLayout mLayoutUnlock;
//    public static ACSFrameLayout getLayout() {
//        return mLayout;
//    }
//    // 접근성 권한을 가지고, 연결이 되면 호출되는 함수
//    public void onServiceConnected() {
//        mContext = this;
//        //Toast.makeText(getApplicationContext(), "reflash", Toast.LENGTH_SHORT).show();
//        Log.d(TAG, "ACS-onServiceConnected -> START");
//        // 알림바가 켜져있고 접근성서비스를 이용하므로 일반서비스 Stop
//        // if ( MapUtil.isNotiIcon() ) {
//        Log.d(TAG, "ACS-onServiceConnected -> STOP Service-1");
////        Intent in = new Intent(mContext, PowerConnectService.class);
////        mContext.stopService(in);
//        Log.d(TAG, "ACS-onServiceConnected -> STOP Service-2");
//
////        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
////
////        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK; // 전체 이벤트 가져오기
//////        info.feedbackType = AccessibilityServiceInfo.DEFAULT | AccessibilityServiceInfo.FEEDBACK_HAPTIC;
////        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN | AccessibilityServiceInfo.FEEDBACK_HAPTIC;
////        info.notificationTimeout = 100; // millisecond
////        setServiceInfo(info);
//
//
//        AccessibilityServiceInfo info   = new AccessibilityServiceInfo();
////        info.packageNames               = new String[]{getApplicationContext().getPackageName()};
//        info.eventTypes                 = AccessibilityEvent.TYPES_ALL_MASK;
//        info.feedbackType               = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
//        info.notificationTimeout        = 100;
//        info.flags                      = AccessibilityServiceInfo.DEFAULT;
////        android:accessibilityEventTypes="typeAllMask"
////        android:accessibilityFlags="flagDefault"
////        android:accessibilityFeedbackType="feedbackSpoken"
////        android:notificationTimeout="100"
////        android:canRetrieveWindowContent="true"
//
//        setServiceInfo(info);
//
//        registerPowerConnectReceiver();
//
//        manager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        //        P_CLOSE_POPUP = true;
//        //        if ( !P_CLOSE_POPUP) {
//        mLayout = (ACSFrameLayout) inflater.inflate(R.layout.always_on_top_acs_layout, null);
//        mLayoutUnlock = (ACSFrameLayout) inflater.inflate(R.layout.unlock_empty_layout, null);
//
//        // TODO softm test
////        manager.removeView(mLayout);
////        manager.addView(mLayout, getLayoutParam());
////        mLayout.setOnTouchListener(this);
////
//
//        RippleBackground rippleBackground=(RippleBackground)mLayout.findViewById(R.id.content);
//        ImageView imageView=(ImageView)mLayout.findViewById(R.id.btnLogo);
////
////        rippleBackground.startRippleAnimation();
//
////        PulsatorLayout pulsator = (PulsatorLayout) mLayout.findViewById(R.id.pulsator);
////        pulsator.start();
//        acsUtil = new AcsUtil(mContext);
//    }
//
//    @Override
//    public void onInterrupt() {
//        Log.d("TEST", "OnInterrupt");
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
////        android.os.Debug.waitForDebugger();  // this line is key
//    }
//
//    @Override
//    public void onDestroy() {
//        Log.d(TAG,"onDestroy");
//        super.onDestroy();
//        try{
//            if (mLayout != null) {
//                manager.removeView(mLayout);
//            }
//            unregisterReceiver(this.powerConnectReceiver);
//            unregisterReceiver(this.powerScreenReceiver);
//            unregisterReceiver(this.powerBatteryReceiver);
//            unregisterReceiver(this.powerBluetoothReceiver);
//            unregisterReceiver(this.powerWIFIReceiver);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
//                        .getSystemService(Context.CONNECTIVITY_SERVICE);
//                connectivityManager.unregisterNetworkCallback(wifiCallback);
//            }
//
//
//
//        }catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//    ConnectivityManager.NetworkCallback wifiCallback;
//    private void registerPowerConnectReceiver() {
//        IntentFilter localIntentFilterPower = new IntentFilter();
//        localIntentFilterPower.addAction(ACTION_SOFTM_POWER_CONNECTED);
//        localIntentFilterPower.addAction(ACTION_SOFTM_POWER_DISCONNECTED);
//
//        localIntentFilterPower.addAction(ACTION_SOFTM_ACL_CONNECTED);
//        localIntentFilterPower.addAction(ACTION_SOFTM_ACL_DISCONNECTED);
//
//        localIntentFilterPower.addAction(PowerConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED);
//        localIntentFilterPower.addAction(PowerConnectReceiver.ACTION_SOFTM_WIFI_DISCONNECTED);
//
//        registerReceiver(this.powerConnectReceiver, localIntentFilterPower);
//
//        IntentFilter localIntentFilterScreen = new IntentFilter();
//        localIntentFilterScreen.addAction(Intent.ACTION_USER_PRESENT);
//        localIntentFilterScreen.addAction(Intent.ACTION_USER_UNLOCKED);
////        localIntentFilterScreen.addAction(Intent.ACTION_SCREEN_ON);
////        localIntentFilterScreen.addAction(Intent.ACTION_SCREEN_OFF);
//        registerReceiver(this.powerScreenReceiver, localIntentFilterScreen);
//
//        IntentFilter localIntentFilterBattery = new IntentFilter();
//        localIntentFilterBattery.addAction(Intent.ACTION_POWER_CONNECTED);
//        localIntentFilterBattery.addAction(Intent.ACTION_POWER_DISCONNECTED);
//        registerReceiver(this.powerBatteryReceiver, localIntentFilterBattery);
//
//        IntentFilter localIntentFilterBluetooth = new IntentFilter();
//        localIntentFilterBluetooth.addAction(ACTION_ACL_CONNECTED);
//        localIntentFilterBluetooth.addAction(ACTION_ACL_DISCONNECTED);
//        registerReceiver(this.powerBluetoothReceiver, localIntentFilterBluetooth);
//
//        IntentFilter localIntentFilterWIFI = new IntentFilter();
//        localIntentFilterWIFI.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
//        localIntentFilterWIFI.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        registerReceiver(this.powerWIFIReceiver, localIntentFilterWIFI);
//
//        Log.d(TAG,"PowerConnectAccessibilityService >> add Receiver PowerConnectReceiver");
///*
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//
//            wifiCallback = new ConnectivityManager.NetworkCallback() {
//                @Override
//                public void onAvailable(Network network) {
//                    super.onAvailable(network);
////                    Utils.toast(mContext,"WI-FI 연결됨");
//                    MapUtil.getCurrentSsid(mContext);
//                    Log.e("onAvailable", "WI-FI 연결됨");
//                }
//
//                @Override
//                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
//                    super.onCapabilitiesChanged(network, networkCapabilities);
//                    MapUtil.getCurrentSsid(mContext);
//                }
//
//                @Override
//                public void onLost(Network network) {
//                    super.onLost(network);
////                    Utils.toast(mContext,"WI-FI 연결 끊김");
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                        ConnectivityManager connectivityManager = getApplicationContext().getSystemService(ConnectivityManager.class);
//                        NetworkCapabilities netCaps = connectivityManager.getNetworkCapabilities(network);
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                            try {
//                                WifiInfo info = (WifiInfo) netCaps.getTransportInfo();
//                            } catch (Exception ex) {
////                                Utils.toast(mContext, ex.toString());
//                            }
//                        }
////                    String ssid = wifiInfo.getSSID();
//
//                        Log.e("onAvailable", "WI-FI 연결 끊김");
//                    }
//                }
//            };
//
//            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//
//            // Builder 선언
//            NetworkRequest.Builder wifiBuilder = new NetworkRequest.Builder();
//
//            // 콜백이 호출되는 조건
//            // TRANSPORT_WIFI = WIFI 상태 / TRANSPORT_CELLULAR = Mobile 네트워크 상태
//            wifiBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
//
//            // 해당 콜백 등록
//            connectivityManager.registerNetworkCallback(wifiBuilder.build(), wifiCallback);
//
//        }*/
//
//
//    }
//
//    float xpos = 0;
//    float ypos = 0;
//    private float downX;
//    private float downY;
//    private final float SCROLL_THRESHOLD = 100;
//    private boolean isOnClick;
//    private static boolean enabled = false;
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
//                    WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_ASS_OVERLAY_POPUP_X, saveX);
//                    WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_ASS_OVERLAY_POPUP_Y, saveY);
//                    if (isOnClick) {
//
//                            if (mLayout.getWindowToken() !=null ) {
//                                manager.removeView(mLayout);
//                            }
//
//                            Handler rHandler = new Handler(Looper.getMainLooper());
//                            rHandler.postDelayed(new Runnable() {
//                                @RequiresApi(api = Build.VERSION_CODES.N)
//                                @Override
//                                public void run() {
//                                    Log.d(TAG, "onTouch");
////                                    view.performClick();
//
//// TODO softm : Gesture Click
////                                    Path clickPath = new Path();
////                                    clickPath.moveTo(xpos, ypos);
////                                    GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
////                                    gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 100));
////                                    dispatchGesture(gestureBuilder.build(), null, null);
//                                    Utils.toast(mContext,R.string.msg_exec_stopped);
////                                    Utils.toast(mContext,"실행이 중지 되었습니다.");
//                                    PowerConnectReceiver.stop();
//
//                                }
//                            }, 100);
//                        Log.d(TAG, "onClick ");
//                    } else {
//                    }
//                }
//                break;
//            case MotionEvent.ACTION_MOVE:
////                if (pointerCount == 1) {
////                    WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
////                    float dx = xpos - motionEvent.getRawX();
////                    float dy = ypos - motionEvent.getRawY();
////                    xpos = motionEvent.getRawX();
////                    ypos = motionEvent.getRawY();
////
////                    Log.d(TAG,(Math.abs(downX - motionEvent.getX())) + " / " + (Math.abs(downY - motionEvent.getY()))+ " / lp.x : " + lp.x + ", dx : " + dx + "lp.y : " + lp.y + ", dy : " + dy);
////
////                    saveX = (long) (lp.x - dx);
////                    saveY = (long) (lp.y - dy);
////                    lp.x = (int)saveX;
////                    lp.y = (int)saveY;
////                    manager.updateViewLayout(view,lp);
////
////                    if (isOnClick && (Math.abs(downX - motionEvent.getX()) > SCROLL_THRESHOLD || Math.abs(downY - motionEvent.getY()) > SCROLL_THRESHOLD)) {
////                        Log.i(TAG, "movement detected");
////                        isOnClick = false;
////                    }
////                    return true;
////                }
//                break;
//        }
//        return false;
//    }
//
//
//    private void handleControlSettingsWifi(AccessibilityEvent event) {
//        String eventContentDescription = (String) event.getContentDescription();
//        List<CharSequence> eventTexts = event.getText();
//        AccessibilityNodeInfo nodeInfo = event.getSource();
//        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
//        if ( EVENT_SETTINGS_SWITCH_CLICK.equals(eventContentDescription) ) {
////            ascUtil.invokeForceStopClick(getRootInActiveWindow(), event);
////            AccessibilityNodeInfo nodeInfo = event.getSource();
////        if ( "com.android.settings".equals(event.getPackageName()) && nodeInfo != null ) {
//            if (rootNode != null) {
//                AccessibilityNodeInfo switchButton = acsUtil.findButtonByClassName(rootNode, "android.widget.Switch");
//                StartStopType startStopType = StartStopType.valueOf(String.valueOf(event.getText().get(1)));
//                boolean onOff = Boolean.valueOf(String.valueOf(event.getText().get(2)));
//                while( acsUtil.isEnabledForStartStopType(startStopType) != onOff ) {
//                    switchButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//                acsUtil.appDetailSettingsSwitchClickedTime = System.currentTimeMillis();
//            }
////        }
////        }
//        }
////            if ( getPackageName() event.getPackageName() )
//        if ( AcsUtil.isValidClickedControlSetting() ) {
////            if ( "com.android.settings".equals(event.getPackageName()) ) {
//                int eventType = event.getEventType();
////                if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//                    if (isControlSettingsAvaliableClick) {
//                        Handler rHandler = new Handler(Looper.getMainLooper());
//                        isControlSettingsAvaliableClick = false;
//                        rHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.d(TAG, "완료.OkClick");
//                                isControlSettingsAvaliableClick = true;
//                            }
//                        }, AcsUtil.CHANGE_SETTINGS_OK_PREVENT_INTERVAL_TIME);
//                        Log.d("ACS","SWITCH_OK_PREVENT_INTERVAL_TIME : " + System.currentTimeMillis());
//                        Log.d("ACS","(AcsUtil.appDetailSettingsClickedTime + SEND_EVENT_DELAY_TIME) + \" / \" + System.currentTimeMillis() : " + (AcsUtil.appDetailSettingsSwitchClickedTime + SEND_EVENT_DELAY_TIME) + " / " + System.currentTimeMillis());
//                        if ( AcsUtil.appDetailSettingsSwitchClickedTime + SEND_EVENT_DELAY_TIME + 3000 > System.currentTimeMillis()) {
//                            //                                                                  3000 보정.
//                            Log.d("ACS","SWITCH Click OK");
//                            List<String> doneInfos = StringsXmlValueUtil.getValuesNEnValues(mContext,"done");
////                          for( String btnStr: STRING_INFO.get(Utils.getLanguage(mContext))[1] ) {
//                            for( String btnStr: doneInfos ) {
//                                AccessibilityNodeInfo buttonNode = acsUtil.findButtonByText(rootNode, btnStr);
//                                if (buttonNode != null) {
//                                    buttonNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                                    Log.d("ACS","Done Button Click");
//                                    break;
//                                }
//                            }
////                            ascUtil.sendEvent(KEY_GLOBAL_ACTION_BACK);
//                            WorkQueue.nextTask();
//                        } else {
//                            Log.d("ACS","SWITCH Click NOT OK");
//                        }
//                    }
////                }
////            }
//        }
//    }
//}
//
