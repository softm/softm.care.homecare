package net.softm.data.kata.engine;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {
    public boolean onStartJob(JobParameters paramJobParameters) {
//        if (PreferenceManager.getDefaultSharedPreferences((Context)this).getBoolean(getString(2131165190), false) && !fg.l((Context)this))
//            fg.a((Context)this);
        Intent in = new Intent(this, SAPService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(in);
            return false;
        }
        startService(in);
        return false;
    }

    public boolean onStopJob(JobParameters paramJobParameters) {
        return false;
    }
}



