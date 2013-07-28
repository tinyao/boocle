package com.czzz.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.view.Menu;
import com.czzz.base.BaseActivity;
import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.base.WebActivity;
import com.czzz.bookcircle.MyApplication;
import com.czzz.bookcircle.UserUtils;
import com.czzz.social.BaseOAuth;
import com.czzz.social.DoubanOAuth;
import com.czzz.social.RenRenOAuth;
import com.czzz.social.SinaOAuth;
import com.czzz.social.net.AccessToken;
import com.czzz.social.net.DialogError;
import com.czzz.social.net.MyWebViewActivity;
import com.czzz.social.net.Weibo;
import com.czzz.social.net.WeiboDialogListener;
import com.czzz.social.net.WeiboException;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class LoginActivity extends BaseActivity implements HttpListener{

	Button loginBtn;
	View loginDouban, loginRen, loginSina;
	SharedPreferences sp;
	
	EditText accountEdt, passEdt;
	
	
	Weibo weibo;
	int taskType;
	
	private DoubanOAuth dbOAuth;
	private RenRenOAuth renOAuth;
	private SinaOAuth sinaOAuth;
	
	private int third_type = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		// first login, go to guide-slide
//		sp = this.getSharedPreferences("config", 0);
//		if(sp.getBoolean("first_launch", true)) {
			Intent intent = new Intent(this, GuideSlideActivity.class);
		    Log.d("DEBUG", "launch Guide slide activity");
		    startActivity(intent);
		    
//		    sp.edit().putBoolean("first_launch", false).commit();
//		}
		
		setContentView(R.layout.account_login);
		
		loginBtn = (Button) findViewById(R.id.welcome_login);
		loginDouban = (View) findViewById(R.id.login_douban);
		loginRen = (View) findViewById(R.id.login_renren);
		loginSina = (View) findViewById(R.id.login_sina);
		accountEdt = (EditText)findViewById(R.id.user_account);
		passEdt = (EditText)findViewById(R.id.user_account_password);
		
		loginBtn.setOnClickListener(listener);
		loginDouban.setOnClickListener(listener);
		loginSina.setOnClickListener(listener);
		loginRen.setOnClickListener(listener);
		
		passEdt.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_GO){
					login();
				}
				return false;
			}
			
		});
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		// return from mainActivity, means have loged in
		sp = this.getSharedPreferences("config", 0);
		if(sp.getBoolean("loged_in", false)){
			finish();
		}
	}

	OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.welcome_login:
				login();
				break;
			case R.id.login_douban:
				dbOAuth = new DoubanOAuth(LoginActivity.this);
				dbOAuth.lauchforVerifyCode(LoginActivity.this);
				third_type = 1;
				break;
			case R.id.login_renren:
				Intent it = new Intent(LoginActivity.this, WebActivity.class);
				it.putExtra("url", RenRenOAuth.OUATH_URL);
				startActivityForResult(it, 2);
				third_type = 2;
				break;
			case R.id.login_sina:
				weibo = Weibo.getInstance();
				weibo.setupConsumerConfig(SinaOAuth.APP_KEY,
						SinaOAuth.APP_SECRET);
				weibo.setRedirectUrl(SinaOAuth.REDIRECT_URL);
				
				Intent ii = new Intent(LoginActivity.this, MyWebViewActivity.class);
				startActivityForResult(ii, 3);
				third_type = 3;
				break;
			}
		}
		
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getSupportMenuInflater().inflate(R.menu.menu_register, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private ProgressDialog pd;
	
	protected void login() {
		// TODO Auto-generated method stub
		
		String email = accountEdt.getText().toString();
		String passwd = passEdt.getText().toString();
		if(email.equals("") || passwd.equals("")){
			Crouton.makeText(this, R.string.hint_login_account_error, Style.ALERT).show();
			return;
		}
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(passEdt.getWindowToken(), 0);

		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("passwd", passwd));
		
		pd = new ProgressDialog(this);
		pd.setMessage(getResources().getString(R.string.hint_logining));
		pd.show();
		
		this.taskType = HttpListener.ACCOUNT_LOGIN;
		new HttpPostTask(this, (HttpListener)this).execute(Pref.LOGIN_URL, params);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		case R.id.abs__home:
			finish();
			break;
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_register:
			Intent registerIntent = new Intent(this, RegisterActivity.class);
			startActivity(registerIntent);
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
//	/**
//	 * 豆瓣认证完成后，获取返回的code
//	 * 
//	 * @param intent
//	 */
//	@Override
//	protected void onNewIntent(Intent intent) {
//		super.onNewIntent(intent);
//		Log.d("DEBUG", "onNewIntent---");
//		// 在这里处理获取返回的code参数
//		Uri uri = intent.getData();
//		String code = uri.getQueryParameter("code");
//		Log.d("DEBUG", "code: " + code);
//		this.taskType = HttpListener.DOUBAN_OAUTH_JSON;
//		dbOAuth.fetchAccessToken(code, this);
//	}
	
//	/**
//	 * 用于新浪微博OAuth认证的回调监听器
//	 * @author tinyao
//	 *
//	 */
//	class MyWeiboDialogListener implements WeiboDialogListener {
//
//		@Override
//		public void onComplete(Bundle values) {
//			/***
//			 * 保存token and expires_in
//			 */
//			String token = values.getString("access_token");
//			String expires_in = values.getString("expires_in");
//			String uid = values.getString("uid");
//			
//			AccessToken accessToken = new AccessToken(token,
//					SinaOAuth.APP_SECRET);
//			accessToken.setExpiresIn(expires_in);
//			weibo.setAccessToken(accessToken);
//
//			String passwd = UserUtils.generatePass(3, uid); 
//			
//			Intent registIntent = new Intent(LoginActivity.this, RegisterActivity.class);
//			registIntent.putExtra("third_login", true);
//			registIntent.putExtra("name", dbOAuth.getDoubanName());
//			registIntent.putExtra("passwd", passwd);
//			startActivity(registIntent);
//			
//		}
//
//		@Override
//		public void onError(DialogError e) {
//			Toast.makeText(getApplicationContext(),
//					"Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
//		}
//
//		@Override
//		public void onCancel() {
//			Toast.makeText(getApplicationContext(), "Auth cancel",
//					Toast.LENGTH_LONG).show();
//		}
//
//		@Override
//		public void onWeiboException(WeiboException e) {
//			Toast.makeText(getApplicationContext(),
//					"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
//					.show();
//		}
//
//	}
	
	private void checkBindUser(String bind_id, int bind_from, String passwd){
		List<NameValuePair> params = null;
		params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("bind_id",
				bind_id));
		params.add(new BasicNameValuePair("bind_from", ""+bind_from));
		params.add(new BasicNameValuePair("passwd", passwd));

		this.taskType = HttpListener.BIND_USER_CHECK;
		new HttpPostTask(this, this).execute(Pref.CHECK_BIND_URL, params);
	}

	
	// 第三方登录
	
	// 根据bind_id和from，判断用户是否已存在
	
	// 若存在，直接使用uid跟pass登录
	
	// 若不存在，获取social信息，完成注册
	
	String passwd;
	
	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		switch(taskType){
		case HttpListener.DOUBAN_OAUTH_JSON: // 获取到access token等json
			// 解析json，获取accessToken
			
			dbOAuth.parseJson4OAuth(this, String.valueOf(data));
			
			Log.d("DEBUG", "oauth: " + dbOAuth.getAccessToken() + "--"
					+ dbOAuth.getDoubanUserId());
			
			// 获取豆瓣用户信息
			this.taskType = HttpListener.DOUBAN_INFO;
			dbOAuth.fetchUserInfo(this, dbOAuth.getDoubanUserId(), this);
			
			break;
		case HttpListener.DOUBAN_INFO:
			dbOAuth.parseJson4User("" + data);
			
			// 判断是否已存在用户
			passwd = UserUtils.generatePass(1, dbOAuth.getDoubanUserId()); 
			Log.d("DEBUG", "password: " + passwd);
			Log.d("DEBUG", "avatar: " + dbOAuth.avatar);
			checkBindUser(dbOAuth.getDoubanUserId(), 1, passwd);
			
			break;
		case HttpListener.ACCOUNT_LOGIN:
			checkUserLogin("" + data);
			break;
		case HttpListener.BIND_USER_CHECK:
			Log.d("DEBUG", "out: " + data);

			BaseOAuth baseOauth = new BaseOAuth();
			
			switch(third_type){
			case 1:
				baseOauth = dbOAuth;
				baseOauth.source = 1;
				break;
			case 2:
				baseOauth = renOAuth;
				baseOauth.source = 2;
				break;
			case 3:
				baseOauth = sinaOAuth;
				baseOauth.source = 3;
				break;
			}
			
			try {
				JSONObject json = new JSONObject(""+data);
				int status = json.getInt("status");
				if(status == 3){
					Intent registIntent = new Intent(this, RegisterActivity.class);
					baseOauth.passwd = passwd;
					registIntent.putExtra("source_info", (Serializable)baseOauth);
					registIntent.putExtra("third_login", true);
					startActivity(registIntent);
					
				}else if(status == 2){
					Crouton.makeText(this, R.string.hint_login_error, Style.ALERT).show();
				}else{
					MyApplication.accoutPref.edit().putString("password", passwd).commit();
					checkUserLogin("" + data);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			pd.dismiss();
			
			break;
			
		case RENREN_INFO_UID:
			try {
				JSONObject renJson = new JSONObject("" + data);
				renOAuth.uid = "" + renJson.getInt("uid");
				
				this.taskType = HttpListener.RENREN_INFO;
				renOAuth.getRenrenInfo(this, (HttpListener)this);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case HttpListener.RENREN_INFO:
			try {
				JSONArray renJson = new JSONArray("" + data);
				JSONObject jsono = renJson.getJSONObject(0);
				renOAuth.uid = "" + jsono.getInt("uid");
				renOAuth.name = jsono.getString("name");
				renOAuth.avatar = jsono.getString("tinyurl");
				renOAuth.gender = jsono.getInt("sex") + 1;
				
				// 判断是否已存在用户
				passwd = UserUtils.generatePass(1, renOAuth.uid); 
				Log.d("DEBUG", "password: " + passwd);
				Log.d("DEBUG", "avatar: " + renOAuth.avatar);
				checkBindUser("" + renOAuth.uid, 2, passwd);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case HttpListener.WEIBO_INFO:
			Log.d("DEBUG", "WEIBO: " + "");
			
			try {
				JSONObject json = new JSONObject(""+data);
				sinaOAuth.uid = json.getString("id");
				sinaOAuth.name = json.getString("name");
				sinaOAuth.avatar = json.getString("avatar_large");
				sinaOAuth.gender = json.getString("gender").equals("f") ? 1 : 2;
				sinaOAuth.desc = json.getString("description");
				
				// 判断是否已存在用户
				passwd = UserUtils.generatePass(1, sinaOAuth.uid); 
				Log.d("DEBUG", "password: " + passwd);
				Log.d("DEBUG", "avatar: " + sinaOAuth.avatar);
				checkBindUser("" + sinaOAuth.uid, 3, passwd);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		}
	}

	private void checkUserLogin(String response) {
		// TODO Auto-generated method stub
		
		JSONObject json;
		try {
			json = new JSONObject(response);
			if(json.getInt("status") == 1){
				User user = User.getInstance(); //new User(this, json.getJSONObject("data"));
				Log.d("DEBUG", "" + response);
				user.init(json.getJSONObject("data"));
				user.save(this);
				user.pass = passEdt.getText().toString();
				if(passwd == null || passwd.equals("")){
					MyApplication.accoutPref.edit().putString("password", passEdt.getText().toString()).commit();
				}
				MyApplication.configPref.edit().putBoolean("loged_in", true).commit();
				
				Intent loginIntent = new Intent(this, MainActivity.class);
				loginIntent.putExtra("new_login", true);
				startActivity(loginIntent);
				overridePendingTransition(R.anim.right_enter,R.anim.exit_fade_out);
				
		        pd.dismiss();
		        Crouton.makeText(this, 
		        		getResources().getString(R.string.hint_login_ok) + ": " + "@" + user.name, 
		        		Style.CONFIRM).show();
				finish();
				return;
			} else {
				Crouton.makeText(this, R.string.hint_login_account_error, Style.ALERT).show();
				pd.dismiss();
				return;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		pd.dismiss();
		Crouton.makeText(this, "Failed: " + data, Style.ALERT).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
//		Log.d("DEBUG", data.getStringExtra("url"));
		Log.d("DEBUG", "intent: " + data);
		
		String mtxt = getResources().getString(R.string.loading_wait);
		
		if(resultCode == 100) {
			// 在这里处理获取返回的code参数
			String code = data.getStringExtra("code");
			// bookInfoTv.append("code: " + code + "\n");
			Log.d("DEBUG", "code: " + code);
			pd = new ProgressDialog(this);
			pd.setMessage(mtxt);
			pd.show();
			this.taskType = HttpListener.DOUBAN_OAUTH_JSON;
			dbOAuth.fetchAccessToken(this, code, (HttpListener)this);
		}
		
		if(resultCode == 101){
			String token = data.getStringExtra("ren_access_token");
			Log.d("DEBUG", "token: " + token);
			renOAuth = new RenRenOAuth();
			renOAuth.accessToken = token;
			pd = new ProgressDialog(this);
			pd.setMessage(mtxt);
			pd.show();
			this.taskType = HttpListener.RENREN_INFO_UID;
			renOAuth.getRenrenUid(this, (HttpListener)this);
		}
		
		if(resultCode == 102){
			sinaOAuth = new SinaOAuth();
			sinaOAuth.accessToken = data.getStringExtra("sina_token");
			sinaOAuth.uid = data.getStringExtra("sina_uid");
			pd = new ProgressDialog(this);
			pd.setMessage(mtxt);
			pd.show();
			this.taskType = HttpListener.WEIBO_INFO;
			sinaOAuth.fetchWiboInfo(this, (HttpListener)this);
		}
	}
	
}
