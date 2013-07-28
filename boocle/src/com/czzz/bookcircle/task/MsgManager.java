package com.czzz.bookcircle.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;

public class MsgManager {
	
	static MsgManager instance;
	private Context context;
	
	public static MsgManager getInstance(Context con){
		if (instance == null){
			return instance = new MsgManager(con);
		}
		return instance;
	}
	
	public MsgManager(Context con){
		this.context = con.getApplicationContext();
	}
	
	public void sendDirectMsg(int recv_id, String body, 
			int book_id){
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("uid", ""+User.getInstance().uid));
		params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
		params.add(new BasicNameValuePair("recver_id", "" + recv_id));
		params.add(new BasicNameValuePair("body", "" + body));
		params.add(new BasicNameValuePair("book_id", ""+book_id));
		
		new HttpPostTask(context, null).execute(Pref.SEND_MSG_URL, params);
	}

}
