package net.softm.data.kata.util;


import static android.content.Context.POWER_SERVICE;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import net.softm.data.kata.AutoRunNotificationListenerService;
import net.softm.data.kata.ConnectAccessibilityService;
import net.softm.data.kata.MainActivity;
import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.PermissionType;
import net.softm.data.kata.kt.Callkt;
import net.softm.data.kata.kt.Timerkt;
import net.softm.data.kata.web.JavaScriptBridge;

import java.util.List;
import java.util.Set;

public class PermissionUtil {
    private static final String TAG = "PermissionUtil";

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 100;
    private static final int REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION = 111;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS_PERMISSION = 222;
    private static final int REQUEST_CODE_BLUETOOTH_PERMISSION = 101;
    public final static int REQUEST_CODE_OVERLAY_ENABLE = 3333;
    public final static int REQUEST_CODE_ALARM_ENABLE = 7777;
    public final static int REQUEST_CODE_USAGESTATS_ENABLE = 4444;

    public static final int REQUEST_CODE_GPS_ENABLE = 2001;

    public static final int REQUEST_CODE_INAPP_UPDATE = 10001;

    public static String[] REQUIRED_LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    };

    public static String[] REQUIRED_BACKGROUND_LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    };
    public static String[] REQUIRED_POST_NOTIFICATIONS_PERMISSIONS = {
            Manifest.permission.POST_NOTIFICATIONS,
    };

    public static String[] REQUIRED_BLUETOOTH_S_PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    public static String[] REQUIRED_BLUETOOTH_M_PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
    };

    static Handler checkHandler;
    public static boolean isPermissionForType(Context context, PermissionType permissionType) {
        boolean valid = true;
        if ( permissionType.equals(PermissionType.LOCATION) ) {
            valid = isLocationPermission(context);
        } else if ( permissionType.equals(PermissionType.BACKGROUND_LOCATION) ) {
            valid = isBackgroundLocationPermission(context);
        } else if ( permissionType.equals(PermissionType.POST_NOTIFICATIONS) ) {
            valid = isPostNotificationsPermission(context);
        } else if ( permissionType.equals(PermissionType.BLUETOOTH) ) {
            valid = isBluetoothPermission(context);
        } else if ( permissionType.equals(PermissionType.NOTIFICAION_ACCESS) ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                valid = hasNotificationAccessPermission(context);
            }
        } else if ( permissionType.equals(PermissionType.WRITE_SETTING) ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                valid = Settings.System.canWrite((Context)context);
            }
        } else if ( permissionType.equals(PermissionType.ACCESSIBILITY) ) {
            valid = hasAccessibilityPermissions() && PermissionUtil.isAccessibilityServiceRunning(context);
        } else if ( permissionType.equals(PermissionType.IGNORE_BATTERY) ) {
            valid = hasIgnoreBatteryPermissions();
        } else if ( permissionType.equals(PermissionType.OVERLAY) ) {
            valid = hasOverlayPermissions(context);
        } else if ( permissionType.equals(PermissionType.ALARM) ) {
            valid = hasAlarmPermissions(context);
        } else if ( permissionType.equals(PermissionType.USAGESTATS) ) {
            valid = hasUsageStatsPermission(context);
        }
        return valid;
    }

    public static boolean isPostNotificationsPermission(Context context) {
        if ( PackageUtil.getAndroidVersionId() < 13 ) return true;
        for (String permission : PermissionUtil.REQUIRED_POST_NOTIFICATIONS_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    public static boolean isBackgroundLocationPermission(Context context) {
        for (String permission : PermissionUtil.REQUIRED_BACKGROUND_LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLocationPermission(Context context) {
//        int hasFineLocationPermission   = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
//        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION);
//        int hasBluetoothScanPermission = ContextCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH_SCAN);
//        int hasBluetoothConnectPermission = ContextCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH_CONNECT);
//        int hasBluetoothPermission = ContextCompat.checkSelfPermission(context,Manifest.permission.BLUETOOTH);
//        return hasFineLocationPermission   == PackageManager.PERMISSION_GRANTED &&
//                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
////                hasBluetoothScanPermission == PackageManager.PERMISSION_GRANTED &&
////                hasBluetoothConnectPermission == PackageManager.PERMISSION_GRANTED
////                hasBluetoothPermission == PackageManager.PERMISSION_GRANTED
//                ;
        for (String permission : PermissionUtil.REQUIRED_LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBluetoothPermission(Context context) {
        String[] permsssions = {};
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ) {
            permsssions = REQUIRED_BLUETOOTH_S_PERMISSIONS;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permsssions = REQUIRED_BLUETOOTH_M_PERMISSIONS;
        }

        for (String permission : permsssions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    public static boolean hasNotificationAccessPermission(Context context) {
        if ( context != null ) {
            Set<String> sets = NotificationManagerCompat.getEnabledListenerPackages(context);
            if (sets != null && sets.contains(context.getPackageName())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean hasAccessibilityPermissions() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) (WApplication.mInstance).getSystemService(Context.ACCESSIBILITY_SERVICE);

        int accessibilityEnabled = 0;
        final String service = WApplication.mInstance.getPackageName() + "/" + ConnectAccessibilityService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    WApplication.mInstance.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            L.e( TAG, e.toString());
//            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    WApplication.mInstance.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);

                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 접근성 권한이 있는지 없는지 확인하는 부분
    // 있으면 true, 없으면 false
    public static boolean hasAccessibilityPermissions2() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) (WApplication.mInstance).getSystemService(Context.ACCESSIBILITY_SERVICE);

        // getEnabledAccessibilityServiceList는 현재 접근성 권한을 가진 리스트를 가져오게 된다
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT);

        for (int i = 0; i < list.size(); i++) {
            AccessibilityServiceInfo info = list.get(i);

            // 접근성 권한을 가진 앱의 패키지 네임과 패키지 네임이 같으면 현재앱이 접근성 권한을 가지고 있다고 판단함
            if (info.getResolveInfo().serviceInfo.packageName.equals(((WApplication.mInstance)).getPackageName())) {
                return true;
            }
        }
        return false;
    }


    public static boolean isAccessibilityServiceRunning(Context ctx) {
        if ( ctx == null ) return false;
        AccessibilityManager manager = (AccessibilityManager) ctx.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return (manager.isEnabled() && ConnectAccessibilityService.mContext != null);
    }

    public static boolean isAccessibilityServiceEnabled(Context context, String serviceClassName) {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo serviceInfo : enabledServices) {
            if (serviceInfo.getResolveInfo().serviceInfo.name.equals(serviceClassName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 저전력 활성화
     */
    public static boolean hasIgnoreBatteryPermissions() {
        PowerManager pm = (PowerManager) (WApplication.mInstance).getSystemService(POWER_SERVICE);
        boolean isWhiteListing = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations((WApplication.mInstance).getPackageName());
        }
        return isWhiteListing;
    }


    public static boolean hasUsageStatsPermission(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = null;
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean hasOverlayPermissions(Context context) {
        boolean valid = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            valid = Settings.canDrawOverlays(context);
        };
        return valid;
    }

    public static boolean hasAlarmPermissions(Context context) {
        boolean valid = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            valid = alarmManager.canScheduleExactAlarms();
        };
        return valid;
    }

    public static boolean onRequestPermissionsResult(Activity context, int permsRequestCode,
                                                  @NonNull String[] permissions,
                                                  @NonNull int[] grandResults) {
        if ( permsRequestCode == REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION ) {
            boolean isAllPermited = true;
            // 모든 퍼미션을 허용
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isAllPermited = false;
                    break;
                }
            }

            if (isBackgroundLocationPermission(context)) {
                JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun,"ok");
            } else {
//                  Utils.goAppDetailSettings(activity);
                JavaScriptBridge.callJavascriptFunc(callbackFunc,callbackGubun, "not");
//                    Toast.makeText(MainActivity.this, "위치 권한 없음.", Toast.LENGTH_SHORT).show();
                checkHandler = new Handler();
                checkHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isBackgroundLocationPermission(context)) {
                            checkHandler.postDelayed(this,Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);
                        } else {
//                            Utils.goMain(context,true);
                            if (WApplication.mainActivity != null) {
                                WApplication.mainActivity.finishAffinity();
                            }
                        }
                    }
                },Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);
            }

        } else if ( permsRequestCode == REQUEST_CODE_LOCATION_PERMISSION && grandResults.length == REQUIRED_LOCATION_PERMISSIONS.length) {
            boolean isAllPermited = true;
            // 모든 퍼미션을 허용
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    isAllPermited = false;
                    break;
                }
            }

            if ( "location".equals(callbackGubun) ) {
                if (isLocationPermission(context)) {
                    JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun,"ok");
//                    Toast.makeText(MainActivity.this, "위치 권한 확인 완료", Toast.LENGTH_SHORT).show();
                } else {
//                  Utils.goAppDetailSettings(activity);
                    JavaScriptBridge.callJavascriptFunc(callbackFunc,callbackGubun, "not");
//                    Toast.makeText(MainActivity.this, "위치 권한 없음.", Toast.LENGTH_SHORT).show();
                    checkHandler = new Handler();
                    checkHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isLocationPermission(context)) {
                                checkHandler.postDelayed(this,Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);
                            } else {
                                Utils.goMain(context,true);
                            }
                        }
                    },Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);
                }
            } else {
                if (isAllPermited) {
                    return true;
                } else {
                    // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                    boolean pms = true;
                    for (String permission : PermissionUtil.REQUIRED_LOCATION_PERMISSIONS) {
                        if ( ActivityCompat.shouldShowRequestPermissionRationale(context, permission) ) {
                            pms = false;
                        }
                    }

//                  if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_LOCATION_PERMISSIONS[0]) ||
//                      ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_LOCATION_PERMISSIONS[1])) {
//                      //					Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
//                      //					finish();
//                      //
                    if ( pms ) {
                        return true;
                    } else {
                        Toast.makeText(context, R.string.error_permission_for_location, Toast.LENGTH_LONG).show();
                    }
                }
            }
        } else  if ( permsRequestCode == REQUEST_CODE_BLUETOOTH_PERMISSION ) {
            String[] permsssions = {};
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ) {
                permsssions = REQUIRED_BLUETOOTH_S_PERMISSIONS;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permsssions = REQUIRED_BLUETOOTH_M_PERMISSIONS;
            }

            if (isBluetoothPermission(context)) {
                JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun,"ok");
            } else {
                JavaScriptBridge.callJavascriptFunc(callbackFunc,callbackGubun, "not");
            }
