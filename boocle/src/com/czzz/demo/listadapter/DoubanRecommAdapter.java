package com.czzz.demo.listadapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.czzz.bookcircle.MyApplication;
import com.czzz.demo.R;
import com.czzz.douban.DoubanBook;
import com.czzz.utils.ShelfCoverDownloader;

public class DoubanRecommAdapter extends BaseAdapter{

		private LayoutInflater mInflater;
		private ArrayList<DoubanBook> booklist;
		private ShelfCoverDownloader coverLoader;

		public DoubanRecommAdapter(Context context,
				ArrayList<DoubanBook> list) {
			mInflater = LayoutInflater.from(context);
			this.booklist = list;
			if(MyApplication.displayCover){
				coverLoader = ShelfCoverDownloader.getInstance();
			}
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.douban_recomm_item, 
						parent, false);
				holder = new ViewHolder();
				holder.title = (TextView) convertView
						.findViewById(R.id.douban_recomm_title);
				holder.publisher = (TextView) convertView
						.findViewById(R.id.douban_recomm_publisher);
				holder.author = (TextView) convertView.findViewById(R.id.douban_recomm_author);
				holder.cover = (ImageView) convertView.findViewById(R.id.douban_recomm_cover);
				holder.intro = (TextView) convertView.findViewById(R.id.douban_recomm_intro);
				holder.rate = (TextView) convertView.findViewById(R.id.douban_recomm_rate_score);
				if(!MyApplication.displayCover) holder.cover.setVisibility(View.GONE);
				convertView.setTag(holder); 
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			DoubanBook item = booklist.get(position);
			
			holder.title.setText(item.title);
			
			String publisher = item.publisher;
			if(publisher == null || publisher.equals(""))
				publisher = "无出版社信息";
			holder.publisher.setText(publisher);
			holder.author.setText(item.author);
			holder.intro.setText(item.summary);
			holder.rate.setText(item.rateAverage + "’");
			
			coverLoader.download(item, holder.cover);
			return convertView;
		}

		public class ViewHolder {
			TextView title;
			TextView publisher;
			TextView author;
			TextView intro;
			ImageView cover;
			TextView rate;
		}

	}