package com.czzz.bookcircle;

import org.apache.http.HttpException;
import org.json.JSONObject;

import android.database.Cursor;

public class User2 {
	
	private String id;
	private String createAt;
	private String name;
	private String screenName;
	private String email;
	private String location;
	private String description;
	private String birthday;
	private String gender;
	
	private String avatarUrl;
	private String url;
	private String school;
	private String im;
	private String phone;
	
	private String doubanUid;
	private String weiboUid;
	private String renrenUid;
	
	private int bookTotal;
	private int likeTotal; 
	
	private BookShelf bookshelf;
	
	/**
	 * 
	 * @param json
	 * @throws HttpException
	 */
	public User2(JSONObject json) throws HttpException {
		super();
		init(json);
	}
	
	/**
	 * 
	 * @param json
	 * @throws HttpException
	 */
	private void init(JSONObject json) throws HttpException {
		
	}
	
}
