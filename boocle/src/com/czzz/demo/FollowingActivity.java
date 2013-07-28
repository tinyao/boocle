package com.czzz.demo;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.User;
import com.czzz.bookcircle.UserUtils;
import com.czzz.demo.listadapter.NearbyUsersAdapter;
import com.czzz.utils.HttpListener;
import com.czzz.view.LoadingView;
import com.czzz.view.RefreshListView;
import com.czzz.view.RefreshListView.OnRefreshListener;

public class FollowingActivity extends AsyncTaskActivity implements OnRefreshListener{
	
	ArrayList<User> followings = new ArrayList<User>();;
	RefreshListView followListV;
	NearbyUsersAdapter userAdapter;
	
	private LoadingView loading;
	private int uid;
	private View footerView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.following);
		
		Intent i = getIntent();
		uid = i.getIntExtra("uid", User.getInstance().uid);
		
		followListV = (RefreshListView) findViewById(R.id.following_list);
		loading = (LoadingView) findViewById(R.id.following_loading_view);
		LayoutInflater layoutInflater = (LayoutInflater)this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
		footerView = layoutInflater.inflate(R.layout.pulldown_footer, null);
		followListV.addFooterView(footerView);
		
		fetchFollowing(uid, 0);
		
		followListV.setOnRefreshListener(this);
		followListV.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				User mu = followings.get(arg2);
				Intent userIntent = new Intent(FollowingActivity.this, UserPageActivity.class);
				userIntent.putExtra("user", mu);
				startActivity(userIntent);
			}
			
		});
		
		footerView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				fetchFollowing(uid, followings.size());
			}
			
		});
	}
	
	private void fetchFollowing(int user_id, int start){
		this.taskType = HttpListener.FETCH_USER_FOLLOWING;
		UserUtils.fetchFollowing(this, (HttpListener)this, 
				user_id, start);
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		Log.d("DEBUG", ""+data);
		ArrayList<User> newfollowings = parseFollowing(""+data);
		if(newfollowings == null) return;
		if(isRefresh) followings.clear();
		
		if(followings.size() == 0){
			followings.addAll(newfollowings);
			userAdapter = new NearbyUsersAdapter(this, followings);
			followListV.setAdapter(userAdapter);
		}else{
			followings.addAll(newfollowings);
			userAdapter = new NearbyUsersAdapter(this, followings);
			userAdapter.notifyDataSetChanged();
		}
		
		if(followings.size()<=15){
			footerView.setVisibility(View.GONE);
		}
		
		if(isRefresh) followListV.completeRefreshing(true);
		
		loading.setViewGone();
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
		loading.setViewGone();
	}

	private ArrayList<User> parseFollowing(String response){
		try {
			JSONObject json = new JSONObject(response);
			if(json.getInt("status") == 1){
				
				ArrayList<User> foings = new ArrayList<User>();
				
				JSONArray jarray = json.getJSONArray("data");
				for(int i=0; i<jarray.length(); i++){
					JSONObject item = jarray.getJSONObject(i).getJSONObject("following");
					User ufo = new User();
					
					foings.add(ufo.init(item));
				}
				return foings;
			}else{
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	private boolean isRefresh = false;
	
	@Override
	public void onRefresh(RefreshListView listView) {
		// TODO Auto-generated method stub
		isRefresh = true;
		fetchFollowing(uid, 0);
	}

	
}
