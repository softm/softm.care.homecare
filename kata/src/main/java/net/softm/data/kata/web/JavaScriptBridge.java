package net.softm.data.kata.web;

import static android.content.Context.WIFI_SERVICE;
import static net.softm.data.kata.util.MapUtil.appendAppNameForPackage;
import static net.softm.data.kata.util.MapUtil.isStart;
import static net.softm.data.kata.util.Utils.encdodeForWeb;
import static net.softm.lib.common.WLog.printException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.work.WorkInfo;

import com.android.billingclient.api.Purchase;
import com.google.firebase.Timestamp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.softm.data.kata.BuildConfig;
import net.softm.data.kata.ConnectService;
import net.softm.data.kata.DummyActivity;
import net.softm.data.kata.GpsTracker;
import net.softm.data.kata.MainActivity;
import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.AdType;
import net.softm.data.kata.common.AppEnable;
import net.softm.data.kata.common.AppType;
import net.softm.data.kata.common.BillingType;
import net.softm.data.kata.common.ChargingMode;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.MapMode;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.common.NaviMode;
import net.softm.data.kata.common.OrgConst;
import net.softm.data.kata.common.Package;
import net.softm.data.kata.common.PackageMusic;
import net.softm.data.kata.common.ParamCallback;
import net.softm.data.kata.common.PermissionType;
import net.softm.data.kata.common.RunStatusScreenMode;
import net.softm.data.kata.common.StartMode;
import net.softm.data.kata.common.StopMode;
import net.softm.data.kata.common.UseStatusMode;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.dto.BasicInfoDTO;
import net.softm.data.kata.dto.base.BasicSettings;
import net.softm.data.kata.geofence.GeofenceConstants;
import net.softm.data.kata.god.dto.PrayClass;
import net.softm.data.kata.god.dto.PrayComment;
import net.softm.data.kata.god.dto.PrayMemo;
import net.softm.data.kata.kt.Callkt;
import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.AlarmUtil;
import net.softm.data.kata.util.AppUtil;
import net.softm.data.kata.util.BillingUtil;
import net.softm.data.kata.util.CheckServiceUtil;
import net.softm.data.kata.util.DeviceUtil;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.Fb;
import net.softm.data.kata.util.FbGodWhisper;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.LicenseUtil;
import net.softm.data.kata.util.MapPoint;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OrgUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.PackageUtil;
import net.softm.data.kata.util.PermissionUtil;
import net.softm.data.kata.util.RestUtil;
import net.softm.data.kata.util.ServiceUtil;
import net.softm.data.kata.util.Utils;
import net.softm.data.kata.util.WorkerUtil;
import net.softm.data.kata.util.base.GroupUtil;
import net.softm.lib.BaseActivity;
import net.softm.lib.common.WLog;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


// softm
//import m.client.push.library.PushManager;
//import minerva.magicidreaderlib.MagicIDReaderCameraMainActivity;
// ---- ---- ---- ---- ---- softm
public class JavaScriptBridge {

	private static final String TAG = "JavaScriptBridge";
	private static final String TAG_SHORT = "JSB";
	public  static final String CALL_NAME = "callApp";

	private Context mContext;
	public static BaseWebView mWebView;
	/** UI 스레드 처리를 위한 핸들러 */
	private Handler mUiHandler;

	/**
	 * softm
	 * 자바스크립트 보안키패드 인증키 생성 함수명
	 */
	public static String mGetKeyForMtransFuncName = "fn_GetKeyForMtrans";

	/**
	 * 자바스크립트 callback 함수명
	 */
	public static String mCallbackFuncName = "";
	/**
	 * 고객관리번호
	 */
	public static String mCusNo;
	/**
	 * 파일 업로드 url
	 */
	public static String mFileUploadUrl;

	/**
	 * 부쉬 등록, 해지 관련 javascript callback이 필요할 경우 true
	 */
	public static boolean mIsPushCallbackNeed = false;

	public JavaScriptBridge(Context context, BaseWebView webView) {
		mContext = context;
		mWebView = webView;
		mUiHandler = new Handler();
	}

	/**
	 * 웹페이지를 로드한다.
	 * @param url 웹페이지 URL
	 */
	public static void loadUrl(String url) {
		L.d(TAG, "loadUrl, url=" + url);
		mWebView.loadUrl(WUrl.mBaseUrl + "/" + url);
	}

