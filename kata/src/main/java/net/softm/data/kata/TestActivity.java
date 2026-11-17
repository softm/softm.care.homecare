package net.softm.data.kata;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.WorkInfo;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;

import net.softm.data.kata.common.AdType;
import net.softm.data.kata.common.BillingType;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.ParamCallback;
import net.softm.data.kata.common.ParamCallback2;
import net.softm.data.kata.common.ReceiverType;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.god.PrayClassAlarmReceiver;
import net.softm.data.kata.job.WorkExecutor;
import net.softm.data.kata.kt.Callkt;
import net.softm.data.kata.kt.Timerkt;
import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.BillingUtil;
import net.softm.data.kata.util.Fb;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.LicenseUtil;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.NotificationUtil;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.PackageUtil;
import net.softm.data.kata.util.PermissionUtil;
import net.softm.data.kata.util.ReviewUtil;
import net.softm.data.kata.util.TestUtil;
import net.softm.data.kata.util.Utils;
import net.softm.data.kata.util.WaitOverlayViewUtil;
import net.softm.data.kata.util.WorkerUtil;
import net.softm.data.kata.web.BasicActivity;
import net.softm.lib.common.WLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Optional;

/**
 * MainActivity
 * @author softm
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class TestActivity extends BasicActivity {
    final String TAG = "TestActivity";
    final Activity activity = this;

    @BindView(R.id.btnTest6)
    Button btnTest6;

    @BindView(R.id.editTextNumber)
    EditText editText;
    @OnTextChanged(R.id.editTextNumber)
    void onEditText(CharSequence text) {
        WApplication.mInstance.testValue = String.valueOf(text);
        WApplication.mInstance.setSharedPreferences("testValue", String.valueOf(text));
    }
    @BindView(R.id.editTextNumber1)
    EditText editText1;
    @OnTextChanged(R.id.editTextNumber1)
    void onEditText1(CharSequence text) {
        WApplication.mInstance.testValue1 = String.valueOf(text);
        WApplication.mInstance.setSharedPreferences("testValue1", String.valueOf(text));
    }
    @BindView(R.id.editTextNumber2)
    EditText editText2;
    @OnTextChanged(R.id.editTextNumber2)
    void onEditText2(CharSequence text) {
        WApplication.mInstance.testValue2 = String.valueOf(text);
        WApplication.mInstance.setSharedPreferences("testValue2", String.valueOf(text));
    }

    @BindView(R.id.editTextNumber3)
    EditText editText3;
    @OnTextChanged(R.id.editTextNumber3)
    void onEditText3(CharSequence text) {
        WApplication.mInstance.testValue3 = String.valueOf(text);
        WApplication.mInstance.setSharedPreferences("testValue3", String.valueOf(text));
    }

    @BindView(R.id.editTextNumber4)
    EditText editText4;
    @OnTextChanged(R.id.editTextNumber4)
    void onEditText4(CharSequence text) {
        WApplication.mInstance.testValue4 = String.valueOf(text);
        WApplication.mInstance.setSharedPreferences("testValue4", String.valueOf(text));
    }

    private long checkTime;

    BluetoothAdapter btAdapter;
    boolean timerStarted = false;
    @OnClick({R.id.btnHome, R.id.btnCompany, R.id.btnDrive, R.id.btnTest, R.id.btnTest2, R.id.btnTest3,R.id.btnTest4,R.id.btnTest5,R.id.btnTest6,
    R.id.btnAlarm,
    R.id.btnTest7, R.id.btnTest8,
    R.id.btnReviewTest, R.id.btnReviewTestReset,
    R.id.btnReviewTest3, R.id.btnReviewTest4,
    R.id.btnWorkerTestAdd, R.id.btnWorkerTestGet,R.id.btnWorkerTestGetAll, R.id.btnWorkerTestCancel,R.id.btnWorkerTestCancelAll,R.id.btnWorkerTestPruneWork,
    R.id.btnWaitOverlay,
    R.id.btnNoti,
    R.id.btnDriveTest,
    R.id.btnInit,
    R.id.btnTimerStart,
    R.id.btnTimerCancel,
    R.id.btnTestMode,
    R.id.btnIsAdLicense,
    R.id.btnTestSpanNoti,
    R.id.btnTestBillingConnect,
    R.id.btnTestBillingQuery1,
    R.id.btnTestBillingQuery2,
    R.id.btnTestBillingQuery3,
    R.id.btnTestPurchasesSubsList,
    R.id.btnTestPurchasesInAppList,
    R.id.btnTestPurchase1,
    R.id.btnTestPurchase2,
    R.id.btnTestPurchase3,
    R.id.btnTestPurchases01,
    R.id.btnTestPurchases02,
    R.id.btnTestPurchases03,
    R.id.btnTestAppUpdate,
    R.id.btnTestAppUpdateReset,
    R.id.btnTestQuerySkuDetails,
    R.id.btnGetTestVar,
    R.id.btnInitPaidError
    })
    @Optional
    void onClick_btnTests(View v) {
//        billingUtil = new BillingUtil(this);
        long currentTime = System.currentTimeMillis();
        switch (v.getId()) {
            case R.id.btnAlarm:
                // AlarmManager 인스턴스를 가져옵니다.
                AlarmManager alarmManager = (AlarmManager) TestActivity.this.getSystemService(Context.ALARM_SERVICE);

                // 알람을 받을 Receiver 인텐트를 설정합니다.
                Intent intent = new Intent(TestActivity.this, PrayClassAlarmReceiver.class); // 리셋할 리시버 설정

                // PendingIntent를 생성하여 알람을 설정합니다.
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        TestActivity.this,
                        0, // 요청 코드
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // PendingIntent의 플래그 설정
                );

                // 현재 시간을 기준으로 1분 후에 알람이 울리도록 Calendar를 설정합니다.
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.MINUTE, 1); // 현재 시간에서 1분 추가

                // 1분 간격으로 반복적인 알람을 설정합니다.
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP, // RTC_WAKEUP을 사용하여 디바이스가 잠금 해제될 때도 알람이 울리도록 합니다.
                        calendar.getTimeInMillis(), // 알람이 울릴 첫 번째 시간
                        1 * 60 * 1000L, // 1분 간격 (밀리초 단위)
                        pendingIntent // 알람이 발생할 때 실행할 PendingIntent
                );
                // 1747814872340

                break;
            case R.id.btnTestPurchases03:
//                billingUtil.updatePayInformationFromPurchase(BillingType.SUBS,(purchase)->{
//                    if ( purchase == null ) Utils.toastDebug(TestActivity.this,"구독정보 없음");
//                });
//                billingUtil.updatePayInformationFromPurchase(BillingType.INAPP_ONETIME,(purchase)->{
//                    if ( purchase == null ) Utils.toastDebug(TestActivity.this,"평생정보 없음");
//                });
                billingUtil.updateUserLicenseFromPayInApp(TestActivity.this,
                    new ParamCallback2<Purchase,Purchase> () {
                        @Override
                        public void onResult(Purchase subsPurchase,Purchase lifeTimePurchase) {
//                            if ( subsPurchase != null ) alert("구독정보 : " + subsPurchase.getPurchaseToken() );
//                            else alert("구독정보 : 없음");
//                            if ( lifeTimePurchase != null ) alert("평생정보 : "  + subsPurchase.getPurchaseToken() );
//                            else alert("평생정보 : 없음");
                            alert(String.format("구독 : %s, 평생 : %s",subsPurchase != null,lifeTimePurchase != null));
                            LicenseUtil.fixUserType();
                        }

                        @Override
                        public void onError(Exception ex) {
                            alert(ex.getMessage());
                        }
                    }
                );
                break;
            case R.id.btnTestPurchases02:
                billingUtil.getIsSubscribed().getValue();
                billingUtil.getIsSubscribed().observe(this, isSubscribed -> {
                    if (isSubscribed) {
                        alert("구독됨");
                    } else {
                        alert("구독중지");
                    }
                });
                break;
            case R.id.btnTestPurchases01:
//                billingService.purchase("donate__01",this);
//                String conSumeToken = "jcmcfoeieodbacophmolhfjg.AO-J1OyqSz-nrsdIXnTvpqAFhm--nG07piSszlVvagtccqofXRlZic4kMiaipL3c22iakPEaQTucp5sjICM_Ffg1e7Wg-fBkYv7HKXofAgUrTm030iVy4gc";
//                String conSumeToken = "nnjakhjmcnjjobfdbfaajlnf.AO-J1OwuWo0nlRxfafuvbAATv10JHP0wOnojlwb3pWUA4esRCVmyYPTb8O6igExMEHl-O6LXu7_okg93Epm_DLox-uVHndnTZ4ST_z3p3Bnk0U5W1mNk0U8";
//                String conSumeToken = "chenccaipeppalcajgmdllgn.AO-J1Oxg59jFE_3I9sASeMK3Fck-aR2hcKG35Fad2oR99NRosrNfBt5pxE70PvoDtg_bb4RQwJ6q9S1FEn3o8lCeI-fvr1B0YOflyNRLA7fnyeRSFcQ8at8";
                String conSumeToken = "jhompanfbpiafjbhmfmdihom.AO-J1Oxfyqcwl5kH59XE9oy4Jdg3mslg6yq0btc0F5r2GSn8kBvr-LdyJmTOXy0Xf9GrDFDTEgpLetUuQNj4Fgz-6WWwBFvd0ehTbWRntfbdBmBAmc8Eox4";
                Utils.toast(this,"소비.취소 : " + conSumeToken);
                billingUtil.consumePurchase(conSumeToken);
//                billingUtil.consumePurchase("aelhlnhpkagmodkbohkhinji.AO-J1OyMytiB1g1rNO9P9wGjx1GEYqohGFWyJePfsEV_Y-tOs8u0t86i_2TpKoFFg8WHhKlWfEf2pmklOyiH42nGgIgw2D9AA7hKbBr8tuaBGDgCjB0IB2w");
                break;
            case R.id.btnTestPurchasesSubsList:
//                billingService.purchase("donate__01",this);
                billingUtil.queryPurchases(BillingClient.ProductType.SUBS,new ParamCallback<Purchase>() {
                    @Override
                    public void onResult(Purchase purchase) {
                        alert("Purchase: " + ( purchase != null?purchase.getProducts().get(0).toString():""));
                    }
                });
                break;
            case R.id.btnTestPurchasesInAppList:
//                billingService.purchase("donate__01",this);
                billingUtil.queryPurchases(BillingClient.ProductType.INAPP,new ParamCallback<Purchase>() {
                    @Override
                    public void onResult(Purchase purchase) {
                        alert("Purchase: " + ( purchase != null?purchase.getProducts().get(0).toString():""));
                    }
                });
                break;
            case R.id.btnTestPurchase1:
//                billingService.purchase("donate__01",this);
                billingUtil.payInApp(TestActivity.this,BillingType.INAPP_CONSUME);
                break;
            case R.id.btnTestPurchase2:
//                billingService.purchase("donate__01",this);
                billingUtil.payInApp(TestActivity.this, BillingType.SUBS);
                break;
            case R.id.btnTestPurchase3:
//                billingService.purchase("donate__01",this);
                billingUtil.payInApp(TestActivity.this,BillingType.INAPP_LIFETIME);
                break;
            case R.id.btnTestBillingConnect:
                billingUtil.startConnection();
                break;
            case R.id.btnTestBillingQuery1:
                billingUtil.queryProductDetails(BillingType.INAPP_CONSUME,new ParamCallback<ProductDetails>() {
                    @Override
                    public void onResult(ProductDetails productDetails) {
                        alert("productDetails: " + productDetails.getTitle());
                    }
                });
                break;
            case R.id.btnTestBillingQuery2:
                billingUtil.queryProductDetails(BillingType.SUBS,new ParamCallback<ProductDetails>() {
                    @Override
                    public void onResult(ProductDetails productDetails) {
                        alert("productDetails: " + productDetails.getTitle());
                    }
                });
                break;
            case R.id.btnTestBillingQuery3:
                billingUtil.queryProductDetails(BillingType.INAPP_LIFETIME,new ParamCallback<ProductDetails>() {
                    @Override
                    public void onResult(ProductDetails productDetails) {
                        alert("productDetails: " + productDetails.getTitle());
                    }
                });
                break;
            case R.id.btnTestQuerySkuDetails:

                List<String> skuList = new ArrayList<>();
                //        skuList.add("subs_01"); // donate__01
                //        skuList.add("donate__01"); // donate__01
                skuList.add("inapp_lifetime"); // donate__01

//                billingUtil.querySkuDetails(skuList);
                break;
            case R.id.btnTestSpanNoti:
                TestUtil.createNotificationChannel(this);
                TestUtil.showNotification(this);
                break;
            case R.id.btnIsAdLicense:
                alert(LicenseUtil.isLicense()?"true":"false");
                break;
            case R.id.btnTestMode:
                TestUtil.testMode(this);
                break;
            case R.id.btnTimerStart:
                timerStarted = true;
                new Timerkt(30, (remainingTime, gubun, timer) -> {
//                    if (PermissionUtil.hasIgnoreBatteryPermissions()) {
                        if (!timerStarted) {
                            new Callkt(gubun).run((params)->{
                                return null;
                            });
                            timer.stop();
                        }
                        TextView tvTimerTick = findViewById(R.id.tvTimerTick);
                        tvTimerTick.setText(String.valueOf(remainingTime));
//                    }
                    return null;
                }, (params) -> {
                    L.log(this, TAG, "Timerkt.complete");
                    return null;
                }, "Test.Timer").run();
//                adb -s adb-R54W8010S4F-67hqlo._adb-tls-connect._tcp. shell pm revoke net.softm.data.kata android.permission.ACCESS_FINE_LOCATION

                Utils.toast(this,"프리퍼런스 클리어됨");

//
                break;
            case R.id.btnTimerCancel:
                timerStarted = !timerStarted;
                Button btnTimerCancel = findViewById(R.id.btnTimerCancel);
                btnTimerCancel.setText(timerStarted?"Timer.실행중":"Timer.종료");
//                tvTimerTick
                break;
            case R.id.btnGetTestVar:
                AdUtil.getAdViewErrorForGoogleCaseIndex();
                alert(String.valueOf(AdUtil.isGoogleAdViewErrorOccurred()));
                LicenseUtil.getPaidErrorForGoogleCaseIndex();
                alert(String.valueOf(LicenseUtil.isGooglePaidErrorOccurred()));
                break;
            case R.id.btnInitPaidError:
                LicenseUtil.setPaidErrorForGoogle(false);
                LicenseUtil.setPaidErrorForGoogleCaseIndex(-1);
                Utils.goMain(this);
                Utils.toast(this,"PaidError 초기화");
                break;
            case R.id.btnInit:
//                Utils.clearAllPreferences(this);
//                Utils.resetAppData(this);
                MapUtil.setFirstStart(false);
                MapUtil.setAppInstalled(false);
                MapUtil.setTest("0");
                AdUtil.setAdViewErrorForGoogle(false);
                AdUtil.setAdViewErrorForGoogleCaseIndex(-1);
                LicenseUtil.setPaidErrorForGoogle(false);
                LicenseUtil.setPaidErrorForGoogleCaseIndex(-1);
                MapUtil.setPostNotificationsConfirmCount("0"); // MapUtil.getPostNotificationsConfirmCount();
                Fb.setLastCheckLiveUserLogTime(0);
                Utils.goMain(this);
                Utils.toast(this,"프리퍼런스 클리어됨");
                break;
            case R.id.btnDriveTest:
                (new AcsUtil(this)).safeDrive(this,"안전운행","안전운행", 3, false);

//                // 대상 앱의 패키지명으로 Intent 생성
//                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.app");
//                if (launchIntent != null) {
//                    // 새 태스크를 생성하지 않고, 이미 실행 중인 태스크를 전면으로 가져오기 위한 플래그 설정
//                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                    // 인텐트를 통해 액티비티 시작
//                    startActivity(launchIntent);
//                } else {
//                    // 앱을 찾을 수 없는 경우, 사용자에게 알림
//                    Toast.makeText(this, "앱을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show();
//                }
//
                break;
            case R.id.btnNoti:
                Notification notification = NotificationUtil.init(this).
                        send("test : " + Math.random(), "메시지",true);
                break;
            case R.id.btnWaitOverlay:
                WaitOverlayViewUtil.init(this).initIntent().show(AcsUtil.SHOW_WAITING_OVERLAY,MainConnectReceiver.ACTION_SOFTM_POWER_CONNECTED, ReceiverType.POWER,3, false,false);
                break;
            case R.id.btnReviewTest:
                ReviewUtil.popupReview(this);
                if ( !ReviewUtil.isReviewRequestAvailable() ) {
                    Utils.toast(this,"Review Avaliable : " + (ReviewUtil.isReviewRequestAvailable()?"true":"false"));
                }
                break;
            case R.id.btnReviewTestReset:
                ReviewUtil.setReviewRequestCheckTime(0);
                break;
            case R.id.btnReviewTest3:
                Utils.toast(this,"Review Avaliable : " + (ReviewUtil.isReviewRequestAvailable()?"true":"false"));
                break;

            case R.id.btnReviewTest4:
                long reviewCheckTime = ReviewUtil.getReviewRequestCheckTime();
                long remindTime = 0;
                if ( reviewCheckTime == 0 ) {
                    remindTime = MapUtil.getAppUpdatedTime()  + ReviewUtil.getReviewRequestFirstIntervalTime() - System.currentTimeMillis();
                } else {
                    remindTime = reviewCheckTime + ReviewUtil.getReviewRequestIntervalTime() - System.currentTimeMillis();
                }
                Utils.toast(this,"Review 호출 가능까지 : " +Utils.millisecondsToTime(remindTime));
                break;

            case R.id.btnTest4:

                AcsUtil acsUtil = new AcsUtil(this);
                acsUtil.forceStop();

                break;
            case R.id.btnTestAppUpdateReset:
                confirm(TestActivity.this,  "초기화활까요?", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MapUtil.setAppUpdateCheckTime(0L);
                        Utils.toast(TestActivity.this,"초기화됨");
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
                break;
            case R.id.btnTestAppUpdate:
                // long APP_UPDATE_DELAY_TIME = 1000L * 60 * 60 * 24; // 1일의 밀리초 값
                // long APP_UPDATE_DELAY_TIME = 1000L * 60 * 5; // 5분
                checkTime = MapUtil.getAppUpdateCheckTime();
                if (System.currentTimeMillis() - checkTime >= Const.APP_UPDATE_DELAY_TIME) {
                    confirm(TestActivity.this, TestActivity.this.getString(R.string.button_app_latest_update_now),getString(R.string.button_app_latest_update_later), TestActivity.this.getString(R.string.msg_app_latest_update, PackageUtil.getMyAppName(TestActivity.this) ), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Utils.toast(TestActivity.this,"업데이트 실행");
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Utils.toast(TestActivity.this,"업데이트 미룸");
                            checkTime = System.currentTimeMillis();
                            MapUtil.setAppUpdateCheckTime(checkTime);
                        }
                    });
                } else {
                    String formatTime = Utils.millisecondsToTime(Const.APP_UPDATE_DELAY_TIME - ( System.currentTimeMillis() - checkTime));
                    Utils.toast(activity, "업데이트 남음 : " + formatTime);
                    //Utils.toast(MainActivity.this,"업데이트 남음 : " + (System.currentTimeMillis() - checkTime - oneDayMillis)/(1000 * 60));
                }
                break;
            case R.id.btnTest6:
                /* TODO SOFTM : 주기성으로 혹시 앱의 접근성을 야기하지 않는가 해서 조치함 */
                // long APP_UPDATE_DELAY_TIME = 1000 * 60 * 60 * 24; // 1일의 밀리초 값
                // long APP_UPDATE_DELAY_TIME = 1000 * 60 * 5; // 5분
