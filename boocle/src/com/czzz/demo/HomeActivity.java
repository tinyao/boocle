package com.czzz.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.czzz.base.DrawerBaseActivity;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.bookcircle.BookUtils;
import com.czzz.bookcircle.task.AlarmTask;
import com.czzz.data.UserBooksHelper;
import com.czzz.demo.listadapter.NearbyBooksAdapter;
import com.czzz.utils.HttpListener;
import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class HomeActivity extends DrawerBaseActivity implements PullToRefreshAttacher.OnRefreshListener{
	
	private static final int DEFAULT_NUM = 15;
	
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private SharedPreferences sp;
	private ListView listView;
	
	private ArrayList<BookCollection> nearbyBooks = new ArrayList<BookCollection>();
	private NearbyBooksAdapter nearbyBooksAdapter;
	private int statusId = 2, sortId = 0;
    private int usortId = 2;
    private int school_id = User.getInstance().school_id; // initial school id
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preCheck();	// checnk for logout and msg-alarm
        
        initLayoutActionBar();
	    
	    /**
         * Here we create a PullToRefreshAttacher manually without an Options instance.
         * PullToRefreshAttacher will manually create one using default values.
         */
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);

        // Set the Refreshable View to be the ListView and the refresh listener to be this.
        mPullToRefreshAttacher.addRefreshableView(listView, this);
	}
	
	/* 初始化fadingActionbar，设置自定义view */
	private void initLayoutActionBar(){
		FadingActionBarHelper helper = new FadingActionBarHelper()
	        .actionBarBackground(R.drawable.actionbar_base)
	        .headerLayout(R.layout.header)
	        .headerPanelLayout(R.layout.search)
	        .contentLayout(R.layout.activity_home);
	    setContentView(helper.createView(this));
	    helper.initActionBar(this);
	    
	    ActionBar mActionBar = this.getSupportActionBar();
	    mActionBar.setDisplayShowCustomEnabled(true);
	    mActionBar.setDisplayShowTitleEnabled(true);
	    
	    LayoutInflater inflater = LayoutInflater.from(this);
	    View actionSpinner = inflater.inflate(R.layout.action_spinner, null, false);
	    ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
	    layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
	    mActionBar.setCustomView(actionSpinner, layoutParams);
	    
	    // fill the listview
	    listView = (ListView) findViewById(android.R.id.list);
//	    ArrayList<String> items = loadItems(R.raw.nyc_sites);
//	    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
//	    listView.setAdapter(adapter);
	    obtainNearbyBooks();
	}
	
	private void preCheck(){
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
	}
	
	@Override
	protected View initDrawerView() {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        View drawerView = inflater.inflate(R.layout.menu_drawer_layout, null);

        drawerView.findViewById(R.id.drawer_menu_book).setOnClickListener(menuClickListener);
        drawerView.findViewById(R.id.drawer_menu_user).setOnClickListener(menuClickListener);;
        drawerView.findViewById(R.id.drawer_menu_msg).setOnClickListener(menuClickListener);;
        drawerView.findViewById(R.id.drawer_menu_douban).setOnClickListener(menuClickListener);;
        drawerView.findViewById(R.id.drawer_menu_setting).setOnClickListener(menuClickListener);;
        
        return drawerView;
	}

	@Override
	protected void onMenuItemClicked(int resId) {
		// TODO Auto-generated method stub
		switch(resId){
		case R.id.drawer_menu_book:
			mMenuDrawer.closeMenu();
			break;
		case R.id.drawer_menu_user:
			Intent userIntent = new Intent(this, UserPageActivity.class);
			startActivity(userIntent);
			break;
		case R.id.drawer_menu_msg:
//			Intent msgIntent = new Intent(this, UserPageActivity.class);
//			startActivity(userIntent);
			break;
		case R.id.drawer_menu_douban:
//			Intent userIntent = new Intent(this, UserPageActivity.class);
//			startActivity(userIntent);
			break;
		case R.id.drawer_menu_setting:
			Intent settingIntent = new Intent(this, SettingActivity.class);
			startActivity(settingIntent);
			break;
		}
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
	
	/* update nearby books */
	protected void updateNearbyBooks(String respone) {
		// TODO Auto-generated method stub
		// 网络返回
		ArrayList<BookCollection> books = BookUtils.parseNearybyBooks(respone);
		
		if(books == null){
			Crouton.makeText(this, R.string.no_more, Style.CONFIRM).show();
			if(nearbyBooks.size() == 0){
				nearbyBooksAdapter = new NearbyBooksAdapter(this, nearbyBooks);
				listView.setAdapter(nearbyBooksAdapter);
			}
		}else{
			if(nearbyBooksAdapter == null || nearbyBooks.size() == 0){
				nearbyBooks.addAll(books);
				nearbyBooksAdapter = new NearbyBooksAdapter(this, nearbyBooks);
				listView.setAdapter(nearbyBooksAdapter);
				
				// 只缓存“status=全部”的书籍
				if(school_id == User.getInstance().school_id && statusId == 2){
					UserBooksHelper cachehelper = UserBooksHelper.getInstance(this);
					cachehelper.cacheNearbyCollections(books);
				}
			} else {
				nearbyBooks.addAll(books);
				nearbyBooksAdapter = new NearbyBooksAdapter(this, nearbyBooks);
				nearbyBooksAdapter.notifyDataSetChanged();
			}
		}
		
	}
	
	/**
     * @return A list of Strings read from the specified resource
     */
    private ArrayList<String> loadItems(int rawResourceId) {
        try {
            ArrayList<String> countries = new ArrayList<String>();
            InputStream inputStream = getResources().openRawResource(rawResourceId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                countries.add(line);
            }
            reader.close();
            return countries;
        } catch (IOException e) {
            return null;
        }
    }

	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
		/**
         * Simulate Refresh with 4 seconds sleep
         */
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                // Notify PullToRefreshAttacher that the refresh has finished
                mPullToRefreshAttacher.setRefreshComplete();
            }
        }.execute();
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
			
			switch(taskId){
			case HttpListener.FETCH_NEARBY_BOOKS:
				nearbyBooks.clear();
				updateNearbyBooks(response);
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
		}

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			super.onStart();
		}
		
	};
	
}