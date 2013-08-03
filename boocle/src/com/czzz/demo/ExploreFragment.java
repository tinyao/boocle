package com.czzz.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.czzz.base.BaseFragment;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.bookcircle.BookUtils;
import com.czzz.bookcircle.UserUtils;
import com.czzz.data.SearchDBHelper;
import com.czzz.data.SearchResultDBHelper;
import com.czzz.data.UserBooksHelper;
import com.czzz.demo.listadapter.DoubanRecommAdapter;
import com.czzz.demo.listadapter.NearbyBooksAdapter;
import com.czzz.demo.listadapter.NearbyUsersAdapter;
import com.czzz.douban.DoubanBook;
import com.czzz.douban.DoubanBookUtils;
import com.czzz.utils.HttpListener;
import com.czzz.view.LoadingView;
import com.czzz.view.RefreshListView;
import com.czzz.view.RefreshListView.OnRefreshListener;
import com.czzz.view.SchoolPopupDialog;
import com.czzz.view.SpinnerPopupDialog;
import com.czzz.view.UserFilterDialog;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ExploreFragment extends BaseFragment implements OnRefreshListener{	
	
	AutoCompleteTextView searchEdt;
	ImageView bottomSchool, bottomFilter;
	TextView bottomSection;
	View bottomMenu;
	private LoadingView loadingView;
	
	private static final int DEFAULT_NUM = 15;
	
	ImageView searchFor;
	
	Animation anim;
	boolean hasEntered = false;
	
	RefreshListView listview;
	
	private SearchDBHelper helper;
	private ArrayList<String> arrayKeys;
	private ArrayAdapter<String> searchAdapter;
	private NearbyUsersAdapter topAdapter;
	
	private Handler mHandler;
//	private int taskType;
	
	private View footerView;
	private ProgressBar morePd;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d("DEBUG", "EXPLORE -- onActivityCreated");
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d("DEBUG", "EXPLORE -- onCreate");
		registerReceiver(); 
  	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d("DEBUG", "EXPLORE -- onCreateView");
		
		View v = inflater.inflate(R.layout.fragment_explore, container, false);
		listview = (RefreshListView) v.findViewById(R.id.explore_user_list);
		loadingView = (LoadingView) v.findViewById(R.id.loading_view_explore);
		
		LayoutInflater layoutInflater = (LayoutInflater)ExploreFragment.this.getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		LinearLayout headerLayout = (LinearLayout)layoutInflater
        		.inflate(R.layout.explore_books_search_header, null, false );
        footerView = layoutInflater.inflate(R.layout.pulldown_footer, null);
        listview.addHeaderView(headerLayout);
        listview.addFooterView(footerView);
        
        morePd = (ProgressBar) v.findViewById(R.id.pulldown_footer_loading);
		morePd.setIndeterminate(false); 
		
        searchEdt = (AutoCompleteTextView) headerLayout.findViewById(R.id.search_edt);
		searchFor = (ImageView) headerLayout.findViewById(R.id.search_forward);
		
		bottomMenu = (View) v.findViewById(R.id.bottom_menu);
		
		bottomSection = (TextView) v.findViewById(R.id.bottom_menu_section);
		bottomSchool = (ImageView) v.findViewById(R.id.bottom_menu_tab);
		bottomFilter = (ImageView) v.findViewById(R.id.bottom_menu_filter);

		bottomSection.setOnClickListener(listener);
		bottomSchool.setOnClickListener(listener);
		bottomFilter.setOnClickListener(listener);
		
		listview.setAdapter(topAdapter);
		listview.setOnRefreshListener(this);
		
		listview.setOnScrollListener(new OnScrollListener(){

			private int mPosition = 0;
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				if(listview.isPulled) return;
				
				if(mPosition < firstVisibleItem){
					bottomMenu.setVisibility(View.GONE);
				}
				if(mPosition > firstVisibleItem){
					bottomMenu.setVisibility(View.VISIBLE);
				}
				
				mPosition = firstVisibleItem;
				
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
			}
			
		});
		
		footerView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				loadMoreItems();
			}
			
		});
		
		initSearchEdtAdapter();
		
		searchEdt.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				
				if(actionId == EditorInfo.IME_ACTION_SEARCH
						|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
					String searchStr = searchEdt.getText().toString();
					if(DoubanBookUtils.isISBNLike(searchStr)){
						if(!DoubanBookUtils.isISBN(searchStr)){
							Crouton.makeText(ExploreFragment.this.getActivity(), R.string.isbn_not_match, Style.ALERT).show();
						}else{
							Intent i = new Intent(ExploreFragment.this.getActivity(), BookInfoActivity.class);
							i.putExtra("isbn", searchStr);
							startActivity(i);
							getActivity().overridePendingTransition(R.anim.shrink_from_bottom,R.anim.exit_fade_out);
						}
					}else{
						updateSearchHistory(searchStr);
						Intent i = new Intent(ExploreFragment.this.getActivity(), BookSearchListActivity.class);
						i.putExtra("keyword", searchStr);
						startActivity(i);
						getActivity().overridePendingTransition(R.anim.shrink_from_bottom,R.anim.exit_fade_out);
					}
					InputMethodManager imm = (InputMethodManager)ExploreFragment.this.getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE); 
					imm.hideSoftInputFromWindow(searchEdt.getWindowToken(), 0);
				}else{

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
		
		searchFor.setOnClickListener(listener);
		
        listview.setOnItemClickListener(itemClickListener);
        
		fillNearbyBooks();
		
        return v;
	}
	
	private OnItemClickListener itemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			// TODO Auto-generated method stub
			Intent goIntent;
			switch(currentSection){
			case 0:
//				if(pos >= nearbyBooks.size()) return;
				BookCollection book = nearbyBooks.get(pos - 1);
				goIntent = new Intent(ExploreFragment.this.getActivity(), BookInfoActivity.class);
				goIntent.putExtra("collection", (Serializable)book);
				goIntent.putExtra("from_explore", true);
				goIntent.putExtra("full", true);
				startActivity(goIntent);
				break;
			case 1:
				goIntent = new Intent(ExploreFragment.this.getActivity(), UserPageActivity.class);
				User user = (User)nearbyUsersAdapter.getItem(pos - 1);
				goIntent.putExtra("user", user);
				startActivity(goIntent);
				break;
			case 2:
				DoubanBook dbook = doubanBooks.get(pos - 1);
				goIntent = new Intent(ExploreFragment.this.getActivity(), BookInfoActivity.class);
				goIntent.putExtra("book", (Serializable)dbook);
				startActivity(goIntent);
				break;
			}
		}
		
	};
	
    protected void updateSearchHistory(String searchStr) {
		// TODO Auto-generated method stub
    	if(!arrayKeys.contains(searchStr)){
    		helper.insert(searchStr, "0");
    		arrayKeys.add(searchStr);
    		searchAdapter = new ArrayAdapter<String>(ExploreFragment.this.getActivity(),
                    android.R.layout.select_dialog_item, arrayKeys);
        	searchEdt.setAdapter(searchAdapter);
    	}
	}

	private void initSearchEdtAdapter() {
		// TODO Auto-generated method stub
    	
    	helper = new SearchDBHelper(ExploreFragment.this.getActivity());
    	helper.openDataBase();
    	
    	Cursor cursor = helper.select();
    	
    	arrayKeys = new ArrayList<String>();
    	while(cursor.moveToNext()){
    		arrayKeys.add(cursor.getString(cursor.getColumnIndexOrThrow(SearchDBHelper.KEY_NAME)));
    	}
    	
    	// 定义字符串数组作为提示的文本
	    searchAdapter = new ArrayAdapter<String>(ExploreFragment.this.getActivity(),
                android.R.layout.select_dialog_item, arrayKeys);
	    searchEdt.setAdapter(searchAdapter);
	}

	ArrayList<Map<String,Object>> userArray;
	
