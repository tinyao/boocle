package com.czzz.bookcircle;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.czzz.demo.ConversationActivity;
import com.czzz.demo.R;

public class MsgUtils {

	private final static int NEW_MSG_NOTI = 1000;
	
	static NotificationManager mNotificationManager;
	
	/**
	 * 根据获取的新消息，返回thread,用于更新通知
	 * @param msgs
	 */
	public static void notifyNewMsgs(Context context, ArrayList<DirectMsg> msgs) {
		// TODO Auto-generated method stub
		
		ArrayList<Integer> uids = new ArrayList<Integer>();
		ArrayList<MsgThread> notis = new ArrayList<MsgThread>();
		
		for(DirectMsg item : msgs){
			if(uids.contains(item.sender_id)){
				// 已有发送者
				int index = uids.indexOf(item.sender_id);
				MsgThread enty = notis.get(index);
				enty.unread_count = enty.unread_count + 1;
				if(enty.msg_id < item.msg_id){
					// 更近的消息
					enty.msg_body = item.body;
					enty.msg_id = item.msg_id;
				}
			}else{
				// 新发送者
				MsgThread thread = new MsgThread();
				thread.unread_count = 1;
				thread.thread_name = item.thread_name;
				thread.thread_uid = item.thread_uid;
				thread.msg_body = item.body;
				thread.msg_id = item.msg_id;
				notis.add(thread);
				uids.add(item.sender_id);
			}
		}
		
		for(MsgThread th : notis){
			notifyStatusBar(context, th);
		}
		
	}
	
	/**
	 * 显示状态栏通知
	 * @param thread
	 */
	private static void notifyStatusBar(Context context, MsgThread thread){
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
			.setContentTitle("新私信【"+ thread.unread_count + "】")
		    .setContentText(thread.thread_name + ": " + thread.msg_body)
		    .setSmallIcon(R.drawable.ic_launcher)
		    .setTicker(thread.thread_name + "发来" + thread.unread_count + "条私信")
		    .setNumber(thread.unread_count)
		    .setAutoCancel(true)
		    .setDefaults(Notification.DEFAULT_SOUND);
		
		Intent resultIntent = new Intent(context, ConversationActivity.class);
		resultIntent.putExtra("thread_uid", thread.thread_uid);
		TaskStackBuilder stackBuilder = TaskStackBuilder.from(context);
		// Adds the back stack
		stackBuilder.addParentStack(ConversationActivity.class);
		// Adds the Intent to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		// Gets a PendingIntent containing the entire back stack
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(NEW_MSG_NOTI + thread.thread_uid, mBuilder.getNotification());
		
	}
	
	public static void cancelMsgNoti(Context context, int thread_uid){
		if(mNotificationManager == null)
			mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NEW_MSG_NOTI + thread_uid);
	}
	
}
