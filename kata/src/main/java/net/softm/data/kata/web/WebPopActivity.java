package net.softm.data.kata.web;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.AdType;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.job.WorkExecutor;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.PermissionUtil;
import net.softm.data.kata.util.RestUtil;
import net.softm.data.kata.util.Utils;
import net.softm.lib.common.Connectivity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;


// softm
//import kr.co.md119ac.m.web.JavaScriptBridge;
// ---- ---- ---- ---- ---- softm

public class WebPopActivity extends BasicActivity {
    final String TAG = "WebPopActivity";
    public BaseWebChromeClient mChromeClient;
    public BaseWebView mBaseWebView;

    public String mCallBackFunc;

    private long mLastTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_pop);
        setClientForWebView();
        initView();
        OverlayViewUtil.stopService();

        // Call before consuming insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            v.setBackgroundColor(getResources().getColor(R.color.mainColor));
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(getResources().getColor(R.color.mainColor));
            return insets;
        });

        RestUtil.getBasicSettings(this,()->{
        },false);
        RestUtil.getBasicInfo(this,()->{
            if ( AdUtil.isAdingPopupBanner() ) {
                runOnUiThread(() -> {
                    if( url.indexOf("youtube.com") > -1 ) {
                        ViewGroup.LayoutParams layoutParams = findViewById(R.id.ad_view_container).getLayoutParams();
//            int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,24,getResources().getDisplayMetrics());
//            layoutParams.width = width;
//            layoutParams.height = 100;
//            findViewById(R.id.ad_view_container).setLayoutParams(layoutParams);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                    AdUtil.addBanner(findViewById(R.id.ad_view_container), WebPopActivity.this, AdType.BANNER);
                });
            }
        },false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // softm
//        JavaScriptBridge.callJavascript("fn_SessionReload()");
        // ---- ---- ---- ---- ---- softm
        if ( Utils.hasPermissionForBasic(this) && MapUtil.isOverlayOpen() ) {
            if ( !WorkExecutor.isExecuting ) {
                // TODO SOFTM : 중복실행 : StartNavi 실행로직과 중첩되어 실행이 되지 않는 상황이 발생함.
                /** 최근앱 히스토리 삭제와 함께 동작할 경우 오류발생함
                 * onDestroy 로직에 아래 로직 처리시 오류 발생함
                 if ( Utils.hasPermission(this) ) {
                 finishAndRemoveTask();
                 }
                 * 20250117 : 최근앱 히스토리 문제
                 * 그런 이유로 Application레벨의 글로벌 타이머를 이용하여 오류 발생은 해결하였으나
                 * 로직은 타이머를 통해 오버레이를 화면에 표현하는 것으로 유지함
                 * Asis : OverlayViewUtil.init(WApplication.mInstance).openOverlayViewShowOnly();
                 * Tobe : WApplication.mInstance.overlayViewServiceCallbackkt.run((params) -> {
                 */
    //                                WApplication.mInstance.overlayViewServiceCallbackkt.stop();
                WApplication.mInstance.overlayViewServiceCallbackkt.run((params) -> {
                    Log.i(TAG, "AU-Success-OverlayViewUtil.init(MainActivity.this).openOverlayViewShowOnly()");
                    OverlayViewUtil.init(WApplication.mInstance).openOverlayViewShowOnly();
                    return null;
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        Date date = new Date();


//        if (mWebview.canGoBack()) {
//            mWebview.goBack();
//        } else {
        if ( Connectivity.isConnected(this) && RestUtil.isServerConnected() && mWebview.getUrl().startsWith(Const.SERVER_HOST) ) {
            JavaScriptBridge.callJavascriptFunc("fn_BackKeyPressed");
        } else {
            finish();
        }
//        }

//        if (date.getTime() - mLastTime > 2000) {
//            mLastTime = date.getTime();
//            Toast.makeText(this, getString(R.string.common_back_close), Toast.LENGTH_SHORT).show();
//        } else {

//            Intent intent = new Intent();
//            intent.putExtra(Const.INTENT_APP_FINISH, true);
//            setResult(RESULT_OK, intent);
////            setResult(RESULT_CANCELED, intent);
//            finish();
//        }
    }
    String url = "";
    @SuppressLint("WrongViewCast")
    private void initView(){
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        mCallBackFunc = intent.getStringExtra("callBackFunc");
        mBaseWebView = (BaseWebView) findViewById(R.id.webview);
//
//        BaseWebClient client = new BaseWebClient(this);
//        mChromeClient = new BaseWebChromeClient(this, mBaseWebView);
//// softm
////        JavaScriptBridge jsBridge = new JavaScriptBridge(this, mBaseWebView);
////        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
////            mBaseWebView.addJavascriptInterface(jsBridge, JavaScriptBridge.CALL_NAME);
////        //}
//// ---- ---- ---- ---- ---- softm
//
//        mBaseWebView.setWebViewClient(client);
//        mBaseWebView.setWebChromeClient(mChromeClient);
////        mBaseWebView.loadUrl(Html.fromHtml(url).toString());
////        mBaseWebView.loadUrl(Html.fromHtml(url).toString());
//        mBaseWebView.loadUrl("file:///android_asset/" + url);
//        mBaseWebView.loadUrl(Html.fromHtml(url).toString());
//        mBaseWebView.loadUrl(WUrl.mBaseUrl + "/" + Html.fromHtml(url).toString());
        String pageName = "";
        WUrl.ReqId reqId = null;
        Html.fromHtml(url);
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            L.e(TAG,"url",e);
        }
//        uri.getQuery();
        try {
            pageName = url.toUpperCase().substring(0, url.lastIndexOf('.'));
        } catch(Exception ex) {
            L.e(TAG,"url", ex);
        }
        try {
            reqId = WUrl.value("WEB_"+pageName);
        } catch(Exception ex) {
            L.e(TAG,"reqId", ex);
        }
        if ( reqId != null ) {
            mBaseWebView.loadUrl(reqId.getReqUrl() + "?"+uri.getQuery());
        } else {
//            if( url.indexOf("youtube.com") > -1 ) {
            if( url.startsWith("https:") ) {
                mBaseWebView.loadUrl(url);
            }
        }
//        mBaseWebView.loadUrl(url);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
        }
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (PermissionUtil.onRequestPermissionsResult(WebPopActivity.this, permsRequestCode, permissions, grandResults)) {
            initView();
        }
    }

    @Override
    public void showNetworkError(int errorCode, String description) {
        super.showNetworkError(errorCode, description);
//        if (Utils.hasPermission(this)) {
        if ( !url.contains(WUrl.ReqId.WEB_INDEX.getUrl()) && !url.contains(WUrl.ReqId.WEB_QNA.getUrl()) && !url.contains(WUrl.ReqId.WEB_LABORATORY.getUrl()) ) {
//            if ( errorCode != 444 ) {
                String webUrlLocal = WUrl.ADDR_LOCAL_ASSET + "/error.html?popup=yes";
                //            String webUrlLocal = WUrl.ReqId.WEB_ERROR.getReqUrl();
                mBaseWebView.loadUrl(webUrlLocal);
//            }
//        }
        }
    }
}