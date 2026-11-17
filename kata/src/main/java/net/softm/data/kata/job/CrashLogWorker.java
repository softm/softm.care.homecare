package net.softm.data.kata.job;

import android.content.Context;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.softm.data.kata.common.CrashErrorType;
import net.softm.data.kata.util.AdUtil;
import net.softm.data.kata.util.Fb;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.LicenseUtil;
import net.softm.data.kata.util.WorkerUtil;

public class CrashLogWorker extends Worker {

    private static final String TAG = "CrashLogWorker";
    Context mContext = null;
    public CrashLogWorker(Context context, WorkerParameters workerParams){
        super(context, workerParams);
        mContext = context;
    }

    @Override
    public Result doWork() {
        L.i(TAG,"doWork","CrashLogWorker");
        try {
            Data inputData = getInputData();
            long timestamp = inputData.getLong("timestamp", 0);
            String crashMessage = inputData.getString("crashMessage");
//            Fc.setCrashError(true);
//            Fc.updateLastCheckCrashTime(); /** 마지막 크래싱 */

            Fb.logCrash(mContext, crashMessage, timestamp);
            int errIndex = AdUtil.isGoogleAdViewErrorCase(crashMessage);
            if ( errIndex > -1 ) {
                L.d(TAG, "Crash.GOOGLE_LOAD_AD");
//                AdUtil.setAdViewErrorForGoogle(true);
//                AdUtil.setAdViewErrorForGoogleCaseIndex(errIndex);
                Fb.crashError(mContext, crashMessage, timestamp, CrashErrorType.GOOGLE_LOAD_AD, AdUtil.getAdViewErrorForGoogleCaseIndex());
            }
            int errIndexPaid = LicenseUtil.isGooglePaidErrorCase(crashMessage);
            if ( errIndexPaid > -1 ) {
                L.d(TAG, "Crash.GOOGLE_PAID");
//                LicenseUtil.setPaidErrorForGoogle(true);
//                LicenseUtil.setPaidErrorForGoogleCaseIndex(errIndexPaid);
                Fb.crashError(mContext, crashMessage, timestamp, CrashErrorType.GOOGLE_PAID, LicenseUtil.getPaidErrorForGoogleCaseIndex());
            }

            /** Crash 로그 전송후, 10초후 Service 죽었으면 다시 살기기 */
            /** 티맵자동실행 로직 */
//            WorkerUtil.startCheckServiceImmediate10SecForCrash(mContext);

        } catch (Exception ex) {
            L.e(TAG,"doWork", ex);
            return Result.failure();
        }
        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

}
