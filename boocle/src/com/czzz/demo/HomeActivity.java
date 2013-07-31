package com.czzz.demo;

import java.io.Serializable;
import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.ActionBar;
import com.czzz.base.DrawerBaseActivity;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.bookcircle.BookUtils;
import com.czzz.bookcircle.MyApplication;
import com.czzz.bookcircle.task.AlarmTask;
import com.czzz.data.SearchDBHelper;
import com.czzz.data.UserBooksHelper;
import com.czzz.demo.listadapter.NearbyBooksAdapter;
import com.czzz.douban.DoubanBookUtils;
import com.czzz.utils.HttpListener;
import com.czzz.utils.ImagesDownloader;
import com.czzz.view.LoadingFooter;
import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class HomeActivity extends DrawerBaseActivity implements PullToRefreshAttacher.OnRefreshListener{
	
	private static final int DEFAULT_NUM = 15;
	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private SharedPreferences sp;
	private ListView listView;
	private LoadingFooter mLoadingFooter;
	private AutoCompleteTextView searchEdt;
	private ImageView searchFor;
	
	private ArrayList<BookCollection> nearbyBooks = new ArrayList<BookCollection>();
	private NearbyBooksAdapter nearbyBooksAdapter;
	private int statusId = 2, sortId = 0;
    private int school_id = User.getInstance().school_id; // initial school id
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preCheck();	// checnk for logout and msg-alarm
        
        initLayoutActionBar();
	    
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

        mPullToRefreshAttacher.addRefreshableView(listView, this);
        
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				// TODO Auto-generated method stub
				BookCollection book = nearbyBooks.get(pos - 1);
				Intent goIntent = new Intent(HomeActivity.this, BookInfoActivity.class);
				goIntent.putExtra("collection", (Serializable)book);
				goIntent.putExtra("from_explore", true);
				goIntent.putExtra("full", true);
				startActivity(goIntent);
			}
			
        });
        
        registerReceiver();
        
        obtainNearbyBooks();
	}
	
	/* 初始化fadingActionbar，设置自定义view */
	private void initLayoutActionBar(){
		final FadingActionBarHelper actionHelper = new FadingActionBarHelper()
	        .actionBarBackground(R.drawable.actionbar_base) 
	        .headerLayout(R.layout.header)
	        .headerPanelLayout(R.layout.search)
	        .contentLayout(R.layout.activity_home);
	    setContentView(actionHelper.createView(this));
	    actionHelper.initActionBar(this);
	    
	    ActionBar mActionBar = this.getSupportActionBar();
	    mActionBar.setDisplayShowCustomEnabled(true);
	    mActionBar.setDisplayShowTitleEnabled(true);
	    
	    LayoutInflater inflater = LayoutInflater.from(this);
	    View actionSpinner = inflater.inflate(R.layout.action_spinner, null, false);
	    ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
	    layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
	    mActionBar.setCustomView(actionSpinner, layoutParams);
	    
	    // fill the listview
	    listView = (ListView) findViewById(android.R.id.list);
	    mLoadingFooter = new LoadingFooter(this);
	    listView.addFooterView(mLoadingFooter.getView());
	    
	    View headerLayout = actionHelper.getCustomHeader();
	    searchEdt = (AutoCompleteTextView) headerLayout.findViewById(R.id.search_edt);
	    searchFor = (ImageView) headerLayout.findViewById(R.id.search_forward);
	    
	    listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
            	
            	actionHelper.onListSrcoll(view);
            	
                if (mLoadingFooter.getState() == LoadingFooter.State.Loading
                        || mLoadingFooter.getState() == LoadingFooter.State.TheEnd) {
                    return;
                }
                if (firstVisibleItem + visibleItemCount >= totalItemCount
                        && totalItemCount != 0
                        && totalItemCount != listView.getHeaderViewsCount()
                                + listView.getFooterViewsCount() && nearbyBooks.size() > 0) {
                    
                	Log.d("DEBUG", "load next page");
                	loadNextPage();
                }
            }
        });
	    
	    searchEdt.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				
				if(actionId == EditorInfo.IME_ACTION_SEARCH
						|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
					String searchStr = searchEdt.getText().toString();
					searchForISBN(searchStr);
				}
				return true;
			}
			
		});
	    
	    searchEdt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if(searchEdt.getText().toString().equals("")){
					searchFor.setImageResource(R.drawable.isbn);
				}else{
					searchFor.setImageResource(R.drawable.ic_search_inverse);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
			}
			
		});
	    
	    searchFor.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String searchStr = searchEdt.getText().toString();
				if(!searchStr.equals("")){
					searchForISBN(searchStr);
				}else{
					Intent ii = new Intent("com.czzz.action.qrscan");
					startActivityForResult(ii,0);
				}
			}
	    	
	    });
	    
	    initSearchEdtAdapter();
	}
	
	private SearchDBHelper helper;
	private ArrayList<String> arrayKeys;
	private ArrayAdapter<String> searchAdapter;
	
	private void initSearchEdtAdapter() {
		// TODO Auto-generated method stub
    	
    	helper = new SearchDBHelper(this);
    	helper.openDataBase();
    	
    	Cursor cursor = helper.select();
    	
    	arrayKeys = new ArrayList<String>();
    	while(cursor.moveToNext()){
    		arrayKeys.add(cursor.getString(cursor.getColumnIndexOrThrow(SearchDBHelper.KEY_NAME)));
    	}
    	
    	// 定义字符串数组作为提示的文本
	    searchAdapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, arrayKeys);
	    searchEdt.setAdapter(searchAdapter);
	}
	
	protected void updateSearchHistory(String searchStr) {
		// TODO Auto-generated method stub
    	if(!arrayKeys.contains(searchStr)){
    		helper.insert(searchStr, "0");
    		arrayKeys.add(searchStr);
    		searchAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.select_dialog_item, arrayKeys);
        	searchEdt.setAdapter(searchAdapter);
    	}
	}
	
	private void searchForISBN(String seakey){
		if(DoubanBookUtils.isISBNLike(seakey)){
			if(!DoubanBookUtils.isISBN(seakey)){
				Crouton.makeText(HomeActivity.this, R.string.isbn_not_match, Style.ALERT).show();
			}else{
				Intent i = new Intent(HomeActivity.this, BookInfoActivity.class);
				i.putExtra("isbn", seakey);
				startActivity(i);
				overridePendingTransition(R.anim.shrink_from_bottom,R.anim.exit_fade_out);
			}
		}else{
			updateSearchHistory(seakey);
			Intent i = new Intent(HomeActivity.this, BookSearchListActivity.class);
			i.putExtra("keyword", seakey);
			startActivity(i);
			overridePendingTransition(R.anim.shrink_from_bottom,R.anim.exit_fade_out);
		}
		InputMethodManager imm = (InputMethodManager)HomeActivity.this
				.getSystemService(Context.INPUT_METHOD_SERVICE); 
		imm.hideSoftInputFromWindow(searchEdt.getWindowToken(), 0);
	}
	
	private void preCheck(){
		boolean finish = getIntent().getBooleanExtra("finish", false);
        if (finish) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        sp = this.getSharedPreferences("config", 0);
		if(!sp.getBoolean("alarm_set", false)) {
			AlarmTask.setMsgAlarm(this);
		    sp.edit().putBoolean("alarm_set", true).commit();
		}
	}
	
	@Override
	protected View initDrawerView() {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        View drawerView = inflater.inflate(R.layout.menu_drawer_layout, null);
        
        ImageView avatarImg = (ImageView) drawerView.findViewById(R.id.drawer_menu_user_avatar);
        TextView nameTxt = (TextView) drawerView.findViewById(R.id.drawer_menu_user_name);
        drawerView.findViewById(R.id.drawer_menu_user_lay).setOnClickListener(menuClickListener);
        drawerView.findViewById(R.id.drawer_menu_message).setOnClickListener(menuClickListener);
        drawerView.findViewById(R.id.drawer_menu_book).setOnClickListener(menuClickListener);
        drawerView.findViewById(R.id.drawer_menu_users).setOnClickListener(menuClickListener);;
        drawerView.findViewById(R.id.drawer_menu_message).setOnClickListener(menuClickListener);;
        drawerView.findViewById(R.id.drawer_menu_douban).setOnClickListener(menuClickListener);;
        drawerView.findViewById(R.id.drawer_menu_setting).setOnClickListener(menuClickListener);;
        
        nameTxt.setText(User.getInstance().name);
        MyApplication.imagesLoader.download(User.getInstance().avatar, avatarImg);
        return drawerView;
	}

	@Override
	protected void onMenuItemClicked(int resId) {
		// TODO Auto-generated method stub
		switch(resId){
		case R.id.drawer_menu_user_lay:
			Intent userIntent = new Intent(this, UserPageActivity.class);
			startActivity(userIntent);
			break;
		case R.id.drawer_menu_book:
			mMenuDrawer.closeMenu();
			break;
		case R.id.drawer_menu_users:
//			Intent userIntent = new Intent(this, UserPageActivity.class);
//			startActivity(userIntent);
			break;
		case R.id.drawer_menu_message:
//			Intent msgIntent = new Intent(this, UserPageActivity.class);
//			startActivity(userIntent);
			break;
		case R.id.drawer_menu_douban:
//			Intent userIntent = new Intent(this, UserPageActivity.class);
//			startActivity(userIntent);
			break;
		case R.id.drawer_menu_setting:
			Intent settingIntent = new Intent(this, SettingActivity.class);
			startActivity(settingIntent);
			break;
		}
	}
	
	/**
	 * fetch nearby Books
	 */
	private void obtainNearbyBooks() {
		// TODO Auto-generated method stub
		
		UserBooksHelper cachehelper = UserBooksHelper.getInstance(this);
		ArrayList<BookCollection> cacheBooks = cachehelper.getCachedNearbyCollections();
		
		if(cacheBooks == null || cacheBooks.size() == 0){
			fetchNeabyBooks(0, DEFAULT_NUM, User.getInstance().school_id, 2, 0);
		}else{
			nearbyBooks.addAll(cacheBooks);
			nearbyBooksAdapter = new NearbyBooksAdapter(this, nearbyBooks);
			listView.setAdapter(nearbyBooksAdapter);
			fetchNeabyNewBooks(nearbyBooks.get(0).cid, statusId);
		}
		
	}
	
	private void loadNextPage(){
		int startCid = 0;
		if(nearbyBooks != null && nearbyBooks.size() > 0){
			startCid = nearbyBooks.get(nearbyBooks.size() - 1).cid;
		}
		
		mLoadingFooter.setState(LoadingFooter.State.Loading);
		loadMoreNearbyBooks(startCid, 
				DEFAULT_NUM, school_id, statusId, sortId);
	}
	
	private void fetchNeabyBooks(int start, int count, int school_id, int status, int sort) {
		// TODO Auto-generated method stub
		BookUtils.fetchNearby(school_id, start, count, status, sort, 
				new HomeResponeHandler(this, HttpListener.FETCH_NEARBY_BOOKS));
	}
	
	private void fetchNeabyNewBooks(int start, int status) {
		// TODO Auto-generated method stub
		BookUtils.fetchNearbyNewBooks(school_id, start, status, 
				new HomeResponeHandler(this, HttpListener.FETCH_NEARBY_BOOKS_NEW));
	}
	
	private void loadMoreNearbyBooks(int start, int count, 
			int school_id, int status, int sort) {
		// TODO Auto-generated method stub
		BookUtils.fetchNearby(school_id, start, count, status, sort, 
				new HomeResponeHandler(this, HttpListener.FETCH_NEARBY_BOOKS_MORE));
	}
	
	/* update nearby books */
	protected void updateNearbyBooks(String respone) {
		// TODO Auto-generated method stub
		// 网络返回
		ArrayList<BookCollection> books = BookUtils.parseNearybyBooks(respone);
		
		if(books == null){
//			Crouton.makeText(this, R.string.no_more, Style.CONFIRM).show();
			mLoadingFooter.setState(LoadingFooter.State.TheEnd, 10);
			if(nearbyBooks.size() == 0){
				nearbyBooksAdapter = new NearbyBooksAdapter(this, nearbyBooks);
				listView.setAdapter(nearbyBooksAdapter);
			}
		}else{
			if(nearbyBooksAdapter == null || nearbyBooks.size() == 0){
				nearbyBooks.addAll(books);
				nearbyBooksAdapter = new NearbyBooksAdapter(this, nearbyBooks);
				listView.setAdapter(nearbyBooksAdapter);
				
				// 只缓存“status=全部”的书籍
				if(school_id == User.getInstance().school_id && statusId == 2){
					UserBooksHelper cachehelper = UserBooksHelper.getInstance(this);
					cachehelper.cacheNearbyCollections(books);
				}
			} else {
				nearbyBooks.addAll(books);
//				nearbyBooksAdapter = new NearbyBooksAdapter(this, nearbyBooks);
				nearbyBooksAdapter.notifyDataSetChanged();
			}
		}
		
	}
	
	private void refreshNewNearbyBooks(String respone){
		Log.d("DEBUG", "" + respone);
		ArrayList<BookCollection> newbooks = BookUtils.parseNearybyBooks(respone);
		
		if(newbooks != null){
			nearbyBooks.addAll(0, newbooks);
			
			if(nearbyBooksAdapter == null)
				nearbyBooksAdapter = new NearbyBooksAdapter(this, nearbyBooks);
			
			if(school_id == User.getInstance().school_id && statusId == 2){
				UserBooksHelper cachehelper = UserBooksHelper.getInstance(this);
				cachehelper.cacheNearbyCollections(newbooks);
			}
			
			if(mPullToRefreshAttacher.isRefreshing()){
				if(newbooks.size() > 0) 
					Crouton.makeText(this, "加载了" + newbooks.size() + "本新藏书", Style.CONFIRM).show();
				nearbyBooksAdapter.notifyDataSetChanged();
//				listView.invalidate();
//				listView.setAdapter(nearbyBooksAdapter);
			}else{
				listView.setAdapter(nearbyBooksAdapter);
			}
			
		}
		
		if(mPullToRefreshAttacher.isRefreshing()){
			mPullToRefreshAttacher.setRefreshComplete();
		}
	}
	
	
	
	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
		fetchNeabyNewBooks(nearbyBooks.get(0).cid, statusId);
	}

	/**
	 * handle http response
	 * @author tinyao
	 *
	 */
	private class HomeResponeHandler extends CustomAsyncHttpResponseHandler{

		public HomeResponeHandler(Activity activity, int taskId) {
			super(activity, taskId);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onSuccess(int httpResultCode, String response) {
			// TODO Auto-generated method stub
			super.onSuccess(httpResultCode, response);
			
			switch(taskId){
			case HttpListener.FETCH_NEARBY_BOOKS:
				nearbyBooks.clear();
				updateNearbyBooks(response);
				break;
			case HttpListener.FETCH_NEARBY_BOOKS_NEW:
				refreshNewNearbyBooks(response);
				break;
			case HttpListener.FETCH_NEARBY_BOOKS_MORE:
				Log.d("DEBUG", "RESPONSE: " + response);
				updateNearbyBooks(response);
				mLoadingFooter.setState(LoadingFooter.State.Idle, 3000);
				break;
			}
		}
		
		@Override
		public void onFailure(Throwable arg0, String arg1) {
			// TODO Auto-generated method stub
			super.onFailure(arg0, arg1);
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			super.onFinish();
		}

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			super.onStart();
		}
		
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		helper.getDB().close();
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	private static final String ACTION_UPDATE_BOOKS = "update_user_books";
	private static final String ACTION_DELETE_ITEM = "delete_user_book_item";
	private static final String ACTION_UPDATE_ITEM = "update_user_book_item";
	
	private UpdateReceiver receiver; 
	
	private void registerReceiver(){
		receiver = new UpdateReceiver();
        IntentFilter filter=new IntentFilter();  
        filter.addAction(ACTION_DELETE_ITEM);
        filter.addAction(ACTION_UPDATE_ITEM);
        filter.addAction(ACTION_UPDATE_BOOKS);

        //动态注册BroadcastReceiver  
        this.registerReceiver(receiver, filter); 
	}
	
	/**
	 * 接收来自其他地方的通知，更新UI（用户信息以及书架信息）
	 * @author tinyao
	 *
	 */
	class UpdateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			if(intent.getAction().equals(ACTION_DELETE_ITEM)){
				int update_cid = intent.getIntExtra("cid", 0);
				for(BookCollection eitem : nearbyBooks){
					if(eitem.cid == update_cid){
						nearbyBooks.remove(eitem);
						nearbyBooksAdapter.notifyDataSetChanged();
						break;
					}
				}
			}
			
			if(intent.getAction().equals(ACTION_UPDATE_BOOKS)) {	// new books added
				ArrayList<BookCollection> newcollections 
						= intent.getParcelableArrayListExtra("new_books");
				if(newcollections != null){
					nearbyBooks.addAll(0, newcollections);
					nearbyBooksAdapter.notifyDataSetChanged();
				}
			}
			
			if(intent.getAction().equals(ACTION_UPDATE_ITEM)){
				int update_cid = intent.getIntExtra("cid", 0);
				for(BookCollection eitem : nearbyBooks){
					if(eitem.cid == update_cid){
						eitem.note = intent.getStringExtra("note");
						eitem.score = intent.getFloatExtra("score", 0);
						eitem.status = intent.getIntExtra("status", 0);
						nearbyBooksAdapter.notifyDataSetChanged();
//						listView.scrollTo(0, 0);
//						listView.onRestoreInstanceState(listState);
						break;
					}
				}
			}
			
		}
		
	}
	
}
