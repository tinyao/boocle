package com.czzz.bookcircle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.czzz.base.Pref;
import com.czzz.data.SearchResultDBHelper;
import com.czzz.demo.DoubanRecomActivity;
import com.czzz.douban.DoubanBook;
import com.czzz.utils.HttpDownloadAsyncTask;
import com.czzz.utils.HttpListener;
import com.czzz.utils.NetHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class BookUtils {

	public static void fetchNearby(int school_id, int start, int count, int status, int sort,
			AsyncHttpResponseHandler responseHandler){
		String url = Pref.NEARBY_BOOKS_URL + "?";
		
		RequestParams params = new RequestParams();
		params.put("school_id", "" + school_id);
		params.put("start", "" + start);
		params.put("total", "" + count);
		params.put("type", "" + status);
		params.put("sort", "" + sort);
		
		NetHttpClient.get(url, params, responseHandler);
		
//		List<NameValuePair> params = new LinkedList<NameValuePair>();
//		params.add(new BasicNameValuePair("school_id", "" + school_id));
//		params.add(new BasicNameValuePair("start", "" + start));
//		params.add(new BasicNameValuePair("total", "" + count));
//		params.add(new BasicNameValuePair("type", "" + status));
//		params.add(new BasicNameValuePair("sort", "" + sort));
//		
//		String paramString = URLEncodedUtils.format(params, "utf-8");
//		
//		url += paramString;
//		
//		Log.d("DEBUG", "url: " + url);
		
//		new HttpDownloadAsyncTask(con, listener).execute(url);
	}
	
	public static void fetchNearbyNewBooks(
			int school_id, int start, int status, AsyncHttpResponseHandler responseHandler){
		String url = Pref.NEARBY_BOOKS_NEW_URL + "?";
		
		RequestParams params = new RequestParams();
		params.put("school_id", "" + school_id);
		params.put("start", "" + start);
		params.put("type", "" + status);
		
		NetHttpClient.get(url, params, responseHandler);
	}
	
	public static void fetchDoubanRecomm(String date, AsyncHttpResponseHandler responseHandler){
		String url = Pref.DOUBAN_HOT_URL + "?";
		
		RequestParams params = new RequestParams();
		params.put("date", "" + date);
		
		NetHttpClient.get(url, params, responseHandler);
		
		
//		List<NameValuePair> params = new LinkedList<NameValuePair>();
//		params.add(new BasicNameValuePair("date", date));
//		
//		String paramString = URLEncodedUtils.format(params, "utf-8");
//		
//		url += paramString;
//		
//		Log.d("DEBUG", "url: " + url);
//		
//		new HttpDownloadAsyncTask(con, listener).execute(url);
	}
	
	public static ArrayList<BookCollection> parseNearybyBooks(String jsonStr){
		try {
			JSONObject json = new JSONObject(jsonStr);
			if(json.getInt("status") != 1){
				return null;
			}
			ArrayList<BookCollection> list = new ArrayList<BookCollection>();
			JSONArray array = json.getJSONArray("data");
			
			for(int i=0; i<array.length(); i++){
				BookCollection entry = new BookCollection();
				
				entry.initCollection(array.getJSONObject(i));
				
//				JSONObject item = array.getJSONObject(i);
//				entry.status = Integer.valueOf(item.getString("status"));
//				entry.cid = Integer.valueOf(item.getString("id"));
//				entry.note = item.getString("note");
//				entry.owner_avatar = Pref.AVATAR_BASE_URL + item.getString("avatar");
//				entry.create_at = item.getString("create_at");
//				entry.owner = item.getString("name");
//				entry.owner_id = Integer.valueOf(item.getString("uid"));
//				
//				JSONObject bookJson =item.getJSONObject("book"); 
//				entry.initBook(bookJson);
//				entry.book.title = item.getString("title");
//				entry.book.isbn13 = item.getString("isbn");
//				entry.book.author = item.getString("author");
//				entry.book.publisher = item.getString("publisher");
//				entry.book.image = item.getString("cover");
				list.add(entry);
			}
			
			return list;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<DoubanBook> parseDoubanRecomms(Context con, String data){

		ArrayList<DoubanBook> books = new ArrayList<DoubanBook>();
		
		try {
			JSONObject json = new JSONObject(""+data);
			
			if(json.getInt("status")==2){
				return null;
			}
			
			JSONArray jsonArray = json.getJSONArray("data");
			JSONObject item;
			for(int i=0; i<jsonArray.length(); i++){
				item = jsonArray.getJSONObject(i);
				DoubanBook book = initRecommBook(item);
				books.add(book);
				
			}
			
			saveRecomms(con, books);
			
			return books;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static DoubanBook initRecommBook(JSONObject item) throws JSONException{
		DoubanBook book = new DoubanBook();
		book.isbn13 = item.getString("isbn");
		book.title = item.getString("title");
		book.subtitle = item.getString("sub_title");
		book.author = item.getString("author");
		book.image = item.getString("cover");
		book.large_img = item.getString("cover_large");
		book.publisher = item.getString("publisher");
		book.translator = item.getString("pubdate");
		book.rateNum = item.getString("rate_num");
		book.rateAverage = item.getString("rate_score");
		book.summary = item.getString("summary");
		return book;
	}
	
	private static  void saveRecomms(final Context con, final ArrayList<DoubanBook> books){
		
		Log.d("DEBUG", "save Recomm....");
		new Thread(){
			public void run(){
				SearchResultDBHelper helper = SearchResultDBHelper.getInstance(con);
				helper.openDataBase();
				String key_date = initCurrentDateStr();
				for(DoubanBook b : books){
					helper.insert(key_date, b);
				}
				helper.close();
				helper.close();
			}
		}.start();
	}
	
	static String key_date = null;
	
	public static String initCurrentDateStr(){
		if(key_date != null){
			return key_date;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		format.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
		return format.format(Calendar.getInstance().getTimeInMillis());
	}
	
}
