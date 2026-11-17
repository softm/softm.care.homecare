package net.softm.care.homecare;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;
import android.view.View;
import android.widget.*;
import org.json.JSONObject; // SOFTM-APP-SHARE 날짜:20261007 : 웹 공유의 제목과 URL을 안전하게 분리
import softm.hannun.update.HannunInAppUpdateManager;
import softm.hannun.ads.HannunAdConfig;
import softm.hannun.ads.HannunAds;

public class MainActivity extends Activity {
    private WebView web;
    private ProgressBar progress;
    private LinearLayout error;
    private HannunAds ads;
    private GeolocationPermissions.Callback locationCallback;
    private String locationOrigin;
    private HannunInAppUpdateManager appUpdater;
    private static final String HOME = "https://homecare.designboard.net/";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        appUpdater = new HannunInAppUpdateManager(this, null);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xfff6f8f7);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        // SOFTM-FULL-WEB 날짜:20261007 : 중복 홈·새로고침 도구를 제거해 웹 지도에 화면 높이를 반환
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        root.addView(progress, new LinearLayout.LayoutParams(-1, 6));
        error = new LinearLayout(this); error.setOrientation(LinearLayout.VERTICAL);
        error.setPadding(24, 48, 24, 24);
        TextView message = new TextView(this);
        message.setText("페이지에 연결할 수 없습니다.\n인터넷 연결을 확인한 뒤 다시 시도해 주세요.");
        message.setTextSize(18); error.addView(message);
        Button retry = new Button(this); retry.setText("다시 시도");
        retry.setOnClickListener(v -> retry()); error.addView(retry);
        root.addView(error); error.setVisibility(View.GONE);
        web = new WebView(this);
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout adSection = new LinearLayout(this);
        root.addView(adSection, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true); settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccess(false); settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return route(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) { return route(Uri.parse(url)); }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                error.setVisibility(View.GONE); web.setVisibility(View.VISIBLE); progress.setVisibility(View.VISIBLE);
            }
            /** SOFTM-APP-SHARE START 날짜:20261007 : WebView의 공유 API 부재를 앱 공유창으로 연결 */
            @Override public void onPageFinished(WebView view, String url) {
                view.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                if (!trusted(Uri.parse(url))) return;
                view.evaluateJavascript("(function(){if(navigator.share)return;Object.defineProperty(navigator,'share',{configurable:true,value:function(data){return new Promise(function(resolve,reject){if(!data||!data.url){reject(new TypeError('공유 링크가 없습니다.'));return;}var result=window.prompt('SOFTM_SHARE_LINK',JSON.stringify({title:String(data.title||''),url:String(data.url)}));if(result==='shared')resolve();else reject(new Error('공유창을 열지 못했습니다.'));});}});})()", null);
            }
            /** SOFTM-APP-SHARE END */
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError failure) {
                if (request.isForMainFrame()) showError();
            }
            @Override public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
                if (request.isForMainFrame() && response.getStatusCode() >= 400) showError();
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            /** SOFTM-APP-SHARE START 날짜:20261007 : 공식 페이지의 링크만 Android 공유 본문으로 전달 */
            @Override public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                if (!"SOFTM_SHARE_LINK".equals(message)) return super.onJsPrompt(view, url, message, defaultValue, result);
                if (!trusted(Uri.parse(url)) || view.getUrl() == null || !trusted(Uri.parse(view.getUrl()))) {
                    result.cancel(); return true;
                }
                try {
                    JSONObject data = new JSONObject(defaultValue);
                    String link = data.getString("url");
                    if (!trusted(Uri.parse(link))) { result.cancel(); return true; }
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, link);
                    share.putExtra(Intent.EXTRA_SUBJECT, data.optString("title", "돌봄한눈"));
                    startActivity(Intent.createChooser(share, "링크 공유"));
                    result.confirm("shared");
                } catch (Exception failure) {
                    result.cancel();
                    Toast.makeText(MainActivity.this, "공유창을 열지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            /** SOFTM-APP-SHARE END */
            @Override public void onProgressChanged(WebView view, int value) {
                progress.setProgress(value); if (value == 100) progress.setVisibility(View.GONE);
            }
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (!trusted(Uri.parse(origin))) { callback.invoke(origin, false, false); return; }
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false); return;
                }
                if (locationCallback != null) locationCallback.invoke(locationOrigin, false, false);
                locationCallback = callback; locationOrigin = origin;
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            }
            @Override public void onGeolocationPermissionsHidePrompt() { locationCallback = null; }
        });
        if (state == null || web.restoreState(state) == null) web.loadUrl(HOME);
        ads = new HannunAds(this, adSection, new HannunAdConfig(
            getString(R.string.admob_native_ad_unit_id), null,
            getString(R.string.admob_interstitial_ad_unit_id),
            getString(R.string.admob_exit_banner_ad_unit_id)));
        ads.start();
    }

    /** SOFTM-ADMOB-INTERSTITIAL END */

    private boolean trusted(Uri uri) {
        return "https".equals(uri.getScheme()) && "homecare.designboard.net".equals(uri.getHost())
            && (uri.getPort() == -1 || uri.getPort() == 443);
    }
    private boolean route(Uri uri) {
        if (trusted(uri)) return false;
        String scheme = uri.getScheme();
        Intent intent;
        if ("tel".equals(scheme)) intent = new Intent(Intent.ACTION_DIAL, uri);
        else if ("https".equals(scheme) || "http".equals(scheme) || "mailto".equals(scheme) || "geo".equals(scheme)
                || "kakaomap".equals(scheme) || "nmap".equals(scheme) || "tmap".equals(scheme)) {
            intent = new Intent(Intent.ACTION_VIEW, uri); intent.addCategory(Intent.CATEGORY_BROWSABLE);
        } else { Toast.makeText(this, "지원하지 않는 링크입니다.", Toast.LENGTH_SHORT).show(); return true; }
        try { startActivity(intent); }
        catch (ActivityNotFoundException e) { Toast.makeText(this, "연결할 앱이 설치되어 있지 않습니다.", Toast.LENGTH_LONG).show(); }
        return true;
    }
    private void showError() { error.setVisibility(View.VISIBLE); web.setVisibility(View.GONE); progress.setVisibility(View.GONE); }
    private void retry() {
        String url = web.getUrl();
        web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        web.loadUrl(url != null && trusted(Uri.parse(url)) ? url : HOME,
            java.util.Collections.singletonMap("Cache-Control", "no-cache"));
    }
    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == 10 && locationCallback != null) {
            boolean allowed = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            locationCallback.invoke(locationOrigin, allowed, false); locationCallback = null;
        }
    }
    @Override public void onBackPressed() { if (web.canGoBack()) web.goBack(); else ads.showExitConfirmation(); }

    /** SOFTM-EXIT-AD-PRELOAD END */
    /** SOFTM-EXIT-AD END */

    @Override protected void onSaveInstanceState(Bundle state) { web.saveState(state); super.onSaveInstanceState(state); }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (appUpdater.onActivityResult(requestCode, resultCode)) return;
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override protected void onPause() { ads.onPause(); appUpdater.onPause(); web.onPause(); super.onPause(); }
    @Override protected void onResume() { super.onResume(); appUpdater.onResume(); if (web != null) web.onResume(); ads.onResume(); }
    @Override protected void onDestroy() {
        appUpdater.destroy();
        if (locationCallback != null) locationCallback.invoke(locationOrigin, false, false);
        ads.destroy();
        web.destroy(); super.onDestroy();
    }
}
