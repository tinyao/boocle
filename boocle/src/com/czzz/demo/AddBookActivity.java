package com.czzz.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.ActionBar;
import com.czzz.base.BaseActivity;
import com.czzz.douban.DoubanBookUtils;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class AddBookActivity extends BaseActivity{

	private ActionBar mActionBar;
	private Button majorBooks, isbnBtn, doubanBtn;
	private EditText searchEdt;
	
	private boolean hasEntered = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mActionBar = getSupportActionBar();

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayUseLogoEnabled(false);
		
		setContentView(R.layout.add_book);
		
		searchEdt = (EditText)findViewById(R.id.search_edt);
		isbnBtn = (Button)findViewById(R.id.add_scan_isbn);
		majorBooks = (Button)findViewById(R.id.add_major_books);
		doubanBtn = (Button)findViewById(R.id.add_douban_books);
		
		majorBooks.setOnClickListener(listener);
		
		isbnBtn.setOnClickListener(listener);
		
		doubanBtn.setOnClickListener(listener);
		
		searchEdt.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if(keyCode == KeyEvent.KEYCODE_ENTER && !hasEntered){
					String searchStr = searchEdt.getText().toString();
					if(DoubanBookUtils.isISBN(searchStr)){
						Intent i = new Intent(AddBookActivity.this, BookInfoActivity.class);
						i.putExtra("isbn", searchStr);
						startActivity(i);
					}else{
						Intent i = new Intent(AddBookActivity.this, BookSearchListActivity.class);
						i.putExtra("keyword", searchStr);
						startActivity(i);
					}
					InputMethodManager imm = (InputMethodManager)AddBookActivity.this
							.getSystemService(Context.INPUT_METHOD_SERVICE); 
					imm.hideSoftInputFromWindow(searchEdt.getWindowToken(), 0);
					hasEntered = true;
				}else{
					hasEntered = false;
				}
				return false;
			}
			
		});
	}
		
	OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.add_scan_isbn:
//				Intent i1 = new Intent(AddBookActivity.this, CaptureActivity.class);
				Intent i1 = new Intent("com.czzz.action.qrscan.addbook");
				startActivityForResult(i1,0);
				break;
			case R.id.add_major_books:
				Intent i2 = new Intent(AddBookActivity.this, MajorBooksActivity.class);
				startActivity(i2);
				break;
			case R.id.add_douban_books:
				Intent i3 = new Intent(AddBookActivity.this, DoubanImportActivity.class);
				startActivity(i3);
				break;
			}
		}
	};
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == Activity.RESULT_OK){
			String codeFormat = data.getStringExtra("bracode_format");
			String codeText = data.getStringExtra("bracode_text");
			
			if(codeFormat.contains("EAN")){
				searchEdt.setText(codeText);
				Intent i = new Intent(AddBookActivity.this, BookInfoActivity.class);
				i.putExtra("isbn", codeText);
				startActivity(i);
			}else{
				Crouton.makeText(AddBookActivity.this, "invalid isbn", Style.ALERT).show();
			}
			
		}
		
	}
	
//	@Override
//	public boolean onOptionsItemSelected(
//			com.actionbarsherlock.view.MenuItem item) {
//		// TODO Auto-generated method stub
//		
//		switch(item.getItemId()){
//		case R.id.abs__home:
//			finish();
//			break;
//		case android.R.id.home:
//			finish();
//			break;
//		}
//		
//		return super.onOptionsItemSelected(item);
//	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			finishWithResult();   
			return false;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public void finishWithResult(){
		Intent i = new Intent();  
        this.setResult(RESULT_OK, i);  
        this.finish();
	}
	
}
