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

public class SpinnerPopupDialog extends Dialog{

	ListView list;
	private Context context;
	private int y;
	private int type;
	private int width;
	
	public int selectId = -1;
	public String selectStr = ""; 
	
	String[] data;
	
	public static final int FILTER_BOOKS = 0;
	public static final int FILTER_USER = 1;
	public static final int SECTION_TYPE = 2;
	
	public SpinnerPopupDialog(Context context, int theme, int y, 
			int width, int type) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.y = y;
		this.width = width;
		this.type = type;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(type == SECTION_TYPE){
        	initSectionView();
        }else if (type == FILTER_BOOKS){
        	initBooksFilter();
        }
	}

	public int status = 2; // all
	public int sort = 0; // new
	public boolean filterChanged = false;
	
	private int statusCheckId = R.id._filter_books_all, sortCheckId = R.id._filter_books_latest;
	
	RadioGroup saleFilter, sortFilter;
	
	
	private void initBooksFilter() {
		// TODO Auto-generated method stub
		setContentView(R.layout.filter_view_books);
		
		Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = y;
        
        dialogWindow.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        
        dialogWindow.setAttributes(lp);
        this.setCanceledOnTouchOutside(true);
		
		saleFilter = (RadioGroup)findViewById(R.id._filter_books_sale);
		sortFilter = (RadioGroup)findViewById(R.id._filter_books_sort);
		Button okBtn = (Button)findViewById(R.id._filter_books_ok);
		
		saleFilter.check(R.id._filter_books_all);
		sortFilter.check(R.id._filter_books_latest);
		
//		saleFilter.setOnCheckedChangeListener(new OnCheckedChangeListener(){
//
//			@Override
//			public void onCheckedChanged(RadioGroup arg0, int arg1) {
//				// TODO Auto-generated method stub
//				filterChanged = true;
//			}
//			
//		});
//		
//		sortFilter.setOnCheckedChangeListener(new OnCheckedChangeListener(){
//
//			@Override
//			public void onCheckedChanged(RadioGroup arg0, int arg1) {
//				// TODO Auto-generated method stub
//				filterChanged = true;
//			}
//			
//		});
		
		okBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				filterChanged = true;
				
				statusCheckId = saleFilter.getCheckedRadioButtonId();
				sortCheckId = sortFilter.getCheckedRadioButtonId();
				
				switch(statusCheckId){
				case R.id._filter_books_all:
					status = 2;
					break;
				case R.id._filter_books_on_sale:
					status = 1;
					break;
				case R.id._filter_books_nonsale:
					status = 0;
					break;
				}
				
				switch(sortCheckId){
				case R.id._filter_books_latest:
					sort = 0;
					break;
				case R.id._filter_books_random:
					sort = 1;
					break;
				}
				
				Log.d("DEBUG", "---" + status + sort);
				SpinnerPopupDialog.this.cancel();
			}
			
		});
		
	}
	
	private void initSectionView() {
		// TODO Auto-generated method stub
		setContentView(R.layout.filter_list);
        
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = y;
        
        dialogWindow.setLayout(width, LayoutParams.WRAP_CONTENT);
        
        switch(type){
        	case 2:
        		dialogWindow.setGravity(Gravity.CLIP_HORIZONTAL | Gravity.BOTTOM);
        		data = new String[] { context.getResources().getString(R.string.nearby_books_label), 
        				context.getResources().getString(R.string.nearby_users_label), 
        				context.getResources().getString(R.string.douban_recomm_label) }; 
        		break;
        }
        
        dialogWindow.setAttributes(lp);
        this.setCanceledOnTouchOutside(true);
        
        list = (ListView)findViewById(R.id.filter_listview);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, 
        		R.layout.filter_list_item, 
        		R.id.filter_item, data);
        
        list.setAdapter(adapter);
        
        list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				selectId = arg2;
				selectStr = data[arg2];
				SpinnerPopupDialog.this.dismiss();
			}
        	
        });
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
		if(type == 0 && !filterChanged){
			saleFilter.check(statusCheckId);
			sortFilter.check(sortCheckId);
		}
	}
	
}
