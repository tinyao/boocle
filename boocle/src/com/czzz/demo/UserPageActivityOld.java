package com.czzz.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.bookcircle.UserUtils;
import com.czzz.demo.listadapter.NearbyUsersAdapter;
import com.czzz.demo.listadapter.ShelfAdapter;
import com.czzz.demo.listadapter.ShelfListAdapter;
import com.czzz.douban.DoubanBookUtils;
import com.czzz.utils.HttpListener;
import com.czzz.utils.ImagesDownloader;
import com.czzz.utils.NetworkUtils;
import com.czzz.utils.TextUtils;
import com.czzz.view.LoadingView;
import com.czzz.view.ShelfListView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class UserPageActivityOld extends AsyncTaskActivity implements
		ShelfListView.OnRefreshListener {

	private ShelfListView shelfList, gridList, followingList;
	private TextView userName, userDesc, userAvatarTxt;
	private ImageView userAvatar;
	private CheckBox genderView;
	private RadioGroup shelfRadioSwitch;
	private TextView bookTotal, favTotal;
	private ProgressBar morePd;

	private ShelfListAdapter listAdapter;
	private ShelfAdapter gridAdapter;
	private NearbyUsersAdapter followingAdapter;
	private View footerView;
	private LoadingView loadingView;
	private ToggleButton followTa;
	
	private User curUser;
	private ArrayList<BookCollection> all = new ArrayList<BookCollection>();
	private ArrayList<User> followings;

	private ImagesDownloader imagesLoader = ImagesDownloader.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_bookshelf);

		shelfList = (ShelfListView) findViewById(R.id.shelf_list);
		gridList = (ShelfListView) findViewById(R.id.shelf_grid_list);
		followingList = (ShelfListView) findViewById(R.id.shelf_following_list);

		// View headerLayout = gridList.getHeaderView();
		View headerLayout = LayoutInflater.from(this).inflate(
				R.layout.user_page_header2, null);
		shelfList.setHeaderView(headerLayout);
		gridList.setHeaderView(headerLayout);
		followingList.setHeaderView(headerLayout);

		footerView = LayoutInflater.from(this).inflate(
				R.layout.pulldown_footer, null);

		shelfList.addFooterView(footerView);
		gridList.addFooterView(footerView);
		footerView.setVisibility(View.GONE);

		loadingView = (LoadingView) findViewById(R.id.loading_view);
		morePd = (ProgressBar) findViewById(R.id.pulldown_footer_loading);
		morePd.setIndeterminate(false);

		userAvatar = (ImageView) headerLayout.findViewById(R.id.avatar_img);
		userAvatarTxt = (TextView) headerLayout.findViewById(R.id.avatar_img_txt);
		userName = (TextView) headerLayout.findViewById(R.id.user_profile_btn);
		userDesc = (TextView) headerLayout.findViewById(R.id.user_desc);
		genderView = (CheckBox) headerLayout.findViewById(R.id.user_gender_img);
		bookTotal = (TextView) headerLayout.findViewById(R.id.user_book_total);
		favTotal = (TextView) headerLayout.findViewById(R.id.user_fav_total);
		followTa = (ToggleButton) headerLayout.findViewById(R.id.user_follow_ta);
		shelfRadioSwitch = (RadioGroup) headerLayout.findViewById(R.id.shelf_radio_group);

		// 获取本UserPage的User
		Intent i = getIntent();
		if(i.hasExtra("user"))
			curUser = (User) i.getSerializableExtra("user");
		else
			curUser = User.getInstance();
		curUser.collections = all;
		
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
						fetchFollowingUsers(curUser.uid, 0);
					}
					
					break;
				}
			}
		});

		footerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "footer clicked ~");
				morePd.setVisibility(View.VISIBLE);
				if (followingList.isShown()){
					// load more following
					fetchFollowingUsers(curUser.uid, followingAdapter.getCount());
				} else{
					fetchUserBooks(curUser.uid, all.size(), 9);
				}
			}

		});

		
		userAvatar.setOnClickListener(listener);
		userName.setOnClickListener(listener);
		
		shelfList.setOnRefreshListener(this);
		gridList.setOnRefreshListener(this);
		followingList.setOnRefreshListener(this);
		
		if(curUser.uid != User.getInstance().uid) {
			followTa.setVisibility(View.VISIBLE);
			isFollowed(curUser.uid);
		}
