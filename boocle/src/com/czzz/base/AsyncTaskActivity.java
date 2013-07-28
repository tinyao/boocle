package com.czzz.base;


import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.czzz.demo.R;
import com.czzz.utils.HttpListener;

public abstract class AsyncTaskActivity extends BaseActivity implements HttpListener{
	
	public int taskType;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
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
		}
		
		return super.onOptionsItemSelected(item);
	}

	
}