//	private void generateNearbyUser() { 
//		// TODO Auto-generated method stub
//		
//		SharedPreferences sp = this.getActivity().getSharedPreferences("config", 0);
//		if(sp.contains("top_user_update")){
//			TopUsersHelper topHelper = new TopUsersHelper(this.getActivity());
//			ArrayList<User> tops = topHelper.getCachedTops();
//			if(tops != null){
//				topAdapter = new NearbyUsersAdapter(this.getActivity(), tops);
//				listview.setAdapter(topAdapter);
//			}
//		}else{
//			this.taskType = HttpListener.FETCH_TOP_USERS;
//			UserUtils.fetchTopUsers(this.getActivity(), (HttpListener)this, User.getInstance().school_id);
//		}
//		
//	}
	
	
	
	/**
	 * fetch nearby Books
	 */
	private void fillNearbyBooks() {
		// TODO Auto-generated method stub
		
		loadingView.setVisibility(View.VISIBLE);
		
		UserBooksHelper cachehelper = UserBooksHelper.getInstance(this.getActivity());
		ArrayList<BookCollection> cacheBooks = cachehelper.getCachedNearbyCollections();
		
		if(cacheBooks == null || cacheBooks.size() == 0){
			fetchNeabyBooks(0, DEFAULT_NUM, User.getInstance().school_id, 2, 0);
		}else{
			nearbyBooks.addAll(cacheBooks);
			nearbyBooksAdapter = new NearbyBooksAdapter(this.getActivity(), nearbyBooks);
			listview.setAdapter(nearbyBooksAdapter);
			loadingView.setViewGone();
			loadNewBooks();
		}
		
	}
	
	private void fetchNeabyBooks(int start, int count, int school_id, int status, int sort) {
		// TODO Auto-generated method stub
		BookUtils.fetchNearby(school_id, start, count, status, sort, 
				new ExploreResponeHandler(getActivity(), HttpListener.FETCH_NEARBY_BOOKS));
	}
	
	private void loadMoreNearbyBooks(int start, int count, 
			int school_id, int status, int sort) {
		// TODO Auto-generated method stub
		BookUtils.fetchNearby(school_id, start, count, status, sort, 
				new ExploreResponeHandler(getActivity(), HttpListener.FETCH_NEARBY_BOOKS_MORE));
	}

	/* update nearby books */
	protected void updateNearbyBooks(String respone) {
		// TODO Auto-generated method stub
		ArrayList<BookCollection> books = BookUtils.parseNearybyBooks(respone);
		
		if(books == null){
			Crouton.makeText(this.getActivity(), R.string.no_more, Style.CONFIRM).show();
			if(nearbyBooks.size() == 0){
				nearbyBooksAdapter = new NearbyBooksAdapter(this.getActivity(), nearbyBooks);
				listview.setAdapter(nearbyBooksAdapter);
			}
		}else{
			
			if(nearbyBooksAdapter == null || nearbyBooks.size() == 0){
				nearbyBooks.addAll(books);
				nearbyBooksAdapter = new NearbyBooksAdapter(this.getActivity(), nearbyBooks);
				listview.setAdapter(nearbyBooksAdapter);
				
				// 只缓存“status=全部”的书籍
				if(school_id == User.getInstance().school_id && statusId == 2){
					UserBooksHelper cachehelper = UserBooksHelper.getInstance(getActivity());
					cachehelper.cacheNearbyCollections(books);
				}
			} else {
				nearbyBooks.addAll(books);
				nearbyBooksAdapter = new NearbyBooksAdapter(this.getActivity(), nearbyBooks);
				nearbyBooksAdapter.notifyDataSetChanged();
			}
		}
		
	}

	private boolean loadingMore = false;
	
	protected void loadMoreItems() {
		// TODO Auto-generated method stub
		
		int startCid = 0;
		loadingMore = true;
		
		switch(currentSection){
		case 0:
			
			if(nearbyBooks != null && nearbyBooks.size() > 0){
				startCid = nearbyBooks.get(nearbyBooks.size() - 1).cid;
			}
			
			morePd.setVisibility(View.VISIBLE);
			loadMoreNearbyBooks(startCid, 
					DEFAULT_NUM, school_id, statusId, sortId);
			break;
		case 1:
			
			if(usortId == 1){
				// 最新用户
				if(nearbyUsers != null && nearbyUsers.size() > 0){
					startCid = nearbyUsers.get(nearbyUsers.size() - 1).uid;
				}
			}else{
				// 藏书最多、随机排序  从
				startCid = nearbyUsers.size();
			}
			
			morePd.setVisibility(View.VISIBLE);
			loadMoreNeabyUsers(startCid, 
					DEFAULT_NUM, school_id, usortId);
			break;
		}
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		if(school_id == 0)
//			school_id = User.getInstance().school_id;
	}
    

	OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			switch(v.getId()){
			case R.id.search_forward:
				if(!searchEdt.getText().toString().equals("")){
					String searchStr = searchEdt.getText().toString();
					if(DoubanBookUtils.isISBNLike(searchStr)){
						if(!DoubanBookUtils.isISBN(searchStr)){
							Crouton.makeText(ExploreFragment.this.getActivity(), R.string.isbn_not_match, Style.ALERT).show();
						}else{
							Intent i = new Intent(ExploreFragment.this.getActivity(), BookInfoActivity.class);
							i.putExtra("isbn", searchStr);
							startActivity(i);
							getActivity().overridePendingTransition(R.anim.shrink_from_bottom,R.anim.exit_fade_out);
						}
					}else{
						updateSearchHistory(searchStr);
						Intent ii = new Intent(ExploreFragment.this.getActivity(), BookSearchListActivity.class);
						ii.putExtra("keyword", searchStr);
						startActivity(ii);
						getActivity().overridePendingTransition(R.anim.shrink_from_bottom,R.anim.exit_fade_out);
					}
					InputMethodManager imm = (InputMethodManager)ExploreFragment.this.getActivity()
							.getSystemService(Context.INPUT_METHOD_SERVICE); 
					imm.hideSoftInputFromWindow(searchEdt.getWindowToken(), 0);
				}else{
					Intent ii = new Intent("com.czzz.action.qrscan");
					startActivityForResult(ii,0);
				}
				break;
			case R.id.home_books_nearby:
				Intent booksNearbyIntent = new Intent(ExploreFragment.this.getActivity(), BookNearbyActivity.class);
				startActivity(booksNearbyIntent);
				break;
//			case R.id.home_library:
//				Intent webViewIntent = new Intent(ExploreFragment.this.getActivity(), WebActivity.class);
//				startActivity(webViewIntent);
//				break;
			case R.id.home_douban_recomm:
				Intent doubanRecommIntent = new Intent(ExploreFragment.this.getActivity(), DoubanRecomActivity.class);
				startActivity(doubanRecommIntent);
				break;
			case R.id.home_nearby_users:
				Intent userNearbyIntent = new Intent(ExploreFragment.this.getActivity(), NearbyUserActivity.class);
				startActivity(userNearbyIntent);
				break;
			case R.id.bottom_menu_section:
				showFilterDialog(DIALOG_SECTION);
				break;
			case R.id.bottom_menu_tab:
				showFilterDialog(DIALOG_SCHOOL);
				break;
			case R.id.bottom_menu_filter:
				showFilterDialog(DIALOG_FILTER);
				break;
			} 
		}
    	
    };
    
    
    private static final int DIALOG_SCHOOL = 0;
    private static final int DIALOG_SECTION = 1;
    private static final int DIALOG_FILTER = 2;
    
    SpinnerPopupDialog dialogSection, dialogFilterBooks;
    UserFilterDialog dialogFilterUsers;
    private int school_id = User.getInstance().school_id; // initial school id
    private SchoolPopupDialog schoolDialog;
    private int statusId = 2, sortId = 0;
    private int usortId = 2;
    
    private ArrayList<BookCollection> nearbyBooks = new ArrayList<BookCollection>();
    private ArrayList<User> nearbyUsers = new ArrayList<User>();
    private ArrayList<DoubanBook> doubanBooks = new ArrayList<DoubanBook>();
	private NearbyBooksAdapter nearbyBooksAdapter;
	private NearbyUsersAdapter nearbyUsersAdapter;
	private DoubanRecommAdapter doubanBooksAdapter;
    
    private int currentSection = 0;
    
    private void showFilterDialog(int dialog_flag){
    	int height = bottomMenu.getHeight();
    	
    	switch(dialog_flag){
    	case DIALOG_SCHOOL:
    		
    		if(schoolDialog == null){
				schoolDialog = new SchoolPopupDialog(
						ExploreFragment.this.getActivity(), R.style.school_popup_style,
						height+1);
			}
			schoolDialog.setOnDismissListener(new OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface dialogInterface) {
					// TODO Auto-generated method stub
					String school = schoolDialog.updateUniversity;
					int sid = schoolDialog.updateUnivId;
					if(schoolDialog.changed){
						school_id = sid;
						if(school_id == User.getInstance().school_id){
//							schoolBtn.setText("我的学校");
						}else{
//							schoolBtn.setText(school);
						}
						schoolDialog.changed = false;
						loadingView.setVisibility(View.VISIBLE);
						footerView.setVisibility(View.GONE);
						
						if(currentSection == 0){
//							nearbyBooks.clear();
							fetchNeabyBooks(0, DEFAULT_NUM, school_id, statusId, sortId);
						}else if(currentSection == 1){
//							nearbyUsers.clear();
							fetchNeabyUsers(0, DEFAULT_NUM, school_id, usortId);
						}
					}
				}
				
			});
			schoolDialog.show();
    		
    		break;
    	case DIALOG_SECTION:
    		
    		dialogSection = new SpinnerPopupDialog(ExploreFragment.this.getActivity(), 
					R.style.spinner_popup_style, 
					height, bottomSection.getWidth(), 2);
    		dialogSection.show();
    		dialogSection.setOnDismissListener(new OnDismissListener(){

				@Override
				public void onDismiss(DialogInterface arg0) {
					// TODO Auto-generated method stub
					if(dialogSection.selectId != -1){
						currentSection = dialogSection.selectId;
						switchCurrentSection();
					}
				}
				
			});
    		
    		break;
    	case DIALOG_FILTER:
    		if(currentSection == 0){
    			
    			// filter for books
    			if(dialogFilterBooks == null)
    				dialogFilterBooks = new SpinnerPopupDialog(ExploreFragment.this.getActivity(), 
    					R.style.spinner_popup_right_style, 
    					height, 0, 0);
        		
        		dialogFilterBooks.setOnDismissListener(new OnDismissListener(){

    				@Override
    				public void onDismiss(DialogInterface arg0) {
    					// TODO Auto-generated method stub
    					if(dialogFilterBooks.filterChanged){
    						statusId = dialogFilterBooks.status;
    						sortId = dialogFilterBooks.sort;
    						if(sortId == 0) 
    							listview.setPulldownPermit(true);
    						else
    							listview.setPulldownPermit(false);
    						dialogFilterBooks.filterChanged = false;
//    						nearbyBooks.clear();
    						loadingView.setVisibility(View.VISIBLE);
    						footerView.setVisibility(View.GONE);
    						
    						fetchNeabyBooks(0, DEFAULT_NUM, school_id, statusId, sortId);
    					}
    				}
        			
        		});
        		
        		dialogFilterBooks.show();
        		
    		}else if(currentSection == 1){
    			
    			// filter for user
    			
    			if(dialogFilterUsers == null)
    				dialogFilterUsers = new UserFilterDialog(ExploreFragment.this.getActivity(), 
    					R.style.spinner_popup_right_style, 
    					height);
        		
    			dialogFilterUsers.setOnDismissListener(new OnDismissListener(){

    				@Override
    				public void onDismiss(DialogInterface arg0) {
    					// TODO Auto-generated method stub
    					if(dialogFilterUsers.filterChanged){
    						usortId = dialogFilterUsers.sort;
    						dialogFilterUsers.filterChanged = false;
//    						nearbyUsers.clear();
    						footerView.setVisibility(View.GONE);
    						loadingView.setVisibility(View.VISIBLE);
    						fetchNeabyUsers(0, DEFAULT_NUM, school_id, usortId);
    					}
    				}
        			
        		});
        		
    			dialogFilterUsers.show();
    		}
    		break;
    	}
    }
    
    
	private void switchCurrentSection() {
		// TODO Auto-generated method stub
		switch(currentSection){
		case 0:
			bottomSection.setText(R.string.nearby_books_label);
			bottomSchool.setEnabled(true);
			bottomFilter.setEnabled(true);
			if(sortId == 0) 
				listview.setPulldownPermit(true);
			else
				listview.setPulldownPermit(false);
			
			if(nearbyBooksAdapter == null){
				nearbyBooks.clear();
				footerView.setVisibility(View.GONE);
				loadingView.setVisibility(View.VISIBLE);
				fetchNeabyBooks(0, DEFAULT_NUM, school_id, statusId, sortId);
			}else{
				listview.setAdapter(nearbyBooksAdapter);
				footerView.setVisibility(View.VISIBLE);
			}
			break;
		case 1:
			bottomSection.setText(R.string.nearby_users_label);
			bottomSchool.setEnabled(true);
			bottomFilter.setEnabled(true);
			listview.setPulldownPermit(false);
			if(nearbyUsersAdapter == null){
				nearbyUsers.clear();
				footerView.setVisibility(View.GONE);
				loadingView.setVisibility(View.VISIBLE);
				listview.setAdapter(nearbyUsersAdapter);
				fetchNeabyUsers(0, DEFAULT_NUM, school_id, usortId);
			}else{
				listview.setAdapter(nearbyUsersAdapter);
				footerView.setVisibility(View.VISIBLE);
			}
			break;
		case 2:
			bottomSection.setText(R.string.douban_recomm_label);
			bottomSchool.setEnabled(false);
			bottomFilter.setEnabled(false);
			listview.setPulldownPermit(false);
			footerView.setVisibility(View.GONE);
			if(doubanBooksAdapter == null){
				doubanBooks.clear();
				doubanBooksAdapter = new DoubanRecommAdapter(this.getActivity(), doubanBooks);
				listview.setAdapter(doubanBooksAdapter);
				loadingView.setVisibility(View.VISIBLE);
				fillDoubanRecommBooks();
			}else{
				listview.setAdapter(doubanBooksAdapter);
			}
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == Activity.RESULT_OK){
			String codeFormat = data.getStringExtra("bracode_format");
			String codeText = data.getStringExtra("bracode_text");
			
			if(codeFormat.contains("EAN")){
				searchEdt.setText(codeText);
				Intent i = new Intent(ExploreFragment.this.getActivity(), BookInfoActivity.class);
				i.putExtra("isbn", codeText);
				startActivity(i);
			}else{
				Crouton.makeText(ExploreFragment.this.getActivity(), "invalid isbn !", Style.ALERT).show();
			}
			
		}
		
	}
	
	/**
	 * 获取同校书友
	 * @param start
	 * @param count
	 * @param school_id 学校id
	 * @param sort 书友排序方式
	 */
    private void fetchNeabyUsers(int start, int count, int school_id, int usort) {
		// TODO Auto-generated method stub
		UserUtils.fetchNearbyUsers(start, count, school_id, usort, 
				new ExploreResponeHandler(getActivity(), HttpListener.FETCH_NEARBY_USERS));
	}
    
    private void loadMoreNeabyUsers(int start, int count, int school_id, int usort) {
		// TODO Auto-generated method stub
		UserUtils.fetchNearbyUsers(start, count, school_id, usort, 
				new ExploreResponeHandler(getActivity(), HttpListener.FETCH_NEARBY_USERS_MORE));
	}
    
    
	/**
	 * fetch douban recommand books
	 */
	private void fillDoubanRecommBooks() {
		// TODO Auto-generated method stub
		
		loadingView.setVisibility(View.VISIBLE);
//		pd.show();
		
		SearchResultDBHelper helper = SearchResultDBHelper.getInstance(this.getActivity());
		helper.openDataBase();
		
		Cursor cursor = helper.selectByKey(BookUtils.initCurrentDateStr());
		if (cursor.getCount() > 0) {
			while(cursor.moveToNext()){
				DoubanBook item = new DoubanBook().init(cursor);
				doubanBooks.add(item);
			}
		} 
		
		doubanBooksAdapter = new DoubanRecommAdapter(this.getActivity(), doubanBooks);
		listview.setAdapter(doubanBooksAdapter);
		
		if(doubanBooks.size() == 0){
			fetchDoubanRecomm();
		}else{
			loadingView.setViewGone();
//			pd.dismiss();
		}
	}
	
	private void fetchDoubanRecomm(){
		String key_date = BookUtils.initCurrentDateStr();
		BookUtils.fetchDoubanRecomm(key_date, 
				new ExploreResponeHandler(getActivity(), HttpListener.FETCH_DOUBAN_RECOMM));
	}
	

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		helper.getDB().close();
		this.getActivity().unregisterReceiver(receiver);
		super.onDestroy();
	}

