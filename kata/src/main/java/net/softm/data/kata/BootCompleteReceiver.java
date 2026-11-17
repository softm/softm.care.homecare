package net.softm.data.kata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import net.softm.data.kata.common.Const;
import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.MapUtil;

public class BootCompleteReceiver extends BroadcastReceiver {
    static int cnt = 0;
    SharedPreferences pref;
    SharedPreferences.Editor spedit;
    String TAG = "BootCompleteReceiver";

    public void onReceive(Context context , Intent intent) {

        String action = intent.getAction();

//        if (action.equals("android.intent.action.BOOT_COMPLETED"))
//        {
            Fa.i(context,TAG, Fa.EVENT_BOOT_COMPLETE);
            MapUtil.setNotNaving(); // 안내 가능하도록
            if ( MapUtil.isStart() ) {
                Toast.makeText(context, context.getString(R.string.app_name) + " 시작", Toast.LENGTH_SHORT).show();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
/** 티맵자동실행 로직 */
//                            if (Utils.hasPermission(context)) {
//                                 Fc.i(context,TAG,"onReceive","ServiceUtil.init(context).startService");
//                                 ServiceUtil.init(context).startService(context.getString(R.string.title_noti_runnig_title), NaviStatus.RUNNING, MapUtil.isPowerConnected(context));
//                            } else {
////                                Utils.goMain(context);
//                            }
                            Log.d(TAG, "BOOT-RUN");
                        }
                    }, Const.BOOT_COMPLETE_START_NAVI_DELAY_TIME);
//                } else {
//                    context.startService(serviceIntent);
//                }
            }
//        }
    }
}