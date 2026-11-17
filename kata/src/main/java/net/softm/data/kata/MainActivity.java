
package net.softm.data.kata;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

import net.softm.data.kata.common.AdType;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.NaviDestination;
import net.softm.data.kata.common.NaviStatus;
import net.softm.data.kata.common.ParamCallback;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.god.dto.PrayClass;
import net.softm.data.kata.job.WorkExecutor;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.AlarmUtil;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.LicenseUtil;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.PackageUtil;
import net.softm.data.kata.util.PermissionUtil;
import net.softm.data.kata.util.RestUtil;
import net.softm.data.kata.util.ReviewUtil;
import net.softm.data.kata.util.Utils;
import net.softm.data.kata.web.BasicActivity;
import net.softm.data.kata.web.JavaScriptApi;
import net.softm.data.kata.web.JavaScriptBridge;
import net.softm.lib.common.WLog;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * MainActivity
 * @author softm
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends BasicActivity {
    public static Intent serviceIntent;
    final String TAG = "MainActivity";
    final Activity activity = this;

    static MainActivity instance;

    private boolean mBackPressedToExitOnce;

//    BillingService billingService;
//    BillingUtil billingUtil;

    private AppUpdateManager mAppUpdateManager;
    Dialog popupDialogExit;
    Intent pIntent;
    String pDocId;
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        L.d(TAG,"onCreate");
        EdgeToEdge.enable((ComponentActivity) this);

        super.onCreate(savedInstanceState);
        WorkExecutor.isExecuting = false;
        WApplication.mainActivity = this;

        OverlayViewUtil.stopService();

        pIntent = getIntent();
        Boolean pRestartService = pIntent.getBooleanExtra("P_RESTART_SERVICE",false);
        Boolean pReview = pIntent.getBooleanExtra("P_REVIEW",false);
        if ( pRestartService ) {
            if (serviceIntent != null) {
                stopService(serviceIntent);
                ConnectService.serviceIntent = null;
                serviceIntent = null;
            }
        }


        if ( mApp.connectServiceMode.equals(WApplication.ConnectServiceMode.RESTART_SEARVICE) ||
             mApp.connectServiceMode.equals(WApplication.ConnectServiceMode.RESTART_SEARVICE_TEST)
        ) {
            /**
             * 저전력 활성화
             */
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
            boolean isWhiteListing = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                isWhiteListing = pm.isIgnoringBatteryOptimizations(getApplicationContext().getPackageName());
            }
            if (!isWhiteListing) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivity(intent);
            }
        }

        WApplication.mInstance.mGeofencingClient = LocationServices.getGeofencingClient(this);
        RestUtil.getBasicSettings(this,()-> {
        });

        if ( MapUtil.isUISupport() ) {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
            doExecInstallReferrer(
                     new ParamCallback<PrayClass>() {
                        @Override
                        public void onResult(PrayClass prayClass) {
                            if ( prayClass != null ) {
                                pDocId = prayClass.getDocId();
                                if ( Utils.isNotEmpty(pDocId) ) {
                                    pIntent.putExtra("P_DOC_ID",pDocId);
                                }
                            }
                            init();
                            initHome();
                        }
                    }
            );
        } else {
            setContentView(R.layout.activity_main_native);
            ButterKnife.bind(this);
        }

        // Call before consuming insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            v.setBackgroundColor(getResources().getColor(R.color.mainColor));
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(getResources().getColor(R.color.mainColor));
            return insets;
        });

        popupDialogExit = new Dialog(MainActivity.this);

        popupDialogExit.setContentView(R.layout.ad_view_popup);

        RestUtil.getBasicInfo(this,()->{
            if ( AdUtil.isAdingMainBanner() ) {
                runOnUiThread(() -> {
                    AdUtil.addBanner(findViewById(R.id.ad_view_container), MainActivity.this, AdType.BANNER);
                    /** 종료시 표시될 광로 로드 */
                    AdUtil.loadBanner(MainActivity.this, AdType.EXIT_BANNER, AdSize.MEDIUM_RECTANGLE,()->{
                    },()->{
                    });
                });
            }
        });

        if ( pReview ) {
            if ( ReviewUtil.isReviewRequestAvailable() ) {
                ReviewUtil.popupReview(this,()->{
                    Intent intent2 = new Intent(MainConnectReceiver.getAction());
                    intent2.putExtra(MainConnectReceiver.INTENT_EXTRA_APP_REVIEW,MainConnectReceiver.INTENT_EXTRA_APP_REVIEW_CALLBACK);
                    this.sendBroadcast(intent2);
                    finishAndRemoveTask();
//                  finish();
                });
            }
        }

