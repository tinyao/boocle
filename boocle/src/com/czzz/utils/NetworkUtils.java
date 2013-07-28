package com.czzz.utils;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class NetworkUtils {

	public final static int NETWORK_OFF = 0;
	public final static int NETWORK_ON = 1;
	public final static int NETWORK_ERROR = 2;

	public final static int COVER_NOT_FOUND = 3;
	public final static int REVIEWS_NOT_FOUND = 4;

	public static boolean isOnline(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	private static final int REQUEST_TIMEOUT = 10 * 1000;// 设置请求超时10秒钟
	private static final int SO_TIMEOUT = 10 * 1000; // 设置等待数据超时时间10秒钟

	/**
	 * 添加请求超时时间和等待时间
	 * 
	 * @author spring sky Email vipa1888@163.com QQ: 840950105 My name: 石明政
	 * @return HttpClient对象
	 */
	public static HttpClient getHttpClient() {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
		HttpClient client = new DefaultHttpClient(httpParams);
		return client;
	}
	
	public static String postNetworkData(Context con, String url, List<BasicNameValuePair> params){
		
		HttpPost httpRequest = new HttpPost(url);
		HttpResponse httpResponse;
		
		try{
			//发出HTTP request 
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            Log.d("DEBUG", "--- " + httpRequest.getURI());
            //取得HTTP response 
            httpResponse = NetworkUtils.getHttpClient().execute(httpRequest);
            if(httpResponse.getStatusLine().getStatusCode() == 200){
            	return EntityUtils.toString(httpResponse.getEntity());
            }else{
//            	return httpResponse.getStatusLine().getStatusCode()
//            			+ "\t" + EntityUtils.toString(httpResponse.getEntity());
            	Toast.makeText(con, httpResponse.getStatusLine().getStatusCode()
            			+ ": " + EntityUtils.toString(httpResponse.getEntity()), Toast.LENGTH_SHORT).show();
            }
            
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
