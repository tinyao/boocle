package com.czzz.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.bookcircle.UserUtils;
import com.czzz.data.UserBooksHelper;
import com.czzz.demo.BookShelfFragment.RefreshReceiver;
import com.czzz.demo.listadapter.NearbyUsersAdapter;
import com.czzz.demo.listadapter.ShelfAdapter;
import com.czzz.demo.listadapter.ShelfListAdapter;
import com.czzz.douban.DoubanBookUtils;
import com.czzz.utils.HttpListener;
import com.czzz.utils.ImageUtils;
import com.czzz.utils.ImagesDownloader;
import com.czzz.utils.TextUtils;
import com.czzz.view.LoadingFooter;
import com.czzz.view.LoadingView;
import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class UserPageActivity extends AsyncTaskActivity implements PullToRefreshAttacher.OnRefreshListener{

	private final static int SECTION_BOOK_GRID = 0;
	private final static int SECTION_BOOK_LIST = 1;
	private final static int SECTION_USER_FOLLOW = 2;
	
	private static final int DEFAULT_NUM = 9;
	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private TextView userName, userDesc, userAvatarTxt;
	private ImageView userAvatar;
	private CheckBox genderView;
	private TextView bookTotal, favTotal;
	private ListView listView;
	private LoadingView loadingView;
	private LoadingFooter mLoadingFooter;

	private User curUser;
	private ArrayList<BookCollection> all = new ArrayList<BookCollection>();
	private ArrayList<User> followings = new ArrayList<User>();
	
	private ShelfListAdapter listAdapter;
	private ShelfAdapter gridAdapter;
	private NearbyUsersAdapter followingAdapter;
	private int currentSection = SECTION_BOOK_GRID;
	
	private boolean isMyself = false;
	
	private ImagesDownloader imagesLoader = ImagesDownloader.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initLayoutActionBar();
		
		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

        mPullToRefreshAttacher.addRefreshableView(listView, this);
        
		// 获取本UserPage的User
		Intent i = getIntent();
		if (i.hasExtra("user"))
			curUser = (User) i.getSerializableExtra("user");
		else
			curUser = User.getInstance();
		
		isMyself = curUser.uid == User.getInstance().uid;
		curUser.collections = all;

		if (isMyself) {
			initProfileView();
		} else {
			// 如果存在于缓存中，则直接使用
			// 如果不存在于缓存中，则创建添加到缓存
			if (User.getTaInstance(curUser.uid) == null) {
				// 不存在
				// 未暂时缓存用户信息
				User.addTaInstance(curUser);
				if (i.hasExtra("from_book_detail")) {
					initUserView(false, false); // 不包含个人信息
				} else {
					initUserView(true, false); // 包含个人信息
				}
			} else {
				// 存在
				curUser = User.getTaInstance(curUser.uid);
				initUserView(true, true); // 包含个人信息以及书籍
			}
		}
		
		registerReceiver(); 
		
	}
	
	private void initProfileView() {
		// TODO Auto-generated method stub
		mActionBar.setTitle("我的主页");
		
		userName.setText(curUser.name);
		userDesc.setText(curUser.desc);
		bookTotal.setText("" + curUser.book_total);
		favTotal.setText("" + curUser.fav_total);
		genderView.setChecked(curUser.gender==1);
		genderView.setVisibility(View.VISIBLE);
		if(curUser.avatar.equals("")) {
			userAvatarTxt.setText(curUser.name.substring(0, 1).toUpperCase(Locale.CHINA));
		}
		setUserAvatar();
		
		gridAdapter = new ShelfAdapter(this, all);
		listAdapter = new ShelfListAdapter(this, all);
		listView.setAdapter(gridAdapter);
		
		// fetch the books
		fetchUserBooks(User.getInstance().uid, 0, 12);
	}
	
	private void setUserAvatar() {
		if (!curUser.avatar.equals("")){
			new ImagesDownloader(ImagesDownloader.AVATAR_TASK).download(curUser.avatar, userAvatar);
		} else {
			// 如果头像未设置，使用默认头像
		}
	}

	private void initLayoutActionBar() {
		// TODO Auto-generated method stub
		final FadingActionBarHelper helper = new FadingActionBarHelper()
				.actionBarBackground(R.drawable.actionbar_base)
				.headerLayout(R.layout.header_userpage_bg)
				.headerPanelLayout(R.layout.user_page_header_fading)
				.contentLayout(R.layout.activity_user_page);
		setContentView(helper.createView(this));
		helper.initActionBar(this);

		ActionBar mActionBar = this.getSupportActionBar();
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(true);

		View headerLayout = helper.getCustomHeader();
		userAvatar = (ImageView) headerLayout.findViewById(R.id.avatar_img);
		userAvatarTxt = (TextView) headerLayout
				.findViewById(R.id.avatar_img_txt);
		userName = (TextView) headerLayout.findViewById(R.id.user_profile_btn);
		userDesc = (TextView) headerLayout.findViewById(R.id.user_desc);
		genderView = (CheckBox) headerLayout.findViewById(R.id.user_gender_img);
		bookTotal = (TextView) headerLayout.findViewById(R.id.user_book_total);
		favTotal = (TextView) headerLayout.findViewById(R.id.user_fav_total);
		// followTa = (ToggleButton)
		// headerLayout.findViewById(R.id.user_follow_ta);
		RadioGroup shelfRadioSwitch = (RadioGroup) headerLayout
				.findViewById(R.id.shelf_radio_group);

		listView = (ListView) findViewById(android.R.id.list);
		loadingView = (LoadingView) findViewById(R.id.loading_view);
		mLoadingFooter = new LoadingFooter(this);
	    listView.addFooterView(mLoadingFooter.getView());
	    
	    userName.setOnClickListener(listener);
	    userAvatar.setOnClickListener(listener);

		shelfRadioSwitch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup arg0, int selectedId) {
						// TODO Auto-generated method stub
						
						switch (selectedId) {
						case R.id.shelf_switch_grid:
							currentSection = SECTION_BOOK_GRID;
							listView.setAdapter(gridAdapter);
							mLoadingFooter.setState(
									bookEnd ? LoadingFooter.State.TheEnd : LoadingFooter.State.Idle);
							break;
						case R.id.shelf_switch_list:
							currentSection = SECTION_BOOK_LIST;
							listView.setAdapter(listAdapter);
							mLoadingFooter.setState(
									bookEnd ? LoadingFooter.State.TheEnd : LoadingFooter.State.Idle);
							break;
						case R.id.shelf_switch_following:
							// fetch the users following
							currentSection = SECTION_USER_FOLLOW;
							if (followingAdapter == null) {
								loadingView.setVisibility(View.VISIBLE);
								followingAdapter = new NearbyUsersAdapter(UserPageActivity.this, 
										followings);
								listView.setAdapter(followingAdapter);
								fetchFollowingUsers(curUser.uid, 0);
							} else{
								listView.setAdapter(followingAdapter);
							}
							mLoadingFooter.setState(
									followEnd ? LoadingFooter.State.TheEnd : LoadingFooter.State.Idle);
							break;
						}
					}
				});

		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				helper.onListSrcoll(view);
				
				if (mLoadingFooter.getState() == LoadingFooter.State.Loading
                        || mLoadingFooter.getState() == LoadingFooter.State.TheEnd) {
                    return;
                }
                if (firstVisibleItem + visibleItemCount >= totalItemCount
                        && totalItemCount != 0
                        && totalItemCount != listView.getHeaderViewsCount()
                                + listView.getFooterViewsCount() ) {
                    
                	if(currentSection != SECTION_USER_FOLLOW && all.size() > 0){	//书
                		loadNextBookPage();
                	}
                	
                	if(currentSection == SECTION_USER_FOLLOW && followings.size() >0){	//关注
                		
                	}
                }
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				if(pos > listAdapter.getCount()) return;
				
				if(currentSection == SECTION_BOOK_LIST){
					Intent bookIntent = new Intent(UserPageActivity.this,
							BookInfoActivity.class);
					bookIntent.putExtra("collection", (Serializable)all.get(pos-1));
					bookIntent.putExtra("full", true);	// 图书信息完整
					startActivity(bookIntent);
				}else if(currentSection == SECTION_USER_FOLLOW){
					Intent toUserPageIntent = new Intent(UserPageActivity.this, UserPageActivity.class);
					toUserPageIntent.putExtra("user", followings.get(pos-1));
					toUserPageIntent.putExtra("from_book_detail", true);
					startActivity(toUserPageIntent);
				}
				
			}

			
		});
	}
	
	OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.avatar_img:
			case R.id.user_profile_btn:
				Intent detailIntent = new Intent(UserPageActivity.this,
						ProfileActivity.class);
				startActivityForResult(detailIntent, 0);
				break;
			}
		}

	};
	
	private void loadNextBookPage(){
		mLoadingFooter.setState(LoadingFooter.State.Loading);
		this.taskType = HttpListener.MORE_USER_BOOKS;
		DoubanBookUtils.fetchUserCollection(this, this, taskType, curUser.uid, 
				all.size(), 9 + (3 - all.size()%3) % 3);
	}
	
