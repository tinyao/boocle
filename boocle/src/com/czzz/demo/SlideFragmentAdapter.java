package com.czzz.demo;

import org.goodev.helpviewpager.HelpFragmentPagerAdapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.czzz.demo.R;


public class SlideFragmentAdapter extends HelpFragmentPagerAdapter {
	protected static final int[] CONTENT 
		= new int[] { R.drawable.guide1, R.drawable.guide2, 
		R.drawable.guide3, R.drawable.guide4 };

	private int mCount = 4;

	public SlideFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getHelpItem(int position) {
		return SlideFragment.newInstance(CONTENT[position % 4]);
	}

	@Override
	public int getHelpCount() {
		return mCount;
	}

	public void setCount(int count) {
		if (count > 0 && count <= 10) {
			mCount = count;
			notifyDataSetChanged();
		}
	}
}