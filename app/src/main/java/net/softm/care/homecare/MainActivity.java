package net.softm.care.homecare;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.*;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import org.json.JSONObject; // SOFTM-APP-SHARE 날짜:20261007 : 웹 공유의 제목과 URL을 안전하게 분리
import softm.hannun.update.HannunInAppUpdateManager;

public class MainActivity extends Activity {
    private WebView web;
    private ProgressBar progress;
    private LinearLayout error;
    private FrameLayout nativeAdContainer;
    private NativeAd nativeAd;
    private InterstitialAd interstitialAd;
    private Dialog exitDialog;
    /** SOFTM-EXIT-AD-PRELOAD START 날짜:20261016 : 종료창을 연 뒤 광고를 기다리지 않도록 사각 배너를 앱 이용 중 미리 준비 */
    private AdView preloadedExitAd;
    private boolean preloadedExitAdReady = false;
    /** SOFTM-EXIT-AD-PRELOAD END */
    private final Handler adHandler = new Handler(Looper.getMainLooper());
    private int interstitialShownCount = 0;
    private boolean interstitialLoading = false;
    private boolean interstitialShowScheduled = false;
    private long appStartedAt = 0L;
    private long lastInterstitialShownAt = 0L;
    private GeolocationPermissions.Callback locationCallback;
    private String locationOrigin;
    private HannunInAppUpdateManager appUpdater;
    private static final String HOME = "https://homecare.designboard.net/";
    private static final long INTERSTITIAL_FIRST_DELAY_MS = 75_000L;
    private static final long INTERSTITIAL_COOLDOWN_MS = 180_000L;
    private static final int INTERSTITIAL_MAX_PER_SESSION = 3;

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
        /** SOFTM-ADMOB-NATIVE START 날짜:20261010 : WebView 탐색을 가리지 않고 앱 하단에 네이티브 광고 영역을 예약 */
        nativeAdContainer = new FrameLayout(this);
        nativeAdContainer.setVisibility(View.GONE);
        root.addView(nativeAdContainer, new LinearLayout.LayoutParams(-1, -2));
        /** SOFTM-ADMOB-NATIVE END */
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
        appStartedAt = System.currentTimeMillis(); // SOFTM-ADMOB-INTERSTITIAL 날짜:20261010 : 전면광고가 앱 진입 직후 사용을 막지 않도록 기준 시각 저장
        initializeAds(); // SOFTM-ADMOB-INTERSTITIAL 날짜:20261010 : 네이티브와 전면광고를 같은 SDK 초기화 흐름에서 준비
    }

    /** SOFTM-ADMOB-NATIVE START 날짜:20261010 : 광고 실패가 본문 이용을 막지 않도록 하단 네이티브 광고만 독립 로드 */
    private void initializeAds() {
        MobileAds.initialize(this, initializationStatus -> {
            loadNativeAd();
            loadInterstitialAd();
            preloadExitBannerAd(); // SOFTM-EXIT-AD-PRELOAD 날짜:20261016 : 종료 동작 전에 광고 요청을 완료해 빈 광고 영역 노출 방지
            scheduleInterstitialOpportunity();
        });
    }

    private void loadNativeAd() {
        AdLoader loader = new AdLoader.Builder(this, getString(R.string.admob_native_ad_unit_id))
            .forNativeAd(ad -> {
                if (isDestroyed()) { ad.destroy(); return; }
                if (nativeAd != null) nativeAd.destroy();
                nativeAd = ad;
                nativeAdContainer.removeAllViews();
                nativeAdContainer.addView(createNativeAdView(ad));
                nativeAdContainer.setVisibility(View.VISIBLE);
            })
            .withAdListener(new AdListener() {
                @Override public void onAdFailedToLoad(LoadAdError error) {
                    nativeAdContainer.setVisibility(View.GONE);
                }
            })
            .withNativeAdOptions(new NativeAdOptions.Builder().build())
            .build();
        loader.loadAd(new AdRequest.Builder().build());
    }

    private NativeAdView createNativeAdView(NativeAd ad) {
        NativeAdView adView = new NativeAdView(this);
        adView.setBackgroundColor(Color.WHITE);
        adView.setPadding(20, 12, 20, 12);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView badge = new TextView(this);
        badge.setText("광고");
        badge.setTextColor(0xff0f766e);
        badge.setTextSize(11);
        badge.setGravity(Gravity.CENTER);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setBackgroundColor(0xffe6f4ef);
        row.addView(badge, new LinearLayout.LayoutParams(dp(38), dp(30)));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(14, 0, 14, 0);

        TextView headline = new TextView(this);
        headline.setText(ad.getHeadline());
        headline.setTextColor(0xff1f2937);
        headline.setTextSize(15);
        headline.setTypeface(Typeface.DEFAULT_BOLD);
        headline.setMaxLines(2);
        adView.setHeadlineView(headline);
        text.addView(headline);

        TextView body = new TextView(this);
        body.setText(ad.getBody());
        body.setTextColor(0xff667085);
        body.setTextSize(12);
        body.setMaxLines(1);
        body.setVisibility(ad.getBody() == null ? View.GONE : View.VISIBLE);
        adView.setBodyView(body);
        text.addView(body);
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1));

        Button callToAction = new Button(this);
        callToAction.setText(ad.getCallToAction());
        callToAction.setTextSize(12);
        callToAction.setAllCaps(false);
        callToAction.setVisibility(ad.getCallToAction() == null ? View.GONE : View.VISIBLE);
        adView.setCallToActionView(callToAction);
        row.addView(callToAction, new LinearLayout.LayoutParams(-2, dp(44)));

        adView.addView(row, new NativeAdView.LayoutParams(-1, -2));
        adView.setNativeAd(ad);
        return adView;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
    /** SOFTM-ADMOB-NATIVE END */

    /** SOFTM-ADMOB-INTERSTITIAL START 날짜:20261010 : 사용 흐름을 과도하게 끊지 않는 전면광고 노출 빈도 제어 */
    private void loadInterstitialAd() {
        if (interstitialLoading || interstitialAd != null) return;
        interstitialLoading = true;
        InterstitialAd.load(this, getString(R.string.admob_interstitial_ad_unit_id), new AdRequest.Builder().build(),
            new InterstitialAdLoadCallback() {
                @Override public void onAdLoaded(InterstitialAd ad) {
                    interstitialLoading = false;
                    interstitialAd = ad;
                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override public void onAdDismissedFullScreenContent() {
                            interstitialAd = null;
                            lastInterstitialShownAt = System.currentTimeMillis();
                            loadInterstitialAd();
                            scheduleInterstitialOpportunity();
                        }

                        @Override public void onAdFailedToShowFullScreenContent(AdError adError) {
                            interstitialAd = null;
                            loadInterstitialAd();
                        }
                    });
                    scheduleInterstitialOpportunity();
                }

                @Override public void onAdFailedToLoad(LoadAdError error) {
                    interstitialLoading = false;
                    interstitialAd = null;
                    adHandler.postDelayed(() -> {
                        if (!isDestroyed()) loadInterstitialAd();
                    }, INTERSTITIAL_COOLDOWN_MS);
                }
            });
    }

    private void scheduleInterstitialOpportunity() {
        if (interstitialShowScheduled || interstitialShownCount >= INTERSTITIAL_MAX_PER_SESSION) return;
        long now = System.currentTimeMillis();
        long firstWait = Math.max(0L, (appStartedAt + INTERSTITIAL_FIRST_DELAY_MS) - now);
        long cooldownWait = lastInterstitialShownAt == 0L ? 0L : Math.max(0L, (lastInterstitialShownAt + INTERSTITIAL_COOLDOWN_MS) - now);
        long wait = Math.max(firstWait, cooldownWait);
        interstitialShowScheduled = true;
        adHandler.postDelayed(() -> {
            interstitialShowScheduled = false;
            showInterstitialIfReady();
        }, wait);
    }

    private void showInterstitialIfReady() {
        if (isDestroyed() || isFinishing() || interstitialShownCount >= INTERSTITIAL_MAX_PER_SESSION) return;
        if (interstitialAd == null) {
            loadInterstitialAd();
            return;
        }
        interstitialShownCount++;
        interstitialAd.show(this);
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
    private void retry() { String url = web.getUrl(); web.loadUrl(url != null && trusted(Uri.parse(url)) ? url : HOME); }
    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == 10 && locationCallback != null) {
            boolean allowed = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            locationCallback.invoke(locationOrigin, allowed, false); locationCallback = null;
        }
    }
    /** SOFTM-EXIT-AD START 날짜:20261016 : 종료창은 준비된 광고만 즉시 표시하고 로딩 중인 빈 영역은 숨김 */
    @Override public void onBackPressed() { if (web.canGoBack()) web.goBack(); else showExitDialog(); }

    private void showExitDialog() {
        if (exitDialog != null && exitDialog.isShowing()) return;
        exitDialog = new Dialog(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(22), dp(24), dp(18));
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(24));
        panel.setBackground(background);

        TextView title = new TextView(this);
        title.setText("앱을 종료하시겠습니까?");
        title.setTextColor(0xff1f2937);
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        panel.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView message = new TextView(this);
        message.setText("돌봄한눈을 종료하고 홈 화면으로 돌아갑니다.");
        message.setTextColor(0xff667085);
        message.setTextSize(14);
        message.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(-1, -2);
        messageParams.setMargins(0, dp(8), 0, dp(14));
        panel.addView(message, messageParams);

        FrameLayout adSlot = new FrameLayout(this);
        adSlot.setBackgroundColor(0xfff3f7f5);
        adSlot.setVisibility(View.GONE); // SOFTM-EXIT-AD-PRELOAD 날짜:20261016 : 광고가 준비되지 않았으면 큰 빈 사각형을 차지하지 않도록 숨김
        LinearLayout.LayoutParams adParams = new LinearLayout.LayoutParams(dp(300), dp(250));
        adParams.gravity = Gravity.CENTER_HORIZONTAL;
        adParams.setMargins(0, 0, 0, dp(18));
        panel.addView(adSlot, adParams);
        AdView exitAdView = attachExitBannerAd(adSlot);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);

        Button stay = new Button(this);
        stay.setText("계속 이용");
        stay.setAllCaps(false);
        stay.setOnClickListener(v -> exitDialog.dismiss());
        actions.addView(stay, new LinearLayout.LayoutParams(0, dp(48), 1));

        Button exit = new Button(this);
        exit.setText("종료");
        exit.setAllCaps(false);
        exit.setTextColor(Color.WHITE);
        exit.setBackgroundColor(0xff0f766e);
        exit.setOnClickListener(v -> {
            finish(); // SOFTM-EXIT-AD-PRELOAD 날짜:20261016 : 실제 종료에서는 다음 광고를 불필요하게 다시 요청하지 않도록 종료 상태를 먼저 설정
            exitDialog.dismiss();
        });
        LinearLayout.LayoutParams exitParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        exitParams.setMargins(dp(10), 0, 0, 0);
        actions.addView(exit, exitParams);
        panel.addView(actions, new LinearLayout.LayoutParams(-1, -2));

        exitDialog.setContentView(panel);
        exitDialog.setOnDismissListener(dialog -> {
            if (exitAdView != null) exitAdView.destroy();
            if (!isFinishing() && !isDestroyed()) preloadExitBannerAd(); // SOFTM-EXIT-AD-PRELOAD 날짜:20261016 : 계속 이용하면 다음 종료창 광고를 다시 미리 준비
        });
        if (exitDialog.getWindow() != null) exitDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        exitDialog.show();
        if (exitDialog.getWindow() != null) exitDialog.getWindow().setLayout(dp(340), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    /** SOFTM-EXIT-AD-PRELOAD START 날짜:20261016 : 네트워크 응답 지연을 종료창 표시 시점에서 앱 이용 시점으로 이동 */
    private void preloadExitBannerAd() {
        if (isDestroyed() || preloadedExitAd != null) return;
        String adUnitId = getString(R.string.admob_exit_banner_ad_unit_id);
        if (adUnitId == null || adUnitId.trim().isEmpty()) return;
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId(adUnitId);
        preloadedExitAd = adView;
        preloadedExitAdReady = false;
        adView.setAdListener(new AdListener() {
            @Override public void onAdLoaded() {
                if (isDestroyed() || preloadedExitAd != adView) {
                    adView.destroy();
                    return;
                }
                preloadedExitAdReady = true;
            }

            @Override public void onAdFailedToLoad(LoadAdError error) {
                if (preloadedExitAd == adView) {
                    preloadedExitAd = null;
                    preloadedExitAdReady = false;
                }
                adView.destroy();
                adHandler.postDelayed(() -> {
                    if (!isDestroyed()) preloadExitBannerAd();
                }, INTERSTITIAL_COOLDOWN_MS);
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
    }

    private AdView attachExitBannerAd(FrameLayout adSlot) {
        if (!preloadedExitAdReady || preloadedExitAd == null) return null;
        AdView adView = preloadedExitAd;
        preloadedExitAd = null;
        preloadedExitAdReady = false;
        adSlot.addView(adView, new FrameLayout.LayoutParams(dp(300), dp(250), Gravity.CENTER));
        adSlot.setVisibility(View.VISIBLE);
        return adView;
    }
    /** SOFTM-EXIT-AD-PRELOAD END */
    /** SOFTM-EXIT-AD END */

    @Override protected void onSaveInstanceState(Bundle state) { web.saveState(state); super.onSaveInstanceState(state); }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (appUpdater.onActivityResult(requestCode, resultCode)) return;
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override protected void onPause() { appUpdater.onPause(); web.onPause(); super.onPause(); }
    @Override protected void onResume() { super.onResume(); appUpdater.onResume(); if (web != null) web.onResume(); }
    @Override protected void onDestroy() {
        appUpdater.destroy();
        adHandler.removeCallbacksAndMessages(null); // SOFTM-ADMOB-INTERSTITIAL 날짜:20261010 : 종료된 화면에서 예약 광고가 다시 뜨지 않도록 정리
        if (locationCallback != null) locationCallback.invoke(locationOrigin, false, false);
        if (nativeAd != null) nativeAd.destroy(); // SOFTM-ADMOB-NATIVE 날짜:20261010 : 광고 객체가 Activity 종료 뒤 리소스를 붙잡지 않도록 해제
        if (preloadedExitAd != null) preloadedExitAd.destroy(); // SOFTM-EXIT-AD-PRELOAD 날짜:20261016 : 표시하지 않은 사전 로드 광고도 Activity 종료 시 해제
        if (exitDialog != null && exitDialog.isShowing()) exitDialog.dismiss(); // SOFTM-EXIT-AD 날짜:20261010 : 종료 중 다이얼로그 광고 뷰를 함께 해제
        web.destroy(); super.onDestroy();
    }
}
