package com.czzz.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class NotificationFragmentPagerAdapter extends FragmentPagerAdapter{
	
	final int PAGE_COUNT = 2;
	
	/** Constructor of the class */
	public NotificationFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		
	}

	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int arg0) {
		Bundle data = new Bundle();
		switch(arg0){
		
			/** Apple tab is selected */
			case 0:
				NotifyMeFragment notimeFragment = new NotifyMeFragment();
				data.putInt("current_page", arg0+1);
				notimeFragment.setArguments(data);
				return notimeFragment;
				
			case 1:
				NotifyOutFragment notioutFragment = new NotifyOutFragment();
				data.putInt("current_page", arg0+1);
				notioutFragment.setArguments(data);
				return notioutFragment;
		}
		
		return null;
	}

	/** Returns the number of pages */
	@Override
	public int getCount() {		
		return PAGE_COUNT;
	}
	
}
