package com.czzz.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.ActionBar;
import com.czzz.base.DrawerBaseActivity;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.bookcircle.BookUtils;
import com.czzz.bookcircle.DirectMsg;
import com.czzz.bookcircle.MsgThread;
import com.czzz.bookcircle.MyApplication;
import com.czzz.bookcircle.task.AlarmTask;
import com.czzz.data.MsgThreadHelper;
import com.czzz.data.SearchDBHelper;
import com.czzz.data.UserBooksHelper;
import com.czzz.demo.listadapter.NearbyBooksAdapter;
import com.czzz.douban.DoubanBookUtils;
import com.czzz.utils.HttpListener;
import com.czzz.view.LoadingFooter;
import com.czzz.view.LoadingFooter.State;
import com.czzz.view.PositionAwareListView;
import com.czzz.view.SchoolPopupDialog;
import com.czzz.view.SpinnerPopupDialog;
import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class HomeActivity extends DrawerBaseActivity implements PullToRefreshAttacher.OnRefreshListener{
	
	private static final int DEFAULT_NUM = 15;
	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private SharedPreferences sp;
	private PositionAwareListView listView;
	private LoadingFooter mLoadingFooter;
	private AutoCompleteTextView searchEdt;
	private ImageView searchFor;
	private TextView unreadTxt;
	private Spinner actionSpinner;
	private CheckBox filterBtn;
	
	private ArrayList<BookCollection> nearbyBooks = new ArrayList<BookCollection>();
	private ArrayList<BookCollection> followBooks;
	private ArrayList<BookCollection> otherSchoolBooks;
	private ArrayList<BookCollection> randomBooks;
	private NearbyBooksAdapter nearbyBooksAdapter, followBooksAdapter, otherSchoolBooksAdapter, randomAdapter;
	private int statusId = 2, sortId = 0;
    private int school_id = User.getInstance().school_id; // initial school id
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        
        initLayoutActionBar();
        
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this, true);

        mPullToRefreshAttacher.addRefreshableView(listView, this);
        
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				// TODO Auto-generated method stub
				BookCollection book = (BookCollection)arg0.getAdapter().getItem(pos);
				
