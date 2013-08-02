package com.czzz.utils;

public interface HttpListener {

	public static final int DOUBAN_OAUTH_CODE = 0;
	public static final int DOUBAN_OAUTH_JSON =1;
	public static final int FETCH_BOOK_INFO = 2;
	public static final int FETCH_BOOK_COVER = 3;
	public static final int FETCH_USER_INFO = 4;
	
	public static final int FETCH_BOOK_COLLECTION = 5;
	public static final int FETCH_BOOK_COMMENTS = 6;
	
	public static final int FETCH_USER_CONTACTS = 7;
	
	public static final int FETCH_SINA_PLACES = 8;
	
	public static final int SEARCH_BOOKS = 9;
	
	public static final int FETCH_PLACE_NAME = 10;
	
	public static final int SEARCH_BOOKS_MORE = 11;
	
	public static final int FETCH_BOOK_RATE = 12;
	
	public static final int ACCOUNT_REGISTER = 13;
	public static final int ACCOUNT_LOGIN = 14;
	public static final int FETCH_USER_BOOKS = 15;
	public static final int ADD_USER_BOOK = 16;
	public static final int QUERY_NEARBYBOOK_BY_ISBN = 17;
	public static final int MORE_USER_BOOKS = 18;
	
	public static final int PROFILE_CHANGE_AVATAR = 19;
	
	public static final int FETCH_NEARBY_BOOKS = 20;
	
	public static final int FETCH_NEARBY_BOOKS_MORE = 21;
	
	public static final int FETCH_NEARBY_USERS = 22;
	
	public static final int FETCH_TOP_USERS = 23;
	
	public static final int FETCH_CIRCLE_USER_INFO = 24;
	
	public static final int SEND_DIRECT_MSG = 25;
	
	public static final int UPDATE_USER_BOOKS = 26;
	
	public static final int REFRESH_USER_INFO = 27;
	
	public static final int FETCH_DOUBAN_RECOMM = 28;
	
	public static final int FETCH_NEARBY_BOOKS_NEW = 29;
	
	public static final int BIND_USER_CHECK = 30;
	
	public static final int RENREN_INFO_UID = 31;

	public static final int RENREN_INFO = 32;
	
	public static final int DOUBAN_INFO = 33;
	
	public static final int WEIBO_INFO = 34;
	
	public static final int FETCH_BUGS = 35;
	
	public static final int FETCH_USER_FOLLOWING = 36;
	
	public static final int CHANGE_PASSWD = 37;
	
	public static final int FETCH_NEARBY_USERS_MORE = 38;
	
	public static final int DELETE_COLLECTION = 39;
	
	public static final int USER_IS_FOLLOWED = 40;
	
	public static final int USER_FOLLOW_TA = 41;
	
	public static final int USER_UNFOLLOW_TA = 42;
	
	public static final int BOOKS_FOLLOW_USER = 43;
	
	public void onTaskCompleted(Object data);
	
	public void onTaskFailed(String data);
	
}
