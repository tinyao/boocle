package com.czzz.view;

import com.czzz.demo.R;
import com.czzz.utils.ImagesDownloader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OwnerView extends LinearLayout{
	
	public ImageView avatar;
	public TextView nameTv;
	public TextView noteTv;
	public static ImagesDownloader imageLoader;
	
	public OwnerView(Context context) {
		this(context, null);
	}

	public OwnerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 导入布局
		LayoutInflater.from(context).inflate(R.layout.owner_view,
				this, true);
		avatar = (ImageView) findViewById(R.id.item_book_cover);
		nameTv = (TextView) findViewById(R.id.item_book_status);
		noteTv = (TextView) findViewById(R.id.item_book_loading);
	}

	public void setOwner(String name, String avatarUrl, String note){
		if(imageLoader == null){
			imageLoader = new ImagesDownloader();
		}
		imageLoader.download(avatarUrl, avatar);
		nameTv.setText(name);
		noteTv.setText(note);
	}
	
}