/*
 */
//                RewardedAd rewardedAd= AdUtil.getRewardedAd();
                if ( AdUtil.isAdingWaitReward() && AdUtil.isAdViewAvailable() ) {
                    AdUtil.loadRewardAd(TestActivity.this, AdType.REWARDEDINTERSTITIAL,()->{
                    },()->{
                        new Callkt("AdUtil.initRewardAd.endCallback").run((param)->{
                            AdUtil.show(TestActivity.this, AdType.REWARDEDINTERSTITIAL);
                            return null;
                        });
                    });
                } else {
                    if ( !AdUtil.isAdViewAvailableForIntervalTime() ) {
                        Utils.toastDebug(TestActivity.this, "광고 재시청 까지 :" + Utils.millisecondsToTime(AdUtil.getNextAdviewTime()));
                    } else if ( !AdUtil.isAdViewAvailableForAdMaxTime() ) {
                        alert("광고 시청시간 초과로 시청이 제한 되었습니다.", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });
                        L.d(TAG, "광고 시청시간 초과로 시청이 제한 되었습니다.");
                    }
                }

                if ( !AdUtil.isAdViewAvailableForAdMaxTime() ) {
                    alert("광고 시청시간 초과로 시청이 제한 되었습니다.",new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            String formatTime = Utils.millisecondsToTime(AdUtil.getNextAdviewTime());
                            Utils.toast(activity, "광고 재시청 까지 : " + formatTime);
                        }
                    });
                    L.d(TAG,"광고 시청시간 초과로 시청이 제한 되었습니다.");
                } else {
                }
