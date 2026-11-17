package net.softm.data.kata.web;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebViewClient;

import net.softm.lib.BaseActivity;
import net.softm.lib.Constant;
import net.softm.data.kata.R;
import net.softm.data.kata.WApplication;



/**
 * BaseActivity
 * 기본엑티비티
 * @author softm
 */
public abstract class BasicActivity extends BaseActivity {
	public BaseWebChromeClient mChromeClient;
	public BaseWebView mWebview;

	public ProgressDialog mProgress;
	public static final String LOG_TAG = "softm";

	protected WApplication mApp;
	public boolean DEBUG = Constant.DEBUG;

	private InputMethodManager imm;
	private static final Boolean bUseImm = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// keyboard
		this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		super.onCreate(savedInstanceState);
		mApp = (WApplication) getApplicationContext();
	}
	public void startProgressBar() {
		stopProgressBar();
		showProgress();
	}
	public void showProgress(int time, View view){
		showProgress();
		view.postDelayed(new Runnable() {
			@Override
			public void run() {
				stopProgress();
			}
		}, time * 1000);
	}

	public void showProgress(){
		if(isFinishing()){
			return;
		}


		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(mProgress == null) {
					mProgress = new ProgressDialog(BasicActivity.this, R.style.MyDialog);

					mProgress.setCancelable(true);
//			ProgressBar pbar = new ProgressBar(this);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//				pbar.setIndeterminateDrawable(getDrawable(R.drawable.progress_bar_custom));
//			}
//			mProgress.addContentView(pbar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
					mProgress.setCanceledOnTouchOutside(false);
// 위에는 내로직 ////////////////////
				}

				mProgress.show();
			}
		});

	}

	public void stopProgress(){
//		if(isFinishing()){
//			return;
//		}
		try {
			if (mProgress != null && mProgress.isShowing()) {
				mProgress.dismiss();
				mProgress = null;
			}
		} catch	(Exception ex) {

		}
	}

	protected void setClientForWebView() {

		mWebview = (BaseWebView) findViewById(R.id.webview);

		BaseWebClient client = new BaseWebClient(this);
		mChromeClient = new BaseWebChromeClient(this, mWebview);

		JavaScriptBridge jsBridge = new JavaScriptBridge(this, mWebview);
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
		mWebview.addJavascriptInterface(jsBridge, JavaScriptBridge.CALL_NAME);
		//}
		mWebview.setWebViewClient(client);
		mWebview.setWebChromeClient(mChromeClient);

		mWebview.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				Uri downloadUri = Uri.parse(url);

				CookieManager cm = CookieManager.getInstance();
				String cookie = cm.getCookie(url);

				String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);

				DownloadManager.Request request = new DownloadManager.Request(downloadUri);
				request.setTitle(fileName);
				request.setDescription(url);
				request.setMimeType(mimetype);

				if(cookie != null){
					request.addRequestHeader("Cookie", cookie);
				}

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
					request.allowScanningByMediaScanner();
					request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
					request.setVisibleInDownloadsUi(true);
				}
				request.setDestinationInExternalPublicDir( Environment.DIRECTORY_DOWNLOADS, fileName);

				DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
				dm.enqueue(request);
			}
		});
	}
	protected int iHTTPStatus;
	/**
	 * App시작 서버상태체크
	 * IntroActivity에서 호출됨
	 * WUtil.startAppByServerState(this)
	 */
	public void showNetworkError(int errorCode, String description) {
		//TODO SOFTM complete 네트웍 에러 - 디자인필요
		// 네트웍 에러 Activty
		// 네트웍 에러 Layout
		iHTTPStatus = errorCode;
		switch(errorCode) {
			case WebViewClient.ERROR_AUTHENTICATION:               // 서버에서 사용자 인증 실패
			case WebViewClient.ERROR_BAD_URL:                            // 잘못된 URL
			case WebViewClient.ERROR_CONNECT:                           // 서버로 연결 실패
			case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:     // SSL handshake 수행 실패
			case WebViewClient.ERROR_FILE:                                   // 일반 파일 오류
			case WebViewClient.ERROR_FILE_NOT_FOUND:                // 파일을 찾을 수 없습니다
			case WebViewClient.ERROR_HOST_LOOKUP:            // 서버 또는 프록시 호스트 이름 조회 실패
			case WebViewClient.ERROR_IO:                               // 서버에서 읽거나 서버로 쓰기 실패
			case WebViewClient.ERROR_PROXY_AUTHENTICATION:    // 프록시에서 사용자 인증 실패
			case WebViewClient.ERROR_REDIRECT_LOOP:                // 너무 많은 리디렉션
			case WebViewClient.ERROR_TIMEOUT:                          // 연결 시간 초과
			case WebViewClient.ERROR_TOO_MANY_REQUESTS:            // 페이지 로드중 너무 많은 요청 발생
			case WebViewClient.ERROR_UNKNOWN:                         // 일반 오류
			case WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME:  // 지원되지 않는 인증 체계
			case WebViewClient.ERROR_UNSUPPORTED_SCHEME:
			case Constant.HTTP_CODE_NETWORK: // 네트웍 소켓에러.
				//TODO SOFTM complete 네트웍 에러 - 디자인필요
				// 네트웍 에러 Activty
				// 네트웍 에러 Layout
//				((BaseActivity)context).toast(description);
//				view.loadData("<B>" +description + "</B>", "text/html; charset=utf-8", "UTF-8");
//				toast("" +description + "");
				stopProgress();
				break;
		}
	}

	public AlertDialog dialog = null;
	public void confirm(String title, String pBtnText, String nBtnText, String msg, int msgGravity, View.OnClickListener onClick, View.OnClickListener onCancel ) {
		dialog = new AlertDialog(BasicActivity.this,title,pBtnText,nBtnText,msg, msgGravity );

		dialog.mPListener = onClick;
		dialog.mNListener = onCancel;
		if(!this.isFinishing()) {
			dialog.show();
		}
//		WLog.i("TEST",msg);
//		WLog.i("TEST",""+((View)(dialog.getButton(AlertDialog.BUTTON_POSITIVE).getParent())).getId());
//		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
	}

	public void alert(int msg) {
//		String strMsg    = getString(msg);
		alert(getString(msg));
	}

	public void alert(String msg) {
		alert("", msg, 0, null );
	}
	public void alert(String title, String msg, int msgGravity, View.OnClickListener onClick ) {
		dialog = new AlertDialog(BasicActivity.this,title,msg, msgGravity );
		dialog.mPListener = onClick;
		if(!this.isFinishing()) {
			dialog.show();
		}
//		WLog.i("TEST",msg);
//		WLog.i("TEST",""+((View)(dialog.getButton(AlertDialog.BUTTON_POSITIVE).getParent())).getId());
//		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
	}

	public void alert(String msg, View.OnClickListener onClick ) {
		dialog = new AlertDialog(BasicActivity.this,"",(msg), 0 );
		dialog.mPListener = onClick;
		if(!this.isFinishing()) {
			dialog.show();
		}
	}

	public void alert(int msg, View.OnClickListener onClick ) {
		dialog = new AlertDialog(BasicActivity.this,"",getString(msg), 0 );
		dialog.mPListener = onClick;
		if(!this.isFinishing()) {
			dialog.show();
		}
	}

	// LotDialogUtil2.alert --> alert
	public void confirm(Context ctx,int msg, View.OnClickListener onClick, View.OnClickListener onCancel ) {
		confirm("",null,null,getString(msg),0, onClick,onCancel);
	}
	// LotDialogUtil2.alert --> alert
	public void confirm(Context ctx,String msg, View.OnClickListener onClick, View.OnClickListener onCancel ) {
		confirm("",null,null,msg,0, onClick,onCancel);
	}

	// LotDialogUtil2.alert --> alert
	public void confirm(Context ctx,String pId, String nId, String msg, View.OnClickListener onClick, View.OnClickListener onCancel ) {
		confirm("",pId,nId,msg,0, onClick, onCancel );
	}

	// LotDialogUtil2.alert --> alert
	public void alert(Context ctx,int msg, View.OnClickListener onClick ) {
		alert("",getString(msg),0, onClick);
	}
	// LotDialogUtil2.alert --> alert
	public void alert(Context ctx,String msg, View.OnClickListener onClick ) {
		alert("",msg,0, onClick);
	}

	// LotDialogUtil2.alert --> alert
	public void alert(Context ctx,int msg, int msgGravity, View.OnClickListener onClick ) {
		alert(null,getString(msg),msgGravity, onClick );
	}
	public void alert(String title, String msg, View.OnClickListener onClick ) {
		alert(title,msg,0, onClick );
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopProgress();
	}
}

