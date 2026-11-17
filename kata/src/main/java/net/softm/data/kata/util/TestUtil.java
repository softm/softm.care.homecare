package net.softm.data.kata.util;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.Button;

import androidx.core.app.NotificationCompat;

import net.softm.data.kata.R;

public class TestUtil {
    private static final String TAG = "TestUtil";
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        SpannableString spannableString = new SpannableString("Bold Notification Text");
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_ID")
                .setSmallIcon(R.drawable.logo_icon)
                .setContentTitle("Test Notification")
                .setContentText(spannableString)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }

    public static void testMode(Context context) {
        Activity activity = (Activity)context;
        if ( MapUtil.isTest() ) {
            MapUtil.setTest("0");
        } else {
            MapUtil.setTest("1");
        }
        ((Button)activity.findViewById(R.id.btnTestMode)).setText(MapUtil.isTest()?"Test.On":"Test.OFF");
        Utils.goMain(context);
    }
}