//		followTaa(curUser.uid);
		
		followTa.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(!followTa.isChecked()){
					unfollowTaa(curUser.uid);
				}else{
					followTaa(curUser.uid);
				}
			}
			
		});
		
	}
	
	private void initAllUserInfo() {
		// TODO Auto-generated method stub
	}
	
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
				fetchUserBooks(curUser.uid, 0, 9);
			}
		} else {
			// 无完整信息和书籍信息，先获取个人信息后获取书籍信息
			Log.d("DEBUG", "fetch uid: " + curUser.uid);
			fetchUserInfo(Integer.valueOf(curUser.uid));
		}

		shelfList.setAdapter(listAdapter);
		gridList.setAdapter(gridAdapter);
		followingList.setAdapter(followingAdapter);

		shelfList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				if (pos > listAdapter.getCount())
					return;

				Intent bookIntent = new Intent(UserPageActivityOld.this,
						BookInfoActivity.class);
				bookIntent.putExtra("collection", (Serializable) all.get(pos));
				bookIntent.putExtra("full", true); // 图书信息完整
				UserPageActivityOld.this.startActivity(bookIntent);
			}

		});
	}

	private void fetchUserInfo(int uid) {
		// TODO Auto-generated method stub
		loadingView.setMessage("获取书友信息...");
		if (!refresh)
			loadingView.setVisibility(View.VISIBLE);
		this.taskType = HttpListener.FETCH_CIRCLE_USER_INFO;
		UserUtils.fetchUserInfo(this, (HttpListener) this, uid);
	}

	protected void fetchUserBooks(int uid, int start, int count) {
		// TODO Auto-generated method stub
		loadingView.setMessage(getResources().getString(R.string.loading_ta_books));
		if (all.size() == 0)
			loadingView.setVisibility(View.VISIBLE);

		this.taskType = HttpListener.FETCH_USER_BOOKS;
		DoubanBookUtils.fetchUserCollection(this, (HttpListener) this,
				taskType, uid, start, count);
	}
	
	protected void fetchFollowingUsers(int uid, int start) {
		// TODO Auto-generated method stub
		this.taskType = HttpListener.FETCH_USER_FOLLOWING;
		UserUtils.fetchFollowing(this, this, uid, start);
	}

	private void createSampleList(ArrayList<BookCollection> lis) {
		// TODO Auto-generated method stub
		listAdapter = new ShelfListAdapter(this, lis);
		shelfList.setAdapter(listAdapter);
	}

	private void createSampleGrid(ArrayList<BookCollection> lis) {
		// TODO Auto-generated method stub
		gridAdapter = new ShelfAdapter(this, lis);
		gridList.setAdapter(gridAdapter);
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		String recvData = TextUtils.unicodeToString("" + data);
		Log.d("DEBUG", "data: " + recvData);
		switch (taskType) {
		case HttpListener.FETCH_USER_BOOKS:
			try {
				ArrayList<BookCollection> cs = DoubanBookUtils.parseUserBooks(
						"" + data, curUser);

				fillBookShelf(cs);

				shelfList.completeRefreshing(true);
				gridList.completeRefreshing(true);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			
			// if(refresh){
			imagesLoader.download(curUser.avatar, userAvatar, -1);
			// }

			fetchUserBooks(curUser.uid, 0, 9);
			break;
		case HttpListener.FETCH_USER_FOLLOWING:
			Log.d("DEBUG", "following: " + data);
			ArrayList<User> fusers = UserUtils.parseNeabyUsers("" + data);
			
			if(fusers == null) {
				morePd.setVisibility(View.GONE);
				if(followings==null){
					Crouton.makeText(this, "没有关注的人", Style.ALERT).show();
				}else{
					Crouton.makeText(this, R.string.no_more, Style.ALERT).show();
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
				followingAdapter = new NearbyUsersAdapter(this, fusers);
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
		Crouton.makeText(this, data, Style.ALERT).show();
		morePd.setVisibility(View.GONE);
		loadingView.setViewGone();
	}

	OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.avatar_img:
			case R.id.user_profile_btn:
				Log.d("DEBUG", "clicked");
				Intent profileIntent = new Intent(UserPageActivityOld.this,
						ProfileActivity.class);
				profileIntent.putExtra("user", curUser);
				startActivity(profileIntent);
				break;
			}
		}

	};

	private void fillBookShelf(ArrayList<BookCollection> cs) {
		if (cs == null) {
			if (all.size() != 0)
				Crouton.makeText(this, R.string.no_more, Style.CONFIRM).show();
			else
				Crouton.makeText(this, R.string.ta_has_no_books, Style.CONFIRM).show();
			morePd.setVisibility(View.GONE);
			loadingView.setViewGone();
			gridList.removeFooterView(footerView);
			shelfList.removeFooterView(footerView);
			return;
		}

		if (refresh) {
			all.clear();
			refresh = false;
		}

		all.addAll(cs);

		if (cs.size() < 9) {
			footerView.setVisibility(View.GONE);
		} else {
			footerView.setVisibility(View.VISIBLE);
		}

		if (gridAdapter == null) {
			createSampleGrid(all);
			createSampleList(all);
		} else {
			listAdapter = new ShelfListAdapter(this, all);
			gridAdapter = new ShelfAdapter(this, all);
			listAdapter.notifyDataSetChanged();
			gridAdapter.notifyDataSetChanged();
		}

		morePd.setVisibility(View.GONE);
		loadingView.setViewGone();

	}

	MenuItem followMenu;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if (curUser.uid != User.getInstance().uid) {
			getSupportMenuInflater().inflate(R.menu.menu_user_page, menu);
			followMenu = menu.findItem(R.id.menu__fragment_follow_star);
		}
		return super.onCreateOptionsMenu(menu);
	}

	int followTask = -1;
	
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
			followUser();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void followUser() {
		// TODO Auto-generated method stub
		new Thread(){
			public void run(){
				List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
				params.add(new BasicNameValuePair("uid", "" + User.getInstance().uid));
				params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
				params.add(new BasicNameValuePair("fuid", "" + curUser.uid));

				String str = NetworkUtils.postNetworkData(UserPageActivityOld.this, Pref.USER_FOLLOW_URL, params);
				Log.d("DEBUG", str);
				Message mmsg  = mmHandler.obtainMessage();
				mmsg.what = 1;
				mmHandler.sendMessage(mmsg);
			}
		}.start();
	}
	
	Handler mmHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			
			switch(msg.what){
			case 1:
				followMenu.setIcon(R.drawable.ic_action_followed_star);
				break;
			case 2:
				followMenu.setIcon(R.drawable.ic_action_follow_star);
				break;
			}
			
		}
		
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			// overridePendingTransition(R.anim.enter_fade_in,R.anim.right_exit);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean refresh = false;

	@Override
	public void onRefresh(ShelfListView listView) {
		// TODO Auto-generated method stub
		
		if (followingList.isShown()){
			// refresh fos;
			followings.clear();
			fetchFollowingUsers(curUser.uid, 0);
		} else {
			refresh = true;
			fetchUserInfo(curUser.uid);
		}
		
	}
	
	private void isFollowed(int fuid) {
		// TODO Auto-generated method stub
		Log.d("DEBUG", "isFollow");
		UserUtils.isFollowed(curUser.uid, 
				new UserPageResponseHandler(this, HttpListener.USER_IS_FOLLOWED));
	}
	
	private void followTaa(int fuid) {
		// TODO Auto-generated method stub
		UserUtils.followTa(curUser.uid, 
				new UserPageResponseHandler(this, HttpListener.USER_FOLLOW_TA));
	}
	
	private void unfollowTaa(int fuid) {
		// TODO Auto-generated method stub
		UserUtils.unfollowTa(curUser.uid, 
				new UserPageResponseHandler(this, HttpListener.USER_UNFOLLOW_TA));
	}
	
	/**
	 * handle http response
	 * @author tinyao
	 *
	 */
	private class UserPageResponseHandler extends CustomAsyncHttpResponseHandler{

		public UserPageResponseHandler(Activity activity, int taskId) {
			super(activity, taskId);
			// TODO Auto-generated constructor stub
			
		}

		@Override
		public void onSuccess(int httpResultCode, String response) {
			// TODO Auto-generated method stub
			super.onSuccess(httpResultCode, response);
			
			Log.d("DEBUG", "respone: " + response);
			
			switch(taskId){
			case HttpListener.USER_IS_FOLLOWED:
				
				try {
					JSONObject fjson = new JSONObject(response);
					if (fjson.getInt("data") == 1) {
						followTa.setChecked(true);
					} else {
						followTa.setChecked(false);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
			case HttpListener.USER_FOLLOW_TA:
				
				try {
					JSONObject fjson = new JSONObject(response);
					if (fjson.getInt("status") == 1) {
						Crouton.makeText(UserPageActivityOld.this, "关注成功", Style.CONFIRM).show();
						followTa.setText("已关注");
					} else {
						Crouton.makeText(UserPageActivityOld.this, "关注失败", Style.ALERT).show();
						followTa.setText("关注Ta");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
			case HttpListener.USER_UNFOLLOW_TA:
				try {
					JSONObject fjson = new JSONObject(response);
					if (fjson.getInt("status") == 1) {
						Crouton.makeText(UserPageActivityOld.this, "已取消关注", Style.CONFIRM).show();
						followTa.setText("关注Ta");
					} else {
						Crouton.makeText(UserPageActivityOld.this, "操作失败", Style.ALERT).show();
						followTa.setText("已关注");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	};
	
	class MyGestureDetector extends SimpleOnGestureListener {

		final ViewConfiguration vc = ViewConfiguration.get(getApplicationContext());
		final int SWIPE_MIN_X = vc.getScaledPagingTouchSlop() * 10;
		final int SWIPE_THRESHOLD_VELOCITY = vc.getScaledMinimumFlingVelocity() * 2;
		final int SWIPE_MAX_OFFPATH = vc.getScaledTouchSlop();
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			
			try{
				if (e2.getX() - e1.getX() > SWIPE_MIN_X && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
					finish(); //left
				}
			} catch (Exception e) {
				
			}
			return false;
		}
	}

}
