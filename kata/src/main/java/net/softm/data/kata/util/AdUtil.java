package net.softm.data.kata.util;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.AdLoadType;
import net.softm.data.kata.common.AdMode;
import net.softm.data.kata.common.AdType;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.kt.Callkt;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AdUtil {
    private static final String TAG = AdUtil.class.getSimpleName();

    public static AdView addBanner(ViewGroup adViewContainer, Context context, AdType adType) {
        return addBanner(adViewContainer, context, adType, null);
    }
    private static void bindAdUnitId(Context context, AdView adView, AdType adType) {
        if ( WUrl.devMode.equals(WUrl.EnumDevMode.RELEASE) &&
                !LicenseUtil.isTestUuids(WApplication.basicInfoDTO.uuid)
        ) {
            if ( AdType.BANNER.equals(adType) || AdType.INLINE.equals(adType) ) {
                adView.setAdUnitId(context.getString(R.string.banner_ad_unit_id_main_banner));
            } if ( AdType.WAIT_BANNER.equals(adType) ) {
                adView.setAdUnitId(context.getString(R.string.banner_ad_unit_id_waiting_banner));
            } else if ( AdType.EXIT_BANNER.equals(adType) ) {
                adView.setAdUnitId(context.getString(R.string.banner_ad_unit_id_main_exit_banner));
            }
        } else {
            if ( AdType.BANNER.equals(adType) || AdType.INLINE.equals(adType) ) {
                adView.setAdUnitId(context.getString(R.string.banner_ad_unit_id_main_banner_for_test));
            } if ( AdType.WAIT_BANNER.equals(adType) ) {
                adView.setAdUnitId(context.getString(R.string.banner_ad_unit_id_waiting_banner_for_test));
            } else if ( AdType.EXIT_BANNER.equals(adType) ) {
                adView.setAdUnitId(context.getString(R.string.banner_ad_unit_id_main_exit_banner_for_test));
            }
        }
    }
    public static AdView addBanner(ViewGroup adViewContainer, Context context, AdType adType,AdSize adSize) {
        try {
            L.d(TAG,"addBanner");
            if ( WApplication.basicInfoDTO != null && Utils.isNotEmpty(AdUtil.getAdMode()) ) {
                L.d(TAG, "basicInfoDTO", WApplication.basicInfoDTO);
            }
            if (AdMode.GOOGLE.equals(AdUtil.getAdMode())) {
                adViewContainer.setVisibility(View.VISIBLE);
                AdView adView = new AdView(context);
                bindAdUnitId(context, adView, adType);

                if ( adSize == null ) {
                    adView.setAdSize(getAdSize(context, adType));
                } else {
                    adView.setAdSize(adSize);
                }

                //            if ( context instanceof Activity ) {
                //            } else {
                //                adView.setAdSize(AdSize.FULL_BANNER);
                //            }
                // Replace ad container with new ad view.
                adViewContainer.removeAllViews();
                adViewContainer.addView(adView);

                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
                //            adView.setAdListener(new AdListener() {
                //                @Override
                //                public void onAdLoaded() {
                //                    super.onAdLoaded();
                //                    Log.e(TAG, "onAdLoaded");
                //                }
                //                @Override
                //                public void onAdClosed() {
                //                    super.onAdClosed();
                //                    Log.e(TAG, "onAdClosed");
                //                }
                //
                //                @Override
                //                public void onAdOpened() {
                //                    Log.e(TAG, "onAdOpened");
                //                }
                //
                //                @Override
                //                public void onAdClicked() {
                //                    super.onAdClicked();
                //                    Log.e(TAG, "onAdClicked");
                //                }
                //                @Override
                //                public void onAdImpression() {
                //                    super.onAdImpression();
                //                    Log.e(TAG, "onAdImpression");
                //                }
                //            });
                // 광고출력 여부 리스너
                L.d(TAG,"AdUtil.loadAd-Next");
                AdUtil.setAdViewErrorForGoogle(false); /*** 배너 로드 정상 : 구글배너 오류 여부 클리어 */
                AdUtil.setAdViewErrorForGoogleCaseIndex(-1);
                return adView;
            } else {
                adViewContainer.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            L.e(TAG,"addBanner",ex);
        }
        return null;
    }

    static AdView mWaitBanner;

    public static AdView getWaitBanner() {
        return mWaitBanner;
    }

    public static void setWaitBanner(AdView adView) {
        mWaitBanner = adView;
    }

    public static AdView loadBanner(Context context, AdType adType) {
        return loadBanner(context, adType, null,null,null);
    }

    public static AdView loadBanner(Context context, AdType adType, AdSize adSize) {
        return loadBanner(context, adType, adSize,null,null);
    }
    public static AdView loadBanner(Context context, AdType adType, AdSize adSize, Runnable startRunnable, Runnable endRunnable) {
        L.d(TAG,"loadBanner");
        try {
            if ( WApplication.basicInfoDTO != null && Utils.isNotEmpty(AdUtil.getAdMode()) ) {
                L.d(TAG, WApplication.basicInfoDTO.toString());
            }
            if ( AdMode.GOOGLE.equals(AdUtil.getAdMode()) ) {
                if ( startRunnable != null ) {
                    Utils.runExecutorService(startRunnable);
                }
                AdView adView = new AdView(context);
                bindAdUnitId(context, adView, adType);

                if ( adSize == null ) {
                 // adView.setAdSize(getAdSize(context, adType));
                    adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
                } else {
                    adView.setAdSize(adSize);
                }

                //            if ( context instanceof Activity ) {
    //            } else {
    //                adView.setAdSize(AdSize.FULL_BANNER);
    //            }
                // Replace ad container with new ad view.

                AdRequest adRequest = new AdRequest.Builder().build();

                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        L.e(TAG, "onAdLoaded");
                        if ( endRunnable != null ) {
                            Utils.runExecutorService(endRunnable);
                        }
                    }
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        super.onAdFailedToLoad(adError);
                        L.e(TAG, "onAdFailedToLoad");
                        if ( endRunnable != null ) {
                            Utils.runExecutorService(endRunnable);
                        }
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        L.e(TAG, "onAdClosed");
                    }

                    @Override
                    public void onAdOpened() {
                        L.e(TAG, "onAdOpened");
                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        L.e(TAG, "onAdClicked");
                    }
                    @Override
                    public void onAdImpression() {
                        super.onAdImpression();
                        L.e(TAG, "onAdImpression");
                    }
                });
                adView.loadAd(adRequest);
                setWaitBanner(adView); // set -> mWaitBanner
                AdUtil.setAdViewErrorForGoogle(false); /*** 배너 로드 정상 : 구글배너 오류 여부 클리어 */
                AdUtil.setAdViewErrorForGoogleCaseIndex(-1);
                return mWaitBanner;
            } else {
            }
        } catch (Exception ex) {
            L.e(TAG,"loadBanner",ex);
        }
        return null;
    }

    public static void addLoadBanner(ViewGroup adViewContainer, Context context, AdType adType) {
        L.d(TAG,"addLoadBanner");
        try {
            if (AdMode.GOOGLE.equals(AdUtil.getAdMode())) {
                if (getWaitBanner() != null && adViewContainer != null) {
                    adViewContainer.setVisibility(View.VISIBLE);
                    if (mWaitBanner.getParent() != null) {
                        ((ViewGroup) mWaitBanner.getParent()).removeView(mWaitBanner);
                    }
                    adViewContainer.removeAllViews();
                    adViewContainer.addView(mWaitBanner);
                }
            } else {
                adViewContainer.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static AdSize getAdSize(Context context, AdType adType) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int adWidthPixels = displayMetrics.widthPixels;
        int adWidth;

        int screenSize = context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK;

        // Activity에서 호출된 경우
        if (context instanceof Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowMetrics windowMetrics = ((Activity) context).getWindowManager().getCurrentWindowMetrics();
                adWidthPixels = windowMetrics.getBounds().width();
            }
            float density = displayMetrics.density;
            adWidth = (int) (adWidthPixels / density);
        }
        // Service에서 호출된 경우
        else if (context instanceof Service) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                Display display = windowManager.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size); // 화면 크기를 가져옴
                adWidthPixels = size.x; // 화면 너비를 픽셀 단위로 가져옴
            }
            float density = displayMetrics.density;
            adWidth = (int) (adWidthPixels / density);
        }
        // 기본 처리 (기타 Context)
        else {
            float density = displayMetrics.density;
            adWidth = (int) (adWidthPixels / density);
        }
        int screenHeight = displayMetrics.heightPixels;
        if ( adType.equals(AdType.BANNER) ) {
//            return AdSize.MEDIUM_RECTANGLE;
/*
 */
            if ((screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE)
                    || (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)) {
                L.d(TAG,"SCREENLAYOUT_SIZE_LARGE");
//                return AdSize.LEADERBOARD;
                return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
            } else {
                L.d(TAG,"NOT SCREENLAYOUT_SIZE_LARGE");
                return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
//            return AdSize.BANNER;
            }

        } else if (adType.equals(AdType.INLINE) ) {
            if ((screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE)
                    || (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)) {
                L.d(TAG,"SCREENLAYOUT_SIZE_LARGE");
//                return AdSize.LEADERBOARD;
                return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, adWidth);
            } else {
                L.d(TAG,"NOT SCREENLAYOUT_SIZE_LARGE");
//            return AdSize.LARGE_BANNER;
                return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, adWidth);
            }

        } else {
//        AdSize.getInlineAdaptiveBannerAdSize(int width, int maxHeight)
//        return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, adWidth);
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
        }
    }

    public static void loadRewardAd(Context context, AdType adType, Runnable startRunnable, Runnable endRunnable) {
        L.d(TAG,"loadRewardAd");
        try {
    //        btnTest6.setEnabled(false);
            if ( !AdType.NONE.equals(AdUtil.getAdTypeForWaitReward()) ) {
                if (AdMode.GOOGLE.equals(AdUtil.getAdMode())) {
                    AdRequest adRequest = new AdRequest.Builder().build();
            //			ca-app-pub-3940256099942544/8691691433 : 전면 동영상 광고
            //			ca-app-pub-3940256099942544/1033173712 : 전면 광고
            //			ca-app-pub-3940256099942544/5224354917 : 보상형 동영상 광고
                    if ( !AdUtil.isAdViewAvailableForAdMaxTime() ) {
            //            Utils.toastDebug(context, "광고 시청시간 초과로 시청이 제한 되었습니다.");
                        L.d(TAG,"광고 시청시간 초과로 시청이 제한 되었습니다.");
                    } else {
                        if ( startRunnable != null ) {
                            Utils.runExecutorService(startRunnable);
                        }
                        if (isAdViewAvailableForIntervalTime()) {
                            String adUnitId = "";
                            if ( adType.equals(AdType.REWARDED) ) {
            //                    mRewardedAd = null;
                                adUnitId = WUrl.devMode.equals(WUrl.EnumDevMode.RELEASE) && !LicenseUtil.isTestUuids(WApplication.basicInfoDTO.uuid) ?context.getString(R.string.banner_ad_unit_id_rewarded):context.getString(R.string.banner_ad_unit_id_rewarded_for_test);
                                RewardedAd.load(context,adUnitId, adRequest,getRewardedAdLoadCallback(context,adType,startRunnable,endRunnable));
                            } else if ( adType.equals(AdType.REWARDEDINTERSTITIAL) ) {
            //                    mRewardedInterstitialAd = null;
                                adUnitId = WUrl.devMode.equals(WUrl.EnumDevMode.RELEASE) && !LicenseUtil.isTestUuids(WApplication.basicInfoDTO.uuid) ?context.getString(R.string.banner_ad_unit_id_rewardedinterstitial):context.getString(R.string.banner_ad_unit_id_rewardedinterstitial_for_test);
                                RewardedInterstitialAd.load(context,adUnitId, adRequest,getRewardedInterstitialAdLoadCallback(context,adType,startRunnable,endRunnable));
                            } else if ( adType.equals(AdType.INTERSTITIAL) ) {
            //                    mInterstitialAd = null;
                                adUnitId = WUrl.devMode.equals(WUrl.EnumDevMode.RELEASE) && !LicenseUtil.isTestUuids(WApplication.basicInfoDTO.uuid) ?context.getString(R.string.banner_ad_unit_id_interstitial):context.getString(R.string.banner_ad_unit_id_interstitial_for_test);
                                InterstitialAd.load(context,adUnitId, adRequest,getInterstitialAdLoadCallback(context,adType,startRunnable,endRunnable));
                            }
                            AdUtil.setAdViewErrorForGoogle(false); /*** 배너 로드 정상 : 구글배너 오류 여부 클리어 */
                            AdUtil.setAdViewErrorForGoogleCaseIndex(-1);
                        } else {
                            L.d(TAG,"다음 광고 시청시간 까지 : " + Utils.millisecondsToTime(AdUtil.getNextAdviewTime()));
            //                Utils.toast(context,"다음 광고 시청시간 까지 : " + Utils.millisecondsToTime(AdUtil.getNextAdviewTime()));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            L.e(TAG,"loadRewardAd",ex);
        }
    }

    public static RewardedAd getRewardedAd() {
        return mRewardedAd;
    }
    private static RewardedAd mRewardedAd;
    private static RewardedAdLoadCallback getRewardedAdLoadCallback(Context context, AdType adType,Runnable startRunnable, Runnable endRunnable) {
        return new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mRewardedAd = rewardedAd;
                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d(TAG, "Ad was clicked.");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        Log.d(TAG, "Ad dismissed fullscreen content.");
                        mRewardedAd = null;
//                        initRewardAd(context,adType,startRunnable,endRunnable);
                        new Callkt("onAdDismissedFullScreenContent").runIO((param)->{
                            Utils.runExecutorService(mCloseCallback);
                            return null;
                        });
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.e(TAG, "Ad failed to show fullscreen content.");
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d(TAG, "Ad recorded an impression.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d(TAG, "Ad showed fullscreen content.");
                    }

                });
                Log.i(TAG, "onAdLoaded");
//                                btnTest6.setEnabled(true);
                if ( endRunnable != null ) {
                    Utils.runExecutorService(endRunnable);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.d(TAG, loadAdError.toString());
                mRewardedAd = null;
//                initRewardAd(context,adType,startRunnable,endRunnable);
            }
        };
    }
    public static InterstitialAd getInterstitialAd() {
        return mInterstitialAd;
    }
    private static InterstitialAd mInterstitialAd;
    private static InterstitialAdLoadCallback getInterstitialAdLoadCallback(Context context, AdType adType, Runnable startRunnable, Runnable endRunnable) {
        return new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d(TAG, "Ad was clicked.");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        Log.d(TAG, "Ad dismissed fullscreen content.");
                        mInterstitialAd = null;
                     // initRewardAd(context,adType,startRunnable,endRunnable);
                        new Callkt("onAdDismissedFullScreenContent").runIO((param)->{
                            Utils.runExecutorService(mCloseCallback);
                            return null;
                        });
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.e(TAG, "Ad failed to show fullscreen content.");
                        mInterstitialAd = null;
                    }

                    @Override
                    public void onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d(TAG, "Ad recorded an impression.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d(TAG, "Ad showed fullscreen content.");
                    }

                });
                Log.i(TAG, "onAdLoaded");
