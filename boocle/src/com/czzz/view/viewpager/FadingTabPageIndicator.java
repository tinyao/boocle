package com.czzz.view.viewpager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.OnScrollListener;

import com.czzz.demo.R;

public class FadingTabPageIndicator extends TabPageIndicator{

	public FadingTabPageIndicator(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		fadeOut();
	}

	public FadingTabPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        fadeOut();
	}
	
	@Override
    public void onPageScrollStateChanged(int state) {
		
		switch(state){
    	case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
    		fadeIn();
    		Log.d("DEBUG", "touch");
    		break;
    	case OnScrollListener.SCROLL_STATE_FLING:
    		fadeOut();
    		Log.d("DEBUG", "fling");
    		break;
    	case OnScrollListener.SCROLL_STATE_IDLE:
    		Log.d("DEBUG", "idle");
    		break;
    	}
	}
	
	private void fadeIn(){
    	Animation fadeInAnim = AnimationUtils.loadAnimation(this.getContext(), R.anim.tab_fade_in);
    	this.startAnimation(fadeInAnim);
    	this.setVisibility(View.VISIBLE);
    }
    
	Handler fadeHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.what == 0){
				fadeOutFinal();
			}
		}
		
	};
	
	private static final int MSG_FADE_OUT = 0;
	private static final int FADE_OUT_DELAY = 700;
	
    private void fadeOut(){
    	new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					sleep(FADE_OUT_DELAY);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					fadeHandler.sendEmptyMessage(MSG_FADE_OUT);
				}
			}
    		
    	}.start();
    	
    }
	
    private void fadeOutFinal(){
	    Animation fadeInAnim = AnimationUtils.loadAnimation(this.getContext(), R.anim.tab_fade_out);
		this.startAnimation(fadeInAnim);
		this.setVisibility(View.INVISIBLE);
    }
}