//        billingUtil = new BillingUtil(this);
//        billingUtil.startConnection();
//        billingUtil = BillingUtil.getInstance(this);
//        billingUtil.startConnection();
//        billingService = new BillingService(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        WLog.d(TAG, "onNewIntent");
        onCreate(null);
    }

    String pCheckMode = null;

    @SuppressLint("WrongConstant")
    private void init() {
        MainConnectReceiver.threadStarted = false; // TODO SOFTM : 초기화 : 2024.12.11 : MainConnectReceiver에서 stopWaitOverlay() 에서 threadStarted = false; 으로 초기화 하고 있으나 비정상적으로 초기화가 실행되지 않아 에외적인 방편으로 MainActivity 진입시 초기화 홤
        setClientForWebView();
        String type = null;
        if ( getIntent() != null && getIntent().getExtras() != null)
            type = getIntent().getExtras().getString(Const.INTENT_SERVER_TYPE);

        MapUtil.setOpenPopup("0");

    }

    void initHome() {
        //		String webUrlLocal = "file:///android_asset/index.html";
        String webUrlLocal = null;
        if ( findViewById(R.id.popup) == null ) { // Popup 호출된경우 진입 안하게.
            Locale locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = getApplicationContext().getResources().getConfiguration().getLocales().get(0);
            }else{
                locale = getApplicationContext().getResources().getConfiguration().locale;
            }
            String displayCountry = locale.getDisplayCountry();
            String country = locale.getCountry();
            String language = locale.getLanguage();

            Intent intent = getIntent();

/** 에러 상황을 알릴 필요 없다고 판단해서 로직 제거함.
            String pUnCaughtException = intent.getStringExtra("UNCAUGHT_EXCEPTION");
            if ( Utils.isNotEmpty(pUnCaughtException) ) {
                if ( ConnectAccessibilityService.mContext == null ) {
                    Fc.i(this,TAG,"initHome","UNCAUGHT_EXCEPTION ConnectAccessibilityService.mContext == null");
                    alert(getString(R.string.msg_uncaught_exception,getString(R.string.app_name)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PermissionUtil.startRequestPermission(MainActivity.this, PermissionType.ACCESSIBILITY,null, null);
                        }
                    });
                } else {
                    Fc.i(this,TAG,"initHome","UNCAUGHT_EXCEPTION ConnectAccessibilityService.mContext != null");
                    alert(getString(R.string.msg_uncaught_exception,getString(R.string.app_name)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PermissionUtil.startRequestPermission(MainActivity.this, PermissionType.ACCESSIBILITY,null, null);
                        }
                    });
                }
            }
*/
            if (!Utils.hasPermissionForBasic(this)
            || ( !PermissionUtil.hasAccessibilityPermissions() && !MapUtil.isSkipAcsPermission() )
            ) {
                if (MapUtil.isFirstStart()) {
                    webUrlLocal = WUrl.ReqId.WEB_START.getReqUrl() + "?lng=" + language;
                } else if (MapUtil.isAppUpdated() && !MapUtil.isAppUpdatedNotiConfirm()) {
                    webUrlLocal = WUrl.ReqId.WEB_MAIN.getReqUrl() + "?lng=" + language;
                } else {
                    webUrlLocal = WUrl.ReqId.WEB_PERMISSION.getReqUrl() + "?lng=" + language;
                }
            } else if ( !MapUtil.isAppInstalled() && Utils.hasPermissionForBasic(this) ) {
                MapUtil.completeAppInstalled();
                webUrlLocal = WUrl.ReqId.WEB_INSTALLED.getReqUrl() + "?lng=" + language;
            } else {
                webUrlLocal = WUrl.ReqId.WEB_MAIN.getReqUrl() + "?lng=" + language;
                String pPage = intent.getStringExtra("P_FOWARD_PAGE");
                String docId = intent.getStringExtra("P_DOC_ID");
                String pDocId = intent.getStringExtra("P_PARTY_DOC_ID");

                String fowardPageUrl = "";
                String paramFowardPageUrl = "";
                String pParams = intent.getStringExtra("P_PARAMS");
                if ( Utils.isNotEmpty(docId) || Utils.isNotEmpty(pDocId)  ) {
                    pPage = WUrl.ReqId.WEB_PRAY.name();
                    if ( Utils.isNotEmpty(docId) ) {
                        pParams = docId;
                    }
                }
                if ( Utils.isNotEmpty(pPage) ) {
                    if ( WUrl.value(pPage) != null && WUrl.value(pPage).getReqUrl() != null ) {
                        fowardPageUrl = WUrl.value(pPage).getUrl();
                        try {
                            paramFowardPageUrl = fowardPageUrl.toUpperCase().substring(0, fowardPageUrl.lastIndexOf('.'));
                        } catch(Exception exF) {
                        }
                    }
                    if ( !WUrl.ReqId.WEB_SEARCH.equals(WUrl.value(pPage)) && !WUrl.ReqId.WEB_HOME_COMPANY.equals(WUrl.value(pPage)) && !WUrl.ReqId.WEB_REG_POI.equals(WUrl.value(pPage)) ) {
                        webUrlLocal = WUrl.value(pPage).getReqUrl() + "?lng=" + language;
                    }
                }

                int pTidx = intent.getIntExtra("P_TIDX", -1);
                pCheckMode = intent.getStringExtra("P_CHECK_MODE");
                PackageManager pm = getPackageManager();
                ComponentName componentName = new ComponentName(this, ConnectAccessibilityService.class);
                if ( !MapUtil.isNotiIcon() ) {
                    if (PermissionUtil.hasAccessibilityPermissions()
//                            PermissionUtil.hasIgnoreBatteryPermissions() &&
                    ) {
                        WApplication.mInstance.connectServiceMode = WApplication.ConnectServiceMode.ACCESSIBILITY_SERVICE;
//                        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    } else {
//                        toast("정상적인 앱 사용을 위해 \"알림바\"를 사용으로 설정하거나,\n" + "\"접근성\" 허용을 해야합니다.");
                        alert(R.string.msg_disabled_noti_icon);
//                        alert("정상적인 앱 사용을 위해 \"알림바\"를 사용으로 설정하거나,\n" + "\"접근성\" 허용을 해야합니다.");
        //
//                        pPage = "PERMISSION";
//                        pCheckMode = "accessibility";
//                        pParams = "";
                    }
                } else {
                    WApplication.mInstance.connectServiceMode = WApplication.ConnectServiceMode.NOTIFICATION_SERVICE;
//                    pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
                }
// file:///android_asset/permission.html?lng=ko

                webUrlLocal += !Utils.isEmpty(pCheckMode) ? "&p_check_mode=" + pCheckMode : "";
                webUrlLocal += !Utils.isEmpty(pParams) ? "&p_params=" + pParams : "";
                webUrlLocal += !Utils.isEmpty(pPage) ? "&p_foward_page=" + paramFowardPageUrl: "";
                webUrlLocal += pTidx != -1 ? "&p_tidx=" + pTidx : "";
                webUrlLocal += !Utils.isEmpty(pPage) ? "&p_foward_page=" + paramFowardPageUrl: "";
                webUrlLocal += Utils.isNotEmpty(pDocId) ? "&p_doc_id=" + pDocId : "";
            }
            mWebview.loadUrl(webUrlLocal);
        }
    }

    @Override
    public void onBackPressed() {
        if ( MapUtil.isUISupport() ) {
            String curUrl = StringUtils.defaultString(mWebview.getUrl());

            if ( MapUtil.isOpenPopup() ) {
                JavaScriptBridge.callJavascriptFunc("fn_BackKeyPressed");
            } else {
                if (findViewById(R.id.popup) == null) { // MainActivity
                    if (curUrl.indexOf(WUrl.ReqId.WEB_MAIN.getUrl()) > -1 ||
                            ( curUrl.indexOf(WUrl.ReqId.WEB_PERMISSION.getUrl()) > -1 && Utils.isEmpty(pCheckMode) ) ||
                            curUrl.indexOf(WUrl.ReqId.WEB_ERROR.getUrl()) > -1 ||
                            curUrl.indexOf(WUrl.ReqId.WEB_START.getUrl()) > -1
                    ) {
                        if ( !LicenseUtil.isPaidLicense() ) { /** 유료회원 광고 미적용 : 2025-06-06 */
                            this.mBackPressedToExitOnce = true;
                            /** 2025-06-04 : 다이얼로그 팝업에서 종료하게 변경 */
                            if ( curUrl.indexOf(WUrl.ReqId.WEB_MAIN.getUrl()) > -1 ) {
                                showAdViewPopup();
                            }
                        } else {
                            if (mBackPressedToExitOnce) {
                                 super.onBackPressed();
                                finish();
                            } else {
                                this.mBackPressedToExitOnce = true;
                                toast(R.string.common_back_close); // 다시 눌러 종료하십시요.
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mBackPressedToExitOnce = false;
                                    }
                                }, Const.BACK_BUTTON_VALID_FINISH_TIME);
                            }
                        }
                    } else {
                        if (mWebview.canGoBack()) {
                            mWebview.goBack();
                        } else {
                            if ( "accessibility".equals( pCheckMode) ) {
                                Utils.goFowardPage(MainActivity.this, WUrl.ReqId.WEB_ONOFF.name(), -1, "");
                            } else {
                                mWebview.loadUrl(WUrl.ReqId.WEB_MAIN.getReqUrl());
                            }
                        }
                    }
                } else { // popup
                    if (mBackPressedToExitOnce) {
                        JavaScriptBridge.callJavascriptFunc("fn_beforeunload", callbackGubun, "ok");
                        WebView popup = findViewById(R.id.popup);
                        ViewGroup mainView = findViewById(R.id.mainView);
                        mainView.removeView(popup);
                        popup.destroy();
                    } else {
                        this.mBackPressedToExitOnce = true;
                        toast(R.string.common_back_popup_close); // 다시 눌러 종료하십시요.
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mBackPressedToExitOnce = false;
                            }
                        }, Const.BACK_BUTTON_VALID_FINISH_TIME);
                    }
                }
            }
        } else {
            if (mBackPressedToExitOnce) {
                super.onBackPressed();
            } else {
                this.mBackPressedToExitOnce = true;
                if (findViewById(R.id.popup) == null) {
                    toast(R.string.common_back_close); // 다시 눌러 종료하십시요.
                } else {
                    toast(R.string.common_back_popup_close); // 다시 눌러 종료하십시요.
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBackPressedToExitOnce = false;
                    }
                }, Const.BACK_BUTTON_VALID_FINISH_TIME);
            }
        }
    }
    FrameLayout adFrame;
    private void showAdViewPopup() {
        // 팝업 다이얼로그 크기 설정 (화면 크기의 90% 넓이, 70% 높이)
        Window window = popupDialogExit.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            // 화면의 너비와 높이 구하기
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);  // 화면의 90%
//            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.7);  // 화면의 70%

            // 크기 설정
            params.width = width;