//            if ( grandResults.length == permsssions.length) {
//                Utils.goMain(context,true);
//            }

        }
        return false;
    }
    public static void onActivityResult(Activity context, int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_OVERLAY_ENABLE) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(context)) {
                    JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun, "ok");
//                    Toast.makeText(context, "오버레이 권한 확인 완료", Toast.LENGTH_SHORT).show();
                } else {
                    JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun, "not");
//                    Toast.makeText(context, "오버레이 권한 없음.", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_CODE_USAGESTATS_ENABLE) {
            if (hasUsageStatsPermission(context)) {
                JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun,"ok");
//                Toast.makeText(context, "사용정보 접근 허용 확인 완료", Toast.LENGTH_SHORT).show();
            } else {
                JavaScriptBridge.callJavascriptFunc(callbackFunc,callbackGubun, "not");
//                Toast.makeText(context, "사용정보 접근 허용 권한 없음.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_GPS_ENABLE) {
            if ( isLocationPermission(context) ) {
//                initHome();
            }
        } else if (requestCode == REQUEST_CODE_INAPP_UPDATE) {
            if (resultCode != context.RESULT_OK) {
                Log.d("AppUpdate", "Update flow failed! Result code: " + resultCode);
                Utils.toast(context, R.string.msg_inapp_update);
//                context.finishAffinity(); // TODO SOFTM : 앱 종료 안하게(유연한 업데이트) : 2024.12.20
            }
        }
    }
    public static void goToNotificationSettings(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android Oreo (API 26) and above, navigate to the Notification Settings for the app
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
            activity.startActivity(intent);
        } else {
            // For versions below Oreo, you can redirect to the app settings page or a general settings page
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivity(intent);
        }
    }

    public static void request(Activity activity,PermissionType permissionType,String[] permsssions) {
        if ( permissionType.equals(PermissionType.BACKGROUND_LOCATION) ) {
            boolean pms = true;
            for (String permission : permsssions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    pms = false;
                }
            }
            if (pms) {
                ActivityCompat.requestPermissions(activity, permsssions, REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION);
            } else {
                ActivityCompat.requestPermissions(activity, permsssions, REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION);
            }
        } else if ( permissionType.equals(PermissionType.POST_NOTIFICATIONS) ) {
            boolean pms = true;
            for (String permission : permsssions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
			        setPermissionDoDeniedForPostNotifications(true);
                    pms = false;
                }
            }
            if ( isPermissionDoDeniedForPostNotifications() ) {
                ((MainActivity)activity).alert(activity, R.string.error_permission_for_post_notifications, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        goToNotificationSettings(activity);
                    }
                });
            } else {
                if (pms) {
                    ActivityCompat.requestPermissions(activity, permsssions, REQUEST_CODE_POST_NOTIFICATIONS_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions(activity, permsssions, REQUEST_CODE_POST_NOTIFICATIONS_PERMISSION);
                }
            }
        } else if ( permissionType.equals(PermissionType.LOCATION) ) {
            requestLocation(activity, null);
        } else if ( permissionType.equals(PermissionType.BLUETOOTH) ) {
            // 2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            boolean pms = true;
            for (String permission : permsssions) {
                if ( ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) ) {
                    pms = false;
                }
            }
            if ( pms ) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
