package com.czzz.douban;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.demo.CustomAsyncHttpResponseHandler;
import com.czzz.demo.R;
import com.czzz.social.DoubanOAuth;
import com.czzz.utils.HttpDownloadAsyncTask;
import com.czzz.utils.HttpListener;
import com.czzz.utils.NetHttpClient;
import com.czzz.utils.ShelfCoverDownloader;
import com.czzz.utils.XmlDownloadTask;
import com.loopj.android.http.RequestParams;

public class DoubanBookUtils {

	public static void fetchBookInfo(String isbn, CustomAsyncHttpResponseHandler responseHandler){
		
		RequestParams params = new RequestParams();
		params.put("apikey", DoubanOAuth.APP_KEY);
		String url = "https://api.douban.com/v2/book/isbn/" + isbn;
		NetHttpClient.get(url, params, responseHandler);
	}
	
	public static void fetchBookInfo(Context con, String isbn, HttpListener listener){
		
		String url = "https://api.douban.com/v2/book/isbn/" + isbn + "?apikey=" + DoubanOAuth.APP_KEY ;
		new HttpDownloadAsyncTask(con, listener).execute(url);
	}
	
	public static void fetchBookCover(Context context, String url, ImageView cover){
		new ShelfCoverDownloader().download(url, cover);
	}
	
	public static void fetchBookCover(Context context, DoubanBook book, ImageView cover){
		new ShelfCoverDownloader().download(book, cover);
	}
	
	public static void searchBooks(Context con, String keyword, 
			int start, int total ,CustomAsyncHttpResponseHandler responseHandler){
		
		RequestParams params = new RequestParams();
		params.put("apikey", DoubanOAuth.APP_KEY);
		params.put("q", keyword);
		params.put("start", "" + start);
		params.put("count", total + "");
		String url = "https://api.douban.com/v2/book/search?" ;
		NetHttpClient.get(url, params, responseHandler);
		
//		String url = "https://api.douban.com/v2/book/search?";
//		
//		List<NameValuePair> params = new LinkedList<NameValuePair>();
//		params.add(new BasicNameValuePair("q", keyword));
//		params.add(new BasicNameValuePair("start", "" + start));
//		params.add(new BasicNameValuePair("count", total + ""));
//		params.add(new BasicNameValuePair("apikey", DoubanOAuth.APP_KEY));
//		
//		String paramString = URLEncodedUtils.format(params, "utf-8");
//		
//		url += paramString;
//		
//		Log.d("DEBUG", "url: " + url);
//		
//		new HttpDownloadAsyncTask(con, listener).execute(url);
	}
	