//                mInterstitialAdForTimerkt = new Timerkt(10,(remainingTime, gubun)->{
//                    if (!mInterstitialAdForShowed) {
//                        if (mRewardedAd != null) {
//                            mInterstitialAdForShowed = true;
//                            mRewardedAd.show(this);
////						DummyActivity.this.finish();
//                        } else {
//                            Log.d(TAG, "The interstitial ad wasn't ready yet.");
//                        }
//                        L.log(this, TAG, "Timerkt.tick", String.format("%02d", remainingTime));
//                    }
//                    return null;
//                },(params)->{
//                    L.log(this,TAG,"Timerkt.complete");
//                    return null;
//                },"ObserverViewService");
//                mInterstitialAdForTimerkt.run();
                break;
            case R.id.btnTest7:
                confirm(TestActivity.this, "광고보기를 초기화 하시겠습니까?", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AdUtil.setAdViewCheckTime(0);
                        LicenseUtil.setLicenseExpireTime(0);
                        AdUtil.loadRewardAd(TestActivity.this, AdType.REWARDEDINTERSTITIAL,()->{
                            btnTest6.setEnabled(false);
                        },()->{
                            new Callkt("AdUtil.initRewardAd.endCallback").run((param)->{
                                btnTest6.setEnabled(true);
                                return null;
                            });
                        });
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
                break;
            case R.id.btnTest8:
                Utils.toast(TestActivity.this,"만료일 : " + Utils.convertMillisecondsToDate(LicenseUtil.getLicenseExpireTime()) +
                                "/ 만료일까지 : " + Utils.millisecondsToTime(LicenseUtil.getLicenseExpireTime()-System.currentTimeMillis())
                        );
                break;
            case R.id.btnWorkerTestAdd:
                WorkerUtil.ifStopStartCheckService(this);
                break;
            case R.id.btnWorkerTestGet:
                WorkInfo.State wState = WorkerUtil.getCheckServiceWorkInfoState(this);
                String wId = WorkerUtil.getCheckServiceWorkInfoId(this);
                alert(wState.name() + "\n" + wId);
                break;
            case R.id.btnWorkerTestGetAll:
                alert(WorkerUtil.getAllState(this));
                break;
            case R.id.btnWorkerTestCancel:
                WorkerUtil.cancelCheckService(this);
                break;
            case R.id.btnWorkerTestCancelAll:
                WorkerUtil.cancelAll(this);
                break;
            case R.id.btnWorkerTestPruneWork:
                WorkerUtil.pruneWork(this);
                break;
            case R.id.btnTest3:
/* // TODO softm : RuntimeException
*/
//                try {
                    String nu = null;
                    nu.charAt(0);
//                    throw new RuntimeException("Error");
//                } catch (Exception ex) {

//                }

//                Utils.goPermission(this,"accessibility_reset",null);
//                AcsUtil ascUtil = new AcsUtil(this);
//                ascUtil.sendEvent(AcsUtil.UNCAUGHT_EXCEPTION);
//                FakeAppUpdateManager fakeUpdateManager = new FakeAppUpdateManager(this);
//
//                fakeUpdateManager.setUpdateAvailable(123);
//                fakeUpdateManager.userAcceptsUpdate();
//                onResume();
//                fakeUpdateManager.completeUpdate();
                break;
            case R.id.btnTest2:
                String formatTime = Utils.millisecondsToTime((AdUtil.getAdViewCheckTime() + AdUtil.getAdViewIntervalTime()) - System.currentTimeMillis());
                Utils.toast(TestActivity.this, "광고 재시청 까지 : " + formatTime);

/*
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_SECURITY_SETTINGS);
//                intent.setData(Uri.parse("package:" + (WApplication.mInstance).getPackageName()));
                startActivity(intent);

//                Intent oIntent = new Intent(this, ConnectAccessibilityService.class);
//                startService(oIntent);
                Settings.Secure.putString(getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "net.softm.data.kata/ConnectAccessibilityService");
                Settings.Secure.putString(getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_ENABLED, "1");
*/
                break;
            case R.id.btnTest:
//                long rnd = Math.random();
                Random random = new Random();
                int randomNumber = random.nextInt(10) + 1; // 1부터 10 사이의 정수 생성
                System.out.println("랜덤 숫자: " + randomNumber);
                alert((ConnectAccessibilityService.mContext==null?"ConnectAccessibilityService.mContext==null":"ConnectAccessibilityService.mContext!=null") + "\n" +
                        "ascUtil.isAcsServiceRunning() : " + PermissionUtil.isAccessibilityServiceRunning(this) + "\n"+
                        "PermissionUtil.hasAccessibilityPermissions() : " + PermissionUtil.hasAccessibilityPermissions() + "\n" +
                        "PermissionUtil.hasAccessibilityPermissions2() : " + PermissionUtil.hasAccessibilityPermissions2() + "\n" +
                        "PermissionUtil.isAccessibilityServiceEnabled() : " + PermissionUtil.isAccessibilityServiceEnabled(this,"net.softm.data.kata.ConnectAccessibilityService") + "\n" +
                        "MapUtil.isAppInstalled() : " + MapUtil.isAppInstalled() + "\n" +
                        "randomNumber : " + randomNumber + "\n"
                );
//                FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
//                crashlytics.setCustomKey("gubun","MainActivity.onClick_btnHome");
//                crashlytics.log("MainActivity.onClick_btnHome.01");

//                ServiceUtil.stop();
//                toast("ServiceUtil.stop();");
//                AcsUtil ascUtil = new AcsUtil(this);
//                ascUtil.sendEvent(AcsUtil.KEY_GLOBAL_ACTION_RECENTS, new RunnableArg() {
//                    // addEvent(EVENT_FORCE_STOP_DIALOG_OK_CLICK,
//                    @Override
//                    public void run() {
//                        //
//                        ascUtil.sendEvent(AcsUtil.EVENT_STOP_RECENTS, new RunnableArg() {
//                            @Override
//                            public void run() {
//                                if (true) {
//                                    Log.d("ACS", "recents_stop.completed.nextTask");
//                                    WorkQueue.nextTask();
//                                }
//                            }
//                        }, 1000);
//                    }
//                },500);
//
//
//                sendBroadcast(new Intent(MainConnectReceiver.ACTION_SOFTM_POWER_CONNECTED));
//                    WorkExecutor.init(this);
//                    WorkExecutor.start();
// TODO softm : Quick Settings Mobile Data mode on / off
//                DeviceUtil.mobileDataEnable(this,!Connectivity.isMobileDataConnected(this),false);
// TODO softm : Quick Settings Wifi mode on / off
//                DeviceUtil.wifiEnable(this,!DeviceUtil.isWifiEnable(this),false);

// TODO softm : Quick Settings Airplane mode on / off
//                AcsUtil acsUtil = new AcsUtil(this);
//                DeviceUtil.tetheringEnable(this,!DeviceUtil.isApOn(this),false);

// TODO softm : Deviceutil.tethering mode on / off
//                WifiApUtil wifiApUtil = new WifiApUtil(MainActivity.this);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    wifiApUtil.enableLocationSettings();
//                }
//                AcsUtil acsUtil = new AcsUtil(this);
//                DeviceUtil.tetheringEnable(this,!DeviceUtil.isApOn(this));
////                acsUtil.setQuickSettings(this, StartStopType.AIRPLANE_MODE,!DeviceUtil.isAirplaneModeOn(this));

// TODO softm : Quick Settings Airplane mode on / off
//                AcsUtil acsUtil = new AcsUtil(this);
//                DeviceUtil.airplaneModeEnable(this,!DeviceUtil.isAirplaneModeOn(this));
////                acsUtil.setQuickSettings(this, StartStopType.AIRPLANE_MODE,!DeviceUtil.isAirplaneModeOn(this));

// TODO softm : Quick Settings Airplane mode on / off
//                AcsUtil acsUtil = new AcsUtil(this);
//                acsUtil.setQuickSettings(this, StartStopType.AIRPLANE_MODE,!DeviceUtil.isAirplaneModeOn(this));

// TODO softm : start
//                if ( DeviceUtil.isAirplaneModeOn(this) ) {
//                    WorkExecutor.init(this);
//                    WorkExecutor.start();
//                } else {
//                    WorkExecutor.init(this);
//                    WorkExecutor.stop();
//                }
// TODO softm : stop
//                WorkExecutor.init(this);
//                WorkExecutor.stop();
// TODO softm : StringsXmlValueUtil
//                List<String> values = StringsXmlValueUtil.getValuesNEnValues(this,"airplane_mode");
//                Log.d(TAG, String.valueOf(values.size()));
// TODO softm : forceStop
//                AcsUtil acsUtil = new AcsUtil(this);
//                acsUtil.forceStop();

// TODO softm : Setting Control - Wifi
//                AcsUtil acsUtil = new AcsUtil(this);
//                acsUtil.setSettingsControl(this, StartStopType.WIFI, !DeviceUtil.isWifiEnable(this),false);

// TODO softm : Quick Settings Show
//                AcsUtil acsUtil = new AcsUtil(this);
//                acsUtil.sendEvent(AcsUtil.KEY_GLOBAL_ACTION_QUICK_SETTINGS);
// TODO softm : wifiEnable
//                DeviceUtil.wifiEnable(MainActivity.this,true);
// TODO softm : Reload page
//                mWebview.reload();

// TODO softm : Wating Overlay Gesture
/*
                Point point = new Point();
                getWindowManager().getDefaultDisplay().getRealSize(point);
                int width = point.x;
                int height = point.y;

                AcsUtil acsUtil = new AcsUtil(this);
                acsUtil.sendEventWaitingOverlay(ConnectReceiver.ACTION_SOFTM_POWER_CONNECTED, ReceiverType.NONE,(long)(1000));
                float startX = width-100;
                float startY = height-300;
                float endX = width-100;
                float endY = height-900;
                acsUtil.sendEvent(AcsUtil.GESTURE_SWIPE_TEST,null, new String[]{String.valueOf(startX),String.valueOf(startY),String.valueOf(endX),String.valueOf(endY)},1500);
*/
// TODO softm : WIFI
/*
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

                BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context c, Intent intent) {
                        boolean success = intent.getBooleanExtra(
                                WifiManager.EXTRA_RESULTS_UPDATED, false);
                        if (success) {
                            // 스캔 성공시 저장된 list를 recycler view를 통해 보여줌
                            List<ScanResult> results = wifiManager.getScanResults();

                            Log.d("wifi", "scan success");

                                mWebView.evaluateJavascript(javascript, resultCallback);

                            StringBuffer st = new StringBuffer();
                            for(ScanResult r : results ){
                                Log.d("wifi",""+r);
                                st.append(r.SSID);
                                st.append("\n");
                            }

                        } else {
                            // scan failure handling
                            // handle failure: new scan did NOT succeed
                            Log.d("wifi", "scanFailure");
                            Utils.toast(MainActivity.this,"wifi scan에 실패하였습니다.");
                            // consider using old scan results: these are the OLD results!
                            List<ScanResult> results = wifiManager.getScanResults();
                            // potentially use older scan results ...
                            StringBuffer st = new StringBuffer();
                            for(ScanResult r : results ){
                                Log.d("wifi",""+r);
                                st.append(r.SSID);
                                st.append("\n");
                            }
                        }
                    }
                };

                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                registerReceiver(wifiScanReceiver, intentFilter);

                boolean success = wifiManager.startScan();
                if (!success) {
                    // scan failure handling
                    // handle failure: new scan did NOT succeed
                    Log.d("wifi", "scanFailure");
                    Utils.toast(MainActivity.this,"wifi scan에 실패하였습니다.");
                    // consider using old scan results: these are the OLD results!
                    List<ScanResult> results = wifiManager.getScanResults();
                    // potentially use older scan results ...
                    StringBuffer st = new StringBuffer();
                    for(ScanResult r : results ){
                        Log.d("wifi",""+r);
                        st.append(r.SSID);
                        st.append("\n");
                    }
                }
*/

// TODO softm : Bluetooth
/*
                btAdapter = BluetoothAdapter.getDefaultAdapter();
                Log.d(TAG, "\n어댑터: " + btAdapter);

                if (btAdapter == null) {
                    Log.d(TAG, "\n블루투스가 지원되지 않습니다.");
                    return;
                } else {
                    if (btAdapter.isEnabled()) {
                        Log.d(TAG, "\n블루투스 사용 가능...");
                        Log.d(TAG, "\n현재 페어링된 기기:");
                        if (ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                            Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
                            for (BluetoothDevice device: devices) {
                                Log.d(TAG,"\n 디바이스: " + device.getName() + ", " + device);
                            }
                        }
                    } else {
//                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                }
*/
// TODO softm : Ap Detail
//                Utils.goAppDetailSettings(this, MapUtil.PACKAGE_SKT_MAP);

//                Locale current = getResources().getConfiguration().locale;

//                conf.locale = new Locale("pl");
//                DisplayMetrics metrics = new DisplayMetrics();
//                getWindowManager().getDefaultDisplay().getMetrics(metrics);
//                Resources resources = new Resources(getAssets(), metrics, conf);
//                /* get localized string */
//                int msgId =Integer.valueOf("2681771622136916280");
//                String str = resources.getString(msgId);

//                  String localeStr = current.getLanguage();
//                  String[] forceStopInfo = FORCE_STOP_INFO.get(localeStr)[1];
//                  for (String forceStop : forceStopInfo ) {
//                      Log.i(TAG,"forceStop:" + forceStop);
//                  }

//
//                Intent intent = new Intent();
//                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
////                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                intent.addCategory(Intent.CATEGORY_DEFAULT);
//                intent.setData(Uri.parse("package:" + MapUtil.PACKAGE_SKT_MAP));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                startActivity(intent);

//                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                Uri uri = Uri.fromParts("package", MapUtil.PACKAGE_SKT_MAP, null);
//                intent.setData(uri);
//                startActivity(intent);

//                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                intent.setData(Uri.parse("package:"+MapUtil.getAppPackage()));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);


//                Intent intent = new Intent();
//                intent.setAction(
//                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                Uri uri = Uri.fromParts("package",
//                        BuildConfig.APPLICATION_ID, null);
//                intent.setData(uri);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);



//
//                  AcsUtil ascUtil = new AcsUtil(this);
//                  ascUtil.appInfoForceStop();

//                  alert("Force Stop : " + localeStr + " / " + FORCE_STOP_INFO.get(localeStr)[1][0].toString());
//                  Log.i(TAG,"Force Stop : " + localeStr + " / " + FORCE_STOP_INFO.get(localeStr)[0]);
//                Intent oIntent = new Intent(this, OverlayCommanderService.class);
////            oIntent.putExtra("P_REFENCE", refence);
//                startService(oIntent);

//                Utils.goAppDetailSettings(this, MapUtil.PACKAGE_SKT_MAP);

//                View rootView = getWindow().getDecorView();

//                File screenShot = ScreenShot(rootView);
//                if(screenShot!=null){
//                    //갤러리에 추가
//                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
//                }


//                int chk = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                if(chk == PackageManager.PERMISSION_DENIED); {
//                    // -1은 deny//GPS 권한도
//                    String[] arr = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}; // EXTERNAL_STORAGE
//                    ActivityCompat.requestPermissions(this, arr, 3000 );
//                }

/*                ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                Method forceStopPackage = null;
                try {
                    forceStopPackage =am.getClass().getDeclaredMethod("forceStopPackage",String.class);
                    forceStopPackage.setAccessible(true);

                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        forceStopPackage.invoke(am, "com.skt.tmap.ku");
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
*/

//                    ActivityManager actMng = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
//                    actMng.killBackgroundProcesses("com.skt.tmap.ku");
//
//                List<ApplicationInfo> packages;
//                PackageManager pm;
//                pm = getPackageManager();
//                //get a list of installed apps.
//                packages = pm.getInstalledApplications(0);
//
//                ActivityManager mActivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
//
//                for (ApplicationInfo packageInfo : packages) {
//                    if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM)==1)continue;
//                    if(packageInfo.packageName.equals("com.skt.tmap.ku")) {
//
//                        mActivityManager.killBackgroundProcesses(packageInfo.packageName);
//
//                    }
//                }
//
//                amKillProcess("com.skt.tmap.ku");

                break;
            default:
        }
    }

    private AppUpdateManager mAppUpdateManager;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        L.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        OverlayViewUtil.stopService();

