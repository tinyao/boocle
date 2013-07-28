package com.czzz.bookcircle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.data.MsgHelper;
import com.czzz.utils.HttpDownloadAsyncTask;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;
import com.czzz.utils.NetHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class UserUtils {

	public static void fetchNearbyUsers(
			int start, int count, int school_id, int sort, AsyncHttpResponseHandler responseHandler){
		String url = Pref.NEARBY_USERS_URL + "?";
		
		RequestParams params = new RequestParams();
		params.put("school_id", "" + school_id);
		params.put("start", "" + start);
		params.put("total", "" + count);
		params.put("sort", "" + sort);
		
		NetHttpClient.get(url, params, responseHandler);
		
//		List<NameValuePair> params = new LinkedList<NameValuePair>();
//		params.add(new BasicNameValuePair("school_id", "" + school_id));
//		params.add(new BasicNameValuePair("start", "" + start));
//		params.add(new BasicNameValuePair("total", "" + count));
//		params.add(new BasicNameValuePair("sort", "" + sort));
//		
//		String paramString = URLEncodedUtils.format(params, "utf-8");
//		
//		url += paramString;
//		
//		Log.d("DEBUG", "url: " + url);
//		
//		new HttpDownloadAsyncTask(con, listener).execute(url);
	}
	
	public static void fetchFollowing(Context con, HttpListener listener, 
			int uid, int start){
		String url = Pref.USER_FOLLOWING_URL + "?";
		
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", "" + uid));
		params.add(new BasicNameValuePair("start", "" + start));
		
		String paramString = URLEncodedUtils.format(params, "utf-8");
		
		url += paramString;
		
		Log.d("DEBUG", "url: " + url);
		
		new HttpDownloadAsyncTask(con, listener).execute(url);
	}
	
	public static void isFollowed(int fuid, AsyncHttpResponseHandler responseHandler) {
		
		RequestParams params = new RequestParams();
		params.put("uid", "" + User.getInstance().uid);
		params.put("fuid", "" + fuid);
		
		NetHttpClient.post(Pref.USER_ISFOLLOW_URL, params, responseHandler);
	}
	
	public static void followTa(int fuid, AsyncHttpResponseHandler responseHandler) {
		
		RequestParams params = new RequestParams();
		params.put("uid", "" + User.getInstance().uid);
		params.put("passwd", User.getInstance().pass);
		params.put("fuid", "" + fuid);
		
		NetHttpClient.post(Pref.USER_FOLLOW_URL, params, responseHandler);
	}
	
	public static void unfollowTa(int fuid, AsyncHttpResponseHandler responseHandler) {
		
		RequestParams params = new RequestParams();
		params.put("uid", "" + User.getInstance().uid);
		params.put("passwd", User.getInstance().pass);
		params.put("fuid", "" + fuid);
		
		NetHttpClient.post(Pref.USER_UNFOLLOW_URL, params, responseHandler);
	}
	
	public static void fetchTopUsers(Context con, HttpListener listener, int school_id){
		String url = Pref.TOP_USERS_URL + "?";
		
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("school_id", "" + school_id));
		
		String paramString = URLEncodedUtils.format(params, "utf-8");
		
		url += paramString;
		
		Log.d("DEBUG", "url: " + url);
		
		new HttpDownloadAsyncTask(con, listener).execute(url);
	}
	
	public static void fetchUserInfo(Context con, HttpListener listener, int uid){
		String url = Pref.READ_USER_URL + "?";
		
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", "" + uid));
		
		String paramString = URLEncodedUtils.format(params, "utf-8");
		
		url += paramString;
		
		Log.d("DEBUG", "url: " + url);
		
		new HttpDownloadAsyncTask(con, listener).execute(url);
	}
	
	public static void fetchUserMsg(int type, AsyncHttpResponseHandler responseHandler){
		
		RequestParams params = new RequestParams();
		params.put("uid", "" + User.getInstance().uid);
		params.put("passwd", "" + User.getInstance().pass);
		params.put("type", "" + type);
		
		NetHttpClient.post(Pref.READ_USER_MSG, params, responseHandler);
		
//		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
//		params.add(new BasicNameValuePair("uid", "" + User.getInstance().uid));
//		params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
//		params.add(new BasicNameValuePair("type", type+""));
//		
//		new HttpPostTask(con, listener).execute(Pref.READ_USER_MSG, params);
	}
	
	public static ArrayList<User> parseNeabyUsers(String jsonStr){
		try {
			JSONObject json = new JSONObject(jsonStr);
			if(json.getInt("status") == 2){
				return null;
			}
			ArrayList<User> list = new ArrayList<User>();
			JSONArray array = json.getJSONArray("data");
			for(int i=0; i<array.length(); i++){
				User user = new User();
				JSONObject item = array.getJSONObject(i);
				user.init(item);
				list.add(user);
			}
			return list;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static User parseUser(String jsonStr){
		try {
			JSONObject json = new JSONObject(jsonStr);
			if(json.getInt("status") == 2){
				return null;
			}
			User user = new User();
			JSONObject ujson = json.getJSONObject("data");
			return user.init(ujson);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static User parseMyself(String jsonStr){
		try {
			Log.d("DEBUG", "user: " + jsonStr);
			JSONObject json = new JSONObject(jsonStr);
			if(json.getInt("status") == 2){
				return null;
			}
			User user = User.getInstance();
			JSONObject ujson = json.getJSONObject("data");
			return user.init(ujson);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 解析返回的全部私信，返回私信列表
	 * @param jsonStr 时间升序
	 * @return
	 */
	public static ArrayList<DirectMsg> parseDMsg(String jsonStr){
		try {
			JSONObject json = new JSONObject(jsonStr);
			if(json.getInt("status") != 1){
				return null;
			}
			
			ArrayList<DirectMsg> list = new ArrayList<DirectMsg>();
			
			JSONArray array = json.getJSONArray("data");
			for(int i=0; i<array.length(); i++){
				DirectMsg msg = new DirectMsg();
				JSONObject item = array.getJSONObject(i);
				msg.init(item);
				
				list.add(msg);
			}
			
			return list;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 发送消息
	 * @param con
	 * @param recv_id
	 * @param body
	 * @param book_id
	 * @param listener
	 */
	public static void sendDirectMsg(Context con, int recv_id, String body, 
			int book_id, HttpListener listener){
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("uid", ""+User.getInstance().uid));
		params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
		params.add(new BasicNameValuePair("recver_id", "" + recv_id));
		params.add(new BasicNameValuePair("body", "" + body));
		params.add(new BasicNameValuePair("book_id", ""+book_id));
		
		new HttpPostTask(con, listener).execute(Pref.SEND_MSG_URL, params);
	}
	
	public static void setRemoteMsgRead(Context con, int msg_id, HttpListener listener){
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("uid", "" + User.getInstance().uid));
		params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
		params.add(new BasicNameValuePair("id", "" + msg_id));
		
		new HttpPostTask(con, listener).execute(Pref.SET_REG_READ_URL, params);
	}
	
	public static void checkRemoteUnreadMsg(Context con, AsyncHttpResponseHandler responseHandler){
		
		int maxId = MsgHelper.getInstance(con).getMaxMsgId();
		
		RequestParams params = new RequestParams();
		params.put("uid", "" + User.getInstance().uid);
		params.put("passwd", "" + User.getInstance().pass);
		params.put("msg_id", "" + maxId);
		
		NetHttpClient.post(Pref.CHECK_MSG_URL, params, responseHandler);
		
//		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
//		params.add(new BasicNameValuePair("uid", "" + User.getInstance().uid));
//		params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
//		params.add(new BasicNameValuePair("msg_id", "" + maxId));
//		
//		new HttpPostTask(con, listener).execute(Pref.CHECK_MSG_URL, params);
	}
	
	public static void parseNewDirectMsg(Context con, String jsonStr){
		try {
			JSONObject json = new JSONObject(jsonStr);
			if(json.getInt("status") == 3){
				Toast.makeText(con, "没有新消息", Toast.LENGTH_SHORT).show();
			}else{
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveMsgs(Context con, ArrayList<DirectMsg> msgs){
		MsgHelper helper = MsgHelper.getInstance(con);
		helper.saveMsgs(msgs);
	}
	
	public String generatePass(String uid, String createAt){
		String start = createAt.substring(0, 5);
		String end = createAt.substring(5);
		
		String longPass = end + uid + start;
		
		if(longPass.length() > 10){
			longPass = longPass.substring(0, 10);
		}
		
		return longPass;
	}
	

	public static String generatePass(int social_type, String uid){ 
		
		String start = uid.substring(0, uid.length()/2).toUpperCase();
		String end = uid.substring(uid.length()/2);
		
		int social = social_type * 23;
		
		String longPass = end + social + uid + start;
		
		if(longPass.length() > 10){
			longPass = longPass.substring(0, 10);
		}
		
		return longPass;
	}
	
}
