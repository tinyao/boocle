package com.czzz.demo;

import java.util.ArrayList;

import com.czzz.demo.R;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class FeedbackActivity extends AsyncTaskActivity{

	
	EditText feedEdt;
	Button feedlistBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.feedback);
		
		feedEdt = (EditText) findViewById(R.id.feedback_edit);
		feedlistBtn = (Button) findViewById(R.id.feedback_list_btn);
		
		Intent i = getIntent();
		if(i.hasExtra("feed_hint")){
			feedEdt.setHint(i.getStringExtra("feed_hint"));
		}
		
		int nowUID = User.getInstance().uid;
		
		if(nowUID == 1 || nowUID == 23 || nowUID == 12 || nowUID == 2 || nowUID == 13){
			feedlistBtn.setVisibility(View.VISIBLE);
		}else{
			feedlistBtn.setVisibility(View.GONE);
		}
		
		feedlistBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent ii = new Intent(FeedbackActivity.this, FeedbackListActivity.class);
				startActivity(ii);
			}
			
		});

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getSupportMenuInflater().inflate(R.menu.menu_user_feedback, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.menu_user_msg:
			
			if(feedEdt.getText().toString().equals("")){
				break;
			}
			sendFeedback(feedEdt.getText().toString());
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void sendFeedback( String content){
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("uid", "" + User.getInstance().uid));
		params.add(new BasicNameValuePair("name", User.getInstance().name));
		params.add(new BasicNameValuePair("content", content));
		params.add(new BasicNameValuePair("device", android.os.Build.MODEL));
		params.add(new BasicNameValuePair("system", android.os.Build.VERSION.RELEASE));
		
		new HttpPostTask(this, (HttpListener)this).execute(Pref.FEED_BACK_URL, params);
		this.setResult(100);
		finish();
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		Crouton.makeText(this, R.string.hint_feedback_ok, Style.CONFIRM).show();
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Crouton.makeText(this, data, Style.ALERT).show();
	}
	
}
