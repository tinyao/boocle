package com.czzz.demo.listadapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.czzz.bookcircle.BugItem;
import com.czzz.demo.R;
import com.czzz.utils.TextUtils;

public class BugAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<BugItem> bugs;
	private Context con;

	public BugAdapter(Context context,
			ArrayList<BugItem> bugs) {
		mInflater = LayoutInflater.from(context);
		this.bugs = bugs;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return bugs.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return bugs.get(position);
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
			convertView = mInflater.inflate(R.layout.bug_list_item, 
					parent, false);
			holder = new ViewHolder();
			holder.name = (TextView) convertView
					.findViewById(R.id.bug_item_name);
			holder.time = (TextView) convertView.findViewById(R.id.bug_item_time);
			holder.body = (TextView) convertView.findViewById(R.id.bug_item_body);
			holder.systemName = (TextView) convertView.findViewById(R.id.bug_item_system);
			holder.deviceName = (TextView) convertView.findViewById(R.id.bug_item_device);
			
			convertView.setTag(holder); 
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		BugItem entry = bugs.get(position);

		holder.name.setText(entry.name);
		holder.time.setText(TextUtils.formatSmartTime(entry.create_at));
		holder.body.setText(entry.content);
		holder.deviceName.setText(entry.device);
		holder.systemName.setText(entry.system);
		
		return convertView;
	}

	public class ViewHolder {
		TextView name, time, body, systemName, deviceName;
	}
	
}
