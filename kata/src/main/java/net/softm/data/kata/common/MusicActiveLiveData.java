package net.softm.data.kata.common;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;

import androidx.lifecycle.LiveData;

public class MusicActiveLiveData extends LiveData<Boolean> {

    private AudioManager audioManager;
    private Handler handler = new Handler();
    private Runnable checkMusicActiveTask = new Runnable() {
        @Override
        public void run() {
            Boolean currentMusicActive = audioManager.isMusicActive();
            if (currentMusicActive != getValue()) {
                setValue(currentMusicActive);
            }
            handler.postDelayed(this, 1000); // 매 초마다 상태를 업데이트
        }
    };

    public MusicActiveLiveData(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    protected void onActive() {
        handler.postDelayed(checkMusicActiveTask, 1000);
    }

    @Override
    protected void onInactive() {
        handler.removeCallbacks(checkMusicActiveTask);
    }
}