//	@Override
//	public synchronized void  onTaskCompleted(Object data) {
//		// TODO Auto-generated method stub
//		String recvData = TextUtils.unicodeToString("" + data);
//		switch(taskType){
//		case HttpListener.FETCH_TOP_USERS:
//			Log.d("DEBUG", "" + recvData);
//			ArrayList<User> tops = UserUtils.parseNeabyUsers("" + recvData);
//			
//			if(tops == null){
//				Toast.makeText(this.getActivity(), "没有了..", Toast.LENGTH_SHORT).show();
//				return;
//			}
//			
//			if(topAdapter == null){
//				topAdapter = new NearbyUsersAdapter(this.getActivity(), tops);
//				listview.setAdapter(topAdapter);
//				
//			} else {
//				topAdapter = new NearbyUsersAdapter(this.getActivity(), tops);
//				topAdapter.notifyDataSetChanged();  
//			}
//			
//			TopUsersHelper topHelper = new TopUsersHelper(this.getActivity());
//			topHelper.cacheTopUsers(tops);
//			
//			break;
//		case HttpListener.FETCH_NEARBY_BOOKS:
//			ArrayList<BookCollection> books = BookUtils.parseNearybyBooks(recvData);
//			
//			if(books == null){
//				Toast.makeText(this.getActivity(), "没有了..", Toast.LENGTH_SHORT).show();
//				
//				if(nearbyBooks.size() == 0){
//					nearbyBooksAdapter = new NearbyBooksAdapter(this.getActivity(), nearbyBooks);
//					listview.setAdapter(nearbyBooksAdapter);
//				}
//			}else{
//				
//				if(nearbyBooksAdapter == null || nearbyBooks.size() == 0){
//					nearbyBooks.addAll(books);
//					nearbyBooksAdapter = new NearbyBooksAdapter(this.getActivity(), nearbyBooks);
//					listview.setAdapter(nearbyBooksAdapter);
//					
//					if(school_id == User.getInstance().school_id){
//						UserBooksHelper cachehelper = UserBooksHelper.getInstance(getActivity());
//						cachehelper.cacheNearbyCollections(books);
//					}
//				} else {
//					nearbyBooks.addAll(books);
//					nearbyBooksAdapter = new NearbyBooksAdapter(this.getActivity(), nearbyBooks);
//					nearbyBooksAdapter.notifyDataSetChanged();
//				}
//			}
//			
//			break;
//		case HttpListener.FETCH_NEARBY_USERS:
//			ArrayList<User> users = UserUtils.parseNeabyUsers("" + recvData);
//			
//			if(users == null){
//				Toast.makeText(this.getActivity(), "没有了..", Toast.LENGTH_SHORT).show();
//				if(nearbyUsers.size() == 0){
//					nearbyUsersAdapter = new NearbyUsersAdapter(this.getActivity(), nearbyUsers);
//					listview.setAdapter(nearbyUsersAdapter);
//				}
//			}else{
//				if(nearbyUsersAdapter == null || nearbyUsers.size() == 0){
//					nearbyUsers.addAll(users);
//					nearbyUsersAdapter = new NearbyUsersAdapter(this.getActivity(), nearbyUsers);
//					listview.setAdapter(nearbyUsersAdapter);
//				} else {
//					morePd.setVisibility(View.GONE);
//					nearbyUsers.addAll(users);
//					nearbyUsersAdapter = new NearbyUsersAdapter(this.getActivity(), nearbyUsers);
//					nearbyUsersAdapter.notifyDataSetChanged();  
//				}
//				users.clear();
//			}
//			break;
//		case HttpListener.FETCH_DOUBAN_RECOMM:
//			doubanBooks = BookUtils.parseDoubanRecomms(getActivity(), recvData);
//			doubanBooksAdapter = new DoubanRecommAdapter(this.getActivity(), doubanBooks);
//			listview.setAdapter(doubanBooksAdapter);
//			break;
//		case HttpListener.FETCH_NEARBY_BOOKS_NEW:
//			Log.d("DEBUG", "" + recvData);
//			ArrayList<BookCollection> newbooks = BookUtils.parseNearybyBooks(recvData);
//			if(newbooks != null){
//				nearbyBooks.addAll(0, newbooks);
//				nearbyBooksAdapter = new NearbyBooksAdapter(this.getActivity(), nearbyBooks);
//				
//				if(school_id == User.getInstance().school_id){
//					UserBooksHelper cachehelper = UserBooksHelper.getInstance(getActivity());
//					cachehelper.cacheNearbyCollections(newbooks);
//				}
//				
//				if(listview.mIsRefreshing){
//					nearbyBooksAdapter.notifyDataSetChanged();
//					listview.completeRefreshing(true);
//				}else{
//					listview.setAdapter(nearbyBooksAdapter);
//				}
//				
//			}else{
//				if(listview.mIsRefreshing) listview.completeRefreshingFail(false);
//			}
//			break;
//		}
//		
//		if(currentSection != 2) footerView.setVisibility(View.VISIBLE);
//		morePd.setVisibility(View.GONE);
//		loadingMore = false;
//		loadingView.setViewGone();
//		taskType = -1;
//	}
	
	
//	@Override
//	public void onTaskFailed(String data) {
//		// TODO Auto-generated method stub
//		Toast.makeText(getActivity(), data, Toast.LENGTH_SHORT).show();
//		listview.completeRefreshing(false);
//		loadingView.setViewGone();
//		morePd.setVisibility(View.GONE);
//		taskType = -1;
//	}

	@Override
	public void onRefresh(RefreshListView listView) {
		// TODO Auto-generated method stub
		loadNewBooks();
	}
	
	protected void loadNewBooks() {
		// TODO Auto-generated method stub
		switch(currentSection){
		case 0:
			morePd.setVisibility(View.VISIBLE);
			fetchNeabyNewBooks(nearbyBooks.get(0).cid, statusId);
			break;
		}
	}
	
	private void updateNearbyUsers(String response){
		ArrayList<User> users = UserUtils.parseNeabyUsers("" + response);
		
		if(users == null){
			Crouton.makeText(this.getActivity(), R.string.no_more, Style.ALERT).show();
			if(nearbyUsers.size() == 0){
				nearbyUsersAdapter = new NearbyUsersAdapter(this.getActivity(), nearbyUsers);
				listview.setAdapter(nearbyUsersAdapter);
			}
		}else{
			if(nearbyUsersAdapter == null || nearbyUsers.size() == 0){
				nearbyUsers.addAll(users);
				nearbyUsersAdapter = new NearbyUsersAdapter(this.getActivity(), nearbyUsers);
				listview.setAdapter(nearbyUsersAdapter);
			} else {
				morePd.setVisibility(View.GONE);
				nearbyUsers.addAll(users);
				nearbyUsersAdapter = new NearbyUsersAdapter(this.getActivity(), nearbyUsers);
				nearbyUsersAdapter.notifyDataSetChanged();  
			}
			users.clear();
		}
	}
	
	private void refreshNewNearbyBooks(String respone){
		Log.d("DEBUG", "" + respone);
		ArrayList<BookCollection> newbooks = BookUtils.parseNearybyBooks(respone);
		if(newbooks != null){
			nearbyBooks.addAll(0, newbooks);
			nearbyBooksAdapter = new NearbyBooksAdapter(this.getActivity(), nearbyBooks);
			
			if(school_id == User.getInstance().school_id && statusId == 2){
				UserBooksHelper cachehelper = UserBooksHelper.getInstance(getActivity());
				cachehelper.cacheNearbyCollections(newbooks);
			}
			
			if(listview.mIsRefreshing){
				if(newbooks.size() > 0) 
					Crouton.makeText(this.getActivity(), "加载了" + newbooks.size() + "本新藏书", Style.CONFIRM).show();
				nearbyBooksAdapter.notifyDataSetChanged();
				listview.completeRefreshing(true);
			}else{
				listview.setAdapter(nearbyBooksAdapter);
			}
			
		}else{
			if(listview.mIsRefreshing) listview.completeRefreshingFail(false);
		}
	}
	
	private void fetchNeabyNewBooks(int start, int status) {
		// TODO Auto-generated method stub
		BookUtils.fetchNearbyNewBooks(school_id, start, status, 
				new ExploreResponeHandler(getActivity(), HttpListener.FETCH_NEARBY_BOOKS_NEW));
	}
	
	private static final String ACTION_DELETE_ITEM = "delete_user_book_item";
	private UpdateReceiver receiver; 
	
	private void registerReceiver(){
		receiver = new UpdateReceiver();
        IntentFilter filter=new IntentFilter();  
        filter.addAction(ACTION_DELETE_ITEM);

        //动态注册BroadcastReceiver  
        this.getActivity().registerReceiver(receiver, filter); 
	}
	
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
		}
		
	}
	
	/**
	 * handle http response
	 * @author tinyao
	 *
	 */
	private class ExploreResponeHandler extends CustomAsyncHttpResponseHandler{

		public ExploreResponeHandler(Activity activity, int taskId) {
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
			case HttpListener.FETCH_NEARBY_BOOKS_MORE:
				updateNearbyBooks(response);
				break;
			case HttpListener.FETCH_NEARBY_USERS:
				nearbyUsers.clear();
				updateNearbyUsers(response);
				break;
			case HttpListener.FETCH_NEARBY_USERS_MORE:
				updateNearbyUsers(response);
				break;
			case HttpListener.FETCH_NEARBY_BOOKS_NEW:
				refreshNewNearbyBooks(response);
				break;
			case HttpListener.FETCH_DOUBAN_RECOMM:
				doubanBooks = BookUtils.parseDoubanRecomms(context, response);
				if(doubanBooks == null) {
					Crouton.makeText(ExploreFragment.this.getActivity(), "服务器故障 =.=|", Style.ALERT).show();
					break;
				}
				doubanBooksAdapter = new DoubanRecommAdapter(context, doubanBooks);
				listview.setAdapter(doubanBooksAdapter);
				break;
			}
		}
		
		@Override
		public void onFailure(Throwable arg0, String arg1) {
			// TODO Auto-generated method stub
			super.onFailure(arg0, arg1);
			bottomMenu.setVisibility(View.VISIBLE);
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			super.onFinish();
			if(currentSection != 2) footerView.setVisibility(View.VISIBLE);
			loadingView.setViewGone();
			if(listview.mIsRefreshing) listview.completeRefreshing(false);
			morePd.setVisibility(View.GONE);
			loadingMore = false;
		}

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			super.onStart();
			switch(taskId){
			case HttpListener.FETCH_NEARBY_BOOKS:
				loadingView.setVisibility(View.VISIBLE);
				break;
			case HttpListener.FETCH_NEARBY_USERS:
				loadingView.setVisibility(View.VISIBLE);
				break;
			}
		}
		
	};
}