	/**
	 * 자바스크립를 실행한다.
	 * @param javascript 자바스크립트 코드
	 */
	public static void callJavascript(final String javascript) {
		WLog.d(TAG, javascript);
//		loadUrl("javascript:" + javascript);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//			mWebView.evaluateJavascript(javascript, null);
//				final BaseActivity activity = (BaseActivity)mWebView.mContext;
//				activity.runOnUiThread(new Runnable()
//				{
//					public void run()
//					{
			BaseActivity activity = (BaseActivity)mWebView.mContext;
			WebView popup = activity.findViewById(R.id.popup);
			ValueCallback<String> resultCallback = null;
			if ( popup == null ) {
				mWebView.evaluateJavascript(javascript, resultCallback);
			} else {
				popup.evaluateJavascript(javascript, resultCallback);
			}


//					}
//				});
		} else {
//			loadUrl("javascript:" + javascript);
			loadUrl("javascript:" + javascript);
		}
	}

	/**
	 * 자바스크립트 함수를 호출한다.
	 * @param funcName 자바스크립트 함수명
	 * @param params 함수 파라미터 목록
	 */
	public static void callJavascriptFunc(String funcName, Object... params) {
		StringBuilder sb = new StringBuilder();
		if (Utils.isNotEmpty(params) && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				sb.append("'").append(params[i]).append("'");
				if (i < params.length - 1) {
					sb.append(",");
				}
			}
		}
		String func = String.format("%s(%s)", funcName, sb.toString());
		callJavascript(func);
		WLog.e(TAG, "func : " + func);
	}

	/**
	 * 네이티브 기능을 수행하기 위한 자바스크립트 인터페이스 메소드
	 * @param jsonString json 문자열
	 */
	@JavascriptInterface
	public void appBridge(String jsonString) {
		try {
			final JSONObject json = new JSONObject(jsonString);
			WLog.d(TAG, "appBridge, json : " + json.toString(2));
			mUiHandler.post(new Runnable() {
				@RequiresApi(api = Build.VERSION_CODES.O)
                public void run() {
					try {
						switch (JavaScriptApi.getApi(json)) {
							case JavaScriptApi.API_001:
								api_001_Start(json);
								break;
							case JavaScriptApi.API_002:
								api_002_Stop(json);
								break;
							case JavaScriptApi.API_003:
								api_003_StartSetting(json);
								break;
							case JavaScriptApi.API_123:
								api_123_CheckPermission(json);
								break;
							case JavaScriptApi.API_004:
								api_004_CheckGrant(json);
								break;
							case JavaScriptApi.API_005:
								api_005_GetLocation(json);
								break;
							case JavaScriptApi.API_006:
								api_006_StartNavi(json);
								break;
							case JavaScriptApi.API_007:
								api_007_StartNaviDestination(json);
								break;
							case JavaScriptApi.API_100:
								api_100_Alert(json);
								break;
							case JavaScriptApi.API_101:
								api_101_Conform(json);
								break;
							case JavaScriptApi.API_102:
								api_102_Toast(json);
								break;
							case JavaScriptApi.API_103:
								api_103_ShowPopup(json);
								break;
							case JavaScriptApi.API_106:
								api_106_ClosePopup(json);
								break;
							case JavaScriptApi.API_107:
								api_107_OutBrowser(json);
								break;
							case JavaScriptApi.API_108:
								api_108_VisibleKeyboard(json);
								break;
							case JavaScriptApi.API_109:
								api_109_SecureKey(json);
								break;
							case JavaScriptApi.API_110:
								api_110_GoMain(json);
								break;
							case JavaScriptApi.API_111:
								api_111_GoTest(json);
								break;
							case JavaScriptApi.API_112:
								api_112_GoPermission(json);
								break;
							case JavaScriptApi.API_113:
								api_113_GoFoward(json);
								break;
							case JavaScriptApi.API_114:
								api_114_CallCamera(json);
								break;
							case JavaScriptApi.API_115:
								api_115_ShowProgress();
								break;
							case JavaScriptApi.API_116:
								api_116_StopProgress();
								break;
							case JavaScriptApi.API_117:
								api_117_ShowProgressWithTimer(json);
								break;
							case JavaScriptApi.API_118:
								api_116_StopProgress();
								break;
							case JavaScriptApi.API_119:
								api_119_AppExit();
								break;
							case JavaScriptApi.API_120:
								api_120_SaveData(json);
								break;
							case JavaScriptApi.API_121:
								api_121_LoadData(json);
								break;
							case JavaScriptApi.API_126:
								api_126_call(json);
								break;
							case JavaScriptApi.API_142: // add softm 네이티브정보(uuid)
								api_142_GetNInfo(json);
								break;
							case JavaScriptApi.API_333:
								api_333_GetPackagesInfo(json);
								break;
							case JavaScriptApi.API_334:
								api_334_GetMusicPackagesInfo(json);
								break;
							case JavaScriptApi.API_300:
								api_300_GetInfo(json);
								break;
							case JavaScriptApi.API_301:
								api_301_GetUrl(json);
								break;
							case JavaScriptApi.API_302:
								api_302_GrantedPermissions(json);
								break;
							case JavaScriptApi.API_303:
								api_303_getInstallPackages(json);
								break;
							case JavaScriptApi.API_304:
								api_304_gotoStoreAppPacakge(json);
								break;
							case JavaScriptApi.API_305:
								api_305_getBluetooths(json);
								break;
							case JavaScriptApi.API_315:
								api_315_enableBluetooth(json);
								break;
							case JavaScriptApi.API_306:
								api_306_getWifis(json);
								break;
							case JavaScriptApi.API_307:
								api_307_GetSysInfo(json);
								break;
							case JavaScriptApi.API_308:
								api_308_SetSysInfo(json);
								break;
							case JavaScriptApi.API_400:
								api_400_ShowUrl(json);
								break;
							case JavaScriptApi.API_401:
								api_401_Reload(json);
								break;
							case JavaScriptApi.API_402:
								api_402_CopyClipboard(json);
								break;

							case JavaScriptApi.API_500:
								api_500_StopOverlay();
								break;
							case JavaScriptApi.API_501:
								api_501_HideOverlay();
								break;
							case JavaScriptApi.API_502:
								api_502_ShowOverlay();
								break;
							case JavaScriptApi.API_503:
								api_503_OpenOverlayView(json);
								break;
							case JavaScriptApi.API_504:
								api_504_DownloadBase64File(json);
								break;
							case JavaScriptApi.API_777:
								api_777_AdView(json);
								break;
							case JavaScriptApi.API_700:
								api_700_SyncUserLicense(json);
								break;
							case JavaScriptApi.API_701:
								api_701_PayInApp(json);
								break;
							case JavaScriptApi.API_800:
								api_800_CreatePrayClass(json);
								break;
							case JavaScriptApi.API_801:
								api_801_SelectPrayClass(json);
								break;
							case JavaScriptApi.API_802:
								api_802_SelectOnePrayClass(json);
								break;
							case JavaScriptApi.API_803:
								api_803_UpdatePrayClass(json);
								break;
							case JavaScriptApi.API_804:
								api_804_DeletePrayClass(json);
								break;
							case JavaScriptApi.API_805:
								api_805_AddPartyPrayClass(json);
								break;
							case JavaScriptApi.API_806:
								api_806_RemovePartyPrayClass(json);
								break;
							case JavaScriptApi.API_807:
								api_807_donePartyPrayClass(json);
								break;
							case JavaScriptApi.API_808:
								api_808_notPartyPrayClass(json);
								break;
							case JavaScriptApi.API_810:
								api_810_AddPrayComment(json);
								break;
							case JavaScriptApi.API_811:
								api_811_SelectPrayComment(json);
								break;
							case JavaScriptApi.API_812:
								api_812_DeletePrayComment(json);
								break;
							case JavaScriptApi.API_820:
								api_820_AddPrayMemo(json);
//								api_810_AddPrayComment(json);
								break;
							case JavaScriptApi.API_821:
								api_821_SelectPrayMemo(json);
//								api_811_SelectPrayComment(json);
								break;

							case JavaScriptApi.API_900:
								api_900_BindAlarm(json);
								break;
							case JavaScriptApi.API_901:
								api_901_AddAlarm(json);
								break;
							case JavaScriptApi.API_902:
								api_902_CancelAlarm(json);
								break;
							case JavaScriptApi.API_910:
								api_910_ShareLink(json);
								break;
							default:
								break;
						}
					} catch (Exception e) {
						L.e(TAG,"appBridge",L.getStackTraceString(e));
					}
				}
			});
		} catch (JSONException e) {
			L.e(TAG,"appBridge",L.getStackTraceString(e));
		}
	}
	private void api_701_PayInApp(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String billingType = body.optString("billingType");
		String callbackFunc = body.optString("callBackFunc");
		BillingType billingTyp_ = BillingType.valueOf(billingType);
//		BillingUtil billingUtil = BillingUtil.getInstance(mContext);
		BillingUtil billingUtil = WApplication.mInstance.billingUtil;
//            BillingUtil billingUtil = new BillingUtil(context);
		billingUtil.startConnection(()->{
			billingUtil.payInApp((Activity) mContext, billingTyp_,new ParamCallback<Purchase>() {
				@Override
				public void onResult(Purchase purchase) {
					callJavascriptFunc(callbackFunc, (new Gson()).toJson(purchase));
	//				new Callkt("api_777_AdView.callJavascriptFunc").run((param2)-> {
	//					callJavascriptFunc(callbackFunc, "ok");
	//					return null;
	//				});
				}
				@Override
				public void onError(Exception exception) {
					callJavascriptFunc(callbackFunc, "error");
				}
			});
		});
//		/**
//		 * Update UserType From 구글결제정보
//		 */
//		LicenseUtil.(mContext,new ParamCallback<String>() {
//			@Override
//			public void onResult(String result) {
//				callJavascriptFunc(callbackFunc, "ok");
////				new Callkt("api_777_AdView.callJavascriptFunc").run((param2)-> {
////					callJavascriptFunc(callbackFunc, "ok");
////					return null;
////				});
//			}
//			@Override
//			public void onError(Exception exception) {
//				callJavascriptFunc(callbackFunc, "error");
//			}
//		});
//		callJavascriptFunc(callbackFunc, "ok");
	}

	private void api_700_SyncUserLicense(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String callbackFunc = body.optString("callBackFunc");

		/**
		 * Update UserType From 구글결제정보
		 */
		LicenseUtil.syncUserLicense(mContext,new ParamCallback<String>() {
			@Override
			public void onResult(String result) {
				callJavascriptFunc(callbackFunc, "ok");
//				new Callkt("api_777_AdView.callJavascriptFunc").run((param2)-> {
//					callJavascriptFunc(callbackFunc, "ok");
//					return null;
//				});
			}
			@Override
			public void onError(Exception exception) {
				callJavascriptFunc(callbackFunc, "error");
			}
		});
//		callJavascriptFunc(callbackFunc, "ok");
	}


	private void api_777_AdView(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String callbackFunc = body.optString("callBackFunc");

		AdUtil.loadRewardAd(mContext, AdType.REWARDEDINTERSTITIAL,()->{
		},()->{
			new Callkt("AdUtil.initRewardAd.endCallback").run((param)->{
				AdUtil.show((MainActivity)mContext, AdType.REWARDEDINTERSTITIAL,()->{
					new Callkt("api_777_AdView.callJavascriptFunc").run((param2)-> {
						callJavascriptFunc(callbackFunc, "ok");
						return null;
					});
				});
//				MapUtil.execCommandActivity(mContext, DummyActivity.COMMAND_VIEW_FULLAD);
				return null;
			});
		});

//		callJavascriptFunc(callbackFunc, "ok");
	}

	private void api_800_CreatePrayClass(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String title = body.optString("title");
		String content = body.optString("content");
		String prayDt = body.optString("prayDt");
		String strGroups = body.optString("groups");
//		Type listType = new TypeToken<List<PrayClass>>() {}.getType();
//		(new Gson()).fromJson(String.valueOf(json.getJSONObject("body")), PrayClass.class);
		PrayClass prayClass = new PrayClass();
		prayClass.setTitle(title);
		prayClass.setContent(content);
		prayClass.setPrayDt(Fb.toTimestamp(prayDt));
		prayClass.setOrgId(OrgUtil.getOrg());
		prayClass.setProfileName(OrgUtil.getProfileName());
		prayClass.setNickName(OrgUtil.getNickName());
		Type listType = new TypeToken<List<String>>() {}.getType();
		prayClass.setGroups((new Gson()).fromJson(strGroups, listType));

		String callbackFunc = body.optString("callBackFunc");
		FbGodWhisper.createPrayClass(mContext,prayClass, new ParamCallback<PrayClass>() {
			@Override
			public void onResult(PrayClass result) {
				callJavascriptFunc(callbackFunc, StringUtils.defaultString((new Gson()).toJson(result)));
			}
		});
	}

	private void api_801_SelectPrayClass(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String fromDt = body.optString("fromDt")+"T00:00";
		String toDt = body.optString("toDt")+"T23:59";
		String title = body.optString("title");
		String strGroups = body.optString("groups");
		String callbackFunc = body.optString("callBackFunc");

		Type listType = new TypeToken<List<String>>() {}.getType();
		List<String> groups = (new Gson()).fromJson(strGroups, listType);

		FbGodWhisper.selectPrayClass(mContext, Fb.toTimestamp(fromDt), Fb.toTimestamp(toDt), title, new ParamCallback<List<PrayClass>>() {
			@Override
			public void onResult(List<PrayClass> result) {
				callJavascriptFunc(callbackFunc, StringUtils.defaultString((new Gson()).toJson(result), ""));
			}
		});
	}

	private void api_802_SelectOnePrayClass(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");
		String callbackFunc = body.optString("callBackFunc");

		FbGodWhisper.selectOnePrayClass(mContext, docId, new ParamCallback<PrayClass>() {
			@Override
			public void onResult(PrayClass result) {
				callJavascriptFunc(callbackFunc, StringUtils.defaultString((new Gson()).toJson(result), ""));
			}
		});
	}

	private void api_803_UpdatePrayClass(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");
		String title = body.optString("title");
		String content = body.optString("content");
		String prayDt = body.optString("prayDt");
		String strGroups = body.optString("groups");
//		Type listType = new TypeToken<List<PrayClass>>() {}.getType();
//		(new Gson()).fromJson(String.valueOf(json.getJSONObject("body")), PrayClass.class);
		PrayClass prayClass = new PrayClass();
		prayClass.setTitle(title);
		prayClass.setContent(content);
		prayClass.setPrayDt(Fb.toTimestamp(prayDt));
		prayClass.setOrgId(OrgUtil.getOrg());
		Type listType = new TypeToken<List<String>>() {}.getType();
		(new Gson()).fromJson(strGroups, listType);
		prayClass.setGroups((new Gson()).fromJson(strGroups, listType));

		String callbackFunc = body.optString("callBackFunc");
		FbGodWhisper.updatePrayClass(mContext, docId, prayClass, new ParamCallback<PrayClass>() {
			@Override
			public void onResult(PrayClass result) {
				callJavascriptFunc(callbackFunc, StringUtils.defaultString((new Gson()).toJson(result)));
			}
		});
	}
	private void api_804_DeletePrayClass(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");

		String callbackFunc = body.optString("callBackFunc");
		FbGodWhisper.deletePrayClass(mContext, docId, new ParamCallback<String>() {
			@Override
			public void onResult(String result) {
				callJavascriptFunc(callbackFunc, result);
			}
		});
	}

	private void api_805_AddPartyPrayClass(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");

		String callbackFunc = body.optString("callBackFunc");
		FbGodWhisper.addPartyPrayClass(mContext, docId, new ParamCallback<String>() {
			@Override
			public void onResult(String result) {
				callJavascriptFunc(callbackFunc, result);
			}
		});
	}

	private void api_806_RemovePartyPrayClass(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");

		String callbackFunc = body.optString("callBackFunc");
		FbGodWhisper.removePartyPrayClass(mContext, docId, new ParamCallback<String>() {
			@Override
			public void onResult(String result) {
				callJavascriptFunc(callbackFunc, result);
			}
		});
	}


	private void api_807_donePartyPrayClass(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");

		String callbackFunc = body.optString("callBackFunc");
		FbGodWhisper.donePartyPrayClass(mContext, docId, new ParamCallback<String>() {
			@Override
			public void onResult(String result) {
				callJavascriptFunc(callbackFunc, result);
			}
		});
	}

	private void api_808_notPartyPrayClass(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");

		String callbackFunc = body.optString("callBackFunc");
		FbGodWhisper.notPartyPrayClass(mContext, docId, new ParamCallback<String>() {
			@Override
			public void onResult(String result) {
				callJavascriptFunc(callbackFunc, result);
			}
		});
	}


	private void api_810_AddPrayComment(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");
		String comment = body.optString("comment");
		PrayComment prayComment = new PrayComment();
		prayComment.setComment(comment);
//		Type listType = new TypeToken<List<String>>() {}.getType();
//		(new Gson()).fromJson(strGroups, listType);
//		prayClass.setGroups((new Gson()).fromJson(strGroups, listType));

		String callbackFunc = body.optString("callBackFunc");
		FbGodWhisper.addPrayComment(mContext, docId, prayComment, new ParamCallback<PrayComment>() {
			@Override
			public void onResult(PrayComment result) {
				callJavascriptFunc(callbackFunc, StringUtils.defaultString((new Gson()).toJson(result)));
			}
		});
	}

	private void api_811_SelectPrayComment(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");
		String comment = body.optString("comment");
		PrayComment prayComment = new PrayComment();
		prayComment.setClassDocId(docId);
//		prayComment.setComment(comment);
//		Type listType = new TypeToken<List<String>>() {}.getType();
//		(new Gson()).fromJson(strGroups, listType);
//		prayClass.setGroups((new Gson()).fromJson(strGroups, listType));

		String callbackFunc = body.optString("callBackFunc");
//		FbGodWhisper.delComment(mContext, docId, prayComment, new ParamCallback<PrayComment>() {
		FbGodWhisper.selectPrayComment(mContext, prayComment, new ParamCallback<List<PrayComment>>() {
			@Override
			public void onResult(List<PrayComment> result) {
				callJavascriptFunc(callbackFunc, StringUtils.defaultString((new Gson()).toJson(result), ""));
			}
		});
	}

	private void api_812_DeletePrayComment(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");
		String classDocId = body.optString("classDocId");
		String comment = body.optString("comment");
		PrayComment prayComment = new PrayComment();
		prayComment.setDocId(docId);
		prayComment.setClassDocId(classDocId);
//		Type listType = new TypeToken<List<String>>() {}.getType();
//		(new Gson()).fromJson(strGroups, listType);
//		prayClass.setGroups((new Gson()).fromJson(strGroups, listType));

		String callbackFunc = body.optString("callBackFunc");
		FbGodWhisper.deletePrayComment(mContext, prayComment, new ParamCallback<String>() {
			@Override
			public void onResult(String result) {
				callJavascriptFunc(callbackFunc, result);
			}
		});
	}

	private void api_820_AddPrayMemo(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String timestampId = body.optString("timestampId");
		String memo = body.optString("memo");
		Timestamp timestamp = new Timestamp(Integer.parseInt(timestampId),0);
		PrayMemo prayMemo = new PrayMemo(timestamp);
		prayMemo.setMemo(memo);
//		Type listType = new TypeToken<List<String>>() {}.getType();
//		(new Gson()).fromJson(strGroups, listType);
//		prayClass.setGroups((new Gson()).fromJson(strGroups, listType));

		String callbackFunc = body.optString("callBackFunc");
		FbGodWhisper.savePrayMemo(mContext, timestamp, prayMemo, new ParamCallback<PrayMemo>() {
			@Override
			public void onResult(PrayMemo result) {
				callJavascriptFunc(callbackFunc, StringUtils.defaultString((new Gson()).toJson(result)));
			}
		});
	}

	private void api_821_SelectPrayMemo(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");
		String comment = body.optString("comment");
		String strTimestampIds = body.optString("timestampIds");
		PrayComment prayComment = new PrayComment();
		prayComment.setClassDocId(docId);
//		prayComment.setComment(comment);
		Type listType = new TypeToken<List<String>>() {}.getType();
		(new Gson()).fromJson(strTimestampIds, listType);
		List<String> timestampIds_ = (new Gson()).fromJson(strTimestampIds, listType);
		List<Timestamp> timestampIds = new ArrayList<Timestamp>();
		for( String strTimestampId : timestampIds_) {
			timestampIds.add(new Timestamp(Long.valueOf(strTimestampId).longValue(),0));
		}
		String callbackFunc = body.optString("callBackFunc");
//		FbGodWhisper.delComment(mContext, docId, prayComment, new ParamCallback<PrayComment>() {
		FbGodWhisper.selectPrayMemo(mContext, timestampIds, new ParamCallback<List<PrayMemo>>() {
			@Override
			public void onResult(List<PrayMemo> result) {
				callJavascriptFunc(callbackFunc, StringUtils.defaultString((new Gson()).toJson(result), ""));
			}
		});
	}

	private void api_822_DeletePrayMemo(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");
		String classDocId = body.optString("classDocId");
		String comment = body.optString("comment");
		PrayComment prayComment = new PrayComment();
		prayComment.setDocId(docId);
		prayComment.setClassDocId(classDocId);
//		Type listType = new TypeToken<List<String>>() {}.getType();
//		(new Gson()).fromJson(strGroups, listType);
//		prayClass.setGroups((new Gson()).fromJson(strGroups, listType));

		String callbackFunc = body.optString("callBackFunc");
		FbGodWhisper.deletePrayComment(mContext, prayComment, new ParamCallback<String>() {
			@Override
			public void onResult(String result) {
				callJavascriptFunc(callbackFunc, result);
			}
		});
	}


	private void api_900_BindAlarm(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");

		String callbackFunc = body.optString("callBackFunc");
		AlarmUtil.bindAlarm(mContext, new ParamCallback<List<PrayClass>>() {
			@Override
			public void onResult(List<PrayClass> result) {
				callJavascriptFunc(callbackFunc, StringUtils.defaultString((new Gson()).toJson(result)));
			}
		});
	}
	private void api_901_AddAlarm(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");
		String prayClassStr = body.optString("prayClass");
		PrayClass prayClass = (new Gson()).fromJson(prayClassStr, PrayClass.class);
		String callbackFunc = body.optString("callBackFunc");
		AlarmUtil.addAlarm(mContext, prayClass, new ParamCallback<PrayClass>() {
			@Override
			public void onResult(PrayClass result) {
				callJavascriptFunc(callbackFunc, StringUtils.defaultString((new Gson()).toJson(result)));
			}
		});
	}
	private void api_902_CancelAlarm(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String docId = body.optString("docId");
		String prayClassStr = body.optString("prayClass");
		PrayClass prayClass = (new Gson()).fromJson(prayClassStr, PrayClass.class);
		String callbackFunc = body.optString("callBackFunc");
		AlarmUtil.cancelAlarm(mContext, prayClass, new ParamCallback<PrayClass>() {
			@Override
			public void onResult(PrayClass result) {
				callJavascriptFunc(callbackFunc, StringUtils.defaultString((new Gson()).toJson(result)));
			}
		});
	}
	private void api_910_ShareLink(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String data = body.optString("data");
		String title = body.optString("title");
		Intent intent = new Intent(Intent.ACTION_SEND);
//		intent.setType("text/html");
//		intent.setType("text/plain");

//		intent.putExtra(Intent.EXTRA_TEXT, data);
//		((MainActivity) mContext).startActivity(Intent.createChooser(intent, title));
//		try {
//			title = URLDecoder.decode(title,"utf-8");
//		} catch (UnsupportedEncodingException e) {
////            throw new RuntimeException(e);
//		}
//		try {
//			title = URLDecoder.decode(title,"utf-8");
//		} catch (UnsupportedEncodingException e) {
////            throw new RuntimeException(e);
//		}
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, data);
		((MainActivity) mContext).startActivity(Intent.createChooser(intent, title));
	}

	/**
	 * 시작
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_001_Start(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
//		String key = body.getString("key");
//		Intent serviceIntent = new Intent(mContext, ConnectServiceNew.class);
		Intent in = new Intent(mContext, ConnectService.class);
		L.d(TAG,"api_001_Start","isStart() : " + isStart());
		if ( MapUtil.isAppEnable() ) {
			MapUtil.startService(mContext);
		}
		String callbackFunc = body.optString("callBackFunc");
		callJavascriptFunc(callbackFunc, "ok");
	}

	/**
	 * 정지
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_002_Stop(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
//		String key = body.getString("key");
//		Intent serviceIntent = new Intent(mContext, PowerConnEnumDevModeectRestartService.class);
		Intent in = new Intent(mContext, ConnectService.class);
		L.d(TAG,"api_002_Stop","isStart() : " + isStart());
		MapUtil.stopService(mContext);
		String callbackFunc = body.optString("callBackFunc");
		callJavascriptFunc(callbackFunc, "ok");
	}

	/**
	 * 권한 설정 화면 실행
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_003_StartSetting(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String grant = body.optString("grant");
		String callbackFunc = body.optString("callBackFunc");
		L.d(TAG,"api_003_StartSetting","grant : " + StringUtils.defaultString(grant,""));
		if ( "overlay".equals(grant) ) { // overlay
			PermissionUtil.startRequestPermission((MainActivity)mContext, PermissionType.OVERLAY,grant, callbackFunc);
//			((MainActivity) mContext).startSettingOverlay(grant, callbackFunc);
		} else if ( "usagestats".equals(grant) ) { // usagestats
			PermissionUtil.startRequestPermission((MainActivity)mContext, PermissionType.USAGESTATS,grant, callbackFunc);
//			((MainActivity)mContext).startSettingUsageStats(grant, callbackFunc);
		} else if ( "gps".equals(grant) ) { // gps
			PermissionUtil.request((MainActivity)mContext, PermissionType.LOCATION_GPS,null);
//			((MainActivity)mContext).startSettingLocationGPS(grant, callbackFunc);
		} else if ( "location".equals(grant) ) { // location
			// (MainActivity)
			PermissionUtil.startRequestPermission(mContext, PermissionType.LOCATION,grant, callbackFunc);
//			((MainActivity)mContext).startRequestLocation(grant, callbackFunc);
		} else if ( "background_location".equals(grant) ) { // location
			PermissionUtil.startRequestPermission(mContext, PermissionType.BACKGROUND_LOCATION,grant, callbackFunc);
//			((MainActivity)mContext).startRequestLocation(grant, callbackFunc);
		} else if ( "post_notifications".equals(grant) ) { // location
			PermissionUtil.startRequestPermission((MainActivity)mContext, PermissionType.POST_NOTIFICATIONS,grant, callbackFunc);
//			((MainActivity)mContext).startRequestLocation(grant, callbackFunc);
		} else if ( "bluetooth".equals(grant) ) { // bluetooth
			PermissionUtil.startRequestPermission((Activity) mContext, PermissionType.BLUETOOTH,grant, callbackFunc);
//			((MainActivity)mContext).startRequestLocation(grant, callbackFunc);
		} else if ( "notification_access".equals(grant) ) { // notification_access
			PermissionUtil.startRequestPermission((MainActivity)mContext, PermissionType.NOTIFICAION_ACCESS,grant, callbackFunc);
//			((MainActivity) mContext).startSettingNotificationAccess(grant, callbackFunc);
		} else if ( "notification_setting".equals(grant) ) { // notification_setting
			PermissionUtil.startSettingNotificationSetting(mContext,grant, callbackFunc);
		} else if ( "accessibility".equals(grant) ) { // accessibility
			PermissionUtil.startRequestPermission((MainActivity)mContext, PermissionType.ACCESSIBILITY,grant, callbackFunc);
//			((MainActivity)mContext).startSettingAccessibility(grant, callbackFunc);
		} else if ( "ignore_battery".equals(grant) ) { // ignore_battery
			PermissionUtil.startRequestPermission((MainActivity)mContext, PermissionType.IGNORE_BATTERY,grant, callbackFunc);
//			((MainActivity)mContext).startSettingIgnoreBattery(grant, callbackFunc);
		} else if ( "write_settings".equals(grant) ) { // write_settings
			PermissionUtil.startRequestPermission((MainActivity) mContext, PermissionType.WRITE_SETTING, grant, callbackFunc);
//			((MainActivity)mContext).startSettingWriteSetting(grant, callbackFunc);
		} else if ( "alarm".equals(grant) ) { // overlay
			PermissionUtil.startRequestPermission((MainActivity)mContext, PermissionType.ALARM,grant, callbackFunc);
//			((MainActivity) mContext).startSettingOverlay(grant, callbackFunc);
		}
//		callJavascriptFunc(callbackFunc, "ok");
	}

	/**
	 * 권한 체크
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_123_CheckPermission(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String grant = body.optString("grant");
		String callbackFunc = body.optString("callBackFunc");
		L.d(TAG,"api_123_CheckPermission","grant : " + StringUtils.defaultString(grant,""));
		if ( "overlay".equals(grant) ) { // overlay
			PermissionUtil.checkPermission((MainActivity)mContext, PermissionType.OVERLAY,grant, callbackFunc);
//			((MainActivity) mContext).startSettingOverlay(grant, callbackFunc);
		} else if ( "usagestats".equals(grant) ) { // usagestats
			PermissionUtil.checkPermission((MainActivity)mContext, PermissionType.USAGESTATS,grant, callbackFunc);
//			((MainActivity)mContext).startSettingUsageStats(grant, callbackFunc);
		} else if ( "gps".equals(grant) ) { // gps
			PermissionUtil.request((MainActivity)mContext, PermissionType.LOCATION_GPS,null);
//			((MainActivity)mContext).startSettingLocationGPS(grant, callbackFunc);
		} else if ( "location".equals(grant) ) { // location
			PermissionUtil.checkPermission((MainActivity)mContext, PermissionType.LOCATION,grant, callbackFunc);
		} else if ( "background_location".equals(grant) ) { // location
			PermissionUtil.checkPermission((MainActivity)mContext, PermissionType.BACKGROUND_LOCATION,grant, callbackFunc);
//			((MainActivity)mContext).startRequestLocation(grant, callbackFunc);
		} else if ( "post_notifications".equals(grant) ) { // location
			PermissionUtil.checkPermission((MainActivity)mContext, PermissionType.POST_NOTIFICATIONS,grant, callbackFunc);
//			((MainActivity)mContext).startRequestLocation(grant, callbackFunc);
		} else if ( "bluetooth".equals(grant) ) { // bluetooth
			PermissionUtil.checkPermission((Activity) mContext, PermissionType.BLUETOOTH,grant, callbackFunc);
//			((MainActivity)mContext).startRequestLocation(grant, callbackFunc);
		} else if ( "notification_access".equals(grant) ) { // notification_access
			PermissionUtil.checkPermission((MainActivity)mContext, PermissionType.NOTIFICAION_ACCESS,grant, callbackFunc);
//			((MainActivity) mContext).startSettingNotificationAccess(grant, callbackFunc);
		} else if ( "notification_setting".equals(grant) ) { // notification_setting
			PermissionUtil.startSettingNotificationSetting(mContext,grant, callbackFunc);
		} else if ( "accessibility".equals(grant) ) { // accessibility
			PermissionUtil.checkPermission((MainActivity)mContext, PermissionType.ACCESSIBILITY,grant, callbackFunc);
//			((MainActivity)mContext).startSettingAccessibility(grant, callbackFunc);
		} else if ( "ignore_battery".equals(grant) ) { // ignore_battery
			PermissionUtil.checkPermission((MainActivity)mContext, PermissionType.IGNORE_BATTERY,grant, callbackFunc);
//			((MainActivity)mContext).startSettingIgnoreBattery(grant, callbackFunc);
		} else if ( "write_settings".equals(grant) ) { // write_settings
			PermissionUtil.checkPermission((MainActivity)mContext, PermissionType.WRITE_SETTING,grant, callbackFunc);
//			((MainActivity)mContext).startSettingWriteSetting(grant, callbackFunc);
		}
//		callJavascriptFunc(callbackFunc, "ok");
	}

	/**
	 * 권한 체크
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_004_CheckGrant(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String grant = body.optString("grant");
		String callbackFunc = body.optString("callBackFunc");
		L.d(TAG,"api_004_CheckGrant","grant : " + StringUtils.defaultString(grant,""));
		if ( "overlay".equals(grant) ) { // overlay
			boolean ok = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? true : false;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(mContext))
				ok = true;
			callJavascriptFunc(callbackFunc, grant, ok ? "ok" : "not");
		} else if ( "alarm".equals(grant) ) { // overlay
			boolean ok = PermissionUtil.hasAlarmPermissions(mContext);
			callJavascriptFunc(callbackFunc, grant, ok ? "ok" : "not");
		} else if ( "usagestats".equals(grant) ) { // usagestats
			boolean ok = PermissionUtil.hasUsageStatsPermission(mContext);
			callJavascriptFunc(callbackFunc,grant, ok?"ok":"not");
		} else if ( "gps".equals(grant) ) { // gps
			boolean ok = MapUtil.isEnableLocationServicesStatus(mContext);
			callJavascriptFunc(callbackFunc,grant, ok?"ok":"not");
		} else if ( "location".equals(grant) ) { // location
			boolean ok = PermissionUtil.isLocationPermission(mContext);
			callJavascriptFunc(callbackFunc,grant, ok?"ok":"not");
		} else if ( "background_location".equals(grant) ) { // location
			boolean ok = PermissionUtil.isBackgroundLocationPermission(mContext);
			callJavascriptFunc(callbackFunc, grant, ok ? "ok" : "not");
		} else if ( "post_notifications".equals(grant) ) { // location
			boolean ok = PermissionUtil.isPostNotificationsPermission(mContext);
			callJavascriptFunc(callbackFunc,grant, ok?"ok":"not");
		} else if ( "bluetooth".equals(grant) ) { // location
			boolean ok = PermissionUtil.isBluetoothPermission(mContext);
			callJavascriptFunc(callbackFunc,grant, ok?"ok":"not");
		} else if ( "notification_access".equals(grant) ) { // alarm_access
			boolean ok = PermissionUtil.hasNotificationAccessPermission(mContext);
			callJavascriptFunc(callbackFunc,grant, ok?"ok":"not");
		} else if ( "accessibility".equals(grant) ) { // accessibility
			boolean ok = PermissionUtil.hasAccessibilityPermissions();
			callJavascriptFunc(callbackFunc,grant, ok?"ok":"not");
		} else if ( "ignore_battery".equals(grant) ) { // ignore_battery
			boolean ok = PermissionUtil.hasIgnoreBatteryPermissions();
			callJavascriptFunc(callbackFunc,grant, ok?"ok":"not");
		} else if ( "write_settings".equals(grant) ) { // write_settings
			boolean ok = true;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				ok =  Settings.System.canWrite(mContext);
			}
			callJavascriptFunc(callbackFunc,grant, ok?"ok":"not");
		}
	}

	/**
	 * 위치 체크
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_005_GetLocation(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
//		String key = body.getString("key");
//		Intent serviceIntent = new Intent(mContext, MyService.class);
		String mapId = body.optString("mapVar");
		String callbackFunc = body.optString("callBackFunc");
		GpsTracker gpsTracker = new GpsTracker(mContext);
		double latitude = gpsTracker.getLatitude();
		double longitude = gpsTracker.getLongitude();
		L.d(TAG,"api_005_GetLocation","latitude / longitude : " + latitude + " / " + longitude);
		String address = MapUtil.getCurrentAddress(mContext,latitude, longitude);
		JavaScriptBridge.callJavascriptFunc(callbackFunc, mapId, latitude, longitude, address);
	}

	/**
	 * 안내 시작 - NaviDestination
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_007_StartNaviDestination(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String destination = body.optString("destination");
//		Intent serviceIntent = new Intent(mContext, MyService.class);
		String callbackFunc = body.optString("callBackFunc");
		GpsTracker gpsTracker = new GpsTracker(mContext);
		double latitude = gpsTracker.getLatitude();
		double longitude = gpsTracker.getLongitude();
		MainActivity activity = ((MainActivity)mContext);
		String address = MapUtil.getCurrentAddress(mContext,latitude, longitude);
		activity.moveTaskToBack(true);

		MapUtil.setNotNaving();
		NaviDestination naviDestination = NaviDestination.value(destination);
		L.d(TAG,"api_007_StartNaviDestination","naviDestination : " + (naviDestination != null?naviDestination.name():"-"));
        if ( naviDestination != null ) {
			OverlayViewUtil.init(mContext).startNavi(naviDestination,true,false);
        }

		//JavaScriptBridge.callJavascriptFunc(callbackFunc, latitude, longitude, address);
	}


	/**
	 * 안내 시작
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_006_StartNavi(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String name = body.optString("name");
		String lat = body.optString("lat");
		String lon = body.optString("lon");
//		Intent serviceIntent = new Intent(mContext, MyService.class);
		String callbackFunc = body.optString("callBackFunc");
		GpsTracker gpsTracker = new GpsTracker(mContext);
		double latitude = gpsTracker.getLatitude();
		double longitude = gpsTracker.getLongitude();
		L.d(TAG,"api_006_StartNavi","latitude / longitude : " + latitude + " / " + longitude);
		if(mContext instanceof MainActivity){
			MainActivity activity = ((MainActivity)mContext);
			activity.moveTaskToBack(true);
		} else if(mContext instanceof WebPopActivity){
			WebPopActivity activity = ((WebPopActivity)mContext);
			activity.moveTaskToBack(true);
		}
		String address = MapUtil.getCurrentAddress(mContext,latitude, longitude);

		Toast.makeText(mContext, name, Toast.LENGTH_LONG).show();

		MapUtil.setNotNaving();
//		MapUtil.startNavi(mContext,true);
		if ( Utils.isEmpty(name) ) {
//			MapUtil.startNaviOld(mContext,true, false);
			OverlayViewUtil.init(mContext).startNavi(NaviDestination.BASIC,false,false);
		} else {
//			startNavi(Context context, NaviDestination naviDestination, boolean immediate, boolean booted) {
//				MapPoint mapPoint = new MapPoint(name,latitude,longitude);

//				MapUtil.startNavi(mContext, mapPoint,true);
			OverlayViewUtil.init(mContext).startNavi(NaviDestination.BASIC, true,false,true,name, Float.parseFloat(lat), Float.parseFloat(lon));
//			d(@NonNull final Context context, NaviDestination naviDestination, boolean immediate, boolean booted, String name, float lat, float  lon)
		}

		//JavaScriptBridge.callJavascriptFunc(callbackFunc, latitude, longitude, address);
	}

	/**
	 * API 100 Alert 다이얼로그 호출 요청을 처리한다.
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_100_Alert(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String title = body.getString("title");
		String msg = body.getString("msg");
		mCallbackFuncName = body.optString("callBackFunc");
		BasicActivity activity = ((BasicActivity)mContext);
		L.d(TAG,"api_100_Alert","title / msg : " + StringUtils.defaultString(title,"") + " / " + StringUtils.defaultString(msg,""));
		activity.alert( title, msg, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!TextUtils.isEmpty(mCallbackFuncName)) {
					callJavascriptFunc(mCallbackFuncName);
				}
			}
		});
	}
	/**
	 * Conform 다이얼로그 호출 요청을 처리한다.
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_101_Conform(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String title = body.optString("title");
		String msg = body.optString("msg");
		String cancel = body.optString("cancelBtn");
		String ok = body.optString("okBtn");
		mCallbackFuncName = body.optString("callBackFunc");
		BasicActivity activity = ((BasicActivity)mContext);
		L.d(TAG,"api_101_Conform","title / msg : " + StringUtils.defaultString(title,"") + " / " + StringUtils.defaultString(msg,""));
		activity.confirm(title, ok, cancel, msg, 0, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!TextUtils.isEmpty(mCallbackFuncName)) {
					callJavascriptFunc(mCallbackFuncName);
				}
			}
		}, new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Nothing
			}
		});
	}
	/**
	 * 단순 메시지창
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_102_Toast(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String msg = body.getString("msg");
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		L.d(TAG,"api_102_Toast","msg : " + StringUtils.defaultString(msg,""));
	}
	/**
	 * goMain
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_110_GoMain(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String restart = body.getString("restart");
		String callBackFunc = body.optString("callBackFunc");
		if(mContext instanceof MainActivity){
			L.d(TAG,"api_110_GoMain","restart : " + StringUtils.defaultString(restart,""));
			Utils.goMain(mContext,Boolean.valueOf(restart));
		}
	}

	/**
	 * goPermission
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_112_GoPermission(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String P_CHECK_MODE = body.getString("P_CHECK_MODE");
		String P_PARAMS = body.optString("P_PARAMS");

		String callBackFunc = body.optString("callBackFunc");
		if(mContext instanceof MainActivity){
			L.d(TAG,"api_112_GoPermission","P_CHECK_MODE : " + P_CHECK_MODE,"P_PARAMS : " + P_PARAMS);
			Utils.goPermission(mContext,P_CHECK_MODE,P_PARAMS);
		}
	}

	/**
	 * goPermission
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_113_GoFoward(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String P_FOWARD_PAGE = body.getString("P_FOWARD_PAGE");
		String P_TIDX = body.optString("P_TIDX");
		String P_PARAMS = body.optString("P_PARAMS");
		if(Utils.isEmpty(P_TIDX)) {
			P_TIDX = "0";
		}
		String callBackFunc = body.optString("callBackFunc");
		if(mContext instanceof MainActivity){
			L.d(TAG,"api_113_GoFoward","P_FOWARD_PAGE : " + P_FOWARD_PAGE,"P_TIDX : " + P_TIDX,"P_PARAMS : " + P_PARAMS);
			Utils.goFowardPage(mContext,P_FOWARD_PAGE,Integer.valueOf(P_TIDX),P_PARAMS);
		}
	}

	/**
	 * goTest
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_111_GoTest(JSONObject json) throws JSONException {
//		JSONObject body = json.getJSONObject("body");
//		String callBackFunc = body.optString("callBackFunc");
		if(mContext instanceof MainActivity){
			L.d(TAG,"api_111_GoTest");
			Utils.goTest(mContext);
		}
	}

	/**
	 * url loadUrl
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_400_ShowUrl(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String url = body.getString("url");
		String callBackFunc = body.optString("callBackFunc");
		L.d(TAG,"api_400_ShowUrl","restart : " + StringUtils.defaultString(url,""));
		if(mContext instanceof MainActivity){
			loadUrl(url);
//            URL url_ = null;
//            try {
//                url_ = new URL(url);
//            } catch (MalformedURLException e) {
//            } finally {
//			}
//            // 경로에서 파일명 추출
//			String fileName = url_!=null?new java.io.File(url_.getPath()).getName():url;
//			// 파일명에서 확장자를 제외한 앞부분만 추출
//			String fileBaseName = fileName.substring(0, fileName.lastIndexOf('.'));
//			loadUrl(WUrl.ReqId.valueOf("WEB_"+fileBaseName.toUpperCase()).getUrl());
		} else if(mContext instanceof WebPopActivity){
			loadUrl(url);
		}
	}

	/**
	 * url url reload
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_401_Reload(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String reload = body.getString("reload");
		String callBackFunc = body.optString("callBackFunc");
		L.d(TAG,"api_401_Reload","reload : " + StringUtils.defaultString(reload,""));
		RestUtil.getBasicSettings(mContext,()->{
		},false);
		RestUtil.getBasicInfo(mContext,()->{
			((Activity) mContext).runOnUiThread(() -> {
				mWebView.reload();
			});
		},false);
	}

	/**
	 * copy
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_402_CopyClipboard(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String copy = body.getString("copy");
		String callBackFunc = body.optString("callBackFunc");
		L.d(TAG,"api_402_CopyClipboard","copy : " + StringUtils.defaultString(copy,""));
		ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("label", copy);
		clipboard.setPrimaryClip(clip);
	}

	/**
	 * 팝업호출1
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_103_ShowPopup(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String url = body.getString("url");
		String callBackFunc = body.optString("callBackFunc");
		L.d(TAG,"api_103_ShowPopup","url : " + StringUtils.defaultString(url,""));
		if(mContext instanceof MainActivity){
			Intent intent = new Intent(mContext, WebPopActivity.class);
			intent.putExtra("url", url);
			intent.putExtra("callBackFunc", callBackFunc);

			((MainActivity)mContext).startActivityForResult(intent, JavaScriptApi.API_103);

			CookieManager cookieManager = CookieManager.getInstance();
			String coolie = cookieManager.getCookie(WUrl.mBaseUrl);

			WLog.d("TAG", "test coolie : " + coolie);
		}
	}

	/**
	 * 팝업닫기1
	 * @param json JSON 데이터
	 */
	private void api_106_ClosePopup(JSONObject json) {
		JSONObject body;
		String jsonData = null;
		String callBackFunc = null;
		try {
			body = json.getJSONObject("body");
			jsonData = body.getString("jsonData");
			callBackFunc = body.getString("callBackFunc");
		} catch (JSONException e) {
			printException(e);
			jsonData = "";
			callBackFunc = "";
		}finally {
			L.d(TAG,"api_106_ClosePopup","callBackFunc / jsonData : " + StringUtils.defaultString(callBackFunc,"") + " / " + StringUtils.defaultString(jsonData,""));
			if(mContext instanceof WebPopActivity){
				Intent intent = new Intent();
				intent.putExtra("jsonData", jsonData);
				intent.putExtra("callBackFunc", callBackFunc);
				((WebPopActivity)mContext).setResult(Activity.RESULT_OK, intent);
				((WebPopActivity)mContext).finish();
			}
		}

	}

	/**
	 * 모바일웹열기
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_107_OutBrowser(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String url = Html.fromHtml(body.getString("url")).toString();
		WLog.e(TAG, "url : " + url);
		L.d(TAG,"api_107_OutBrowser","url : " + StringUtils.defaultString(url,"") );
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(i);
	}


	/**
	 * 키보드 보이고/안보이고
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_108_VisibleKeyboard(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String visible = body.optString("visible");
		L.d(TAG,"api_108_VisibleKeyboard","visible : " + StringUtils.defaultString(visible,"") );
		if ("Y".equals(visible)) {
			// keyboard
			InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			TimerTask myTask = new TimerTask(){
				public void run(){
					if( imm != null )
						try {
							imm.showSoftInput(((MainActivity)mContext).getCurrentFocus(), InputMethodManager.SHOW_FORCED);
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			};

			Timer timer = new Timer();
			timer.schedule(myTask, 500);
		} else {
			((MainActivity)mContext).getCurrentFocus();
			InputMethodManager manager = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			manager.hideSoftInputFromWindow(((MainActivity)mContext).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 보안키패드
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_109_SecureKey(JSONObject json) throws JSONException {

	}
	/**
	 * 사진촬영
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_114_CallCamera(JSONObject json) throws JSONException {

	}

	/**
	 * 프로그래스 on
	 */
	private void api_115_ShowProgress(){
		L.d(TAG,"api_115_ShowProgress","" );
		if(mContext instanceof MainActivity){
			((MainActivity)mContext).showProgress();
		}else if(mContext instanceof WebPopActivity){
			((WebPopActivity)mContext).showProgress();
		}
	}

	/**
	 * 프로그래스 off
	 */
	private void api_116_StopProgress(){
		L.d(TAG,"api_116_StopProgress","" );
		if(mContext instanceof MainActivity){
			((MainActivity)mContext).stopProgress();
		}else if(mContext instanceof WebPopActivity){
			((WebPopActivity)mContext).stopProgress();
		}
	}

	/**
	 * 오버레이 중지
	 */
	private void api_500_StopOverlay(){
		L.d(TAG,"api_500_StopOverlay","" );
		OverlayViewUtil.stopService();
	}

	/**
	 * 오버레이 숨기기
	 */
	private void api_501_HideOverlay(){
		L.d(TAG,"api_501_HideOverlay","" );
		OverlayViewUtil.hide();
	}

	/**
	 * 오버레이 표시
	 */
	private void api_502_ShowOverlay(){
//		OverlayViewUtil.show(mContext);
	}

	/**
	 * 오버레이 표시
	 */
	private void api_503_OpenOverlayView(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
//		OverlayViewUtil.show(mContext);
		Boolean expandSub = body.getBoolean("expandSub");
		OverlayViewUtil.init(mContext).openOverlayViewShowOnly(expandSub);
	}

	/**
	 * blob 파일 다운로드
	 */
	private void api_504_DownloadBase64File(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String blob = body.getString("blob");
		String fileName = body.getString("fileName");
		downloadBase64File(blob,fileName);
	}

	/**
	 * 프로그래스 on (타이머 적용)
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_117_ShowProgressWithTimer(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		int time = body.optInt("time");
		L.d(TAG,"api_117_ShowProgressWithTimer","time : " + String.valueOf(time) );
		if(mContext instanceof MainActivity){
			MainActivity activity = (MainActivity)mContext;
			activity.showProgress(time, activity.mWebview);
		}else if(mContext instanceof WebPopActivity){
			WebPopActivity activity = (WebPopActivity)mContext;
			activity.showProgress(time, activity.mBaseWebView);
		}
	}

	/**
	 * 앱강제 종료
	 */
	private void api_119_AppExit(){
		if(mContext instanceof MainActivity){
			L.d(TAG,"api_119_AppExit","MainActivity" );
			((MainActivity)mContext).finish();
		}else if(mContext instanceof WebPopActivity){
			L.d(TAG,"api_119_AppExit","WebPopActivity" );
			WebPopActivity activity = (WebPopActivity)mContext;
			Intent intent = new Intent();
			intent.putExtra(Const.INTENT_APP_FINISH, true);
			activity.setResult(Activity.RESULT_CANCELED, intent);
			activity.finish();
		}
	}


	/**
	 * 데이터 저장
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_120_SaveData(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		String value = body.getString("value");

		Bundle pBundle = new Bundle();
		pBundle.putString(StringUtils.defaultString(key,"").substring(1) ,StringUtils.defaultString(value,""));
		Fa.i(mContext,TAG,Fa.EVENT_SAVE_SETTING, pBundle);

		Gson gson = new Gson();
		if( key.equals(OrgConst.PREF_KEY_GROUPS) ) {
			OrgUtil.setGroups(value);
		} else if( key.equals(OrgConst.PREF_KEY_ORG) ) {
			OrgUtil.setOrg(value);
		} else if( key.equals(OrgConst.PREF_KEY_ORG_REG) ) {
			OrgUtil.setGroups(value);
		} else if( key.equals(OrgConst.PREF_KEY_PROFILE_NAME) ) {
			OrgUtil.setProfileName(value);
		} else if( key.equals(OrgConst.PREF_KEY_NICK_NAME) ) {
			OrgUtil.setNickName(value);
		} else if( key.equals(Const.PREF_KEY_APP_INSTALLED) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setAppInstalled(v);
		} else if( key.equals(Const.PREF_KEY_SKIP_ACS_PERMISSION) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setSkipAcsPermission(v);
		} else if( key.equals(Const.PREF_KEY_APP_UPDATED) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setAppUpdated(v);
		} else if( key.equals(Const.PREF_KEY_APP_UPDATED_NOTI_CONFIRM) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setAppUpdatedNotiConfirm(v);
		} else if( key.equals(Const.PREF_KEY_FIRST_START) ) {
			MapUtil.setFirstStart(value.equals("1"));
//		} else if( key.equals(Const.PREF_KEY_PERMISSION_DODENIED_POST_NOTIFICATIONS) ) {
//			Boolean v = body.getBoolean("value");
//			MapUtil.setPermissionDoDeniedForPostNotifications(v);
		} else if( key.equals(Const.PREF_KEY_APP_PACKAGE) ) {
			MapUtil.setAppPackage(value);
			if ( MapUtil.isOverlayOpen() ) {
//				OverlayViewUtil.show();
				OverlayViewUtil.init(mContext).openOverlayViewShowOnly();
			}
		} else if( key.equals(Const.PREF_KEY_MUSIC_APP_PACKAGE) ) {
			MapUtil.setMusicAppPackage(value);
			if ( MapUtil.isOverlayOpen() ) {
//				OverlayViewUtil.show();
				OverlayViewUtil.init(mContext).openOverlayViewShowOnly();
			}
		} else if( key.equals(Const.PREF_KEY_BLUETOOTH) ) {
			MapUtil.setBluetooth(value);
		} else if( key.equals(Const.PREF_KEY_BLUETOOTHS) ) {
			MapUtil.setBluetooths(value);
		} else if( key.equals(Const.PREF_KEY_START_STOP_ON_OFF) ) {
			MapUtil.setStartStopOnOff(value);
		} else if( key.equals(Const.PREF_KEY_WIFI) ) {
			MapUtil.setWifi(value);
		} else if( key.equals(Const.PREF_KEY_OPEN_POPUP) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setOpenPopup(v?"1":"0");
			if ( !v ) {
				if ( wifiScanReceiver != null ) {
					mContext.unregisterReceiver(wifiScanReceiver);
					wifiScanReceiver = null;
				}
			}
		} else if( key.equals(Const.PREF_KEY_START_POWER) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setStart(v);
		} else if( key.equals(Const.PREF_KEY_APP_ENABLE) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setAppEnable(v?AppEnable.ENABLE:AppEnable.DISABLE);
			Fa.i(mContext,TAG,v?Fa.EVENT_APP_ENABLE:Fa.EVENT_APP_DISABLE);
			if ( MapUtil.isAppEnable() ) {
				if ( isStart() ) {
					MapUtil.startService(mContext);
				}
			} else {
				MapUtil.stopService(mContext);
			}
		} else if( key.equals(Const.PREF_KEY_USING_REG_HOME) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setUsingRegHome(v);
			if ( MapUtil.isOverlayOpen() ) {
				OverlayViewUtil.init(mContext).openOverlayViewShowOnly();
			}
		} else if( key.equals(Const.PREF_KEY_POI_FAVORITE) ) {
			WApplication.mInstance.setSharedPreferences(key, value);
			if ( MapUtil.isOverlayOpen() ) {
				OverlayViewUtil.init(mContext).openOverlayViewShowOnly();
			}
		} else if( key.equals(Const.PREF_KEY_POIS_FAVORITE) ) {
			WApplication.mInstance.setSharedPreferences(key, value);
//			if ( MapUtil.isOverlayOpen() ) {
//				OverlayViewUtil.init(mContext).openOverlayViewShowOnly(true);
//			}
		} else if( key.equals(Const.PREF_KEY_POI_RECENT) ) {
			MapPoint mapPoint = MapUtil.getPointFromString(value);
			MapUtil.setPoiRecent(mapPoint);
		} else if( key.equals(Const.PREF_KEY_POIS_RECENT) ) {
			WApplication.mInstance.setSharedPreferences(key, value);
//			if ( MapUtil.isOverlayOpen() ) {
//				OverlayViewUtil.init(mContext).openOverlayViewShowOnly(true);
//			}
		} else if( key.equals(Const.PREF_KEY_POI_HOME) ) {
			WApplication.mInstance.setSharedPreferences(key, value);
		} else if( key.equals(Const.PREF_KEY_POI_COMPANY) ) {
			WApplication.mInstance.setSharedPreferences(key, value);
		} else if( key.equals(Const.PREF_KEY_INDEPENDENCE_CONDITION) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setIndependenceCondition(v);
		} else if( key.equals(Const.PREF_KEY_START_BLUETOOTH) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setStartBluetooth(v);
		} else if( key.equals(Const.PREF_KEY_START_WIFI) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setStartWifi(v);
		} else if( key.equals(Const.PREF_KEY_CHECK_SERVICE) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setCheckService(v);
			WorkerUtil.cancelCheckService(mContext);
			if ( v ) {
				if ( ServiceUtil.isStartAble() ) {
					WorkerUtil.startCheckService(mContext);
				}
			}
		} else if( key.equals(Const.PREF_KEY_CHECK_SERVICE_CHECK_NOTI) ) {
			MapUtil.setCheckServiceCheckNoti(UseStatusMode.value(value));
		} else if( key.equals(Const.PREF_KEY_CHECK_SERVICE_CHECK_LOG_SEND_NOTI) ) {
			MapUtil.setCheckserviceCheckLogSendNoti(UseStatusMode.value(value));
		} else if( key.equals(Const.PREF_KEY_RUN_STATUS_SCREEN_START_MODE) ) {
			MapUtil.setRunStatusScreenStartMode(RunStatusScreenMode.value(value));
		} else if( key.equals(Const.PREF_KEY_RUN_STATUS_SCREEN_STOP_MODE) ) {
			MapUtil.setRunStatusScreenStopMode(RunStatusScreenMode.value(value));
		} else if( key.equals(Const.PREF_KEY_ONLY_STOP_AFTER_NAVING) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setOnlyStopAfterNaving(v);
		} else if( key.equals(Const.PREF_KEY_NAVI_DESTINATION) ) {
			MapUtil.setNaviDestination(NaviDestination.value(value));
		} else if( key.equals(Const.PREF_KEY_NAVI_MODE) ) {
			MapUtil.setNaviMode(NaviMode.value(value));
		} else if( key.equals(Const.PREF_KEY_MAP_MODE) ) {
			MapUtil.setMapMode(MapMode.value(value));
		} else if( key.equals(Const.PREF_KEY_NAVI_AUTO_START) ) {
			MapUtil.setNaviAutoStart(StartMode.value(value));
//		} else if( key.equals(Const.PREF_KEY_START_MODE) ) { /** 사용안함 */
//			MapUtil.setStartMode(StartMode.value(value)); /** 사용안함 */
		} else if( key.equals(Const.PREF_KEY_STOP_MODE) ) {
			MapUtil.setStopMode(StopMode.value(value));
		} else if( key.equals(Const.PREF_KEY_CHARGING_MODE) ) {
			MapUtil.setChargingMode(ChargingMode.value(value));
		} else if( key.equals(Const.PREF_KEY_GEOFENCE_RADIUS_IN_METERS) ) {
			MapUtil.setGeofenceRadiusInMeters(value);
		} else if( key.equals(Const.PREF_KEY_NAVI_OVERLAY_WAIT_TIME) ) {
			MapUtil.setOverlayWaitTime(value);
		} else if( key.equals(Const.PREF_KEY_APP_UPDATE_DELAY_TIME ) ) {
			MapUtil.setAppUpdateDelayTime(Long.parseLong(value));
		} else if( key.equals(Const.PREF_KEY_APP_UPDATE_CHECK_TIME ) ) {
			MapUtil.setAppUpdateCheckTime(Long.parseLong(value));
		} else if( key.equals(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_START) ) {
			MapUtil.setRunStatusScreenTimeForStart(value);
		} else if( key.equals(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_STOP) ) {
			MapUtil.setRunStatusScreenTimeForStop(value);
		} else if( key.equals(Const.PREF_KEY_RUN_SPEED) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setRunSpeed(v);
		} else if( key.equals(Const.PREF_KEY_GEOFENCE) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setGeofence(v);
		} else if( key.equals(Const.PREF_KEY_RUN_SPEED_LIMIT_TIME) ) {
			MapUtil.setRunSpeedLimitTime(value);
		} else if( key.equals(Const.PREF_KEY_RUN_SPEED_LIMIT) ) {
			MapUtil.setRunSpeedLimit(value);
		} else if( key.equals(Const.PREF_KEY_RENAVI_PREVENT_TIME) ) {
			MapUtil.setRenaviPreventTime(value);
		} else if( key.equals(Const.PREF_KEY_NAVI_DESTINATION_EXECUTION_HOLD_TIME) ) {
			MapUtil.setDestinationExecutionHoldTime(String.valueOf(Long.parseLong(value)));
		} else if( key.equals(Const.PREF_KEY_WORK_GOTO_OFF_TIME) ) {
			MapUtil.setWorkGotoOffTime(value.equals("1")?true:false);
		} else if( key.equals(Const.PREF_KEY_WORK_GOTO_TIME_FROM) ) {
			MapUtil.setWorkGotoTimeFrom(value);
		} else if( key.equals(Const.PREF_KEY_WORK_GOTO_TIME_TO) ) {
			MapUtil.setWorkGotoTimeTo(value);
		} else if( key.equals(Const.PREF_KEY_WORK_OFF_TIME_FROM) ) {
			MapUtil.setWorkOffTimeFrom(value);
		} else if( key.equals(Const.PREF_KEY_WORK_OFF_TIME_TO) ) {
			MapUtil.setWorkOffTimeTo(value);
		} else if( key.equals(Const.PREF_KEY_NAVI_POPUP_MODE) ) {
			MapUtil.setPopupMode(value);
			if ( !MapUtil.isOverlayClose() ) {
				OverlayViewUtil.init(mContext).openOverlayViewShowOnly();
			} else {
				OverlayViewUtil.stopService();
			}
		} else if( key.equals(Const.PREF_KEY_NOTI_ICON) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setNotiIcon(v?"1":"0");
			AcsUtil ascUtil = new AcsUtil(mContext);
			if ( MapUtil.isNotiIcon() ) {
				WApplication.mInstance.connectServiceMode = WApplication.ConnectServiceMode.NOTIFICATION_SERVICE;
			} else {
				WApplication.mInstance.connectServiceMode = WApplication.ConnectServiceMode.ACCESSIBILITY_SERVICE;
			}
			if ( WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.NOTIFICATION_SERVICE) ) {
				ascUtil.unregisterReceiver();
				ServiceUtil.init(mContext).startService(null,null,false);
			} else if ( WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.ACCESSIBILITY_SERVICE) ) {
				ServiceUtil.stop();
				ascUtil.registerReceiver();
			}
		} else if( key.equals(Const.PREF_KEY_TEST) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setTest(v?"1":"0");
		} else if( key.equals(Const.PREF_KEY_ENHANCED_EXECUTION) ) {
			Boolean v = body.getBoolean("value");
			MapUtil.setEnhancedExecution(v?"1":"0");
			if ( MapUtil.isEnhancedExecution() ) {
				WApplication.mInstance.workType = WApplication.WorkType.WORK_MANAGER;
			} else {
				WApplication.mInstance.workType = WApplication.WorkType.THREAD;
			}
			Fc.setCustomKey(Fc.CUSTOM_KEY_WORK_TYPE,WApplication.mInstance.workType.name());
		} else {
			WApplication.mInstance.setSharedPreferences(key, value);
		}

		String strBasicInfo = StringUtils.defaultString(gson.toJson(settingsInfo), "");
