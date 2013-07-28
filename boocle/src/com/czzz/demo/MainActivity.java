package com.czzz.demo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.SearchView;
import com.czzz.base.User;
import com.czzz.bookcircle.MyApplication;
import com.czzz.bookcircle.task.AlarmTask;
import com.czzz.view.viewpager.FadingTabPageIndicator;
import com.czzz.view.viewpager.TabPageIndicator;
import com.czzz.view.viewpager.UnderlinePageIndicator;

public class MainActivity extends SherlockFragmentActivity implements ActionBar.OnNavigationListener {
	ActionBar mActionBar;
	ViewPager mPager;
	SearchView searchView;
	
	MyFragmentPagerAdapter fragmentPagerAdapter;
	FragmentManager fm;
	
	SharedPreferences sp;
	
	private static final String[] CONTENT = new String[] { "探索发现", "我的主页", "私信列表"};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d("DEBUG", "Main Activity create...");
		
//		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		
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
        
		mActionBar = getSupportActionBar();
		
		setContentView(R.layout.activity_main);
		
		if(MyApplication.hideTabs) {
			mActionBar.setDisplayShowTitleEnabled(false);
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		} else {
			mActionBar.setDisplayShowTitleEnabled(true);
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		}
		
		/** Getting a reference to ViewPager from the layout */
		mPager = (ViewPager) findViewById(R.id.main_pager);
		mPager.setOffscreenPageLimit(2);
		fm = getSupportFragmentManager();
		
		FragmentPagerAdapter adapter = new MyFragmentPagerAdapter(fm);
		mPager.setAdapter(adapter);
		
		mPager.setOnPageChangeListener(pageChangeListener);
		
		if(!MyApplication.hideTabs) {

			/** Creating Apple Tab */
			Tab tab = mActionBar.newTab().setTabListener(tabListener)
					.setText(R.string.tab_explore);
			mActionBar.addTab(tab);
	
			tab = mActionBar.newTab().setTabListener(tabListener)
					.setText(R.string.tab_personal);
			mActionBar.addTab(tab);
	
			tab = mActionBar.newTab().setTabListener(tabListener)
					.setText(R.string.tab_message);
			mActionBar.addTab(tab);
			
		} else {
			UnderlinePageIndicator indicator  = (UnderlinePageIndicator)findViewById(R.id.main_indicator);
	        indicator.setViewPager(mPager);
	        
	        // Specify a SpinnerAdapter to populate the dropdown list.
	        ArrayAdapter<String> dropAdapter = new ArrayAdapter<String>(mActionBar.getThemedContext(),
	            android.R.layout.simple_spinner_item, android.R.id.text1,
	            CONTENT);

	        dropAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

	        // Set up the dropdown list navigation in the action bar.
	        mActionBar.setListNavigationCallbacks(dropAdapter, this);
	        
	        indicator.setOnPageChangeListener(new OnPageChangeListener(){

				@Override
				public void onPageScrollStateChanged(int arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onPageSelected(int position) {
					// TODO Auto-generated method stub
					mActionBar.setSelectedNavigationItem(position);
				}
	        	
	        });
		}
		
		User.getInstance().init(this);

	}
	
	/** Defining a listener for pageChange */
	ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			super.onPageSelected(position);