//            params.height = height;

            // 다이얼로그의 위치를 화면의 중앙으로 설정
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
        }

        FrameLayout adFrame = popupDialogExit.findViewById(R.id.ad_frame);
//        if ( AdUtil.getWaitBanner() != null ) {
            runOnUiThread(() -> {
                //            AdView adView = AdUtil.loadWaitBanner(MainActivity.this, AdType.BANNER, AdSize.MEDIUM_RECTANGLE,()->{
                //            },()->{
                //                new Callkt("showAdViewPopup").run((param) -> {
                AdUtil.addLoadBanner(adFrame, MainActivity.this, AdType.BANNER);
                popupDialogExit.show();
                //                    return null;
                //                });
                //            });
            });
//        } else {
//            runOnUiThread(() -> {
//                AdUtil.loadWaitBanner(MainActivity.this, AdType.BANNER, AdSize.MEDIUM_RECTANGLE,()->{
//                },()->{
//                    new Callkt("showAdViewPopup").run((param) -> {
//                    AdUtil.addWaitBanner(adFrame, MainActivity.this, AdType.BANNER);
//                    popupDialogExit.show();
//                                        return null;
//                    });
//                });
//            });
//        }

        Button btnCancel = popupDialogExit.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBackPressedToExitOnce = false;
                popupDialogExit.dismiss();
            }
        });

        Button btnDismiss = popupDialogExit.findViewById(R.id.btn_dismiss);
        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                popupDialogExit.dismiss();
            }
        });

        popupDialogExit.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    // 백키 눌렀을 때의 처리 (예: 다이얼로그 닫지 않음)
                    mBackPressedToExitOnce = false;
                    popupDialogExit.hide();
                    return true;  // true: 이벤트 소비 (다이얼로그 닫히지 않음)
                }
                return false;  // false: 기본 동작 수행 (다이얼로그 닫히기)
            }

        });

        // 팝업 다이얼로그 표시
        // popupDialog.show();
    }

    @Override
    public void showNetworkError(int errorCode, String description) {
        super.showNetworkError(errorCode, description);
//        if (Utils.hasPermission(this)) {
            String webUrlLocal = WUrl.ADDR_LOCAL_ASSET + "/error.html";
//            String webUrlLocal = WUrl.ReqId.WEB_ERROR.getReqUrl();
            WebView popup = activity.findViewById(R.id.popup);
            if (popup == null) {
                mWebview.loadUrl(webUrlLocal);
            } else {
                popup.loadUrl(webUrlLocal);
            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            PermissionUtil.stopHandler(MainActivity.this);
        } catch (Exception ex ){
            ex.printStackTrace();
        }

        if (MapUtil.isUISupport()) { // IUX9E@2281
/*TODO softm : asset/index.html이용하므로, 네트웍 연결 안될때 로직 제거
            if (iHTTPStatus != Constant.HTTP_CODE_SUCCESS && iHTTPStatus != 0) {
                showNetworkError(Constant.HTTP_CODE_NETWORK, getString(net.softm.lib.R.string.msg_network_not_connected));
            }
*/
        }
        /** 티맵자동실행 로직 */
//            if (Utils.hasPermissionForBasic(this)) {
//                if ( ServiceUtil.isDestory() ) { /** // TODO SOFTM : 20241215 꼭 필요함 : "initIntent.overlayViewUtil.mContext.bindService(" 로직에서
//                    // MapUtil.startConnectService ConnectService 가 중첩해서 동작하는것을 OverlayViewService가 unbindService를 안하고 동작한다고 판단하함
//                    // ServiceUtil.getConnectServiceIntent() == null 를 적용하면 에러 발생 안함. */
//                    if ( MapUtil.isAppEnable() ) {
//                        Fc.d(this, TAG, "MapUtil.startConnectService");
//                        MapUtil.startConnectService(this, null, null, false);
//                    }
//                }
//            }
//        }

        if (!MapUtil.isUISupport()) {
            if (MapUtil.isStartPower()) {
                btnStart.performClick();
            } else {
                btnStop.performClick();
            }
            NaviDestination defaultNaviMode = MapUtil.getNaviDestination();
            if (NaviDestination.HOME == defaultNaviMode) {
                btnHome.performClick();
            } else if (NaviDestination.COMPANY == defaultNaviMode) {
                btnCompany.performClick();
            } else if (NaviDestination.DRIVE == defaultNaviMode) {
                btnDrive.performClick();
            }
        }
    }

    @Override
    protected void onPause() {
        L.d(TAG,"onPause");
        super.onPause();

        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE
//                            && // 유연한 업데이트 사용 시 (AppUpdateType.FLEXIBLE) 사용
//                            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
//                  if ( Utils.hasPermissionForBasic(MainActivity.this) && MapUtil.isOverlayOpen() ) {
                    if ( Utils.hasPermissionForBasic(MainActivity.this) ) {
                        AlarmUtil.bindAlarm(WApplication.mInstance, new ParamCallback<List<PrayClass>>() {
                            @Override
                            public void onResult(List<PrayClass> result) {
                                if ( result.size() > 0 ) {
                                    L.d(TAG,"bindAlarm",TAG);
                                    Utils.toast(MainActivity.this, "기도모임이 있습니다.");
                                }
                            }
                        });
//                        if ( !OverlayViewService.isForeground ) {
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
                                if ( MapUtil.isAppEnable() ) {
                                    WApplication.mInstance.overlayViewServiceCallbackkt.run((params) -> {
                                        Log.i(TAG, "AU-Success-OverlayViewUtil.init(MainActivity.this).openOverlayViewShowOnly()");
                                        OverlayViewUtil.init(WApplication.mInstance).openOverlayViewShowOnly(false);
                                        return null;
                                    });
                                }
                        }
//                        }
//                        else {
//                            Log.i(TAG,"AU-Success-OverlayViewUtil.Exists");
//                        }
                    }
                } else {

                }
            }
        });

        appUpdateInfoTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//              if ( Utils.hasPermissionForBasic(MainActivity.this) && MapUtil.isOverlayOpen() ) {
                if ( Utils.hasPermissionForBasic(MainActivity.this) ) {
                    AlarmUtil.bindAlarm(WApplication.mInstance, new ParamCallback<List<PrayClass>>() {
                        @Override
                        public void onResult(List<PrayClass> result) {
                            if ( result.size() > 0 ) {
                                L.d(TAG,"bindAlarm",TAG);
                                Utils.toast(MainActivity.this, "기도모임이 있습니다.");
                            }
                        }
                    });
//                    if ( !OverlayViewService.isForeground ) {
//                        Callbackkt callbackkt = new Callbackkt(Const.NAVI_OVERLAY_COUNTDOWN_TIME+1000,"WorkQueue.nextTask;");
//                        callbackkt.run((param)->{
                            // TODO SOFTM : 중복실행 : StartNavi 실행로직과 중첩되어 실행이 되지 않는 상황이 발생함.
                            if ( !WorkExecutor.isExecuting ) {
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
                                if ( MapUtil.isAppEnable() ) {
                                 // WApplication.mInstance.overlayViewServiceCallbackkt.stop();
                                    WApplication.mInstance.overlayViewServiceCallbackkt.run((params) -> {
                                        Log.i(TAG, "AU-Failure-OverlayViewUtil.init(MainActivity.this).openOverlayViewShowOnly()");
                                        OverlayViewUtil.init(WApplication.mInstance).openOverlayViewShowOnly(false);
                                        return null;
                                    });
                                }
                            }
//                            return null;
//                        });
//                    }
//                    else {
//                        Log.i(TAG,"AU-Failure-OverlayViewUtil.Exists");
//                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        L.d(TAG,"onDestroy");
        super.onDestroy();
        NaviStatus naviStatus = (NaviStatus) getIntent().getSerializableExtra("P_NOTI_STATUS");
        String notificationDetails = getIntent().getStringExtra("P_NOTI_DETAIL");
//		if ( StringUtils.isEmpty(naviStatus) ) {
        if ( naviStatus == null || NaviStatus.NONE == naviStatus ) {
//			WApplication.mInstance.cancelAllNotification(); // 모든 노티 제거.
        }
        if ( WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.RESTART_SEARVICE) ||
             WApplication.mInstance.connectServiceMode.equals(WApplication.ConnectServiceMode.RESTART_SEARVICE_TEST)
        ) {
            if (serviceIntent != null) {
                stopService(serviceIntent);
                serviceIntent = null;
            }
        }
        WApplication.mainActivity = null;