//		Fc.setCustomKey(Fc.CUSTOM_KEY_BASIC_INFO,strBasicInfo);
		Fc.i(mContext,TAG_SHORT,"saveData",strBasicInfo);

		String callbackFunc = body.optString("callBackFunc");
		if ( Utils.isNotEmpty(callbackFunc) ) {
			callJavascriptFunc(callbackFunc, value);
		}
	}

	/**
	 * 데이터 불러오기
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_121_LoadData(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		String value = WApplication.mInstance.getSharedPreferences(key, "");
		L.d(TAG,"api_121_LoadData","key / value : " + StringUtils.defaultString(key,"") + " / " + StringUtils.defaultString(value,"") );
		if( key.equals(Const.PREF_KEY_APP_PACKAGE) ) value = MapUtil.getAppPackage();
		if( key.equals(Const.PREF_KEY_MUSIC_APP_PACKAGE_ICON) ) value = MapUtil.getMusicAppPackage();
		if( key.equals(Const.PREF_KEY_BLUETOOTH)   ) value = MapUtil.getBluetooth().toString();
		if( key.equals(Const.PREF_KEY_BLUETOOTHS)   ) value = MapUtil.getBluetooths().toString();
		if( key.equals(Const.PREF_KEY_START_STOP_ON_OFF)   ) value = MapUtil.getStartStopOnOff().toString();

		if( key.equals(Const.PREF_KEY_WIFI)   ) value = MapUtil.getWifi().toString();

		if( key.equals(Const.PREF_KEY_OPEN_POPUP) ) value = MapUtil.isOpenPopup()?"1":"0";

		if( key.equals(Const.PREF_KEY_NAVI_DESTINATION) && "".equals(value) ) value = NaviDestination.HOME.getId(); // 집
		if( key.equals(Const.PREF_KEY_NAVI_MODE) && "".equals(value) ) value = NaviMode.HOMETOCOMPANY.getId(); // 집<>회사
		if( key.equals(Const.PREF_KEY_MAP_MODE) && "".equals(value) ) value = MapMode.TMAP.getId(); // 티맵
//		if( key.equals(Const.PREF_KEY_START_MODE) && "".equals(value) ) value = StartMode.START.getId(); // 기본시작
		if( key.equals(Const.PREF_KEY_STOP_MODE) && "".equals(value) ) value = StopMode.FORCE.getId(); // 강제종료

		if( key.equals(Const.PREF_KEY_CHARGING_MODE) && "".equals(value) ) value = String.valueOf(ChargingMode.ALL_CHARGING.getId()); // 모두
		if( key.equals(Const.PREF_KEY_START_POWER) && "".equals(value) ) value = "1"; // 시작
		if( key.equals(Const.PREF_KEY_INDEPENDENCE_CONDITION) && "".equals(value) ) value = "1"; // 독립 조건
		if( key.equals(Const.PREF_KEY_START_BLUETOOTH) && "".equals(value) ) value = "0"; // 종료
		if( key.equals(Const.PREF_KEY_START_WIFI) && "".equals(value) ) value = "0"; // 종료

		if( key.equals(Const.PREF_KEY_CHECK_SERVICE) && "".equals(value) ) value = "0"; // 안함
		if( key.equals(Const.PREF_KEY_CHECK_SERVICE_CHECK_NOTI) && "".equals(value) ) value = UseStatusMode.NONE.getId(); // 안함
		if( key.equals(Const.PREF_KEY_CHECK_SERVICE_CHECK_LOG_SEND_NOTI) && "".equals(value) ) value = UseStatusMode.NONE.getId(); // 안함

		if( key.equals(Const.PREF_KEY_RUN_STATUS_SCREEN_START_MODE) && "".equals(value) ) value = RunStatusScreenMode.NONE.getId(); // 안함
		if( key.equals(Const.PREF_KEY_RUN_STATUS_SCREEN_STOP_MODE) && "".equals(value) ) value = RunStatusScreenMode.NONE.getId(); // 안함
		if( key.equals(Const.PREF_KEY_ONLY_STOP_AFTER_NAVING) && "".equals(value) ) value = "0"; // Off
		if( key.equals(Const.PREF_KEY_NOTI_ICON) ) {
			value = MapUtil.isNotiIcon()?"1":"0"; // On
		}
		if( key.equals(Const.PREF_KEY_NAVI_POPUP_MODE) && "".equals(value) ) value = MapUtil.isOverlayOpen()?"1":"0"; // 실행 no >> 변경예정 : 오버레이 닫기:0, 유지:1
		if( key.equals(Const.PREF_KEY_NAVI_AUTO_START) && "".equals(value) ) value = StartMode.START.getId(); // isNaviAutoStart
		if( key.equals(Const.PREF_KEY_GEOFENCE_RADIUS_IN_METERS) && "".equals(value) ) {
			value = String.valueOf(GeofenceConstants.GEOFENCE_RADIUS_IN_METERS); // 목적지 진입 반경 ( m )
		}
		if( key.equals(Const.PREF_KEY_NAVI_OVERLAY_WAIT_TIME) && "".equals(value) ) {
			value = String.valueOf(Const.NAVI_OVERLAY_COUNTDOWN_TIME/1000); // 팝업(오버레이) 대기시간 ( 초 )
		}
		if( key.equals(Const.PREF_KEY_APP_UPDATE_DELAY_TIME) && "".equals(value) ) {
			value = String.valueOf(Const.APP_UPDATE_DELAY_TIME); //
		}
		if( key.equals(Const.PREF_KEY_APP_UPDATE_CHECK_TIME) && "".equals(value) ) {
//			value = String.valueOf(Const.APP_UPDATE_DELAY_TIME); //
		}
		if( key.equals(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_START) && "".equals(value) ) {
			value = String.valueOf(Const.RUN_STATUS_SCREEN_TIME_FOR_START /1000); // 화면 켜진 상태 : 실행 제외 : 체크 시간 ( 초 )
		}
		if( key.equals(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_STOP) && "".equals(value) ) {
			value = String.valueOf(Const.RUN_STATUS_SCREEN_TIME_FOR_STOP /1000); // 화면 켜진 상태 : 실행 제외 : 체크 시간 ( 초 )
		}
		if( key.equals(Const.PREF_KEY_RENAVI_PREVENT_TIME) && "".equals(value) ) {
			value = String.valueOf(Const.NAVI_RENAVI_PREVENT_TIME/1000); // 내비 재실행 방지시간
		}

		if( key.equals(Const.PREF_KEY_NAVI_DESTINATION_EXECUTION_HOLD_TIME) && "".equals(value) ) {
			value = String.valueOf(Const.NAVI_DESTINATION_EXECUTION_HOLD_TIME/1000); // 내비 재실행 방지시간
		}

		if( key.equals(Const.PREF_KEY_GEOFENCE_RADIUS_IN_METERS) && "".equals(value) ) {
			value = String.valueOf(GeofenceConstants.GEOFENCE_RADIUS_IN_METERS); // 목적지 진입 반경 ( m )
		}

//		if ( key.equals(Const.PREF_KEY_WORK_GOTO_OFF_TIME ) ) value = "0"; // 중지
//		if ( key.equals(Const.PREF_KEY_WORK_GOTO_TIME_FROM) ) value = ""; //
//		if ( key.equals(Const.PREF_KEY_WORK_GOTO_TIME_TO  ) ) value = "";
//		if ( key.equals(Const.PREF_KEY_WORK_OFF_TIME_FROM ) ) value = "";
//		if ( key.equals(Const.PREF_KEY_WORK_OFF_TIME_TO   ) ) value = "";

		String callbackFunc = body.optString("callBackFunc");
		callJavascriptFunc(callbackFunc, value);
	}

	/**
	 * 전화걸기
	 * @param json json data
	 * @throws JSONException
	 */
	private void api_126_call(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String tel = body.optString("tel");
		L.d(TAG,"api_126_call","tel : " + StringUtils.defaultString(tel,"") );
		try {
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tel));
			mContext.startActivity(intent);
		} catch (SecurityException e) {
			printException(e);
		}
	}

	/**
	 * 네이트브정보 (UUID)
	 * add softm
	 */
	private void api_142_GetNInfo(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		mCallbackFuncName = body.optString("callBackFunc");
		L.d(TAG,"api_142_GetNInfo", StringUtils.defaultString(getBasicInformation(),"") );
		callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, getBasicInformation() );
	}
	private static HashMap<String,Object> settingsInfo = new HashMap<String,Object>();
	private void setInfo(String k, Object v)  {
		String key = Utils.toCamelCase(k.substring(1).toLowerCase());
		settingsInfo.put(key,v);
	}

	private void api_333_GetPackagesInfo(JSONObject json) throws JSONException {
		Gson gson = new Gson();
//		WebBasicInformation data = new WebBasicInformation();
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		mCallbackFuncName = body.optString("callBackFunc");
		L.d(TAG,"api_333_GetPackagesInfo","key : " + StringUtils.defaultString(gson.toJson(Package.getPakages(key)),"") );
		Log.i(TAG,"gson.toJson(Package.getPakages(key)) : " + gson.toJson(Package.getPakages(key)));
		callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(Package.getPakages(key)));
	}
	private void api_334_GetMusicPackagesInfo(JSONObject json) throws JSONException {
		Gson gson = new Gson();
//		WebBasicInformation data = new WebBasicInformation();
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		mCallbackFuncName = body.optString("callBackFunc");
		L.d(TAG,"api_334_GetMusicPackagesInfo","key : " + StringUtils.defaultString(gson.toJson(PackageMusic.getPakages(key)),"") );
		Log.i(TAG,"gson.toJson(PackageMusic.getPakages(key)) : " + gson.toJson(PackageMusic.getPakages(key)));
		callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(PackageMusic.getPakages(key)));
	}
	private HashMap<String,Object> genInfo() {
		settingsInfo = new HashMap<String,Object>();
		setInfo("_VERSION_CODE", BuildConfig.VERSION_CODE);
		setInfo("_VERSION_NAME", BuildConfig.VERSION_NAME);
		setInfo("_APPLICATION_ID", BuildConfig.APPLICATION_ID);

		setInfo("_SERVER_HOST", Const.SERVER_HOST);
		setInfo("_PATH_NAME", Const.PATH_NAME);

		setInfo(Const.PREF_KEY_APP_PACKAGE, MapUtil.getAppPackage());
		Package builtInpkg = Package.value(MapUtil.getAppPackage());
		setInfo(Const.PREF_KEY_APP_PACKAGE_ICON, builtInpkg != null ? builtInpkg.getIcon():"");

		setInfo(Const.PREF_KEY_MUSIC_APP_PACKAGE, MapUtil.getMusicAppPackage());
		PackageMusic builtInMusicpkg = PackageMusic.value(MapUtil.getMusicAppPackage());
		setInfo(Const.PREF_KEY_MUSIC_APP_PACKAGE_ICON, builtInMusicpkg != null ? builtInMusicpkg.getIcon():"");

		setInfo(Const.PREF_KEY_BLUETOOTH, MapUtil.getBluetooth());
		setInfo(Const.PREF_KEY_BLUETOOTHS, MapUtil.getBluetooths());

		Log.i(TAG,"Const.PREF_KEY_START_STOP_ON_OFF : " + Const.PREF_KEY_START_STOP_ON_OFF);
		Log.i(TAG,"Const.PREF_KEY_START_STOP_ON_OFF : " + MapUtil.getStartStopOnOff());

		setInfo(Const.PREF_KEY_START_STOP_ON_OFF, MapUtil.getStartStopOnOff());
		setInfo(Const.PREF_KEY_USING_REG_HOME, MapUtil.isUsingRegHome());

		setInfo(Const.PREF_KEY_WIFI, MapUtil.getWifi());
		setInfo(Const.PREF_KEY_NAVI_DESTINATION, MapUtil.getNaviDestination().getId());
		setInfo(Const.PREF_KEY_NAVI_MODE, MapUtil.getNaviMode().getId());
		setInfo(Const.PREF_KEY_MAP_MODE, MapUtil.getMapMode().getId());
//		setInfo(Const.PREF_KEY_START_MODE, MapUtil.getStartMode().getId()); // 사용안함
		setInfo(Const.PREF_KEY_STOP_MODE, MapUtil.getStopMode().getId());
		setInfo(Const.PREF_KEY_APP_ENABLE, MapUtil.getAppEnable().getId());

		setInfo(Const.PREF_KEY_CHARGING_MODE, MapUtil.getChargingMode().getId());
		setInfo(Const.PREF_KEY_START_POWER, MapUtil.isStartPower());
		setInfo(Const.PREF_KEY_START_BLUETOOTH, MapUtil.isStartBluetooth());
		setInfo(Const.PREF_KEY_START_WIFI, MapUtil.isStartWifi());

		setInfo(Const.PREF_KEY_ONLY_STOP_AFTER_NAVING, MapUtil.isOnlyStopAfterNaving());

		setInfo(Const.PREF_KEY_NOTI_ICON, MapUtil.isNotiIcon());
		setInfo(Const.PREF_KEY_TEST, MapUtil.isTest());
		setInfo(Const.PREF_KEY_ENHANCED_EXECUTION, MapUtil.isEnhancedExecution());

		setInfo(Const.PREF_KEY_NAVI_POPUP_MODE, MapUtil.isOverlayOpen());
		setInfo(Const.PREF_KEY_NAVI_AUTO_START, MapUtil.getNaviAutoStart());

		setInfo(Const.PREF_KEY_GEOFENCE, MapUtil.isGeofence());
		setInfo(Const.PREF_KEY_GEOFENCE_RADIUS_IN_METERS, MapUtil.getGeofenceRadiusInMeters());

		setInfo(Const.PREF_KEY_NAVI_OVERLAY_WAIT_TIME, MapUtil.getOverlayWaitTime());
		setInfo(Const.PREF_KEY_APP_UPDATE_DELAY_TIME, MapUtil.getAppUpdateDelayTime());
		setInfo(Const.PREF_KEY_APP_UPDATE_CHECK_TIME, MapUtil.getAppUpdateCheckTime());

		WorkInfo.State wState = WorkerUtil.getCheckServiceWorkInfoState(mContext);
		String wId = WorkerUtil.getCheckServiceWorkInfoId(mContext);
		setInfo("_CHECK_SERVICE_WORK_STATE", wState.name());
		setInfo("_CHECK_SERVICE_WORK_ID", wId);

		setInfo(Const.PREF_KEY_CHECK_SERVICE, MapUtil.isCheckServiceForUser());
		setInfo(Const.PREF_KEY_CHECK_SERVICE_CHECK_NOTI, MapUtil.getCheckServiceCheckNoti().getId());
		setInfo(Const.PREF_KEY_CHECK_SERVICE_CHECK_LOG_SEND_NOTI, MapUtil.getCheckserviceCheckLogSendNoti().getId());

		setInfo(Const.PREF_KEY_RUN_STATUS_SCREEN_START_MODE, MapUtil.getRunStatusScreenStartMode().getId());
		setInfo(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_START, MapUtil.getRunStatusScreenTimeStart());
		setInfo(Const.PREF_KEY_RUN_STATUS_SCREEN_STOP_MODE, MapUtil.getRunStatusScreenStopMode().getId());
		setInfo(Const.PREF_KEY_RUN_STATUS_SCREEN_TIME_FOR_STOP, MapUtil.getRunStatusScreenTimeStart());

		setInfo(Const.PREF_KEY_RUN_SPEED, MapUtil.isRunSpeed());
		setInfo(Const.PREF_KEY_RUN_SPEED_LIMIT_TIME, MapUtil.getRunSpeedLimitTime());
		setInfo(Const.PREF_KEY_RUN_SPEED_LIMIT, MapUtil.getRunSpeedLimit());

		setInfo(Const.PREF_KEY_RENAVI_PREVENT_TIME, MapUtil.getRenaviPreventTime());
		setInfo(Const.PREF_KEY_NAVI_DESTINATION_EXECUTION_HOLD_TIME, MapUtil.getDestinationExecutionHoldTime());

		setInfo(Const.PREF_KEY_WORK_GOTO_OFF_TIME, MapUtil.isEnableWorkGotoOffTime());
		setInfo(Const.PREF_KEY_WORK_GOTO_TIME_FROM, MapUtil.getWorkGotoTimeFrom());
		setInfo(Const.PREF_KEY_WORK_GOTO_TIME_TO, MapUtil.getWorkGotoTimeTo());
		setInfo(Const.PREF_KEY_WORK_OFF_TIME_FROM, MapUtil.getWorkOffTimeFrom());
		setInfo(Const.PREF_KEY_WORK_OFF_TIME_TO, MapUtil.getWorkOffTimeTo());

		setInfo(Const.PREF_KEY_INDEPENDENCE_CONDITION, MapUtil.isIndependenceCondition());

		setInfo(Const.PREF_KEY_POI_HOME, MapUtil.getPoiHome());
		setInfo(Const.PREF_KEY_POI_COMPANY, MapUtil.getPoiCompany());
		setInfo(Const.PREF_KEY_POIS_FAVORITE, MapUtil.getPoisFavorite());
		setInfo(Const.PREF_KEY_POIS_RECENT, MapUtil.getPoisRecent());
		setInfo(Const.PREF_KEY_POI_RECENT, MapUtil.getPoiRecent());

		HashMap<String, Boolean> permissions = new HashMap<String, Boolean>();
		permissions.put(String.valueOf(PermissionType.LOCATION),PermissionUtil.isLocationPermission(mContext));
		permissions.put(String.valueOf(PermissionType.BACKGROUND_LOCATION),PermissionUtil.isBackgroundLocationPermission(mContext));
		permissions.put(String.valueOf(PermissionType.POST_NOTIFICATIONS),PermissionUtil.isPostNotificationsPermission(mContext));
		permissions.put(String.valueOf(PermissionType.BLUETOOTH),PermissionUtil.isBluetoothPermission(mContext));
		permissions.put(String.valueOf(PermissionType.NOTIFICAION_ACCESS),PermissionUtil.hasNotificationAccessPermission(mContext));
		permissions.put(String.valueOf(PermissionType.WRITE_SETTING),Build.VERSION.SDK_INT >= Build.VERSION_CODES.M?Settings.System.canWrite(mContext):true);
		permissions.put(String.valueOf(PermissionType.ACCESSIBILITY),PermissionUtil.hasAccessibilityPermissions());
		permissions.put(String.valueOf(PermissionType.IGNORE_BATTERY),PermissionUtil.hasIgnoreBatteryPermissions());
		permissions.put(String.valueOf(PermissionType.OVERLAY),Build.VERSION.SDK_INT >= Build.VERSION_CODES.M?Settings.canDrawOverlays(mContext):true);
		permissions.put(String.valueOf(PermissionType.USAGESTATS),PermissionUtil.hasUsageStatsPermission(mContext));

		setInfo("_PERMISSIONS",permissions );
		setInfo( Const.PREF_KEY_APP_INSTALLED,MapUtil.isAppInstalled() );
		setInfo( "_APP_UPDATE_AVAILABLE",WApplication.appUpdateAvailable );
		setInfo( Const.PREF_KEY_APP_UPDATED,MapUtil.isAppUpdated() );
		setInfo( Const.PREF_KEY_APP_UPDATED_NOTI_CONFIRM,MapUtil.isAppUpdatedNotiConfirm() );
		setInfo( Const.PREF_KEY_APP_UPDATED_TIME,MapUtil.getAppUpdatedTime() );
		setInfo("_IS_ACCESSBILITY_SERVICE_RUNNING", PermissionUtil.isAccessibilityServiceRunning(mContext));
		HashMap<String, Object> versions = new HashMap<String, Object>();
		versions.put("sdk",Build.VERSION.SDK_INT);
		try {
			ApplicationInfo applicationInfo = mContext.getApplicationInfo();
			if (applicationInfo != null) {
				versions.put("target",applicationInfo.targetSdkVersion);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
					versions.put("min",applicationInfo.minSdkVersion);
				} else {
					versions.put("min",Build.VERSION.SDK_INT);
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					versions.put("compile",applicationInfo.compileSdkVersion);
				} else {
					versions.put("compile",applicationInfo.targetSdkVersion);
				}
				versions.put("androidId", PackageUtil.getAndroidVersionId());
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		HashMap<String, Object> device = new HashMap<String, Object>();
		try {
			device.put("uuid", DeviceUtil.getUUID(mContext));
			device.put("brand", Build.BRAND);
			device.put("model", Build.MODEL);
			//		device.put("sdkInt", Build.VERSION.RELEASE);
			//       device.put("version", Build.VERSION.RELEASE);
			device.put("sdkInt", android.os.Build.VERSION.SDK_INT);
			device.put("versionId", PackageUtil.getAndroidVersionId());
			device.put("sdkInt", android.os.Build.VERSION.SDK_INT);
			device.put("versionName", BuildConfig.VERSION_NAME);
			device.put("versionCode", BuildConfig.VERSION_CODE);

			Locale locale;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				locale = mContext.getResources().getConfiguration().getLocales().get(0);
			} else {
				locale = mContext.getResources().getConfiguration().locale;
			}
			String displayCountry = locale.getDisplayCountry();
			String country = locale.getCountry();
			String language = locale.getLanguage();
			device.put("displayCountry", displayCountry);
			device.put("country", country);
			device.put("language", language);
			device.put("isTablet", DeviceUtil.isTablet(mContext));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		setInfo("_DEVICE", device);

		setInfo("_APP_RELEASE_UPDTED_TIME", AppUtil.getAppReleaseUpdtedTime() );
		setInfo("_USER_TYPE", WApplication.mInstance.userType.name() );
		setInfo("_USER_LEVEL", WApplication.mInstance.userLevel.name() );
		setInfo("_VERSIONS",versions );
		setInfo("_IS_IN_KOREA",MapUtil.isInKorea(mContext) );

		setInfo(Const.PREF_KEY_LICENSE_EXPIRE_TIME, LicenseUtil.getLicenseExpireTime() );
		setInfo(Const.PREF_KEY_AD_VIEW_CHECK_TIME, AdUtil.getAdViewCheckTime() );
		setInfo("_NEXT_ADVIEW_TIME", AdUtil.getNextAdviewTime() );
		setInfo("_SYSTEM_CURRENT_TIME_MILLIS", System.currentTimeMillis() );

		setInfo("_IS_DEBUG", WUrl.devMode != WUrl.EnumDevMode.RELEASE );
		setInfo("_IS_LICENSE", LicenseUtil.isLicense() );
		setInfo("_IS_PAID_LICENSE", LicenseUtil.isPaidLicense() );
		setInfo("_IS_SUBS_LICENSE", LicenseUtil.isSubsLicense() );
		setInfo("_IS_LIFE_TIME_LICENSE", LicenseUtil.isLifeTimeLicense() );
		setInfo("_IS_BETA_LICENSE", LicenseUtil.isBetaLicense() );

		setInfo(Const.PREF_KEY_PURCHASE_SUBS_ORIGINALJSON, LicenseUtil.getPurchaseSubs() );
		setInfo(Const.PREF_KEY_PURCHASE_LIFETIME_ORIGINALJSON, LicenseUtil.getPurchaseLifeTime() );

		setInfo("_IS_AD_VIEW_LICENSE", LicenseUtil.isAdViewLicense() );
		setInfo("_IS_AD_VIEW_AVAILABLE", AdUtil.isAdViewAvailable() );
		setInfo("_IS_AD_VIEW_AVAILABLE_FOR_AD_MAX_TIME", AdUtil.isAdViewAvailableForAdMaxTime() );
		setInfo("_IS_AD_VIEW_AVAILABLE_FOR_INTERVAL_TIME", AdUtil.isAdViewAvailableForIntervalTime() );

		setInfo("_IS_PAID" , LicenseUtil.isPaid() );
		setInfo("_IS_ADING", AdUtil.isAding() );

		setInfo("_IS_ADING_WAIT_REWARD", AdUtil.isAdingWaitReward() );
		setInfo("_IS_ADING_WAIT_BANNER", AdUtil.isAdingWaitBanner() );
		setInfo("_IS_ADING_POPUP_BANNER", AdUtil.isAdingPopupBanner() );
		setInfo("_IS_ADING_MAIN_BANNER", AdUtil.isAdingMainBanner() );
		setInfo("_IS_GROUP_MASTER", GroupUtil.isGroupMasterUUIDs(WApplication.basicInfoDTO.uuid) );

		Gson gson = new Gson();
		String cloneBasicInfoDTO = gson.toJson(WApplication.basicInfoDTO);
		BasicInfoDTO basicInfoDTO = gson.fromJson(cloneBasicInfoDTO, BasicInfoDTO.class);
		if ( Utils.isNotEmpty(LicenseUtil.getBetaLicensedUUIDs()) ) basicInfoDTO.betaLicensedUUIDs.clear();
		if ( Utils.isNotEmpty(CheckServiceUtil.getCheckServiceLogUUIDs()) ) basicInfoDTO.checkServiceLogUUIDs.clear();
		if ( Utils.isNotEmpty(CheckServiceUtil.getCheckServiceCheckLogSendNotiUUIDs()) ) basicInfoDTO.checkServiceCheckLogSendNotiUUIDs.clear();
		if ( Utils.isNotEmpty(CheckServiceUtil.getCheckServiceCheckNotiUUIDs()) ) basicInfoDTO.checkServiceCheckNotiUUIDs.clear();
		setInfo("_BASIC_INFO", basicInfoDTO);

		Gson gson2 = new Gson();
		String cloneBasicSettings = gson.toJson(WApplication.basicSettings);
		BasicSettings basicSettings = gson.fromJson(cloneBasicSettings, BasicSettings.class);
		if ( Utils.isNotEmpty(GroupUtil.getGroupMasterUUIDs()) ) basicSettings.groupMasterUUIDs.clear();
		setInfo("_BASIC_SETTINGS", basicSettings);

		setInfo(OrgConst.PREF_KEY_ORG, OrgUtil.getOrg());
		setInfo(OrgConst.PREF_KEY_ORG_REG, OrgUtil.getOrgReg());
		setInfo(OrgConst.PREF_KEY_GROUPS, OrgUtil.getGroups());

		setInfo(OrgConst.PREF_KEY_PROFILE_NAME, encdodeForWeb(OrgUtil.getProfileName()) );
		setInfo(OrgConst.PREF_KEY_NICK_NAME, encdodeForWeb(OrgUtil.getNickName()) );

//		(AdUtil.getAdViewCheckTime() + WApplication.basicInfoDTO.adViewIntervalTime) -System.currentTimeMillis()
		return settingsInfo;
	}

	private void api_300_GetInfo(JSONObject json) throws JSONException {

		Gson gson = new Gson();
//		WebBasicInformation data = new WebBasicInformation();

		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		mCallbackFuncName = body.optString("callBackFunc");
//		ArrayList datas = new ArrayList();

		genInfo();

		Log.i(TAG,"gson.toJson(basicInfo) : " + gson.toJson(settingsInfo));
		callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(settingsInfo));
//CaseUtil.
//		Utils.toCamelCase(Const.PREF_KEY_APP_PACKAGE.substring(1).toLowerCase());

//["_", "APP", "_", "PACKAGE"]
		//["ab", " ", "de", " ", "fg"]
//		if( key.equals(Const.PREF_KEY_APP_PACKAGE) ) value = MapUtil.getAppPackage();
//		if( key.equals(Const.PREF_KEY_NAVI_DESTINATION) && "".equals(value) ) value = NaviDestination.HOME.getId(); // 집
//		if( key.equals(Const.PREF_KEY_NAVI_MODE) && "".equals(value) ) value = NaviMode.HOMETOCOMPANY.getId(); // 집<>회사
//		if( key.equals(Const.PREF_KEY_START_POWER) && "".equals(value) ) value = "1"; // 시작
//		if( key.equals(Const.PREF_KEY_NOTI_ICON) ) {
//			value = MapUtil.isNotiIcon()?"1":"0"; // On
//		}
//		if( key.equals(Const.PREF_KEY_NAVI_POPUP_MODE) && "".equals(value) ) value = MapUtil.isPopup()?"1":"0"; // 실행 no >> 변경예정 : 오버레이 닫기:0, 유지:1
//		if( key.equals(Const.PREF_KEY_NAVI_AUTO_START) && "".equals(value) ) value = MapUtil.isNaviAutoStart()?"1":"0"; // isNaviAutoStart
//		if( key.equals(Const.PREF_KEY_GEOFENCE_RADIUS_IN_METERS) && "".equals(value) ) {
//			value = String.valueOf(GeofenceConstants.GEOFENCE_RADIUS_IN_METERS); // 목적지 진입 반경 ( m )
//		}
//		if( key.equals(Const.PREF_KEY_OVERLAY_WAIT_TIME) && "".equals(value) ) {
//			value = String.valueOf(Const.NAVI_OVERLAY_COUNTDOWN_TIME/1000); // 팝업(오버레이) 대기시간 ( 초 )
//		}
//		if( key.equals(Const.PREF_KEY_RENAVI_PREVENT_TIME) && "".equals(value) ) {
//			value = String.valueOf(Const.NAVI_RENAVI_PREVENT_TIME/1000); // 내비 재실행 방지시간
//		}
//
//		if( key.equals(Const.PREF_KEY_GEOFENCE_RADIUS_IN_METERS) && "".equals(value) ) {
//			value = String.valueOf(GeofenceConstants.GEOFENCE_RADIUS_IN_METERS); // 목적지 진입 반경 ( m )
//		}

	}

	/**
	 * 시스템 값 가져오기
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	private void api_307_GetSysInfo(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		String value = WApplication.mInstance.getSharedPreferences(key, "");

		mCallbackFuncName = body.optString("callBackFunc");
		Gson gson = new Gson();

		HashMap<String, Object> v  = new HashMap<String,Object>();
		if( key.equals(Const.SYS_INFO_BLUETOOTH_ENABLE) || !Utils.isEmpty(key)) {
			BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
			Log.d(TAG, "\n어댑터: " + btAdapter);

			String k = Utils.toCamelCase(key.substring(1).toLowerCase());
			if (btAdapter == null) {
				Log.d(TAG, "\n블루투스가 지원되지 않습니다.");
				v.put(k,"NA");

			} else {
				v.put(k,btAdapter.isEnabled());
			}
		}

		String callbackFunc = body.optString("callBackFunc");
		L.d(TAG,"api_307_GetSysInfo", "key / value : " + StringUtils.defaultString(key,"") + " / "  + StringUtils.defaultString(gson.toJson(v),"") );
		callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(v));

	}
	/**
	 * 시스템 값 설정
	 * @param json JSON 데이터
	 * @throws JSONException
	 */
	@SuppressLint("MissingPermission")
	private void api_308_SetSysInfo(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		String value = WApplication.mInstance.getSharedPreferences(key, "");

		mCallbackFuncName = body.optString("callBackFunc");
		Gson gson = new Gson();

		HashMap<String, Object> v  = new HashMap<String,Object>();
		if( key.equals(Const.SYS_INFO_BLUETOOTH_ENABLE) || !Utils.isEmpty(key)) {
			Boolean b = body.getBoolean("value");
//			BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
//
//			if ( b ) btAdapter.enable();
//			else btAdapter.disable();

			DeviceUtil.bluetoothEnable(mContext,b);

			String k = Utils.toCamelCase(key.substring(1).toLowerCase());
			v.put(k,b);
		}

		String callbackFunc = body.optString("callBackFunc");
		L.d(TAG,"api_308_SetSysInfo", "key / value : " + StringUtils.defaultString(key,"") + " / "  + StringUtils.defaultString(gson.toJson(v),"") );
		callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(v));

	}


	/**
	 * Root URL
	 */
	private void api_301_GetUrl(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		mCallbackFuncName = body.optString("callBackFunc");
		L.d(TAG,"api_301_GetUrl", "key : " + StringUtils.defaultString(key,"") );
		callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, WUrl.ReqId.valueOf(key).getReqUrl());
	}

	/**
	 * Utils.hasPermission(mContext)
	 */
	private void api_302_GrantedPermissions(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		mCallbackFuncName = body.optString("callBackFunc");

		Gson gson = new Gson();
		ArrayList datas = new ArrayList();
		if ( key.equals("overlay") || Utils.isEmpty(key) ) {
			HashMap<String,Object> data = new HashMap<String,Object>();
			boolean ok = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? true : false;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(mContext)) ok = true;
			data.put("name", "overlay" );
			data.put("value", ok);
			datas.add(data);
		}
