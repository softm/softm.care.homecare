package net.softm.data.kata;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import net.softm.lib.common.Connectivity;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.job.WorkQueue;
import net.softm.data.kata.kt.Timerkt;
import net.softm.data.kata.util.DeviceUtil;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.MapPoint;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.PermissionUtil;
import net.softm.data.kata.util.Utils;
import net.softm.data.kata.web.BasicActivity;


/**
 * MainActivity
 * @author softm
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class ExecActivity extends BasicActivity {
	final String TAG = "ExecActivity";
	static ExecActivity instance;
	long startTime = 0l;
	final long INTERVAL = 1000 * 60 * 5;
	static boolean isComplete = false;
	int checkHandlerCnt = 0;
	Handler checkHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		startTime = System.currentTimeMillis();
		L.d(TAG,"isComplete - onCreate : " +isComplete);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transparent);
		Intent intent = getIntent();
		String pStartYes = intent.getStringExtra("P_START_NAVI");

		if ( "YES".equals(pStartYes) ) {
			isComplete = false;
			if ( !PermissionUtil.isAccessibilityServiceRunning(this) ) {
//				isComplete = true;
//				finish();
//				return;
			}
			if ( !Connectivity.isConnected(this) && DeviceUtil.isDeviceSupportsMobileData(this) ) {
				confirm(this,getString(R.string.button_label_on),getString(R.string.cancel), this.getString(R.string.msg_confirm_turn_on_mobile_data), new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if ( !PermissionUtil.isAccessibilityServiceRunning(ExecActivity.this) ) {
							DeviceUtil.mobileDataEnable(ExecActivity.this,true);
							new Timerkt(10,(remainingTime, gubun, timer)->{
								if ( Connectivity.isConnected(ExecActivity.this) ) {
									exec();
								}
								return null;
							},(params)->{
								exec();
								return null;
							},"ObserverViewService").run();
							return;
						}
						WorkQueue.init();

						WorkQueue.addTask(new Runnable() {
							@Override
							public void run() {
								String msgOnOff =  ExecActivity.this.getString(R.string.label_off);
								String msg = ExecActivity.this.getString(R.string.msg_startstop_onoff,ExecActivity.this.getString(R.string.label_airplane_mode),msgOnOff);
								Utils.toast(ExecActivity.this, msg);
								DeviceUtil.airplaneModeEnable(ExecActivity.this, false,true);
							}
						});
						if ( Connectivity.isSimSupport(ExecActivity.this) ) { // Mobile
							WorkQueue.addTask(new Runnable() {
								@Override
								public void run() {
									String msgOnOff = ExecActivity.this.getString(R.string.label_on);
									String msg = ExecActivity.this.getString(R.string.msg_startstop_onoff, ExecActivity.this.getString(R.string.label_mobile_data), msgOnOff);
									Utils.toast(ExecActivity.this, msg);
									DeviceUtil.mobileDataEnable(ExecActivity.this, true, true);
								}
							});
						} else {
							WorkQueue.addTask(new Runnable() {
								@Override
								public void run() {
									String msgOnOff = ExecActivity.this.getString(R.string.label_on);
									String msg = ExecActivity.this.getString(R.string.msg_startstop_onoff, ExecActivity.this.getString(R.string.label_wifi), msgOnOff);
									Utils.toast(ExecActivity.this, msg);
									DeviceUtil.wifiEnable(ExecActivity.this, true, true);
								}
							});
						}
						WorkQueue.addTask(new Runnable() {
							@Override
							public void run() {
		//						Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
		//						startActivity(i);

								checkHandler = new Handler(Looper.getMainLooper());
								checkHandler.postDelayed(new Runnable() {
									@Override
									public void run() {
										if (!Connectivity.isConnected(ExecActivity.this)) {
											//다시 돌아왔을경우 onResume 시 종료되어야 ExecActivity가 화면을 가려서 생기는 문제가 없음.
											isComplete = true;
											if ( checkHandlerCnt <= 10 ) {
												checkHandlerCnt++;
												checkHandler.postDelayed(this, Const.ACTIVITY_CHANGE_CHECK_INTERVAL_TIME);
											} else {
												checkHandler.removeCallbacksAndMessages(null);
											}
										} else {
											checkHandler.removeCallbacksAndMessages(null);
											exec ();
										}
									}
								},Const.ACTIVITY_CHANGE_CHECK_INTERVAL_TIME);
							}
						});

						WorkQueue.execute();

					}
				}, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						//result.cancel();
//						isComplete = true;
						exec ();
//						finish();
					}
				});
				return;
			} else {
				exec ();
			}
		} else {
//			Intent i = new Intent(this, MainActivity.class); // 메인
//			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(i);
		}
	}

	String P_NAME = null;
	float P_LAT = 0.0f;
	float P_LON = 0.0f;

	private void exec () {
		L.d(TAG,"isComplete - onCreate - pStartYes == 'Y' : " +isComplete);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
				WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		Intent intent = getIntent();
		P_NAME = intent.getStringExtra("P_NAME");
		P_LAT = intent.getFloatExtra("P_LAT", 0.0f);
		P_LON = intent.getFloatExtra("P_LON", 0.0f);
		/**
		 // TODO SOFTM : 20241209
		 //  AutoRunNotificationListenerService.onNotificationRemoved 에서 MapUtil.setNotNaving(); 을 처리하여 실행중 상태를 해제하고 있으나
		 //  이 부분이 클리어 되지 않는 상황이 존재하므로 앱을 실행시 클리어
		 */
		MapUtil.setNotNaving();

		NaviDestination naviDestination = (NaviDestination) intent.getSerializableExtra("P_NAVI_DESTINATION");
