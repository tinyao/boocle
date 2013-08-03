package com.czzz.demo;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.czzz.base.BaseActivity;
import com.czzz.base.User;
import com.czzz.bookcircle.UserUtils;
import com.czzz.demo.listadapter.NearbyUsersAdapter;
import com.czzz.utils.HttpListener;
import com.czzz.view.LoadingFooter;
import com.czzz.view.SchoolPopupDialog;
import com.czzz.view.LoadingFooter.State;
import com.czzz.view.PositionAwareListView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class UserExploreActivity extends BaseActivity implements PullToRefreshAttacher.OnRefreshListener{

	private static final int DEFAULT_NUM = 20;
	private int usortId = 2;
	private int school_id = User.getInstance().school_id;
	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private PositionAwareListView listView;
	private LoadingFooter mLoadingFooter;
	private Spinner actionSpinner;
	
	private ArrayList<User> nearbyUsers = new ArrayList<User>();
	private NearbyUsersAdapter nearbyUsersAdapter;
	private ArrayList<User> followings, otherSchoolUsers, randomUsers;
	private NearbyUsersAdapter followingAdapter, otherSchoolAdapter, randomUserAdapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // 设置自定义Action Spinner
        LayoutInflater inflater = LayoutInflater.from(this);
	    View actionSpinnerLay = inflater.inflate(R.layout.action_spinner, null, false);
	    ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
	    layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
	    mActionBar.setCustomView(actionSpinnerLay, layoutParams);
	    actionSpinner = (Spinner) actionSpinnerLay.findViewById(R.id.action_spinner_btn);
        
        listView = (PositionAwareListView) findViewById(android.R.id.list);
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        mPullToRefreshAttacher.addRefreshableView(listView, this);
        mLoadingFooter = new LoadingFooter(this);
	    listView.addFooterView(mLoadingFooter.getView());
	    listView.addHeaderView(new View(this));
        
        fetchNeabyUsers(0, DEFAULT_NUM, User.getInstance().school_id, usortId);
        
        listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				Intent goIntent = new Intent(UserExploreActivity.this, UserPageActivity.class);
				User user = (User)nearbyUsersAdapter.getItem(pos-1);
				goIntent.putExtra("user", user);
				startActivity(goIntent);
			}
        	
        });
        
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
            	
                if (mLoadingFooter.getState() == LoadingFooter.State.Loading
                        || mLoadingFooter.getState() == LoadingFooter.State.TheEnd) {
                    return;
                }
                if (firstVisibleItem + visibleItemCount >= totalItemCount
                        && totalItemCount != 0
                        && totalItemCount != listView.getHeaderViewsCount()
                                + listView.getFooterViewsCount() && nearbyUsers.size() > 0) {
                    
                	Log.d("DEBUG", "load next page");
                	loadNextPage();
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
					Log.d("DEBUG", "");
					nearbyUsersAdapter = new NearbyUsersAdapter(UserExploreActivity.this, nearbyUsers);
					listView.setAdapter(nearbyUsersAdapter, true);
					break;
				case SectionID.SECTION_FOLLOW:
					if (followings == null){ // 没有获取：第一次切换
						followings = new ArrayList<User>();
						followingAdapter = new NearbyUsersAdapter(UserExploreActivity.this, followings);
						listView.setAdapter(followingAdapter, true);
						mPullToRefreshAttacher.setRefreshing(true);
						UserUtils.fetchFollowing(UserExploreActivity.this, User.getInstance().uid, 0, new UserExploreResponeHandler(UserExploreActivity.this, 
								HttpListener.FETCH_USER_FOLLOWING) );
					} else{
						listView.setAdapter(followingAdapter, true);
					}
					break;
				case SectionID.SECTION_OTHER_SCHOOL:
					
					school_id = perSchool_id;
					
					if(otherSchoolAdapter == null){
						otherSchoolUsers = new ArrayList<User>();
						otherSchoolAdapter = new NearbyUsersAdapter(UserExploreActivity.this, otherSchoolUsers);
						listView.setAdapter(otherSchoolAdapter, true);
					} else {
						listView.setAdapter(otherSchoolAdapter, true);
					}
					showSchoolDialog();
					break;
				case SectionID.SECTION_RANDOM:
					if(randomUsers == null){
						randomUsers = new ArrayList<User>();
						randomUserAdapter = new NearbyUsersAdapter(UserExploreActivity.this, randomUsers);
						listView.setAdapter(randomUserAdapter, true);
					} else {
						listView.setAdapter(randomUserAdapter);
					}
					mPullToRefreshAttacher.setRefreshing(true);
					UserUtils.fetchNearbyUsers(0, DEFAULT_NUM, 0, 0, 
							new UserExploreResponeHandler(UserExploreActivity.this, 
									HttpListener.FETCH_NEARBY_USERS));
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
	    	
	    });
        
	}
	
	private int currentSection = SectionID.SECTION_SAME_SCHOOL;
	
	private static class SectionID {
		private static final int SECTION_SAME_SCHOOL = 0;
		private static final int SECTION_FOLLOW = 1;
		private static final int SECTION_OTHER_SCHOOL = 2;
		private static final int SECTION_RANDOM = 3;
	}
	
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
					perSchool_id = sid;
					schoolDialog.changed = false;
					mPullToRefreshAttacher.setRefreshing(true);
					UserUtils.fetchNearbyUsers(0, DEFAULT_NUM, school_id, usortId, 
							new UserExploreResponeHandler(UserExploreActivity.this, HttpListener.FETCH_NEARBY_USERS));
				}
				school_id = sid;
			}
			
		});
		schoolDialog.show();
	}
	
	int perSchool_id = User.getInstance().school_id;
	
	protected void loadNextPage() {
		// TODO Auto-generated method stub
		int startCid = 0;
		mLoadingFooter.setState(LoadingFooter.State.Loading);
		
		switch(currentSection){
		case SectionID.SECTION_SAME_SCHOOL:
			if(usortId == 1){
				// 最新用户
				if(nearbyUsers != null && nearbyUsers.size() > 0){
					startCid = nearbyUsers.get(nearbyUsers.size() - 1).uid;
				}
			}else{
				// 藏书最多、随机排序  从
				startCid = nearbyUsers.size();
			}
			mLoadingFooter.setState(LoadingFooter.State.Loading);
			UserUtils.fetchNearbyUsers(startCid, DEFAULT_NUM, school_id, usortId, 
					new UserExploreResponeHandler(this, HttpListener.FETCH_NEARBY_USERS_MORE));
			break;
		case SectionID.SECTION_OTHER_SCHOOL:
			if(usortId == 1){
				// 最新用户
				if(nearbyUsers != null && nearbyUsers.size() > 0){
					startCid = nearbyUsers.get(nearbyUsers.size() - 1).uid;
				}
			}else{
				// 藏书最多、随机排序  从
				startCid = nearbyUsers.size();
			}
			mLoadingFooter.setState(LoadingFooter.State.Loading);
			UserUtils.fetchNearbyUsers(startCid, DEFAULT_NUM, school_id, usortId, 
					new UserExploreResponeHandler(this, HttpListener.FETCH_NEARBY_USERS_MORE));
			break;
		case SectionID.SECTION_FOLLOW:
			mLoadingFooter.setState(State.TheEnd);
			break;
		case SectionID.SECTION_RANDOM:
			mLoadingFooter.setState(State.TheEnd);
			break;
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
				new UserExploreResponeHandler(this, HttpListener.FETCH_NEARBY_USERS));
	}
    
	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
		switch(currentSection){
		case SectionID.SECTION_SAME_SCHOOL:
			nearbyUsers.clear();
			fetchNeabyUsers(0, DEFAULT_NUM, school_id, usortId);
			break;
		case SectionID.SECTION_OTHER_SCHOOL:
			otherSchoolUsers.clear();
			fetchNeabyUsers(0, DEFAULT_NUM, school_id, usortId);
			break;
		case SectionID.SECTION_FOLLOW:
			followings.clear();
			UserUtils.fetchFollowing(UserExploreActivity.this, User.getInstance().uid, 0, new UserExploreResponeHandler(UserExploreActivity.this, 
					HttpListener.FETCH_USER_FOLLOWING) );
			break;
		case SectionID.SECTION_RANDOM:
			randomUsers.clear();
			UserUtils.fetchNearbyUsers(0, DEFAULT_NUM, 0, 0, 
					new UserExploreResponeHandler(UserExploreActivity.this, 
							HttpListener.FETCH_NEARBY_USERS));
			break;
		}
		
		
	}
	
	private void updateNearbyUsers(ArrayList<User> users){
		
		if(users == null){
			mLoadingFooter.setState(LoadingFooter.State.TheEnd, 10);
		}else{
			
			switch(currentSection){
			case SectionID.SECTION_SAME_SCHOOL:
				nearbyUsers.addAll(users);
				nearbyUsersAdapter.notifyDataSetChanged();
				break;
			case SectionID.SECTION_OTHER_SCHOOL:
				if(mLoadingFooter.getState() == LoadingFooter.State.Loading){	// 不是加载更多，而是切换学校
					otherSchoolUsers.addAll(users);
					otherSchoolAdapter.notifyDataSetChanged();
				}else{
					otherSchoolUsers.clear();
					otherSchoolUsers.addAll(users);
					otherSchoolAdapter.notifyDataSetChanged();
//					listView.setAdapter(otherSchoolAdapter, true);
				}
				break;
			case SectionID.SECTION_RANDOM:
				if(mLoadingFooter.getState() == LoadingFooter.State.Loading){	// 不是加载更多，而是切换学校
					randomUsers.addAll(users);
					randomUserAdapter.notifyDataSetChanged();
				}else{
					randomUsers.clear();
					randomUsers.addAll(users);
					listView.setAdapter(randomUserAdapter, true);
				}
				break;
			}
			
			if(users.size() < DEFAULT_NUM){
				mLoadingFooter.setState(LoadingFooter.State.TheEnd);
			}else{
				mLoadingFooter.setState(LoadingFooter.State.Idle, 3000);
			}
				
		}
		
//		ArrayList<User> users = UserUtils.parseNeabyUsers("" + response);
//		
//		if(users == null){
//			Crouton.makeText(this, R.string.no_more, Style.ALERT).show();
//			mLoadingFooter.setState(LoadingFooter.State.TheEnd);
//		}else{
//			if(nearbyUsersAdapter == null || nearbyUsers.size() == 0){
//				nearbyUsers.addAll(users);
//				nearbyUsersAdapter = new NearbyUsersAdapter(this, nearbyUsers);
//				listView.setAdapter(nearbyUsersAdapter);
//			} else {
//				nearbyUsers.addAll(users);
//				nearbyUsersAdapter.notifyDataSetChanged();  
//			}
//			
//			if(users.size() < DEFAULT_NUM){
//				mLoadingFooter.setState(LoadingFooter.State.TheEnd);
//			}else{
//				mLoadingFooter.setState(LoadingFooter.State.Idle, 3000);
//			}
//			users.clear();
//			
//		}
	}
	
	/**
	 * handle http response
	 * @author tinyao
	 *
	 */
	private class UserExploreResponeHandler extends CustomAsyncHttpResponseHandler{

		public UserExploreResponeHandler(Activity activity, int taskId) {
			super(activity, taskId);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onSuccess(int httpResultCode, String response) {
			// TODO Auto-generated method stub
			super.onSuccess(httpResultCode, response);
			ArrayList<User> fusers = UserUtils.parseNeabyUsers(response);
			switch(taskId){
			case HttpListener.FETCH_NEARBY_USERS:
//				if(currentSection != SectionID.SECTION_RANDOM) nearbyUsers.clear();
				updateNearbyUsers(fusers);
				if(mPullToRefreshAttacher.isRefreshing()) 
					mPullToRefreshAttacher.setRefreshComplete();
				break;
			case HttpListener.FETCH_NEARBY_USERS_MORE:
				updateNearbyUsers(fusers);
				break;
			case HttpListener.FETCH_USER_FOLLOWING:
				if(followings.size()==0 && fusers == null){
					Crouton.makeText(UserExploreActivity.this, "您还没有关注其他书友", Style.ALERT).show();
				}else{
					followings.addAll(fusers);
//					followBooksAdapter = new NearbyBooksAdapter(HomeActivity.this, followBooks);
//					listView.setAdapter(followBooksAdapter, true);
					followingAdapter.notifyDataSetChanged();
					if(mLoadingFooter.getState() == LoadingFooter.State.Loading){
						mLoadingFooter.setState(LoadingFooter.State.Idle, 3000);
					}
				}
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
			if(mPullToRefreshAttacher.isRefreshing())
				mPullToRefreshAttacher.setRefreshComplete();
		}

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			super.onStart();
		}
	}

}