/** 20250403 : 필수 권한 배제 : 위치 */
//		if ( key.equals("location") || Utils.isEmpty(key) ) {
//			HashMap<String,Object> data = new HashMap<String,Object>();
//			data.put("name", "location" );
//			data.put("value", PermissionUtil.isLocationPermission(mContext));
//			datas.add(data);
//		}
//		if ( key.equals("bluetooth") || Utils.isEmpty(key) ) {
//			HashMap<String,Object> data = new HashMap<String,Object>();
//			data.put("name", "bluetooth" );
//			data.put("value", PermissionUtil.isBluetoothPermission(mContext));
//			datas.add(data);
//		}
		if ( key.equals("notification_access") || Utils.isEmpty(key) ) {
			HashMap<String,Object> data = new HashMap<String,Object>();
			data.put("name", "notification_access" );
			data.put("value", PermissionUtil.hasNotificationAccessPermission(mContext));
			datas.add(data);
		}
/** 20250403 : 필수 권한 배제 : 접근성 */
//		if ( key.equals("accessibility") || Utils.isEmpty(key) ) {
//			HashMap<String,Object> data = new HashMap<String,Object>();
//			data.put("name", "accessibility" );
//			data.put("value", PermissionUtil.hasAccessibilityPermissions() );
//			datas.add(data);
//		}
		if ( key.equals("ignore_battery") || Utils.isEmpty(key) ) {
			HashMap<String,Object> data = new HashMap<String,Object>();
			data.put("ignore_battery", PermissionUtil.hasIgnoreBatteryPermissions() );
			data.put("name", "ignore_battery" );
			data.put("value", PermissionUtil.hasIgnoreBatteryPermissions());
			datas.add(data);
		}
		if ( key.equals("post_notifications") || Utils.isEmpty(key) ) {
			HashMap<String,Object> data = new HashMap<String,Object>();
			data.put("post_notifications", PermissionUtil.isPostNotificationsPermission(mContext) );
			data.put("name", "post_notifications" );
			data.put("value", PermissionUtil.isPostNotificationsPermission(mContext));
			datas.add(data);
		}
