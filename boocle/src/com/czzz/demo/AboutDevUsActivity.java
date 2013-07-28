package com.czzz.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.czzz.base.BaseActivity;
import com.czzz.base.User;

public class AboutDevUsActivity extends BaseActivity{


	View tinyao, ibird, zxming;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dev_us);
		
		tinyao = findViewById(R.id.dev_tinyao);
		ibird = findViewById(R.id.dev_ibird);
		zxming = findViewById(R.id.dev_zxming);
		
		tinyao.setOnClickListener(listener);
		ibird.setOnClickListener(listener);
		zxming.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			User muser = new User();
			switch(v.getId()){
			case R.id.dev_tinyao:
				muser.uid = 12;
				break;
			case R.id.dev_ibird:
				muser.uid = 79;
				break;
			case R.id.dev_zxming:
				muser.uid = 2;
				break;
			}
			
			Intent userPageIntent = new Intent(AboutDevUsActivity.this, UserPageActivity.class);
			userPageIntent.putExtra("user", muser);
			userPageIntent.putExtra("from_book_detail", true);
			startActivity(userPageIntent);
		}
		
	};
	

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(R.anim.enter_fade_in, R.anim.shrink_exit_top);
	}
	
	
}
