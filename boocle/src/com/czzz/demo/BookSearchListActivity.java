package com.czzz.demo;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.czzz.base.BaseListActivity;
import com.czzz.bookcircle.MyApplication;
import com.czzz.data.SearchResultDBHelper;
import com.czzz.douban.DoubanBook;
import com.czzz.douban.DoubanBookUtils;
import com.czzz.utils.HttpListener;
import com.czzz.utils.MySuggestionProvider;
import com.czzz.utils.ShelfCoverDownloader;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class BookSearchListActivity extends BaseListActivity {
	
	private ListView listview;
	private SearchView searchView;
	
	ProgressBar refreshPd;
	View loadView;
	
	int currentFirstVisibleItem;
	int currentVisibleItemCount;
	int currentScrollState;
	
	ArrayList<DoubanBook> doubanList;
	
	String key = "";
	int currentMax = 0;

	private MySimpleAdapter adapter;
	
	Animation anim;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(SampleList.THEME); // Used for theme switching in samples
        super.onCreate(savedInstanceState);
        mActionBar = getSupportActionBar();

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayUseLogoEnabled(false);
        
        setContentView(R.layout.book_search_result);
        
        anim = AnimationUtils.loadAnimation(this, R.anim.refresh_animation);    
        
        loadView = findViewById(R.id.search_list_pd_view);
        
        listview = this.getListView();

		listview.setTextFilterEnabled(true);
		
		listview.addFooterView(LayoutInflater.from(BookSearchListActivity.this)
				.inflate(R.layout.pulldown_footer, null));
		
		View footer = findViewById(R.id.footer_view);
		refreshPd = (ProgressBar) findViewById(R.id.pulldown_footer_loading);
		refreshPd.setIndeterminate(false); 
        
        Intent in = getIntent();
        Bundle bnd = in.getExtras();
        if(bnd != null){
        	key = in.getStringExtra("keyword");
        	if(key != null) searchBooks(key);
        	
        	fromAdding = in.hasExtra("add_book_search");
        	// from shelf to search books to add
        }
        
		footer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "footer clicked ~");
				refreshPd.setVisibility(View.VISIBLE);
				loadMoreBooks();
			}
			
		});

	}
	
	private boolean fromAdding = false;
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getSupportMenuInflater().inflate(R.menu.menu_search_list, menu);

		MenuItem searchM = menu.findItem(R.id.menu_search);
		
		searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		searchView.setQueryHint("Search for Books");
		traverseView(searchView, 0);

		searchView.setOnQueryTextListener(new OnQueryTextListener(){

			@Override
			public boolean onQueryTextSubmit(String query) {
				// TODO Auto-generated method stub
				if(DoubanBookUtils.isISBN(query)){
					Intent i = new Intent(BookSearchListActivity.this, BookInfoActivity.class);
					i.putExtra("isbn", query);
					startActivity(i);
					overridePendingTransition(R.anim.shrink_from_bottom,R.anim.exit_fade_out);
				}else{
					SearchRecentSuggestions suggestions = new SearchRecentSuggestions(BookSearchListActivity.this,
					MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
					suggestions.saveRecentQuery(query, null);

					loadView.setVisibility(View.VISIBLE);
					searchBooks(query);
					key = query;
				}
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
				imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
				searchView.clearFocus();
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
		
		searchM.expandActionView();
		searchView.setQuery(key==null ? "" : key, false);
		
		if(fromAdding) {
			searchView.requestFocus();
			loadView.setVisibility(View.GONE);
		}else{
			searchView.clearFocus();
		}
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		case R.id.abs__home:
			finish();
			break;
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_scan:
			Intent ii = new Intent("com.czzz.action.qrscan");
			startActivityForResult(ii,0);
			break;
		case R.id.menu_refresh:
			loadView.setVisibility(View.VISIBLE);
			refreshBooks(key);
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		
		if(doubanList == null){
			return;
		}
		
		Log.d("DEBUG", "click poosition: " + position + "   list count: " + doubanList.size());
		
		DoubanBook bookSelect = doubanList.get(position);
		
		Intent bookIntent = new Intent(BookSearchListActivity.this, BookInfoActivity.class);
		Bundle bnd = new Bundle();
		bnd.putSerializable("book", bookSelect);
		bookIntent.putExtras(bnd);
//		bookIntent.putExtra("isbn", isbnSelected);
		startActivity(bookIntent);
	}

	/**
	 * Serach book by keyword
	 * @param keyword
	 */
	protected void searchBooks(String keyword){
		if (!searchBookfromCache(keyword)) {
			DoubanBookUtils.searchBooks(this, keyword, 0, 20, 
					new SearchHttpHandler(this, HttpListener.SEARCH_BOOKS));
		}
	}
	
	/**
	 * Serach book by keyword
	 * @param keyword
	 */
	protected void refreshBooks(String keyword){
		DoubanBookUtils.searchBooks(this, keyword, 0, 20, 
				new SearchHttpHandler(this, HttpListener.SEARCH_BOOKS));
	}
	
	
	protected void fillBookList(ArrayList<DoubanBook> booklist) {
		Log.d("DEBUG", "fill search list....");
		adapter = new MySimpleAdapter(this, booklist);
		setListAdapter(adapter);

	}
	
	
	protected void loadMoreBooks() {
		// TODO Auto-generated method stub
		DoubanBookUtils.searchBooks(this, key, currentMax, 20, 
				new SearchHttpHandler(this, HttpListener.SEARCH_BOOKS_MORE));
		Log.d("DEBUG", "currentMax --- " + currentMax);
	}


	/**
	 * Http请求监听器，用于处理HttpAsyncTask中的响应事件
	 * @author tinyao
	 *
	 */
	private class HttpTaskListener implements HttpListener{

		int type;
		
		public HttpTaskListener(int type1){
			this.type = type1;
		}

		@Override
		public void onTaskCompleted(Object data) {
			// TODO Auto-generated method stub
			Log.d("DEBUG", "respone: " + data);
			switch(type){
			case HttpListener.SEARCH_BOOKS: // 获取到书籍信息
				if(data == null){
					Crouton.makeText(BookSearchListActivity.this, R.string.book_not_found, Style.ALERT).show();
//					if(pd != null) pd.dismiss();
					loadView.setVisibility(View.GONE);
					break;
				}
				
				//Log.d("DEBUG", "" + data);
				Log.d("DEBUG", "start to parse json...");
				
				doubanList = DoubanBookUtils.parseSearchBooks(String.valueOf(data));
				
				loadView.setVisibility(View.GONE);
				
				fillBookList(doubanList);
				
				saveSearchResults();
				
				currentMax = 20;
				
				Log.d("DEBUG", "----------SEARCH_BOOKS----------" + currentMax);
				
				break;
				
			case HttpListener.SEARCH_BOOKS_MORE:
				
				ArrayList<DoubanBook> rr = DoubanBookUtils.parseSearchBooks(String.valueOf(data));
				
//				try {
//					JSONObject json = new JSONObject(String.valueOf(data));
//					Log.d("DEBUG", "----" + json
//							+ "\n\n\n" + doubanList);
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
                refreshPd.setVisibility(View.GONE);  
                adapter = new MySimpleAdapter(BookSearchListActivity.this, doubanList);
                doubanList.addAll(rr);
                adapter.notifyDataSetChanged();  
                
				currentMax = currentMax + 20;
				Log.d("DEBUG", "----------load more----------" + currentMax);
				
				break;
			}
		}

		@Override
		public void onTaskFailed(String data) {
			// TODO Auto-generated method stub
			loadView.setVisibility(View.GONE);
			Crouton.makeText(BookSearchListActivity.this, data, Style.ALERT).show();
		}
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
				convertView = mInflater.inflate(R.layout.book_search_result_item, 
						parent, false);
				holder = new ViewHolder();
				holder.title = (TextView) convertView
						.findViewById(R.id.search_item_book_title);
				holder.publisher = (TextView) convertView
						.findViewById(R.id.search_item_book_publisher);
				holder.author = (TextView) convertView.findViewById(R.id.search_item_book_author);
				holder.cover = (ImageView) convertView.findViewById(R.id.search_item_book_cover);
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
			
			if(MyApplication.displayCover){
				coverLoader.download(item, holder.cover);
			}
			return convertView;
		}

		public class ViewHolder {
			TextView title;
			TextView publisher;
			TextView author;
			ImageView cover;
		}

	}
	
	/**
	 * custom the searchview
	 * @param view
	 * @param index
	 */
	private void traverseView(View view, int index) {
	    if (view instanceof SearchView) {
	        SearchView v = (SearchView) view;
	        for(int i = 0; i < v.getChildCount(); i++) {
	            traverseView(v.getChildAt(i), i);
	        }
	    } else if (view instanceof LinearLayout) {
	        LinearLayout ll = (LinearLayout) view;
	        for(int i = 0; i < ll.getChildCount(); i++) {
	            traverseView(ll.getChildAt(i), i);
	        }
	    } else if (view instanceof EditText) {
	        ((EditText) view).setTextColor(Color.WHITE);
	        ((EditText) view).setHintTextColor(Color.WHITE);
	    } else if (view instanceof TextView) {
	        ((TextView) view).setTextColor(Color.WHITE);
	    } else if (view instanceof ImageView) {
	        // TODO dissect images and replace with custom images
	    } else {
	        Log.v("View Scout", "Undefined view type here...");
	    }
	}

	SearchResultDBHelper resultshelper;
	
	public void saveSearchResults() {
		// TODO Auto-generated method stub
		resultshelper = new SearchResultDBHelper(this);
		
		new Thread(){
			public void run(){
				resultshelper.openDataBase();
				if(doubanList.size() != 0 && 
						!resultshelper.hasContained(key, doubanList.get(0).isbn13)){
					for(DoubanBook item : doubanList){
						resultshelper.insert(key, item);
					}
				}
				resultshelper.getDB().close();
				resultshelper.close();
			}
		}.start();
		
	}
	
	public boolean searchBookfromCache(String keyword){
		resultshelper = new SearchResultDBHelper(this);
		resultshelper.openDataBase();
		Cursor cursor = resultshelper.selectByKey(keyword);
		if (cursor.getCount() > 0) {
			doubanList = new ArrayList<DoubanBook>();
			while(cursor.moveToNext()){
				DoubanBook item = new DoubanBook().init(cursor);
				doubanList.add(item);
			}
			loadView.setVisibility(View.GONE);
			fillBookList(doubanList);
			currentMax = 20;
			return true;
		}
		resultshelper.getDB().close();
		resultshelper.close();
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == Activity.RESULT_OK){
			String codeFormat = data.getStringExtra("bracode_format");
			String codeText = data.getStringExtra("bracode_text");
			
			if(codeFormat.contains("EAN")){
				Intent i = new Intent(BookSearchListActivity.this, BookInfoActivity.class);
				i.putExtra("isbn", codeText);
				startActivity(i);
			}else{
				Crouton.makeText(BookSearchListActivity.this, "Invalid ISBN !", Style.ALERT).show();
			}
			
		}
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			finish();
			overridePendingTransition(R.anim.enter_fade_in, R.anim.shrink_exit_top);
			return false;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	class SearchHttpHandler extends CustomAsyncHttpResponseHandler{

		public SearchHttpHandler(Context con, int taskId) {
			super(con, taskId);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onSuccess(int httpResultCode, String response) {
			// TODO Auto-generated method stub
			super.onSuccess(httpResultCode, response);
			
			Log.d("DEBUG", "Success search response: " + response);
			
			switch(taskId){
			case HttpListener.SEARCH_BOOKS: // 获取到书籍信息
				if(response == null){
					Crouton.makeText(BookSearchListActivity.this, R.string.book_not_found, Style.ALERT).show();
					loadView.setVisibility(View.GONE);
					break;
				}
				
				Log.d("DEBUG", "start to parse json...");
				doubanList = DoubanBookUtils.parseSearchBooks(response);
				
				loadView.setVisibility(View.GONE);
				
				fillBookList(doubanList);
				saveSearchResults();
				currentMax = 20;
				Log.d("DEBUG", "----------SEARCH_BOOKS----------" + currentMax);
				break;
				
			case HttpListener.SEARCH_BOOKS_MORE:
				
				ArrayList<DoubanBook> rr = DoubanBookUtils.parseSearchBooks(response);
				
//				try {
//					JSONObject json = new JSONObject(String.valueOf(data));
//					Log.d("DEBUG", "----" + json
//							+ "\n\n\n" + doubanList);
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
                refreshPd.setVisibility(View.GONE);  
                adapter = new MySimpleAdapter(BookSearchListActivity.this, doubanList);
                doubanList.addAll(rr);
                adapter.notifyDataSetChanged();  
                
				currentMax = currentMax + 20;
				Log.d("DEBUG", "----------load more----------" + currentMax);
				
				break;
			}
		}
		
		@Override
		public void onFailure(Throwable arg0, String arg1) {
			// TODO Auto-generated method stub
			super.onFailure(arg0, arg1);
			Log.d("DEBUG", "Failure search response: " + arg1);
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			super.onFinish();
			loadView.setVisibility(View.GONE);
		}
		
	};
	
}
