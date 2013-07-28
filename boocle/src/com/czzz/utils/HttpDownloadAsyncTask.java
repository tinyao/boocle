package com.czzz.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HttpDownloadAsyncTask extends AsyncTask<Object, Object, Object> {

	HttpListener taskListener;
	Context context;
	

	public HttpDownloadAsyncTask(Context context, HttpListener taskListener) {
		this.taskListener = taskListener;
		this.context = context;
	}

	@Override
	protected Object doInBackground(Object... urls) {
		// TODO Auto-generated method stub
		
		if(!NetworkUtils.isOnline(context)) {
			return NetworkUtils.NETWORK_OFF;
		}
		
		try {
			return downloadUrl(String.valueOf(urls[0]));
		} catch (IOException e) {
			return NetworkUtils.NETWORK_ERROR;
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		// TODO Auto-generated method stub
		if (result instanceof Integer) {
			int tmp = (Integer)result;
			switch(tmp){
			case NetworkUtils.NETWORK_OFF:
				taskListener.onTaskFailed("哎呀，你好像没有打开网络连接...");
				break;
			case NetworkUtils.NETWORK_ERROR:
				taskListener.onTaskFailed("不好意思，网络连接好像好故障了...");
				break;
			}
			return;
		}
		taskListener.onTaskCompleted(result);
	}
	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case 0:
				taskListener.onTaskFailed(msg.obj+"");
				break;
			}
			super.handleMessage(msg);
		}
		
	};

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	private Object downloadUrl(String myurl) throws IOException {
		InputStream is = null;
		// Only display the first 500 characters of the retrieved
		// web page content.

		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(10000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();
			Log.d("DEBUG_TAG", "The response is: " + response);
			
			if(response == 200){
				is = conn.getInputStream();
				// Convert the InputStream into a string
				String contentAsString = readIt(is);
				Log.d("DEBUG_TAG", "The response is: " + contentAsString);
				return contentAsString;
			}else{
				is = conn.getInputStream();
				// Convert the InputStream into a string
				String contentAsString = readIt(is);
				Log.d("DEBUG_TAG", "The response is: " + contentAsString);
				
				Message mmsg = mHandler.obtainMessage();
				mmsg.what = 0;
				mmsg.obj = "Error " + response + ": " + contentAsString;
				mHandler.sendMessage(mmsg);
				return null;
			}

		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	// Reads an InputStream and converts it to a String.
	public String readIt(InputStream stream) throws IOException,
			UnsupportedEncodingException {

		Log.d("DEBUG", "connection over.....");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				stream, "UTF-8"));
		StringBuilder sb = new StringBuilder();

		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}

		return sb.toString();
	}
	
}
