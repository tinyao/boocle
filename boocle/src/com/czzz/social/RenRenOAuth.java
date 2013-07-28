package com.czzz.social;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;

public class RenRenOAuth extends BaseOAuth{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8733998079427593562L;

	public static final String OUATH_URL = "https://graph.renren.com/oauth/authorize" +
			"?client_id=218790&response_type=token" +
			"&redirect_uri=http://graph.renren.com/oauth/login_success.html";
	
	public static final String REN_INFO_URL = "https://api.renren.com/restserver.do";
	
	public String accessToken = "";
	private String renrenUid = "";
	private String expiresIn = "";
	public String school = "";
	 

	public void getRenrenUid(Context con, HttpListener listener){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("method", "users.getLoggedInUser"));
		params.add(new BasicNameValuePair("v",
				"1.0"));
		params.add(new BasicNameValuePair("api_key", "e79d3dc366ce417e8a54206621b15d9a"));
		params.add(new BasicNameValuePair("access_token", accessToken));
		params.add(new BasicNameValuePair("format", "json"));

		new HttpPostTask(con, listener).execute(REN_INFO_URL, params);
	}
	
	public void getRenrenInfo(Context con, HttpListener listener){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("method", "users.getInfo"));
		params.add(new BasicNameValuePair("v",
				"1.0"));
		params.add(new BasicNameValuePair("api_key", "e79d3dc366ce417e8a54206621b15d9a"));
		params.add(new BasicNameValuePair("access_token", accessToken));
		params.add(new BasicNameValuePair("format", "json"));
		params.add(new BasicNameValuePair("uids", ""+uid));

		new HttpPostTask(con, listener).execute(REN_INFO_URL, params);
	}
	
	
}
