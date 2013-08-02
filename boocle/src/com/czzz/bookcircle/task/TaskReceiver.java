package com.czzz.bookcircle.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.bookcircle.DirectMsg;
import com.czzz.bookcircle.MsgThread;
import com.czzz.bookcircle.MsgUtils;
import com.czzz.bookcircle.MyApplication;
import com.czzz.bookcircle.UserUtils;
import com.czzz.demo.ConversationActivity;
import com.czzz.demo.R;
import com.czzz.utils.HttpListener;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class TaskReceiver extends BroadcastReceiver implements HttpListener{

	private Context context;
	private final static int NEW_MSG_NOTI = 1000;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		this.context = context;
		User user = User.getInstance();
		if(user.uid == 0){
			user.init(context);
		}
		
		String action = intent.getAction();
		
		// 上传书籍任务
		if (action.equals("bookcircle.task.upload_books")) {
			ArrayList<BookCollection> collections = intent.getParcelableArrayListExtra("upload_books");
			new BookUploaderManager(context, collections).test();
		}
		
		// 发送消息任务
		if (action.equals("bookcircle.task.send_msg")) {
			int recver_id = intent.getIntExtra("recver_id", 0);
			String body = intent.getStringExtra("body");
			int book_id = intent.getIntExtra("book_id", 0);
			MsgManager manager = MsgManager.getInstance(context); 
			manager.sendDirectMsg(recver_id, body, book_id);
		}
		
		// 定时检测新消息
		if (action.equals("bookcircle.task.check_msg")){
			SharedPreferences sp = context.getSharedPreferences("config", 0);
			boolean firstAlarm = !sp.contains("first_alarm");
			if(firstAlarm){
				sp.edit().putBoolean("first_alarm", false).commit();
			}else{
				checkNewMsg();
			}
		}
		
		// 登录完成后，进行任务加载
		if (action.equals("bookcircle.task.first_launch")){
			
		}
		
		// 开机启动, 重新设置Alarm
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			AlarmTask.setMsgAlarm(context);
		}
		
		if (action.equals("bookcircle.task.download_apk")){
			
			if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {  
                //如果没有SD卡  
                Builder builder = new Builder(context);  
                builder.setTitle("提示");  
                builder.setMessage("当前设备无SD卡，数据无法下载");  
                builder.setPositiveButton("", new OnClickListener() {  
                    @Override  
                    public void onClick(DialogInterface dialog, int which) {  
                        dialog.dismiss();  
                    }  
                });  
                builder.show();  
                return;  
            }else{
//            	pd = new ProgressDialog(context);
//            	pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//            	pd.setMax(100);
//            	pd.show();
            	downUrl = intent.getStringExtra("download_url");
            	
            	Log.d("DEBUG", "download: " + downUrl);
            	updateDownloadApkNoti(0);
            	Thread downLoadThread = new Thread(downApkRunnable);
                downLoadThread.start(); 
            }

		}
		
	}
	
	String downUrl;
	
	/**
	 * 检测新消息
	 */
	private void checkNewMsg(){
		SharedPreferences sp = context.getSharedPreferences("config", 0);
		if(sp.getBoolean("first_launch", true)){
			sp.edit().putBoolean("first_launch", false).commit();
			return;
		}
		UserUtils.checkRemoteUnreadMsg(context, new AsyncHttpResponseHandler(){

			@Override
			public void onSuccess(int resCode, String data) {
				// TODO Auto-generated method stub
				super.onSuccess(resCode, data);
				Log.d("DEBUG", "new msg: " + data);
				JSONObject json;
				ArrayList<DirectMsg> msgs = null;
				try {
					json = new JSONObject(""+data);
					if(json.getInt("status") == 3){
//						Toast.makeText(context, "没有新消息", Toast.LENGTH_SHORT).show();
						return;
					}
					msgs = UserUtils.parseDMsg(""+data);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(msgs == null) return;
				
				// 保存消息
				UserUtils.saveMsgs(context, msgs);
				
				if(MyApplication.isChatActivityVisible() || MyApplication.isMsgFragmentVisible()){
					Intent updateMsglistIntent = new Intent("bookcircle.task.load_msglist_new");
					updateMsglistIntent.putParcelableArrayListExtra("new_msgs", msgs);
					context.sendOrderedBroadcast(updateMsglistIntent, null);
				}else{
					// 状态栏通知
//					notifyNewMsgs(msgs);
					MsgUtils.notifyNewMsgs(context, msgs);
				}
				
				Intent updateMsgUnreadIntent = new Intent("bookcircle.task.new_msg_update_home");
				updateMsgUnreadIntent.putParcelableArrayListExtra("new_msgs", msgs);
				context.sendOrderedBroadcast(updateMsgUnreadIntent, null);
				
			}
			
		});
	}
	
//	/**
//	 * 显示状态栏通知
//	 * @param thread
//	 */
//	private void notifyStatusBar(MsgThread thread){
//		
//		NotificationCompat.Builder mBuilder =
//		        new NotificationCompat.Builder(context)
//			.setContentTitle("@" + thread.thread_name 
//				+ "给你的新私信【"+ thread.unread_count + "】")
//		    .setContentText(thread.msg_body)
//		    .setSmallIcon(R.drawable.ic_launcher)
//		    .setTicker(thread.thread_name + "发来" + thread.unread_count + "条私信")
//		    .setNumber(thread.unread_count)
//		    .setAutoCancel(true)
//		    .setDefaults(Notification.DEFAULT_SOUND);
//		
//		Intent resultIntent = new Intent(context, ConversationActivity.class);
//		resultIntent.putExtra("thread_uid", thread.thread_uid);
//		TaskStackBuilder stackBuilder = TaskStackBuilder.from(context);
//		// Adds the back stack
//		stackBuilder.addParentStack(ConversationActivity.class);
//		// Adds the Intent to the top of the stack
//		stackBuilder.addNextIntent(resultIntent);
//		// Gets a PendingIntent containing the entire back stack
//		PendingIntent resultPendingIntent =
//		        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//		mBuilder.setContentIntent(resultPendingIntent);
//		NotificationManager mNotificationManager =
//		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//		// mId allows you to update the notification later on.
//		mNotificationManager.notify(NEW_MSG_NOTI + thread.thread_uid, mBuilder.getNotification());
//		
//	}
	
//	/**
//	 * 根据获取的新消息，返回thread,用于更新通知
//	 * @param msgs
//	 */
//	private void notifyNewMsgs(ArrayList<DirectMsg> msgs) {
//		// TODO Auto-generated method stub
//		
//		ArrayList<Integer> uids = new ArrayList<Integer>();
//		ArrayList<MsgThread> notis = new ArrayList<MsgThread>();
//		
//		for(DirectMsg item : msgs){
//			if(uids.contains(item.sender_id)){
//				// 已有发送者
//				int index = uids.indexOf(item.sender_id);
//				MsgThread enty = notis.get(index);
//				enty.unread_count = enty.unread_count + 1;
//				if(enty.msg_id < item.msg_id){
//					// 更近的消息
//					enty.msg_body = item.body;
//					enty.msg_id = item.msg_id;
//				}
//			}else{
//				// 新发送者
//				MsgThread thread = new MsgThread();
//				thread.unread_count = 1;
//				thread.thread_name = item.thread_name;
//				thread.thread_uid = item.thread_uid;
//				thread.msg_body = item.body;
//				thread.msg_id = item.msg_id;
//				notis.add(thread);
//				uids.add(item.sender_id);
//			}
//		}
//		
//		for(MsgThread th : notis){
//			notifyStatusBar(th);
//		}
		
//		ArrayList<MsgThread> threads = MsgThreadHelper
//				.getInstance(context).getCachedThread();
//		for(MsgThread th : threads){
//			if(th.unread_count != 0){
//				notifyStatusBar(th);
//			}
//		}
//	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		Log.d("DEBUG", "new msg: " + data);
		JSONObject json;
		ArrayList<DirectMsg> msgs = null;
		try {
			json = new JSONObject(""+data);
			if(json.getInt("status") == 3){
//				Toast.makeText(context, "没有新消息", Toast.LENGTH_SHORT).show();
				return;
			}
			msgs = UserUtils.parseDMsg(""+data);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(msgs == null) return;
		
		// 保存消息
		UserUtils.saveMsgs(context, msgs);
		
		if(MyApplication.isChatActivityVisible() || MyApplication.isMsgFragmentVisible()){
			Intent updateMsglistIntent = new Intent("bookcircle.task.load_msglist_new");
			updateMsglistIntent.putParcelableArrayListExtra("new_msgs", msgs);
			context.sendOrderedBroadcast(updateMsglistIntent, null);
		}else{
			// 状态栏通知
//			notifyNewMsgs(msgs);
			MsgUtils.notifyNewMsgs(context, msgs);
		}
		
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		// 因为是后台更新，所以不提示错误信息
//		Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
	}
	
	NotificationManager downNotificationManager;
	Notification downNotification;
	
	private void updateDownloadApkNoti(int progres){
		
		int icon = R.drawable.ic_launcher;        // icon from resources
		CharSequence tickerText = "正在下载更新";              // ticker-text
		long when = System.currentTimeMillis();         // notification time
		CharSequence contentTitle = "书圈-新版本";  // message title
		CharSequence contentText = "正在下载更新" + progres + "%";      // message text

		// the next two lines initialize the Notification, using the configurations above
		downNotification = new Notification(icon, tickerText, when);
		downNotification.setLatestEventInfo(context, contentTitle, contentText, null);
		downNotification.flags = Notification.FLAG_ONGOING_EVENT|Notification.FLAG_AUTO_CANCEL;
		//PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(
				context, 
		        R.string.app_name, 
		        new Intent(), 
		        PendingIntent.FLAG_UPDATE_CURRENT);
		downNotification.contentIntent = contentIntent;
		
		downNotificationManager = (NotificationManager) 
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		downNotificationManager.notify(302, downNotification);
	}
	
	String apkFile;
	
	/** 
     * 从服务器下载新版apk的线程 
     */  
    private Runnable downApkRunnable = new Runnable(){
    	
        @Override  
        public void run() {  
            
        	int downloadCount = 0;
            int currentSize = 0;
            long totalSize = 0;
            int updateTotalSize = 0;
             
            HttpURLConnection httpConnection = null;
            InputStream is = null;
            FileOutputStream fos = null;
             
            try {
                URL url = new URL(downUrl);
                
                String apkName = downUrl.substring(downUrl.lastIndexOf("/"));
                
                apkFile = Environment.getExternalStorageDirectory()
                		.getAbsolutePath() + "/download/" + apkName;  
                File ApkFile = new File(apkFile);
                
                httpConnection = (HttpURLConnection)url.openConnection();
                httpConnection.setRequestProperty("User-Agent", "PacificHttpClient");
                if(currentSize > 0) {
                    httpConnection.setRequestProperty("RANGE", "bytes=" + currentSize + "-");
                }
                httpConnection.setConnectTimeout(10000);
                httpConnection.setReadTimeout(20000);
                updateTotalSize = httpConnection.getContentLength();
                if (httpConnection.getResponseCode() == 404) {
                    throw new Exception("fail!");
                }
                is = httpConnection.getInputStream();    
                
                fos = new FileOutputStream(ApkFile, false);
                byte buffer[] = new byte[4096];
                int readsize = 0;
                while((readsize = is.read(buffer)) > 0){
                    fos.write(buffer, 0, readsize);
                    totalSize += readsize;
                    
                    Log.d("DEBUG", "totalSize: " + totalSize
                    		+ "\nupdateTotalSize: " + updateTotalSize);
                    
                    if(totalSize == updateTotalSize){
                        mHandler.sendEmptyMessage(0);
                        return;
                    }
                    
                    //为了防止频繁的通知导致应用吃紧，百分比增加10才通知一次
                    if((downloadCount == 0) || 
                    		(int) (totalSize*100/updateTotalSize) - 5 > downloadCount){ 
                        downloadCount += 5;
                        Message mmsg = mHandler.obtainMessage();
                        mmsg.what = 1;
                        mmsg.arg1 = (int) (totalSize*100/updateTotalSize);
                        mHandler.sendMessage(mmsg);
                    }
                    
                }
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
                if(httpConnection != null) {
                    httpConnection.disconnect();
                }
                if(is != null) {
                    try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                if(fos != null) {
                    try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
            }
              
        }  
    }; 
    
    private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case 0:
				downNotificationManager.cancel(302);
				
				Intent intent = new Intent(Intent.ACTION_VIEW);  
			    intent.setDataAndType(Uri.parse("file://"  
			                + apkFile ),  
			                "application/vnd.android.package-archive");  
				
				PendingIntent contentIntent = PendingIntent.getActivity(
						context, 
				        R.string.app_name, 
				        intent, 
				        PendingIntent.FLAG_UPDATE_CURRENT);
				
				Notification updateNotification = new Notification();
			    //设置通知栏显示内容
			    updateNotification.icon = R.drawable.ic_launcher;
			    updateNotification.tickerText = "下载完成";
			    updateNotification.setLatestEventInfo(context,"书圈-新版本", 
			    		"下载完成 100%: " + apkFile, contentIntent);
			    //发出通知
			    downNotificationManager.notify(203, updateNotification);
			    
			    Intent insIntent = new Intent();
			    insIntent.setAction(android.content.Intent.ACTION_VIEW);
			    insIntent.setDataAndType(Uri.parse("file://" + apkFile), 
			    		"application/vnd.android.package-archive");
			    insIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    context.startActivity(insIntent);
				
				break;
			case 1:
				if(msg.arg1 == 100){
					downNotificationManager.cancel(302);
				}else{
					updateDownloadApkNoti(msg.arg1);
				}
				break;
			}
			super.handleMessage(msg);
		}
    	
    };
}
