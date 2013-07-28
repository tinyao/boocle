/*
 * Copyright 2011 Sina.
 *
 * Licensed under the Apache License and Weibo License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.open.weibo.com
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.czzz.social.net;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;


/**
 * Encapsulation main Weibo APIs, Include: 1. getRquestToken , 2. getAccessToken, 3. url request.
 * Implements a weibo api as a asynchronized way. Every object used this runner should implement interface RequestListener.
 *
 * @author  ZhangJie (zhangjie2@staff.sina.com.cn)
 */
public class AsyncWeiboRunner extends AsyncTask<Object, String, String>{
	
	Handler mHandler;
	Weibo weibo;
	WeiboParameters params;
	Token token;
	String method;
	Context context;
	
	public AsyncWeiboRunner(Context context, Handler handler, WeiboParameters params, 
			String method, Token token, Weibo weibo){
		this.mHandler = handler;
		this.weibo = weibo;
		this.params = params;
		this.token = token;
		this.context = context;
		this.method = method;
	}
	
	@Override
	protected String doInBackground(Object... urls) {
		// TODO Auto-generated method stub
		try {
			String rr = weibo.request(context, String.valueOf(urls[0]), params, method, token);
			return rr;
		} catch (WeiboException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "error";
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		Message msg = mHandler.obtainMessage();
		msg.what = 0;
		msg.obj = result;
		mHandler.sendMessage(msg);
		super.onPostExecute(result);
	}
	
	
	
}
