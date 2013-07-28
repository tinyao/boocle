package com.czzz.base;

import android.content.Intent;

import com.actionbarsherlock.app.SherlockFragment;
import com.czzz.demo.R;


public class BaseFragment extends SherlockFragment{


	@Override
	public void startActivity(Intent intent) {
		// TODO Auto-generated method stub
		super.startActivity(intent);
		this.getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}


	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		// TODO Auto-generated method stub
		super.startActivityForResult(intent, requestCode);
		this.getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}
	
	
}
