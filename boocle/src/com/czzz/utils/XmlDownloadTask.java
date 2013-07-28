package com.czzz.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.czzz.demo.CustomAsyncHttpResponseHandler;
import com.czzz.douban.BookCollectionEntry;
import com.czzz.douban.BookCollectionXmlParser;
import com.czzz.douban.BookCommentEntry;
import com.czzz.douban.BookCommentXmlParser;

public class XmlDownloadTask extends AsyncTask<Object, Object, Object>{

	HttpListener taskListener;
	int taskType;
	Context context;
	
	public XmlDownloadTask(Context context, HttpListener taskListener, int type) {
		this.taskListener = taskListener;
		this.taskType = type;
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
			return NetworkUtils.REVIEWS_NOT_FOUND;
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		// TODO Auto-generated method stub
		if (result instanceof Integer) {
			int tmp = (Integer)result;
			switch(tmp){
			case NetworkUtils.NETWORK_OFF:
//				resHandler.onFailure(new Throwable(), "can't resolve host");
				taskListener.onTaskFailed("哎呀，你好像没有打开网络连接...");
				break;
			case NetworkUtils.NETWORK_ERROR:
//				resHandler.onFailure(new Throwable(), "can't resolve host");
				taskListener.onTaskFailed("不好意思，网络连接好像好故障了...");
				break;
			case NetworkUtils.REVIEWS_NOT_FOUND:
//				resHandler.onFailure(new Throwable(), "这个书好像木有评论");
				taskListener.onTaskFailed("这个书好像木有评论");
				break;
			}
			return;
		}
		taskListener.onTaskCompleted(result);
//		resHandler.onSuccess(200, result);
	}

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	private Object downloadUrl(String myurl) throws IOException {
		InputStream is = null;

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
			
			if(response != 200) return null;
			
			is = conn.getInputStream();
			
			switch(taskType){
			case HttpListener.FETCH_BOOK_COLLECTION:
				BookCollectionXmlParser xmlcollectoinParser = new BookCollectionXmlParser();
				List<BookCollectionEntry> collections = xmlcollectoinParser.parse(is);
				return collections;
			case HttpListener.FETCH_BOOK_COMMENTS:
				BookCommentXmlParser xmlcommentParser = new BookCommentXmlParser();
				List<BookCommentEntry> comments = xmlcommentParser.parse(is);
				return comments;
			default:
				return null;
			}
			
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (is != null) {
				is.close();
			}
		}
		
		return null;
	}

}
