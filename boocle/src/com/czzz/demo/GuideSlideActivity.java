package com.czzz.demo;

import org.goodev.helpviewpager.CirclePageIndicator;
import org.goodev.helpviewpager.OnLastPageListener;
import org.goodev.helpviewpager.PageIndicator;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;

public class GuideSlideActivity extends FragmentActivity{
	
	SlideFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_guide_slide);
        
        
        mAdapter = new SlideFragmentAdapter(getSupportFragmentManager());
        
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(4);
        mPager.setAdapter(mAdapter);
        
        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        
        mIndicator.setOnLastPageListener(new OnLastPageListener() {
            @Override
            public void onLastPage() {
                finish();
            }
        });
    }

    
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyUp(keyCode, event);
	}
    
}
