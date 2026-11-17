package net.softm.data.kata.web;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import net.softm.lib.BaseActivity;
import net.softm.lib.common.WLog;
import net.softm.data.kata.R;

/**
 * WebChromeClient Custom
 */
public class BaseWebChromeClient extends WebChromeClient {
	private final String TAG = BaseWebChromeClient.class.getName();

	/**
	 * application context
	 */
	private Context mContext = null;
	private BaseWebView mWebview = null;

    public BaseWebChromeClient(Context context, BaseWebView webView){
		mContext = context;
		mWebview = webView;
	}
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (newProgress < 100) {
            WLog.d( "로딩중[" + newProgress + "]");
            ///// >mSplash.setVisibility(View.VISIBLE);
            // Drawable alpha = mSplash.getBackground();
            // alpha.setAlpha(newProgress);
//            if (!mProgress.isShowing()) {
//                mProgress.show();
//            }
        } else {
            WLog.d("로딩완료.");
            ////// >mSplash.setVisibility(View.INVISIBLE);
            // mProgressBar.setVisibility(View.INVISIBLE);
            // mProgressBar.setLayoutParams(new LinearLayout.LayoutParams(0,
            // 0));
//            if (mProgress.isShowing()) {
//                mProgress.dismiss();
//            }
        }
    }

    @Override
	public boolean onConsoleMessage(ConsoleMessage cm) {
		StringBuilder message = new StringBuilder("Console: ")
				.append(cm.message())
				.append(" / SourceId: ")
				.append(cm.sourceId())
				.append(" / LineNumber: ")
				.append(cm.lineNumber());


        String line = cm.message() + " " + cm.sourceId() + ":" + cm.lineNumber();
        ConsoleMessage.MessageLevel level = cm.messageLevel();
        if (level == ConsoleMessage.MessageLevel.TIP)
            WLog.v(TAG, "javascript >> " + message.toString());
        else if (level == ConsoleMessage.MessageLevel.DEBUG)
            WLog.d(TAG, "javascript >> " + message.toString());
        else if (level == ConsoleMessage.MessageLevel.LOG)
            WLog.i(TAG, "javascript >> " + message.toString());
        else if (level == ConsoleMessage.MessageLevel.WARNING)
            WLog.w(TAG, "javascript >> " + message.toString());
        else if (level == ConsoleMessage.MessageLevel.ERROR)
            WLog.e(TAG, "javascript >> " + message.toString());

        return true;
	}

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        WLog.e("onJsAlert : "+ message);
        ((BasicActivity) mContext).alert(mContext, message, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.confirm();
            }
        });
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        WLog.e("onJsConfirm : "+ message);
        ((BasicActivity ) mContext).confirm(mContext, message, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.confirm();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.cancel();
            }
        });
        return true;
    }
//
//    @Override
//    public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
//        Log.d(TAG, "onCreateWindow ");
//
//        BaseWebView popup = new BaseWebView(mContext);
////        popup.setTag(Integer.valueOf(this.IIIIiiIiiI.getChildCount() + 1));
//        popup.setWebViewClient(new BaseWebClient(mContext));
////      popup.setWebChromeClient(new BaseWebChromeClient(mContext, mWebview ){
//        popup.setWebChromeClient(new BaseWebChromeClient(mContext, (BaseWebView) view ){
//            @Override
//            public void onCloseWindow(WebView window) {
//                super.onCloseWindow(window);
//                window.setVisibility(View.GONE);
//                mWebview.removeView(window);
//            }
//        });
//
//        mWebview.addView(popup);
//        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
//        transport.setWebView(popup);
//        resultMsg.sendToTarget();
//
//        return true;
//    }

/**/
    @Override
    public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
        Log.d(TAG, "onCreateWindow ");
        BaseActivity activity = ((BaseActivity) mContext);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        BaseWebView popup = new BaseWebView(mContext);

        JavaScriptBridge jsBridge = new JavaScriptBridge(mContext, mWebview);
        popup.addJavascriptInterface(jsBridge, JavaScriptBridge.CALL_NAME);

//        popup.setTag(Integer.valueOf(this.IIIIiiIiiI.getChildCount() + 1));
        popup.setId(R.id.popup);
        popup.setWebViewClient(new BaseWebClient(mContext));
//      popup.setWebChromeClient(new BaseWebChromeClient(mContext, mWebview ){
        popup.setWebChromeClient(new BaseWebChromeClient(mContext, (BaseWebView) view ){
            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                window.setVisibility(View.GONE);
                BaseActivity activity = ((BaseActivity) mContext);
                ViewGroup mainView = activity.findViewById(R.id.mainView);
                mainView.removeView(window);
            }

        });

        ViewGroup mainView = activity.findViewById(R.id.mainView);
        mainView.addView(popup);
        ViewGroup.LayoutParams params = popup.getLayoutParams();
        params.width = params.MATCH_PARENT;
        params.height = params.MATCH_PARENT;
        popup.setLayoutParams(params);

        ((WebView.WebViewTransport)resultMsg.obj).setWebView(popup);
        resultMsg.sendToTarget();
        return true;
    }

/*
    @Override public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        BaseWebView popup = new BaseWebView(mContext);

        JavaScriptBridge jsBridge = new JavaScriptBridge(mContext, mWebview);
        popup.addJavascriptInterface(jsBridge, JavaScriptBridge.CALL_NAME);

//        popup.setTag(Integer.valueOf(this.IIIIiiIiiI.getChildCount() + 1));
        popup.setId(R.id.popup);
        popup.setWebViewClient(new BaseWebClient(mContext));
//      popup.setWebChromeClient(new BaseWebChromeClient(mContext, mWebview ){

        final Dialog dialog = new Dialog(mContext);
        dialog.setContentView(popup);
        ViewGroup.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        dialog.show();
        popup.setWebChromeClient(new BaseWebChromeClient(mContext, (BaseWebView) view ){
            @Override
            public void onCloseWindow(WebView window) {
                super.onCloseWindow(window);
                dialog.dismiss();
            }

        });
        ((WebView.WebViewTransport)resultMsg.obj).setWebView(popup);
        resultMsg.sendToTarget();
        return true;
    }
*/
}