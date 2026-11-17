//package net.softm.data.kata;
//
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.PowerManager;
//
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//public class MainActivity2 extends AppCompatActivity {
//    private Intent serviceIntent;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
//        boolean isWhiteListing = false;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            isWhiteListing = pm.isIgnoringBatteryOptimizations(getApplicationContext().getPackageName());
//        }
//        if (!isWhiteListing) {
//            Intent intent = new Intent();
//            intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
//            startActivity(intent);
//        }
//
//        if (RealService.serviceIntent==null) {
//            serviceIntent = new Intent(this, RealService.class);
//            startService(serviceIntent);
//        } else {
//            serviceIntent = RealService.serviceIntent;//getInstance().getApplication();
//            Toast.makeText(getApplicationContext(), "already", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (serviceIntent!=null) {
//            stopService(serviceIntent);
//            serviceIntent = null;
//        }
//    }
//}
