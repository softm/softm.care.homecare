package net.softm.data.kata.util;

import static net.softm.data.kata.util.MapUtil.appendAppNameForPackage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import androidx.annotation.NonNull;

import net.softm.data.kata.WApplication;
import net.softm.data.kata.common.Package;
import net.softm.data.kata.common.PackageMusic;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class PackageUtil {

    public static String getMyAppName(@NonNull final Context context) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = context.getApplicationInfo();
        String appName = pm.getApplicationLabel(appInfo).toString();
        return appName;
    }

    public static boolean isTmapInstalled(@NonNull final Context context) {
        return MapUtil.isInstallApp(context, Package.SKT_MAP.getId());
    }

    public static boolean isNaviToHomeScreen() {
        return isTmap() || isKakaoNavi() || isKakaoMap() || isNaverMap();
    }

    public static boolean isSDKNavi() {
        return Package.SKT_MAP.equals(WApplication.appInfo.packageName) || Package.KAKAO_NAVI.equals(WApplication.appInfo.packageName);
    }

    public static boolean isSDKHomeCompanyNavi() {
        return Package.SKT_MAP.equals(WApplication.appInfo.packageName);
    }

    public static boolean isTmap() {
        return Package.SKT_MAP.equals(WApplication.appInfo.packageName);
    }

    public static boolean isGoogleMap() {
        return Package.GOOGLE_MAP.equals(WApplication.appInfo.packageName) ||
        Package.GOOGLE_MAP_GO.equals(WApplication.appInfo.packageName) ||
        Package.GOOGLE_MAP_GO2.equals(WApplication.appInfo.packageName)
                ;
    }

    public static boolean isNaverMap() {
        return Package.NAVER_MAP.equals(WApplication.appInfo.packageName);
    }

    public static boolean isOneNavi() {
        return Package.ONENAVI.equals(WApplication.appInfo.packageName);
    }

    public static boolean isMappy() {
        return Package.MAPPY.equals(WApplication.appInfo.packageName);
    }

    public static boolean isAltan() {
        return Package.ATLAN.equals(WApplication.appInfo.packageName);
    }

    public static boolean isKakaoMap() {
        return Package.KAKAO_MAP.equals(WApplication.appInfo.packageName);
    }

    public static boolean isKakaoNavi() {
        return Package.KAKAO_NAVI.equals(WApplication.appInfo.packageName);
    }

    public static boolean isWaze() {
        return Package.WAZE.equals(WApplication.appInfo.packageName);
    }
    public static boolean isOsmAnd() {
        return Package.OSMAND.equals(WApplication.appInfo.packageName);
    }
    public static boolean isOsmAndPlus() {
        return Package.OSMANDPLUS.equals(WApplication.appInfo.packageName);
    }
    public static boolean isSygic() {
        return Package.SYGIC.equals(WApplication.appInfo.packageName);
    }

    public static boolean isMapsMe() {
        return Package.MAPSME.equals(WApplication.appInfo.packageName);
    }

    public static boolean isGoDuck() {
        return Package.GODUCK.equals(WApplication.appInfo.packageName);
    }

    public static boolean isBaidu() {
        return Package.BAIDU.equals(WApplication.appInfo.packageName);
    }


    public static String encodeIcon(Drawable icon){
        String appIcon64 = new String();
        Drawable ic = icon;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap bitmap = null;
        try {
            if ( ic instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) ic).getBitmap();
            } else {
                bitmap = Bitmap.createBitmap(ic.getIntrinsicWidth(), ic.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                ic.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                ic.draw(canvas);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                appIcon64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                //	String imageString = encodeIcon(packageInfo.loadIcon(pm));
            }
        }

        return appIcon64;
    }

    public static HashMap<String, Object> getAppInfo(String key) {

        PackageManager pm = WApplication.mInstance.getPackageManager();
        HashMap<String, Object> data = new HashMap<String, Object>();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo( key, PackageManager.GET_META_DATA);
            String name = appInfo.loadLabel(pm).toString().replaceAll(System.getProperty("line.separator"), " ");
            data.put("name", appendAppNameForPackage(appInfo.packageName,name));
            data.put("icon", "data:image/png;base64," + encodeIcon(appInfo.loadIcon(pm)));
            data.put("package", appInfo.packageName);
            data.put("installed", true);
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
            String name = "";
            Package pkg = Package.value(key);
            if ( pkg != null ) {
                data.put("name", pkg.getName());
                data.put("icon", pkg.getIcon());
                data.put("seq", pkg.getSeq());
                data.put("package", key);
                data.put("installed", false);
            } else {
                PackageMusic pkgMusic = PackageMusic.value(key);
                if ( pkgMusic != null ) {
                    data.put("name", pkgMusic.getName());
                    data.put("icon", pkgMusic.getIcon());
                    data.put("seq", pkgMusic.getSeq());
                    data.put("package", key);
                    data.put("installed", false);
                } else {
                    data.put("name", name);
                    data.put("icon", null);
                    data.put("package", key);
                    data.put("installed", false);
                }
            }
        }
        return data;
    }

    public static boolean isInstall(String key) {

        PackageManager pm = WApplication.mInstance.getPackageManager();
        HashMap<String, Object> data = new HashMap<String, Object>();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo( key, PackageManager.GET_META_DATA);
            String name = appInfo.loadLabel(pm).toString().replaceAll(System.getProperty("line.separator"), " ");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public static float getAndroidVersionId() {
        int sdkInt = android.os.Build.VERSION.SDK_INT;

        switch (sdkInt) {
            case 1:
                return 1.0f;
            case 2:
                return 1.1f;
            case 3:
                return 1.5f;
            case 4:
                return 1.6f;
            case 5:
            case 6:
            case 7:
                return 2.0f;
            case 8:
                return 2.2f;
            case 9:
            case 10:
                return 2.3f;
            case 11:
            case 12:
            case 13:
                return 3.0f;
            case 14:
            case 15:
                return 4.0f;
            case 16:
                return 4.1f;
            case 17:
                return 4.2f;
            case 18:
                return 4.3f;
            case 19:
                return 4.4f;
            case 20:
                return 4.4f; // 4.4W treated as 4.4
            case 21:
                return 5.0f;
            case 22:
                return 5.1f;
            case 23:
                return 6.0f;
            case 24:
                return 7.0f;
            case 25:
                return 7.1f;
            case 26:
                return 8.0f;
            case 27:
                return 8.1f;
            case 28:
                return 9.0f;
            case 29:
                return 10.0f;
            case 30:
                return 11.0f;
            case 31:
                return 12.0f;
            case 32:
                return 12.1f; // Android 12L
            case 33:
                return 13.0f;
            case 34:
                return 14.0f;
            case 35:
                return 15.0f;
            default:
                return -1.0f; // Unknown Android Version
        }
    }


}