//				BookCollection book = nearbyBooks.get(pos - 1);
				Intent goIntent = new Intent(HomeActivity.this, BookInfoActivity.class);
				goIntent.putExtra("collection", (Serializable)book);
				goIntent.putExtra("from_explore", true);
				goIntent.putExtra("full", true);
				startActivity(goIntent);
			}
			
        });
        
        registerReceiver();
        
        nearbyBooksAdapter = new NearbyBooksAdapter(this, nearbyBooks);
        listView.setAdapter(nearbyBooksAdapter);
        obtainNearbyBooks();
        
        User.getInstance().init(this);
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
	    View actionSpinnerLay = inflater.inflate(R.layout.action_spinner, null, false);
	    ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
	    layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
	    mActionBar.setCustomView(actionSpinnerLay, layoutParams);
	    actionSpinner = (Spinner) actionSpinnerLay.findViewById(R.id.action_spinner_btn);
	    
	    // fill the listview
	    listView = (PositionAwareListView) findViewById(android.R.id.list);
	    filterBtn = (CheckBox) findViewById(R.id.filter_btn);
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
	    
	    actionSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				// TODO Auto-generated method stub
				
				Log.d("DEBUG", "spinner selected");
				
				currentSection = pos;
				if(currentSection != SectionID.SECTION_OTHER_SCHOOL){
					school_id = User.getInstance().school_id;
				}
				
				if(currentSection == SectionID.SECTION_RANDOM){
					mLoadingFooter.setState(State.TheEnd);
				} else {
					mLoadingFooter.setState(State.Idle, 1000);
				}
				
				switch(pos){
				case SectionID.SECTION_SAME_SCHOOL:
					listView.setAdapter(nearbyBooksAdapter, true);
					break;
				case SectionID.SECTION_FOLLOW:
					if (followBooks == null){ // 没有获取：第一次切换
						followBooks = new ArrayList<BookCollection>();
						followBooksAdapter = new NearbyBooksAdapter(HomeActivity.this, followBooks);
						listView.setAdapter(followBooksAdapter);
						mPullToRefreshAttacher.setRefreshing(true);
						BookUtils.fetchFollowUserBooks(0, 0,
								new HomeResponeHandler(HomeActivity.this, HttpListener.BOOKS_FOLLOW_USER));
					} else{
						listView.setAdapter(followBooksAdapter, true);
					}
					break;
				case SectionID.SECTION_OTHER_SCHOOL:
					school_id = preSchool_id;
					if(otherSchoolBooks == null){
						otherSchoolBooks = new ArrayList<BookCollection>();
						otherSchoolBooksAdapter = new NearbyBooksAdapter(HomeActivity.this, otherSchoolBooks);
						listView.setAdapter(otherSchoolBooksAdapter, true);
					} else {
						listView.setAdapter(otherSchoolBooksAdapter, true);
					}
					showSchoolDialog();
					break;
				case SectionID.SECTION_RANDOM:
					if(randomBooks == null){
						randomBooks = new ArrayList<BookCollection>();
						randomAdapter = new NearbyBooksAdapter(HomeActivity.this, randomBooks);
						listView.setAdapter(randomAdapter, true);
					} else {
						listView.setAdapter(randomAdapter);
					}
					mPullToRefreshAttacher.setRefreshing(true);
					BookUtils.fetchNearby(0, 0, DEFAULT_NUM, 2, 1, 
							new HomeResponeHandler(HomeActivity.this, HttpListener.FETCH_NEARBY_BOOKS));
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "nothing Selected: " + currentSection);
				if(currentSection == SectionID.SECTION_OTHER_SCHOOL){
					showSchoolDialog();
				}
			}
	    	
	    });
	    
	    filterBtn.setVisibility(View.VISIBLE);
	    filterBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ShowFilterDialog();
			}
	    	
	    });
	    
	    initSearchEdtAdapter();
	    
        menuNameTxt.setText(User.getInstance().name);
        	if(User.getInstance().avatar.equals("")){
            menuAvatarTxt.setText(User.getInstance().name.substring(0, 1).toUpperCase(Locale.CHINA));
        }
        MyApplication.imagesLoader.download(User.getInstance().avatar, menuAvatarImg);
	    
	    new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				ArrayList<MsgThread> threads = MsgThreadHelper
						.getInstance(HomeActivity.this).getCachedThread();
				if(threads == null) return;
				for(MsgThread th : threads){
					unreadCount += th.unread_count;
				}
				mHandler.sendEmptyMessage(0);
			}
        	
        }.start();
	}
	
	private int currentSection = SectionID.SECTION_SAME_SCHOOL;
	int preSchool_id = User.getInstance().school_id;
	
	private static class SectionID {
		private static final int SECTION_SAME_SCHOOL = 0;
		private static final int SECTION_FOLLOW = 1;
//		private static final int SECTION_SAME_CITY = 2;
		private static final int SECTION_OTHER_SCHOOL = 2;
		private static final int SECTION_RANDOM = 3;
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
	
	private ImageView menuAvatarImg;
	private TextView menuNameTxt, menuAvatarTxt;
	
	@Override
	protected View initDrawerView() {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        View drawerView = inflater.inflate(R.layout.menu_drawer_layout, null);
        
        menuAvatarImg = (ImageView) drawerView.findViewById(R.id.drawer_menu_user_avatar);
        menuNameTxt = (TextView) drawerView.findViewById(R.id.drawer_menu_user_name);
        menuAvatarTxt = (TextView) drawerView.findViewById(R.id.drawer_menu_user_avatar_txt);
        drawerView.findViewById(R.id.drawer_menu_addbook).setOnClickListener(menuClickListener);
        drawerView.findViewById(R.id.drawer_menu_user_lay).setOnClickListener(menuClickListener);
        drawerView.findViewById(R.id.drawer_menu_message).setOnClickListener(menuClickListener);
        drawerView.findViewById(R.id.drawer_menu_book).setOnClickListener(menuClickListener);
        drawerView.findViewById(R.id.drawer_menu_users).setOnClickListener(menuClickListener);;
        drawerView.findViewById(R.id.drawer_menu_message).setOnClickListener(menuClickListener);;
        drawerView.findViewById(R.id.drawer_menu_douban).setOnClickListener(menuClickListener);;
        drawerView.findViewById(R.id.drawer_menu_setting).setOnClickListener(menuClickListener);;
        unreadTxt = (TextView)drawerView.findViewById(R.id.drawer_menu_msg_unread_txt);
        
        return drawerView;
	}
	
	private int unreadCount = 0;
	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.what!=0) return;
			if(unreadCount != 0){
				unreadTxt.setText("" + unreadCount);
				unreadTxt.setVisibility(View.VISIBLE);
			}else{
				unreadTxt.setVisibility(View.GONE);
			}
		}
		
	};
	
	private SchoolPopupDialog schoolDialog;

	private void showSchoolDialog(){
		if(schoolDialog == null){
			schoolDialog = new SchoolPopupDialog(
					this, R.style.school_popup_style,
					0);
		}
		schoolDialog.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				// TODO Auto-generated method stub
				String school = schoolDialog.updateUniversity;
				int sid = schoolDialog.updateUnivId;
				if(schoolDialog.changed){
					school_id = sid;
					preSchool_id = sid;
					schoolDialog.changed = false;
					mPullToRefreshAttacher.setRefreshing(true);
					
					fetchNeabyBooks(0, DEFAULT_NUM, school_id, statusId, sortId);
				}
				school_id = sid;
			}
			
		});
		schoolDialog.show();
	}
	
	private SpinnerPopupDialog dialogFilterBooks;
	
	private void ShowFilterDialog(){
		
		if(dialogFilterBooks == null){
			int height = filterBtn.getHeight();
			dialogFilterBooks = new SpinnerPopupDialog(this, 
					R.style.spinner_popup_right_style, height, 0, 0);
		}
		
		dialogFilterBooks.setOnDismissListener(new OnDismissListener(){

			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				if(dialogFilterBooks.filterChanged){
					statusId = dialogFilterBooks.status;
					sortId = dialogFilterBooks.sort;
					mPullToRefreshAttacher.setRefreshing(true);
					
					switch(currentSection){
					case SectionID.SECTION_SAME_SCHOOL:
						nearbyBooks.clear();
						fetchNeabyBooks(0, DEFAULT_NUM, school_id, statusId, sortId);
						break;
					case SectionID.SECTION_OTHER_SCHOOL:
						otherSchoolBooks.clear();
						fetchNeabyBooks(0, DEFAULT_NUM, school_id, statusId, sortId);
						break;
					case SectionID.SECTION_FOLLOW:
						followBooks.clear();
						BookUtils.fetchFollowUserBooks(0, 0,
								new HomeResponeHandler(HomeActivity.this, HttpListener.BOOKS_FOLLOW_USER));
						break;
					case SectionID.SECTION_RANDOM:
						randomBooks.clear();
						BookUtils.fetchNearby(0, 0, DEFAULT_NUM, statusId, 1, 
								new HomeResponeHandler(HomeActivity.this, HttpListener.FETCH_NEARBY_BOOKS));
						break;
					}
					
					dialogFilterBooks.filterChanged = false;
				}
				filterBtn.setChecked(false);
			}
			
		});
		
		dialogFilterBooks.show();
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
			Intent userExploreIntent = new Intent(this, UserExploreActivity.class);
			startActivity(userExploreIntent);
			break;
		case R.id.drawer_menu_message:
			Intent msgIntent = new Intent(this, MessageActivity.class);
			startActivity(msgIntent);
			break;
		case R.id.drawer_menu_douban:
