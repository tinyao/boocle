package com.czzz.base;


import com.czzz.demo.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

public class BaseActivity extends SherlockActivity{
	
	protected ActionBar mActionBar;
	
	private GestureDetector gestureDetector;
	private View.OnTouchListener gestureListener;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mActionBar = getSupportActionBar();

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayUseLogoEnabled(false);
        
	}
	
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
    	super.onResume();
	}
    
    public void setGestureBackOn(){
    	View contentView = this.getWindow().getDecorView().findViewById(android.R.id.content);
    	gestureDetector = new GestureDetector(this, new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return gestureDetector.onTouchEvent(event);
			}
		};
		contentView.setOnTouchListener(gestureListener);
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


	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
//		overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
		this.overridePendingTransition(R.anim.fade_in_enter,
				R.anim.activity_scroll_to_right);
	}


	@Override
	public void startActivity(Intent intent) {
		// TODO Auto-generated method stub
		super.startActivity(intent);
		overridePendingTransition(R.anim.activity_scroll_from_right, R.anim.fade_out_exit);
	}


	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		// TODO Auto-generated method stub
		super.startActivityForResult(intent, requestCode);
		overridePendingTransition(R.anim.activity_scroll_from_right, R.anim.fade_out_exit);
	}
	
	class MyGestureDetector extends SimpleOnGestureListener {

		final ViewConfiguration vc = ViewConfiguration.get(getApplicationContext());
		final int SWIPE_MIN_X = vc.getScaledPagingTouchSlop() * 10;
		final int SWIPE_THRESHOLD_VELOCITY = vc.getScaledMinimumFlingVelocity() * 2;
		final int SWIPE_MAX_OFFPATH = vc.getScaledTouchSlop();
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			
			try{
				if (e2.getX() - e1.getX() > SWIPE_MIN_X && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
					finish(); //left
				}
			} catch (Exception e) {
				
			}
			return false;
		}
	}
}


