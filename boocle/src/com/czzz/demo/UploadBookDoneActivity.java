package com.czzz.demo;

import android.os.Bundle;
import android.widget.TextView;

import com.czzz.base.AsyncTaskActivity;

public class UploadBookDoneActivity extends AsyncTaskActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		this.setContentView(new TextView(this));
		
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		
	}

}
