package net.softm.data.kata;

import static net.softm.data.kata.util.MapUtil.loadAppPackage;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.OrientationEventListener;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.kakao.sdk.common.KakaoSdk;

import net.softm.data.kata.common.AppType;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.InstallType;
import net.softm.data.kata.common.NaviStatus;
import net.softm.data.kata.common.OrientationType;
import net.softm.data.kata.common.ParamCallback;
import net.softm.data.kata.common.UserLevel;
import net.softm.data.kata.common.UserType;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.dto.AppInfoDTO;
import net.softm.data.kata.dto.BasicInfoDTO;
import net.softm.data.kata.dto.base.BasicSettings;
import net.softm.data.kata.geofence.GeofenceBroadcastReceiver;
import net.softm.data.kata.geofence.GeofenceConstants;
import net.softm.data.kata.geofence.GeofenceErrorMessages;
import net.softm.data.kata.god.dto.PrayClass;
import net.softm.data.kata.kt.Callbackkt;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.AlarmUtil;
import net.softm.data.kata.util.BillingUtil;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.Fb;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.LicenseUtil;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.NotificationUtil;
import net.softm.data.kata.util.OrgUtil;
import net.softm.data.kata.util.PermissionUtil;
import net.softm.data.kata.util.RestUtil;
import net.softm.data.kata.util.Utils;
import net.softm.data.kata.util.WorkerUtil;
import net.softm.lib.DefaultApplication;
import net.softm.lib.common.WLog;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WApplication
 * @author softm
 */
public class WApplication extends DefaultApplication implements OnCompleteListener<Void> {

	private static final String TAG = "WApplication";
	private static final String TAG_SHORT = "WA";

	public static WApplication mInstance = null;
	public static BillingUtil billingUtil = null;
	public static BasicInfoDTO basicInfoDTO = null;
	public static BasicSettings basicSettings = null;
	public static Activity mainActivity = null;
	public static boolean appUpdateAvailable = false;
	public static AppInfoDTO appInfo = null;
	public static AppInfoDTO musicAppInfo = null;
	public static String testValue = "300";
	public static String testValue1 = "0.7";
	public static String testValue2 = "0.1";
	public static String testValue3 = "0.5";
	public static String testValue4 = "0.5";
	public static String testValue5 = "";

	/** Org */
	public static String org;

	/** OrgReg */
	public static String orgReg;

	public UserType userType = UserType.FREE_USER;;
	public UserLevel userLevel = UserLevel.FREE;;

	public Handler mHandler = new Handler();

	HashMap<String, Object> mStorage = new HashMap<String, Object>();

	public PackageInfo mPackageInfo;
	public TelephonyManager mTelephonyManager;
	private SharedPreferences mSharedPre;
	/**
	 * Provides access to the Geofencing API.
	 */
	public GeofencingClient mGeofencingClient;

	/**
	 * The list of geofences used in this sample.
	 */
	public ArrayList<Geofence> mGeofenceList;

    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum PendingGeofenceTask { // SOFTM ADDED CLEAR
        ADD, REMOVE, NONE, CLEAR
    }

	/**
	 * Removes geofences. This method should be called after the user has granted the location
	 * permission.
	 */
	@SuppressWarnings("MissingPermission")
	public void removeGeofences() {
        mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
		WApplication.mInstance.mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
		MapUtil.setNaviEndTime();
	}

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    public void clearGeofences() {
        mPendingGeofenceTask = PendingGeofenceTask.CLEAR;
        WApplication.mInstance.mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
        MapUtil.setNaviEndTime(0L);
    }

