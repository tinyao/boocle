package com.czzz.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.util.Log;

import com.czzz.social.net.Weibo;

public class PlacesUtils {

	/**
	 * 解析新浪位置返回的json，获取位置列表
	 * @param jsonStr
	 * @return
	 * @throws JSONException
	 */
	public static ArrayList<Map<String,String>> parsePlacesNearby(String jsonStr) throws JSONException{
		JSONObject json = new JSONObject(jsonStr);
		JSONArray places = json.getJSONArray("pois");
		
		ArrayList<Map<String,String>> list = new ArrayList<Map<String,String>>();
		
		for(int i=0; i<places.length(); i++){
			// parse each place in the array
			JSONObject j = new JSONObject(String.valueOf(places.get(i)));
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("name", j.getString("title"));
			map.put("address", j.getString("address"));
			map.put("lat", j.getString("lat"));
			map.put("lon", j.getString("lon"));
			map.put("city", j.getString("city"));
			map.put("icon", j.getString("icon"));
			list.add(map);
		}
		
		return list;
	}
	
	/**
	 * 新浪返回的位置json是unicode码，将其转化为普通字符串
	 * @param str
	 * @return
	 */
	public static String unicodeToString(String str) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))"); 
		Matcher matcher = pattern.matcher(str);
		char ch;
		while(matcher.find()){
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			str = str.replace(matcher.group(1), ch + "");   
		}
		return str;

	}
	
	/**
	 * 获取附近地点
	 * @param context
	 * @param params
	 * @param placesListener
	 */
	public static void getPlacesNearby(Context context, List<NameValuePair> params, 
			HttpListener placesListener) {
		
		String url = Weibo.SERVER + "place/nearby/pois.json" + "?source=" + Weibo.getAppKey() + "&";
		
		String paramString = URLEncodedUtils.format(params, "utf-8");
		
		url += paramString;
		
		Log.d("DEBUG", "url： " + url);
		
		new HttpDownloadAsyncTask(context, placesListener).execute(url);
		
	}
	
}
