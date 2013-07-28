package com.czzz.base;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.czzz.bookcircle.BookCollection;

public class User implements Serializable{

	public int uid = 0;
	public String name;
	public String email = "";
	public String pass = "";
	public String avatar = "";
	public String desc = "";
	public int book_total;
	public int fav_total;
	public int school_id = 0;
	public String school_name = "";
	private String bind_id = "";
	private int bind_from = 0;
	
	// 1为女， 2为男
	public int gender = 1;
	
	public String tags = "";
	public String major = "";
	public String phone = "";
	public String site = "";
	public String im = "";

	// public String renrenUID = "";
	// public String weiboUID = "";
	// public String doubanUID = "";
	public String createTime = "";

	public ArrayList<BookCollection> collections = null;
	
	private static User userInstace = null;	//用户自己的实例
	
//	private static User taInstance = null;
	
	private static ArrayList<User> taList = new ArrayList<User>();

	public User() {

	}

	public synchronized static User getInstance() {
		// 获取登录用户的实例
		if (userInstace == null) {
			userInstace = new User();
		}
		return userInstace;
	}
	
	
	/**
	 * 获取指定uid对应的 用户缓存，不存在则返回null
	 * @param uid
	 * @return
	 */
	public synchronized static User getTaInstance(int uid) {
		// 获取一个缓存用户的实例
		for (User item : taList){
			if (item.uid == uid) return item;
		}
		
		return null;
	}
	
	public static void clearTaList(){
		taList.clear();
	}
	
	/**
	 * 用户缓存列表
	 * @param ta
	 */
	public static void addTaInstance(User ta){
		if (taList.size() < 3){
			taList.add(ta);
		}else{
			taList.remove(0);
			taList.add(ta);
		}
	}
	
	public static void setTaUser(User ta){
		int index = 0;
		for (User item : taList){
			if (item.uid == ta.uid) {
				taList.set(index, ta);
				break;
			}
			index++;
		}
	}
	
	/**
	 * 判断uid是否存在于缓存列表
	 * @param uid
	 * @return
	 */
	public static boolean hasTempCache(int uid){
		for (User item : taList){
			if (item.uid == uid) return true;
		}
		return false;
	}
	
	/**
	 * 清除自己
	 */
	public static void clearUser(){
		if(userInstace != null)
			userInstace = null;
	}

	@SuppressWarnings("finally")
	public User init(JSONObject userJson) {

		try {
			uid = Integer.valueOf(userJson.getString("uid"));
			name = userJson.getString("name");
			String avatar_name = userJson.getString("avatar");
			
			if(avatar_name.contains("http://") || avatar_name.contains("https://")){	
				// 来自第三方登录
				avatar = avatar_name;
			}else{	// 系统注册
				avatar = avatar_name.equals("") ? "" : Pref.AVATAR_BASE_URL + avatar_name;
			}
			
			gender = Integer.valueOf(userJson.getString("gender"));
			desc = userJson.getString("desc");
			book_total = Integer.valueOf(userJson.getString("book_total"));
			fav_total = Integer.valueOf(userJson.getString("fav_total"));
			
			school_id = Integer.valueOf(userJson.getString("school_id"));
			school_name = userJson.getString("school_name");
			createTime = userJson.getString("create_at");
			
			email = userJson.getString("email");
			major = userJson.getString("major");
			phone = userJson.getString("phone");
			site = userJson.getString("site");
			im = userJson.getString("im");
			tags = userJson.getString("tags");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			return this;
		}

	}
	
	

	public void init(Context context) {
		SharedPreferences sp = context.getSharedPreferences("account", 0);
		uid = sp.getInt("uid", 0);
		pass = sp.getString("password", "");
		name = sp.getString("name", "");
		email = sp.getString("email", "");
		avatar = sp.getString("avatar", "");
		desc = sp.getString("desc", "");
		gender = sp.getInt("gender", 1);
		major = sp.getString("major", "");
		phone = sp.getString("phone", "");
		site = sp.getString("site", "");
		im = sp.getString("im", "");
		book_total = sp.getInt("book_total", 0);
		fav_total = sp.getInt("fav_total", 0);
		school_id = sp.getInt("school_id", 0);
		school_name = sp.getString("school_name", "");
		tags = sp.getString("tags", "");
		// renrenUID = sp.getString("renrenUID", "");
		// doubanUID = sp.getString("doubanUID", "");
		// weiboUID = sp.getString("createTime", "");
		createTime = sp.getString("create_at", "");
	}
	
	public void init(Cursor cursor){
        uid = cursor.getInt(cursor.getColumnIndexOrThrow("uid"));
        name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
        avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
        book_total = cursor.getInt(cursor.getColumnIndexOrThrow("book_total"));
        fav_total = cursor.getInt(cursor.getColumnIndexOrThrow("fav_total"));
        school_id = cursor.getInt(cursor.getColumnIndexOrThrow("school_id"));
        school_name = cursor.getString(cursor.getColumnIndexOrThrow("school_name"));
        gender = cursor.getInt(cursor.getColumnIndexOrThrow("gender"));
        desc = cursor.getString(cursor.getColumnIndexOrThrow("desc"));
        major = cursor.getString(cursor.getColumnIndexOrThrow("major"));
        phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
        site = cursor.getString(cursor.getColumnIndexOrThrow("site"));
        im = cursor.getString(cursor.getColumnIndexOrThrow("im"));
        createTime = cursor.getString(cursor.getColumnIndexOrThrow("create_at"));
        
	}

	public void save(Context context) {
		// TODO Auto-generated method stub
		SharedPreferences sp = context.getSharedPreferences("account", 0);
		sp.edit().putInt("uid", uid).putString("name", name)
				.putString("email", email).putString("avatar", avatar)
				.putInt("book_total", book_total)
				.putInt("fav_total", fav_total)
				.putInt("school_id", school_id).putString("tags", tags)
				.putString("school_name", school_name)
				.putInt("gender", gender)
				.putString("desc", desc).putString("major", major)
				.putString("phone", phone).putString("im", im)
				.putString("site", site)
				// .putString("renrenUID", renrenUID == null ? "" : renrenUID)
				// .putString("doubanUID", doubanUID == null ? "" : doubanUID)
				// .putString("weiboUID", weiboUID == null ? "" : weiboUID)
				.putString("create_at", createTime).commit();

	}

	protected static Date parseDate(String str, String format) {

		Map<String, SimpleDateFormat> formatMap = new HashMap<String, SimpleDateFormat>();

		if (str == null || "".equals(str)) {
			return null;
		}
		SimpleDateFormat sdf = formatMap.get(format);
		if (null == sdf) {
			sdf = new SimpleDateFormat(format, Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			formatMap.put(format, sdf);
		}
		try {
			synchronized (sdf) {
				// SimpleDateFormat is not thread safe
				return sdf.parse(str);
			}
		} catch (ParseException pe) {

		}
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		
		return "User:[" +
				"\nname=" + name +
				"\nuid=" + uid + 
				"\navatar=" + avatar + 
				"\nemail=" + email + 
				"\ndesc=" + desc +
				"]";
	}

}
