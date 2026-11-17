package net.softm.data.kata.util;

import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import net.softm.data.kata.R;
import net.softm.data.kata.common.BillingType;
import net.softm.data.kata.common.ParamCallback;
import net.softm.data.kata.common.ParamCallback2;
import net.softm.data.kata.common.UserType;
import net.softm.data.kata.kt.Callbackkt;
import net.softm.data.kata.kt.Callkt;

import java.util.List;

public class BillingUtil {

    private static final String TAG = "BillingUtil";

    private static BillingUtil instance;
    private BillingClient billingClient;
    private Context context;
    private final MutableLiveData<Boolean> isInAppOnetime = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isInAppConsume = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSubscribed = new MutableLiveData<>(false);
    private ParamCallback<Purchase> payInAppCallback;

    private final PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            L.d(TAG,"purchasesUpdatedListener");
//            for (Purchase purchase : purchases) {
//                handlePurchase(purchase);
//            }
            checkPurchaseStatus();
        }
    };

    public static BillingUtil getInstance(Context context) {
//        if (instance == null) {
//            synchronized (BillingUtil.class) {
//                if (instance == null) {
//                    instance = new BillingUtil(context);
//                }
//            }
//        }
        instance = new BillingUtil(context);
        return instance;
    }

    private BillingUtil(Context context) {
        this.context = context;
        this.billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();
    }

    public void startConnection() {
        startConnection(null);
    }

    private int retryConnect = 0;
    private int maxTriesConnect = 10;

    public void startConnection(Runnable runnable) {
        retryConnect = 0;
        startConnection(runnable,false);
    }
    public void startConnection(Runnable runnable, boolean retry) {
        payInAppCallback = null;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    L.d(TAG,"startConnection","BillingClient setup completed successfully.");
                    if ( runnable != null ) {
                        Utils.runExecutorService(runnable);
                    }
                    checkPurchaseStatus();
                } else {
                    if ( runnable != null ) {
                        Utils.runExecutorService(runnable);
                    }
                    L.e(TAG,"startConnection","BillingClient setup failed with response code: " + billingResult.getResponseCode());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                L.e(TAG,"startConnection","Billing service disconnected. Reconnecting...");
                if ( maxTriesConnect > retryConnect ) {
                    retryConnect++;
                    startConnection(runnable,true);
                } else {
                    Utils.toast(R.string.msg_confirm_do_data_network_connect);
                }
                // startConnectionWithBackoff(runnable);
            }
        });
    }
    /** 대응책으로 코딩만 해 놓음 */
    private void startConnectionWithBackoff(Runnable runnable) {
        if (billingClient.getConnectionState() == BillingClient.ConnectionState.DISCONNECTED) {
            // Try reconnecting after a delay (exponential backoff or a fixed delay)
            new Callbackkt(1000,"payInAppCallback[sendSubscriptionToServer]").runIO((param) -> {
                startConnection(runnable, true);
                return null;
            });
        }
    }

    private void checkPurchaseStatus() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        verifyInappStatus(purchases);
                    }
                }
        );
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                (billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        verifySubscriptionStatus(purchases);
                    }
                }
        );
    }

    private void verifyInappStatus(List<Purchase> purchases) {
        L.d(TAG,"verifyInappStatus");
        boolean hasValidInAppOnetime = false;
        boolean hasValidInAppConsume = false;
        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                String productId = "";
                if ( purchase.getProducts() != null ) {
                    productId = purchase.getProducts().get(0);
                }
                if ( Utils.isNotEmpty(productId) ) {
                    if ( BillingType.INAPP_CONSUME.getProdcutId().equals(productId) ) {
                        hasValidInAppConsume = true;
                        handlePurchase(purchase);
                    } else if ( BillingType.INAPP_LIFETIME.getProdcutId().equals(productId) ) {
                        hasValidInAppOnetime = true;
                        handlePurchase(purchase);
                    }
                }

            }
        }
        L.d(TAG,"verifyInappStatus",String.format("isInAppConsume : %s / isInAppOnetime : %s",isInAppConsume.getValue(),isInAppOnetime.getValue()));
        isInAppConsume.postValue(hasValidInAppConsume);
        isInAppOnetime.postValue(hasValidInAppOnetime);
    }

    private void verifySubscriptionStatus(List<Purchase> purchases) {
        L.d(TAG,"verifySubscriptionStatus");
        boolean hasValidInAppOnetime = false;
        boolean hasValidInAppConsume = false;
        boolean hasValidSubscription = false;
        for (Purchase purchase : purchases) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                String productId = "";
                if ( purchase.getProducts() != null ) {
                    productId = purchase.getProducts().get(0);
                }
                if ( Utils.isNotEmpty(productId) ) {
                    if ( BillingType.SUBS.getProdcutId().equals(productId) ) {
                        hasValidSubscription = true;
                        handlePurchase(purchase);
                    }
                }

            }
        }
        L.d(TAG,"verifySubscriptionStatus",String.format("isSubscribed : %s",isSubscribed.getValue()));
        isSubscribed.postValue(hasValidSubscription);
    }

    public LiveData<Boolean> getIsInAppOnetime() {
        return isInAppOnetime;
    }

    public LiveData<Boolean> getIsInAppConsume() {
        return isInAppConsume;
    }

    public LiveData<Boolean> getIsSubscribed() {
        return isSubscribed;
    }

    private void handlePurchase(Purchase purchase) {
        L.d(TAG,"handlePurchase");
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            String productId;
            if ( purchase.getProducts() != null ) {
                productId = purchase.getProducts().get(0);
            } else {
                productId = "";
            }
            BillingType billingType;
            if ( Utils.isNotEmpty(productId) ) {
                if ( BillingType.SUBS.getProdcutId().equals(productId) ||
                     BillingType.INAPP_LIFETIME.getProdcutId().equals(productId) )
                {
                    if (!purchase.isAcknowledged()) {
                        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                        if( BillingType.SUBS.getProdcutId().equals(productId) ) {
                            billingType = BillingType.SUBS;
                            L.d(TAG,"handlePurchase","acknowledgePurchase",String.format("billingType : %s / productId : %s", BillingType.SUBS.getId(), productId));
                        } else {
                            billingType = BillingType.INAPP_LIFETIME;
                            L.d(TAG,"handlePurchase","acknowledgePurchase",String.format("billingType : %s / productId : %s", BillingType.INAPP_LIFETIME.getId(), productId));
                        }

                        billingClient.acknowledgePurchase(params, billingResult -> {
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                L.d(TAG, String.format("handlePurchase.[%s].success",billingType), billingResult.getResponseCode());
                                /** 구독 / 평생 완료 포인트 */ /** 서버에 구독 정보 전송 */
                                sendSubscriptionToServer(purchase, billingType, productId, purchase.getPurchaseToken(),purchase.getPurchaseTime());
                            } else {
                                L.e(TAG, String.format("handlePurchase.[%s].failed",billingType), billingResult.getResponseCode());
                            }
                        });
                    }
                } else if ( BillingType.INAPP_CONSUME.getProdcutId().equals(productId) ) {
                    billingType = BillingType.INAPP_CONSUME;
                    ConsumeParams consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
                        L.d(TAG,"handlePurchase","consumeAsync",String.format("billingType : %s / productId : %s", BillingType.INAPP_CONSUME.getId(), productId));
                        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
                            @Override
                            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    L.d(TAG, String.format("handlePurchase.[%s].success",billingType), billingResult.getResponseCode());
                                    /** 기부[소비성] 완료 포인트 */ /** 서버에 구독 정보 전송 */
                                    sendSubscriptionToServer(purchase, billingType, productId, purchase.getPurchaseToken(),purchase.getPurchaseTime());

                                } else {
                                    L.e(TAG, String.format("handlePurchase.[%s].failed",billingType), billingResult.getResponseCode());
                                }
                            }
                        });
                }
            }

        }
    }

    private void sendSubscriptionToServer(Purchase purchase,BillingType billingType, String productId, String purchaseToken, long purchaseTime) {
        L.d(TAG, "sendSubscriptionToServer",purchase);
        LicenseUtil.registerLicenseForPurchase(context, purchase, billingType, productId, purchase.getPurchaseToken(),purchase.getPurchaseTime());
        if ( payInAppCallback != null ) {
            new Callkt("payInAppCallback[sendSubscriptionToServer]").run((param) -> {
                payInAppCallback.onResult(purchase);
                payInAppCallback = null;
                return null;
            });
        }
    }

    public void payInApp(Activity activity, BillingType billingType) {
        payInApp(activity, billingType ,null);
    }
    public void payInApp(Activity activity, BillingType billingType, ParamCallback payInAppCallback) {
        L.d(TAG, "payInApp", billingType);
        this.payInAppCallback = payInAppCallback;
        queryProductDetails(billingType, new ParamCallback<ProductDetails>() {
            @Override
            public void onResult(ProductDetails productDetails) {
                L.d(TAG, "payInApp", (productDetails!=null?productDetails:""));
                if(billingClient.getConnectionState() == BillingClient.ConnectionState.DISCONNECTED ||
                    billingClient.getConnectionState() == BillingClient.ConnectionState.CLOSED) {
                    startConnection(()->{
                        payInApp(activity, billingType);
                    });
                } else {
                    ((Activity)context).runOnUiThread(()->{
                        startBillingFlow(activity, billingType, productDetails);
                    });
                }
            }
        });
    }

    /** 비소비성 구매처리 확인 */
    private void acknowledgePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        L.d(TAG, "Purchase acknowledged successfully.");
                    } else {
                        L.e(TAG, "Failed to acknowledge purchase: " + billingResult.getResponseCode());
                    }
                });
            }
        }
    }
    /** 소비성 구매처리 확인 */
    public void consumePurchase(String purchaseToken) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build();

        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Utils.toastDebug(context,"Purchase consumed successfully.");
                    L.d(TAG, "Purchase consumed successfully.");
                } else {
                    Utils.toastDebug(context,"Failed to consume purchase: " + billingResult.getResponseCode());
                    L.e(TAG, "Failed to consume purchase: " + billingResult.getResponseCode());
                }
            }
        });
    }

    public void updateUserLicenseFromPayInApp(Context context, ParamCallback2<Purchase,Purchase> callback) {
        L.d(TAG,"updatePurchaseFromPayInApp");
        getPurchaseFromPayInApp(
                new ParamCallback2<Purchase,Purchase>() {
                    @Override
                    public void onResult(Purchase subsPurchase,Purchase lifeTimePurchase) {
                        if ( subsPurchase == null ) Utils.toastDebug(context,"구독정보 없음");
                        if ( lifeTimePurchase == null ) Utils.toastDebug(context,"평생정보 없음");
                        if ( subsPurchase != null ) {
                            LicenseUtil.applyLicenseForPurchase(context, subsPurchase, BillingType.SUBS);
                        } else { /** 구독정보 없음 */
                            if ( Utils.isNotEmpty( LicenseUtil.getPurchaseTokenSubs() ) ) { /** 토큰이 생성되어있어 기존에 가입되어 있었을 경우 */
                                if (UserType.PAID_USER.equals(LicenseUtil.getUserType())) { /** LICENSE_USER 였다가 없어진경우 */
                                    LicenseUtil.unRegisterLicenseForPurchase(context, BillingType.SUBS);
                                }
                            }
                        }
                        if ( lifeTimePurchase != null ) {
                            LicenseUtil.applyLicenseForPurchase(context, lifeTimePurchase, BillingType.INAPP_LIFETIME);
                        } else { /** 평생정보 정보 없음 */
                            if ( Utils.isNotEmpty( LicenseUtil.getPurchaseTokenLifeTime() ) ) { /** 토큰이 생성되어있어 기존에 가입되어 있었을 경우 */
                                if (UserType.PAID_USER.equals(LicenseUtil.getUserType())) { /** LICENSE_USER 였다가 없어진경우 */
//                                if ( Utils.isNotEmpty( LicenseUtil.getPurchaseTokenLifeTime() ) ) { /** LICENSE_USER 였다가 없어진경우 */
                                    LicenseUtil.unRegisterLicenseForPurchase(context, BillingType.INAPP_LIFETIME);
                                }
                            }
                        }

                        new Callkt("updatePurchaseFromPayInApp").run((param) -> {
                            callback.onResult(subsPurchase,lifeTimePurchase);
                            return null;
                        });
                    }
                    @Override
                    public void onError(Exception ex) {
                        L.e(TAG,"updatePurchaseFromPayInApp", ex);
                        callback.onError(ex);
                    }
                }
        );
    }
    public void getPurchaseFromPayInApp(ParamCallback2<Purchase,Purchase> callback) {
        getPurchasesForBillingType(BillingType.SUBS,
            new ParamCallback<Purchase>() {
                @Override
                public void onResult(Purchase purchase) {
                    getPurchasesForBillingType(BillingType.INAPP_LIFETIME,new ParamCallback<Purchase>(){
                        @Override
                        public void onResult(Purchase purchase2) {
                            callback.onResult(purchase,purchase2);
                        }
                    });
                }
                @Override
                public void onError(Exception ex) {
                    callback.onError(ex);
                }
            }
        );
    }

    public void getPurchasesForBillingType(BillingType billingType, ParamCallback<Purchase> callback) {
        queryProductDetails(billingType,new ParamCallback<ProductDetails>() {
            @Override
            public void onResult(ProductDetails productDetails) {
                if (productDetails != null) {
                    queryPurchasesForBillingType(billingType,new ParamCallback<Purchase>() {
                        @Override
                        public void onResult(Purchase purchase) {
                            if (BillingType.SUBS.equals(billingType)) {
                                if (purchase != null) {
                                    Utils.toastDebug(context, "구독사용자");
                                }
                                L.d(TAG, "getPurchasesForBillingType", billingType, purchase != null);
//                                LicenseUtil.setBillingSubs(purchase != null);
                            } else if (BillingType.INAPP_LIFETIME.equals(billingType)) {
                                if (purchase != null) {
                                    Utils.toastDebug(context, "평생사용자");
                                }
                                L.d(TAG, "getPurchasesForBillingType", billingType, purchase != null);
//                                LicenseUtil.setBillingOneTime(purchase != null);
                            }
                            new Callkt("getPurchasesForBillingType").run((param) -> {
                                callback.onResult(purchase);
                                return null;
                            });
                        }
                        @Override
                        public void onError(Exception ex) {
                            L.e(TAG,"getPurchasesForBillingType", ex);
                            callback.onError(ex);
                        }
                    });
                } else {
                    L.e(TAG, "queryProductDetails-error", billingType, null );
                    new Callkt("queryProductDetails-error").run((param) -> {
                        callback.onError(new Exception("네트웍 오류 Or 기타 오류 조건"));
                        return null;
                    });
                }
            }

            @Override
            public void onError(Exception ex) {
                callback.onError(ex);
            }
        });
    }

    /** 구매 목록 조회 */
    public void queryPurchases(String productType, ParamCallback<Purchase> callback) {
        QueryPurchasesParams qp = QueryPurchasesParams.newBuilder()
                                    .setProductType(productType)
                                    .build();
        billingClient.queryPurchasesAsync(qp, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                    // 구매 목록을 처리
                    //purchase.getProducts().get(0).toString()
                    //purchase.getQuantity()
                    //purchase.getOriginalJson()
                    //purchase.getOrderId()
//                    Purchase.PurchaseState.PURCHASED
//                        Purchase.PurchaseState.PURCHASED
//                    purchase.getPurchaseState()
                    for (Purchase purchase : purchases) {
                        new Callkt("queryPurchases").run((param) -> {
                            callback.onResult(purchase);
                            return null;
                        });
                        L.d(TAG, "queryPurchases", purchase, purchase.getProducts().get(0));
                    }
                    if ( purchases.isEmpty() ) {
                        new Callkt("queryPurchases-null").run((param) -> {
                            callback.onResult(null);
                            return null;
                        });
                    }
                }
            }
        });
    }

    /** 구매 목록 조회 */
    public void queryPurchasesForBillingType(BillingType billingType, ParamCallback<Purchase> callback) {
        if (billingClient.isReady()) {
            String productType = "";
            String productId="";
            if (BillingType.INAPP_LIFETIME.equals(billingType)) {
                productType = BillingClient.ProductType.INAPP;
                productId = BillingType.INAPP_LIFETIME.getProdcutId();
            } else if (BillingType.INAPP_CONSUME.equals(billingType)) {
                productType = BillingClient.ProductType.INAPP;
                productId = BillingType.INAPP_CONSUME.getProdcutId();
            } else if (BillingType.SUBS.equals(billingType)) {
                productType = BillingClient.ProductType.SUBS;
                productId = BillingType.SUBS.getProdcutId();
            }

            QueryPurchasesParams qp = QueryPurchasesParams.newBuilder()
                    .setProductType(productType)
                    .build();
            String finalProductId = productId;
            billingClient.queryPurchasesAsync(qp, new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> purchases) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        // 구매 목록을 처리
                        //purchase.getProducts().get(0).toString()
                        //purchase.getQuantity()
                        //purchase.getOriginalJson()
                        //purchase.getOrderId()
//                    Purchase.PurchaseState.PURCHASED
//                        Purchase.PurchaseState.PURCHASED
//                    purchase.getPurchaseState()
                        boolean isMatching = false;
                        for (Purchase purchase : purchases) {
                            String productId_ = purchase.getProducts().get(0).toString();
                            if ( finalProductId.equals(productId_) ) {
                                isMatching = true;
                                L.d(TAG, "queryPurchasesForBillingType", billingType, purchase.getProducts().get(0), purchase);
                                new Callkt("queryPurchasesForBillingType").run((param) -> {
                                    callback.onResult(purchase);
                                    return null;
                                });
                            }
                        }
                        if ( !isMatching ) { /** 구독이나, 평생이 없는 경우 */
                            L.d(TAG, "queryPurchasesForBillingType", billingType, "정보없음");
                            new Callkt("queryPurchasesForBillingType-null").run((param) -> {
                                callback.onResult(null);
                                /**
                                try {
                                      // 빈 purchase 객체를 생성하여 넘김
                                    Purchase purchase = new Purchase("{}","");
                                    callback.onResult(purchase);
                                } catch (JSONException e) {
                                   // throw new RuntimeException(e);
                                }
                                */
                                return null;
                            });
                        }
                    } else { /** 이 조건은 네트웍 오류나 기타 오류 조건 */
                        new Callkt("queryPurchasesForBillingType-error").run((param) -> {
                            callback.onError(new Exception(String.format("네트웍 오류 Or 기타 오류 조건 ResponseCode : [%s]",billingResult.getResponseCode())));
                            return null;
                        });
                    }
                }
            });
        } else {
            L.e(TAG,"queryPurchasesForBillingType", "BillingClient is not ready.");
            new Callkt("queryPurchasesForBillingType-notReady").run((param) -> {
                callback.onError(new Exception("BillingClient is not ready."));
                return null;
            });
        }
    }


    public void queryProductDetails(BillingType billingType, ParamCallback callback) {
        L.d(TAG, "queryProductDetails",billingType);
        String productId = "";
        if (billingClient.isReady()) {
            String productType = "";
            if ( BillingType.INAPP_LIFETIME.equals(billingType) ) {
                productType = BillingClient.ProductType.INAPP;
                productId = BillingType.INAPP_LIFETIME.getProdcutId();
            } else if ( BillingType.INAPP_CONSUME.equals(billingType) ) {
                productType = BillingClient.ProductType.INAPP;
                productId = BillingType.INAPP_CONSUME.getProdcutId();
            } else if ( BillingType.SUBS.equals(billingType) ) {
                productType = BillingClient.ProductType.SUBS;
                productId = BillingType.SUBS.getProdcutId();
            }

            ImmutableList.Builder<QueryProductDetailsParams.Product> productListBuilder = ImmutableList.builder();
            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)  // SKU 설정
                    .setProductType(productType)  // 인앱 상품
                    .build();
            productListBuilder.add(product);

            QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(productListBuilder.build())  // 생성된 Product 목록 추가
                    .build();

            String finalProductId = productId;
            retryAttempt = 0;
            queryProductDetailsAsyncRetry(productId,queryProductDetailsParams, billingType, callback);
//
//            billingClient.queryProductDetailsAsync(queryProductDetailsParams, (billingResult, productDetailsList) -> {
//                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null && !productDetailsList.isEmpty() ) {
//                    for (ProductDetails productDetails : productDetailsList) {
//                        String productId_ = productDetails.getProductId();
//                        String price = productDetails.getOneTimePurchaseOfferDetails() != null
//                                ? productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice()
//                                : "Unknown Price";
//
//                        L.d(TAG, "queryProductDetails","Product Id: " + productId_);
//                        L.d(TAG, "queryProductDetails","Product Title: " + productDetails.getTitle());
//                        L.d(TAG, "queryProductDetails","Product Price: " + price);
//
//                        if ( productDetails.getSubscriptionOfferDetails() != null ) {
//                            /** 구독 가격 */
//                            for (ProductDetails.SubscriptionOfferDetails offerDetails : productDetails.getSubscriptionOfferDetails()) {
//                                String offerId = offerDetails.getOfferId();
//                                String basePlanId = offerDetails.getBasePlanId();
//                                List<ProductDetails.PricingPhase> pricingPhases = offerDetails.getPricingPhases().getPricingPhaseList();
//                                for (ProductDetails.PricingPhase pricingPhase : pricingPhases) {
//                                    L.d(TAG, "queryProductDetails", "Offer ID: " + offerId);
//                                    L.d(TAG, "queryProductDetails", "BasePlan ID: " + basePlanId);
//                                    L.d(TAG, "queryProductDetails", "Price : " + pricingPhase.getFormattedPrice());
//                                    L.d(TAG, "queryProductDetails", "Billing period: " + pricingPhase.getBillingPeriod());
//                                }
//                            }
//                        }
//
//                        if ( BillingType.INAPP_LIFETIME.equals(billingType) ) {
//                            if ( BillingType.INAPP_LIFETIME.getProdcutId().equals(productId_) ) {
//                                new Callkt("queryProductDetails").run((param) -> {
//                                    callback.onResult(productDetails);
//                                    return null;
//                                });
//                                break;
//                            }
//                        } else if ( BillingType.INAPP_CONSUME.equals(billingType) ) {
//                            if ( BillingType.INAPP_CONSUME.getProdcutId().equals(productId_) ) {
//                                new Callkt("queryProductDetails").run((param) -> {
//                                    callback.onResult(productDetails);
//                                    return null;
//                                });
//                                break;
//                            }
//                        } else if ( BillingType.SUBS.equals(billingType) ) {
//                            if ( BillingType.SUBS.getProdcutId().equals(productId_) ) {
//                                new Callkt("queryProductDetails").run((param) -> {
//                                    callback.onResult(productDetails);
//                                    return null;
//                                });
//                                break;
//                            }
//                        }
//
//                    }
//                } else {  /** 이 조건은 네트웍 오류나 기타 오류 조건이므로 처리를 하면 안됨 */
//                    L.e(TAG,"queryProductDetails", "Failed to query product details. Response code: " + billingResult.getResponseCode(), finalProductId );
//                    new Callkt("queryProductDetails-null").run((param) -> {
//                        callback.onError(new Exception(String.format("네트웍 오류 Or 기타 오류 조건 ResponseCode : [%s]",billingResult.getResponseCode())));
//                        return null;
//                    });
//
//                }
//            });
        } else {
            L.e(TAG,"queryProductDetails", "BillingClient is not ready.");
            new Callkt("queryProductDetails-notReady").run((param) -> {
                callback.onError(new Exception("BillingClient is not ready."));
                return null;
            });
        }
    }
    private int retryAttempt = 0;
    private int maxTries = 1;
    public void queryProductDetailsAsyncRetry(String productId, QueryProductDetailsParams queryProductDetailsParams,  BillingType billingType, ParamCallback callback) {
        billingClient.queryProductDetailsAsync(queryProductDetailsParams, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null && !productDetailsList.getProductDetailsList().isEmpty() ) {
                for (ProductDetails productDetails : productDetailsList.getProductDetailsList()) {
                    String productId_ = productDetails.getProductId();
                    String price = productDetails.getOneTimePurchaseOfferDetails() != null
                            ? productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice()
                            : "Unknown Price";

                    L.d(TAG, "queryProductDetails","Product Id: " + productId_);
                    L.d(TAG, "queryProductDetails","Product Title: " + productDetails.getTitle());
                    L.d(TAG, "queryProductDetails","Product Price: " + price);

                    if ( productDetails.getSubscriptionOfferDetails() != null ) {
                        /** 구독 가격 */
                        for (ProductDetails.SubscriptionOfferDetails offerDetails : productDetails.getSubscriptionOfferDetails()) {
                            String offerId = offerDetails.getOfferId();
                            String basePlanId = offerDetails.getBasePlanId();
                            List<ProductDetails.PricingPhase> pricingPhases = offerDetails.getPricingPhases().getPricingPhaseList();
                            for (ProductDetails.PricingPhase pricingPhase : pricingPhases) {
                                L.d(TAG, "queryProductDetails", "Offer ID: " + offerId);
                                L.d(TAG, "queryProductDetails", "BasePlan ID: " + basePlanId);
                                L.d(TAG, "queryProductDetails", "Price : " + pricingPhase.getFormattedPrice());
                                L.d(TAG, "queryProductDetails", "Billing period: " + pricingPhase.getBillingPeriod());
                            }
                        }
                    }

                    if ( BillingType.INAPP_LIFETIME.equals(billingType) ) {
                        if ( BillingType.INAPP_LIFETIME.getProdcutId().equals(productId_) ) {
                            new Callkt("queryProductDetails").run((param) -> {
                                callback.onResult(productDetails);
                                return null;
                            });
                            break;
                        }
                    } else if ( BillingType.INAPP_CONSUME.equals(billingType) ) {
                        if ( BillingType.INAPP_CONSUME.getProdcutId().equals(productId_) ) {
                            new Callkt("queryProductDetails").run((param) -> {
                                callback.onResult(productDetails);
                                return null;
                            });
                            break;
                        }
                    } else if ( BillingType.SUBS.equals(billingType) ) {
                        if ( BillingType.SUBS.getProdcutId().equals(productId_) ) {
                            new Callkt("queryProductDetails").run((param) -> {
                                callback.onResult(productDetails);
                                return null;
                            });
                            break;
                        }
                    }

                }
            } else {  /** 이 조건은 네트웍 오류나 기타 오류 조건이므로 처리를 하면 안됨 */
                L.e(TAG,"queryProductDetails", "Failed to query product details. Response code: " + billingResult.getResponseCode(), productId );

                if ( maxTries > retryAttempt ) {
                    retryAttempt++;
                    queryProductDetailsAsyncRetry(productId, queryProductDetailsParams, billingType, callback);
                } else {
                    new Callkt("queryProductDetails-null").run((param) -> {
                        callback.onError(new Exception(String.format("네트웍 오류 Or 기타 오류 조건 ResponseCode : [%s]",billingResult.getResponseCode())));
                        return null;
                    });
                }

            }
        });
    }

    /** 사용안함 제품 목록 조회 (구버전 API: `querySkuDetailsAsync`)
    public void querySkuDetails(List<String> skuList) {
        Log.d(TAG, "querySkuDetails[구버전]-querySkuDetailsAsync");
        if (billingClient.isReady()) {
            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);  // 또는 SkuType.SUBS

            billingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                        // 구버전 API로 제품 정보 처리
                        for (SkuDetails skuDetails : skuDetailsList) {
                            Log.d(TAG, "Product SKU: " + skuDetails.getSku());  // 구버전 API에서는 getSku()로 SKU ID를 가져옵니다.
                            Log.d(TAG, "Product Title: " + skuDetails.getTitle());
                            Log.d(TAG, "Product Price: " + skuDetails.getPrice());
                        }
                    }
                }
            });
        } else {
            Log.e(TAG, "BillingClient is not ready.");
        }
    }
    */
    public void startBillingFlow(Activity activity, BillingType billingType,ProductDetails productDetails) {
        L.d(TAG,"startBillingFlow");
        if (billingClient.isReady()) {
            String productType = "";
            String productId = "";
            if ( BillingType.INAPP_LIFETIME.equals(billingType) ) {
                productType = BillingClient.ProductType.INAPP;
                productId = BillingType.INAPP_LIFETIME.getProdcutId();
            } else if ( BillingType.INAPP_CONSUME.equals(billingType) ) {
                productType = BillingClient.ProductType.INAPP;
                productId = BillingType.INAPP_CONSUME.getProdcutId();
            } else if ( BillingType.SUBS.equals(billingType) ) {
                productType = BillingClient.ProductType.SUBS;
                productId = BillingType.SUBS.getProdcutId();
            }

            if (  productDetails != null ) {
                BillingFlowParams billingFlowParams;
                if ( BillingType.SUBS.equals(billingType) ) {
                    /** 구독시 */
                    billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(
                                    ImmutableList.of(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetails)
                                                    .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken()) /** 구독시 */
                                                    .build()
                                    )
                            ).build();

                } else {
                    billingFlowParams = BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(
                                    ImmutableList.of(
                                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetails)
                                                    .build()
                                    )
                            ).build();
                }
                L.d(TAG,"startBillingFlow", billingType, productDetails);
                BillingResult result = billingClient.launchBillingFlow(activity, billingFlowParams);
                if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                    L.e(TAG,"startBillingFlow", "Error launching billing flow: " + result.getResponseCode());
                } else if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    /** 결제 플로우 호출 성공시 구글 결제 에러 클리어 */
                    LicenseUtil.setPaidErrorForGoogle(false);
                    LicenseUtil.setPaidErrorForGoogleCaseIndex(-1);
                }
            } else {
                L.e(TAG, "startBillingFlow","Product details not found or failed to query product details.");
            }
        } else {
            L.e(TAG, "startBillingFlow","BillingClient is not ready.");
        }
    }

    public void endConnection() {
        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
            instance = null;
            L.d(TAG, "endConnection","BillingClient connection ended.");
        }
        payInAppCallback = null;
    }
}