//			Intent userIntent = new Intent(this, UserPageActivity.class);
//			startActivity(userIntent);
			break;
		case R.id.drawer_menu_setting:
			Intent settingIntent = new Intent(this, SettingActivity.class);
			startActivity(settingIntent);
			break;
		case R.id.drawer_menu_addbook:
			showBookAddDialog();
			break;
		}
	}
	
	private void showBookAddDialog(){
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.add_book_dialog, null);
        final View scanV = (View) view.findViewById(R.id.add_book_to_scan);
        final View searchV = (View) view.findViewById(R.id.add_book_to_search);
        final AlertDialog chooseDialog = new AlertDialog.Builder(this)
	    	.setTitle(R.string.add_books).setView(view)
	    	.create();
        scanV.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i1 = new Intent("com.czzz.action.qrscan.addbook");
				startActivityForResult(i1,0);
				chooseDialog.dismiss();
			}
        	
        });
        searchV.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i2 = new Intent(HomeActivity.this, BookSearchListActivity.class);
				i2.putExtra("add_book_search", true);
				startActivity(i2);
				overridePendingTransition(R.anim.grow_from_bottom, R.anim.fade_out_exit);
				chooseDialog.dismiss();
			}
        	
        });
    
        chooseDialog.setCanceledOnTouchOutside(true);
        chooseDialog.show();
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
		mLoadingFooter.setState(LoadingFooter.State.Loading);
		
		switch(currentSection){
		case SectionID.SECTION_SAME_SCHOOL:
			if(nearbyBooks != null && nearbyBooks.size() > 0){
				startCid = nearbyBooks.get(nearbyBooks.size() - 1).cid;
			}
			loadMoreNearbyBooks(startCid, 
					DEFAULT_NUM, school_id, statusId, sortId);
			break;
		case SectionID.SECTION_OTHER_SCHOOL:
			if(otherSchoolBooks != null && otherSchoolBooks.size() > 0){
				startCid = otherSchoolBooks.get(otherSchoolBooks.size() - 1).cid;
			}
			
			loadMoreNearbyBooks(startCid, 
					DEFAULT_NUM, school_id, statusId, sortId);
			break;
		case SectionID.SECTION_FOLLOW:
			BookUtils.fetchFollowUserBooks(followBooks.size(), 0,
					new HomeResponeHandler(HomeActivity.this, HttpListener.BOOKS_FOLLOW_USER));
			break;
		case SectionID.SECTION_RANDOM:
			mLoadingFooter.setState(State.TheEnd);
			break;
		}
		
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
	protected void updateNearbyBooks(ArrayList<BookCollection> books) {
		// TODO Auto-generated method stub
		
		if(books == null){
			mLoadingFooter.setState(LoadingFooter.State.TheEnd, 10);
		}else{
			
			switch(currentSection){
			case SectionID.SECTION_SAME_SCHOOL:
				nearbyBooks.addAll(books);
				nearbyBooksAdapter.notifyDataSetChanged();
				// 只缓存“status=全部 && 本校”的书籍
				if(nearbyBooks.size()<=DEFAULT_NUM && school_id == User.getInstance().school_id && statusId == 2){
					UserBooksHelper cachehelper = UserBooksHelper.getInstance(this);
					cachehelper.cacheNearbyCollections(books);
				}
				break;
			case SectionID.SECTION_OTHER_SCHOOL:
				if(mLoadingFooter.getState() == LoadingFooter.State.Loading){	// 不是加载更多，而是切换学校
					otherSchoolBooks.addAll(books);
					otherSchoolBooksAdapter.notifyDataSetChanged();
				}else{
					otherSchoolBooks.clear();
					otherSchoolBooks.addAll(books);
					listView.setAdapter(otherSchoolBooksAdapter, true);
				}
				break;
			case SectionID.SECTION_RANDOM:
				if(mLoadingFooter.getState() == LoadingFooter.State.Loading){	// 不是加载更多，而是切换学校
					randomBooks.addAll(books);
					randomAdapter.notifyDataSetChanged();
				}else{
					randomBooks.clear();
					randomBooks.addAll(books);
					listView.setAdapter(randomAdapter, true);
				}
				break;
			}
				
		}
		
	}
	
	/* update nearby books */
