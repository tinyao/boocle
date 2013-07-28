package com.czzz.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public final class SlideFragment extends Fragment {
	private static final String KEY_CONTENT = "TestFragment:Content";

	public static SlideFragment newInstance(int resId) {
		SlideFragment fragment = new SlideFragment();
		fragment.imageResid = resId;
		return fragment;
	}

//	private String mContent = "???";
	private int imageResid = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
			imageResid = savedInstanceState.getInt(KEY_CONTENT);
		}


		ViewGroup viewGroup = (ViewGroup)getActivity().getLayoutInflater().inflate(
                R.layout.slide_item, null);

        ImageView slideImg = (ImageView)viewGroup.findViewById(R.id.slide_img);
        
        slideImg.setImageResource(imageResid);
        
        Log.d("DEBUG", "slide: " + imageResid);
        
//        container.addView(viewGroup);

		LinearLayout layout = new LinearLayout(getActivity());
		layout.setLayoutParams(
				new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		layout.setGravity(Gravity.CENTER);
//		layout.addView(text);
//		layout.setBackgroundResource(imageResid);
//		layout.removeAllViews();
		layout.addView(viewGroup);
//		layout.setBackgroundColor(Color.BLACK);

		return layout;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_CONTENT, imageResid);
	}
}