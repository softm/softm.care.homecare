package net.softm.data.kata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import net.softm.data.kata.util.Fa;
import net.softm.data.kata.util.MapUtil;

public class PackageEventReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageEventReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
//        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        String action = intent.getAction();
        if( action.equals(Intent.ACTION_MY_PACKAGE_REPLACED) ) {
            MapUtil.setAppUpdatedTime();
            MapUtil.setAppUpdated(true);
            MapUtil.setAppUpdatedNotiConfirm(false);
            Fa.i(context, TAG,Fa.EVENT_PACKAGE_REPLACED);
            Toast.makeText(context, R.string.msg_app_updated, Toast.LENGTH_LONG).show();
//            Utils.goPermission(context,"",null);
/** TODO SOFTM : 아래 로직 넣으면 업데이트시 오류 발생함..???
*/
//            Utils.goPermission(context,"",null);
//            WApplication.mInstance.startConnectService(context.getString(R.string.title_noti_runnig_title),NaviStatus.RUNNING, false);
//            Utils.goPermission(mInstance,"accessibility","");
//                if (!Utils.hasPermission(context)) {
//                    crashlytics.log("!Utils.hasPermission(context)");
//                    Utils.goMain(context);
//                } else if ( !MapUtil.isNotiIcon() ) {
//                    if (!PermissionUtil.hasAccessibilityPermissions()) {
//                        crashlytics.log("!Utils.hasPermission(context)");
//                        Utils.goMain(context);
//                    } else {
//                        crashlytics.log("Granted.01");
//                    }
//                } else {
//                    crashlytics.log("Granted.02");
//                }

        }
    }
}