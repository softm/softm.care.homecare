package net.softm.data.kata.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class StopLocationListener implements LocationListener {
    private boolean isStopped = false;
    private long lastMovingTime;
    private Location lastLocation;
    private final StopDetectedCallback callback;

    public interface StopDetectedCallback {
        void onStopDetected();
        void onLocationChanged(Location location);
    }

    public StopLocationListener(StopDetectedCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);

            if (distance < 1.0) { // 이동 거리가 1미터 미만일 경우
                if (!isStopped) {
                    if (System.currentTimeMillis() - lastMovingTime > 3000) { // 3초 동안 멈춘 상태
                        isStopped = true;
                        callback.onStopDetected();
                    } else {
                        callback.onLocationChanged(location);
                    }
                } else {
                    callback.onLocationChanged(location);
                }
            } else {
                isStopped = false;
                lastMovingTime = System.currentTimeMillis();
                callback.onLocationChanged(location);
            }
        }
        lastLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}
}