//        RestUtil.getBasicInfo(this,()->{
//        });

        setContentView(R.layout.activity_test_main);
        ButterKnife.bind(this);
        init();
        initHome();

        ((Button)findViewById(R.id.btnTestMode)).setText(MapUtil.isTest()?"Test.On":"Test.OFF");
        editText.setText(String.valueOf(WApplication.mInstance.testValue));
        editText1.setText(String.valueOf(WApplication.mInstance.testValue1));
        editText2.setText(String.valueOf(WApplication.mInstance.testValue2));
        editText3.setText(String.valueOf(WApplication.mInstance.testValue3));
        editText4.setText(String.valueOf(WApplication.mInstance.testValue4));

        // GoogleSignInClient 설정
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        // 앱이 시작될 때 자동으로 로그인된 계정 확인
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            String accountName = account.getDisplayName(); // 계정 이름
            String accountEmail = account.getEmail(); // 계정 이메일
            // 로그인된 계정 정보 처리
        } else {
            // 로그인되지 않은 상태일 경우
            // 로그인 화면으로 리다이렉션
/**
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 1);
*/
        }

//        getGoogleAccount(this);

//        // Google Sign-In 옵션 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()  // 이메일 요청
                .build();

        // GoogleSignInClient 초기화
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (account != null) {
            Log.d("GoogleSignIn", "User is already signed in: " + account.getDisplayName());
        } else {
            Log.d("GoogleSignIn", "User is not signed in.");
        }

        if (account != null) {
            // 로그인된 계정의 정보 얻기
            String accountName = account.getDisplayName();
            String accountEmail = account.getEmail();

            // 정보 출력
            System.out.println("Logged-in Account: " + accountName);
            System.out.println("Email: " + accountEmail);
        } else {
            System.out.println("No account is signed in.");
        }
        getGoogleAccount(this);

