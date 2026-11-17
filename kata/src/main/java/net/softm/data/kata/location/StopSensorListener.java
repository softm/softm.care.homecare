package net.softm.data.kata.location;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class StopSensorListener implements SensorEventListener {
    private static final float THRESHOLD = 0.2f; // 정지 상태로 간주할 임계값
    private long lastMovingTime;
    private boolean isStopped = false;
    private final StopDetectedCallback callback;

    public interface StopDetectedCallback {
        void onStopDetected();
    }

    public StopSensorListener(StopDetectedCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        double acceleration = Math.sqrt(x * x + y * y + z * z);

        if (acceleration < THRESHOLD) {
            if (!isStopped) {
                if (System.currentTimeMillis() - lastMovingTime > 3000) { // 3초 동안 정지 상태 유지
                    isStopped = true;
                    callback.onStopDetected();
                }
            }
        } else {
            isStopped = false;
            lastMovingTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 정확도 변경 처리
    }
}
