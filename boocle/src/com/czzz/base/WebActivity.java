package com.czzz.base;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.view.Window;
import com.czzz.demo.R;

public class WebActivity extends BaseActivity {

	private WebView mWebView;
	private Handler mHandler = new Handler();
	Intent ii;

	SharedPreferences sp;
	
	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle icicle) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);//让进度条显示在标题栏上  
		
		super.onCreate(icicle);
		
		mActionBar.setDisplayShowTitleEnabled(true);
		
		setContentView(R.layout.webview_lay);
		mWebView = (WebView) findViewById(R.id.webview);
		
		ii = getIntent();
		String url = ii.getStringExtra("url");
		String title = ii.getStringExtra("title");
		if(title != null){
			mActionBar.setTitle("书评: " + title);
		} else {
			//mActionBar.setTitle("书评: " + title);
		}
 
		mWebView.getSettings().setJavaScriptEnabled(true);
		
		if(url.contains("renren")){
			CookieSyncManager.createInstance(this);
	        CookieManager cookieManager = CookieManager.getInstance();
	        cookieManager.removeAllCookie();
		}
		
		mWebView.setWebViewClient(client);
//		mWebView.getSettings().setSupportZoom(true);
//		mWebView.getSettings().setBuiltInZoomControls(true);
//		mWebView.getSettings().setDefaultZoom(ZoomDensity.CLOSE);//默认缩放模式
//		mWebView.getSettings().setUseWideViewPort(true);
//		mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		
		mWebView.addJavascriptInterface(new Object() {
			public void clickOnAndroid() {
				mHandler.post(new Runnable() {
					public void run() {
						mWebView.loadUrl("javascript:wave()");
					}
				});
			}
		}, "demo");
		
		sp = this.getSharedPreferences("social_pref", 0);
		
		mWebView.clearCache(true);
		mWebView.loadUrl(url);
		
	}

	
	private WebViewClient client = new WebViewClient(){
		
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			
			view.loadUrl(url);
			return true;
		}
		
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			view.clearCache(true);
			setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			
			setSupportProgressBarIndeterminateVisibility(true);
			/**
			 * 豆瓣OAUTH认证
			 */
			if (url.indexOf("http://bookcircle.us/callback?code=") != -1) {  
				int start = url.indexOf("http://bookcircle.us/callback?code=") + 35;  
                String verifyCode = url.substring(start, url.length());
                ii.putExtra("code", verifyCode);
                setResult(100, ii); 
                Log.d("DEBUG", verifyCode);
                finish(); 
                return;
			}
			
			/**
			 * 人人
			 */
			if (url.indexOf("access_token=") != -1){
				
				Log.d("DEBUG", "url: " + url);
				
				
				int token_start = url.indexOf("access_token=");
				int expire_start = url.indexOf("&expires_in");
				int scope = url.indexOf("&scope=");
				
				String accessToken = url.substring(token_start + 13, expire_start);
				String expiresIn = url.substring(expire_start + 11, scope == -1 ? url.length() : scope);
				
				try {
					accessToken = URLDecoder.decode(accessToken, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Log.d("DEBUG", "access: " + accessToken
						+ "\nexpires: " + expiresIn);
				
				sp.edit().putString("renren_token", accessToken)
					.putString("renren_expires_In", expiresIn).commit();
				
				ii.putExtra("ren_access_token", accessToken);
                setResult(101, ii); 
				
				finish();
				return;
			}
			
			super.onPageStarted(view, url, favicon);
			
		}
		
	};
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
