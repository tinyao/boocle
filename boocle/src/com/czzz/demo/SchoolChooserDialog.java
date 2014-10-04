package com.czzz.demo;

import java.util.ArrayList;
import java.util.List;

import com.czzz.demo.R;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.data.SchoolDBHelper;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

@SuppressLint("Instantiatable")
public class SchoolChooserDialog extends Dialog implements HttpListener{

	private Context context;
	public String updateUniversity;
	public int updateUnivId = -1;

	protected SchoolChooserDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	public SchoolChooserDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    public SchoolChooserDialog(Context context, int style) {
		super(context, style);
		// TODO Auto-generated constructor stub
		this.context = context;
		initData();
	}
    

	// Scrolling flag
    private boolean scrolling = false;
    String provinces[] = new String[35];
    String universities[][] = new String[35][];
    
    private Button okBtn, ignoreBtn;
    
//    String name, passwd;
    
    WheelView pronWheel;
    WheelView univWheel;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.school_chooser_dialog);
        
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        this.setCanceledOnTouchOutside(true);
        
        pronWheel = (WheelView) findViewById(R.id.country);
        univWheel = (WheelView) findViewById(R.id.city);
        okBtn = (Button) findViewById(R.id.profile_change_school_ok);
        ignoreBtn = (Button) findViewById(R.id.profile_change_school_cancel);
        
        okBtn.setOnClickListener(listener);
        ignoreBtn.setOnClickListener(listener);
        
		pronWheel.setVisibleItems(6);
        ArrayWheelAdapter<String> cadapter =
                new ArrayWheelAdapter<String>(context, provinces);
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

        int schoolId = User.getInstance().school_id;
        
        Log.d("DEBUG", "school_id: " + schoolId);
        
        if(schoolId == 0){
        	pronWheel.setCurrentItem(0);
            univWheel.setCurrentItem(0);
        }else{
        	int provinceIndex= schoolId / 100 - 10;
            int schoolIndex = schoolId % 100;
            pronWheel.setCurrentItem(provinceIndex + 1);
            univWheel.setCurrentItem(schoolIndex);
        }
        
    }
    
    private View.OnClickListener listener = new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.profile_change_school_ok:
				updateSchool();
				break;
			case R.id.profile_change_school_cancel:
				SchoolChooserDialog.this.dismiss();
				break;
			}
		}
    	
    };
    
	protected void updateSchool() {
		// TODO Auto-generated method stub
		int provIndex = pronWheel.getCurrentItem();
		int univIndex = univWheel.getCurrentItem();
		int schoolId = (provIndex==0 ? provIndex : (provIndex - 1 + 10) * 100 + univIndex);
		String schoolName = universities[provIndex][univIndex];
		
		Log.d("DEBUG", "schoolId: " + schoolId + "---" + schoolName
				+ User.getInstance().name + User.getInstance().pass);
		
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("uid", "" + User.getInstance().uid));
		params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
		params.add(new BasicNameValuePair("school_id", "" + schoolId));
		params.add(new BasicNameValuePair("school_name", "" + schoolName));
		
		new HttpPostTask(context, (HttpListener)this).execute(Pref.USER_INFO_UPDATE, params);
		updateUniversity = schoolName;
		updateUnivId = schoolId;
		SchoolChooserDialog.this.cancel();
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
		
		int pcount = 0;
		int prePronvince = 0;
		
		provinces[0] = "全国";
		universities[0] = new String[]{"所有高校"};
		
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
			universities[row+1] = tempRow;
			row++;
		}
		
		cursor.close();db.close();
		schoolByProvince.clear();schools.clear();
		schoolByProvince = null;
		schools = null;
	}

	/**
     * Updates the city wheel
     */
    private void updateCities(WheelView city, String cities[][], int index) {
        ArrayWheelAdapter<String> adapter =
            new ArrayWheelAdapter<String>(context, cities[index]);
        adapter.setTextSize(18);
        city.setViewAdapter(adapter);
        city.setCurrentItem(cities[index].length / 2);        
    }

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		Log.d("DEBUG", "update school: " + data);
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Crouton.makeText((Activity) context, data, Style.ALERT).show();
	}
    
}
