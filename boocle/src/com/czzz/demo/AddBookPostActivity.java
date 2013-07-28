package com.czzz.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.data.UserBooksHelper;
import com.czzz.douban.DoubanBook;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;
import com.czzz.utils.ImagesDownloader;
import com.czzz.utils.TextUtils;

public class AddBookPostActivity extends AsyncTaskActivity{

	private DoubanBook book;
	private BookCollection collection;
	private EditText noteEdt;
	private ImageView avatarImg;
	private TextView avatarTxt;
	private CheckBox checkBtn;
	private RatingBar ratingBar;
	
	private boolean isEdit = false;
	private boolean fromScan = false;
	private int listPos = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_book_post);
		mActionBar.setTitle(R.string.add_to_bookshelf);
		View view = (View)findViewById(R.id.add_book_post_bottom);
		noteEdt = (EditText)findViewById(R.id.add_book_note);
		avatarImg = (ImageView) findViewById(R.id.add_book_avatar);
		checkBtn = (CheckBox)findViewById(R.id.add_book_status_check);
		ratingBar = (RatingBar)findViewById(R.id.add_book_rating);
		avatarTxt = (TextView) findViewById(R.id.add_book_avatar_txt);
		
		Intent intent = getIntent();
		Bundle bnd = intent.getExtras();
		if(bnd.containsKey("editing"))
			isEdit = bnd.getBoolean("editing");
		if(isEdit) mActionBar.setTitle(R.string.edit_the_book);
		if(bnd.containsKey("collection")){
			collection = (BookCollection) bnd.getSerializable("collection");
			book = collection.book;
			if(collection.owner_id == User.getInstance().uid){
				noteEdt.append(collection.note);
				ratingBar.setRating(collection.score);
				checkBtn.setChecked(collection.status == 1);
			}
		}else if(bnd.containsKey("book")){
			book = (DoubanBook) bnd.getSerializable("book");
		}
		
		fromScan = bnd.getBoolean("from_scan", false);
		if(fromScan){
			mActionBar.setTitle(R.string.have_the_book);
			listPos = bnd.getInt("list_pos", 0);
			if(bnd.containsKey("book_note")){
				noteEdt.append(bnd.getString("book_note"));
			}
			ratingBar.setRating(bnd.getFloat("book_score", 0));
			checkBtn.setChecked(bnd.getInt("book_status", 0) == 1);
		}
		
		if(User.getInstance().avatar == null || User.getInstance().avatar.equals("")) {
			avatarTxt.setText(User.getInstance().name.substring(0, 1).toUpperCase(Locale.CHINA));
			avatarTxt.setVisibility(View.VISIBLE);
		} else {
			avatarTxt.setVisibility(View.GONE);
		}
		ImagesDownloader.getInstance().download(User.getInstance().avatar, avatarImg);
		
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(0, R.anim.exit_out_top);
			}
			
		});
		
		checkBtn.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "checked: " + isChecked);
			}
			
		});
		
	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getSupportMenuInflater().inflate(R.menu.menu_postbook, menu);
		if(fromScan){
			menu.findItem(R.id.menu_post_book).setVisible(false);
		}
		return super.onCreateOptionsMenu(menu);
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
		case R.id.menu_post_book:
			Log.d("DEBUG", "post check: " + checkBtn.isChecked());
			
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
			imm.hideSoftInputFromWindow(noteEdt.getWindowToken(), 0);
			
//			if(fromScan){
//				Intent backScan = new Intent();
//				backScan.putExtra("note", noteEdt.getText().toString());
//				backScan.putExtra("score", ratingBar.getRating());
//				backScan.putExtra("status", checkBtn.isChecked() ? 1 : 0);
//				backScan.putExtra("list_pos", listPos);
//                setResult(100, backScan); 
//                finish(); 
//                break;
//			}
			
			if(isEdit){
				updateBook(checkBtn.isChecked() ? 1 : 0, noteEdt.getText().toString(), ratingBar.getRating());
			}else{
				postBook(checkBtn.isChecked() ? 1 : 0, noteEdt.getText().toString(), ratingBar.getRating());
			}
			
			finish();
			
			break;
		}
		
		return false;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
		if(fromScan){
			Intent backScan = new Intent();
			backScan.putExtra("note", noteEdt.getText().toString());
			backScan.putExtra("score", ratingBar.getRating());
			backScan.putExtra("status", checkBtn.isChecked() ? 1 : 0);
			backScan.putExtra("list_pos", listPos);
            setResult(100, backScan); 
		}
		
		super.finish();
		overridePendingTransition(0, R.anim.exit_out_top);
	}
	

	/**
	 * 提交藏书：基本信息+备注+状态
	 */
	private void postBook(int status, String note, float rating) {
		// TODO Auto-generated method stub
		
		ArrayList<BookCollection> collectionsToAdd = new ArrayList<BookCollection>();
		
		BookCollection entry = new BookCollection();
		entry.book = book;
		entry.status = status;
		entry.note = TextUtils.removeEndEmptyLines(note);
		entry.score = rating;
		
		collectionsToAdd.add(entry);
		
		Log.d("DEBUG", "book: " + entry.book);
		
		Intent intent = new Intent( "bookcircle.task.upload_books" );
		intent.putParcelableArrayListExtra("upload_books", collectionsToAdd);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendBroadcast(intent);
		
	}
	
//	private void updateBook(int status, String note, float rating) {
//		// TODO Auto-generated method stub
//		
//		ArrayList<BookCollection> collectionsToAdd = new ArrayList<BookCollection>();
//		
//		BookCollection entry = new BookCollection();
//		entry.status = status;
//		entry.note = note;
//		entry.score = rating;
//		
//		collectionsToAdd.add(entry);
//		
//		Intent intent = new Intent( "bookcircle.task.update_books" );
//		intent.putParcelableArrayListExtra("update_book", collectionsToAdd);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        sendBroadcast(intent);
//		
//	}
	
	private void updateBook(final int status, final String note, final float rating) {
		
		BookCollection entry = new BookCollection();
		entry.status = status;
		entry.note = note;
		entry.score = rating;
		
		new Thread(){
			public void run(){
				List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
				params.add(new BasicNameValuePair("uid", ""+User.getInstance().uid));
				params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
				params.add(new BasicNameValuePair("id", "" + collection.cid));
				params.add(new BasicNameValuePair("status", "" + status));
				params.add(new BasicNameValuePair("note", note));
				params.add(new BasicNameValuePair("score", "" + rating));
				
				new HttpPostTask(AddBookPostActivity.this, 
						(HttpListener)AddBookPostActivity.this)
						.execute(Pref.UPDATE_ENTRY_URL, params);
				
				UserBooksHelper helper = new UserBooksHelper(AddBookPostActivity.this);
				helper.openDataBase();
				helper.update(collection.cid, status, note, rating);
				helper.close();
				
				// 发送广播，更新书架上的这本书
				Intent intent=new Intent();  
	            intent.setAction("update_user_book_item");
	            intent.putExtra("cid", collection.cid);
	            intent.putExtra("note", note);
	            intent.putExtra("status", status);
	            intent.putExtra("score", rating);
	            AddBookPostActivity.this.sendBroadcast(intent);
			}
		}.start();
		
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		Log.d("DEBUG", ""+data);
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			finish();
			overridePendingTransition(0, R.anim.exit_out_top);
			return false;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
}