	// toast
	public void toast( String value ) {
		Toast.makeText(this, value, Toast.LENGTH_LONG).show();
	}
	/**
	 * Adds geofences. This method should be called after the user has granted the location
	 * permission.
	 */
	@SuppressWarnings("MissingPermission")
	public void addGeofences(String name, float lat , float lon ) {
		Handler mHandler = new Handler(Looper.getMainLooper());
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG,"add fence : " + name + " / " + lat + " / "  + lon );
			}
		}, 0);

		// Empty list for storing geofences.
		mGeofenceList = new ArrayList<>();
		mGeofenceList.add(new Geofence.Builder()
				// Set the request ID of the geofence. This is a string to identify this
				// geofence.
				.setRequestId(name)

				// Set the circular region of this geofence.
				.setCircularRegion(
						lat,
						lon,
						GeofenceConstants.GEOFENCE_RADIUS_IN_METERS
				)

				// Set the expiration duration of the geofence. This geofence gets automatically
				// removed after this period of time.
				.setExpirationDuration(GeofenceConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

				// Set the transition types of interest. Alerts are only generated for these
				// transition. We track entry and exit transitions in this sample.
				.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
						Geofence.GEOFENCE_TRANSITION_EXIT)

				// Create the geofence.
				.build()
		);
        mPendingGeofenceTask = PendingGeofenceTask.ADD;
		WApplication.mInstance.mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent()).addOnCompleteListener(this);
		MapUtil.setNaviEndTime(0);
	}
	private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;


	/**
	 * Used when requesting to add or remove geofences.
	 */
	private PendingIntent mGeofencePendingIntent;
	@Override
	public void onComplete(@NonNull Task<Void> task) {
//		mPendingGeofenceTask = PendingGeofenceTask.NONE;
		if (task.isSuccessful()) {
//			int messageId = getGeofencesAdded() ? R.string.geofences_added :
//					R.string.geofences_removed;
//			Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
//			toast("geo");
			Geofence fence0 = null;
			String fenceInfo = null;
			String gubun = "";
			String notiDetail = "";
			String strToast = "";
			NaviStatus naviStatus = null;
			if ( WApplication.mInstance.mGeofenceList != null && WApplication.mInstance.mGeofenceList.size() > 0 ) {
				fence0 = WApplication.mInstance.mGeofenceList.get(0);
				fenceInfo = fence0.getRequestId();
				String[] items = fenceInfo.split("##");
				for(int i=0;i<items.length;i++) {
					Log.d(TAG,items[i]);
				}
				notiDetail = items[0];
				strToast = (items.length<=3?"시작":items[3].equals("REFENCE")?"재설정":"시작");
				naviStatus = (items.length<=3?NaviStatus.START:gubun.equals("REFENCE")?NaviStatus.REFENCE:NaviStatus.START); // S : 안내시작, R : 안내재설정, E : 안내완료
			}
//                P_NAME+"##" + P_LAT + "##" + P_LON
            if (mPendingGeofenceTask.equals(PendingGeofenceTask.ADD) ) {
                toast("안내 " +  strToast + "");
//				String naviStatus = (items.length<=3?" 안내시작":gubun.equals("REFENCE")?" 안내재설정":" 안내시작")
				NotificationUtil.init(WApplication.this).getNotification(notiDetail,naviStatus);
            } else if (mPendingGeofenceTask.equals(PendingGeofenceTask.REMOVE) ) {
				toast("안내 완료");
				NotificationUtil.init(WApplication.this).getNotification(notiDetail,NaviStatus.END); // 안내완료
				mGeofenceList = new ArrayList<>();
				Handler mHandler = new Handler(Looper.getMainLooper());
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						NotificationUtil.init(WApplication.this).getNotification(getString(R.string.title_noti_runnig_title),NaviStatus.RUNNING);
//						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//							startConnectServiceForNoti(getString(R.string.title_noti_runnig),NaviStatus.RUNNING);
//						} else {
//							NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//							notificationManager.cancelAll();
//						}
					}
				}, Const.NAVI_END_NOTIFICATION_CANCEL_TIME);// 60000
            } else if (mPendingGeofenceTask.equals(PendingGeofenceTask.CLEAR) ) {
//                toast("안내취소");
                mGeofenceList = new ArrayList<>();
				NotificationUtil.init(WApplication.this).getNotification(getString(R.string.title_noti_runnig_title),NaviStatus.RUNNING);
            }
		} else {
			// Get the status code for the error and log it using a user-friendly message.
			String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
			Log.w(TAG, errorMessage);
		}

	}

	/**
	 * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
	 * Also specifies how the geofence notifications are initially triggered.
	 */
	private GeofencingRequest getGeofencingRequest() {
		GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

		// The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
		// GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
		// is already inside that geofence.
		builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

		// Add the geofences to be monitored by geofencing service.
		builder.addGeofences(mGeofenceList);

		// Return a GeofencingRequest.
		return builder.build();
	}

	/**
	 * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
	 * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
	 * current list of geofences.
	 *
	 * @return A PendingIntent for the IntentService that handles geofence transitions.
	 */
	public PendingIntent getGeofencePendingIntent() {
		// Reuse the PendingIntent if we already have it.
		if (mGeofencePendingIntent != null) {
			return mGeofencePendingIntent;
		}
		Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
		// addGeofences() and removeGeofences().
		mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		return mGeofencePendingIntent;
	}

	/**
	 * This sample hard codes geofence data. A real app might dynamically create geofences based on
	 * the user's location.
	 */
	private void populateGeofenceList() {
		for (Map.Entry<String, LatLng> entry : GeofenceConstants.BAY_AREA_LANDMARKS.entrySet()) {

			mGeofenceList.add(new Geofence.Builder()
					// Set the request ID of the geofence. This is a string to identify this
					// geofence.
					.setRequestId(entry.getKey())

					// Set the circular region of this geofence.
					.setCircularRegion(
							entry.getValue().latitude,
							entry.getValue().longitude,
							GeofenceConstants.GEOFENCE_RADIUS_IN_METERS
					)

					// Set the expiration duration of the geofence. This geofence gets automatically
					// removed after this period of time.
					.setExpirationDuration(GeofenceConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

					// Set the transition types of interest. Alerts are only generated for these
					// transition. We track entry and exit transitions in this sample.
					.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
							Geofence.GEOFENCE_TRANSITION_EXIT)

					// Create the geofence.
					.build()
			);
		}
	}

	public void cancelAllNotification() {
		Handler mHandler = new Handler(Looper.getMainLooper());
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.cancelAll();
			}
		}, 0);// 0
	}


	public Callbackkt overlayViewServiceCallbackkt;

	// uncaught exception handler variable
	private Thread.UncaughtExceptionHandler defaultUEH;

	// handler listener
	private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
			new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread thread, Throwable ex) {
					Fa.e(WApplication.mInstance,TAG,"uncaught_exception",ex.toString());
					Fc.e(WApplication.mInstance,TAG,"uncaught_exception",ex);
					Fc.recordException(ex);
					String exStackTraceString = L.getStackTraceString(ex);
//					if ( AdUtil.isCheckGoogleBannerError(exStackTraceString) ) {
					int errIndex = AdUtil.isGoogleAdViewErrorCase(exStackTraceString);
					if ( errIndex > -1 ) {
						AdUtil.setAdViewErrorForGoogle(true);
						AdUtil.setAdViewErrorForGoogleCaseIndex(errIndex);
					}

					int errIndexPaid = LicenseUtil.isGooglePaidErrorCase(exStackTraceString);
					if ( errIndexPaid > -1 ) {
						LicenseUtil.setPaidErrorForGoogle(true);
						LicenseUtil.setPaidErrorForGoogleCaseIndex(errIndexPaid);
					}

//					Fc.logCrashToFirestore(WApplication.mInstance, ex, System.currentTimeMillis());
					WorkerUtil.crashLog(WApplication.mInstance, exStackTraceString, System.currentTimeMillis());
					if ( Fc.isValildCrash() ) {
						L.d(TAG, "uncaughtException.isValildCrash");
						Fc.e(WApplication.mInstance,TAG,"do_launch_main");
						Fc.setCrashError(true);
						Fc.updateLastCheckCrashTime(); /** 마지막 크래싱 */
						/*******************************************************************/
						/*** MainActivity 호출을 Fc.logCrashToFirestore Worker 내부에서 실행하게 */
						/** 변경하자 MainActivity가 실행안되는 경우가 있어
						 *  >> 변경하려고했으나 , 더 올바르지 않음... */
						/**
						 * 메인 화면 호출 안함 : 20250313
							Intent intent = new Intent(getContext(), MainActivity.class);
							// 최근 앱 목록에서 제외되도록 설정
							intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
							intent.putExtra("UNCAUGHT_EXCEPTION","UNCAUGHT_EXCEPTION");

							// here I do logging of exception to a db
							PendingIntent myActivity = PendingIntent.getActivity(getContext(),
									192837, intent,
									PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

							AlarmManager alarmManager;
							alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

							alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
								10000, myActivity );
						 */
					} else {
						Fc.e(WApplication.mInstance,TAG,"dosenot_launch_main");
						String formatTime = Utils.millisecondsToTime(WApplication.basicInfoDTO.checkCrashIntervalTime - ( System.currentTimeMillis() - Fc.getLastCheckCrashTime()));
						L.d(TAG, "uncaughtException.!isValildCrash", "비정상종료 반복 발생으로 메인 재실행 방지 : " + formatTime);
						Utils.toast(WApplication.mInstance, "비정상종료 반복 발생으로 메인 재실행 방지 : " + formatTime);
					}
					System.exit(2);

					// re-throw critical exception further to the os (important)
					defaultUEH.uncaughtException(thread, ex);
				}
	};
	private OrientationEventListener orientationEventListener;
	public boolean isLandscape;
	public OrientationType orientationType = OrientationType.PORTRAIT;
	@Override
	public void onCreate() {
//		WUrl.devMode = WUrl.EnumDevMode.valueOf(BuildConfig.BUILD_TYPE.toUpperCase());
		L.i(TAG, "Application:Create ###############################");
		mInstance = this;
		/**
		 * 운영 기본 정보 조회
		 */
		RestUtil.getBasicInfo(mInstance,()->{
			String currentVersionCode = String.valueOf(BuildConfig.VERSION_CODE);
			String savedVersionCode = getSharedPreferences("version_code", "");

			/** onAppUpdated */
			if (currentVersionCode != savedVersionCode) {
				onAppUpdated(savedVersionCode, currentVersionCode);
				// 업데이트된 버전 저장
				setSharedPreferences("version_code", currentVersionCode);
			} else {
				onAppUpdated(savedVersionCode, currentVersionCode);
			}
		},false);

		OrgUtil.setOrg("softmch1");
		OrgUtil.setOrgReg("softmch1");
		WApplication.mInstance.org = OrgUtil.getOrg();
		WApplication.mInstance.orgReg = OrgUtil.getOrgReg();

		RestUtil.getBasicSettings(mInstance,()->{
			AlarmUtil.bindAlarm(this, new ParamCallback<List<PrayClass>>() {
				@Override
				public void onResult(List<PrayClass> result) {
					if ( result.size() > 0 ) {
						L.d(TAG,"bindAlarm",TAG);
						Utils.toast(WApplication.mInstance, "기도모임이 있습니다.");
					}
				}
			});
		},false);

/*** TODO SOFTM : REMOVE UNCAUGHT EXCEPTION HANDLER
 ***/
		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		// setup handler for uncaught exception
		Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);