//                Toast.makeText(context, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(activity, permsssions, REQUEST_CODE_BLUETOOTH_PERMISSION);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(activity, permsssions, REQUEST_CODE_BLUETOOTH_PERMISSION);
            }
        } else if ( permissionType.equals(PermissionType.NOTIFICAION_ACCESS) ) {
            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS);
                ComponentName componentName = new ComponentName(activity.getPackageName(), AutoRunNotificationListenerService.class.getName());
                intent.putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, componentName.flattenToString());
            } else {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            }
            activity.startActivity(intent);
        } else if ( permissionType.equals(PermissionType.WRITE_SETTING) ) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + (WApplication.mInstance).getPackageName()));
            activity.startActivity(intent);
        } else if ( permissionType.equals(PermissionType.ACCESSIBILITY) ) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
//

            ComponentName componentName = new ComponentName(activity, ConnectAccessibilityService.class);

//            intent.setComponent(componentName);

//            intent.setClassName(context, ConnectAccessibilityService.class.getName());
//            intent.putExtra(Intent.EXTRA_COMPONENT_NAME,)

//            intent.setData(Uri.parse("package:" + context.getPackageName()));

            activity.startActivity(intent);
        } else if ( permissionType.equals(PermissionType.IGNORE_BATTERY) ) {
            requestIgnoreBatteryOptimizations(activity,null);
        } else if ( permissionType.equals(PermissionType.OVERLAY) ) {
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
            activity.startActivityForResult(intent, REQUEST_CODE_OVERLAY_ENABLE);
        } else if ( permissionType.equals(PermissionType.ALARM) ) {
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, uri);
//            intent.addCategory(Intent.CATEGORY_DEFAULT);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } else if ( permissionType.equals(PermissionType.LOCATION_GPS) ) {
            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivityForResult(callGPSSettingIntent, PermissionUtil.REQUEST_CODE_GPS_ENABLE);
        } else if ( permissionType.equals(PermissionType.USAGESTATS) ) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//            intent.putExtra("finishActivityOnSaveCompleted", true);
            activity.startActivityForResult(intent, REQUEST_CODE_USAGESTATS_ENABLE);
        }
    }
    public static void requestLocation(Activity activity, Runnable runnable) {
//        permsssions = REQUIRED_LOCATION_PERMISSIONS;
//        reqCode = REQUEST_CODE_LOCATION_PERMISSION;

        // 2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
        // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
        boolean pms = true;
        for (String permission : REQUIRED_LOCATION_PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                pms = false;
            }
        }
        if (pms) {
            // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
//                Toast.makeText(context, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
            ActivityCompat.requestPermissions(activity, REQUIRED_LOCATION_PERMISSIONS, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
            // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
            ActivityCompat.requestPermissions(activity, REQUIRED_LOCATION_PERMISSIONS, REQUEST_CODE_LOCATION_PERMISSION);
        }

        if ( runnable != null ) {
            new Timerkt(30, (remainingTime, gubun, timer) -> {
                if (PermissionUtil.isLocationPermission(activity)) {
                    if (runnable != null) {
                        new Callkt(gubun).run((params)->{
                            Utils.runExecutorService(runnable);
                            return null;
                        });
                        timer.stop();
                    }
                }
                return null;
            }, (params) -> {
                L.log(activity, TAG, "Timerkt.complete");
                return null;
            }, "PermissionUtil.requestLocation").run();
        }
    }
    public static void requestIgnoreBatteryOptimizations(Context context, Runnable runnable) {
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if ( runnable != null ) {
            new Timerkt(30, (remainingTime, gubun, timer) -> {
                if (PermissionUtil.hasIgnoreBatteryPermissions()) {
                    if (runnable != null) {
                        new Callkt(gubun).run((params)->{
                            Utils.runExecutorService(runnable);
                            return null;
                        });
                        timer.stop();
                    }
                }
                return null;
            }, (params) -> {
                L.log(context, TAG, "Timerkt.complete");
                return null;
            }, "PermissionUtil.requestIgnoreBatteryOptimizations").run();
        }
    }

    static String callbackGubun = null;
    static String callbackFunc = null;
    public static String checkHandlerGubun = null;
    public static void stopHandler(Activity context) {
        if ( checkHandler != null ) {
            if ( "IGNORE_BATTERY".equals(checkHandlerGubun) ) {
                if (PermissionUtil.hasIgnoreBatteryPermissions()) {
//                    Utils.goPermission(context,"accessibility","");
                    Utils.goMain(context);
                    checkHandlerGubun = "";
                }
            } else if ( PermissionType.POST_NOTIFICATIONS.getId().equals(checkHandlerGubun) ) {
                if (PermissionUtil.isPostNotificationsPermission(context)) {
//                    Utils.toast(context,R.string.msg_post_notifications_complete);
//                    Utils.goMain(context);
                    JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun, "ok");
                    checkHandlerGubun = "";
//                    WApplication.mainActivity.finish();
                }
            }
            checkHandler.removeCallbacksAndMessages(null);
        }
    }
    public static void startRequestPermission(Context context, PermissionType permissionType) {
        startRequestPermission(context, permissionType,null,null);
    }

    public static void startRequestPermission(Context context, PermissionType permissionType, String _callbackGubun, String _callbackFunc) {
        callbackGubun = _callbackGubun;
        callbackFunc = _callbackFunc;
        boolean valid = false;
        String[] permsssions = {};
        int reqCode = 0;

        valid = isPermissionForType(context,permissionType);

        if ( permissionType.equals(PermissionType.BACKGROUND_LOCATION) ) {
            permsssions = REQUIRED_BACKGROUND_LOCATION_PERMISSIONS;
            reqCode = REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION;
        } else if ( permissionType.equals(PermissionType.LOCATION) ) {
            permsssions = REQUIRED_LOCATION_PERMISSIONS;
            reqCode = REQUEST_CODE_LOCATION_PERMISSION;
        } else if ( permissionType.equals(PermissionType.BLUETOOTH) ) {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ) {
                permsssions = REQUIRED_BLUETOOTH_S_PERMISSIONS;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permsssions = REQUIRED_BLUETOOTH_M_PERMISSIONS;
            }
        } else if ( permissionType.equals(PermissionType.NOTIFICAION_ACCESS)) {
        } else if ( permissionType.equals(PermissionType.WRITE_SETTING)) {
        } else if ( permissionType.equals(PermissionType.POST_NOTIFICATIONS) ) {
            permsssions = REQUIRED_POST_NOTIFICATIONS_PERMISSIONS;
            reqCode = REQUEST_CODE_POST_NOTIFICATIONS_PERMISSION;
        }

        if ( !valid ) {
            request((Activity)context,permissionType,permsssions);
            if ( permissionType.equals(PermissionType.BLUETOOTH) ) {
                //
            } else {
                checkPermission(context,permissionType,_callbackGubun, _callbackFunc);
            }
        } else {
            JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun, "ok");
