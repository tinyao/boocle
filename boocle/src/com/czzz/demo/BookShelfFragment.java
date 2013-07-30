package com.czzz.demo;

import java.io.Serializable;

import com.czzz.demo.R;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.czzz.base.BaseFragment;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.bookcircle.UserUtils;
import com.czzz.data.UserBooksHelper;
import com.czzz.demo.listadapter.NearbyUsersAdapter;
import com.czzz.demo.listadapter.ShelfAdapter;
import com.czzz.demo.listadapter.ShelfListAdapter;
import com.czzz.douban.DoubanBookUtils;
import com.czzz.utils.HttpListener;
import com.czzz.utils.ImageUtils;
import com.czzz.utils.ImagesDownloader;
import com.czzz.utils.TextUtils;
import com.czzz.view.LoadingView;
import com.czzz.view.ShelfListView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class BookShelfFragment extends BaseFragment implements HttpListener, ShelfListView.OnRefreshListener{

	private ShelfListView shelfList, gridList, followingList;
	private TextView userProfile, descText, avatarTxt;
	private TextView bookTotal, favTotal;
	private RadioButton gridShelf, listShelf, followingListBtn;
	private RadioGroup shelfRadioSwitch;
	private ImageView avatar;
	private CheckBox genderView;
	private Bitmap avatarBitmap;
	
	private ShelfListAdapter listAdapter;
	private ShelfAdapter gridAdapter;
	private NearbyUsersAdapter followingAdapter;
	
	private int taskType;
	
	private ProgressBar morePd;
	private View footerView;
	private LoadingView loadingView;
	
	private User mUser;
	
	boolean newLogin = false;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d("DEBUG", "BookShelf onActivityCreated...");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d("DEBUG", "BookShelf onCreate...");
		mUser = User.getInstance();
		setHasOptionsMenu(true);
		
		newLogin = this.getActivity().getIntent().getBooleanExtra("new_login", false);
		
		registerReceiver(); 
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		Log.d("DEBUG", "BookShelf onCreateView...");
		
		View v = inflater.inflate(R.layout.fragment_bookshelf, container, false);
		
		shelfList = (ShelfListView) v.findViewById(R.id.shelf_list);
		gridList = (ShelfListView) v.findViewById(R.id.shelf_grid_list);
		followingList = (ShelfListView) v.findViewById(R.id.shelf_following_list);

		//View headerLayout = gridList.getHeaderView();
		View headerLayout = inflater.inflate(R.layout.user_page_header2, null);
		shelfList.setHeaderView(headerLayout);
		gridList.setHeaderView(headerLayout);
		followingList.setHeaderView(headerLayout);
		
		footerView = LayoutInflater.from(this.getActivity())
				.inflate(R.layout.pulldown_footer, null);
		shelfList.addFooterView(footerView);
		gridList.addFooterView(footerView);
		footerView.setVisibility(View.GONE);
		loadingView = (LoadingView) v.findViewById(R.id.loading_view);
		morePd = (ProgressBar) v.findViewById(R.id.pulldown_footer_loading);
		morePd.setIndeterminate(false); 
		
		avatar = (ImageView) headerLayout.findViewById(R.id.avatar_img);
		avatarTxt = (TextView) headerLayout.findViewById(R.id.avatar_img_txt);
		
		Log.d("DEBUG", "avatar: " + avatar);
		genderView = (CheckBox) headerLayout.findViewById(R.id.user_gender_img);
		userProfile = (TextView) headerLayout.findViewById(R.id.user_profile_btn);
		descText = (TextView) headerLayout.findViewById(R.id.user_desc);
		bookTotal = (TextView) headerLayout.findViewById(R.id.user_book_total);
		favTotal = (TextView) headerLayout.findViewById(R.id.user_fav_total);
		loadingView = (LoadingView) v.findViewById(R.id.loading_view);
		gridShelf = (RadioButton) headerLayout
				.findViewById(R.id.shelf_switch_grid);
		listShelf = (RadioButton) headerLayout
				.findViewById(R.id.shelf_switch_list);
		followingListBtn = (RadioButton) headerLayout.findViewById(R.id.shelf_switch_following);
		shelfRadioSwitch = (RadioGroup) headerLayout.findViewById(R.id.shelf_radio_group);
//		gridShelf.setChecked(true);
		
		shelfList.setAdapter(listAdapter);
		gridList.setAdapter(gridAdapter);
		followingList.setAdapter(followingAdapter);
		
		shelfList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				if(pos > listAdapter.getCount()) return;
				
				Intent bookIntent = new Intent(BookShelfFragment.this.getActivity(),
						BookInfoActivity.class);
				bookIntent.putExtra("collection", (Serializable)all.get(pos));
				bookIntent.putExtra("full", true);	// 图书信息完整
				BookShelfFragment.this.startActivity(bookIntent);
			}
			
		});
		
		initProfileView();
		
		userProfile.setOnClickListener(listener);
		avatar.setOnClickListener(listener);

		shelfRadioSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup arg0, int selectedId) {
				// TODO Auto-generated method stub
				switch(selectedId){
				case R.id.shelf_switch_grid:
					gridList.setVisibility(View.VISIBLE);
					shelfList.setVisibility(View.GONE);
					followingList.setVisibility(View.GONE);
					break;
				case R.id.shelf_switch_list:
					gridList.setVisibility(View.GONE);
					shelfList.setVisibility(View.VISIBLE);
					followingList.setVisibility(View.GONE);
					break;
				case R.id.shelf_switch_following:
					gridList.setVisibility(View.GONE);
					shelfList.setVisibility(View.GONE);
					followingList.setVisibility(View.VISIBLE);
					
					// fetch the users following
					if(followingAdapter == null) {
						loadingView.setVisibility(View.VISIBLE);
						fetchFollowingUsers(mUser.uid, 0);
					}
					
					break;
				}
			}
			
		});
		
		footerView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "footer clicked ~");
				morePd.setVisibility(View.VISIBLE);
				if (followingList.isShown()){
					// load more following
					fetchFollowingUsers(mUser.uid, followingAdapter.getCount());
				} else{
					fetchUserBooks(mUser.uid, all.size(), 9 + (3 - all.size()%3) % 3 );
				}
			}
			
		});
		
		shelfList.setOnRefreshListener(this);
		gridList.setOnRefreshListener(this);
		followingList.setOnRefreshListener(this);
		
		followingList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent toUserPageIntent = new Intent(BookShelfFragment.this.getActivity(), UserPageActivity.class);
				toUserPageIntent.putExtra("user", followings.get(arg2));
				toUserPageIntent.putExtra("from_book_detail", true);
				startActivity(toUserPageIntent);
			}

			
		});
		
		return v;
	}

	private static final String ACTION_UPDATE_BOOKS = "update_user_books";
	private static final String ACTION_UPDATE_INFO = "update_user_info";
	private static final String ACTION_UPDATE_ITEM = "update_user_book_item";
	private static final String ACTION_DELETE_ITEM = "delete_user_book_item";
	private RefreshReceiver receiver; 
	
	private void registerReceiver(){
		receiver = new RefreshReceiver();  
        IntentFilter filter=new IntentFilter();  
        filter.addAction(ACTION_UPDATE_BOOKS);
        filter.addAction(ACTION_UPDATE_INFO);
        filter.addAction(ACTION_UPDATE_ITEM);
        filter.addAction(ACTION_DELETE_ITEM);

        //动态注册BroadcastReceiver  
        this.getActivity().registerReceiver(receiver, filter); 
	}
	
	
	
	private void initProfileView() {
		// TODO Auto-generated method stub
		userProfile.setText(mUser.name);
		descText.setText(mUser.desc);
		bookTotal.setText("" + mUser.book_total);
		favTotal.setText("" + mUser.fav_total);
		genderView.setChecked(mUser.gender==1);
		genderView.setVisibility(View.VISIBLE);
		if(mUser.avatar.equals("")) {
			avatarTxt.setText(mUser.name.substring(0, 1).toUpperCase(Locale.CHINA));
		}
		setUserAvatar();
		
		// fetch the books
		fetchUserBooks(User.getInstance().uid, 0, 9);
		
	}

	private void setUserAvatar() {
		if (!mUser.avatar.equals("")){
			// 如果已经设置了头像
//			File avFile = new File(ImageUtils.avatarPath);
//			if(avFile.exists() && !newLogin){
//				// 如果头像已有本地缓存 并且不是新登录
//				avatarBitmap = BitmapFactory.decodeFile(avFile.getPath(), null);
//				avatar.setImageBitmap(avatarBitmap);
//			} else {
				// 本地无头像缓存 或者 第一次登录
				new ImagesDownloader(ImagesDownloader.AVATAR_TASK).download(mUser.avatar, avatar);
//			}
		} else {
			// 如果头像未设置，使用默认头像
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d("DEBUG", "bookShelf onResume...");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
//		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.menu_book_shelf, menu);
	}

//	public void refresh() {
//	     /* Attach a rotating ImageView to the refresh item as an ActionView */
//	     LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//	     ImageView iv = (ImageView) inflater.inflate(R.layout.action_refresh, null);
//
//	     Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.refresh_animation);
//	     rotation.setRepeatCount(Animation.INFINITE);
//	     iv.startAnimation(rotation);
//
//	     refreshItem.setActionView(iv);
//
//	     //TODO trigger loading
//	 }
//	
//	public void completeRefresh() {
//	    refreshItem.getActionView().clearAnimation();
//	    refreshItem.setActionView(null);
//	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
//		case R.id.menu__fragment_add_book:
//			showBookAddDialog();
//			break;
//		case R.id.menu__fragment_add_book_scan:
//			Intent i1 = new Intent("com.czzz.action.qrscan.addbook");
//			startActivityForResult(i1,0);
//			break;
//		case R.id.menu__fragment_add_book_search:
//			Intent i2 = new Intent(getActivity(), BookSearchListActivity.class);
//			i2.putExtra("add_book_search", true);
//			startActivity(i2);
//			break;
		case R.id.menu__fragment_following:
			Intent followIntent = new Intent(this.getActivity(), FollowingActivity.class);
			startActivity(followIntent);
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	private void showBookAddDialog(){
		LayoutInflater inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.add_book_dialog, null);
        final View scanV = (View) view.findViewById(R.id.add_book_to_scan);
        final View searchV = (View) view.findViewById(R.id.add_book_to_search);
        final AlertDialog chooseDialog = new AlertDialog.Builder(this.getActivity())
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
				Intent i2 = new Intent(getActivity(), BookSearchListActivity.class);
				i2.putExtra("add_book_search", true);
				startActivity(i2);
				chooseDialog.dismiss();
			}
        	
        });
    
        chooseDialog.setCanceledOnTouchOutside(true);
        chooseDialog.show();
	}
	
	protected void fetchUserBooks(int uid, int start, int count) {
		// TODO Auto-generated method stub
		
		if(collectionsHelper == null){
			collectionsHelper = new UserBooksHelper(this.getActivity());
			collectionsHelper.openDataBase();
		}
		
		ArrayList<BookCollection> bc = null;
		if(all.size() == 0){	// 首次加载时，检测本地缓存
			bc = collectionsHelper.getCachedCollections();
		}
		
		if(bc != null) {
			all = bc;
			createSampleGrid(all);
			createSampleList(all);
			morePd.setVisibility(View.GONE);
			loadingView.setViewGone();
			footerView.setVisibility(View.VISIBLE);
		}else{
			this.taskType = HttpListener.FETCH_USER_BOOKS;
			DoubanBookUtils.fetchUserCollection(this.getActivity(), this, taskType, uid, start, count);
		}
		
	}
	
	protected void moreUserBooks(int uid, int start, int count) {
		// TODO Auto-generated method stub
		this.taskType = HttpListener.MORE_USER_BOOKS;
		DoubanBookUtils.fetchUserCollection(this.getActivity(), this, taskType, uid, start, count);
	}
	
	protected void fetchFollowingUsers(int uid, int start) {
		// TODO Auto-generated method stub
		this.taskType = HttpListener.FETCH_USER_FOLLOWING;
		UserUtils.fetchFollowing(this.getActivity(), this, uid, start);
	}
	
	private void createSampleList(ArrayList<BookCollection> lis) {
		// TODO Auto-generated method stub
		
		listAdapter =  new ShelfListAdapter( 
				BookShelfFragment.this.getActivity(), lis);
		shelfList.setAdapter(listAdapter);
		
	}

	private void createSampleGrid(ArrayList<BookCollection> lis) {
		// TODO Auto-generated method stub
		gridAdapter =  new ShelfAdapter(BookShelfFragment.this.getActivity(), lis);
		gridList.setAdapter(gridAdapter);
	}

	
	
	OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.avatar_img:
			case R.id.user_profile_btn:
				Log.d("DEBUG", "clicked");
				Intent detailIntent = new Intent(BookShelfFragment.this.getActivity(),
						ProfileActivity.class);
				startActivityForResult(detailIntent, 0);
				break;
			case R.id.add_books_btn:
				Intent addbookIntent = new Intent(BookShelfFragment.this.getActivity(),
						AddBookActivity.class);
				startActivity(addbookIntent);
				break;
			}
		}

	};

	
	UserBooksHelper collectionsHelper;
	ArrayList<BookCollection> all = new ArrayList<BookCollection>();
	ArrayList<User> followings;
	
	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		switch(taskType) {
		case HttpListener.FETCH_USER_BOOKS:
		case HttpListener.UPDATE_USER_BOOKS:
			Log.d("DEBUG", "data: " + TextUtils.unicodeToString(""+ data));
			if(taskType == HttpListener.UPDATE_USER_BOOKS) all.clear();
			try {
				ArrayList<BookCollection> cs= DoubanBookUtils.parseUserBooks(""+data, mUser);
				
				if(cs == null){
					if(all.size() != 0)
						Crouton.makeText(this.getActivity(), R.string.no_more, Style.CONFIRM).show();
					else{
						Crouton.makeText(this.getActivity(), R.string.you_have_no_books, Style.CONFIRM).show();
						if(taskType == HttpListener.UPDATE_USER_BOOKS){
							shelfList.completeRefreshing(false);
							gridList.completeRefreshing(false);
						}
					}
					morePd.setVisibility(View.GONE);
					loadingView.setViewGone();
//					gridList.removeFooterView(footerView);
//					shelfList.removeFooterView(footerView);
					footerView.setVisibility(View.GONE);
					return;
				}
				
				all.addAll(cs);
				
				if(cs.size() < 9) {
//					gridList.removeFooterView(footerView);
//					shelfList.removeFooterView(footerView);
					footerView.setVisibility(View.GONE);
				}else{
					footerView.setVisibility(View.VISIBLE);
				}
				
				/* 第一次加载 */
				if(gridAdapter == null){
					createSampleGrid(all);
					createSampleList(all);
					
				} else {
					listAdapter = new ShelfListAdapter(this.getActivity(), all);
					gridAdapter = new ShelfAdapter(this.getActivity(), all);
					listAdapter.notifyDataSetChanged();
					gridAdapter.notifyDataSetChanged();
				}
				
				if(collectionsHelper == null)
					collectionsHelper = new UserBooksHelper(this.getActivity());
				collectionsHelper.cacheUserCollections(cs);
				
				morePd.setVisibility(View.GONE);
				loadingView.setViewGone();
				
				shelfList.completeRefreshing(true);
				gridList.completeRefreshing(true);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case HttpListener.MORE_USER_BOOKS:
			try {
				ArrayList<BookCollection> cs= DoubanBookUtils.parseUserBooks(""+data, mUser);
				if(cs == null){
					morePd.setVisibility(View.GONE);
					return;
				}
				all.addAll(cs);
				listAdapter =  new ShelfListAdapter(BookShelfFragment.this.getActivity(), all);
				gridAdapter =  new ShelfAdapter(BookShelfFragment.this.getActivity(), all);
				listAdapter.notifyDataSetChanged();
				gridAdapter.notifyDataSetChanged();
				
				morePd.setVisibility(View.GONE);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case HttpListener.REFRESH_USER_INFO:
			UserUtils.parseMyself(TextUtils.unicodeToString(""+ data));
			updateUserBooks(User.getInstance().uid, 0, 9); 
			
			descText.setText(mUser.desc);
			bookTotal.setText("" + mUser.book_total);
			favTotal.setText("" + mUser.fav_total);
			userProfile.setText(mUser.name);
			genderView.setChecked(mUser.gender==1);
			genderView.setVisibility(View.VISIBLE);
			if(mUser.avatar.equals("")) {
				avatarTxt.setText(mUser.name.substring(0, 1).toUpperCase(Locale.CHINA));
			}
			new ImagesDownloader(ImagesDownloader.AVATAR_TASK).download(mUser.avatar, avatar);

			// update the info to database
			User.getInstance().save(getActivity());
			
			break;
		case HttpListener.FETCH_USER_FOLLOWING:
			Log.d("DEBUG", "following: " + data);
			
			ArrayList<User> fusers = UserUtils.parseNeabyUsers("" + data);
			
			if(fusers == null) {
				morePd.setVisibility(View.GONE);
				if(followings==null){
					Crouton.makeText(this.getActivity(), "没有关注的人", Style.ALERT).show();
				}else{
					Crouton.makeText(this.getActivity(), R.string.no_more, Style.ALERT).show();
				}
				loadingView.setViewGone();
				return;
			}
			
			if(fusers.size() < 15) {
				footerView.setVisibility(View.GONE);
			}else{
				footerView.setVisibility(View.VISIBLE);
			}
			
			if(followingAdapter == null) {
				followings = fusers;
				followingAdapter = new NearbyUsersAdapter(this.getActivity(), fusers, R.layout.shelf_follow_list_item);
				followingList.setAdapter(followingAdapter);
				followingList.addFooterView(footerView);
				loadingView.setViewGone();
			} else { 
				followings.addAll(fusers);
				followingAdapter.notifyDataSetChanged();
				morePd.setVisibility(View.GONE);
			}
			
			followingList.completeRefreshing(false);
			
			break;
		}
	}


	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Crouton.makeText(this.getActivity(), data, Style.ALERT).show();
		loadingView.setViewGone();
		morePd.setVisibility(View.GONE);
		shelfList.completeRefreshing(false);
		gridList.completeRefreshing(false);
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
			
			if(intent.getAction().equals(ACTION_UPDATE_BOOKS)) {
				bookTotal.setText("" + mUser.book_total);
				favTotal.setText("" + mUser.fav_total);
				ArrayList<BookCollection> newcollections 
						= intent.getParcelableArrayListExtra("new_books");
				if(newcollections != null){
					loadNewBooks(newcollections);
				}
			}
			
			if(intent.getAction().equals(ACTION_UPDATE_INFO)){
				
				if(intent.getBooleanExtra("avatar_change", false)){
					avatarBitmap = BitmapFactory.decodeFile(ImageUtils.avatarPath, null);
					avatar.setImageBitmap(avatarBitmap);
				}
				if(intent.getBooleanExtra("desc_change", false)){
					descText.setText(mUser.desc);
				}
				if(intent.getBooleanExtra("gender_change", false)){
					genderView.setChecked(mUser.gender==1);
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
						notifyListView();
						gridList.scrollTo(0, 0);
						shelfList.scrollTo(0, 0);
						gridList.onRestoreInstanceState(gridState);
						shelfList.onRestoreInstanceState(listState);
						break;
					}
				}
			}
			
			if(intent.getAction().equals(ACTION_DELETE_ITEM)){
				int update_cid = intent.getIntExtra("cid", 0);
				for(BookCollection eitem : all){
					if(eitem.cid == update_cid){
						all.remove(eitem);
						notifyListView();
						bookTotal.setText("" + mUser.book_total);
						gridList.scrollTo(0, 0);
						shelfList.scrollTo(0, 0);
						gridList.onRestoreInstanceState(gridState);
						shelfList.onRestoreInstanceState(listState);
						break;
					}
				}
			}
		}
		
	}
	
	private Parcelable gridState, listState;
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		gridState = gridList.onSaveInstanceState();
		listState = shelfList.onSaveInstanceState();
		super.onPause();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	/**
	 * 刷新书架UI, 加载新添加的书籍
	 */
	private void loadNewBooks(ArrayList<BookCollection> newcollections){
		all.addAll(0, newcollections);
		resetListView();
	}
	
	private void resetListView(){
		listAdapter = new ShelfListAdapter(this.getActivity(), all);
		gridAdapter = new ShelfAdapter(this.getActivity(), all);
		shelfList.setAdapter(listAdapter);
		gridList.setAdapter(gridAdapter);
	}

	private void notifyListView(){
		listAdapter =  new ShelfListAdapter(BookShelfFragment.this.getActivity(), all);
		gridAdapter =  new ShelfAdapter(BookShelfFragment.this.getActivity(), all);
		listAdapter.notifyDataSetChanged();
		gridAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		this.getActivity().unregisterReceiver(receiver);
		super.onDestroy();
	}

	
	protected void updateUserBooks(int uid, int start, int count) {
		// TODO Auto-generated method stub
		
		collectionsHelper.deleteAll();
		footerView.setVisibility(View.GONE);
		this.taskType = HttpListener.UPDATE_USER_BOOKS;
		DoubanBookUtils.fetchUserCollection(this.getActivity(), this, taskType, uid, start, count);
	}
	
	protected void updateMyInfo() {
		// TODO Auto-generated method stub
		this.taskType = HttpListener.REFRESH_USER_INFO;
		UserUtils.fetchUserInfo(this.getActivity(), (HttpListener)this, User.getInstance().uid);
	}

	@Override
	public void onRefresh(ShelfListView listView) {
		// TODO Auto-generated method stub
		if (followingList.isShown()){
			// refresh fos;
			followings.clear();
			fetchFollowingUsers(mUser.uid, 0);
		} else {
			updateMyInfo();
		}
	}

	
	
}