//                                btnTest6.setEnabled(true);
                if ( endRunnable != null ) {
                    Utils.runExecutorService(endRunnable);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.d(TAG, loadAdError.toString());
                mInterstitialAd = null;
//                initRewardAd(context,adType,startRunnable,endRunnable);
            }
        };
    }

    public static RewardedInterstitialAd getRewardedInterstitialAd() {
        return mRewardedInterstitialAd;
    }
    private static RewardedInterstitialAd mRewardedInterstitialAd;
    private static RewardedInterstitialAdLoadCallback getRewardedInterstitialAdLoadCallback(Context context, AdType adType, Runnable startRunnable, Runnable endRunnable) {
        return new RewardedInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mRewardedInterstitialAd = rewardedInterstitialAd;
                mRewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdClicked() {
                        // Called when a click is recorded for an ad.
                        Log.d(TAG, "Ad was clicked.");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Set the ad reference to null so you don't show the ad a second time.
                        Log.d(TAG, "Ad dismissed fullscreen content.");
                        mRewardedInterstitialAd = null;
//                        initRewardAd(context,adType,startRunnable,endRunnable);
                        new Callkt("onAdDismissedFullScreenContent").runIO((param)->{
                            Utils.runExecutorService(mCloseCallback);
                            return null;
                        });

                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.e(TAG, "Ad failed to show fullscreen content.");
                        mRewardedInterstitialAd = null;
                    }

                    @Override
                    public void onAdImpression() {
                        // Called when an impression is recorded for an ad.
                        Log.d(TAG, "Ad recorded an impression.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d(TAG, "Ad showed fullscreen content.");
                    }

                });
                Log.i(TAG, "onAdLoaded");
