package net.softm.data.kata.util;

import android.content.Context;

import com.android.billingclient.api.Purchase;
import com.google.gson.Gson;

import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.AdViewOPType;
import net.softm.data.kata.common.BillingType;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.PaidOPType;
import net.softm.data.kata.common.ParamCallback;
import net.softm.data.kata.common.ParamCallback2;
import net.softm.data.kata.common.UserLevel;
import net.softm.data.kata.common.UserType;
import net.softm.data.kata.dto.PurchaseDTO;
import net.softm.data.kata.kt.Callkt;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LicenseUtil {
    private static final String TAG = "LicenseUtil";

    public static PurchaseDTO getPurchaseSubs() {
        Gson gson = new Gson();
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_PURCHASE_SUBS_ORIGINALJSON, "{}");
        PurchaseDTO rtn = null;
        try {
            rtn = gson.fromJson(v,PurchaseDTO.class);
        } catch (Exception ex){
        }
        return rtn;
    }

    public static void setPurchaseSubs(Purchase purchase) {
        String v = purchase!=null?purchase.getOriginalJson():"{}";
//        try {
//            rtn = gson.fromJson(v,MapPoint.class);
//        } catch (Exception ex){
//        }
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_PURCHASE_SUBS_ORIGINALJSON, v);
    }

    public static PurchaseDTO getPurchaseLifeTime() {
        Gson gson = new Gson();
        String v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_PURCHASE_LIFETIME_ORIGINALJSON, "{}");
        PurchaseDTO rtn = null;
        try {
            rtn = gson.fromJson(v, PurchaseDTO.class);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return rtn;
    }

    public static void setPurchaseLifeTime(Purchase purchase) {
        String v = purchase!=null?purchase.getOriginalJson():"{}";
//        try {
//            rtn = gson.fromJson(v,MapPoint.class);
//        } catch (Exception ex){
//        }
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_PURCHASE_LIFETIME_ORIGINALJSON, v);
    }

    public static String getPurchaseTokenSubs() {
        String _defaultNaviMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_PURCHASE_TOKEN_SUBS, "");
        return _defaultNaviMode;
    }

    public static void setPurchaseTokenSubs(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_PURCHASE_TOKEN_SUBS, v);
    }

    public static String getPurchaseTokenLifeTime() {
        String _defaultNaviMode = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_PURCHASE_TOKEN_LIFETIME, "");
        return _defaultNaviMode;
    }

    public static void setPurchaseTokenLifeTime(String v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_PURCHASE_TOKEN_LIFETIME, v);
    }

    public static void setUserType(UserType v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_USER_TYPE, v.getId());
        WApplication.mInstance.userType = v;
    }

    public static UserType getUserType() {
        try {
            String userType = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_USER_TYPE, UserType.FREE_USER.getId()); /** 무료사용자 */
            return UserType.value(userType);
        } catch (Exception ex) {
            return UserType.FREE_USER;
        }
    }
    public static void setUserLevel(UserLevel v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_USER_LEVEL, v.getId());
        WApplication.mInstance.userLevel = v;
    }

    public static UserLevel getUserLevel() {
        try {
            String userLevel = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_USER_LEVEL, UserLevel.FREE.getId()); /** 무료 */
            return UserLevel.value(userLevel);
        } catch (Exception ex) {
            return UserLevel.FREE;
        }
    }

    public static long getRemindLicenseExpireTime() {
        return getLicenseExpireTime() - System.currentTimeMillis() ;
    }
    public static long getLicenseExpireTime() { //
        String _v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_LICENSE_EXPIRE_TIME, "0");  // 0
        _v = StringUtils.defaultString(_v,"0");
        return Long.valueOf(_v);
    }

    public static void setLicenseExpireTime(long v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_LICENSE_EXPIRE_TIME, String.valueOf(v));
    }

    public static void addLicenseExpireTimeForAdView() {
        long licenseExpireTime = getLicenseExpireTime();
        if ( licenseExpireTime > 0 ) { /** 라이센스 만료 기간이 존재하는데 */
            /** 광고보상시간을 추가해
             * 라이센스만료 시간을 지정해도
             * 라이센스만료 시간이 오늘보다 작아 여전히 만료상태가 되는 경우
             * 라이센스만료 시간을 0으로 초기화 하여 광고보상시간을 추가함 */
            if ( licenseExpireTime + AdUtil.getAdViewLicenseAddTime() <= System.currentTimeMillis()) {
                licenseExpireTime = 0;
            }
        }
        licenseExpireTime = licenseExpireTime == 0? System.currentTimeMillis():licenseExpireTime;
        long newLicenseExpireTime = licenseExpireTime + AdUtil.getAdViewLicenseAddTime();
        setLicenseExpireTime(newLicenseExpireTime);
    }

    public static void fixUserType() {
        if ( !LicenseUtil.isPaidLicense() ) {
            if ( LicenseUtil.isBetaLicense() ) {
                WApplication.mInstance.userType = UserType.BETA_USER;
                WApplication.mInstance.userLevel = UserLevel.AD_VIEW;
            }
            if ( LicenseUtil.isAdViewLicense() ) {
                WApplication.mInstance.userType = UserType.AD_VIEW_USER;
                WApplication.mInstance.userLevel = UserLevel.AD_VIEW;
            } else if ( LicenseUtil.isBetaLicense() ) {
                WApplication.mInstance.userType = UserType.BETA_USER;
                WApplication.mInstance.userLevel = UserLevel.BETA;
            } else {
                WApplication.mInstance.userType = UserType.FREE_USER;
                WApplication.mInstance.userLevel = UserLevel.FREE;
            }
        }
    }

    public static boolean isSubsLicense() {
//        return UserType.PAID_USER.equals(getUserType()) && UserLevel.SUBS.equals(getUserLevel());
        String _v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_LICENSE_SUBS, "0"); // Off
        return  _v.equals("1")?true:false;
    }

    public static boolean isLifeTimeLicense() {
//        return UserType.PAID_USER.equals(getUserType()) && UserLevel.LIFETIME.equals(getUserLevel());
        String _v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_LICENSE_LIFETIME, "0"); // Off
        return  _v.equals("1")?true:false;
    }

    public static void setSubsLicense(boolean _v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_LICENSE_SUBS, _v?"1":"0");
    }

    public static void setLifeTimeLicense(boolean _v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_LICENSE_LIFETIME, _v?"1":"0");
    }

    public static boolean isPaidLicense() {
        return LicenseUtil.isSubsLicense() || LicenseUtil.isLifeTimeLicense();
    }

    public static boolean isLicense() {
        return isPaidLicense() || LicenseUtil.isBetaLicense() || LicenseUtil.isAdViewLicense();
    }

    public static boolean isAdViewLicense() { //
        return !isPaidLicense() && getRemindLicenseExpireTime() > 0;
    }

    public static boolean isBetaLicense() {
        try {
            if ( !isPaidLicense() ) {
                if (Utils.isNotEmpty(WApplication.basicInfoDTO) &&
                    Utils.isNotEmpty(LicenseUtil.getBetaLicensedUUIDs()) &&
                    LicenseUtil.isBetaLicensedUUIDs(WApplication.basicInfoDTO.uuid)
                ) {
                    return true;
                }
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }

    public static void registerLicenseForPurchase(Context context, Purchase purchase, BillingType billingType, String productId, String purchaseToken, long purchaseTime) {
        applyLicenseForPurchase(context, purchase, billingType);
        Fb.purchase(context, purchase, billingType, productId, purchaseToken, purchaseTime);
        if ( BillingType.SUBS.equals(billingType) ) {
            Fa.i(context, TAG, Fa.EVENT_REGISTER_SUBS_LICENSE);
        } else if ( BillingType.INAPP_LIFETIME.equals(billingType) ) {
            Fa.i(context, TAG, Fa.EVENT_REGISTER_LIFETIME_LICENSE);
        }
    }

    public static void unRegisterLicenseForPurchase(Context context,BillingType billingType) {
        // 로깅
        // Fa. 이벤트 로깅
       String purchaseToken = "";
        if ( BillingType.SUBS.equals(billingType) ) {
            purchaseToken = getPurchaseTokenSubs();
            setPurchaseTokenSubs("");
            setSubsLicense(false);
            setPurchaseSubs(null);
            Fa.i(context, TAG, Fa.EVENT_UNREGISTER_SUBS_LICENSE);
        } else if ( BillingType.INAPP_LIFETIME.equals(billingType) ) {
            purchaseToken = getPurchaseTokenLifeTime();
            setPurchaseTokenLifeTime("");
            setLifeTimeLicense(false);
            setPurchaseLifeTime(null);
            Fa.i(context, TAG, Fa.EVENT_UNREGISTER_LIFETIME_LICENSE);
        }

        String productId = billingType.getProdcutId();
        if ( Utils.isEmpty(getPurchaseTokenSubs()) && Utils.isEmpty(getPurchaseTokenLifeTime()) ) {
            setUserType(UserType.FREE_USER);
            setUserLevel(UserLevel.FREE);
            setLicenseExpireTime(0);
        }

        Fb.purchaseUnRegister(context, billingType, productId, purchaseToken);
    }

    public static void applyLicenseForPurchase(Context context, Purchase purchase, BillingType billingType) {
        long oneMonth = 1000L * 60 * 60 * 24 * 31;
        long oneYear = 1000L * 60 * 60 * 24 * 365;
//        2678400000
//        31536000000
        long licenseExpireTime = 0;
        if ( BillingType.SUBS.equals(billingType) ) {
        } else if ( BillingType.INAPP_LIFETIME.equals(billingType) ) {
        }
        System.currentTimeMillis();
        if (BillingType.SUBS.equals(billingType)) { /** 구독 */
            setUserType(UserType.PAID_USER);
            setUserLevel(UserLevel.SUBS);
            setSubsLicense(true);
            setPurchaseSubs(purchase);
            setPurchaseTokenSubs(purchase.getPurchaseToken());
            setLicenseExpireTime(purchase.getPurchaseTime() + oneMonth);
        } else if (BillingType.INAPP_LIFETIME.equals(billingType)) { /** 평생 */
            setUserType(UserType.PAID_USER);
            setUserLevel(UserLevel.LIFETIME);
            setLifeTimeLicense(true);
            setPurchaseLifeTime(purchase);
            setPurchaseTokenLifeTime(purchase.getPurchaseToken());
            setLicenseExpireTime(purchase.getPurchaseTime() + oneYear);
        }
    }

    public static void syncUserLicense(Context context, ParamCallback<String> callback) {
     // new Callkt("getBillingInfo").runIO((param)-> {
     // BillingUtil billingUtil = BillingUtil.getInstance(context);
        BillingUtil billingUtil = WApplication.mInstance.billingUtil;
        if ( billingUtil == null ) {
            return;
        }
     // BillingUtil billingUtil = new BillingUtil(context);
        billingUtil.startConnection(()->{
            billingUtil.updateUserLicenseFromPayInApp(context,
                    new ParamCallback2<Purchase, Purchase>() {
                        @Override
                        public void onResult(Purchase subsPurchase, Purchase lifeTimePurchase) {
//                            if (subsPurchase != null)
//                                Utils.toastDebug(context, "구독정보 : " + subsPurchase.getPurchaseToken());
//                            if (lifeTimePurchase != null)
//                                Utils.toastDebug(context, "평생정보 : " + subsPurchase.getPurchaseToken());
//                                Utils.toastDebug(context,String.format("구독 : %s, 평생 : %s",subsPurchase != null,lifeTimePurchase != null));
                            Utils.toastDebug(context,String.format("구독 : %s, 평생 : %s",LicenseUtil.isSubsLicense(),LicenseUtil.isLifeTimeLicense()));
                            LicenseUtil.fixUserType();

                            if ( callback != null ) {
                                new Callkt("syncUserLicense").run((param) -> {
                                    callback.onResult("Success");
                                    return null;
                                });
                            }
                        }

                        @Override
                        public void onError(Exception ex) {
                            Utils.toastDebug(context,String.format("[Error] 구독 : %s, 평생 : %s",LicenseUtil.isSubsLicense(),LicenseUtil.isLifeTimeLicense()));
//                                Utils.toastDebug(context, ex.getMessage());
                            if ( callback != null ) {
                                new Callkt("syncUserLicense").run((param) -> {
                                    callback.onError(ex);
                                    return null;
                                });
                            }
                        }
                    }
            );
//        });
        });
    }

    /** 베타 라이센스를 적용할 UUID */
    public static List<String> getBetaLicensedUUIDs() {
        try {
            return WApplication.basicInfoDTO.betaLicensedUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
    public static boolean isBetaLicensedUUIDs(String uuid) {
        try {
            return getBetaLicensedUUIDs().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }

    /** 유료 결제 라이센스 운영 형태 */
    public static PaidOPType getPaidOPType() {
        try {
            return WApplication.basicInfoDTO.paidOPType;
        } catch (Exception ex) {
            return PaidOPType.NONE;
        }
    }
    public static void setPaidOPType(PaidOPType paidOPType) {
        try {
            WApplication.basicInfoDTO.paidOPType = paidOPType;
        } catch (Exception ex) {
        }
    }

    /** 유료 결제 라이센스 운영 형태를 강제로 Paid로 적용할 uuid */
    public static List<String> getForcePaidOPTypeUUIDs() {
        try {
            return WApplication.basicInfoDTO.forcePaidOPTypeUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static boolean isForcePaidOPTypeUUIDs(String uuid) {
        try {
            return getForcePaidOPTypeUUIDs().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }

    ///////////////////////
    public static AdViewOPType getAdViewOPType() {
        try {
            return WApplication.basicInfoDTO.adViewOPType;
        } catch (Exception ex) {
            return AdViewOPType.NONE;
        }
    }

    public static void setAdViewOPType(AdViewOPType adViewOPType) {
        try {
            WApplication.basicInfoDTO.adViewOPType = adViewOPType;
        } catch (Exception ex) {
        }
    }

    /** Service 체크 로깅 대상 UUID */
    public static List<String> getForceAdViewOPTypeAdViewUUIDs() {
        try {
            return WApplication.basicInfoDTO.forceAdViewOPTypeAdViewUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static boolean isForceAdViewOPTypeAdViewUUIDs(String uuid) {
        try {
            return getForceAdViewOPTypeAdViewUUIDs().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }

    /** Service 체크 로깅 대상 UUID */
    public static List<String> getForceAdViewOPTypeAdViewStopUUIDs() {
        try {
            return WApplication.basicInfoDTO.forceAdViewOPTypeAdViewStopUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
    public static boolean isForceAdViewOPTypeAdViewStopUUIDs(String uuid) {
        try {
            return getForceAdViewOPTypeAdViewStopUUIDs().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean isPaid() {
        try {
            return PaidOPType.PAID.equals(getPaidOPType());
        } catch (Exception ex) {
            return false;
        }
    }

    public static int isGooglePaidErrorCase(String exStackTraceString) {
        int matchIndex = -1;
        if ( Utils.isNotEmpty(WApplication.basicInfoDTO.googlePaidErrorCase) ) {
            for (List<String> subarray : WApplication.basicInfoDTO.googlePaidErrorCase) {
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

    public static boolean isGooglePaidErrorOccurred() {
        String _enable = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_PAID_GOOGLE_ERROR, "0"); // 사용
        return  _enable.equals("1")?true:false;
    }

    public static void setPaidErrorForGoogle(boolean enable) {
        L.d(TAG,"setPaidErrorForGoogle : " + enable);
        WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_PAID_GOOGLE_ERROR, enable?"1":"0");
    }

    public static void setPaidErrorForGoogleCaseIndex(int idx) {
        L.d(TAG,"setPaidErrorForGoogleCaseIndex : " + idx);
        WApplication.mInstance.setSharedPreferencesCommit(Const.PREF_KEY_PAID_ERROR_GOOGLE_CASE_INDEX, idx);
    }

    public static int getPaidErrorForGoogleCaseIndex() {
        return Long.valueOf(WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_PAID_ERROR_GOOGLE_CASE_INDEX,-1L)).intValue();
    }

    /** 유료 제외 uuid */
    public static List<String> getExceptPaidUUIDs() {
        try {
            return WApplication.basicInfoDTO.exceptPaidUUIDs;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    /** 유료 제외 uuid
     * isExceptPaidUUIDs(DeviceUtil.getUUID(mContext))
     */
    public static boolean isExceptPaidUUIDs(String uuid) {
        try {
            return Utils.isNotEmpty(getExceptPaidUUIDs()) && getExceptPaidUUIDs().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }

    /** 테스트 제외 uuid */
    public static List<String> getTestUuids() {
        try {
            return WApplication.basicInfoDTO.testUuids;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    /** 테스트 제외 uuid
     * isTestUuids(DeviceUtil.getUUID(mContext))
     */
    public static boolean isTestUuids(String uuid) {
        try {
            return Utils.isNotEmpty(getTestUuids()) && getTestUuids().contains(uuid);
        } catch (Exception ex) {
            return false;
        }
    }

}

