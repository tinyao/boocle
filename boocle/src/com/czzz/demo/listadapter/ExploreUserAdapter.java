package com.czzz.demo.listadapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.czzz.demo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ExploreUserAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<Map<String,Object>> userlist;
	private Context con;

	public ExploreUserAdapter(Context context, ArrayList<Map<String,Object>> list) {
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.explore_user_list_item, 
					parent, false);
			holder = new ViewHolder();
			holder.avatar = (ImageView) convertView
					.findViewById(R.id.explore_user_avatar);
			holder.name = (TextView) convertView
					.findViewById(R.id.explore_user_item_name);
			holder.desc = (TextView) convertView.findViewById(R.id.explore_user_item_desc);
			holder.total = (TextView) convertView.findViewById(R.id.explore_user_item_collections);
			
			convertView.setTag(holder); 
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Map<String, Object> map = userlist.get(position);
		

		holder.name.setText((String) map.get("name"));
		holder.avatar.setImageResource((Integer)map.get("avatar"));
		holder.desc.setText((String) map.get("desc"));
		holder.total.setText((String) map.get("total"));
		
		
//		String publisher = booklist.get(position).publisher;
//		if(publisher == null || publisher.equals(""))
//			publisher = "无出版社信息";
//		holder.publisher.setText(publisher);
//		holder.isbn.setText(booklist.get(position).isbn13);
		return convertView;
	}

	public class ViewHolder {
		ImageView avatar;
		TextView name;
		TextView desc;
		TextView total;
	}

}