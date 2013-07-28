package com.czzz.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.bookcircle.MyApplication;
import com.czzz.demo.R;

public class AvatarUploader extends AsyncTask<Object, Object, Object> {

	HttpListener taskListener;
	Context context;

	public AvatarUploader(Context context, HttpListener taskListener) {
		this.taskListener = taskListener;
		this.context = context;
	}

	@Override
	protected Object doInBackground(Object... data) {
		// TODO Auto-generated method stub

		if (!NetworkUtils.isOnline(context)) {
			return NetworkUtils.NETWORK_OFF;
		}

		try {
			return uploadAvatar((String) data[0]); // file
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
//		Log.d("DEBUG", ""+values);
//		notification.setLatestEventInfo(context, "上传头像...", String.valueOf(values) + "%", null);
//		mNotificationManager.notify(200, notification);
	}

	private String uploadAvatar(String file) throws IOException {
		// TODO Auto-generated method stub

		showNotification();
		
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;
		String exsistingFileName = file;

		// Is this the place are you doing something wrong.
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		String responseFromServer = "";

		try {
			// ------------------ CLIENT REQUEST
			Log.e("MediaPlayer", "Inside second Method");
			FileInputStream fileInputStream = new FileInputStream(new File(
					exsistingFileName));

			// open a URL connection to the Servlet
			URL url = new URL(Pref.AVATAR_UPLOADER_URL);

			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();

			// Allow Inputs
			conn.setDoInput(true);

			// Allow Outputs
			conn.setDoOutput(true);

			// Don't use a cached copy.
			conn.setUseCaches(false);

			// Use a post method.
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uid\""
					+ lineEnd);
			dos.writeBytes("Content-Type: text/plain; charset=" + "UTF-8"
					+ lineEnd);
			dos.writeBytes(lineEnd + User.getInstance().uid);
			dos.writeBytes(lineEnd);

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"passwd\""
					+ lineEnd);
			dos.writeBytes("Content-Type: text/plain; charset=" + "UTF-8"
					+ lineEnd);
			dos.writeBytes(lineEnd + User.getInstance().pass);
			dos.writeBytes(lineEnd);

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\""
					+ exsistingFileName + "\"" + lineEnd);
			dos.writeBytes(lineEnd);
			Log.e("MediaPlayer", "Headers are written");

			// create a buffer of maximum size
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			Log.e("MediaPlayer", "File is written");
			fileInputStream.close();
			dos.flush();
			dos.close();

		} catch (MalformedURLException ex) {
			Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
		} catch (IOException ioe) {
			Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
		}

		// ------------------ read the SERVER RESPONSE
		try {
			inStream = new DataInputStream(conn.getInputStream());
			String str;
			StringBuilder sb = new StringBuilder();

			while ((str = inStream.readLine()) != null) {
				sb.append(str);
				Log.e("MediaPlayer", "Server Response" + str);
			}

			inStream.close();
			return sb.toString();
		} catch (IOException ioex) {
			Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
			return ioex.getMessage();
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
		 
		 try {
			JSONObject json = new JSONObject(String.valueOf(result));
			if(json.getInt("status") == 1){
				// 头像上传成功，返回图片在服务器端的文件名
				String newAvatarName = json.getString("data");
				if (String.valueOf(newAvatarName).contains(".jpg")){
					User.getInstance().avatar = Pref.AVATAR_BASE_URL + newAvatarName;
					MyApplication.accoutPref.edit().putString("avatar", Pref.AVATAR_BASE_URL + newAvatarName).commit();
//					sp.edit().putString("avatar", Pref.AVATAR_BASE_URL + newAvatarName).commit();
					
					
					mNotificationManager.cancel(200);
				}
				
			}else{
				taskListener.onTaskFailed(unicodeToString(json.getString("data")));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 Log.d("DEBUG", "upload over...");
	}

	private Notification notification;
	private NotificationManager mNotificationManager;
	
	private void showNotification(){
		
		mNotificationManager 
			= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		int icon = R.drawable.ic_launcher;        // icon from resources
		CharSequence tickerText = "正在上传头像";              // ticker-text
		long when = System.currentTimeMillis();         // notification time
		CharSequence contentTitle = "上传头像...";  // message title
		CharSequence contentText = "";

		// the next two lines initialize the Notification, using the configurations above
		notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText, null);
		notification.flags = Notification.FLAG_ONGOING_EVENT|Notification.FLAG_AUTO_CANCEL;
		PendingIntent contentIntent = PendingIntent.getActivity(
				context, 
		        R.string.app_name, 
		        new Intent(), 
		        PendingIntent.FLAG_UPDATE_CURRENT);
		notification.contentIntent = contentIntent;
		
		mNotificationManager.notify(200, notification);
	}
	
	/**
	 * 新浪返回的位置json是unicode码，将其转化为普通字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String unicodeToString(String str) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str);
		char ch;
		while (matcher.find()) {
			ch = (char) Integer.parseInt(matcher.group(2), 16);
			str = str.replace(matcher.group(1), ch + "");
		}
		return str;

	}
	
	public static String stringToUnicode(String str){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<str.length(); i++){
			if(i<=256){
				
			}
			sb.append("%" + Integer.toHexString(str.charAt(i)));
		}
		Log.d("DEBUG", "" + sb);
		return sb.toString();
	}

}
