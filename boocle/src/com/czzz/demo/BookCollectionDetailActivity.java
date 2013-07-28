package com.czzz.demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.czzz.base.BaseActivity;
import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.data.UserBooksHelper;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;
import com.czzz.utils.ImagesDownloader;
import com.czzz.utils.TextUtils;
import com.czzz.view.tablelist.widget.UITableView;
import com.czzz.view.tablelist.widget.UITableView.ClickListener;

public class BookCollectionDetailActivity extends BaseActivity implements HttpListener{

	UITableView bookTable;
	BookCollection entry;
	private TextView ownerTv; 
	View gotoProfile, gotoProfileBtn;
	private ImageView ownerAvatar;
	
	boolean newProfile = false;
	boolean fullInfo = false;
	boolean fromBookInfo = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.book_collection_detail);
		
		Intent data = getIntent();
		
		entry = (BookCollection) data.getSerializableExtra("book");
		
		if(data.hasExtra("from_explore")) newProfile = true;
		if(data.hasExtra("from_userpage")) newProfile = true;
		if(data.hasExtra("full")) fullInfo = true;
		
		bookTable = (UITableView) findViewById(R.id.book_collection_info);
		gotoProfile = (View) findViewById(R.id.goto_profile);
		gotoProfileBtn = (View) findViewById(R.id.goto_profile_btn);
		ownerTv = (TextView) findViewById(R.id.book_collection_owner);
		ownerAvatar = (ImageView) findViewById(R.id.book_collection_owner_avatar);
		Log.d("DEBUG", "avatar: " + entry.owner_avatar);
		new ImagesDownloader().download(entry.owner_avatar, ownerAvatar);
		ownerTv.setText(entry.owner);
		
		createBookTable();
		
		gotoProfile.setOnClickListener(listener);
		gotoProfileBtn.setOnClickListener(listener);
		
	}
	
	private OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if(!newProfile) {
				finish();
				return;
			}
			Intent ii = new Intent(BookCollectionDetailActivity.this, UserPageActivity.class);
			User user = new User();
			user.name = entry.owner;
			user.uid = entry.owner_id;
			user.avatar = entry.owner_avatar;
			ii.putExtra("user", user);
			ii.putExtra("from_book_detail", true);
			startActivity(ii);
		}
		
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getSupportMenuInflater().inflate(R.menu.menu_collect_detail, menu);

		if(entry.owner_id == User.getInstance().uid){
			menu.removeItem(R.id.menu_collect_msg);
		}else{
			menu.removeItem(R.id.menu_collect_delete);
		}
		
		return super.onCreateOptionsMenu(menu);
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.menu_collect_msg:
			// 私信
			
			if(entry.owner_id == User.getInstance().uid){
				Toast.makeText(this, "亲，不用给自己私信了吧", Toast.LENGTH_SHORT).show();
				break;
			}
			
			Intent toMsgIntent = new Intent(this, ConversationActivity.class);
			toMsgIntent.putExtra("thread_uid", entry.owner_id);
			toMsgIntent.putExtra("thread_name", entry.owner);
			toMsgIntent.putExtra("thread_avatar", entry.owner_avatar);
			toMsgIntent.putExtra("book_title", entry.book.title);
			startActivity(toMsgIntent);
			break;
		case R.id.menu_collect_delete:
			// 删除藏书
			AlertDialog mDialog = new AlertDialog.Builder(this)
				.setTitle("确定要删除这本藏书么？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						deleteCollection();
					}
					
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				}).create();
			mDialog.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void createBookTable() {
		// TODO Auto-generated method stub
		bookTable.addBasicItem("书名:", "《" + entry.book.title + "》");
		bookTable.addBasicItem("作者:", entry.book.author, false, true);
		bookTable.addBasicItem("出版:", entry.book.publisher, false, true);
		bookTable.addBasicItem("状态:", entry.status==1 ? "二手" : "非二手", 
				User.getInstance().uid == entry.owner_id, true);
		
		bookTable.addBasicItem("备注:", 
				entry.note, 
				"可填写价格/新旧等信息..." ,
				User.getInstance().uid == entry.owner_id, false);
		
		bookTable.addBasicItem("时间:", TextUtils.formatSmartTime(entry.create_at), false, true);
		bookTable.commit();
		
		CustomClickListener listener = new CustomClickListener();
		bookTable.setClickListener(listener);
	}
	
	private class CustomClickListener implements ClickListener {

		@Override
		public void onClick(int index) {
			// TODO Auto-generated method stub
			switch(index){
			case 0:
				Intent bookIntent = new Intent(BookCollectionDetailActivity.this,
						BookInfoActivity.class);
				bookIntent.putExtra("notscan", true);
				if(fullInfo){
					bookIntent.putExtra("book", (Serializable)entry.book);
				}else{
					bookIntent.putExtra("isbn", entry.book.isbn13);
				}
				startActivity(bookIntent);
				break;
			case 3: // 修改状态
				showStatusDialog();
				break;
			case 4: // 修改备注
				showUpdateDialog();
				break;
			}
		}
		
	}
	
	private void deleteCollection(){
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("uid", ""+User.getInstance().uid));
		params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
		params.add(new BasicNameValuePair("isbn", "" + entry.book.isbn13));
		
		new HttpPostTask(BookCollectionDetailActivity.this, 
				(HttpListener)BookCollectionDetailActivity.this)
			.execute(Pref.DELETE_ENTRY_URL, params);
		
		UserBooksHelper mhelper = UserBooksHelper.getInstance(this);
		mhelper.openDataBase();
		mhelper.delete(entry.cid);
		
		User.getInstance().book_total--;
		SharedPreferences sp = this.getSharedPreferences("account", 0);
		sp.edit().putInt("book_total", User.getInstance().book_total).commit();
		
		// 更新书架UI，删除书籍
		Intent ii = new Intent("delete_user_book_item");
		ii.putExtra("cid", entry.cid);
		this.sendBroadcast(ii);
		
		finish();
	}

	public void showStatusDialog(){  
		   
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);  
	    
	    builder.setSingleChoiceItems(R.array.entry_state, entry.status, new DialogInterface.OnClickListener() {  
	   
	        @Override  
	        public void onClick(DialogInterface dialog, int which) {  
	            //which是选中的位置(基于0的)  
	            dialog.dismiss();
	            entry.status = which;
	            bookTable.update(3, which == 0 ? "非二手" : "二手"); // 更新列表显示
				updateEntry();
	        }  
	    }); 

	    AlertDialog ad = builder.create();
	    ad.setCanceledOnTouchOutside(true);
	    ad.show();  
	}  
	
	private void showUpdateDialog(){
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_profile_edit, null);
        final EditText edt = (EditText) view.findViewById(R.id.profile_dialog_edit);
        edt.setText(entry.note);
        edt.setSelection(entry.note.length());
        AlertDialog editDialog = new AlertDialog.Builder(this)
	    	.setTitle("修改备注").setView(view)
	    	.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					entry.note = edt.getText().toString();
					bookTable.update(4, entry.note);
					updateEntry();
				}
	    	})
	    	.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
	    	}).create();
    
	    editDialog.setCanceledOnTouchOutside(true);
	    editDialog.show();
	}
	
	private void updateEntry(){
		
		new Thread(){
			public void run(){
				List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
				params.add(new BasicNameValuePair("uid", ""+User.getInstance().uid));
				params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
				params.add(new BasicNameValuePair("id", "" + entry.cid));
				params.add(new BasicNameValuePair("status", "" + entry.status));
				params.add(new BasicNameValuePair("note", entry.note));
				
				new HttpPostTask(BookCollectionDetailActivity.this, 
						(HttpListener)BookCollectionDetailActivity.this).execute(Pref.UPDATE_ENTRY_URL, params);
				
				UserBooksHelper helper = new UserBooksHelper(BookCollectionDetailActivity.this);
				helper.openDataBase();
				helper.update(entry.cid, entry.status, entry.note, 0);
				helper.close();
				
				// 发送广播，更新书架上的这本书
				Intent intent=new Intent();  
	            intent.setAction("update_user_book_item");
	            intent.putExtra("cid", entry.cid);
	            intent.putExtra("note", entry.note);
	            intent.putExtra("status", entry.status);
	            BookCollectionDetailActivity.this.sendBroadcast(intent);
			}
		}.start();
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			finish();
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
	}
	
	
	
}
