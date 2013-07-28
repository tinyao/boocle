package com.czzz.demo;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.bookcircle.MyApplication;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class PasswdChangeActivity extends AsyncTaskActivity{

	EditText originPassEdt, newpassEdt, newpassConfirm;
	boolean originBool = false, newpassBool = false, passConfirmBool = false;
	
	Button okBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_change_password);
		
		originPassEdt = (EditText) findViewById(R.id.change_passwd_origin);
		newpassConfirm = (EditText) findViewById(R.id.change_passwd_new_again);
		newpassEdt = (EditText) findViewById(R.id.change_passwd_new);
		
		okBtn = (Button) findViewById(R.id.change_passwd_btn);
		
		okBtn.setOnClickListener(new OnClickListener(){
 
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				changePassword();
			}
			
		});
	}
	
	ProgressDialog pd;
	
	private void changePassword(){
		if(!newpassEdt.getText().toString().equals(newpassConfirm.getText().toString())) {
			Crouton.makeText(this, R.string.hint_passwd_not_same, Style.ALERT).show();
			return;
		}
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(newpassConfirm.getWindowToken(), 0);
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("uid", ""+User.getInstance().uid));
		params.add(new BasicNameValuePair("passwd", originPassEdt.getText().toString()));
		params.add(new BasicNameValuePair("newpass", newpassConfirm.getText().toString()));
		
		this.taskType = HttpListener.CHANGE_PASSWD;
		new HttpPostTask(this, (HttpListener)this).execute(Pref.USER_CHANGE_PASSWD, params);
		pd = new ProgressDialog(this);
		pd.setMessage(getResources().getString(R.string.loading_wait));
		pd.show();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		originPassEdt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length() > 0) {
					originBool = true;
				} else {
					originBool = false;
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
		
		newpassEdt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length() > 0) {
					newpassBool = true;
				} else {
					newpassBool = false;
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
		
		newpassConfirm.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if(s.length() > 0) {
					passConfirmBool = true;
				} else {
					passConfirmBool = false;
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
		if(originBool && newpassBool && passConfirmBool ){
			okBtn.setEnabled(true);
		} else {
			okBtn.setEnabled(false);
		}
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		try {
			JSONObject resp = new JSONObject(""+data);
			if(resp.getInt("status") == 1){
				Crouton.makeText(this, R.string.edit_success, Style.CONFIRM).show();
			}else{
				Crouton.makeText(this, resp.getString("data"), Style.ALERT).show();
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MyApplication.accoutPref.edit().putString("password", newpassConfirm.getText().toString()).commit();
		
		pd.dismiss();
		finish();
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Crouton.makeText(this, "Failed: " + data, Style.ALERT).show();
		pd.dismiss();
	}

}