//		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

		WUrl.ReqId.WEB_MAIN.getReqUrl();
		WUrl.ReqId.WEB_PERMISSION.getReqUrl();
		WUrl.ReqId.WEB_STATE.getReqUrl();
		WUrl.ReqId.WEB_VERSION.getReqUrl();
		WUrl.ReqId.WEB_ERROR.getReqUrl();
		WUrl.ReqId.WEB_NOTICE.getReqUrl();
		WUrl.ReqId.WEB_APP_IRON.getReqUrl();
		WUrl.ReqId.GET_CERT_URL.getReqUrl();

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.createInstance(this);
		}

		try {
			mPackageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		} catch (PackageManager.NameNotFoundException e) {
			WLog.printException(e);
		}

		GeofenceConstants.init();
		Const.init();
		GeofenceConstants.GEOFENCE_RADIUS_IN_METERS = Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_GEOFENCE_RADIUS_IN_METERS, String.valueOf(GeofenceConstants.GEOFENCE_RADIUS_IN_METERS)));
		Const.NAVI_OVERLAY_COUNTDOWN_TIME           = Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_OVERLAY_WAIT_TIME,String.valueOf(Const.NAVI_OVERLAY_COUNTDOWN_TIME/1000))).intValue() * 1000;
		Const.NAVI_RENAVI_PREVENT_TIME              = Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RENAVI_PREVENT_TIME,String.valueOf(Const.NAVI_RENAVI_PREVENT_TIME/1000))).intValue() * 1000;
		Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME  = Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_NAVI_DESTINATION_EXECUTION_HOLD_TIME,String.valueOf(Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME))).intValue();
		WApplication.mInstance.testValue = WApplication.mInstance.getSharedPreferences("testValue", WApplication.mInstance.testValue);
		WApplication.mInstance.testValue1 = WApplication.mInstance.getSharedPreferences("testValue1",WApplication.mInstance.testValue1);
		WApplication.mInstance.testValue2 = WApplication.mInstance.getSharedPreferences("testValue2",WApplication.mInstance.testValue2);
		WApplication.mInstance.testValue3 = WApplication.mInstance.getSharedPreferences("testValue3",WApplication.mInstance.testValue3);
		WApplication.mInstance.testValue4 = WApplication.mInstance.getSharedPreferences("testValue4",WApplication.mInstance.testValue4);

		Const.RUN_STATUS_SCREEN_TIME_FOR_START = Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_START,String.valueOf(Const.RUN_STATUS_SCREEN_TIME_FOR_START /1000))).intValue() * 1000;
		Const.RUN_STATUS_SCREEN_TIME_FOR_STOP = Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_STOP,String.valueOf(Const.RUN_STATUS_SCREEN_TIME_FOR_STOP /1000))).intValue() * 1000;
		Const.RUN_SPEED_LIMIT_TIME  = Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RUN_SPEED_LIMIT_TIME,String.valueOf(Const.RUN_SPEED_LIMIT_TIME))).intValue();
		Const.RUN_SPEED_LIMIT  = Float.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_RUN_SPEED_LIMIT,String.valueOf(Const.RUN_SPEED_LIMIT))).intValue();

		Const.APP_UPDATE_DELAY_TIME = Long.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_APP_UPDATE_DELAY_TIME,String.valueOf(Const.APP_UPDATE_DELAY_TIME)));

		userType = LicenseUtil.getUserType();
		userLevel = LicenseUtil.getUserLevel();

		MapUtil.initNavingForNotiListener();
		MapUtil.initNaving();
		MapUtil.clearPoiRecent();
		MapUtil.setPostNotificationsConfirmCount(String.valueOf(0));
		PermissionUtil.setPermissionDoDeniedForPostNotifications(false);

