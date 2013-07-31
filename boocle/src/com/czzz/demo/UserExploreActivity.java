package com.czzz.demo;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
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

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class UserExploreActivity extends BaseActivity implements PullToRefreshAttacher.OnRefreshListener{

	private static final int DEFAULT_NUM = 20;
	private int usortId = 2;
	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private ListView listView;
	
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
        
        
        fetchNeabyUsers(0, DEFAULT_NUM, User.getInstance().school_id, usortId);
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
		
	}
	
	private void updateNearbyUsers(String response){
		ArrayList<User> users = UserUtils.parseNeabyUsers("" + response);
		
		if(users == null){
			Crouton.makeText(this, R.string.no_more, Style.ALERT).show();
			if(nearbyUsers.size() == 0){
				nearbyUsersAdapter = new NearbyUsersAdapter(this, nearbyUsers);
				listView.setAdapter(nearbyUsersAdapter);
			}
		}else{
			if(nearbyUsersAdapter == null || nearbyUsers.size() == 0){
				nearbyUsers.addAll(users);
				nearbyUsersAdapter = new NearbyUsersAdapter(this, nearbyUsers);
				listView.setAdapter(nearbyUsersAdapter);
			} else {
//				morePd.setVisibility(View.GONE);
				nearbyUsers.addAll(users);
				nearbyUsersAdapter = new NearbyUsersAdapter(this, nearbyUsers);
				nearbyUsersAdapter.notifyDataSetChanged();  
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
				break;
//			case HttpListener.FETCH_NEARBY_USERS_MORE:
//				updateNearbyUsers(response);
//				break;
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

}
