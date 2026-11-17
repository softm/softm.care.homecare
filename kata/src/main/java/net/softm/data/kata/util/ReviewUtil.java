package net.softm.data.kata.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewException;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.model.ReviewErrorCode;

import net.softm.data.kata.MainActivity;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.Const;
import net.softm.data.kata.common.ReviewOPType;

import org.apache.commons.lang3.StringUtils;

public class ReviewUtil {
    private static final String TAG = "ReviewUtil";

    public static boolean isReviewRequestAvailable() {
        if ( isAvailableReview() ) {
            long checkTime = getReviewRequestCheckTime();
            if (checkTime == 0) {
                return (
                        System.currentTimeMillis() - MapUtil.getAppUpdatedTime() >= ReviewUtil.getReviewRequestFirstIntervalTime()
                );
            } else {
                return (
                        System.currentTimeMillis() - checkTime >= ReviewUtil.getReviewRequestIntervalTime()
                );
            }
        } else {
            return false;
        }
    }

    public static long getReviewRequestCheckTime() { //
        String _v = WApplication.mInstance.getSharedPreferences(Const.PREF_KEY_REVIEW_REQUEST_CHECK_TIME, "0");  // 0
        _v = StringUtils.defaultString(_v,"0");
        return Long.valueOf(_v);
    }

    public static void setReviewRequestCheckTime(long v) {
        WApplication.mInstance.setSharedPreferences(Const.PREF_KEY_REVIEW_REQUEST_CHECK_TIME, String.valueOf(v));
    }

    public static void updateReviewRequestCheckTime() {
        setReviewRequestCheckTime(System.currentTimeMillis()); // 1737866411326
    }

    public static void popupReview(Activity activity) {
        popupReview(activity,null);
    }

    public static void popupReview(Activity activity,Runnable runnable) {
        if ( ReviewUtil.isReviewRequestAvailable() ) {
            ReviewManager manager = ReviewManagerFactory.create(activity);
            //                ReviewManager manager = new FakeReviewManager(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            Fa.i(activity,TAG,Fa.EVENT_POPUP_REVIEW);
        /** 인앱리뷰 테스트 런처가 화면에 표시되려면
         * 쉐어앱 테스트나, 직접 프로덕션에 올려야 화면이 표시된다.
         * */
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // We can get the ReviewInfo object
                    ReviewInfo reviewInfo = task.getResult();
                    Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);

                    flow.addOnSuccessListener(task2 -> {
                        Utils.toastDebug(activity, "인앱리뷰 호출하기 성공!");
                        L.d(TAG, "인앱리뷰 호출하기 성공! 호출 뒤의 상황은 알 수 없다.");
                        Fa.i(activity,TAG,Fa.EVENT_POPUP_REVIEW_SUCCESS);
                    }).addOnFailureListener(task3 -> {
                        Utils.toastDebug(activity, "인앱리뷰 호출하기 실패. ERR 발생");
                        L.d(TAG, "인앱리뷰 호출하기 실패. ERR 발생");
                        Fa.i(activity,TAG,Fa.EVENT_POPUP_REVIEW_FAILURE);
                    }).addOnCompleteListener(task4 -> {
//                        Utils.toastDebug(activity, "인앱리뷰 호출하기 성공 or 실패.완료!");
                        L.d(TAG, "인앱리뷰 호출하기 성공 or 실패. 여기서도 강제종료 했는지는 알 수 없다.");
                        if ( runnable != null ) {
                            Utils.runExecutorService(runnable);
                        }
                    });
                    ReviewUtil.updateReviewRequestCheckTime();
                } else {
                    try {
                        // There was some problem, log or handle the error code.
                        @ReviewErrorCode int reviewErrorCode = ((ReviewException) task.getException()).getErrorCode();
                        Utils.toastDebug(activity, "request.addOnCompleteListener error : " + task.getException());
                    } catch (Exception ex) {
                        L.e(TAG,"popupReview",ex);
                    }
                }
            });
        }
    }

    public static boolean isAvailableReview() {
        try {
            return ReviewOPType.START.equals(ReviewUtil.getReviewOPType())?true:false;
        } catch (Exception ex) {
            return false;
        }
    }

    public static void goReview(Context context) {
        Intent i = new Intent(context, MainActivity.class); // 메인
        i.putExtra("P_REVIEW", true);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }

    /** 리뷰 운영 방식 */
    public static ReviewOPType getReviewOPType() {
        try {
            return WApplication.basicInfoDTO.reviewOPType;
        } catch (Exception ex) {
            return ReviewOPType.STOP;
        }
    }

    /** 설치 후 처음 리뷰 요청 간격 */
    public static long getReviewRequestFirstIntervalTime() {
        try {
            return WApplication.basicInfoDTO.reviewRequestFirstIntervalTime;
        } catch (Exception ex) {
            return 1000L * 60 * 60 * 24 * 1;
        }
    }

    /** 설치 후 처음 리뷰 요청 그 후 간격 */
    public static long getReviewRequestIntervalTime() {
        try {
            return WApplication.basicInfoDTO.reviewRequestIntervalTime;
        } catch (Exception ex) {
            return 1000L * 60 * 60 * 24 * 3;
        }
    }

}

