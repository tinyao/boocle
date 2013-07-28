package com.czzz.social;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import com.czzz.base.WebActivity;
import com.czzz.utils.HttpDownloadAsyncTask;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

/**
 * 豆瓣OAuth登录 参数及方法
 * @author tinyao
 *
 */
public class DoubanOAuth extends BaseOAuth{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4678332863164914051L;
	public static final String DOUBAN_OAUTH_TOKEN_BASE_URL = "https://www.douban.com/service/auth2/token";
	public static final String DOUBAN_OAUTH_CODE_BASE_URL = "https://www.douban.com/service/auth2/auth";
	
	public static final String DOUBAN_PREF = "social_pref";
	
	public static final String APP_KEY = "0bb168971e811887231439c55b834995";
	public static final String APP_SECRET = "d553b6bbddc3d270";
	public static final String REDIRECT_URL = "http://bookcircle.us/callback";
	
	private String accessToken = "";
	private String expiresIn = "";
	private String refreshToken = "";
	
//	Context context;
//	ProgressDialog pd;
	
	public DoubanOAuth(Context con){
//		this.context = con;
		SharedPreferences sp = con.getSharedPreferences(DOUBAN_PREF, 0);
		accessToken = sp.getString("access_token", "");
		uid = sp.getString("user_id", "");
		expiresIn = sp.getString("expires_in", "");
		refreshToken = sp.getString("refresh_token", "");
	}
	
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getDoubanUserId() {
		return uid;
	}
	
	public String getDoubanName() {
		return name;
	}

	public void setDoubanUserId(String doubanUserId) {
		this.uid = doubanUserId;
	}

	public String getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(String expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * 打开网页，豆瓣认证，回调context的onNewIntent方法
	 * @param context
	 */
	public void lauchforVerifyCode(Context context){
		Uri uri = Uri.parse(
				DOUBAN_OAUTH_CODE_BASE_URL 
				+ "?client_id=" + APP_KEY 
				+ "&redirect_uri=" + REDIRECT_URL 
				+ "&response_type=code");
		Log.d("DEBUG", uri + "");
		Intent it = new Intent(context, WebActivity.class);
		it.putExtra("url", uri + "");
		Activity ai = (Activity)context;
		ai.startActivityForResult(it, 1);
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

		new HttpPostTask(con, listener).execute(DOUBAN_OAUTH_TOKEN_BASE_URL, params);
	}
	
	public void fetchUserInfo(Context con, String uid, HttpListener listener){
		String url = "https://api.douban.com/v2/user/" + uid;
		new HttpDownloadAsyncTask(con, listener).execute(url);
	}

	/**
	 * 从json中解析出accessToken等信息，并存入sharedpreferences
	 * @param json
	 */
	public void parseJson4OAuth(Context context, String json){
		
		Log.d("DEBUG", "oauth_json: " + json);
		
		try {
			JSONObject js = new JSONObject(json);
			
			accessToken = js.getString("access_token");
			uid = js.getString("douban_user_id");
			expiresIn = js.getString("expires_in");
			name = js.getString("douban_user_name");
			refreshToken = js.getString("refresh_token");
			
			SharedPreferences sp = context.getSharedPreferences(DOUBAN_PREF, 0);
			sp.edit().putString("access_token", accessToken)
					.putString("user_id", uid)
					.putString("expires_in", expiresIn)
					.putString("refresh_token", refreshToken).commit();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void parseJson4User(String json){
		
		Log.d("DEBUG", "info_json: " + json);
		
		try {
			JSONObject js = new JSONObject(json);
			
			avatar = js.getString("avatar");
			desc = js.getString("desc");
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