//        billingUtil = new BillingUtil(this);
//        billingUtil.startConnection();

//        billingService = new BillingService(this);

        if ( WApplication.mInstance.billingUtil != null ) {
            billingUtil = WApplication.mInstance.billingUtil;
        } else {
            billingUtil = BillingUtil.getInstance(this);
        }

    }

//    BillingService billingService;
    BillingUtil billingUtil;

    public static String getGoogleAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByType("com.google");
//        Account[] accounts = accountManager.getAccounts();

        if (accounts.length > 0) {
            return accounts[0].name; // 첫 번째 Google 계정 리턴
        } else {
            return null; // Google 계정이 없으면 null 리턴
        }
    }

    private GoogleSignInClient mGoogleSignInClient;

    private BillingClient billingClient;

//    public void setupBillingClient(Context context) {
//        billingClient = BillingClient.newBuilder(context)
//                .setListener((billingResult, purchases) -> {
//                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
//                        for (Purchase purchase : purchases) {
//                            // 구독 토큰을 서버로 전송
//                            String purchaseToken = purchase.getPurchaseToken();
//                            sendPurchaseTokenToServer(purchaseToken);
//                        }
//                    }
//                })
//                .enablePendingPurchases()
//                .build();
//
//        billingClient.startConnection(new BillingClientStateListener() {
//            @Override
//            public void onBillingSetupFinished(BillingResult billingResult) {
//                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//                    queryPurchases();
//                }
//            }
//
//            @Override
//            public void onBillingServiceDisconnected() {
//                // Google Play 서비스와의 연결이 끊어졌을 때 처리
//            }
//        });
//    }

