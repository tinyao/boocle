package com.czzz.social.net;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.Window;
import com.czzz.base.BaseActivity;
import com.czzz.demo.R;
import com.czzz.social.SinaOAuth;

/***
 * 自定义webview(Oauth认证)
 * 
 * @author zhangjia
 * 
 */
public class MyWebViewActivity extends BaseActivity implements WeiboDialogListener {
	private final String TAG = "jjhappyforever";
	private WebView mWebView;
	private WeiboDialogListener mListener;
	private LinearLayout linearLayout;
	private Intent ii;

	public void InitWebView() {
		linearLayout = (LinearLayout) findViewById(R.id.web_parent);
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WeiboWebViewClient());
		
		WeiboParameters parameters = new WeiboParameters();
		parameters.add("client_id", SinaOAuth.APP_KEY);
        parameters.add("response_type", "token");
        parameters.add("redirect_uri", SinaOAuth.REDIRECT_URL);
        parameters.add("display", "mobile");
		
        String url = Weibo.URL_OAUTH2_ACCESS_AUTHORIZE + "?" + Utility.encodeUrl(parameters);
		
        Log.d("DEBUG", url);
        
		mWebView.loadUrl(url);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);//让进度条显示在标题栏上
		
		super.onCreate(savedInstanceState);
		
		mListener = this;
		
		setContentView(R.layout.webview_lay);
		
		ii = getIntent();
		
		InitWebView();
	}

	/***
	 * WebViewClient
	 * 
	 * @author zhangjia
	 * 
	 */
	private class WeiboWebViewClient extends WebViewClient {

		/***
		 * 地址改变都会调用
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, "Redirect URL: " + url);
			if (url.startsWith(Weibo.getInstance().getRedirectUrl())) {
				handleRedirectUrl(view, url);
				return true;
			}
			// launch non-dialog URLs in a full browser
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			return true;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);

			mListener.onError(new DialogError(description, errorCode,
					failingUrl));
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.d(TAG, "onPageStarted URL: " + url);
			// google issue. shouldOverrideUrlLoading not executed
			
			setSupportProgressBarIndeterminateVisibility(true);
			/**
			 * 点击授权，url正确
			 */
			if (url.startsWith(Weibo.getInstance().getRedirectUrl())) {
				handleRedirectUrl(view, url);
				view.stopLoading();

				return;
			}
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.d(TAG, "onPageFinished URL: " + url);
			super.onPageFinished(view, url);
			setSupportProgressBarIndeterminateVisibility(false);
//			linearLayout.setVisibility(View.GONE);
		}

		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			handler.proceed();
		}

	}

	private void handleRedirectUrl(WebView view, String url) {
		Bundle values = Utility.parseUrl(url);
		String error = values.getString("error");
		String error_code = values.getString("error_code");
		// 授权成功
		if (error == null && error_code == null) {
			mListener.onComplete(values);
			// 拒绝失败等
		} else if (error.equals("access_denied")) {
			mListener.onCancel();
		} else {
			// 异常
			mListener.onWeiboException(new WeiboException(error, Integer
					.parseInt(error_code)));
		}
	}

	@Override
	public void onComplete(Bundle values) {
		/***
		 * 在这里要save the access_token
		 */
		String token = values.getString("access_token");
		String remind_in = values.getString("remind_in");
		String expires_in = values.getString("expires_in");
		String uid = values.getString("uid");

		Log.d("DEBUG", "accessToken: " + token);
		
		SharedPreferences preferences = getSharedPreferences("oauth_verify",
				0);
		Editor editor = preferences.edit();
		editor.putString("sina_token", token)
			.putString("sina_remind_in", remind_in)
			.putString("expires_in", expires_in)
			.putString("uid", uid).commit();

		this.setIntent(new Intent());
		ii.putExtra("sina_token", token);
		ii.putExtra("sina_uid", uid);
		setResult(102, ii);
		finish();
	}

	@Override
	public void onWeiboException(WeiboException e) {
		e.printStackTrace();
	}

	@Override
	public void onError(DialogError e) {
		e.printStackTrace();
	}

	@Override
	public void onCancel() {
		finish();
	}

}
