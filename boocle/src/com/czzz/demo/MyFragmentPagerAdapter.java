package com.czzz.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter{
	
	private static final String[] CONTENT = new String[] { "探索发现", "我的主页", "私信列表"};
			//{ "探索", "个人", "私信"};
	
	private final int PAGE_COUNT = 3;
	ExploreFragment exploreFragment;
	BookShelfFragment bookshelfFragment;
//	SettingFragment settingFragment;
	MessageFragment msgFragment;
	
	FragmentManager fm;
	
	static final class TabInfo
	{
		private final Class<?> clss;
		private final Bundle args;

		TabInfo(Class<?> _class, Bundle _args)
		{
			clss = _class;
			args = _args;
		}
	}
	
	
	/** Constructor of the class */
	public MyFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		this.fm = fm;
	}

	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int arg0) {
		Bundle data = new Bundle();
		switch(arg0){
		
			case 0:
				if(exploreFragment == null){
					exploreFragment = new ExploreFragment();
				}
				data.putInt("current_page", arg0 + 0);
				exploreFragment.setArguments(data);
				return exploreFragment;
			case 1:
				if(bookshelfFragment == null){
					bookshelfFragment = new BookShelfFragment();
				}
				data.putInt("current_page", arg0 + 1);
				bookshelfFragment.setArguments(data);
				return bookshelfFragment;
			case 2:
				if(msgFragment == null) {
					msgFragment = new MessageFragment();
				}
				data.putInt("current_page", arg0 + 2);
				msgFragment.setArguments(data);
				return msgFragment;
		}
		
		return null;
	}

	/** Returns the number of pages */
	@Override
	public int getCount() {		
		return PAGE_COUNT;
	}
	
	@Override
    public CharSequence getPageTitle(int position) {
        return CONTENT[position % CONTENT.length].toUpperCase();
    }
	
}