//	protected void updateOtherSchoolBooks(ArrayList<BookCollection> books) {
//		// TODO Auto-generated method stub
//		if(books == null){
//			mLoadingFooter.setState(LoadingFooter.State.TheEnd, 10);
//		}else{
//			if(mLoadingFooter.getState() != LoadingFooter.State.Loading){
//				otherSchoolBooks.clear();
//			}
//			otherSchoolBooks.addAll(books);
//			otherSchoolBooksAdapter.notifyDataSetChanged();
//		}
//	}
	
	private void refreshNewNearbyBooks(ArrayList<BookCollection> newbooks){
		
		if(newbooks != null){
			
			switch(currentSection){
			case SectionID.SECTION_SAME_SCHOOL:
				nearbyBooks.addAll(0, newbooks);
				break;
			case SectionID.SECTION_OTHER_SCHOOL:
				otherSchoolBooks.addAll(0, newbooks);
				break;
			case SectionID.SECTION_FOLLOW:
				followBooks.addAll(0, followBooks);
				break;
			}
			
			if(school_id == User.getInstance().school_id && statusId == 2){		// 缓存
				UserBooksHelper cachehelper = UserBooksHelper.getInstance(this);
				cachehelper.cacheNearbyCollections(newbooks);
			}
			
			if(mPullToRefreshAttacher.isRefreshing()){
				if(newbooks.size() > 0) 
					Crouton.makeText(this, "加载了" + newbooks.size() + "本新藏书", Style.CONFIRM).show();
				
				switch(currentSection){
				case SectionID.SECTION_SAME_SCHOOL:
					nearbyBooksAdapter.notifyDataSetChanged();
					break;
				case SectionID.SECTION_OTHER_SCHOOL:
					otherSchoolBooksAdapter.notifyDataSetChanged();
					break;
				case SectionID.SECTION_FOLLOW:
					followBooksAdapter.notifyDataSetChanged();
					break;
				}
				
			}
		}
		
		if(mPullToRefreshAttacher.isRefreshing()){
			mPullToRefreshAttacher.setRefreshComplete();
		}
	}
	
	
	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
		switch(currentSection){
		case SectionID.SECTION_SAME_SCHOOL:
			fetchNeabyNewBooks(nearbyBooks.size()==0 ? 0 : nearbyBooks.get(0).cid, statusId);
			break;
		case SectionID.SECTION_OTHER_SCHOOL:
			fetchNeabyNewBooks(nearbyBooks.size()==0 ? 0 : nearbyBooks.get(0).cid, statusId);
			break;
		case SectionID.SECTION_FOLLOW:
			Log.d("DEBUG", "follow CID: " + followBooks.get(0).cid);
			BookUtils.fetchFollowUserBooks(0, followBooks.get(0).cid,
					new HomeResponeHandler(HomeActivity.this, HttpListener.FETCH_NEARBY_BOOKS_NEW));
			break;
		case SectionID.SECTION_RANDOM:
			randomBooks.clear();
			BookUtils.fetchNearby(0, 0, DEFAULT_NUM, 2, 1, 
					new HomeResponeHandler(HomeActivity.this, HttpListener.FETCH_NEARBY_BOOKS));
			break;
		}
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
			if(response == null) return;
			switch(taskId){
			case HttpListener.FETCH_NEARBY_BOOKS:
//				nearbyBooks.clear();
				ArrayList<BookCollection> books = BookUtils.parseNearybyBooks(response);
				updateNearbyBooks(books);
				break;
			case HttpListener.FETCH_NEARBY_BOOKS_NEW:
				ArrayList<BookCollection> newbooks = BookUtils.parseNearybyBooks(response);
				refreshNewNearbyBooks(newbooks);
				break;
			case HttpListener.FETCH_NEARBY_BOOKS_MORE:
				Log.d("DEBUG", "RESPONSE: " + response);
				ArrayList<BookCollection> booksMore = BookUtils.parseNearybyBooks(response);
				updateNearbyBooks(booksMore);
				mLoadingFooter.setState(LoadingFooter.State.Idle, 3000);
				break;
			case HttpListener.BOOKS_FOLLOW_USER:
				Log.d("DEBUG", "Response: " + response);
				ArrayList<BookCollection> fBooks = BookUtils.parseNearybyBooks(response);
				if(followBooks.size()==0 && fBooks == null){
					Crouton.makeText(HomeActivity.this, "您还没有关注其他书友", Style.ALERT).show();
				}else{
					followBooks.addAll(fBooks);
//					followBooksAdapter = new NearbyBooksAdapter(HomeActivity.this, followBooks);
//					listView.setAdapter(followBooksAdapter, true);
					followBooksAdapter.notifyDataSetChanged();
					if(mLoadingFooter.getState() == LoadingFooter.State.Loading){
						mLoadingFooter.setState(LoadingFooter.State.Idle, 3000);
					}
				}
				break;
			}
		}
		
		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			super.onFinish();
			if(mPullToRefreshAttacher.isRefreshing())
				mPullToRefreshAttacher.setRefreshComplete();
		}

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			super.onStart();
		}
		
	}
	
