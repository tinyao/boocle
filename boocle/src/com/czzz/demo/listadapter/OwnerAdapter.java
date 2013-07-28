package com.czzz.demo.listadapter;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.czzz.bookcircle.BookCollection;
import com.czzz.demo.R;
import com.czzz.utils.ImagesDownloader;

public class OwnerAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private ArrayList<BookCollection> ownerList;
	
	public OwnerAdapter(Context context,
			ArrayList<BookCollection> list) {
		mInflater = LayoutInflater.from(context);
		if(list.size() > 6){
			this.ownerList = new ArrayList<BookCollection>(list.subList(0, 6));
		}else{
			this.ownerList = list;
		}
	}
	
	public OwnerAdapter(Context context,
			ArrayList<BookCollection> list, boolean hasLimit) {
		mInflater = LayoutInflater.from(context);
		this.ownerList = list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return ownerList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return ownerList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	ImagesDownloader imagesLoader = new ImagesDownloader();
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.owner_view, 
					parent, false);
			
			holder = new ViewHolder();
			holder.avatar = (ImageView) convertView.findViewById(R.id.book_owner_avatar);
			holder.nameTv = (TextView) convertView.findViewById(R.id.book_owner_name);
			holder.noteTv = (TextView) convertView.findViewById(R.id.book_owner_note);
			holder.avatarTxt = (TextView) convertView.findViewById(R.id.book_owner_avatar_txt);
			convertView.setTag(holder); 
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		BookCollection coll = ownerList.get(position);

		holder.nameTv.setText(coll.owner);
		holder.noteTv.setText(coll.note);
		
		if(coll.owner_avatar == null || coll.owner_avatar.equals("")) {
			holder.avatarTxt.setText(coll.owner.substring(0, 1).toUpperCase(Locale.CHINA));
			holder.avatarTxt.setVisibility(View.VISIBLE);
		} else {
			holder.avatarTxt.setVisibility(View.GONE);
		}
		imagesLoader.download(coll.owner_avatar, holder.avatar);
		
		return convertView;
		
	}

	class ViewHolder{
		ImageView avatar;
		TextView avatarTxt;
		TextView nameTv;
		TextView noteTv;
	}

}
