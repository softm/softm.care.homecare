package net.softm.data.kata.god;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import net.softm.data.kata.god.dto.PrayClass;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.NotificationUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class PrayClassAlarmReceiver extends BroadcastReceiver {
    static String TAG = "PrayClassAlarmReceive  r";
    @Override
    public void onReceive(Context context, Intent intent) {
        L.d(TAG,"onReceive",intent);
        int alarmId = intent.getIntExtra("alarmId",-1);
        PrayClass prayClass = (new Gson()).fromJson(intent.getStringExtra("prayClass"),PrayClass.class);
        NotificationUtil.cancel(context,alarmId);
//        NotificationUtil.init(context).send(prayClass.getTitle(), prayClass.getTitle() + " 기도모임이 있습니다.", true,Integer.valueOf(alarmId));
        String title = "";
        try {
            title = URLDecoder.decode(prayClass.getTitle(),"utf-8");
        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
        }
        NotificationUtil.init(context).sendPray(title, title + " 기도모임이 있습니다.", true,Integer.valueOf(alarmId), prayClass);
//        NotificationUtil.init(context).send("", "기도모임이 " + + "있습니다.", true);
//        OverlayViewUtil.init(context).addMsg("기도모임이 있습니다." ).openOverlayViewShowOnly(true);
        L.d(TAG,"onReceive",prayClass.getTitle());
        return;
    }

}