//		WApplication.mInstance.sendNotification(getString(R.string.title_noti_runnig_title), "I" ); // 실행중
/** 20250117 : 방향 전환시 오류로 제거함
 *  20250117 : 최근앱 히스토리 문제
        if ( Utils.hasPermission(this) ) {
            finishAndRemoveTask();
        }
 */
    }

    String callbackGubun = null;
    String callbackFunc = null;
    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (PermissionUtil.onRequestPermissionsResult(MainActivity.this, permsRequestCode, permissions, grandResults)) {
            initHome();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PermissionUtil.onActivityResult(MainActivity.this,requestCode,resultCode,data);

        this.callbackGubun = "";
        this.callbackFunc = "";

        if ( requestCode == JavaScriptApi.API_103 ) {

            setClientForWebView();
            if ( data != null && data.getExtras() != null ) {
                String jsonData = data.getExtras().getString("jsonData");
                String callBackFunc = data.getExtras().getString("callBackFunc");
                if ( Utils.isNotEmpty(callBackFunc) ) {
                    JavaScriptBridge.callJavascriptFunc(callBackFunc);
                }
            }
        }

    }

    private void popupSnackbarForCompleteUpdate() {
        if (mAppUpdateManager != null){
            mAppUpdateManager.completeUpdate();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // 앱이 시작될 때 사용자 로그인 상태 확인
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.d("GoogleSignIn", "User is already signed in: " + account.getDisplayName());
        }

        mAppUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        Task<AppUpdateInfo> appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && // 유연한 업데이트 사용 시 (AppUpdateType.FLEXIBLE) 사용
                        // 무조건 업데이트 사용 시 (AppUpdateType.IMMEDIATE) 사용
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    Log.d("AppUpdate", "mAppUpdateManager.UPDATE_AVAILABLE");
                    // long APP_UPDATE_DELAY_TIME = 1000 * 60 * 60 * 24; // 1일의 밀리초 값
                    // long APP_UPDATE_DELAY_TIME = 1000 * 60 * 5; // 5분
                    long currentTime = System.currentTimeMillis();
                    checkTime = MapUtil.getAppUpdateCheckTime();
                    if (System.currentTimeMillis() - checkTime >= Const.APP_UPDATE_DELAY_TIME) {
                        Log.d("AppUpdate", "mAppUpdateManager.done_confirm");
                        confirm(MainActivity.this,MainActivity.this.getString(R.string.button_app_latest_update_now),getString(R.string.button_app_latest_update_later), MainActivity.this.getString(R.string.msg_app_latest_update, PackageUtil.getMyAppName(MainActivity.this) ), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d("AppUpdate", "mAppUpdateManager.Start App Update");
                                Utils.toast(MainActivity.this,"Start App Update");
                                // 업데이트가 사용 가능한 상태 (업데이트 있음) -> 이곳에서 업데이트를 요청해주자
                                try {
//                        AppUpdateOptions appUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build();
                                    AppUpdateOptions appUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build();
                                    mAppUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            // 현재 Activity
                                            MainActivity.this,
                                            // 유연한 업데이트 사용 시 (AppUpdateType.FLEXIBLE) 사용
                                            appUpdateOptions,
                                            // 전역변수로 선언해준 Code
                                            PermissionUtil.REQUEST_CODE_INAPP_UPDATE);
                                    Log.d("AppUpdate", "mAppUpdateManager.startUpdateFlowForResult");

                                } catch (IntentSender.SendIntentException e) {
                                    Log.e("AppUpdate", "AppUpdateManager Error", e);
                                    e.printStackTrace();
                                }
                            }
                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Utils.toast(MainActivity.this,R.string.msg_app_latest_update_later);
                                checkTime = System.currentTimeMillis();
                                MapUtil.setAppUpdateCheckTime(checkTime);
                            }
                        });
                    }
                } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate();
                    Log.d("AppUpdate", "InstallStatus.DOWNLOADED");
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                     // AppUpdateOptions appUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build();
                        AppUpdateOptions appUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build();
                        mAppUpdateManager.startUpdateFlowForResult(appUpdateInfo, MainActivity.this,appUpdateOptions, PermissionUtil.REQUEST_CODE_INAPP_UPDATE);
                        Log.d("AppUpdate", "mAppUpdateManager.startUpdateFlowForResult[UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS]");
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        });

        if ( mAppUpdateManager != null ) {
            mAppUpdateManager.registerListener(installStateUpdatedListener);
            Log.d("AppUpdate", "mAppUpdateManager.registerListener");
        }
    }

    InstallStateUpdatedListener installStateUpdatedListener = new
    InstallStateUpdatedListener() {
        @Override
        public void onStateUpdate(InstallState state) {
            if (state.installStatus() == InstallStatus.DOWNLOADED){
                popupSnackbarForCompleteUpdate();
            } else if (state.installStatus() == InstallStatus.INSTALLED){
                if (mAppUpdateManager != null){
                    mAppUpdateManager.unregisterListener(this);
                    Log.d("AppUpdate", "mAppUpdateManager.unregisterListener[onStateUpdate]");
                }
            } else {
                Log.d("AppUpdate", "onStateUpdate : " + state.installStatus());
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if ( mAppUpdateManager != null ) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
            Log.d("AppUpdate", "mAppUpdateManager.unregisterListener[onStop]");
        }
    }

    @BindView(R.id.txtMessage)
    @Nullable
    TextView txtMessage = null;
    @BindView(R.id.btnStart)
    @Nullable
    Button btnStart = null;
    @BindView(R.id.btnStop)
    @Nullable
    Button btnStop = null;
    private long checkTime;

    @OnClick({R.id.btnStart, R.id.btnStop})
    @Optional
    void onClick_btnStart(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackground(ContextCompat.getDrawable(this, R.drawable.button_gray));
        } else {
            v.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.button_gray));
        }
        switch (v.getId()) {
            case R.id.btnStart:
                //btnStop.setEnabled(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    btnStop.setBackground(ContextCompat.getDrawable(this, R.drawable.button_default));
                } else {
                    btnStop.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.button_default));
                }
                txtMessage.setText(Html.fromHtml(getString(R.string.start_message)));
                MapUtil.setStart(true);
                break;
            case R.id.btnStop:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    btnStart.setBackground(ContextCompat.getDrawable(this, R.drawable.button_default));
                } else {
                    btnStart.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.button_default));
                }
                txtMessage.setText(Html.fromHtml(getString(R.string.stop_message)));
                MapUtil.setStart(false);
                break;
            default:
        }
    }

    @BindView(R.id.btnNavi)
    @Nullable
    Button btnNavi = null;
    @OnClick({R.id.btnNavi})
    @Optional
    void onClick_btnNavi(View v) {
        MapUtil mapUtil = new MapUtil(this);
        showProgress();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopProgress();
            }
        }, 5000);

        MapUtil.setNotNaving();
        NaviDestination defaultNaviMode = MapUtil.getNaviDestination();
        if (NaviDestination.HOME == defaultNaviMode) {
            mapUtil.runToHome();
        } else if (NaviDestination.COMPANY == defaultNaviMode) {
            mapUtil.runToCompany();
        } else if (NaviDestination.DRIVE == defaultNaviMode) {
            mapUtil.runToDrive();
        }
    }

    @BindView(R.id.btnHome)
    @Nullable
    Button btnHome = null;
    @BindView(R.id.btnCompany)
    @Nullable
    Button btnCompany = null;
    @BindView(R.id.btnDrive)
    @Nullable
    Button btnDrive = null;

    @OnClick({R.id.btnHome, R.id.btnCompany, R.id.btnDrive})
    @Optional
    void onClick_btnHome(View v) {
        long currentTime = System.currentTimeMillis();
        switch (v.getId()) {
            case R.id.btnHome:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    btnCompany.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                    btnDrive.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                } else {
                    btnCompany.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                    btnDrive.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                }
                MapUtil.setNaviDestination(NaviDestination.HOME);
                break;
            case R.id.btnCompany:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    btnHome.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                    btnDrive.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                } else {
                    btnHome.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                    btnDrive.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                }
                MapUtil.setNaviDestination(NaviDestination.COMPANY);
                break;
            case R.id.btnDrive:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    btnHome.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                    btnCompany.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                } else {
                    btnHome.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                    btnCompany.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.chip_gray));
                }
                MapUtil.setNaviDestination(NaviDestination.DRIVE);
                break;
            default:
        }
    }

    private void doExecInstallReferrer(ParamCallback<PrayClass> paramCallback) {
        InstallReferrerClient referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                    try {

                        ReferrerDetails response = referrerClient.getInstallReferrer();
                        String referrerUrl = response.getInstallReferrer();
// referrerUrl = "inviteCode=abc123&campaign=spring2025"
                        L.d(TAG,"REFERRER", "referrer: " + referrerUrl);
                        String referrer = response.getInstallReferrer(); // 예: docId=abc123&campaign=spring2025
                        L.d(TAG, "Referrer", "Raw referrer: " + referrer);
                        if (referrer != null && !referrer.isEmpty()) {
                            // referrer 파라미터 파싱
                            Map<String, String> params = parseReferrer(referrer);

                            String docId = params.get("docId");
                            //                        String campaign = params.get("campaign");

                            L.d(TAG, "Referrer", "docId: " + docId);
                            //                        L.d(TAG, "Referrer", "campaign: " + campaign);
                            //                        L.d(TAG, "REFERRER", "초대코드: " + inviteCode + ", 캠페인: " + campaign);
                            PrayClass prayClass = new PrayClass();
                            prayClass.setDocId(docId);
                            paramCallback.onResult(prayClass);
                        } else {
                            // ⚠ 리퍼러가 없는 경우의 기본 진입 흐름
                            paramCallback.onResult(null);
                        }
                    } catch (RemoteException e) {
//                        e.printStackTrace();
                        L.e(TAG,"doExecInstallReferrer", e);
                        paramCallback.onResult(null);
                    }
                } else {
                    paramCallback.onResult(null);
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // retry if needed
            }
        });
    }
    private Map<String, String> parseReferrer(String referrer) {
        Map<String, String> params = new HashMap<>();
        if (referrer == null || referrer.isEmpty()) return params;

        String[] pairs = referrer.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                params.put(kv[0], kv[1]);
            }
        }
        return params;
    }

}