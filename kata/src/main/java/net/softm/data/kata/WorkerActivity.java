package net.softm.data.kata;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import net.softm.data.kata.job.WorkExecutor;
import net.softm.data.kata.web.BasicActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;


/**
 * WorkerActivity
 * @author softm
 *
 */
public class WorkerActivity extends BasicActivity {
    final String TAG = "SOFTM";
    static WorkerActivity instance;

    @BindView(R.id.btnTest)
    @Nullable
    Button btnTest = null;
    @OnClick({R.id.btnTest})
    @Optional
    void onClick_btnTest(View v) {
//        MapUtil.runNavi(mContext, NaviDestination.HOME,true);
        Log.d(TAG,"test");
        // TODO softm : Quick Settings Airplane mode on / off
//                AcsUtil acsUtil = new AcsUtil(this);
//        DeviceUtil.tetheringEnable(this,!DeviceUtil.isApOn(this),false);

        WorkExecutor.start(false,new Runnable() {
            @Override
            public void run() {
                WorkerActivity.this.finish();
            }
        });
       // finish();
    }


    @BindView(R.id.btnTest2)
    @Nullable
    Button btnTest2 = null;
    @OnClick({R.id.btnTest2})
    @Optional
    void onClick_btnTest2(View v) {
//        MapUtil.runNavi(mContext, NaviDestination.HOME,true);
        Log.d(TAG,"test");
        // TODO softm : Quick Settings Airplane mode on / off
//                AcsUtil acsUtil = new AcsUtil(this);
//        DeviceUtil.tetheringEnable(this,!DeviceUtil.isApOn(this),false);

        WorkExecutor.stop(false,new Runnable() {
            @Override
            public void run() {
                WorkerActivity.this.finish();
            }
        });


//        WorkQueue.execute();

//        finish();
    }


//    @BindView(R.id.btnTest2)
//    @Nullable
//    Button btnTest2 = null;
//    @Optional
//    void onClick_btnHome2(View v) {
////        MapUtil.runNavi(mContext, NaviDestination.HOME,true);
//        Log.d(TAG,"test");
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String pCommand = intent.getStringExtra("P_COMMAND");
        String pArg1 = intent.getStringExtra("P_ARG1");
        String pArg2 = intent.getStringExtra("P_ARG2");
        String pArg3 = intent.getStringExtra("P_ARG3");
        String pArg4 = intent.getStringExtra("P_ARG4");
        String pArg5 = intent.getStringExtra("P_ARG5");
        try {
            if ( "start".equals(pCommand) ) {
                btnTest.performClick();
            } else if ( "stop".equals(pCommand) ) {
                btnTest2.performClick();
            } else {
            }
        } catch(Exception ex) {

        } finally {
//            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}