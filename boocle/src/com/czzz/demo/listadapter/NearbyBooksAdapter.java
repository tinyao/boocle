package com.czzz.demo.listadapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.czzz.bookcircle.BookCollection;
import com.czzz.demo.R;
import com.czzz.utils.ShelfCoverDownloader;
import com.czzz.utils.TextUtils;

public class NearbyBooksAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<BookCollection> booklist;
	private Context con;

	public NearbyBooksAdapter(Context context,
			ArrayList<BookCollection> list) {
		mInflater = LayoutInflater.from(context);
		this.booklist = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return booklist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return booklist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	ShelfCoverDownloader imagesLoader = ShelfCoverDownloader.getInstance();
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if (convertView == null) { 
			convertView = mInflater.inflate(R.layout.book_nearby_list_item, 
					parent, false);
			holder = new ViewHolder();
			holder.cover = (ImageView) convertView.findViewById(R.id.nearby_book_cover);
			holder.title = (TextView) convertView
					.findViewById(R.id.nearby_book_title);
			holder.comment = (TextView) convertView.findViewById(R.id.nearby_book_comments);
			holder.createTime = (TextView) convertView.findViewById(R.id.nearby_book_create_time);
			holder.owner = (CheckedTextView) convertView.findViewById(R.id.nearby_book_user);
			holder.status = (ImageView) convertView.findViewById(R.id.nearby_book_status);
			holder.ratingBar = (RatingBar) convertView.findViewById(R.id.nearby_book_score);
			convertView.setTag(holder); 
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		BookCollection entry = booklist.get(position);
		
		holder.title.setText(entry.book.title);
		holder.comment.setText(TextUtils.removeEndEmptyLines(entry.note));
		holder.createTime.setText(TextUtils.formatSmartTime(entry.create_at));
		holder.owner.setText("@" + entry.owner);
		holder.owner.setChecked(entry.owner_gender == 1);
		holder.ratingBar.setRating(entry.score);
		if(!entry.book.image.equals(""))
			imagesLoader.download(entry.book.image, holder.cover);
		
		if(entry.status == 1){
			holder.status.setVisibility(View.VISIBLE);
		} else{
			holder.status.setVisibility(View.GONE);
		}
		
		return convertView;
	}

	public class ViewHolder {
		ImageView cover;
		TextView title;
		TextView comment;
		TextView score;
		ImageView status;
		CheckedTextView owner;
		TextView createTime;
		RatingBar ratingBar;
	}
	
}