//	private void loadNextFollowPage(){
//		int startCid = 0;
//		if(nearbyBooks != null && nearbyBooks.size() > 0){
//			startCid = nearbyBooks.get(nearbyBooks.size() - 1).cid;
//		}
//		
//		mLoadingFooter.setState(LoadingFooter.State.Loading);
//		loadMoreNearbyBooks(startCid, 
//				DEFAULT_NUM, school_id, statusId, sortId);
//	}

	private void initUserView(boolean hasFullInfo, boolean hasCollections) {
		// TODO Auto-generated method stub
		userName.setText(curUser.name);

		imagesLoader.download(curUser.avatar, userAvatar, -1);

		if (hasFullInfo) {
			Log.d("DEBUG", "has full info...");
			userDesc.setText(curUser.desc);
			if(curUser.avatar.equals("")) {
				userAvatarTxt.setText(curUser.name.substring(0, 1).toUpperCase(Locale.CHINA));
			}
			bookTotal.setText("" + curUser.book_total);
			favTotal.setText("" + curUser.fav_total);
			genderView.setChecked(curUser.gender == 1);
			genderView.setVisibility(View.VISIBLE);
			// 获取书籍信息
			if (hasCollections && curUser.collections.size() != 0) {
				// 检测是否已经缓存
				fillBookShelf(curUser.collections);
			} else {
				fetchUserBooks(curUser.uid, 0, 12);
			}
		} else {
			// 无完整信息和书籍信息，先获取个人信息后获取书籍信息
			Log.d("DEBUG", "fetch uid: " + curUser.uid);
			fetchUserInfo(Integer.valueOf(curUser.uid));
		}

		listView.setAdapter(gridAdapter);
	}
	
	/**
	 * 
	 * @param cs 最新获取到的书籍
	 */
	private void fillBookShelf(ArrayList<BookCollection> cs) {
		
		if (cs == null) {
			if (all.size() != 0)
				Crouton.makeText(this, R.string.no_more, Style.CONFIRM).show();
			else{
				Crouton.makeText(this, 
						isMyself ? R.string.you_have_no_books : R.string.ta_has_no_books, 
								Style.CONFIRM).show();
				loadingView.setViewGone();
			}
			return;
		}

		if(mPullToRefreshAttacher.isRefreshing()) {
			all.clear();
		}
		
		all.addAll(cs);

		if(mPullToRefreshAttacher.isRefreshing()){
			notifyDataList();
		} else {
			createAdapter(all);
		}
		
		if(isMyself){	//如果是自己的话，缓存
			if(collectionsHelper == null) collectionsHelper = new UserBooksHelper(this);
			collectionsHelper.cacheUserCollections(cs);
		}
		
//		if (gridAdapter == null) {	// 第一次加载
//			
//		} else {
//			listAdapter = new ShelfListAdapter(this, all);
//			gridAdapter = new ShelfAdapter(this, all);
//			listAdapter.notifyDataSetChanged();
//			gridAdapter.notifyDataSetChanged();
//		}

		loadingView.setViewGone();
		if(mPullToRefreshAttacher.isRefreshing()) {
			mPullToRefreshAttacher.setRefreshComplete();
		}
	}
	
	private void notifyDataList(){
		if(currentSection == SECTION_BOOK_GRID){
			gridAdapter.notifyDataSetChanged();
		}else if(currentSection == SECTION_BOOK_LIST){
			listAdapter.notifyDataSetChanged();
		}else if(currentSection == SECTION_USER_FOLLOW){
			followingAdapter.notifyDataSetChanged();
		}
	}
	
	private void createAdapter(ArrayList<BookCollection> lis) {
		// TODO Auto-generated method stub
		gridAdapter = new ShelfAdapter(this, lis);
		listAdapter = new ShelfListAdapter(this, lis);
		if(currentSection == 0){
			listView.setAdapter(gridAdapter);
		}else if(currentSection == 1){
			listView.setAdapter(listAdapter);
		}
	}
	
	private void fetchUserInfo(int uid) {
		// TODO Auto-generated method stub
		loadingView.setMessage("获取书友信息...");
//		if (!refresh)
//			loadingView.setVisibility(View.VISIBLE);
		this.taskType = HttpListener.FETCH_CIRCLE_USER_INFO;
		UserUtils.fetchUserInfo(this, (HttpListener) this, uid);
	}
	
	protected void fetchUserBooks(int uid, int start, int count) {
		// TODO Auto-generated method stub
		
		if(isMyself){
			if(collectionsHelper == null) {
				collectionsHelper = new UserBooksHelper(this);
				collectionsHelper.openDataBase();
			}
			ArrayList<BookCollection> bc = null;
			if(all.size() == 0){	// 首次加载时，检测本地缓存
				bc = collectionsHelper.getCachedCollections();
			}
			if(bc != null){
				fillBookShelf(bc);	// 有缓存直接显示
			} else{		// 无缓存，网络获取
				this.taskType = HttpListener.FETCH_USER_BOOKS;
				DoubanBookUtils.fetchUserCollection(this, (HttpListener) this,
						taskType, uid, start, count);
			}
		} else {
			if (all.size() == 0)
				loadingView.setVisibility(View.VISIBLE);

			this.taskType = HttpListener.FETCH_USER_BOOKS;
			DoubanBookUtils.fetchUserCollection(this, (HttpListener) this,
					taskType, uid, start, count);
		}
		
	}
	
	protected void fetchFollowingUsers(int uid, int start) {
		// TODO Auto-generated method stub
		this.taskType = HttpListener.FETCH_USER_FOLLOWING;
		UserUtils.fetchFollowing(this, this, uid, start);
	}
	
	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		String recvData = TextUtils.unicodeToString("" + data);
		Log.d("DEBUG", "data: " + recvData);
		switch (taskType) {
		case HttpListener.FETCH_USER_BOOKS:
		case HttpListener.UPDATE_USER_BOOKS:
			try {
				ArrayList<BookCollection> cs = DoubanBookUtils.parseUserBooks("" + data, curUser);
				fillBookShelf(cs);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mLoadingFooter.setState(LoadingFooter.State.Idle);
			break;
		case HttpListener.FETCH_CIRCLE_USER_INFO:
			curUser = UserUtils.parseUser(recvData);
			loadingView.setViewGone();

			if (curUser == null) {
				Crouton.makeText(this, "获取失败", Style.ALERT).show();
				return;
			}

			// 将新对象加入到缓存列表中
			curUser.collections = all;
			User.setTaUser(curUser);

			userDesc.setText(curUser.desc);
			bookTotal.setText("" + curUser.book_total);
			favTotal.setText("" + curUser.fav_total);
			genderView.setChecked(curUser.gender == 1);
			genderView.setVisibility(View.VISIBLE);
			userName.setText(curUser.name);
			
			if(curUser.avatar.equals("")) {
				userAvatarTxt.setText(curUser.name.substring(0, 1).toUpperCase(Locale.CHINA));
			}
			
			imagesLoader.download(curUser.avatar, userAvatar, -1);

			fetchUserBooks(curUser.uid, 0, 12);
			break;
		case HttpListener.FETCH_USER_FOLLOWING:
			Log.d("DEBUG", "following: " + data);
			ArrayList<User> fusers = UserUtils.parseNeabyUsers("" + data);
			
			if(fusers == null) {
				if(followings.size() == 0){
					Crouton.makeText(this, "没有关注的人", Style.ALERT).show();
				}else{
					Crouton.makeText(this, R.string.no_more, Style.ALERT).show();
				}
				loadingView.setViewGone();
				return;
			}
			
			followings.addAll(fusers);
			followingAdapter.notifyDataSetChanged();
			mPullToRefreshAttacher.setRefreshComplete();
			loadingView.setViewGone();
			
			mLoadingFooter.setState(LoadingFooter.State.Idle);
			break;
		case HttpListener.REFRESH_USER_INFO:
			if(isMyself) {
				UserUtils.parseMyself(TextUtils.unicodeToString(""+ data));
			} else{
				UserUtils.parseUser(recvData);
			}
			
			updateUserBooks(curUser.uid, 0, 12); 
			
			userDesc.setText(curUser.desc);
			bookTotal.setText("" + curUser.book_total);
			favTotal.setText("" + curUser.fav_total);
			userName.setText(curUser.name);
			genderView.setChecked(curUser.gender==1);
			genderView.setVisibility(View.VISIBLE);
			if(curUser.avatar.equals("")) {
				userAvatarTxt.setText(curUser.name.substring(0, 1).toUpperCase(Locale.CHINA));
			}
			new ImagesDownloader(ImagesDownloader.AVATAR_TASK).download(curUser.avatar, userAvatar);

			// update the info to database
			if(isMyself) 
				User.getInstance().save(this);
			
			break;
		case HttpListener.MORE_USER_BOOKS:
			try {
				ArrayList<BookCollection> cs = DoubanBookUtils.parseUserBooks("" + data, curUser);
				if(cs==null || cs.size()==0){
					mLoadingFooter.setState(LoadingFooter.State.TheEnd, 0);
					bookEnd = true;
				} else {
					all.addAll(cs);
					notifyDataList();
					if(cs.size()<9){
						mLoadingFooter.setState(LoadingFooter.State.TheEnd);
						bookEnd = true;
					}else{
						mLoadingFooter.setState(LoadingFooter.State.Idle);
						bookEnd = false;
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
	}

	private boolean bookEnd = false;
	private boolean followEnd = false;
	
	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Crouton.makeText(this, data, Style.ALERT).show();
		loadingView.setViewGone();
		mPullToRefreshAttacher.setRefreshComplete();
	}
	
	MenuItem followMenu;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if (curUser.uid != User.getInstance().uid) {
			getSupportMenuInflater().inflate(R.menu.menu_user_page, menu);
			followMenu = menu.findItem(R.id.menu__fragment_follow_star);
		} else{
			getSupportMenuInflater().inflate(R.menu.menu_book_shelf, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_user_msg:
			// 私信
			Intent toMsgIntent = new Intent(this, ConversationActivity.class);
			toMsgIntent.putExtra("thread_uid", curUser.uid);
			toMsgIntent.putExtra("thread_name", curUser.name);
			toMsgIntent.putExtra("thread_avatar", curUser.avatar);
			startActivity(toMsgIntent);
			break;
		case R.id.menu__fragment_follow_star:
//			followUser();
			break;
		case R.id.menu__fragment_add_book:
			showBookAddDialog();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void updateUserInfo(int uid) {
		// TODO Auto-generated method stub
		this.taskType = HttpListener.REFRESH_USER_INFO;
		UserUtils.fetchUserInfo(this, (HttpListener)this, uid);
	}
	
	UserBooksHelper collectionsHelper;
	
	protected void updateUserBooks(int uid, int start, int count) {
		// TODO Auto-generated method stub
		if(isMyself) collectionsHelper.deleteAll();
		this.taskType = HttpListener.UPDATE_USER_BOOKS;
		DoubanBookUtils.fetchUserCollection(this, this, taskType, uid, start, count);
	}
	
	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
//		mLoadingFooter.setState(LoadingFooter.State.Idle);
		if(mLoadingFooter.getState() == LoadingFooter.State.TheEnd) return;
		
		if (currentSection == SECTION_USER_FOLLOW){
			followings.clear();
			fetchFollowingUsers(curUser.uid, 0);
		} else {
			updateUserInfo(curUser.uid);
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
				Intent i2 = new Intent(UserPageActivity.this, BookSearchListActivity.class);
				i2.putExtra("add_book_search", true);
				startActivity(i2);
				chooseDialog.dismiss();
			}
        	
        });
    
        chooseDialog.setCanceledOnTouchOutside(true);
        chooseDialog.show();
	}
	
	/**
	 * 刷新书架UI, 加载新添加的书籍
	 */
	private void loadNewBooks(ArrayList<BookCollection> newcollections){
		all.addAll(0, newcollections);
//		resetListView();
		notifyDataList();
	}
	
	private Parcelable listState;
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		listState = listView.onSaveInstanceState();
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	private static final String ACTION_UPDATE_BOOKS = "update_user_books";
	private static final String ACTION_UPDATE_INFO = "update_user_info";
	private static final String ACTION_UPDATE_ITEM = "update_user_book_item";
	private static final String ACTION_DELETE_ITEM = "delete_user_book_item";
	private RefreshReceiver receiver; 
	private Bitmap avatarBitmap;
	
	private void registerReceiver(){
		receiver = new RefreshReceiver();  
        IntentFilter filter=new IntentFilter();  
        filter.addAction(ACTION_UPDATE_BOOKS);
        filter.addAction(ACTION_UPDATE_INFO);
        filter.addAction(ACTION_UPDATE_ITEM);
        filter.addAction(ACTION_DELETE_ITEM);

        //动态注册BroadcastReceiver  
        this.registerReceiver(receiver, filter); 
	}
	
	/**
	 * 接收来自其他地方的通知，更新UI（用户信息以及书架信息）
	 * @author tinyao
	 *
	 */
	class RefreshReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			if(!isMyself) return; 
			
			if(intent.getAction().equals(ACTION_UPDATE_BOOKS)) {
				bookTotal.setText("" + curUser.book_total);
				favTotal.setText("" + curUser.fav_total);
				ArrayList<BookCollection> newcollections 
						= intent.getParcelableArrayListExtra("new_books");
				if(newcollections != null){
					loadNewBooks(newcollections);
				}
			}
			
			if(intent.getAction().equals(ACTION_UPDATE_INFO)){
				
				if(intent.getBooleanExtra("avatar_change", false)){
					avatarBitmap = BitmapFactory.decodeFile(ImageUtils.avatarPath, null);
					userAvatar.setImageBitmap(avatarBitmap);
				}
				if(intent.getBooleanExtra("desc_change", false)){
					userDesc.setText(curUser.desc);
				}
				if(intent.getBooleanExtra("gender_change", false)){
					genderView.setChecked(curUser.gender==1);
					genderView.setVisibility(View.VISIBLE);
				}
			}
			
			if(intent.getAction().equals(ACTION_UPDATE_ITEM)){
				int update_cid = intent.getIntExtra("cid", 0);
				for(BookCollection eitem : all){
					if(eitem.cid == update_cid){
						eitem.note = intent.getStringExtra("note");
						eitem.score = intent.getFloatExtra("score", 0);
						eitem.status = intent.getIntExtra("status", 0);
						notifyDataList();
						listView.scrollTo(0, 0);
						listView.onRestoreInstanceState(listState);
						break;
					}
				}
			}
			
			if(intent.getAction().equals(ACTION_DELETE_ITEM)){
				int update_cid = intent.getIntExtra("cid", 0);
				for(BookCollection eitem : all){
					if(eitem.cid == update_cid){
						all.remove(eitem);
						notifyDataList();
						bookTotal.setText("" + curUser.book_total);
						listView.scrollTo(0, 0);
						listView.onRestoreInstanceState(listState);
						break;
					}
				}
			}
		}
		
	}
	
}
