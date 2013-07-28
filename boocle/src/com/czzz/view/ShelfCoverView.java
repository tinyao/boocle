package com.czzz.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.czzz.demo.R;

public class ShelfCoverView extends LinearLayout {

	public ImageView coverView;
	public ImageView statusView;
	public ProgressBar loadBar;

	public ShelfCoverView(Context context) {
		this(context, null);
	}

	public ShelfCoverView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 导入布局
		LayoutInflater.from(context).inflate(R.layout.bookshelf_cover_view,
				this, true);
		coverView = (ImageView) findViewById(R.id.item_book_cover);
		statusView = (ImageView) findViewById(R.id.item_book_status);
		loadBar = (ProgressBar) findViewById(R.id.item_book_loading);
	}


	/**
	 * 设置状态
	 */
	public void setStatus(int status) {
		if(status == 1){
			statusView.setVisibility(View.VISIBLE);
		}else{
			statusView.setVisibility(View.INVISIBLE);
		}
//		statusView.setImageResource(resId);
	}


	/**
	 * 设置状态
	 */
	public void setStatus(String status) {

		if (status.equals("wish"))
			statusView.setVisibility(View.VISIBLE);
		else
			statusView.setVisibility(View.INVISIBLE);

	}

}