//		if ( key.equals("write_settings") || Utils.isEmpty(key) ) {
//			HashMap<String,Object> data = new HashMap<String,Object>();
//			boolean ok = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? true : false;
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(mContext)) ok = true;
//			data.put("write_settings", ok );
//			data.put("name", "write_settings" );
//			data.put("value", PermissionUtil.hasIgnoreBatteryPermissions());
//			datas.add(data);
//		}
		L.d(TAG,"api_302_GrantedPermissions", "key / value : " + StringUtils.defaultString(key,"") + " / "  + StringUtils.defaultString(gson.toJson(datas),"") );
		callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(datas));
	}
	private void api_304_gotoStoreAppPacakge(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		mCallbackFuncName = body.optString("callBackFunc");
		Utils.gotoStoreAppPackage(mContext,key);
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
    private void api_303_getInstallPackages(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
//		AppType appType = AppType.value(body.getString("appType"));
		AppType appType = AppType.valueOf(body.getString("appType"));
		String key = body.getString("key");
		Integer category = Integer.parseInt( Utils.isEmpty(body.getString("category"))?"-1":body.getString("category"), 10);
		mCallbackFuncName = body.optString("callBackFunc");

		Gson gson = new Gson();
		ArrayList<Map<String, Object>> datas = new ArrayList();

		PackageManager pm = WApplication.mInstance.getPackageManager();

		if ( !Utils.isEmpty(key) ) {
			HashMap<String, Object> data = new HashMap<String, Object>();
			datas.add(PackageUtil.getAppInfo(key));
		} else {
			int flags = PackageManager.GET_META_DATA |
					PackageManager.GET_SHARED_LIBRARY_FILES |
					PackageManager.GET_UNINSTALLED_PACKAGES
					;
			List<ApplicationInfo> installedApplications = pm.getInstalledApplications(flags);
			    for(int loopCnt=0;loopCnt<=installedApplications.size()-1;loopCnt++) {
					ApplicationInfo appInfo = installedApplications.get(loopCnt);
					int cCategory;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						cCategory = appInfo.category;
					} else {
						cCategory = 0;
					}
				if (
					(
						category == -1 ||
						category == cCategory ||
						(
							category == 6 && MapUtil.isMapCategoryApp(appInfo.packageName)
						)
						||
						(
							category == 1 && MapUtil.isMusicCategoryApp(appInfo.packageName)
						)
					) &&
					pm.getLaunchIntentForPackage(appInfo.packageName) != null
				) {
					HashMap<String, Object> data = new HashMap<String, Object>();

					String name = "";
					try {
						if (appInfo.labelRes != 0) {
							Resources res = pm.getResourcesForApplication(appInfo);
							name = res.getString(appInfo.labelRes);
						} else {
							name = appInfo.loadLabel(pm).toString();
						}
						name = name.replaceAll(System.getProperty("line.separator"), " ");
					} catch (PackageManager.NameNotFoundException e) {
						e.printStackTrace();
					}
					data.put("name", appendAppNameForPackage(appInfo.packageName,name));

					data.put("package", appInfo.packageName);

					data.put("installed", true);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						data.put("category",appInfo.category );
					} else {
						data.put("category",0 );
					}

					if ( AppType.NAVI.equals(appType) ) {
						Package btlPkg = Package.value(appInfo.packageName);
						if ( btlPkg != null ) {
							data.put("seq", btlPkg.getSeq());
							data.put("seq2", btlPkg.getSeq());
							data.put("category", 6 );
						} else {
							if ( cCategory == 6 ) {
								data.put("seq", 20);
							} else {
								data.put("seq", 999999999);
							}
						}
						PackageMusic btlMusicPkg = PackageMusic.value(appInfo.packageName);
						if (btlMusicPkg != null) {
							data.put("category", 1 );
							data.put("seq", btlMusicPkg.getSeq());
							data.put("seq2", btlMusicPkg.getSeq());
						}
					}

					if ( AppType.MUSIC.equals(appType) ) {
						PackageMusic btlMusicPkg = PackageMusic.value(appInfo.packageName);
						if (btlMusicPkg != null) {
							data.put("seq", btlMusicPkg.getSeq());
							data.put("seq2", btlMusicPkg.getSeq());
							data.put("category", 1 );
						} else {
							if ( cCategory == 1 ) {
								data.put("seq", 20);
							} else {
								data.put("seq", 999999999);
							}
						}
						Package btlPkg = Package.value(appInfo.packageName);
						if ( btlPkg != null ) {
							data.put("category", 6 );
							data.put("seq", btlPkg.getSeq());
							data.put("seq2", btlPkg.getSeq());
						}
					}

					Map<String, Object> detail  = new HashMap<String, Object>() {{
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							put("category",appInfo.category );
						} else {
							put("category",0 );
						}
					}};
					datas.add(data);
				}
			}
			if ( appType == null || AppType.NAVI.equals(appType) ) {
				for (Package pkg : Package.values()) {
					if (!Utils.hasKeyMapArrayList(datas, "package", pkg.getId())) {
						HashMap<String, Object> data = new HashMap<String, Object>();
						if (pkg.getSeq() > 0) {
							data.put("name", pkg.getName());
							data.put("package", pkg.getId());
							data.put("installed", false);
							data.put("category", 1);
							data.put("seq", pkg.getSeq());
							data.put("seq2", pkg.getSeq());
							data.put("icon", pkg.getIcon());
							datas.add(data);
						}
					}
				}
			}

			if ( appType == null || AppType.MUSIC.equals(appType) ) {
				for (PackageMusic pkg : PackageMusic.values()) {
					if (!Utils.hasKeyMapArrayList(datas, "package", pkg.getId())) {
						HashMap<String, Object> data = new HashMap<String, Object>();
						data.put("name", pkg.getName());
						data.put("package", pkg.getId());
						data.put("installed", false);
						data.put("category", 6);
						data.put("seq", pkg.getSeq());
						data.put("seq2", pkg.getSeq());
						data.put("icon", pkg.getIcon());
						datas.add(data);
					}
				}
			}
		}
		L.d(TAG,"api_303_getInstallPackages", StringUtils.defaultString(gson.toJson(datas),"") );
		callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(datas));
	}



	@SuppressLint("MissingPermission")
	private void api_305_getBluetooths(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		mCallbackFuncName = body.optString("callBackFunc");

//		if (Utils.isEmpty(key)) {
//			Utils.toast(mContext, R.string.loading_list);
//		}

		Gson gson = new Gson();
		ArrayList datas = new ArrayList();

		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		Log.d(TAG, "\n어댑터: " + btAdapter);

		if (btAdapter == null) {
			Log.d(TAG, "\n블루투스가 지원되지 않습니다.");
			return;
		} else {
			if (btAdapter.isEnabled()) {
				Log.d(TAG, "\n블루투스 사용 가능...");
				Log.d(TAG, "\n현재 페어링된 기기:");

			} else {
//                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		if (!Utils.isEmpty(key)) {
			HashMap<String, Object> data = new HashMap<String, Object>();
			if (PermissionUtil.isBluetoothPermission(mContext)) {
				Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
				for (BluetoothDevice device: devices) {
					if( key.equals(device.getAddress())) {
						Log.d(TAG,"\n 디바이스: " + device.getName() + ", " + device);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
							device.getAlias();
						}
						datas.add(device);
//						device.getType();
////				    device.getUuids();
//						device.getName();
//						device.getBondState();
//						device.getAddress();
					}
				}

				if( datas.size() == 0 ) {
					String xname = "";
					data.put("address", key);
					data.put("type", "");
					data.put("name", "");
					data.put("bondState", "");
					datas.add(data);
				}
			}
		} else {
			if (PermissionUtil.isBluetoothPermission(mContext)) {
				Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
				for (BluetoothDevice device: devices) {
					HashMap<String, Object> data = new HashMap<String, Object>();
					Log.d(TAG,"\n 디바이스: " + device.getName() + ", " + device);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
						device.getAlias();
					}
					data.put("address", device.getAddress());
					data.put("type", device.getType());
					data.put("name", device.getName());
					data.put("bondState", device.getBondState());

					datas.add(data);
//						device.getType();
////				    device.getUuids();
//						device.getName();
//						device.getBondState();
//						device.getAddress();
				}
			}

		}
		L.d(TAG,"api_305_getBluetooths", StringUtils.defaultString(gson.toJson(datas),"") );
		callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(datas));
	}

	@SuppressLint("MissingPermission")
	private void api_315_enableBluetooth(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		mCallbackFuncName = body.optString("callBackFunc");
		if (!DeviceUtil.isBluetoothEnable(mContext)) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
				DeviceUtil.bluetoothEnable(mContext, true);
				callJavascriptFunc(mCallbackFuncName, "ok");
			} else {
				MapUtil.execCommandActivity(mContext, DummyActivity.COMMAND_BLUETOOTH_ENABLE_FOR_MAINACTIVITY);
			}
		} else {
			callJavascriptFunc(mCallbackFuncName, "ok");
		}
		L.d(TAG,"api_315_enableBluetooth", "ok" );
	}

	BroadcastReceiver wifiScanReceiver = null;

	@SuppressLint("MissingPermission")
	private void api_306_getWifis(JSONObject json) throws JSONException {
		JSONObject body = json.getJSONObject("body");
		String key = body.getString("key");
		mCallbackFuncName = body.optString("callBackFunc");

		if ( Utils.isEmpty(key) ) {
			Utils.toast(mContext,R.string.loading_list);
		}
		Gson gson = new Gson();


		WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
		if ( wifiScanReceiver != null ) {
			mContext.unregisterReceiver(wifiScanReceiver);
		}

		String bssid = MapUtil.getCurrentBssid(mContext);
		String ssid = MapUtil.getCurrentSsid(mContext);

		wifiScanReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent intent) {
				ArrayList datas = new ArrayList();
				boolean success = intent.getBooleanExtra(
						WifiManager.EXTRA_RESULTS_UPDATED, false);
				if (success) {
					// 스캔 성공시 저장된 list를 recycler view를 통해 보여줌
					List<ScanResult> results = wifiManager.getScanResults();
					HashMap<String, String> resultKeys = new HashMap<String, String>();

					Log.d("wifi", "scan success");

					mWebView.evaluateJavascript("", null);

					StringBuffer st = new StringBuffer();
					for(ScanResult wifi : results ){
						if ( wifi.level >= -70 ) {
							HashMap<String, Object> data = new HashMap<String, Object>();
							if (!resultKeys.containsKey(wifi.SSID)) {
								Log.d(TAG, "\n WIFI: " + wifi.SSID + ", " + wifi);
								resultKeys.put(wifi.SSID, wifi.BSSID);
								data.put("ssid", wifi.SSID);
								data.put("bssid", wifi.BSSID);
								data.put("connected", StringUtils.defaultString(ssid).equals(wifi.SSID));
								datas.add(data);
							}
						}
					}
					callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(datas));

				} else {
					// scan failure handling
					// handle failure: new scan did NOT succeed
					Log.d("wifi", "scanFailure");
					Utils.toast(mContext,"wifi scan에 실패하였습니다.");
					// consider using old scan results: these are the OLD results!
					List<ScanResult> results = wifiManager.getScanResults();
					HashMap<String, String> resultKeys = new HashMap<String, String>();
					// potentially use older scan results ...
					StringBuffer st = new StringBuffer();
					for(ScanResult wifi : results ){
						if ( wifi.level >= -70 ) {
							HashMap<String, Object> data = new HashMap<String, Object>();
							if (!resultKeys.containsKey(wifi.SSID)) {
								Log.d(TAG, "\n WIFI: " + wifi.SSID + ", " + wifi);
								resultKeys.put(wifi.SSID, wifi.BSSID);
								data.put("ssid", wifi.SSID);
								data.put("bssid", wifi.BSSID);
								data.put("connected", StringUtils.defaultString(ssid).equals(wifi.SSID));
								datas.add(data);
							}
						}
					}
					callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(datas));

				}
			}
		};

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mContext.registerReceiver(wifiScanReceiver, intentFilter);

		boolean success = wifiManager.startScan();
		ArrayList datas = new ArrayList();
		if (!success) {
			// scan failure handling
			// handle failure: new scan did NOT succeed
			Log.d("wifi", "scanFailure");
			Utils.toast(mContext,"wifi scan에 실패하였습니다.");
			// consider using old scan results: these are the OLD results!
			List<ScanResult> results = wifiManager.getScanResults();
			HashMap<String, String> resultKeys = new HashMap<String, String>();
			// potentially use older scan results ...
			StringBuffer st = new StringBuffer();
			for(ScanResult wifi : results ){
				if ( wifi.level >= -70 ) {
					HashMap<String, Object> data = new HashMap<String, Object>();
					if (!resultKeys.containsKey(wifi.SSID)) {
						Log.d(TAG, "\n WIFI: " + wifi.SSID + ", " + wifi);
						resultKeys.put(wifi.SSID, wifi.BSSID);
						data.put("ssid", wifi.SSID);
						data.put("bssid", wifi.BSSID);
						data.put("connected", StringUtils.defaultString(ssid).equals(wifi.SSID));
						datas.add(data);
					}
				}
			}
			callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(datas));
		} else {
			callJavascriptFunc(JavaScriptBridge.mCallbackFuncName, gson.toJson(datas));
		}
		L.d(TAG,"api_306_getWifis", StringUtils.defaultString(gson.toJson(datas),"") );
	}



	@JavascriptInterface
	public String getBasicInformation() {
		Gson gson = new Gson();
		WebBasicInformation data = new WebBasicInformation();
		L.d(TAG,"getBasicInformation", StringUtils.defaultString(gson.toJson(data),"") );
		return gson.toJson(data);
	}

	/** Show a toast from the web page */
	@JavascriptInterface
	public void showToast(String toast) {
		L.d(TAG,"showToast", StringUtils.defaultString(toast,"") );
		Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
	}

	@JavascriptInterface
	public void showLoading() {
		L.d(TAG,"showLoading", "" );
		((BasicActivity)mContext).stopProgress();
		((BasicActivity)mContext).showProgress();
//		if (!((BaseActivity)mContext).mProgress.isShowing()) {
//			((BaseActivity)mContext).runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					((BaseActivity)mContext).mProgress.show();
//				}
//			});
//		}
	}

	@JavascriptInterface
	public void hideLoading() {
		L.d(TAG,"stopProgress", "" );
		((BasicActivity)mContext).stopProgress();
//		if (((BaseActivity)mContext).mProgress.isShowing()) {
//			((BaseActivity)mContext).runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
////					mContext.mProgress.dismiss();
//					((BaseActivity)mContext).stopProgress();
//					((BaseActivity)mContext).stopProgressBar();
//				}
//			});
//		}
	}

	//		@JavascriptInterface
