package net.softm.data.kata;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

import net.softm.data.kata.common.WUrl;
import net.softm.data.kata.job.WorkExecutor;
import net.softm.data.kata.util.L;
import net.softm.data.kata.util.OverlayViewUtil;
import net.softm.data.kata.util.Utils;
import net.softm.data.kata.web.BasicActivity;
import net.softm.lib.common.WLog;

import butterknife.ButterKnife;


/**
 * MainActivity
 * @author softm
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class SplashActivity extends BasicActivity {
    final String TAG = "SplashActivity";
    final Activity activity = this;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        L.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        WorkExecutor.isExecuting = false;
        WApplication.mainActivity = this;

        OverlayViewUtil.stopService();

        Intent pIntent = getIntent();
        Boolean pRestartService = pIntent.getBooleanExtra("P_RESTART_SERVICE",false);
        Boolean pReview = pIntent.getBooleanExtra("P_REVIEW",false);
        showProgress();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setClientForWebView();
        handleDeepLink(pIntent);
//        getInstallReferrer();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        WLog.d(TAG, "onNewIntent");
        onCreate(null);
    }

    @Override
    public void showNetworkError(int errorCode, String description) {
        super.showNetworkError(errorCode, description);
//        if (Utils.hasPermission(this)) {
            String webUrlLocal = WUrl.ADDR_LOCAL_ASSET + "/error.html";
//            String webUrlLocal = WUrl.ReqId.WEB_ERROR.getReqUrl();
            WebView popup = activity.findViewById(R.id.popup);
            if (popup == null) {
                mWebview.loadUrl(webUrlLocal);
            } else {
                popup.loadUrl(webUrlLocal);
            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        L.d(TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        L.d(TAG,"onDestroy");
        super.onDestroy();
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    private void handleDeepLink(Intent intent) {
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String docId = data.getQueryParameter("pDocId"); // party docId
                String user = data.getQueryParameter("user");

                L.d(TAG,"DeepLink", "docId: " + docId + ", user: " + user);

                if (docId != null) {
//                    openDocument(docId);
                    Utils.goPartyPray(SplashActivity.this, docId);
                } else {
                    this.finish();
                }
            }
        }
    }
}