//			MapUtil.openOverlayViewNew(this,naviDestination, true, true, true,  false);
		MapUtil mapUtil = new MapUtil(this);
		if (NaviDestination.HOME.equals(naviDestination)) { // 집
			L.d(TAG, "Map.NaviDestination.HOME");
//			mapUtil.runTmapGoHome();
			Fa.i(this,TAG,Fa.EVENT_NOTIBAR_CLICK_HOME);
			MapUtil.runNavi(this, naviDestination, true);
		} else if (NaviDestination.COMPANY.equals(naviDestination)) { // 회사
			L.d(TAG, "Map.NaviDestination.COMPANY");
//			mapUtil.runTmapGoCompany();
			Fa.i(this,TAG,Fa.EVENT_NOTIBAR_CLICK_COMPANY);
			MapUtil.runNavi(this, naviDestination, true);
		} else if (NaviDestination.DRIVE.equals(naviDestination)) { // 운전
			L.d(TAG, "Map.NaviDestination.DRIVE");
//			mapUtil.runTmapSafeDrive();
			Fa.i(this,TAG,Fa.EVENT_NOTIBAR_CLICK_START_APP);
			MapUtil.runNavi(this, naviDestination, true);
//				mapUtil.runTmapRoute(null,-1f,-1f);
		} else if (NaviDestination.BASIC.equals(naviDestination)) { // 기본
			L.d(TAG, "Map.NaviDestination.BASIC");
//			mapUtil.runTmapRoute(P_NAME,P_LAT,P_LON);
			MapPoint mapPoin = new MapPoint(P_NAME,P_LAT,P_LON);
			MapUtil.runNavi(this, mapPoin, true);
		} else if (NaviDestination.APP.equals(naviDestination)) { // App
			L.d(TAG, "Map.NaviDestination.APP");
			mapUtil.runApp(ExecActivity.this);
		}

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					showProgress();
				} catch (Exception ex) {

				}
			}
		}, 0);

		// clear intent in Activity
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				stopProgress();
				Intent i = new Intent(ExecActivity.this, ExecActivity.class); // 메인
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(i);
//					stopProgress();
				moveTaskToBack(true);
				isComplete = true;
				L.d(TAG,"isComplete - clear intent in Activity : " +isComplete);
			}
		}, 3000);
	}
	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		String pStartYes = intent.getStringExtra("P_START_NAVI");
		if ( pStartYes == null ) {
//			if(( System.currentTimeMillis()-startTime ) > 3000 ) {
			if( isComplete ) {
// TODO SOFTM - 20210801 - 원래 enable되어있었는데, 어떤 경우를 보완한 로직인지 알 수 없어. 제거함.
//				Intent i = new Intent(ExecActivity.this, MainActivity.class); // 메인
//				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				startActivity(i);
//				L.d(TAG,"isComplete - onResume : " +isComplete);
// TODO SOFTM
// ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
				finish();
			}
		} else {
			if ( isComplete ) {
				finish();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopProgress();
	}
}