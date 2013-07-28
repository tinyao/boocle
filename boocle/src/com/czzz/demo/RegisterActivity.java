package com.czzz.demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.social.BaseOAuth;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class RegisterActivity extends AsyncTaskActivity{
	
	EditText accountEdt, emailEdt, passEdt, passConfirm;
	
	TextView result;
	
//	String bind_id = "";
//	String avatar = "";
//	int bind_from = 0;
	BaseOAuth baseOAuth;
	
	Button registerButton;
	
	private ProgressDialog pd;
	
	boolean accountBool=false, emailBool=false, passBool=false, confirmBool=false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_register);
		
		this.mActionBar.setTitle(R.string.register_account);
		
		accountEdt = (EditText) findViewById(R.id.register_user_account);
		emailEdt = (EditText) findViewById(R.id.register_user_email);
		passEdt = (EditText) findViewById(R.id.register_user_password);
		passConfirm = (EditText) findViewById(R.id.register_user_password_confirm);
		registerButton = (Button) findViewById(R.id.register_btn);
		result = (TextView)findViewById(R.id.regoster_result);
		
		Intent ii = getIntent();
		
		if(ii.hasExtra("third_login")){
			
			this.mActionBar.setTitle("补充信息");
			
			passEdt.setVisibility(View.GONE);
			passConfirm.setVisibility(View.GONE);
			
//			bind_id = ii.getStringExtra("bind_id");
//			bind_from = ii.getIntExtra("bind_from", 0);
//			avatar = ii.getStringExtra("avatar");
			baseOAuth = (BaseOAuth) ii.getSerializableExtra("source_info");
			
			accountEdt.append(baseOAuth.name);
			passEdt.append(baseOAuth.passwd);
			passConfirm.append(baseOAuth.passwd);
			passBool=true;
			confirmBool=true;
			accountBool=true;
//			emailEdt.setBackgroundResource(R.drawable.login_input_rounded_bottom);
			registerButton.setText(R.string.next_step);
		}
		
		registerButton.setOnClickListener(new OnClickListener(){
 
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				registerAccount();
			}
			
		});
		
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		accountEdt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length() > 0) {
					accountBool = true;
				} else {
					accountBool = false;
				}
				checkFormComplete();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

		});
		
		emailEdt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length() > 0) {
					emailBool = true;
				} else {
					emailBool = false;
				}
				checkFormComplete();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

		});
		
		passEdt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length() > 0) {
					passBool = true;
				} else {
					passBool = false;
				}
				checkFormComplete();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

		});
		
		passConfirm.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length() > 0) {
					confirmBool = true;
				} else {
					confirmBool = false;
				}
				checkFormComplete();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}

		});
	}


	private void checkFormComplete(){
		if(accountBool && emailBool && passBool && confirmBool){
			registerButton.setEnabled(true);
		} else {
			registerButton.setEnabled(false);
		}
	}


	TextWatcher watcher = new TextWatcher(){

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}
		
	};
	

	/**
	 * 
	 */
	protected void registerAccount() {
		// TODO Auto-generated method stub
		
		if(!passEdt.getText().toString().equals(passConfirm.getText().toString())) {
			Crouton.makeText(this, R.string.hint_passwd_not_same, Style.ALERT).show();
			return;
		}
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(passConfirm.getWindowToken(), 0);
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("name", accountEdt.getText().toString()));
		params.add(new BasicNameValuePair("email", emailEdt.getText().toString()));
		params.add(new BasicNameValuePair("passwd", passConfirm.getText().toString()));
		
		if(baseOAuth != null){
			params.add(new BasicNameValuePair("bind_from", "" + baseOAuth.source ));
			params.add(new BasicNameValuePair("bind_id", baseOAuth.uid));
			params.add(new BasicNameValuePair("avatar", baseOAuth.avatar));
			params.add(new BasicNameValuePair("desc", baseOAuth.desc));
			params.add(new BasicNameValuePair("gender", ""+baseOAuth.gender));
		}
		
		this.taskType = HttpListener.ACCOUNT_REGISTER;
		new HttpPostTask(this, (HttpListener)this).execute(Pref.REGISTER_URL, params);
		pd = new ProgressDialog(this);
		pd.setMessage(getResources().getString(R.string.loading_wait));
		pd.show();
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		switch(taskType){
		case HttpListener.ACCOUNT_REGISTER:
			try {
				JSONObject json = new JSONObject(""+data);
				if(json.getInt("status") == 1) {
//					if(pd != null) pd.dismiss();
					Crouton.makeText(this, R.string.register_ok, Style.CONFIRM).show();
					passwd = passEdt.getText().toString();
					login(emailEdt.getText().toString(), passwd);
					SharedPreferences sp = this.getSharedPreferences("account", 0);
					sp.edit().putString("password", passEdt.getText().toString()).commit();
				}else{
					pd.dismiss();
					Crouton.makeText(this, json.getString("data"), Style.ALERT).show();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case HttpListener.ACCOUNT_LOGIN:
			checkUserLogin("" + data);
			break;
		}
		
	}
	
	private String passwd = "";
	
	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		result.setText("failed: " + data);
		pd.dismiss();
	}
	
	/**
	 * 注册成功后登录
	 */
	protected void login(String email, String passwd) {
		// TODO Auto-generated method stub
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("email", email));
		params.add(new BasicNameValuePair("passwd", passwd));
		
		this.taskType = HttpListener.ACCOUNT_LOGIN;
		new HttpPostTask(this, (HttpListener)this).execute(Pref.LOGIN_URL, params);
		
		pd.setMessage(getResources().getString(R.string.hint_logining));
	}
	
	private void checkUserLogin(String response) {
		// TODO Auto-generated method stub
		JSONObject json;
		try {
			json = new JSONObject(response);
			if(json.getInt("status") == 1){
				User user = User.getInstance(); //new User(this, json.getJSONObject("data"));
				Log.d("DEBUG", "" + response);
				user.init(json.getJSONObject("data")).save(this);
				user.pass = passwd;
				
				pd.dismiss();
				Intent i = new Intent(RegisterActivity.this, SchoolChooserActivity.class);
				startActivity(i);
				finish();
				return;
			} else {
				Crouton.makeText(this, R.string.hint_login_account_error, Style.ALERT).show();
				return;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


//	@Override
//	public void finish() {
//		// TODO Auto-generated method stub
//		this.onDestroy();
//	}
	
}