//		L.d(TAG,"onCreate-setGoogleBannerError");
//		AdUtil.setAdViewErrorForGoogle(false);

		connectServiceMode = MapUtil.isNotiIcon()?ConnectServiceMode.NOTIFICATION_SERVICE:ConnectServiceMode.ACCESSIBILITY_SERVICE;
		workType = MapUtil.isEnhancedExecution()?WorkType.WORK_MANAGER:WorkType.THREAD;

		Fa.setDefaultEventParameters(this);

		loadAppPackage(MapUtil.getAppPackage(), AppType.NAVI);
		loadAppPackage(MapUtil.getMusicAppPackage(), AppType.MUSIC);

		try {
			KakaoSdk.init(this, "759e945d7bd014d4f93c9fde7392d9d4");
		} catch (Exception ex) {
		}

		overlayViewServiceCallbackkt = new Callbackkt(1000,"overlayViewServiceCallbackkt", true);

		// register to be informed of activities starting up
		registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

			@Override
			public void onActivityCreated(Activity activity,
										  Bundle savedInstanceState) {

			}

			@Override
			public void onActivityStarted(Activity activity) {

			}

			@Override
			public void onActivityResumed(Activity activity) {
				if ( activity instanceof MainActivity ) {
					billingUtil = BillingUtil.getInstance(activity);
					L.d(TAG,"onActivityResumed","billingUtil.getInstance(activity)");
				}
			}

			@Override
			public void onActivityPaused(Activity activity) {

			}

			@Override
			public void onActivityStopped(Activity activity) {

			}

			@Override
			public void onActivitySaveInstanceState(Activity activity,
													Bundle outState) {

			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				if ( activity instanceof MainActivity ) {
					if ( billingUtil!= null) {
						billingUtil.endConnection();
						L.d(TAG, "onActivityDestroyed", "billingUtil.endConnection()");
					}
				}
//                try {
//                	if ( db != null ) {
//                		db.close();
//                	}
//                } catch (SQLiteException se ) {
//                    Log.e(getClass().getSimpleName(), "error >> db.close();");
//                } finally {
//                	db = null;
//                    dbHelper = null;
//                }
			}
		});
		super.onCreate();
	}

	private void onAppUpdated(String oldVersion, String newVersion) {
		MapUtil.setAppUpdated(false);
		MapUtil.setAppUpdatedNotiConfirm(false);
		if (StringUtils.isEmpty(oldVersion)) { /** 처음 설치됨 */
			Fa.i(this,TAG,Fa.EVENT_APPUPDATE_FIRST_INSTALL,newVersion);
			MapUtil.setAppInstalled(false);
			Fb.userRegister(this, InstallType.NEW_INSTALL, oldVersion,newVersion); /** uuid기록 */
		} else if (newVersion.equals(oldVersion)) { /** 재설치됨 */
			Fa.i(this,TAG,Fa.EVENT_APPUPDATE_SAME_INSTALL,oldVersion);
			Fb.userRegister(this, InstallType.RE_INSTALL, oldVersion,newVersion); /** uuid기록 */
		} else { /** 업데이트됨 */
			MapUtil.setAppUpdated(true);
			Fa.i(this,TAG,Fa.EVENT_APPUPDATE_UPDATED,oldVersion + "_" + newVersion);
			Fb.userRegister(this, InstallType.UPDATE_INSTALL, oldVersion,newVersion); /** uuid기록 */
		}

		MapUtil.setAppUpdatedTime();
	}
	@Override
	public void onTerminate() {
		WLog.i(TAG, "Application:Terminate ############################");
		super.onTerminate();
	}

	protected void onInitDataBase() {
	}

	/**
	 * 사진목록에서 선택한 사진 데이터
	 */
	public byte[] mPictureData;

	/**
	 * OCR에서 촬영한 신분증 사진 데이터
	 */
	public byte[] mOcrData;

	public String mLinkUrl;

	/**
	 * 푸쉬 JSON : 앱이 백그라운드에 있을때 노티를 누를때만 사용됨.
	 */
	public String mPushJson;
	/**
	 * 푸쉬 Title : 앱이 백그라운드에 있을때 노티를 누를때만 사용됨.
	 */
	public String mPushTitle;
	/**
	 * 푸쉬 Ext : 앱이 백그라운드에 있을때 노티를 누를때만 사용됨.
	 */
	public String mPushExtData;
	/**
	 * 인증서PEM문자열 : 인증서경로 or 공인인증서 PEM 문자열 - Scrap 에서 사용하기 위해 저장함.
	 */
	public String mSignCert;
	/**
	 * 인증서개인키 PEM문자열 : 개인키경로 or 공인인증서 개인키 PEM 문자열 - Scrap 에서 사용하기 위해 저장함.
	 */
	public String mSignPri;
	/**
	 * signEncPw : 공인인증서 암호화된 비밀번호 - Scrap 에서 사용하기 위해 저장함.
	 */
	public String signEncPw;
	/**
	 * is_right_ssl_cert : SSL이 정상인지 여부를 판단하는 플래그.
	 */
	public boolean is_right_ssl_cert;
	/**
	 * is_right_ssl_cert_mgs_ing : SSL이 인증서가 정상이 아니라는 메세지가 떠 있는지 판단하는 플래그.
	 */
	public boolean is_right_ssl_cert_mgs_ing;
	/**
	 * mAppExit : App 강제 종료 플래그.
	 */
	public boolean mAppExit;

	public String getSharedPreferences(String key, String defVal){
		if(mSharedPre == null){
			mSharedPre = PreferenceManager.getDefaultSharedPreferences(this);
		}

		return mSharedPre.getString(key, defVal);
	}

	public void setSharedPreferences(String key, String val){
		if(mSharedPre == null){
			mSharedPre = PreferenceManager.getDefaultSharedPreferences(this);
		}

		mSharedPre.edit().putString(key, val).apply();
	}

	public void setSharedPreferencesCommit(String key, String val){
		if(mSharedPre == null){
			mSharedPre = PreferenceManager.getDefaultSharedPreferences(this);
		}

		mSharedPre.edit().putString(key, val).commit();
	}

	public boolean getSharedPreferences(String key, boolean defVal){
		if(mSharedPre == null){
			mSharedPre = PreferenceManager.getDefaultSharedPreferences(this);
		}

		return mSharedPre.getBoolean(key, defVal);
	}

	public long getSharedPreferences(String key, long defVal){
		if(mSharedPre == null){
			mSharedPre = PreferenceManager.getDefaultSharedPreferences(this);
		}

		return mSharedPre.getLong(key, defVal);
	}

	public void setSharedPreferences(String key, boolean val){
		if(mSharedPre == null){
			mSharedPre = PreferenceManager.getDefaultSharedPreferences(this);
		}

		mSharedPre.edit().putBoolean(key, val).apply();
	}

	public void setSharedPreferences(String key, long val){
		if(mSharedPre == null){
			mSharedPre = PreferenceManager.getDefaultSharedPreferences(this);
		}

		mSharedPre.edit().putLong(key, val).apply();
	}

	public void setSharedPreferencesCommit(String key, long val){
		if(mSharedPre == null){
			mSharedPre = PreferenceManager.getDefaultSharedPreferences(this);
		}

		mSharedPre.edit().putLong(key, val).commit();
	}

	public float getSharedPreferences(String key, float defVal){
		if(mSharedPre == null){
			mSharedPre = PreferenceManager.getDefaultSharedPreferences(this);
		}

		return Float.valueOf(mSharedPre.getString(key, String.valueOf(defVal)));
	}

	public void setSharedPreferences(String key, float val){
		if(mSharedPre == null){
			mSharedPre = PreferenceManager.getDefaultSharedPreferences(this);
		}

		mSharedPre.edit().putFloat(key, val).apply();
	}