	/**
	 * 从json解析，获取Book对象
	 * @param jsonStr
	 * @return
	 */
	public static DoubanBook parseBookInfo(String jsonStr){
		
		JSONObject json = null;
		Log.d("DEBUG", "jsonStr: " + jsonStr);
		
		try {
			json = new JSONObject(jsonStr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(json != null && !json.has("code")) {
			DoubanBook book = new DoubanBook();
			book.init(json);
			return book;
		}else{
			return null;
		}
		
	}
	
	public static ArrayList<DoubanBook> parseSearchBooks(String jsonStr){
		
		ArrayList<DoubanBook> list = new ArrayList<DoubanBook>();
		
		try {
			JSONObject json = new JSONObject(jsonStr);
			JSONArray books = json.getJSONArray("books");
			
			for(int i=0; i<books.length(); i++){
				DoubanBook book = new DoubanBook();
				book.init(new JSONObject(String.valueOf(books.get(i))));
				if(!book.title.equals("") && !book.isbn13.equals(""))
					list.add(book);
			}
			
			return list;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 豆瓣藏书解析
	 * @param jsonStr
	 * @return
	 * @throws JSONException
	 */
	public static ArrayList<BookCollectionEntry> parseCollections(String jsonStr) throws JSONException{
		ArrayList<BookCollectionEntry> list = new ArrayList<BookCollectionEntry>();
		JSONObject json = new JSONObject(jsonStr);
		JSONArray array = json.getJSONArray("collections");
		
		for(int i = 0; i<array.length(); i++){
			BookCollectionEntry entry = new BookCollectionEntry();
			JSONObject item = array.getJSONObject(i);
			entry.status = item.getString("status");
			entry.updated = item.getString("updated");
			JSONObject bookjson = item.getJSONObject("book");
			entry.book = new DoubanBook().init(bookjson);
			list.add(entry);
		}
		
		return list;
	}
	
	/**
	 * 书圈藏书
	 * @param jsonStr
	 * @param owner
	 * @return
	 * @throws JSONException
	 */
	public static ArrayList<BookCollection> parseUserBooks(String jsonStr, User owner) throws JSONException{
		ArrayList<BookCollection> list = new ArrayList<BookCollection>();
		JSONObject json = new JSONObject(jsonStr);
		
		if(json.getInt("status") == 2){
			return null;
		}
		
		JSONArray bookArray = json.getJSONArray("data");
		if(bookArray != null && bookArray.length() == 0){
			return null;
		}
		
		JSONObject item;
		for(int i=0; i < bookArray.length(); i++){
			item = bookArray.getJSONObject(i);
			BookCollection entry = new BookCollection();
			entry.owner = owner.name;
			entry.owner_id = owner.uid;
			entry.owner_avatar = owner.avatar;
			entry.initItem(item);
			list.add(entry);
		}
		
		return list;
	}
	
	public static void fetchBookCollection(Context con, HttpListener listener, int type, String uid){
		String url = "http://api.douban.com/people/" + uid + "/collection?cat=book&max-results=1000"
				+ "&apikey=" + DoubanOAuth.APP_KEY;
		new XmlDownloadTask(con, listener, type).execute(url);
	}
	
	public static void fetchBookCollection2(Context con, HttpListener listener, int type, 
			String uid, int start, int count){
		String url = "https://api.douban.com/v2/book/user/" + uid + "/collections?";
		
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("start", "" + start));
		params.add(new BasicNameValuePair("count", "" + count));
		params.add(new BasicNameValuePair("apikey", DoubanOAuth.APP_KEY));
		
		String paramString = URLEncodedUtils.format(params, "utf-8");
		
		url += paramString;
		
		Log.d("DEBUG", "url: " + url);
		
		new HttpDownloadAsyncTask(con, listener).execute(url);
	}
	
	public static void fetchUserCollection(Context con, HttpListener listener, int type, 
			int uid, int start, int count){
		String url = Pref.USER_COLLECTIONS_URL + "?";
		
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("uid", "" + uid));
		params.add(new BasicNameValuePair("start", "" + start));
		params.add(new BasicNameValuePair("total", "" + count));
		
		String paramString = URLEncodedUtils.format(params, "utf-8");
		
		url += paramString;
		
		Log.d("DEBUG", "url: " + url);
		
		new HttpDownloadAsyncTask(con, listener).execute(url);
	}
	
	public static void fetchBookCollection2(Context con, HttpListener listener, int type, 
			String uid){
		String url = "https://api.douban.com/v2/book/user/" + uid + "/collections?";
		
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		params.add(new BasicNameValuePair("start", "" + 0));
		params.add(new BasicNameValuePair("count", "" + 100));
		params.add(new BasicNameValuePair("apikey", DoubanOAuth.APP_KEY));
		
		String paramString = URLEncodedUtils.format(params, "utf-8");
		
		url += paramString;
		
		Log.d("DEBUG", "url: " + url);
		
		new HttpDownloadAsyncTask(con, listener).execute(url);
	}
	
	public static void fetchBookComments(Context con, HttpListener listener, int type, String isbn){
		String url = "http://api.douban.com/book/subject/isbn/" + isbn + "/reviews"
				+ "?apikey=" + DoubanOAuth.APP_KEY;
		new XmlDownloadTask(con, listener, type).execute(url);
	}
	
//	public static void fetchBookComments(String isbn, 
//			CustomAsyncHttpResponseHandler responseHandler){
//		RequestParams params = new RequestParams();
//		params.put("apikey", DoubanOAuth.APP_KEY);
//		String url = "http://api.douban.com/book/subject/isbn/" + isbn + "/reviews";
//		
//		new XmlDownloadTask(con, listener, type).execute(url);
//		
//		NetHttpClient.get(url, params, responseHandler);
//	}
	
	public static boolean isISBN(String searchStr){
        return searchStr.length() == 10 || searchStr.length() == 13;
//        Toast.makeText(con, R.string.isbn_not_match, Toast.LENGTH_SHORT).show();
	}
	
	public static boolean isISBNLike(String searchStr){
		String pattern = "[0-9]+(.[0-9]+)?";  
        //对()的用法总结：将()中的表达式作为一个整体进行处理，必须满足他的整体结构才可以。  
        //(.[0-9]+)? ：表示()中的整体出现一次或一次也不出现  
        Pattern p = Pattern.compile(pattern);  
        Matcher m = p.matcher(searchStr); 
        
        return m.matches() && searchStr.length() > 8;
	}
	
}
