package com.czzz.demo;

import java.util.ArrayList;
import java.util.List;

import com.czzz.demo.R;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.bookcircle.MyApplication;
import com.czzz.data.SchoolDBHelper;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class SchoolChooserActivity extends AsyncTaskActivity{

    // Scrolling flag
    private boolean scrolling = false;
    String provinces[] = new String[34];
    String universities[][] = new String[34][];
    
    private Button okBtn, ignoreBtn;
    
    String name, passwd;
    
    WheelView pronWheel;
    WheelView univWheel;
    
    ProgressDialog pd;
    private int schoolId;
    private String schoolName;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.school_chooser);

        pronWheel = (WheelView) findViewById(R.id.country);
        univWheel = (WheelView) findViewById(R.id.city);
        okBtn = (Button) findViewById(R.id.school_ok_btn);
        ignoreBtn = (Button) findViewById(R.id.schoool_ignore_btn);
        
        okBtn.setOnClickListener(listener);
        ignoreBtn.setOnClickListener(listener);
        
//        TextView feedback = (TextView) findViewById(R.id.about_feedback);
//        feedback.setOnClickListener(new OnClickListener(){
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Intent feed = new Intent(SchoolChooserActivity.this, FeedbackActivity.class);
//				feed.putExtra("feed_hint", "如果没有找到您所在的学校，请在这里留言～\n我们会根据需要，将尽快支持您所在的学校!");
//				startActivity(feed);
//			}
//        	
//        });
    }
    
    private OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.school_ok_btn:
				pd = new ProgressDialog(SchoolChooserActivity.this);
				pd.setMessage("请稍等...");
				pd.show();
				updateSchool();
				break;
			case R.id.schoool_ignore_btn:
				Intent feed = new Intent(SchoolChooserActivity.this, FeedbackActivity.class);
				feed.putExtra("feed_hint", "如果没有找到您所在的学校，请在这里留言～\n我们会根据需要，将尽快支持您所在的学校!");
				startActivityForResult(feed, 0);
				break;
			}
		}
    	
    };
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initData();
        
		pronWheel.setVisibleItems(6);
        ArrayWheelAdapter<String> cadapter =
                new ArrayWheelAdapter<String>(this, provinces);
        cadapter.setTextSize(18);
        pronWheel.setViewAdapter(cadapter);

        pronWheel.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
			    if (!scrolling) {
			        updateCities(univWheel, universities, newValue);
			    }
			}
		});
        
        // set country scroll listener
        pronWheel.addScrollingListener( new OnWheelScrollListener() {
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }
            public void onScrollingFinished(WheelView wheel) {
            	// update the cities
                scrolling = false;
                updateCities(univWheel, universities, pronWheel.getCurrentItem());
            }
        });

        int provinceIndex= DEFAULT_UNIV / 100 - 10;
        int schoolIndex = DEFAULT_UNIV % 100;
        pronWheel.setCurrentItem(provinceIndex);
        univWheel.setCurrentItem(schoolIndex);
//        pronWheel.setCurrentItem(33 - 10);
        
        
	}
    
    private final static int DEFAULT_UNIV = 3303;

	protected void updateSchool() {
		// TODO Auto-generated method stub
		int provIndex = pronWheel.getCurrentItem();
		int univIndex = univWheel.getCurrentItem();
		schoolId = (provIndex==0 ? provIndex : (provIndex + 10) * 100 + univIndex);
		schoolName = universities[provIndex][univIndex];
		
		Log.d("DEBUG", "schoolId: " + schoolId + "---" + schoolName);
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("uid", "" + User.getInstance().uid));
		params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
		params.add(new BasicNameValuePair("school_id", "" + schoolId));
		params.add(new BasicNameValuePair("school_name", "" + schoolName));
		
		new HttpPostTask(this, (HttpListener)this).execute(Pref.USER_INFO_UPDATE, params);
	}

	private void initData() {
		// TODO Auto-generated method stub
    	String DATABASE_PATH = "/data/data/" + "com.czzz.demo" + "/lib/";              
		SQLiteDatabase db = SQLiteDatabase.openDatabase(
				DATABASE_PATH + "libschool.db.so", null, 
				SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		
		SchoolDBHelper helper = new SchoolDBHelper(db);
		Cursor cursor = helper.select();
		
		ArrayList<String> schoolByProvince = null;
	    ArrayList<ArrayList<String>> schools = new ArrayList<ArrayList<String>>();
		
		int pcount = -1;
		int prePronvince = 0;
		
		while(cursor.moveToNext()){
			
			int provinceId = cursor.getInt(cursor.getColumnIndexOrThrow(SchoolDBHelper.PROVINCE_ID));
			if (provinceId != prePronvince){
				// new province
				pcount++;
				if (schoolByProvince != null){
					schools.add(schoolByProvince);
				}
				provinces[pcount] = cursor.getString(
						cursor.getColumnIndexOrThrow(SchoolDBHelper.PROVINCE_NAME));
				Log.d("DEBUG", "==========================");
				Log.d("DEBUG", "pcount: " + pcount + "--" + provinces[pcount]);
				prePronvince = provinceId;
				schoolByProvince = new ArrayList<String>();
			} 
			
			schoolByProvince.add(cursor.getString(cursor.getColumnIndexOrThrow(
					SchoolDBHelper.SCHOOL_NAME)));
			
			if (cursor.isLast()) {
				schools.add(schoolByProvince);
			}
		}
		
		String[] tempRow;
		int row = 0;
		// convert array to vector
		for(ArrayList<String> unvs: schools) {
			tempRow = unvs.toArray(new String[]{});
			universities[row] = tempRow;
			row++;
		}
		
		schoolByProvince.clear();schools.clear();
		schoolByProvince = null;
		schools = null;
	}

	/**
     * Updates the city wheel
     */
    private void updateCities(WheelView city, String cities[][], int index) {
        ArrayWheelAdapter<String> adapter =
            new ArrayWheelAdapter<String>(this, cities[index]);
        adapter.setTextSize(18);
        city.setViewAdapter(adapter);
        city.setCurrentItem(cities[index].length / 2);        
    }

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		Log.d("DEBUG", "update school..." + data);
		
		// save the school info
		User.getInstance().school_id = schoolId;
		User.getInstance().school_name = schoolName;
		SharedPreferences sp = this.getSharedPreferences("account", 0);
		sp.edit().putInt("school_id", schoolId).putString("school_name", schoolName).commit();
		
		Intent guideIntent = new Intent(this, HomeActivity.class);
		guideIntent.putExtra("new_login", true);
		MyApplication.configPref.edit().putBoolean("loged_in", true).commit();
		startActivity(guideIntent);
		pd.dismiss();
		this.finish();
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Crouton.makeText(this, data, Style.ALERT).show();
		pd.dismiss();
	}
	
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		case R.id.abs__home:
		case android.R.id.home:
			Crouton.makeText(this, "请选择您所在的高校", Style.CONFIRM).show();
			return false;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Crouton.makeText(this, "请选择您所在的高校", Style.CONFIRM).show();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == 100){
			// 跳过
			Intent guideIntent = new Intent(this, HomeActivity.class);
			guideIntent.putExtra("new_login", true);
			MyApplication.configPref.edit().putBoolean("loged_in", true).commit();
			startActivity(guideIntent);
			this.finish();
		}
		
	}
	
}