/*
softm 백업.
	WorkManager mWorkManager;
	private void setupWorkRequest() {
		mWorkManager = WorkManager.getInstance();
		Log.d(TAG, "setupWorkRequest: " + "setting request");
		// Create charging constraint
		Constraints constraints = new Constraints.Builder()
//                .setRequiresCharging(true)
				.build();
//        OneTimeWorkRequest chargerListen = new OneTimeWorkRequest
//                .Builder(ChargerListener.class)
//                .setConstraints(constraints)
//                .build();
		OneTimeWorkRequest worker = new OneTimeWorkRequest
				.Builder(ChargerListener.class)
//				.setConstraints(constraints)
				.build();

		PeriodicWorkRequest.Builder requestBuild =
				new PeriodicWorkRequest.Builder(ChargerListener.class, 15,
						TimeUnit.MINUTES);
		PeriodicWorkRequest reWorker = requestBuild.build();
		mWorkManager.enqueue(reWorker);

		//mWorkManager.enqueue(worker);
	}*/

	public enum ConnectServiceMode {
		  NOTIFICATION_SERVICE
		, RESTART_SEARVICE_TEST
		, RESTART_SEARVICE
		, ACCESSIBILITY_SERVICE
	};

	public static ConnectServiceMode connectServiceMode = ConnectServiceMode.NOTIFICATION_SERVICE;

	public enum WorkType {
		THREAD, WORK_MANAGER
	}

	public static WorkType workType = WorkType.THREAD;
}



