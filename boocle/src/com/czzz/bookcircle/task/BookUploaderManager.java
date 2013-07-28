package com.czzz.bookcircle.task;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.data.UserBooksHelper;
import com.czzz.demo.R;
import com.czzz.demo.UploadBookDoneActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class BookUploaderManager implements TaskListener {
	
	private static final int UPLOAD_BOOKS = 120;
	
	Context context;
//	ArrayList<DoubanBook> books;
	ArrayList<BookCollection> newCollections;
	BookUploader uploader;
	NotificationManager mNotificationManager;
	
	int presentId = -1;
	
	public BookUploaderManager(Context context, ArrayList<BookCollection> collections){
		this.newCollections = collections;
		this.context = context;
		uploader = new BookUploader(this);
		String ns = Context.NOTIFICATION_SERVICE;
		sp = context.getSharedPreferences("account", 0);
		mNotificationManager = (NotificationManager) context.getSystemService(ns);
	}
	
	public void test(){
		presentId = 0;
		upload(newCollections.get(0));
	}
	
	public void upload(BookCollection entry){
		
		uploader.execute(entry);
		
		Log.d("DEBUG", "----------- new book uploading: " + entry.book.title);
		
		int icon = R.drawable.ic_launcher;        // icon from resources
		CharSequence tickerText = "正在提交: " + entry.book.title;              // ticker-text
		long when = System.currentTimeMillis();         // notification time
		CharSequence contentTitle = "添加书籍到我的书架";  // message title
		CharSequence contentText = "《" + entry.book.title + "》" + "(" + entry.book.author + ")";      // message text

		// the next two lines initialize the Notification, using the configurations above
		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText, null);
		notification.flags = Notification.FLAG_ONGOING_EVENT|Notification.FLAG_AUTO_CANCEL;
		//PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(
				context, 
		        R.string.app_name, 
		        new Intent(), 
		        PendingIntent.FLAG_UPDATE_CURRENT);
		notification.contentIntent = contentIntent;
		
		mNotificationManager.notify(UPLOAD_BOOKS, notification);
		
		uploader.start();
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		
		// 更新记录，将完成的书添加到本地缓存
		updateLocalUser("" + data, newCollections.get(presentId));
		
		presentId++;
		if (presentId < newCollections.size()) {
			// 继续上传下一本书
			uploader = new BookUploader(this);
			upload(newCollections.get(presentId));
		} else {
			// 全部结束，取消通知
			mNotificationManager.cancel(UPLOAD_BOOKS);

			// 发送broadcast更新书架UI
			Intent intent=new Intent();  
            intent.setAction("update_user_books");
            // create a list of BookCollection
            intent.putParcelableArrayListExtra("new_books", newCollections);
            
            context.sendBroadcast(intent);
		}
		
	}

	SharedPreferences sp;
	
	private void updateLocalUser(String result, BookCollection entry) {
		// TODO Auto-generated method stub
		
		if(newCollections == null){
			newCollections = new ArrayList<BookCollection>();
		}
		
		try {
			JSONObject json = new JSONObject(result);
			if(json.getInt("status") == 2){
				return;
			}
			if(json.getInt("status") == 3){
				Crouton.makeText((Activity) context, "你已经添加过" 
							+ "《" +  entry.book.title + "》", Style.ALERT).show();
//				Toast.makeText(context, "你已经添加过" 
//							+ "《" +  entry.book.title + "》", Toast.LENGTH_SHORT).show();
				newCollections.remove(entry);
				return;
			}
			
			JSONObject out = json.getJSONObject("data");
			JSONObject num = out.getJSONObject("num");
			JSONObject info = out.getJSONObject("book");
			
			User mUser = User.getInstance();
			mUser.book_total = Integer.valueOf(num.getString("book_total"));
			mUser.fav_total = Integer.valueOf(num.getString("fav_total"));
			
			sp.edit().putInt("book_total", mUser.book_total)
				.putInt("fav_total", mUser.fav_total).commit();
			
			// 更新书籍库
//			BookCollection entry = new BookCollection();
//			entry.book = book;
			entry.cid = Integer.valueOf(info.getString("id"));
			entry.owner_id = mUser.uid;
			entry.owner = mUser.name;
			entry.owner_avatar = mUser.avatar;
			entry.status = Integer.valueOf(info.getString("status"));
			entry.note = info.getString("note");
			entry.create_at = info.getString("create_at");
			
//			newCollections.add(entry);
			
			UserBooksHelper.getInstance(context).insert("", entry);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Crouton.makeText((Activity) context, data, Style.ALERT).show();
//		Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
		mNotificationManager.cancel(UPLOAD_BOOKS);
		
	}

}
