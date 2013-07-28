package com.czzz.demo;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.czzz.base.BaseActivity;

public class AboutApp extends BaseActivity {

	TextView usInfoBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.setting_about_us);

		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		usInfoBtn = (TextView) findViewById(R.id.about_about_dev);
		TextView version = (TextView) findViewById(R.id.app_version);
		
		PackageInfo info;
		try {
			info = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0);
			version.setText(this.getResources().getString(R.string.app_name) + " " + info.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		usInfoBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent feed = new Intent(AboutApp.this, AboutDevUsActivity.class);
				startActivity(feed);
				overridePendingTransition(R.anim.shrink_from_bottom,R.anim.exit_fade_out);
			}
			
		});
		
	}

}
