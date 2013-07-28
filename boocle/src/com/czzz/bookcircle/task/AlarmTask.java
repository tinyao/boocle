package com.czzz.bookcircle.task;

import com.czzz.bookcircle.MyApplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 定时任务：定时检测新私信
 * 
 * @author tinyao
 * 
 */
public class AlarmTask {

	private static final int MSG_CHECK_INTER = 3;
	
	/**
	 * 定时检测新私信
	 */
	public static void setMsgAlarm(Context context) {
		if(!MyApplication.configPref.contains("msg_interval")){
			MyApplication.configPref.edit().putInt("msg_interval", 3).commit();
		}
		
		int interval = MyApplication.configPref.getInt("msg_interval", 3);
		
		Log.d("DEBUG", "set alarm..." + interval);
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent("bookcircle.task.check_msg");
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC_WAKEUP, 
				System.currentTimeMillis() + 1000 * 60 * interval,
				1000 * 60 * interval, pi); // Millisec * Second * Minute
	}

	public static void cancelMsgAlarm(Context context) {
		Intent intent = new Intent("bookcircle.task.check_msg");
		PendingIntent sender = PendingIntent
				.getBroadcast(context, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

}
