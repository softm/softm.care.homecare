package net.softm.data.kata.job;

import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_ACL_DISCONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_POWER_DISCONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_WIFI_CONNECTED;
import static net.softm.data.kata.MainConnectReceiver.ACTION_SOFTM_WIFI_DISCONNECTED;
import static net.softm.data.kata.MainConnectReceiver.getAction;
import static net.softm.data.kata.util.AcsUtil.KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE;

import android.content.Context;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.softm.data.kata.MainConnectReceiver;
import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.StartMode;
import net.softm.data.kata.util.AcsUtil;
import net.softm.data.kata.util.Fc;
import net.softm.data.kata.util.MapUtil;
import net.softm.data.kata.util.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import androidx.work.Worker;
//import androidx.work.WorkerParameters;
//
//import net.softm.data.kata.old.PowerConnectReceiverOld;


public class MainConnectWorker extends Worker {

    private static final String TAG = "MainConnectWorker";
    public static String WORK_TAG = "MainConnectWorker";
    Context mContext = null;
    public MainConnectWorker(Context context, WorkerParameters workerParams){
        super(context, workerParams);
        mContext = context;
    }

//    private PowerConnectReceiverOld powerConnectReceiver;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public Result doWork() {
        Fc.i(mContext,TAG,"doWork");
        Data inputData = getInputData();
        long waitingTime = inputData.getLong("waitingTime", 0);
        String adviewUserEarnedReward = inputData.getString("adviewUserEarnedReward");
        AcsUtil acsUtil = new AcsUtil(mContext);
        acsUtil.sendEvent(KEY_GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE);
        try {
            if (!MapUtil.getNaviAutoStart().equals(StartMode.NONE)) {
                if (WApplication.appInfo.getDetail() == null) {
                    Utils.toast(mContext, mContext.getString(R.string.msg_no_app_selected));
                    return Result.failure();
                }
                Fc.i(mContext,TAG,"doWork","run");
                if (getAction().equals(ACTION_SOFTM_POWER_CONNECTED) || getAction().equals(ACTION_SOFTM_ACL_CONNECTED) || getAction().equals(ACTION_SOFTM_WIFI_CONNECTED)) {
                    if (MapUtil.isNaving()) {
                        // if (MapUtil.isNavingForNotiListener()) {
                        Utils.toast(mContext, mContext.getString(R.string.msg_app_is_running, WApplication.appInfo.name));
                        Fc.i(mContext,TAG,"doWork","앱이 실행중입니다.");
                        MainConnectReceiver.stopToShowOverlay();
                        return Result.failure();
                    }
                    // MapUtil.isStartPower();
                    // MapUtil.isStartWifi();
                    // MapUtil.isStartBluetooth();
                    //                boolean isP = MapUtil.isStartPower() && ;
                } else if (getAction().equals(ACTION_SOFTM_POWER_DISCONNECTED) || getAction().equals(ACTION_SOFTM_ACL_DISCONNECTED) || getAction().equals(ACTION_SOFTM_WIFI_DISCONNECTED)) {
                    if (MapUtil.isNaviApp() && MapUtil.isNotNaving()) {
                        if (MapUtil.isOnlyStopAfterNaving()) {
                            Fc.i(mContext,TAG,"doWork","실행중인 앱이 없습니다.");
                            Utils.toast(mContext, mContext.getString(R.string.no_apps_running));
                            MainConnectReceiver.stopToShowOverlay();
                            return Result.failure();
                        }
                    }
                }
            }

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Fc.i(mContext,TAG,"doWork","SHOW.WAITING.OVERLAY.call");
                    acsUtil.sendEventWaitingOverlay(getAction(), MainConnectReceiver.getReceiverType(), (long) (waitingTime / 1000), adviewUserEarnedReward, () -> {
//                                    try {
//                                        Thread.sleep(waitingTime+1000); // 5.5초 Sleep
//                                    } catch (InterruptedException e) {
//                                        throw new RuntimeException(e);
//                                    }
                        MainConnectReceiver.execReceiver(mContext);
                    });
                    MainConnectReceiver.threadStarted = true;
                }
            });
        } catch (Exception ex) {
            Fc.e(mContext,TAG,"doWork",ex.toString());
        }

        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }
}
