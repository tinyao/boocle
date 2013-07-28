package com.czzz.view;

import com.czzz.demo.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class UserFilterDialog extends Dialog{

	private int y;
	
	public static final int FILTER_USER = 1;
	
	public UserFilterDialog(Context context, int theme, int y) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		this.y = y;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initUsersFilter();
	}

	public int sort = 2; // most
	public boolean filterChanged = false;
	
	RadioGroup sortFilter;
	private int sortCheckId = R.id._filter_user_most;
	
	private void initUsersFilter() {
		// TODO Auto-generated method stub
		setContentView(R.layout.filter_view_user);
		
		Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = y;
        
        dialogWindow.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        
        dialogWindow.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        
        dialogWindow.setAttributes(lp);
        this.setCanceledOnTouchOutside(true);
		
        sortFilter = (RadioGroup)findViewById(R.id._filter_user_sort);
		Button okBtn = (Button)findViewById(R.id._filter_books_ok);
		
		sortFilter.check(R.id._filter_user_most);
		
		okBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				filterChanged = true;
				
				sortCheckId = sortFilter.getCheckedRadioButtonId();
				
				switch(sortCheckId){
				case R.id._filter_user_random:
					sort = 0;
					break;
				case R.id._filter_user_latest:
					sort = 1;
					break;
				case R.id._filter_user_most:
					sort = 2;
					break;
				}
				
				Log.d("DEBUG", "---" + sort);
				UserFilterDialog.this.cancel();
			}
			
		});
		
	}

	
	@Override
	public void dismiss() {
		super.dismiss();
		if(!filterChanged){
			sortFilter.check(sortCheckId);
		}
	}
	
}