//            if ( "location".equals(callbackGubun) ) {
//                this.callbackGubun = "";
//                this.callbackFunc = "";
//            }
        }
    }


    // 잠금 및 보안 설정 창 이동하기
//    public void startSettingSecuritySetting(String callbackGubun, String callbackFunc) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(MainActivity.this)) {        //ask for permission
//            Intent intent = new Intent();
//            intent.setAction(Settings.ACTION_SECURITY_SETTINGS);
//            intent.setData(Uri.parse("package:" + (WApplication.mInstance).getPackageName()));
//            startActivity(intent);
//
//            checkHandler = new Handler();
//            checkHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (!Settings.System.canWrite(MainActivity.this)) {
//                        checkHandler.postDelayed(this,Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);
//                    } else {
////                    Utils.goMain(MainActivity.this,true);
//                        MapUtil.completeFirstStart(); // TODO softm 테스트후.순서 바뀌면 제거.
//                        JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun, "ok");
//                    }
//                }
//            },Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);
//        } else {
//            JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun, "ok");
//        }
//    }



    /**
     * 알림 On/Off
     * @param callbackGubun
     * @param callbackFunc
     */
    public static void startSettingNotificationSetting(Context ctx, String callbackGubun, String callbackFunc) {
        Intent intent = Utils.getNotiSettingIntent(ctx);
        ctx.startActivity(intent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ctx);
        boolean currV = notificationManagerCompat.areNotificationsEnabled();

        checkHandler = new Handler();
        checkHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if ( currV == notificationManagerCompat.areNotificationsEnabled() ) {
                    checkHandler.postDelayed(this,Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);
                } else {
//                    Utils.goMain(MainActivity.this,true);
                    JavaScriptBridge.callJavascriptFunc(callbackFunc, callbackGubun, notificationManagerCompat.areNotificationsEnabled()?"ok":"not");
                }
            }
        },Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);
    }

    public static void checkPermission(Context context, PermissionType permissionType,String _callbackGubun, String _callbackFunc) {
        checkHandler = new Handler();
        checkHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                L.d(TAG,"checkPermission",permissionType.name());
                if (!isPermissionForType(context, permissionType)) {
                    checkHandlerGubun = permissionType.getId();
                    checkHandler.postDelayed(this, Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);
                } else {
                    if (permissionType.equals(PermissionType.OVERLAY)) {
//                        MapUtil.completeFirstStart();
                        /*TODO softm Remove : Notification 기존채널 삭제. : (마지막 권한 - 1) 번째 권한 한번만 실행하게. 현제는 위치정보 접근 권한 설정이 이에 해당됨. */
                        /*TODO softm Remove : Notification 기존채널 삭제. : 처음 권한이 오버레이 권한 설정후 기존 채널정보 삭제. */
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationManager notiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            try {
                                notiManager.deleteNotificationChannel(NotificationUtil.CHANNEL_ID1);
                                notiManager.deleteNotificationChannel(NotificationUtil.CHANNEL_ID2);
                                notiManager.deleteNotificationChannel(NotificationUtil.CHANNEL_ID3);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (permissionType.equals(PermissionType.ACCESSIBILITY)) {
                        // TODO SOFTM : Logging : 앱 설치하고 몇번 접근성 활성화 했는지 추적
                        MapUtil.increaseAccessibilityServiceEnableCount();

                        Fa.i(context,TAG,Fa.EVENT_ACS_ENABLE);
                        Fc.i(context,TAG,Fc.CUSTOM_KEY_ACS_ENABLE_COUNT,MapUtil.getAccessibilityServiceEnableCount());
                        Fc.setCustomKey(Fc.CUSTOM_KEY_ACS_ENABLE_COUNT,MapUtil.getAccessibilityServiceEnableCount());

                        if ( MapUtil.isOverlayOpen() ) { // 권한 메시지 갱신 : 권한 메시지 클리어 위해
                            OverlayViewUtil.redrawPermission();
                        }
                    }
                    L.d(TAG,"checkPermission",permissionType.name(),"OK");
//                        checkHandler.removeCallbacksAndMessages(null);
                    JavaScriptBridge.callJavascriptFunc(_callbackFunc, _callbackGubun, "ok");
                }
            }
        }, Const.SETTING_CHANGE_CHECK_INTERVAL_TIME);
    }


    public static boolean isPermissionDoDeniedForPostNotifications() {
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_PERMISSION_DODENIED_POST_NOTIFICATIONS, "0");
        return  v.equals("0")?false:true;
    }

    public static void setPermissionDoDeniedForPostNotifications(boolean v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_PERMISSION_DODENIED_POST_NOTIFICATIONS, v?"1":"0");
    }
}