//	private class Offset{
//		int pos, top;
//		public Offset(int pos, int top){
//			this.pos = pos;
//			this.top = top;
//		}
//	}
//	
//	private Offset getListOffset(){
//		int savedPosition = listView.getFirstVisiblePosition();
//		View firstVisibleView = listView.getChildAt(0);
//		int savedTop = (firstVisibleView == null) ? 0 : firstVisibleView.getTop();
//		return new Offset(savedPosition, savedTop);
//	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if(helper != null){
			helper.getDB().close();
			User.clearTaList();
			this.unregisterReceiver(receiver);
		}
		super.onDestroy();
	}
	
	private static final String ACTION_UPDATE_BOOKS = "update_user_books";
	private static final String ACTION_DELETE_ITEM = "delete_user_book_item";
	private static final String ACTION_UPDATE_ITEM = "update_user_book_item";
	public static final String ACTION_LOAD_MSG_NEW = "bookcircle.task.new_msg_update_home";
	public static final String ACTION_THREAD_CLEAR_UNREAD = "action_clear_unread";
	
	private UpdateReceiver receiver; 
	
	private void registerReceiver(){
		receiver = new UpdateReceiver();
        IntentFilter filter=new IntentFilter();  
        filter.addAction(ACTION_DELETE_ITEM);
        filter.addAction(ACTION_UPDATE_ITEM);
        filter.addAction(ACTION_UPDATE_BOOKS);
        filter.addAction(ACTION_LOAD_MSG_NEW);
        filter.addAction(ACTION_THREAD_CLEAR_UNREAD);

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
						break;
					}
				}
			}
			
			if(intent.getAction().equals(ACTION_LOAD_MSG_NEW)){
				ArrayList<DirectMsg> new_msgs 
						= intent.getParcelableArrayListExtra("new_msgs");
				unreadCount += new_msgs.size();
				if(unreadCount!=0){
					unreadTxt.setText("" + unreadCount);
					unreadTxt.setVisibility(View.VISIBLE);
				}else{
					unreadTxt.setVisibility(View.GONE);
				}
			}
			
			if(intent.getAction().equals(ACTION_THREAD_CLEAR_UNREAD)){
				unreadCount = 0;
				ArrayList<MsgThread> threads = MsgThreadHelper
						.getInstance(HomeActivity.this).getCachedThread();
				for(MsgThread th : threads){
					unreadCount += th.unread_count;
				}
				mHandler.sendEmptyMessage(0);
			}
			
		}
		
	}
	
}