//		public void openOZ(String ozr,String params) {
//		    Toast.makeText (mContext, "ozr : " + ozr, Toast.LENGTH_SHORT).show();
//		    Log.d("openOZ", ozr);
//			//WUtil.openOZ(mCoXntext,ozr);
//			WUtil.openOZ(MainActivity.this,ozr,params);
//		}
	@JavascriptInterface
	public void hideKeyboard() {
		L.d(TAG,"hideKeyboard", "" );
		InputMethodManager im = (InputMethodManager)mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
		im.hideSoftInputFromWindow(((BaseActivity)mContext).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@JavascriptInterface
	public void setKeyboard(final boolean b) {
		L.d(TAG,"setKeyboard", "b : " + String.valueOf(b) );
		((BaseActivity)mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
//	            	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
//	        				);
//	                ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE))
//	                .toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);

				// Check if no view has focus:
//	                View view = activity.getCurrentFocus();
//	                if (view != null) {
//	                    InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
//	                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//	                }
				if ( b == false ) {

					((RelativeLayout)mWebView.getParent()).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

					//(ViewGroup)mWebview.getParent().setDescendantFocusability
					mWebView.setFocusable(false);
					mWebView.setFocusableInTouchMode(true);
				} else {
					((RelativeLayout)mWebView.getParent()).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
					mWebView.setFocusable(true);
					mWebView.setFocusableInTouchMode(true);
				}

			}
		});
	}

	private class WebBasicInformation{
		private String uuid;

//		private String macAddress;
//
//		public String getMacAddress() {
//			return macAddress;
//		}
//
//		public void setMacAddress(String macAddress) {
//			this.macAddress = macAddress;
//		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
	}


	@JavascriptInterface
	public void downloadBase64File(String base64Data, String fileName) {
		byte[] fileData = Base64.decode(base64Data, Base64.DEFAULT);
//		String fileNam2e2 = URLUtil.guessUrl(fileName);
		String fileNameOnly = fileName.substring(0,fileName.lastIndexOf("."));
		String fileExtOnly = fileName.substring(fileName.lastIndexOf(".")+1);
		String wFileName = fileNameOnly.replaceAll("\\W+", "") + "." + fileExtOnly;
//		File file = new File(mContext.getExternalFilesDir(null), fileName);
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), wFileName);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(fileData);
			fos.flush();
//			Toast.makeText(mContext, "Download complete: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if( file.exists() ) {
			shareExcelFile(file.getAbsolutePath());
		}
	}

	private void shareExcelFile(String filePath) {
		File file = new File(filePath);
		Uri fileUri;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			fileUri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
		} else {
			fileUri = Uri.fromFile(file);
		}

		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("application/vnd.ms-excel"); // 또는 "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
		shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
		shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		((MainActivity)mContext).startActivity(Intent.createChooser(shareIntent, "엑셀 파일 공유"));
	}
}