//                                btnTest6.setEnabled(true);
                if ( endRunnable != null ) {
                    Utils.runExecutorService(endRunnable);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.d(TAG, loadAdError.toString());
                mRewardedInterstitialAd = null;
//                initRewardAd(context,adType,startRunnable,endRunnable);
            }
        };
    }

    public static void show(Activity activity, AdType adType) {
        show(activity, adType, null);
    }

    static Runnable mCloseCallback;
    public static void show(Activity activity, AdType adType, Runnable closeCallback) {
        L.d(TAG,"show");
        mCloseCallback = closeCallback;
        if ( adType.equals(AdType.REWARDED ) ) {
            RewardedAd ad = AdUtil.getRewardedAd();
            if (ad != null) {
                ad.show(activity, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d(TAG, "The user earned the reward.");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                        AdUtil.setAdViewCheckTime(System.currentTimeMillis());
                        LicenseUtil.addLicenseExpireTimeForAdView();
                    }
                });
            } else {
                Log.d(TAG, "The rewarded ad wasn't ready yet.");
            }
            mRewardedAd = null;
        } else if ( adType.equals(AdType.REWARDEDINTERSTITIAL ) ) {
            RewardedInterstitialAd ad= AdUtil.getRewardedInterstitialAd();
            if (ad != null) {
                ad.show(activity, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d(TAG, "The user earned the reward.");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                        AdUtil.setAdViewCheckTime(System.currentTimeMillis());
                        LicenseUtil.addLicenseExpireTimeForAdView();
                    }
                });
            } else {
                Log.d(TAG, "The rewarded ad wasn't ready yet.");
            }
            mRewardedInterstitialAd = null;
        } else if ( adType.equals(AdType.INTERSTITIAL ) ) {
            InterstitialAd ad= AdUtil.getInterstitialAd();
            if (ad != null) {
                ad.show(activity);
                // Handle the reward.
                Log.d(TAG, "The user earned the reward.");
                AdUtil.setAdViewCheckTime(System.currentTimeMillis());
                LicenseUtil.addLicenseExpireTimeForAdView();
            } else {
                Log.d(TAG, "The rewarded ad wasn't ready yet.");
            }
            mInterstitialAd = null;
        }

    }

    public static long getAdViewCheckTime() { //
        String _v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_AD_VIEW_CHECK_TIME, "0");  // 0
        _v = StringUtils.defaultString(_v,"0");
        return Long.valueOf(_v);
    }

    public static void setAdViewCheckTime(long v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_AD_VIEW_CHECK_TIME, String.valueOf(v));
    }

    public static boolean isAdViewAvailable() {
        return ( isAdViewAvailableForAdMaxTime() ) && ( isAdViewAvailableForIntervalTime() );
    }

    public static boolean isAdViewAvailableForAdMaxTime() {
        return (
                LicenseUtil.getLicenseExpireTime() - System.currentTimeMillis() < AdUtil.getAdViewLicenseAddMaxTime()
        );
//        LicenseUtil.getLicenseExpireTime() - System.currentTimeMillis() = -49520784
//        LicenseUtil.getLicenseExpireTime() = 1738694372795
//        WApplication.basicInfoDTO.adViewLicenseAddMaxTime = -1702967296
    }

    public static boolean isAdViewAvailableForIntervalTime() {
        return (
                System.currentTimeMillis() - AdUtil.getAdViewCheckTime() >= AdUtil.getAdViewIntervalTime()
        );
    }


    public static long getNextAdviewTime() {
        return
                (AdUtil.getAdViewCheckTime() + AdUtil.getAdViewIntervalTime()) - System.currentTimeMillis()
        ;
    }

    public static boolean isAdingMainBanner() {
        try {
            return !AdMode.NONE.equals(AdUtil.getAdMode()) && !AdType.NONE.equals(AdUtil.getAdTypeForMainBanner());
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isAdingPopupBanner() {
        try {
            return !AdMode.NONE.equals(AdUtil.getAdMode()) && !AdType.NONE.equals(AdUtil.getAdTypeForPopupBanner());
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isAdingWaitBanner() {
        try {
            return !AdMode.NONE.equals(AdUtil.getAdMode()) && !AdType.NONE.equals(AdUtil.getAdTypeForWaitBanner());
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isAdingWaitReward() {
        try {
            return !AdMode.NONE.equals(AdUtil.getAdMode()) && !AdType.NONE.equals(AdUtil.getAdTypeForWaitReward());
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isAding() {
        try {
            return !AdMode.NONE.equals(AdUtil.getAdMode());
        } catch (Exception ex) {
            return false;
        }
    }

    public static AdMode getAdMode() {
        try {
            return WApplication.basicInfoDTO.adMode;
        } catch (Exception ex) {
            return AdMode.NONE;
        }
    }

    public static void setAdMode( AdMode adMode) {
        try {
            WApplication.basicInfoDTO.adMode = adMode;
        } catch (Exception ex) {
        }
    }

    public static AdMode getDefaultAdMode() {
        try {
            return WApplication.basicInfoDTO.defaultAdMode;
        } catch (Exception ex) {
            return AdMode.NONE;
        }
    }

    /** 광고 재시청 인터벌 */
    public static long getAdViewIntervalTime() {
        try {
            return WApplication.basicInfoDTO.adViewIntervalTime;
        } catch (Exception ex) {
            return 0;
        }
    }

    /** 광고 시청에 대한 라이센스 보상 시간 */
    public static long getAdViewLicenseAddTime() {
        try {
            return WApplication.basicInfoDTO.adViewLicenseAddTime;
        } catch (Exception ex) {
            return 1000L * 60 * 60 * 24 * 2;
        }
    }

    /** 광고 시청에 대한 보상시간 누적 최대 시간 */
    public static long getAdViewLicenseAddMaxTime() {
        try {
            return WApplication.basicInfoDTO.adViewLicenseAddMaxTime;
        } catch (Exception ex) {
            return 1000L * 60 * 60 * 24 * 365;
        }
    }

    /** 광고를 모드를 defaultAdMode로 적용할 uuid */
    public static List<String> getForceAdModeUUIDs() {
        try {
            return WApplication.basicInfoDTO.forceAdModeUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    /** 광고 타입 : 메인화면 (MainActivity) */
    public static AdType getAdTypeForMainBanner() {
        try {
            return WApplication.basicInfoDTO.adTypeForMainBanner;
        } catch (Exception ex) {
            return AdType.NONE;
        }
    }

    /** 광고 타입 : 팝업화면 (WebPopActivity) */
    public static AdType getAdTypeForPopupBanner() {
        try {
            return WApplication.basicInfoDTO.adTypeForPopupBanner;
        } catch (Exception ex) {
            return AdType.NONE;
        }
    }

    /** 광고 타입 : 대기화면 (WaitOverlayViewService) */
    public static AdType getAdTypeForWaitBanner() {
        try {
            return WApplication.basicInfoDTO.adTypeForWaitBanner;
        } catch (Exception ex) {
            return AdType.NONE;
        }
    }

    /** 광고 타입 : 대기화면 보상광고 (WaitOverlayViewService) */
    public static AdType getAdTypeForWaitReward() {
        try {
            return WApplication.basicInfoDTO.adTypeForWaitReward;
        } catch (Exception ex) {
            return AdType.NONE;
        }
    }

    /** 광고 로드 방식 */
    public static AdLoadType getAdTypeForWaitBannerLoadType() {
        try {
            return WApplication.basicInfoDTO.adTypeForWaitBannerLoadType;
        } catch (Exception ex) {
            return AdLoadType.DIRECT;
        }
    }

    public static boolean isCheckGoogleBannerError(String exStackTraceString) {
        if ( Utils.isNotEmpty(exStackTraceString) ) {
            if ( exStackTraceString.contains("java.lang.VerifyError") &&
                (
                    exStackTraceString.contains("AdUtil.addBanner") &&
                    exStackTraceString.contains("BaseAdView.loadAd")
                )
                ||
                (
                    exStackTraceString.contains("AdUtil.loadRewardAd") &&
                    exStackTraceString.contains("RewardedAd.load")
                )
            )
            {
                return true;
            }
//            if ( exStackTraceString.contains("java.lang.NullPointerException") &&
//                 exStackTraceString.contains("TestActivity.onClick_btnTests")  )
//            {
//                return true;
//            }
        }
        return false;
    }
    public static int isGoogleAdViewErrorCase(String exStackTraceString) {
        int matchIndex = -1;
        if ( Utils.isNotEmpty(WApplication.basicInfoDTO.googleAdViewErrorCase) ) {
            for (List<String> subarray : WApplication.basicInfoDTO.googleAdViewErrorCase) {
                matchIndex++;
                int subSize = subarray.size();
                int trueCount = 0;
                for (String element : subarray) {
                    if (exStackTraceString.contains(element)) {
                        trueCount++;
                    }
                }
                if ( trueCount == subSize ) return matchIndex;
            }
        }
        return -1;
    }

    public static boolean isGoogleAdViewErrorOccurred() {
        String _enable = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_AD_VIEW_GOOGLE_ERROR, "0"); // 사용
        return  _enable.equals("1")?true:false;
    }

    public static void setAdViewErrorForGoogle(boolean enable) {
        L.d(TAG,"setAdViewErrorForGoogle : " + enable);
        WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_AD_VIEW_GOOGLE_ERROR, enable?"1":"0");
    }

    public static void setAdViewErrorForGoogleCaseIndex(int idx) {
        L.d(TAG,"setAdViewErrorForGoogleCaseIndex : " + idx);
        WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_AD_VIEW_ERROR_GOOGLE_CASE_INDEX, idx);
    }

    public static int getAdViewErrorForGoogleCaseIndex() {
        return Long.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_AD_VIEW_ERROR_GOOGLE_CASE_INDEX,-1L)).intValue();
    }

    /** 광고 제외 uuid */
    public static List<String> getExceptAdModeUUIDs() {
        try {
            return WApplication.basicInfoDTO.exceptAdModeUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    /** 광고 제외 uuid
     * isExceptAdModeUUIDs(DeviceUtil.getUUID(mContext))
     */
    public static boolean isExceptAdModeUUIDs(String uuid) {
        try {
            return Utils.isNotEmpty(getExceptAdModeUUIDs()) && getExceptAdModeUUIDs().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }

}

