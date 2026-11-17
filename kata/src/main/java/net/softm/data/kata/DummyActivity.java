package net.softm.data.kata;

import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_DISCONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_DISCONNECTED;
import static net.softm.data.kata.util.MapUtil.isStart;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import net.softm.data.kata.common.AppEnable;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.dto.StartStopOnOffVO;
import net.softm.data.kata.kt.Callbackkt;
import net.softm.data.kata.kt.Timerkt;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.DeviceUtil;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.LicenseUtil;
import net.softm.data.kata.util.MapPoint;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OverlayObserverViewUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.PermissionUtil;
import net.softm.data.kata.util.ReviewUtil;
import net.softm.data.kata.util.Utils;
import net.softm.data.kata.web.BasicActivity;
import net.softm.data.kata.web.JavaScriptBridge;

import org.apache.commons.lang3.StringUtils;

import java.util.List;


/**
 * MainActivity
 * @author softm
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class DummyActivity extends BasicActivity {
	final String TAG = "DummyActivity";
	static WorkerActivity instance;
	public static String COMMAND_VIEW_FULLAD = "COMMAND_VIEW_FULLAD";
	public static String COMMAND_LAUNCH_REVIEW_FLOW = "COMMAND_LAUNCH_REVIEW_FLOW";
	public static String COMMAND_REQUEST_LOCATION_PERMISSION = "COMMAND_REQUEST_LOCATION_PERMISSION";
	public static String COMMAND_FAV_LOCATION = "COMMAND_FAV_LOCATION";
	public static String COMMAND_MUSIC_PLAY_NOTICE = "COMMAND_MUSIC_PLAY_NOTICE";
	public static String COMMAND_BLUETOOTH_ENABLE = "COMMAND_BLUETOOTH_ENABLE";
	public static String COMMAND_BLUETOOTH_ENABLE_FOR_MAINACTIVITY = "COMMAND_BLUETOOTH_ENABLE_FOR_MAINACTIVITY";
	public static String COMMAND_REMOVE_RECENT_CHIP = "COMMAND_REMOVE_RECENT_CHIP";
	public static String COMMAND_REMOVE_FAVORITE_CHIP = "COMMAND_REMOVE_FAVORITE_CHIP";
//	Do not reopen
	boolean mInterstitialAdForShowed = false;
	Timerkt mInterstitialAdForTimerkt;
	Callbackkt mCallbackktForfinish ;
	DummyActivity mContext;
	String pCommand;
	@SuppressLint("MissingPermission")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		L.i(TAG,"onCreate");
		setContentView(R.layout.activity_transparent);
		Intent intent = getIntent();
		pCommand = intent.getStringExtra("P_COMMAND");
		String pArg1 = StringUtils.defaultString(intent.getStringExtra("P_ARG1"));
		String pArg2 = StringUtils.defaultString(intent.getStringExtra("P_ARG2"));
		String pArg3 = StringUtils.defaultString(intent.getStringExtra("P_ARG3"));
		String pArg4 = StringUtils.defaultString(intent.getStringExtra("P_ARG4"));
		String pArg5 = StringUtils.defaultString(intent.getStringExtra("P_ARG5"));
		L.i(TAG,"onCreate","pCommand : " + pCommand);
		L.i(TAG,"onCreate","pArg1 : " + pArg1);
		L.i(TAG,"onCreate","pArg2 : " + pArg2);
		L.i(TAG,"onCreate","pArg3 : " + pArg3);
		L.i(TAG,"onCreate","pArg4 : " + pArg4);
		L.i(TAG,"onCreate","pArg5 : " + pArg5);

		if ( "openOverlayViewShowOnly".equals(pCommand) ) {
//			MapUtil.openOverlayViewShowOnly(this,true);
//			showProgress();
/* 20241124 제거 - 대치 > OverlayViewUtil.init(this).openOverlayViewShowOnly();
			AcsUtil acsUtil = new AcsUtil(this);
			acsUtil.sendEvent(AcsUtil.OPEN_OVERLAY_VIEW_SHOWONLY,new Runnable(){
				@Override
				public void run() {
//					stopProgress();
//					DummyActivity.this.finish();
				}
			},500);
			// 딜레이 안주면 오버레이가 메인액티비티                 MapUtil.startConnectService
*/
			if (PermissionUtil.hasOverlayPermissions(this) ) {
				OverlayViewUtil.init(this).openOverlayViewShowOnly(pArg1.equals(Boolean.toString(true)));
				Fa.i(this,TAG,Fa.EVENT_NOTIBAR_CLICK_OVERLAY_VIEW_SHOWONLY);
			}
		} else if ( "TurnOnScreen".equals(pCommand) ) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
				this.setTurnScreenOn(true);
			} else {
				final Window window = getWindow();
				window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
			}
		} else if ( "toast".equals(pCommand) ) {
			try {
				Toast tt = Toast.makeText(this, pArg1, Integer.valueOf(pArg2));
//				tt.setGravity(Integer.valueOf(pArg3),Integer.valueOf(pArg4),Integer.valueOf(pArg5));
				tt.show();
//				LayoutInflater inflater = getLayoutInflater();
//				View layout = inflater.inflate(R.layout.toast_border, (ViewGroup)findViewById(R.id.toast_layout_root));
//
//				TextView text = (TextView) layout.findViewById(R.id.text);
//				text.setText(pArg1);
//
//				Toast toast = new Toast(getApplicationContext());
//				toast.setDuration(Integer.valueOf(pArg2));
//				toast.setGravity(Integer.valueOf(pArg3),Integer.valueOf(pArg4),Integer.valueOf(pArg5));
//				toast.setView(layout);
//				toast.show();

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ( "AppEnable".equals(pCommand) ) {
			if (pArg1.equals(Boolean.toString(true))) { //
				MapUtil.setAppEnable(AppEnable.ENABLE);
				Utils.toast(this,R.string.msg_enabled);
				Fa.i(this,TAG,Fa.EVENT_APP_ENABLE);
			} else {
				StartStopOnOffVO ss = MapUtil.getStartStopOnOff();
				if ( ss.getPlayMusic().getStop().isUseYn() ) {
					DeviceUtil.playMusicEnable(mContext,false,false);
				}
				MapUtil.setAppEnable(AppEnable.DISABLE);
				Utils.toast(this,R.string.msg_disabled);
				Fa.i(this,TAG,Fa.EVENT_APP_DISABLE);
			}

			if ( MapUtil.isAppEnable() ) {
				if ( isStart() ) {
					MapUtil.startService(this);
				}
			} else {
				MapUtil.stopService(this);
			}

			if (PermissionUtil.hasOverlayPermissions(this) && MapUtil.isOverlayOpen() ) {
				OverlayViewUtil.init(this).openOverlayViewShowOnly();
			}

		} else if ( "stopOverlayObserverViewService".equals(pCommand) ) {
//			if (PermissionUtil.hasOverlayPermissions(this) ) {
				MainConnectReceiver.stopToShowOverlay();
				OverlayObserverViewUtil.stopService();
				// 알림 제거
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				if (notificationManager != null) {
					notificationManager.cancel(OverlayObserverViewUtil.NOTIFICATION_ID);
				}
				Utils.toast(this,R.string.msg_canceled);
//			}
		} else if ( "startOverlayObserverViewService".equals(pCommand) ) {
			if (PermissionUtil.hasOverlayPermissions(this)  ) {
				OverlayObserverViewUtil.init(this).start();
			}
		} else if ( "runSpeedStart".equals(pCommand) ) {
			if ( MainConnectReceiver.isRunSpeed()) {
				String intentStr = MainConnectReceiver.getAction();
				if (Utils.isNotEmpty(intentStr)) {
					Intent intentRunSpeed = new Intent(intentStr);
					intentRunSpeed.putExtra(MainConnectReceiver.INTENT_EXTRA_RUN_SPEED,MainConnectReceiver.INTENT_EXTRA_RUN_SPEED_CLICK_START);
					this.sendBroadcast(intentRunSpeed);
					OverlayObserverViewUtil.stopService();
				}
			} else {
				MainConnectReceiver.stopToShowOverlay();
				OverlayObserverViewUtil.stopService();
			}

		} else if ( COMMAND_VIEW_FULLAD.equals(pCommand) ) {
			mInterstitialAdForTimerkt = new Timerkt(10,(remainingTime, gubun, timer)->{
				if ( !mInterstitialAdForShowed) {
					if (AdUtil.getRewardedAd() != null) {
						mInterstitialAdForShowed = true;
						/** 블루투스 연결 & 뮤직 앱에서 이용 */
					//	registerConnectReceiver();
						AdUtil.show(DummyActivity.this, AdUtil.getAdTypeForWaitReward(),()->{
							Intent intent2 = new Intent(MainConnectReceiver.getAction());
							intent2.putExtra(MainConnectReceiver.INTENT_EXTRA_ADVIEW_USER_EARNED_REWARD,MainConnectReceiver.INTENT_EXTRA_ADVIEW_USER_EARNED_REWARD_CALLBACK);
							DummyActivity.this.sendBroadcast(intent2);

							String formatTime = Utils.millisecondsToTime(LicenseUtil.getRemindLicenseExpireTime());
//							Utils.toast(DummyActivity.this, "사용기간 : " + formatTime + " 까지 연장됨");
							Utils.toast(DummyActivity.this, this.getString(R.string.msg_usage_period_extended,formatTime));

							Fa.i(DummyActivity.this,TAG,Fa.EVENT_AD_VIEW_USER_EARNED_REWARD);
							DummyActivity.this.finish();
						});
					} else {
						Log.d(TAG, "The interstitial ad wasn't ready yet.");
					}
					L.log(this, TAG, "Timerkt.tick", String.format("%02d", remainingTime));
				}
				return null;
			},(params)->{
				if ( !mInterstitialAdForShowed ) {
					DummyActivity.this.finish();
				}
				L.log(this,TAG,"Timerkt.complete");
				return null;
			},"ObserverViewService");
			mInterstitialAdForTimerkt.run();
			return;
		} else if ( COMMAND_LAUNCH_REVIEW_FLOW.equals(pCommand) ) {
			mCallbackktForfinish = new Callbackkt(10000,"launch_refiew_finish", true);
			mCallbackktForfinish.run((params) -> {
//				Utils.toastDebug(this,"DummyActivity.호출후.10초대기.액티비티 종료");
//				finish();
				return null;
			});

			ReviewUtil.popupReview(this,()->{
				Utils.toastDebug(this,"리뷰런처.호출.완료.액티비티 종료");
//				finish();
			});
			return;
		} else if ( COMMAND_REQUEST_LOCATION_PERMISSION.equals(pCommand) ) {
			PermissionUtil.requestLocation(this,()->{
				OverlayViewUtil.init(this).openOverlayViewShowOnly();
				finish();
			});
			return;
		} else if ( COMMAND_FAV_LOCATION.equals(pCommand) ) {
			/** localstorage와 preference와의 동기와 문제 */
			/** 주소를 못 얻오 오는 문제 등으로 사용안함
			if ( PermissionUtil.isLocationPermission(this) ) {

					GpsTracker gpsTracker = new GpsTracker(DummyActivity.this);
					double latitude = gpsTracker.getLatitude();
					double longitude = gpsTracker.getLongitude();

					confirm(this,getString(R.string.common_ok),getString(R.string.cancel), this.getString(R.string.msg_add_favorite_current_location), new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							String favoritePOIs = WApplication.mInstance.getSharedPreferences("_POIS_FAVORITE", "");
							ArrayList<MapPoint> favoritePoints = new ArrayList<MapPoint>();
							favoritePoints = MapUtil.getPointsFromString(favoritePOIs);

							ArrayList<MapPoint> finalFavoritePoints = favoritePoints;
							new Callkt("COMMAND_FAV_LOCATION").runIO((param)->{
								//				_POIS_FAVORITE
								TMapTapi tMapTapi = MapUtil.getInstanceForTmap(mContext);
								String address;
								try {
									address = new TMapData().convertGpsToAddress(latitude, longitude);
								} catch (IOException e) {
									throw new RuntimeException(e);
								} catch (ParserConfigurationException e) {
									throw new RuntimeException(e);
								} catch (SAXException e) {
									throw new RuntimeException(e);
								}

								address = Utils.isEmpty(address)?mContext.getString(R.string.label_unknown):address;
								finalFavoritePoints.add(new MapPoint(address, (float) latitude, (float) longitude));
								Gson gson = new Gson();
								String json = gson.toJson(finalFavoritePoints);
								WApplication.mInstance.setSharedPreferences("_POIS_FAVORITE", json);
								Utils.toast(mContext,R.string.msg_add_favorite_current_location_added);
								OverlayViewUtil.init(mContext).openOverlayViewShowOnly();
								finish();
								return null;
							});
						}
					}, new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							finish();
						}
					});
			} else {
				PermissionUtil.requestLocation(this, () -> {
					OverlayViewUtil.init(this).openOverlayViewShowOnly();
					finish();
				});
			}
			return;
		 */
		} else if ( COMMAND_MUSIC_PLAY_NOTICE.equals(pCommand) ) {
			alert(R.string.msg_go_music_player_notice, new View.OnClickListener() {
				@Override
				public void onClick(View view) {
//					OverlayViewUtil.show(true);
					MapUtil.setNoticeAlertMusicPlayer(true);
					MapUtil.runApp(mContext,MapUtil.getMusicAppPackage());
				}
			});
			return;

		} else if ( COMMAND_BLUETOOTH_ENABLE.equals(pCommand) || COMMAND_BLUETOOTH_ENABLE_FOR_MAINACTIVITY.equals(pCommand) ) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, 1);
			return;
		} else if ( COMMAND_REMOVE_RECENT_CHIP.equals(pCommand) ) {
			List<MapPoint> recentPoints = MapUtil.getPoisRecent();
			int idx = Integer.parseInt(pArg1);
			if ( recentPoints.size() > idx ) {
				String poiName = recentPoints.get(idx).getName();
				confirm(poiName,getString(R.string.common_ok),getString(R.string.cancel), (this.getString(R.string.msg_remove_recent_poi,poiName)),0, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						MapUtil.removePoiRecentForIndex(Integer.parseInt(pArg1));
						OverlayViewUtil.init(mContext).openOverlayViewShowOnly(true);
						finish();
					}
				}, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						OverlayViewUtil.init(mContext).openOverlayViewShowOnly(true);
						finish();
					}
				});
			}
			return;

		} else if ( COMMAND_REMOVE_FAVORITE_CHIP.equals(pCommand) ) {
			List<MapPoint> favoritePoints = MapUtil.getPoisFavorite();
			int idx = Integer.parseInt(pArg1);
			if ( favoritePoints.size() > idx ) {
				String poiName = favoritePoints.get(idx).getName();
				confirm(poiName,getString(R.string.common_ok),getString(R.string.cancel), (this.getString(R.string.msg_remove_favorite_poi,poiName)),0, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
//						MapUtil.removePoiFavoriteForIndex(Integer.parseInt(pArg1));
//						OverlayViewUtil.init(mContext).openOverlayViewShowOnly(true);
						Utils.goFowardPage(mContext, WUrl.ReqId.WEB_SEARCH.name(),2,"REMOVE_FAVORITE" + "&p_poi_index="+pArg1);
						finish();
					}
				}, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						OverlayViewUtil.init(mContext).openOverlayViewShowOnly(true);
						finish();
					}
				});
			}
			return;

		}
		finish();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		PermissionUtil.onActivityResult(this,requestCode,resultCode,data);
		if (requestCode == 1) {
			if ( PermissionUtil.isBluetoothPermission(mContext) ) {
				if ( COMMAND_BLUETOOTH_ENABLE.equals(pCommand) ) {
					Utils.toast(mContext, R.string.msg_bluetooth_enabled);
					OverlayViewUtil.init(mContext).openOverlayViewShowOnly(true);
				} else if ( COMMAND_BLUETOOTH_ENABLE_FOR_MAINACTIVITY.equals(pCommand) ) {
					JavaScriptBridge.callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, "ok");
				}
			} else {
				if ( COMMAND_BLUETOOTH_ENABLE.equals(pCommand) ) {
				} else if ( COMMAND_BLUETOOTH_ENABLE_FOR_MAINACTIVITY.equals(pCommand) ) {
					JavaScriptBridge.callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, "not");
				}
				Utils.toast(mContext,R.string.msg_turn_on_the_bluetooth);
			}
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		/** 블루투스 연결 & 뮤직 앱에서 이용 */
	//	unregisterConnectReceiver();
	}

	/** 블루투스 연결 & 뮤직 앱에서 이용 */
	MainConnectReceiver mainConnectReceiver;
	private void registerConnectReceiver() {
		mainConnectReceiver = new MainConnectReceiver();
		L.d(TAG, "ConnectService", "registerConnectReceiver");
		IntentFilter localIntentFilterPower = new IntentFilter();
		localIntentFilterPower.addAction(ACTION_SOFTM_POWER_CONNECTED);
		localIntentFilterPower.addAction(ACTION_SOFTM_POWER_DISCONNECTED);

		localIntentFilterPower.addAction(ACTION_SOFTM_ACL_CONNECTED);
		localIntentFilterPower.addAction(ACTION_SOFTM_ACL_DISCONNECTED);
		L.d(TAG, "registerConnectReceiver", "add Receiver ConnectReceiver");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			WApplication.mInstance.registerReceiver(this.mainConnectReceiver, localIntentFilterPower,RECEIVER_EXPORTED);
		} else { // Below Android 12
			WApplication.mInstance.registerReceiver(this.mainConnectReceiver, localIntentFilterPower);
		}
	}

	private void unregisterConnectReceiver() {
		L.d(TAG,"ConnectService","unregisterConnectReceiver");
		try{
			mContext.unregisterReceiver(this.mainConnectReceiver);
		} catch (Exception ex) {
//            ex.printStackTrace();
		}
		L.d(TAG, "unRegisterConnectReceiver");

	}

}