			mActionBar.setSelectedNavigationItem(position);
		}

	};
	
	/** Defining tab listener */
	ActionBar.TabListener tabListener = new ActionBar.TabListener() {

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			mPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

	};
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putInt("tab", mActionBar.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

		// SearchView ss= new
		// SearchView(getSupportActionBar().getThemedContext());
		// ss.setQueryHint("Search books…");

		menu.clear();
		
		getSupportMenuInflater().inflate(R.menu.menu_book_shelf, menu);

		//searchView = (SearchView) menu.findItem(R.id.menu_search)
		//		.getActionView();
		
//		MenuItem search=menu.findItem(R.id.menu_search);
//		search.collapseActionView();
//        SearchView searchview=(SearchView) search.getActionView();
//        searchview.setIconifiedByDefault(false);
//        SearchManager mSearchManager=(SearchManager)getSystemService(Context.SEARCH_SERVICE);
//        SearchableInfo info=mSearchManager.getSearchableInfo(getComponentName());
//        searchview.setSearchableInfo(info); //需要在Xml文件加下建立searchable.xml,搜索框配置文件
		
		// 2013-05-01
		/*
		searchView.setQueryHint("Search for Books");
		Log.d("DEBUG", "set searchable");
		traverseView(searchView, 0);
		*/
		
//		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//		
//		SearchManager searchManager =
//		           (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//		
//		searchView.setIconifiedByDefault(false);
//        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
//        searchView.setSearchableInfo(info); //需要在Xml文件加下建立searchable.xml,搜索框配置文件
		
		/*
		searchView.setOnQueryTextListener(new OnQueryTextListener(){

			@Override
			public boolean onQueryTextSubmit(String query) {
				// TODO Auto-generated method stub
				if(DoubanBookUtils.isISBN(query)){
					Intent i = new Intent(MainActivity.this, BookInfoActivity.class);
					i.putExtra("isbn", query);
					startActivity(i);
					overridePendingTransition(R.anim.shrink_from_bottom,R.anim.exit_fade_out);
				}else{

			        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(MainActivity.this,
			                MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
			        suggestions.saveRecentQuery(query, null);
			        
					Intent i = new Intent(MainActivity.this, BookSearchListActivity.class);
					i.putExtra("keyword", query);
					startActivity(i);
					overridePendingTransition(R.anim.shrink_from_bottom,R.anim.exit_fade_out);
				}
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
				imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
		
		*/
		
		return true;

	}
	
	
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item) {
		 switch(item.getItemId()){
		 case R.id.menu_setting:
			 Intent i = new Intent(this, SettingActivity.class);
			 startActivity(i);
			 break;
		 case R.id.menu__fragment_add_book:
			showBookAddDialog();
			break;
		 }
		 return false;
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
					Intent i2 = new Intent(MainActivity.this, BookSearchListActivity.class);
					i2.putExtra("add_book_search", true);
					startActivity(i2);
					chooseDialog.dismiss();
				}
	        	
	        });
	    
	        chooseDialog.setCanceledOnTouchOutside(true);
	        chooseDialog.show();
		}
	
	/**
	 * custom the searchview
	 * @param view
	 * @param index
	 */
	private void traverseView(View view, int index) {
	    if (view instanceof SearchView) {
	        SearchView v = (SearchView) view;
	        for(int i = 0; i < v.getChildCount(); i++) {
	            traverseView(v.getChildAt(i), i);
	        }
	    } else if (view instanceof LinearLayout) {
	        LinearLayout ll = (LinearLayout) view;
	        for(int i = 0; i < ll.getChildCount(); i++) {
	            traverseView(ll.getChildAt(i), i);
	        }
	    } else if (view instanceof EditText) {
	        ((EditText) view).setTextColor(Color.WHITE);
	        ((EditText) view).setHintTextColor(Color.WHITE);
	    } else if (view instanceof TextView) {
	        ((TextView) view).setTextColor(Color.WHITE);
	    } else if (view instanceof ImageView) {
	        // TODO dissect images and replace with custom images
	    } else {
	        Log.v("View Scout", "Undefined view type here...");
	    }
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		User.clearUser();
		User.clearTaList();
		if(receiver != null) unregisterReceiver(receiver);
		super.onDestroy();
	}

//	@Override
//	public boolean onKeyUp(int keyCode, KeyEvent event) {
//		// TODO Auto-generated method stub
//		if(keyCode == KeyEvent.KEYCODE_BACK) {
//			
//		}
//		return super.onKeyUp(keyCode, event);
//	}
	
	LoadingDialogReceiver receiver;
	int tabLoaded = 0;
	public static final String ACTION_FIRST_LOADING = "action_first_loading";
	private ProgressDialog pdialog;
	
	private void registerReceiver(){
		receiver = new LoadingDialogReceiver();  
        IntentFilter filter=new IntentFilter();  
        filter.addAction(ACTION_FIRST_LOADING);

        //动态注册BroadcastReceiver  
        registerReceiver(receiver, filter); 
	}
	
	/**
	 * 初始化加载
	 * @author tinyao
	 *
	 */
	public class LoadingDialogReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(ACTION_FIRST_LOADING)){
				tabLoaded++;
				if(tabLoaded == 3){
					// dismiss the loading dialog;
					pdialog.dismiss();
				}
			}
		}
		
	}

	
	// override the transition animation
	
	@Override
	public void startActivity(Intent intent) {
		// TODO Auto-generated method stub
		super.startActivity(intent);
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		// TODO Auto-generated method stub
		super.startActivityForResult(intent, requestCode);
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}
	
	long waitTime = 2500;  
	long touchTime = 0;	// 上一次按返回时间
	
	/**
	 * 再按一次退出
	 */
	@Override  
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
	    if(event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {  
	        long currentTime = System.currentTimeMillis();  
	        if((currentTime - touchTime) >= waitTime) {  
	            Toast.makeText(this, R.string.back_one_more, Toast.LENGTH_SHORT).show();  
	            touchTime = currentTime;
	        }else {  
	            finish();
	        }  
	        return true;  
	    }  
	    return super.onKeyDown(keyCode, event);  
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		mPager.setCurrentItem(itemPosition);
		return true;
	}  
	
}