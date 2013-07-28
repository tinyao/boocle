package com.czzz.bookcircle;

import java.io.Serializable;

import android.database.Cursor;

public class MsgThread implements Serializable{
	
	private static final long serialVersionUID = 802995777460577462L;
	
	public int _id;
	public int msg_id;
	public int thread_uid;
	public String thread_name;
	public String thread_avatar;
	public String msg_body; // 最新一条信息
	public boolean is_recv; // 是否是收到
	public long msg_time;
	public int unread_count;
	
	public void init(Cursor cursor){
		_id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
		msg_id = cursor.getInt(cursor.getColumnIndexOrThrow("msg_id"));
		thread_uid = cursor.getInt(cursor.getColumnIndexOrThrow("thread_uid"));
		thread_name = cursor.getString(cursor.getColumnIndexOrThrow("thread_name"));
		thread_avatar = cursor.getString(cursor.getColumnIndexOrThrow("thread_avatar"));
		msg_body = cursor.getString(cursor.getColumnIndexOrThrow("msg_body"));
		is_recv = cursor.getInt(cursor.getColumnIndexOrThrow("is_recv")) == 1;
		msg_time = Long.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("msg_time")));
		unread_count = cursor.getInt(cursor.getColumnIndexOrThrow("unread_count"));
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "["
				+ "thread_uid: " + thread_uid
				+ "\nthread_name: " + thread_name
				+ "]";
	}
	
}
