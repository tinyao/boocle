package com.czzz.demo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.czzz.base.AsyncTaskActivity;
import com.czzz.bookcircle.BookUtils;
import com.czzz.bookcircle.MyApplication;
import com.czzz.data.SearchResultDBHelper;
import com.czzz.douban.DoubanBook;
import com.czzz.utils.HttpListener;
import com.czzz.utils.ShelfCoverDownloader;
import com.czzz.view.LoadingView;
import com.czzz.view.RefreshListView;
import com.czzz.view.RefreshListView.OnRefreshListener;

public class DoubanRecomActivity extends AsyncTaskActivity implements OnRefreshListener{
	
	private RefreshListView hotList;
	private LoadingView loading;
	private MySimpleAdapter adapter;
	private boolean refreshing = false;
	
	ArrayList<DoubanBook> books = new ArrayList<DoubanBook>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.home_douban_recom);
		
		hotList = (RefreshListView) findViewById(R.id.douban_hot_list);
		loading = (LoadingView) findViewById(R.id.loading_view); 
	
		fillBooks();
		
		hotList.setOnRefreshListener(this);
		
		hotList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				DoubanBook book = books.get(arg2);
				Intent info = new Intent(DoubanRecomActivity.this, BookInfoActivity.class);
				info.putExtra("book", (Serializable)book);
				startActivity(info);
			}
			
		});
		
	}
	
	
	private void fillBooks() {
		// TODO Auto-generated method stub
		SearchResultDBHelper helper = SearchResultDBHelper.getInstance(DoubanRecomActivity.this);
		helper.openDataBase();
		Cursor cursor = helper.selectByKey(initCurrentDateStr());
		if (cursor.getCount() > 0) {
			while(cursor.moveToNext()){
				DoubanBook item = new DoubanBook().init(cursor);
				books.add(item);
			}
		}
		
		adapter = new MySimpleAdapter(this, books);
		hotList.setAdapter(adapter);
		
		if(books.size() == 0){
			fetchDoubanRecomm();
		}else{
			loading.setViewGone();
		}
	}


	private void fetchDoubanRecomm(){
		
		initCurrentDateStr();
		BookUtils.fetchDoubanRecomm(key_date, null);
	}
	
	private String key_date = null;
	
	private String initCurrentDateStr(){
		if(key_date == null){
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			format.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
			return key_date = format.format(Calendar.getInstance().getTimeInMillis());
		}else{
			return key_date;
		}
		
	}
	
	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		Log.d("DEBUG", ""+data);
		
		books.clear();
		
		try {
			JSONObject json = new JSONObject(""+data);
			JSONArray jsonArray = json.getJSONArray("data");
			JSONObject item;
			for(int i=0; i<jsonArray.length(); i++){
				item = jsonArray.getJSONObject(i);
				DoubanBook book = initBook(item);
				books.add(book);
				
			}
			
			adapter = new MySimpleAdapter(this, books);
			hotList.setAdapter(adapter);
			
			new Thread(){
				public void run(){
					SearchResultDBHelper helper = SearchResultDBHelper.getInstance(DoubanRecomActivity.this);
					helper.openDataBase();
					for(DoubanBook b : books){
						helper.insert(key_date, b);
					}
					helper.close();
					helper.close();
				}
			}.start();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(refreshing) {
			hotList.completeRefreshing(true);
			refreshing = false;
		}
		loading.setViewGone();
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
		if(refreshing) {
			hotList.completeRefreshingFail(false);
			refreshing = false;
		}
		loading.setViewGone();
	}
    
	private DoubanBook initBook(JSONObject item) throws JSONException{
		DoubanBook book = new DoubanBook();
		book.isbn13 = item.getString("isbn");
		book.title = item.getString("title");
		book.subtitle = item.getString("sub_title");
		book.author = item.getString("author");
		book.image = item.getString("cover");
		book.large_img = item.getString("cover_large");
		book.publisher = item.getString("publisher");
		book.translator = item.getString("pubdate");
		book.rateNum = item.getString("rate_num");
		book.rateAverage = item.getString("rate_score");
		book.summary = item.getString("summary");
		return book;
	}
	
	private class MySimpleAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private ArrayList<DoubanBook> booklist;
		private ShelfCoverDownloader coverLoader;

		public MySimpleAdapter(Context context,
				ArrayList<DoubanBook> list) {
			mInflater = LayoutInflater.from(context);
			this.booklist = list;
			if(MyApplication.displayCover){
				coverLoader = new ShelfCoverDownloader();
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
			
			coverLoader.download(item, holder.cover);
			return convertView;
		}

		public class ViewHolder {
			TextView title;
			TextView publisher;
			TextView author;
			ImageView cover;
		}

	}

	@Override
	public void onRefresh(RefreshListView listView) {
		// TODO Auto-generated method stub
		refreshing = true;
		fetchDoubanRecomm();
	}
}

