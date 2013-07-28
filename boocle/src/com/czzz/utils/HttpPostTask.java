package com.czzz.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class HttpPostTask extends AsyncTask<Object, Object, Object>{

	HttpListener taskListener;
	Context context;
	
	public HttpPostTask(Context context, HttpListener taskListener){
		this.taskListener = taskListener;
		this.context = context;
	}
	
	@Override
	protected Object doInBackground(Object... data) {
		// TODO Auto-generated method stub
		
		if(!NetworkUtils.isOnline(context)) {
			return NetworkUtils.NETWORK_OFF;
		}
		
		String url = String.valueOf(data[0]);
		@SuppressWarnings("unchecked")
		List<NameValuePair> params = (List<NameValuePair>) data[1];
		
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
            	Toast.makeText(context, httpResponse.getStatusLine().getStatusCode()
            			+ ": " + EntityUtils.toString(httpResponse.getEntity()), Toast.LENGTH_SHORT).show();
            }
            
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
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
		
		if(result != null) {
			String postRespone = TextUtils.unicodeToString(""+result);
			Log.d("Post respone: ", postRespone);
			taskListener.onTaskCompleted(postRespone);
		}
		
//		String[] res = String.valueOf(result).split("\t");
//		if(res[0].equals("200")){
//			// 正常
//			
//		}else{
//			//异常
//			taskListener.onTaskFailed(TextUtils.unicodeToString(res[1]));
//		}
	}
	

}
