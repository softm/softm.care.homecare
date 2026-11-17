package net.softm.data.kata.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DeviceInfoExtractor {

    private Context context;

    public DeviceInfoExtractor(Context context) {
        this.context = context;
    }

    Map<String, Object> deviceInfo = new HashMap<>();

    // 기기 정보 추출
    public Map<String, Object> getDeviceInfo() {

        // 디바이스 브랜드 및 모델
        deviceInfo.put("brand", Build.BRAND);
        deviceInfo.put("model", Build.MODEL);

        // 여유 RAM
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        deviceInfo.put("ramAvailable", formatSize(memoryInfo.availMem));

        // 디스크 여유 공간
        File path = Environment.getDataDirectory();
        long freeSpace = path.getFreeSpace();
        deviceInfo.put("diskSpaceAvailable", formatSize(freeSpace));

        // 화면 방향
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        deviceInfo.put("orientation", metrics.heightPixels > metrics.widthPixels ? "Portrait" : "Landscape");

        return deviceInfo;
    }

    Map<String, Object> osInfo = new HashMap<>();
    // 운영체제 정보 추출
    public Map<String, Object> getOSInfo() {
        osInfo.put("version", Build.VERSION.RELEASE);
        osInfo.put("rooted", isDeviceRooted());
        osInfo.put("orientation", context.getResources().getDisplayMetrics().heightPixels >
                context.getResources().getDisplayMetrics().widthPixels ? "Portrait" : "Landscape");

        return osInfo;
    }

    // 디바이스 Root 여부 확인
    private boolean isDeviceRooted() {
        String[] paths = {"/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    // 바이트를 사람이 읽을 수 있는 형식으로 변환
    private String formatSize(long size) {
        double kb = size / 1024.0;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;
        if (gb >= 1) {
            return String.format("%.2f GB", gb);
        } else if (mb >= 1) {
            return String.format("%.2f MB", mb);
        } else {
            return String.format("%.2f KB", kb);
        }
    }

    // 실제 메서드 구현
    public String getBrand() {
        return String.valueOf(deviceInfo.get("brand"));
    }

    public String getModel() {
        return String.valueOf(deviceInfo.get("model"));
    }

    public String getRamAvailable() {
        return String.valueOf(deviceInfo.get("ramAvailable"));
    }

    public String getDiskSpaceAvailable() {
        return String.valueOf(deviceInfo.get("diskSpaceAvailable"));
    }

    public String getOrientation() {
        return String.valueOf(deviceInfo.get("orientation"));
    }

    public String getVersion() {
        return String.valueOf(osInfo.get("version"));
    }

    public String getOSOrientation() {
        return String.valueOf(osInfo.get("orientation"));
    }
}
