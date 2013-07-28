package com.czzz.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.czzz.demo.R;

public class LoadingView extends LinearLayout{
	
	private ProgressBar progress;
	private TextView text;
	
	public LoadingView(Context context) {
		this(context, null);
	}

	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 导入布局
		LayoutInflater.from(context).inflate(R.layout.loading_mask_layout,
				this, true);
		progress = (ProgressBar) findViewById(R.id.loading_progress);
		text = (TextView) findViewById(R.id.loading_text);
	}
	
	public void setMessage(String msg){
		text.setText(msg);
	}
	
	public void setResultMsg(String msg){
		progress.setVisibility(View.INVISIBLE);
		text.setVisibility(View.VISIBLE);
		text.setText(msg);
	}
	
	public void setViewGone(){
		this.setVisibility(GONE);
	}

}
