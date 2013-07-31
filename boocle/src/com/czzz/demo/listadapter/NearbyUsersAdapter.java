package com.czzz.demo.listadapter;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.czzz.base.User;
import com.czzz.demo.R;
import com.czzz.utils.ImagesDownloader;

public class NearbyUsersAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<User> userlist;
	private Context con;

	public NearbyUsersAdapter(Context context,
			ArrayList<User> list) {
		mInflater = LayoutInflater.from(context);
		this.userlist = list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return userlist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return userlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	ImagesDownloader imagesLoader = ImagesDownloader.getInstance();
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.shelf_follow_list_item, 
					parent, false);
			holder = new ViewHolder();
			holder.avatarTxt = (TextView) convertView.findViewById(R.id.explore_user_avatar_txt);
			holder.avatar = (ImageView) convertView.findViewById(R.id.explore_user_avatar);
			holder.name = (TextView) convertView
					.findViewById(R.id.explore_user_item_name);
			holder.gender = (ImageView) convertView.findViewById(R.id.explore_user_item_gender);
			holder.desc = (TextView) convertView.findViewById(R.id.explore_user_item_desc);
			holder.bookNum = (TextView) convertView.findViewById(R.id.explore_user_item_collections);
			convertView.setTag(holder); 
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		User user = userlist.get(position);

		holder.name.setText(user.name);
		holder.desc.setText(user.desc);
		holder.gender.setImageResource(user.gender==1 ? R.drawable.icon_girl : R.drawable.icon_boy);
		holder.bookNum.setText("" + user.book_total);
		if(user.avatar == null || user.avatar.equals("")) {
			holder.avatarTxt.setText(user.name.substring(0, 1).toUpperCase(Locale.CHINA));
			holder.avatarTxt.setVisibility(View.VISIBLE);
		} else {
			holder.avatarTxt.setVisibility(View.GONE);
		}
		imagesLoader.download(user.avatar, holder.avatar);
		
		return convertView;
	}

	public class ViewHolder {
		ImageView avatar;
		TextView name;
		TextView desc;
		TextView bookNum;
		ImageView gender;
		TextView avatarTxt;
	}

}