//    private void queryPurchases() {
//        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, (billingResult, purchases) -> {
//            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
//                for (Purchase purchase : purchases) {
//                    // 구독 토큰을 서버로 전송
//                    String purchaseToken = purchase.getPurchaseToken();
//                    sendPurchaseTokenToServer(purchaseToken);
//                }
//            }
//        });
//    }

    // 서버로 결제 토큰을 전송하는 메소드
    private void sendPurchaseTokenToServer(String purchaseToken) {
        // 예시: Retrofit을 사용해 서버로 토큰 전송
        // serverApi.verifySubscription(purchaseToken);
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
/*
			if (!hasPermission()) {
				if (!localMode) {
//					webUrlLocal = "http://deco.iptime.org/permission.html";
//					webUrlLocal = Const.SERVER_HOST + "/t/permission.html";
					webUrlLocal = "file:///android_asset/permission.html";
				} else {
					webUrlLocal = "file:///android_asset/permission.html";
				}
			} else {
				if (!localMode) {
					webUrlLocal = "http://deco.iptime.org/index.html";
					webUrlLocal = Const.SERVER_HOST + "/t/index.html";
				} else {
					webUrlLocal = "file:///android_asset/index.html";
				}
			}
*/
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
            if (!Utils.hasPermissionForBasic(this) ||
                ( !PermissionUtil.hasAccessibilityPermissions() && !MapUtil.isAppInstalled() )
            ) {
                if ( MapUtil.isFirstStart() ) {
                    webUrlLocal = WUrl.ReqId.WEB_START.getReqUrl() + "?lng=" + language;
                } else if ( MapUtil.isAppUpdated() && !MapUtil.isAppUpdatedNotiConfirm() ) {
                    webUrlLocal = WUrl.ReqId.WEB_MAIN.getReqUrl() + "?lng=" + language;
                } else {
                    webUrlLocal = WUrl.ReqId.WEB_PERMISSION.getReqUrl()+"?lng="+language;
                }
            } else {
                webUrlLocal = WUrl.ReqId.WEB_MAIN.getReqUrl() + "?lng=" + language;
                String pPage = intent.getStringExtra("P_FOWARD_PAGE");
                String fowardPageUrl = "";
                String paramFowardPageUrl = "";
                if ( Utils.isNotEmpty(pPage) ) {
                    if ( WUrl.value(pPage) != null && WUrl.value(pPage).getReqUrl() != null ) {
                        fowardPageUrl = WUrl.value(pPage).getUrl();
                        try {
                            paramFowardPageUrl = fowardPageUrl.toUpperCase().substring(0, fowardPageUrl.lastIndexOf('.'));
                        } catch(Exception exF) {
                        }
                    }
                    if ( !WUrl.ReqId.WEB_SEARCH.equals(WUrl.value(pPage)) && !WUrl.ReqId.WEB_REG_POI.equals(WUrl.value(pPage)) ) {
                        webUrlLocal = WUrl.value(pPage).getReqUrl() + "?lng=" + language;
                    }
                }

                int pTidx = intent.getIntExtra("P_TIDX", -1);
                pCheckMode = intent.getStringExtra("P_CHECK_MODE");
                String pParams = intent.getStringExtra("P_PARAMS");
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

            }
            mWebview.loadUrl(webUrlLocal);
//            if ( !Utils.hasPermission(this) ) requestPermissionForLocation();
        }
