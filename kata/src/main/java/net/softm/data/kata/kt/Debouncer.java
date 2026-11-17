package net.softm.data.kata.kt;

import java.util.Timer;
import java.util.TimerTask;

public class Debouncer {

    private Timer timer;
    private final long delayInMillis;

    // 생성자에서 딜레이 설정
    public Debouncer(long delayInMillis) {
        this.delayInMillis = delayInMillis;
    }

    public void debounce(Runnable task) {
        // 기존 타이머 취소
        if (timer != null) {
            timer.cancel();
        }

        // 새로운 타이머 설정
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, delayInMillis);
    }
}