package com.czzz.bookcircle;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

import com.czzz.base.Pref;

public class DirectMsg implements Serializable, Parcelable{
	
	private static final long serialVersionUID = -3685816235938929262L;

	public int msg_id;
	
	public int sender_id;
	
	public int recver_id;
	
	public int thread_uid;
	
	public String thread_name;
	
	public String thread_avatar;

	public boolean is_unread;
	
	public String body;
	
	public int book_id;
	
	public boolean is_recv;
	
	public long create_at;
	
	public boolean is_sending = false;
	
	public void init(JSONObject json){
		try {
			msg_id = Integer.valueOf(json.getString("id"));
			recver_id = Integer.valueOf(json.getString("recver_id"));
			sender_id = Integer.valueOf(json.getString("sender_id"));
			thread_uid = Integer.valueOf(json.getString("uid"));
			thread_name = json.getString("name");
			thread_avatar = json.getString("avatar");
			
			if(thread_avatar.contains("http://") || thread_avatar.contains("https://")){	
				// 来自第三方登录
				// 。。。
			}else{	// 系统注册
				thread_avatar = thread_avatar.equals("") ? "" : Pref.AVATAR_BASE_URL + thread_avatar;
			}
			
			body = json.getString("body");
			is_unread = Integer.valueOf(json.getString("is_read")) == 0;
			book_id = Integer.valueOf(json.getString("book_id"));
			create_at = Long.valueOf(json.getString("create_at"));
			
			if(thread_uid == sender_id){ 	//对方是发送者 那么消息为接收
				is_recv = true;
			}
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * init from cursor
	 * @param cur
	 */
	public void init(Cursor cursor){
		msg_id = cursor.getInt(cursor.getColumnIndexOrThrow("msg_id"));
		sender_id = cursor.getInt(cursor.getColumnIndexOrThrow("sender_id"));
		recver_id = cursor.getInt(cursor.getColumnIndexOrThrow("recver_id"));
		thread_uid = cursor.getInt(cursor.getColumnIndexOrThrow("thread_uid"));
		thread_name = cursor.getString(cursor.getColumnIndexOrThrow("thread_name"));
		thread_avatar = cursor.getString(cursor.getColumnIndexOrThrow("thread_avatar"));
		is_unread = cursor.getInt(cursor.getColumnIndexOrThrow("is_unread")) == 1;
		is_recv = cursor.getInt(cursor.getColumnIndexOrThrow("is_recv")) == 1;
		body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
		book_id = cursor.getInt(cursor.getColumnIndexOrThrow("book_id"));
		create_at = Long.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("create_at")));
	}

	public static final Parcelable.Creator<DirectMsg> CREATOR = new Creator<DirectMsg>() {   

        public DirectMsg createFromParcel(Parcel source) {   

        	DirectMsg msg = new DirectMsg(); 
        	msg = (DirectMsg) source.readSerializable();
            return msg;   

        }   

        public DirectMsg[] newArray(int size) {   

            return new DirectMsg[size];   

        }   

    };
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		// TODO Auto-generated method stub
		parcel.writeSerializable(this);
	}

}
