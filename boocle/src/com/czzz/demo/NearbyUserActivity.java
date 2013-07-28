package com.czzz.demo;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.User;
import com.czzz.bookcircle.UserUtils;
import com.czzz.demo.listadapter.NearbyUsersAdapter;
import com.czzz.utils.HttpListener;
import com.czzz.utils.TextUtils;
import com.czzz.view.SchoolPopupDialog;
import com.czzz.view.SpinnerPopupDialog;
import com.czzz.view.tablelist.widget.UITableView;

public class NearbyUserActivity extends AsyncTaskActivity{
	
	private ActionBar mActionBar;
	UITableView contactlist, educationList;
	Button prefBtn;
	private ListView userListView;
	private View loadingView;
	private View footer;
	private View footerView;
	private Button schoolBtn, sortBtn;
	private ProgressBar morePd;
	private SchoolPopupDialog schoolDialog;
	
	private int school_id = User.getInstance().school_id;
	private int sort = 0;
	
	private ArrayList<User> nearbyUsers = new ArrayList<User>();
	private NearbyUsersAdapter nearbyUsersAdapter;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mActionBar = getSupportActionBar();

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayUseLogoEnabled(false);
		
		setContentView(R.layout.home_user_nearby);
		
		userListView = (ListView)findViewById(R.id.nearby_users_list);
		loadingView = (View)findViewById(R.id.loading_view);
		sortBtn = (Button)findViewById(R.id.nearby_users_sort);
		schoolBtn = (Button)findViewById(R.id.nearby_users_school);
		
		footerView = LayoutInflater.from(this)
				.inflate(R.layout.pulldown_footer, null);
		
		userListView.addFooterView(footerView);
		footer = findViewById(R.id.footer_view);
		morePd = (ProgressBar) findViewById(R.id.pulldown_footer_loading);
		morePd.setIndeterminate(false); 
		
		fetchNeabyUsers(0, 9, User.getInstance().school_id, 0);
		
		footer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "footer clicked ~");
				morePd.setVisibility(View.VISIBLE);
				loadMoreUsers();
			}
			
		});

		schoolBtn.setOnClickListener(listener);
		sortBtn.setOnClickListener(listener);
		
		userListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent ii = new Intent(NearbyUserActivity.this, UserPageActivity.class);
				User user = nearbyUsers.get(arg2);
				ii.putExtra("user", user);
				startActivity(ii);
			}
			
		});
		
	}
	
	private View.OnClickListener listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int[] location = new int[2];// x,y
			int height = sortBtn.getHeight();
			int width = sortBtn.getWidth();
			sortBtn.getLocationOnScreen(location);	// 获取spinner的高度
			switch(v.getId()){
			case R.id.nearby_users_school:
				if(schoolDialog == null){
					schoolDialog = new SchoolPopupDialog(
							NearbyUserActivity.this, R.style.spinner_popup_style,
							location[1] + height/2);
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
								schoolBtn.setText("我的学校");
							}else{
								schoolBtn.setText(school);
							}
							nearbyUsers.clear();
							schoolDialog.changed = false;
							fetchNeabyUsers(0, 9, school_id, sort);
						}
					}
					
				});
				schoolDialog.show();
				break;
			case R.id.nearby_users_sort:
				final SpinnerPopupDialog dialog = new SpinnerPopupDialog(NearbyUserActivity.this, 
						R.style.spinner_popup_style, 
						location[1] + height/2, width, 4);
				dialog.show();
				dialog.setOnDismissListener(new OnDismissListener(){

					@Override
					public void onDismiss(DialogInterface arg0) {
						// TODO Auto-generated method stub
						if(dialog.selectId != -1){
							sort = dialog.selectId;
							nearbyUsers.clear();
							fetchNeabyUsers(0, 9, school_id, sort);
							sortBtn.setText(dialog.selectStr);
						}
					}
					
				});
				break;
			}
		}
	};
	
	protected void loadMoreUsers() {
		// TODO Auto-generated method stub
		footerView.setVisibility(View.VISIBLE);
		this.taskType = HttpListener.FETCH_NEARBY_USERS;
		UserUtils.fetchNearbyUsers(nearbyUsers.size(), 9, school_id, sort, null);
	}

	/**
	 * 获取同校书友
	 * @param start
	 * @param count
	 * @param school_id 学校id
	 * @param sort 书友排序方式
	 */
    private void fetchNeabyUsers(int start, int count, int school_id, int sort) {
		// TODO Auto-generated method stub
    	footerView.setVisibility(View.VISIBLE);
    	loadingView.setVisibility(View.VISIBLE);
		this.taskType = HttpListener.FETCH_NEARBY_USERS;
		UserUtils.fetchNearbyUsers(start, count, school_id, sort, null);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		case R.id.abs__home:
			finish();
			break;
		case android.R.id.home:
			finish();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		String recvData = TextUtils.unicodeToString("" + data);
		switch(taskType){
		case HttpListener.FETCH_NEARBY_USERS:
			Log.d("DEBUG", "user: " + recvData);
			ArrayList<User> users = UserUtils.parseNeabyUsers("" + recvData);
			
			if(users == null){
				Toast.makeText(this, R.string.no_more, Toast.LENGTH_SHORT).show();
				morePd.setVisibility(View.GONE);
				loadingView.setVisibility(View.GONE);
				footerView.setVisibility(View.GONE);
				return;
			}else{
				footerView.setVisibility(View.VISIBLE);
			}
			
			if(users.size() < 9){
				footerView.setVisibility(View.GONE);
			}
			
			nearbyUsers.addAll(users);
			
			if(nearbyUsersAdapter == null){
				nearbyUsersAdapter = new NearbyUsersAdapter(this, nearbyUsers);
				userListView.setAdapter(nearbyUsersAdapter);
			} else {
				nearbyUsersAdapter = new NearbyUsersAdapter(this, nearbyUsers);
				nearbyUsersAdapter.notifyDataSetChanged();  
			}
			
			loadingView.setVisibility(View.GONE);
			morePd.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
		loadingView.setVisibility(View.GONE);
		morePd.setVisibility(View.GONE);
	}
	
}

