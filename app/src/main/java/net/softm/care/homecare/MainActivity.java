package net.softm.care.homecare;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
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
    private LinearLayout landing;
    private boolean firstContentShown;
    private boolean mainFrameFailed;
    private int loadGeneration;
    private long firstLoadStartedAt;
    private HannunAds ads;
    private GeolocationPermissions.Callback locationCallback;
    private String locationOrigin;
    private HannunInAppUpdateManager appUpdater;
    private static final String HOME = "https://homecare.designboard.net/";
    /** SOFTM-MEMBERS START 날짜:20261120 : 외부 SNS 인증 복귀와 기관 증빙 선택을 공식 웹 화면에만 허용 */
    private static final int DOCUMENT_REQUEST = 620;
    private static final int SAVE_DOCUMENT_REQUEST = 621;
    private ValueCallback<Uri[]> documentCallback;
    private String documentOrigin;
    private JsPromptResult fileSaveResult;
    private byte[] fileSaveBytes;
    private String fileSaveOrigin;
    private boolean authReturn(Uri uri) {
        if (uri == null || !trusted(uri) || !"/auth-callback.html".equals(uri.getPath()) || uri.getQuery() != null) return false;
        String fragment = uri.getFragment();
        return fragment != null && (fragment.matches("code=[A-Za-z0-9_-]{43}") || fragment.matches("error=oauth_(cancelled|failed)"));
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (web != null && authReturn(intent.getData())) web.loadUrl(intent.getData().toString());
    }
    /** SOFTM-MEMBERS END */

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

        landing = new LinearLayout(this);
        landing.setOrientation(LinearLayout.VERTICAL);
        landing.setGravity(android.view.Gravity.CENTER);
        landing.setPadding(dp(28), dp(32), dp(28), dp(32));
        landing.setBackgroundColor(0xfff3f8f5);
        ImageView landingIcon = new ImageView(this);
        landingIcon.setImageResource(R.drawable.ic_launcher);
        landingIcon.setContentDescription(null);
        landing.addView(landingIcon, new LinearLayout.LayoutParams(dp(88), dp(88)));
        TextView landingTitle = new TextView(this);
        landingTitle.setText("돌봄한눈");
        landingTitle.setTextColor(0xff174c3c);
        landingTitle.setTextSize(26);
        landingTitle.setGravity(android.view.Gravity.CENTER);
        landingTitle.setTypeface(landingTitle.getTypeface(), android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleLayout = new LinearLayout.LayoutParams(-1, -2);
        titleLayout.topMargin = dp(20);
        landing.addView(landingTitle, titleLayout);
        TextView landingMessage = new TextView(this);
        landingMessage.setText("내 주변 돌봄 정보를 준비하고 있어요");
        landingMessage.setTextColor(0xff547067);
        landingMessage.setTextSize(15);
        landingMessage.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams messageLayout = new LinearLayout.LayoutParams(-1, -2);
        messageLayout.topMargin = dp(8);
        landing.addView(landingMessage, messageLayout);
        ProgressBar landingProgress = new ProgressBar(this);
        landingProgress.setIndeterminate(true);
        landingProgress.setContentDescription("돌봄한눈 불러오는 중");
        LinearLayout.LayoutParams landingProgressLayout = new LinearLayout.LayoutParams(dp(48), dp(48));
        landingProgressLayout.topMargin = dp(24);
        landing.addView(landingProgress, landingProgressLayout);
        // SOFTM-MEMBERS 날짜:20261120 : 숨겨진 WebView도 전체 크기로 측정해 첫 콘텐츠 높이가 0이 되는 대기 실패를 방지
        FrameLayout content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        content.addView(landing, new FrameLayout.LayoutParams(-1, -1));

        error = new LinearLayout(this); error.setOrientation(LinearLayout.VERTICAL);
        error.setPadding(24, 48, 24, 24);
        TextView message = new TextView(this);
        message.setText("페이지에 연결할 수 없습니다.\n인터넷 연결을 확인한 뒤 다시 시도해 주세요.");
        message.setTextSize(18); error.addView(message);
        Button retry = new Button(this); retry.setText("다시 시도");
        retry.setOnClickListener(v -> retry()); error.addView(retry);
        content.addView(error, new FrameLayout.LayoutParams(-1, -1)); error.setVisibility(View.GONE);
        web = new WebView(this);
        web.setBackgroundColor(Color.TRANSPARENT);
        web.setVisibility(View.INVISIBLE);
        content.addView(web, new FrameLayout.LayoutParams(-1, -1));
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
                mainFrameFailed = false;
                loadGeneration += 1;
                if (!firstContentShown) firstLoadStartedAt = SystemClock.uptimeMillis();
                error.setVisibility(View.GONE);
                if (!firstContentShown) {
                    landing.setVisibility(View.VISIBLE);
                    web.setVisibility(View.INVISIBLE);
                }
                progress.setVisibility(View.VISIBLE);
            }
            /** SOFTM-APP-SHARE START 날짜:20261007 : WebView의 공유 API 부재를 앱 공유창으로 연결 */
            @Override public void onPageFinished(WebView view, String url) {
                view.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                progress.setVisibility(View.GONE);
                if (mainFrameFailed || !trusted(Uri.parse(url))) return;
                waitForFirstContent(loadGeneration, 0);
                view.evaluateJavascript("window.hannunSaveFile=function(data){return window.prompt('SOFTM_SAVE_FILE',JSON.stringify(data))==='saved';};window.print=function(){window.prompt('SOFTM_PRINT','');};", null); // SOFTM-MEMBERS 날짜:20261120 : 인증된 증빙·상담 CSV와 인쇄를 Android 시스템 창으로 연결
                view.evaluateJavascript("(function(){if(navigator.share)return;Object.defineProperty(navigator,'share',{configurable:true,value:function(data){return new Promise(function(resolve,reject){if(!data||!data.url){reject(new TypeError('공유 링크가 없습니다.'));return;}var result=window.prompt('SOFTM_SHARE_LINK',JSON.stringify({title:String(data.title||''),url:String(data.url)}));if(result==='shared')resolve();else reject(new Error('공유창을 열지 못했습니다.'));});}});})()", null);
            }
            /** SOFTM-APP-SHARE END */
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError failure) {
                if (request.isForMainFrame()) {
                    android.util.Log.w("HomecareWeb", "main_frame_error code=" + failure.getErrorCode()); // SOFTM-MEMBERS 날짜:20261120 : Release 검증에 오류 코드만 남기고 주소·회원정보는 기록하지 않음
                    showError();
                }
            }
            @Override public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse response) {
                if (request.isForMainFrame() && response.getStatusCode() >= 400) {
                    android.util.Log.w("HomecareWeb", "main_frame_http status=" + response.getStatusCode());
                    showError();
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            /** SOFTM-MEMBERS START 날짜:20261120 : 사업자등록증·기관 사진 첨부는 시스템 선택창과 임시 URI 권한을 사용 */
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (view.getUrl() == null || !trusted(Uri.parse(view.getUrl()))) { callback.onReceiveValue(null); return true; }
                if (documentCallback != null) documentCallback.onReceiveValue(null);
                documentCallback = callback;
                documentOrigin = view.getUrl();
                Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                pick.addCategory(Intent.CATEGORY_OPENABLE);
                pick.setType("*/*");
                java.util.ArrayList<String> accepted = new java.util.ArrayList<>();
                for (String entry : params.getAcceptTypes()) {
                    for (String candidate : entry.split(",")) {
                        String type = candidate.trim().toLowerCase(java.util.Locale.ROOT);
                        if (("application/pdf".equals(type) || "image/jpeg".equals(type) || "image/png".equals(type) || "image/webp".equals(type) || "image/*".equals(type)) && !accepted.contains(type)) accepted.add(type);
                    }
                }
                if (accepted.isEmpty()) accepted.add("image/*");
                pick.putExtra(Intent.EXTRA_MIME_TYPES, accepted.toArray(new String[0]));
                pick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
                pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try { startActivityForResult(pick, DOCUMENT_REQUEST); }
                catch (ActivityNotFoundException failure) {
                    documentCallback.onReceiveValue(null); documentCallback = null;
                    Toast.makeText(MainActivity.this, "파일을 선택할 앱을 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            /** SOFTM-MEMBERS END */
            /** SOFTM-APP-SHARE START 날짜:20261007 : 공식 페이지의 링크만 Android 공유 본문으로 전달 */
            @Override public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                if ("SOFTM_SAVE_FILE".equals(message) || "SOFTM_PRINT".equals(message)) {
                    if (!trusted(Uri.parse(url)) || view.getUrl() == null || !trusted(Uri.parse(view.getUrl()))) { result.cancel(); return true; }
                    if ("SOFTM_SAVE_FILE".equals(message)) { saveDocument(defaultValue, result); return true; }
                    try {
                        android.print.PrintManager manager = (android.print.PrintManager) getSystemService(PRINT_SERVICE);
                        manager.print("돌봄한눈 기록", web.createPrintDocumentAdapter("돌봄한눈 기록"), new android.print.PrintAttributes.Builder().build());
                        result.confirm("opened");
                    } catch (Exception failure) { result.cancel(); Toast.makeText(MainActivity.this, "인쇄 창을 열지 못했습니다.", Toast.LENGTH_SHORT).show(); }
                    return true;
                }
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
        if (state == null || web.restoreState(state) == null) {
            web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            Uri incoming = getIntent().getData();
            web.loadUrl(authReturn(incoming) ? incoming.toString() : HOME, java.util.Collections.singletonMap("Cache-Control", "no-cache")); // SOFTM-MEMBERS 날짜:20261120 : 콜드 복귀에서도 코드 검증은 웹의 원래 PKCE 정보로 수행
        }
        ads = new HannunAds(this, adSection, new HannunAdConfig(
            getString(R.string.admob_native_ad_unit_id), null,
            getString(R.string.admob_interstitial_ad_unit_id),
            getString(R.string.admob_exit_banner_ad_unit_id)));
        ads.start();
    }

    /** SOFTM-ADMOB-INTERSTITIAL END */

    /** SOFTM-MEMBERS START 날짜:20261120 : 파일 내용·쿠키를 외부 URL에 보내지 않고 사용자가 고른 문서 위치에만 저장 */
    private void saveDocument(String encoded, JsPromptResult result) {
        if (fileSaveResult != null || encoded == null || encoded.length() > 14 * 1024 * 1024) { result.cancel(); return; }
        try {
            JSONObject file = new JSONObject(encoded);
            String mime = file.getString("type");
            if (!java.util.Arrays.asList("application/pdf", "image/jpeg", "image/png", "image/webp", "text/csv").contains(mime)) throw new IllegalArgumentException();
            byte[] bytes = android.util.Base64.decode(file.getString("base64"), android.util.Base64.DEFAULT);
            if (bytes.length > 10 * 1024 * 1024) throw new IllegalArgumentException();
            String name = file.getString("name").replaceAll("[\\\\/\\p{Cntrl}]", "_").trim();
            if (name.isEmpty() || name.length() > 120) throw new IllegalArgumentException();
            Intent save = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            save.addCategory(Intent.CATEGORY_OPENABLE);
            save.setType(mime);
            save.putExtra(Intent.EXTRA_TITLE, name);
            save.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            fileSaveResult = result; fileSaveBytes = bytes; fileSaveOrigin = web.getUrl();
            startActivityForResult(save, SAVE_DOCUMENT_REQUEST);
        } catch (Exception failure) {
            fileSaveResult = null; fileSaveBytes = null; fileSaveOrigin = null;
            result.cancel();
            Toast.makeText(this, "파일 저장 창을 열지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    /** SOFTM-MEMBERS END */

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
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private void waitForFirstContent(int generation, int attempt) {
        if (mainFrameFailed || generation != loadGeneration) return;
        if (firstContentShown) { web.setVisibility(View.VISIBLE); return; } // SOFTM-MEMBERS 날짜:20261120 : 오류 이후 재시도 성공 시 기존 WebView를 다시 표시
        web.evaluateJavascript(
            "(function(){var b=document.body;if(!b)return false;" +
            "return b.innerText.trim().length>0&&innerWidth>0&&innerHeight>0;})()", // SOFTM-MEMBERS 날짜:20261120 : 고정 배치 지도는 body 높이가 0이므로 실제 WebView 뷰포트로 준비 상태 판단
            value -> {
                if (firstContentShown || mainFrameFailed || generation != loadGeneration) return;
                boolean minimumLandingShown = SystemClock.uptimeMillis() - firstLoadStartedAt >= 600;
                if ("true".equals(value) && minimumLandingShown) {
                    firstContentShown = true;
                    landing.setVisibility(View.GONE);
                    error.setVisibility(View.GONE);
                    web.setVisibility(View.VISIBLE);
                    return;
                }
                if (attempt >= 60) {
                    showError();
                    return;
                }
                web.postDelayed(() -> waitForFirstContent(generation, attempt + 1), 250);
            }
        );
    }
    private void showError() {
        mainFrameFailed = true;
        loadGeneration += 1;
        landing.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
        web.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.GONE);
    }
    private void retry() {
        String url = web.getUrl();
        mainFrameFailed = false;
        if (!firstContentShown) landing.setVisibility(View.VISIBLE);
        error.setVisibility(View.GONE);
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
        if (requestCode == SAVE_DOCUMENT_REQUEST) {
            if (fileSaveResult == null) return;
            final JsPromptResult result = fileSaveResult;
            final byte[] bytes = fileSaveBytes;
            final Uri destination = data == null ? null : data.getData();
            if (resultCode != RESULT_OK || destination == null || !"content".equals(destination.getScheme()) || bytes == null || !java.util.Objects.equals(fileSaveOrigin, web.getUrl())) {
                fileSaveResult = null; fileSaveBytes = null; fileSaveOrigin = null; result.cancel(); return;
            }
            fileSaveBytes = null;
            new Thread(() -> {
                boolean saved = false;
                try (java.io.OutputStream output = getContentResolver().openOutputStream(destination, "w")) {
                    if (output != null) { output.write(bytes); saved = true; }
                } catch (Exception failure) { saved = false; }
                final boolean completed = saved;
                runOnUiThread(() -> {
                    if (fileSaveResult != result) return;
                    fileSaveResult = null; fileSaveOrigin = null;
                    if (completed) result.confirm("saved");
                    else { result.cancel(); Toast.makeText(this, "파일을 저장하지 못했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show(); }
                });
            }, "homecare-document-save").start();
            return;
        }
        if (requestCode == DOCUMENT_REQUEST) {
            if (documentCallback == null) return;
            Uri[] selected = null;
            if (resultCode == RESULT_OK && data != null && web.getUrl() != null && web.getUrl().equals(documentOrigin) && trusted(Uri.parse(web.getUrl()))) {
                java.util.ArrayList<Uri> uris = new java.util.ArrayList<>();
                if (data.getClipData() != null) {
                    for (int i = 0; i < Math.min(data.getClipData().getItemCount(), 10); i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        if (uri != null && "content".equals(uri.getScheme())) uris.add(uri);
                    }
                } else if (data.getData() != null && "content".equals(data.getData().getScheme())) uris.add(data.getData());
                if (!uris.isEmpty()) selected = uris.toArray(new Uri[0]);
            }
            documentCallback.onReceiveValue(selected); documentCallback = null; documentOrigin = null;
            return;
        }
        if (appUpdater.onActivityResult(requestCode, resultCode)) return;
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override protected void onPause() { ads.onPause(); appUpdater.onPause(); web.onPause(); super.onPause(); }
    @Override protected void onResume() { super.onResume(); appUpdater.onResume(); if (web != null) web.onResume(); ads.onResume(); }
    @Override protected void onDestroy() {
        if (fileSaveResult != null) { fileSaveResult.cancel(); fileSaveResult = null; fileSaveBytes = null; fileSaveOrigin = null; }
        if (documentCallback != null) { documentCallback.onReceiveValue(null); documentCallback = null; }
        appUpdater.destroy();
        if (locationCallback != null) locationCallback.invoke(locationOrigin, false, false);
        ads.destroy();
        web.destroy(); super.onDestroy();
    }
}
