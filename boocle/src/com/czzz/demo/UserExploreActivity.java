package com.czzz.demo;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.R.color;
import com.actionbarsherlock.app.ActionBar;
import com.czzz.base.BaseActivity;
import com.czzz.base.User;
import com.czzz.bookcircle.BookUtils;
import com.czzz.bookcircle.UserUtils;
import com.czzz.demo.listadapter.DoubanRecommAdapter;
import com.czzz.demo.listadapter.NearbyUsersAdapter;
import com.czzz.utils.HttpListener;
import com.czzz.view.LoadingFooter;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class UserExploreActivity extends BaseActivity implements PullToRefreshAttacher.OnRefreshListener{

	private static final int DEFAULT_NUM = 20;
	private int usortId = 2;
	private int school_id = User.getInstance().school_id;
	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private ListView listView;
	private LoadingFooter mLoadingFooter;
	
	private ArrayList<User> nearbyUsers = new ArrayList<User>();
	private NearbyUsersAdapter nearbyUsersAdapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // 设置自定义Action Spinner
        LayoutInflater inflater = LayoutInflater.from(this);
	    View actionSpinner = inflater.inflate(R.layout.action_spinner, null, false);
	    ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
	    layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
	    mActionBar.setCustomView(actionSpinner, layoutParams);
        
        listView = (ListView) findViewById(android.R.id.list);
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
	}
	
	protected void loadNextPage() {
		// TODO Auto-generated method stub
		int startCid = 0;
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
		fetchNeabyUsers(0, DEFAULT_NUM, school_id, usortId);
	}
	
	private void updateNearbyUsers(String response){
		ArrayList<User> users = UserUtils.parseNeabyUsers("" + response);
		
		if(users == null){
			Crouton.makeText(this, R.string.no_more, Style.ALERT).show();
			mLoadingFooter.setState(LoadingFooter.State.TheEnd);
		}else{
			if(nearbyUsersAdapter == null || nearbyUsers.size() == 0){
				nearbyUsers.addAll(users);
				nearbyUsersAdapter = new NearbyUsersAdapter(this, nearbyUsers);
				listView.setAdapter(nearbyUsersAdapter);
			} else {
				nearbyUsers.addAll(users);
				nearbyUsersAdapter.notifyDataSetChanged();  
			}
			
			if(users.size() < DEFAULT_NUM){
				mLoadingFooter.setState(LoadingFooter.State.TheEnd);
			}else{
				mLoadingFooter.setState(LoadingFooter.State.Idle, 3000);
			}
			users.clear();
			
		}
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
			
			switch(taskId){
			case HttpListener.FETCH_NEARBY_USERS:
				nearbyUsers.clear();
				updateNearbyUsers(response);
				if(mPullToRefreshAttacher.isRefreshing()) 
					mPullToRefreshAttacher.setRefreshComplete();
				break;
			case HttpListener.FETCH_NEARBY_USERS_MORE:
				updateNearbyUsers(response);
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
