package com.czzz.demo.listadapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.czzz.base.User;
import com.czzz.bookcircle.MsgThread;
import com.czzz.demo.R;
import com.czzz.demo.UserPageActivity;
import com.czzz.utils.ImagesDownloader;
import com.czzz.utils.TextUtils;

public class MsgThreadAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<MsgThread> msglist;
	private Context context;

	public MsgThreadAdapter(Context context,
			ArrayList<MsgThread> list) {
		this.context = context;
		mInflater = LayoutInflater.from(context);
		this.msglist = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return msglist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return msglist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	ImagesDownloader imagesLoader = ImagesDownloader.getInstance();
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.thread_item, 
					parent, false);
			holder = new ViewHolder();
			holder.avatar = (ImageView) convertView.findViewById(R.id.thread_item_avatar);
			holder.name = (TextView) convertView
					.findViewById(R.id.thread_item_name);
			holder.body = (TextView) convertView.findViewById(R.id.thread_item_body);
			holder.unread = (TextView) convertView.findViewById(R.id.thread_item_unread_count);
			holder.time = (TextView) convertView.findViewById(R.id.thread_item_time);
			convertView.setTag(holder); 
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		
		final MsgThread thread = msglist.get(position);
		
		holder.name.setText(thread.thread_name);
		holder.body.setText(thread.msg_body.replace("\n", " "));
		holder.time.setText(TextUtils.formatSmartTime(thread.msg_time));
		imagesLoader.download(thread.thread_avatar, holder.avatar);
		
		holder.avatar.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				User taUser = new User();
				taUser.avatar = thread.thread_avatar;
				taUser.uid = thread.thread_uid;
				taUser.name = thread.thread_name;
				Intent userPageIntent = new Intent(context, UserPageActivity.class);
				userPageIntent.putExtra("user", taUser);
				userPageIntent.putExtra("from_book_detail", true);
				context.startActivity(userPageIntent);
			}
			
		});
		
		if(thread.unread_count == 0){
			holder.unread.setVisibility(View.GONE);
		}else{
			holder.unread.setVisibility(View.VISIBLE);
			holder.unread.setText(""+thread.unread_count);
		}
		
		return convertView;
	}

	public class ViewHolder {
		ImageView avatar;
		TextView name;
		TextView body;
		TextView unread;
		TextView time;
	}

}