//		mWebview.loadUrl("http://uiplay.jui.io/?p=button_4");

    }

    @Override
    public void showNetworkError(int errorCode, String description) {
        super.showNetworkError(errorCode, description);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initOverlay() {

    }

    @Override
    protected void onPause() {
        L.d(TAG,"onPause");
        super.onPause();
//        OverlayViewUtil.stopService();

        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE
//                            && // 유연한 업데이트 사용 시 (AppUpdateType.FLEXIBLE) 사용
//                            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    if ( Utils.hasPermissionForBasic(TestActivity.this) && MapUtil.isOverlayOpen() ) {
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
                                WApplication.mInstance.overlayViewServiceCallbackkt.run((params) -> {
                                    Log.i(TAG, "AU-Success-OverlayViewUtil.init(MainActivity.this).openOverlayViewShowOnly()");
                                    /** 테스트할때 자꾸 떠서 제거함
                                    OverlayViewUtil.init(WApplication.mInstance).openOverlayViewShowOnly();
                                     * */
                                    return null;
                                });
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
                if ( Utils.hasPermissionForBasic(TestActivity.this) && MapUtil.isOverlayOpen() ) {
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
//                                WApplication.mInstance.overlayViewServiceCallbackkt.stop();
                                WApplication.mInstance.overlayViewServiceCallbackkt.run((params) -> {
                                    Log.i(TAG, "AU-Failure-OverlayViewUtil.init(MainActivity.this).openOverlayViewShowOnly()");
                                    /** 테스트할때 자꾸 떠서 제거함
                                     OverlayViewUtil.init(WApplication.mInstance).openOverlayViewShowOnly();
                                     */
                                    return null;
                                });
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

    public boolean isExecute = true;

    @Override
    protected void onDestroy() {
        L.d(TAG,"onDestroy");
        super.onDestroy();
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (PermissionUtil.onRequestPermissionsResult(TestActivity.this, permsRequestCode, permissions, grandResults)) {
            initHome();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean mBackPressedToExitOnce;

    @Override
    public void onBackPressed() {
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
}
