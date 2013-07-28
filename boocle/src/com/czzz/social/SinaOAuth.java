package com.czzz.social;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.czzz.social.net.Weibo;
import com.czzz.social.net.WeiboDialogListener;
import com.czzz.utils.HttpDownloadAsyncTask;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;

public class SinaOAuth extends BaseOAuth{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4801003930637006644L;
	public static final String SINA_OAUTH_CODE_BASE_URL = "https://api.weibo.com/oauth2/authorize";
	public static final String SINA_OAUTH_TOKEN_BASE_URL = "https://api.weibo.com/oauth2/access_token"; 
	
	public static final String APP_KEY = "931381862";
	
	public static final String APP_SECRET = "277aec0e9ef5a5bec4bc48dadc595916";
	
	public static final String REDIRECT_URL = "http://www.sina.com";
	
	Weibo weibo;
	
	/**
	 * 打开网页，豆瓣认证，回调context的onNewIntent方法
	 * @param context
	 */
	public void lauchforVerifyCode(Context context){
//		Uri uri = Uri.parse(
//				SINA_OAUTH_CODE_BASE_URL 
//				+ "?client_id=" + APP_KEY 
//				+ "&redirect_uri=" + REDIRECT_URL 
//				+ "&response_type=code");
//		Intent it = new Intent(Intent.ACTION_VIEW, uri);
//		context.startActivity(it);
		
		weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(APP_KEY, APP_SECRET);
		weibo.setRedirectUrl(REDIRECT_URL);
		// 认证
//		weibo.authorize((Activity)context, new WeiboDialogListener());
		
	}
	
	/**
	 * 通过第一步的code，获取accessToken
	 * @param code 第一步认证的code
	 * @param listener AsynTask回调监听:listener的type为1
	 */
	public void fetchAccessToken(Context con, String code, HttpListener listener) {

		List<NameValuePair> params = null;
		params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("client_id",
				APP_KEY));
		params.add(new BasicNameValuePair("client_secret", APP_SECRET));
		params.add(new BasicNameValuePair("redirect_uri", REDIRECT_URL));
		params.add(new BasicNameValuePair("grant_type", "authorization_code"));
		params.add(new BasicNameValuePair("code", code));

		new HttpPostTask(con, listener).execute(SINA_OAUTH_TOKEN_BASE_URL, params);
	}
	
	public void fetchWiboInfo(Context con, HttpListener listener){
		String url = "https://api.weibo.com/2/users/show.json?";
		
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("source", APP_KEY));
		params.add(new BasicNameValuePair("access_token", accessToken));
		params.add(new BasicNameValuePair("uid", uid));
		
		String paramString = URLEncodedUtils.format(params, "utf-8");
		url += paramString;
		
		Log.d("DEBUG", "url: " + url);
		
		new HttpDownloadAsyncTask(con, listener).execute(url);
	}

}
