package com.czzz.demo.listadapter;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.czzz.bookcircle.BookCollection;
import com.czzz.demo.BookCollectionDetailActivity;
import com.czzz.demo.BookInfoActivity;
import com.czzz.demo.R;
import com.czzz.utils.ShelfCoverDownloader;
import com.czzz.view.ShelfCoverView;

public class ShelfAdapter extends BaseAdapter {

	Context context;
	ArrayList<BookCollection> bookList;
	private LayoutInflater inflater;

	public ShelfAdapter(Context con, ArrayList<BookCollection> coverList) {
		this.context = con;
		this.bookList = coverList;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		int len = bookList.size();
		if(len % 3 == 0){
			return len/3;
		}else{
			return len/3 + 1;
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return bookList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	ShelfCoverDownloader coverloader = ShelfCoverDownloader.getInstance();
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder holder;
		
		if (null == convertView) {
			Log.d("DEBUG", "new convertview...");
			convertView = inflater.inflate(R.layout.bookcase_list_item, parent,
					false);
			holder = new ViewHolder();
			holder.cover1 = (ShelfCoverView) convertView.findViewById(R.id.item_book_1);
			holder.cover2 = (ShelfCoverView) convertView.findViewById(R.id.item_book_2);
			holder.cover3 = (ShelfCoverView) convertView.findViewById(R.id.item_book_3);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		int length = bookList.size();
		

		String url;
		
		if (length > 3 * position) {
			url = bookList.get(3 * position + 0).book.image;
			coverloader.download(url, holder.cover1.coverView);
			holder.cover1.setStatus(bookList.get(3 * position + 0).status);
		}
		if (length > 3 * position + 1) {
			url = bookList.get(3 * position + 1).book.image;
			coverloader.download(url, holder.cover2.coverView);
			holder.cover2.setStatus(bookList.get(3 * position + 1).status);
			holder.cover2.setVisibility(View.VISIBLE);
		} else {
			holder.cover2.setVisibility(View.INVISIBLE);
		}
		if (length > 3 * position + 2) {
			url = bookList.get(3 * position + 2).book.image;
			coverloader.download(url, holder.cover3.coverView);
			holder.cover3.setStatus(bookList.get(3 * position + 2).status);
			holder.cover3.setVisibility(View.VISIBLE);
		} else {
			holder.cover3.setVisibility(View.INVISIBLE);
		}

		holder.cover1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent bookIntent = new Intent(context,
						BookInfoActivity.class);
				bookIntent.putExtra("collection", (Serializable)bookList.get(position * 3 + 0));
				bookIntent.putExtra("full", true);	// 图书信息完整
				context.startActivity(bookIntent);
//				((Activity) context).overridePendingTransition(R.anim.right_enter,R.anim.exit_fade_out);
			}

		});

		holder.cover2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent bookIntent = new Intent(context,
						BookInfoActivity.class);
				bookIntent.putExtra("collection", (Serializable)bookList.get(position * 3 + 1));
				bookIntent.putExtra("full", true);
				context.startActivity(bookIntent);
//				((Activity) context).overridePendingTransition(R.anim.right_enter,R.anim.exit_fade_out);
			}

		});

		holder.cover3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent bookIntent = new Intent(context,
						BookInfoActivity.class);
				bookIntent.putExtra("collection", (Serializable)bookList.get(position * 3 + 2));
				bookIntent.putExtra("full", true);
				context.startActivity(bookIntent);
//				((Activity) context).overridePendingTransition(R.anim.right_enter,R.anim.exit_fade_out);
			}

		});

		
		return convertView;
	}
	
	public class ViewHolder {
		ShelfCoverView cover1, cover2, cover3;
	}
	
//	private View.OnClickListener listener = new View.OnClickListener() {
//		
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			BookCollection book;
//			switch(v.getId()){
//			case R.id.item_book_1:
//				book = bookList.get(position * 3 + 1);
//				break;
//			case R.id.item_book_2:
//				break;
//			case R.id.item_book_3:
//				break;
//			}
//		}
//	};

}
