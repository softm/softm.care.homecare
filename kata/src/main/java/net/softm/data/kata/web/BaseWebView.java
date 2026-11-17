package net.softm.data.kata.web;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.core.content.ContextCompat;

import net.softm.lib.common.WLog;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.WUrl;

import java.util.UUID;


/**
 * WebView Custom
 */
@SuppressLint("NewApi")
public class BaseWebView extends WebView {

	private final String TAG = BaseWebView.class.getName();

    public static String mUserAgentDefault;
    public static String mUserAgent = "";

	public Context mContext;

	public BaseWebView(Context context) {
		super(context);
		if(!isInEditMode())
            init(context);
	}

	public BaseWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(!isInEditMode())
            init(context);
	}

	public BaseWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if(!isInEditMode())
            init(context);
	}

    public void init(Context context) {
        mContext = context;
        setDefaultWebSettings();
    }
	/**
	 * get Device ID
	 */
	public static String getDeviceID(Context context){

		if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED){
			return "";
		}

		String deviceID;
		final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final String tmDevice = "" + tm.getDeviceId();
		final String tmSerial = "" + tm.getSimSerialNumber();
		final String androidId = ""+ android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

		UUID uuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32)|tmSerial.hashCode());
		deviceID = uuid.toString();
		return deviceID;
	}
	/**
	 * 앱에서 사용되는 User-Agent 값을 생성한다. <br>
	 * baseUserAgent 값이 있는 경우 baseUserAgent에 앱전용 User-Agent값이 append된다.
	 * @return User-Agent 문자열
	 */
	public String makeUserAgentString(Context context, String string) {
		StringBuilder sb = new StringBuilder();
		if (!TextUtils.isEmpty(string)) {
			sb.append(string);
		}
		WApplication app = (WApplication) context.getApplicationContext();

//		String phoneNumber = "unknown";
//		if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
//			phoneNumber = app.mTelephonyManager.getLine1Number();
//		}
		sb.append(getSettings().getUserAgentString());
		String versionName = app.mPackageInfo.versionName;
		sb.append(" ").append("COMPANY(").append("softm").append(")");
		sb.append(" ").append("REQUEST_SVC_CD(").append("10").append(")");
		sb.append(" ").append("DEVICE_APP_VER(").append(versionName).append(")");
//TODO SOFTM complete UUID 헤더키값
//		sb.append(";").append("DEVICE_SRL_NO=").append(getDeviceID(context));
		sb.append(" ").append("DEVICE_OS(").append("ANDROID").append(")");
		sb.append(" ").append("OS_VERSION(").append(Build.VERSION.RELEASE).append(")");
//		sb.append(";").append("DEVICE_TEL_NO=").append((phoneNumber == null )? "unknown" : phoneNumber.replace("-", "").replace("+82", "0"));
//        sb.append(";").append("DEVICE_TEL_NO=").append("unknown");
		sb.append(" ").append("DEVICE_MODEL(").append(Build.MODEL).append(")");
		sb.append(" ").append("DEVICE_TEL_CORP(").append((app.mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT)? "unknown" : app.mTelephonyManager.getNetworkOperatorName()).append(")");
//        sb.append(";").append("DEVICE_TEL_CORP=").append("unknown");
		sb.append(" ").append("EASY_PROC(").append(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_IS_SIMPLE_LOGIN, "N")).append(")");

//		sb.append(";").append("COMPANY=").append("softm");
//		sb.append(";").append("REQUEST_SVC_CD=").append("10");
//		sb.append(";").append("DEVICE_APP_VER=").append(versionName);
////TODO SOFTM complete UUID 헤더키값
////		sb.append(";").append("DEVICE_SRL_NO=").append(getDeviceID(context));
//		sb.append(";").append("DEVICE_OS=").append("ANDROID");
//		sb.append(";").append("OS_VERSION=").append(Build.VERSION.RELEASE);
////		sb.append(";").append("DEVICE_TEL_NO=").append((phoneNumber == null )? "unknown" : phoneNumber.replace("-", "").replace("+82", "0"));
////        sb.append(";").append("DEVICE_TEL_NO=").append("unknown");
//		sb.append(";").append("DEVICE_MODEL=").append(Build.MODEL);
//		sb.append(";").append("DEVICE_TEL_CORP=").append((app.mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_ABSENT)? "unknown" : app.mTelephonyManager.getNetworkOperatorName());
////        sb.append(";").append("DEVICE_TEL_CORP=").append("unknown");
//		sb.append(";").append("EASY_PROC=").append(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_IS_SIMPLE_LOGIN, "N"));
		WLog.d(TAG, "reUserAgent : "+ sb);

		mUserAgent = sb.toString();
		return sb.toString();
	}
	public void setDefaultWebSettings() {
		WebSettings settings = getSettings();

        mUserAgentDefault = settings.getUserAgentString();

		settings.setJavaScriptEnabled(true);
		settings.setBuiltInZoomControls(true);
		settings.setSupportZoom(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setUserAgentString(makeUserAgentString(mContext, settings.getUserAgentString()));
//		settings.setUserAgentString(TransKeyUtil.makeUserAgentString(mContext, settings.getUserAgentString()));
        settings.setSaveFormData(false);
		settings.setMediaPlaybackRequiresUserGesture(false);
        mUserAgent = settings.getUserAgentString();

		if (Build.VERSION.SDK_INT >= 11) {
			settings.setDisplayZoomControls(false);
		}

		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);
		settings.setSavePassword(false);
		settings.setLoadsImagesAutomatically(true);

		if ((Build.VERSION.SDK_INT > 7) && (Build.VERSION.SDK_INT < 18)) {
			settings.setPluginState(WebSettings.PluginState.ON);
		}

		settings.setAllowFileAccess(true);
		settings.setGeolocationEnabled(true);
		settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
		settings.setSupportMultipleWindows(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);

		//WebView inside Browser doesn't want initial focus to be set.
		settings.setNeedInitialFocus(false);

		// enable smooth transition for better performance during panning or
		// zooming
		settings.setEnableSmoothTransition(true);

		//disable content url access
		settings.setAllowContentAccess(false);

		//HTML5 API flags
		settings.setDomStorageEnabled(true);
		settings.setDatabaseEnabled(true);
//		settings.setAppCacheEnabled(true);
		settings.setPluginState(WebSettings.PluginState.ON);
		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			settings.setTextZoom(100);
		}

		// HTML5 configuration parametersettings.
//		settings.setAppCachePath(mContext.getDir("appcache", 0).getPath());
		settings.setGeolocationDatabasePath(mContext.getDir("geolocation", 0).getPath());
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			settings.setDatabasePath("/data/data/" + mContext.getPackageName() + "/databases/");
		}

		// origin policy for file access
		settings.setAllowUniversalAccessFromFileURLs(false);
		settings.setAllowFileAccessFromFileURLs(false);

		//쿠키 동기화
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);

        if (Build.VERSION.SDK_INT >= 19) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        setHorizontalScrollBarEnabled(true);
        setHorizontalScrollbarOverlay(true);
        setVerticalScrollBarEnabled(true);
        setVerticalScrollbarOverlay(true);
        setScrollbarFadingEnabled(true);

//TODO SOFTM complete javascript debug
		if( WUrl.devMode != WUrl.EnumDevMode.RELEASE ) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				WebView.setWebContentsDebuggingEnabled(true);
			}
		}
	}
}