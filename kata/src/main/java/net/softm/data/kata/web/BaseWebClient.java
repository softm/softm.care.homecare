package net.softm.data.kata.web;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;
import androidx.webkit.WebViewAssetLoader;

import net.softm.data.kata.BuildConfig;
import net.softm.data.kata.MainActivity;
import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.AdType;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.RestUtil;
import net.softm.data.kata.util.Utils;
import net.softm.lib.Constant;
import net.softm.lib.common.WLog;

import java.util.Locale;


/**
 * WebClient Custom
 */


public class BaseWebClient extends WebViewClient {

	private String TAG = "BaseWebClient";
	private Context mContext;
//    WebViewAssetLoader assetLoader = null;
	public BaseWebClient(Context context) {
		mContext = context;

	}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      WebResourceRequest request) {
        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(mContext))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(mContext))
                .build();
        return assetLoader.shouldInterceptRequest(request.getUrl());
    }
		
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {

        view.setVisibility(View.INVISIBLE);
		super.onPageStarted(view, url, favicon);
        WLog.d(TAG, "onPageStarted : " + url);
        if(mContext instanceof MainActivity) {
            MainActivity activity = (MainActivity)mContext;
            activity.startProgressBar();
/*TODO softm : asset/index.html이용하므로, 네트웍 연결 안될때 로직 제거
            //TODO SOFTM 상태체크
 */
            if ( url.indexOf("start.html") == -1 && url.indexOf("index.html") == -1 && url.indexOf("permission.html") == -1

                    && url.indexOf("setting.html") == -1
                    && url.indexOf("onoff.html") == -1
                    && url.indexOf("membership.html") == -1
                    && url.indexOf("search.html") == -1
                    && url.indexOf("reg_poi.html") == -1
                    && url.indexOf("membership.html") == -1
                    && url.indexOf("qna.html") == -1
                    && url.indexOf("error.html") == -1
                    && url.indexOf("run_option.html") == -1
                    && url.indexOf("home_company.html") == -1
                    && url.indexOf("laboratory.html") == -1
                    && url.indexOf("developer_support.html") == -1
                    && url.indexOf("installed.html") == -1
                    && url.indexOf("pray.html") == -1
                    && url.indexOf("profile.html") == -1
            ) {
                activity.iHTTPStatus  = Constant.HTTP_CODE_SUCCESS;
                if ( WApplication.basicInfoDTO.useServerStateCheck ) Utils.startAppByServerState(mContext,url,view);
                else view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.VISIBLE);
            }
//            if ( url.indexOf(WUrl.ReqId.WEB_MAIN.getUrl()) == -1 ) {
                RestUtil.getBasicInfo(mContext,()->{
                    if ( AdUtil.isAdingMainBanner() ) {
                        ((Activity) mContext).runOnUiThread(() -> {
                            AdUtil.addBanner(((Activity) mContext).findViewById(R.id.ad_view_container), mContext, AdType.BANNER);
                        });
                    }
                });
//            } else {
//                ViewGroup adViewContainer = ((Activity) mContext).findViewById(R.id.ad_view_container);
//                adViewContainer.removeAllViews();
//            }
                view.setVisibility(View.VISIBLE);
        }else if(mContext instanceof WebPopActivity){
            WebPopActivity activity = (WebPopActivity)mContext;
            activity.startProgressBar();
/*TODO softm : asset/index.html이용하므로, 네트웍 연결 안될때 로직 제거
            //TODO SOFTM 상태체크
 */
            if ( url.indexOf("error.html") == -1 ) {
                activity.iHTTPStatus  = Constant.HTTP_CODE_SUCCESS;
//                Utils.startAppByServerState(mContext,url,view);
            } else {
                view.setVisibility(View.VISIBLE);
            }
                view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        WLog.d(TAG, "onReceivedError : " + errorCode);
        WLog.d(TAG, "onReceivedError : " + description);
        WLog.d(TAG, "onReceivedError : " + failingUrl);
        if(mContext instanceof MainActivity) {
            ((MainActivity)mContext).stopProgress();
    //TODO SOFTM complete 네트웍 에러
    //TODO SOFTM Network Error 처리 안함. 주석.
            ((MainActivity)mContext).showNetworkError(errorCode,description);
        }else if(mContext instanceof WebPopActivity){
            ((WebPopActivity)mContext).stopProgress();
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        L.d(TAG, "onPageFinished",  url);
        String pathName="";
        if (url != null) {
            Uri uri = Uri.parse(url);
            pathName = uri.getLastPathSegment();
        }
        if(mContext instanceof MainActivity) {
            Fa.logScreenName(mContext, pathName,"MainActivity");
            MainActivity activity = (MainActivity)mContext;
            activity.stopProgress();
//            WLog.ee(TAG, "onPageFinished, cookie=" + CookieManager.getInstance().getCookie(WUrl.mBaseUrl) + ", URL : " + WUrl.mBaseUrl);
            if ( activity.iHTTPStatus == Constant.HTTP_CODE_SUCCESS ) {
                view.setVisibility(View.VISIBLE);
            }
        }else if(mContext instanceof WebPopActivity){
            Fa.logScreenName(mContext, pathName,"WebPopActivity");
            WebPopActivity activity = (WebPopActivity)mContext;
            activity.stopProgress();
            if(activity.mCallBackFunc != null && !"".equals(activity.mCallBackFunc)){
                JavaScriptBridge.callJavascriptFunc(activity.mCallBackFunc);
            }
            if(url.contains("CO110000")) {
                activity.mBaseWebView.invalidate();
            }
            if ( activity.iHTTPStatus == Constant.HTTP_CODE_SUCCESS ) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        WLog.d(TAG, "onReceivedSslError()");

        if(BuildConfig.DEBUG)
            handler.proceed();
        else {

            switch (error.getPrimaryError()) {
                case SslError.SSL_UNTRUSTED:
                case SslError.SSL_EXPIRED:
                case SslError.SSL_IDMISMATCH:
                case SslError.SSL_NOTYETVALID:
                    String error_code = "";
                    if (error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
                        error_code = "(SSL_UNTRUSTED)";
                    } else if (error.getPrimaryError() == SslError.SSL_EXPIRED) {
                        error_code = "(SSL_EXPIRED)";
                    } else if (error.getPrimaryError() == SslError.SSL_IDMISMATCH) {
                        error_code = "(SSL_IDMISMATCH)";
                    } else if (error.getPrimaryError() == SslError.SSL_NOTYETVALID) {
                        error_code = "(SSL_NOTYETVALID)";
                    } else {
                        error_code = "(SSL_UNKNOWN)";
                    }

                    ((BasicActivity)mContext).alert(mContext, mContext.getString(R.string.ssl_cert_untrusted_msg) + error_code, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mContext instanceof MainActivity) {
                                MainActivity activity = (MainActivity) mContext;
                                activity.finish();
                                android.os.Process.killProcess(android.os.Process.myPid());
                            } else if (mContext instanceof WebPopActivity) {
                                WebPopActivity activity = (WebPopActivity) mContext;
                                activity.finish();
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        }
                    });
                    break;

                case SslError.SSL_INVALID:

                    if (Build.VERSION.SDK_INT >= 24) { // 안드로이드 OS 7.0(Nougat) 이상일 때
                        ((BasicActivity)mContext).alert(mContext, R.string.ssl_invalid_update_01, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mContext instanceof MainActivity) {
                                    MainActivity activity = (MainActivity) mContext;
                                    activity.finish();
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                } else if (mContext instanceof WebPopActivity) {
                                    WebPopActivity activity = (WebPopActivity) mContext;
                                    activity.finish();
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            }
                        });
                    } else {
                        ((BasicActivity)mContext).alert(mContext, R.string.ssl_invalid_update_02, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mContext instanceof MainActivity) {
                                    MainActivity activity = (MainActivity) mContext;
                                    activity.finish();
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                } else if (mContext instanceof WebPopActivity) {
                                    WebPopActivity activity = (WebPopActivity) mContext;
                                    activity.finish();
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                }
                            }
                        });
                    }
                    break;
            }
        }
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        WLog.d(TAG, "onReceivedHttpError");
    }

    /**
     * host 검증
     * @param view
     */
    private boolean checkValidHost(String url,WebView view) {
//        MainActivity activity = (MainActivity)mContext;
        Activity activity = (Activity)mContext;
        if(url.startsWith(Const.SERVER_HOST) ) {
            return false;
        } else if( url.startsWith("https:") ) {
            return false;
        } else if (url.startsWith("file:")) {
            return false;
        } else if (url.startsWith("mailto:")) {
            Intent mail = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            activity.startActivity(mail);
            return true;
        } else if (url.startsWith("tel:")) {
            Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            activity.startActivity(tel);
            return true;
        }
        else if (url.startsWith("sms:")) {
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            activity.startActivity(i);
            return true;
        } else if (url.startsWith("intent:")) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                Intent existPackage = activity.getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                if (existPackage != null) {
                    activity.startActivity(intent);
                } else {
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                    marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                    activity.startActivity(marketIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return true;
            }
        } else {
            WLog.d(TAG, "not allow url");
//            view.loadUrl(Const.SERVER_HOST + "/t");
            Locale locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = mContext.getResources().getConfiguration().getLocales().get(0);
            }else{
                locale = mContext.getResources().getConfiguration().locale;
            }
            String displayCountry = locale.getDisplayCountry();
            String country = locale.getCountry();
            String language = locale.getLanguage();
            view.loadUrl(WUrl.ReqId.WEB_MAIN.getReqUrl()+"?lng="+language);
            return true;
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        return checkValidHost(url,view) ;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return checkValidHost(url,view) ;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        // show progress bar
//			mProgress.show();
//        WLog.d(TAG, "onLoadResource : " + url);
    }
}