package com.czzz.view;

import java.util.ArrayList;

import com.czzz.demo.R;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.czzz.base.User;
import com.czzz.data.SchoolDBHelper;

public class SchoolPopupDialog extends Dialog{

	private Context context;
	public String updateUniversity;
	public int updateUnivId = -1;
	private int y;
	
	public boolean changed = false;
	
    public SchoolPopupDialog(Context context, int style, int y) {
		super(context, style);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.y = y;
		initData();
	}
    

	// Scrolling flag
    private boolean scrolling = false;
    String provinces[] = new String[35];
    String universities[][] = new String[35][];
    
    private Button okBtn, ignoreBtn;
    private TextView backMy;
    
    WheelView pronWheel;
    WheelView univWheel;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.school_pop_dialog);
        
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = y;
        
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        dialogWindow.setAttributes(lp);
        this.setCanceledOnTouchOutside(true);
        
        pronWheel = (WheelView) findViewById(R.id.country);
        univWheel = (WheelView) findViewById(R.id.city);
        okBtn = (Button) findViewById(R.id.profile_change_school_ok);
        ignoreBtn = (Button) findViewById(R.id.profile_change_school_cancel);
        backMy = (TextView) findViewById(R.id.back_to_my_school);
        
        okBtn.setOnClickListener(listener);
        ignoreBtn.setOnClickListener(listener);
        backMy.setOnClickListener(listener);
        
		pronWheel.setVisibleItems(7);
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

        changed = false;
        
        int schoolId = updateUnivId == -1 ? User.getInstance().school_id : updateUnivId;
        
        if(schoolId == 0){
        	pronWheel.setCurrentItem(0);
            univWheel.setCurrentItem(0);
        }else{
        	int provinceIndex= schoolId / 100 - 10;
            int schoolIndex = schoolId % 100;
            pronWheel.setCurrentItem(provinceIndex + 1);
            univWheel.setCurrentItem(schoolIndex);
        }
        
        Log.d("DEBUG", "set item");
        
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
				SchoolPopupDialog.this.dismiss();
				break;
			case R.id.back_to_my_school:
				int provinceIndex= User.getInstance().school_id / 100 - 10;
	            int schoolIndex = User.getInstance().school_id % 100;
	            pronWheel.setCurrentItem(provinceIndex + 1);
	            univWheel.setCurrentItem(schoolIndex);
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
		
		updateUniversity = schoolName;
		updateUnivId = schoolId;
		changed = true;
		SchoolPopupDialog.this.cancel();
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
		int row = 1;
		// convert array to vector
		for(ArrayList<String> unvs: schools) {
			tempRow = unvs.toArray(new String[]{});
			universities[row] = tempRow;
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
        city.setCurrentItem(Math.min(cities[index].length / 2, 2));        
    }

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		if(!changed){	//没有确定，还原学校
			int schoolId = updateUnivId == -1 ? User.getInstance().school_id : updateUnivId;
	        int provinceIndex= schoolId / 100 - 10;
	        int schoolIndex = schoolId % 100;
	        pronWheel.setCurrentItem(provinceIndex + 1) ;
	        univWheel.setCurrentItem(schoolIndex);
		}
	}
	
}
