package com.czzz.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.czzz.utils.ImagesDownloader.BitmapDownloaderTask;
import com.czzz.utils.ImagesDownloader.DownloadedDrawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class ImageDownloader extends AsyncTask<Object, Object, Object> {

	HttpListener taskListener;
	Context context;

	Map<String, Bitmap> imageCache;

	public ImageDownloader(Context con, HttpListener taskListener) {
		this.taskListener = taskListener;
		this.context = con;
		imageCache = new HashMap<String, Bitmap>();
	}

	@Override
	protected Object doInBackground(Object... urls) {
		// TODO Auto-generated method stub
		
		if(!NetworkUtils.isOnline(context)) {
			return NetworkUtils.NETWORK_OFF;
		}
		
		try {
			Bitmap bm = getCachedImage((String) urls[0]);
			
			if (bm != null) {
				Log.d("DEBUG", "use cached cover");
				return bm;
			} else {
				Log.d("DEBUG", "download cover");
				return downloadImage(String.valueOf(urls[0]));
			}
		} catch (IOException e) {
			return NetworkUtils.COVER_NOT_FOUND;
		}
	}

	private Bitmap getCachedImage(String url) {
		// TODO Auto-generated method stub
		// Caching code right here
		String filename = String.valueOf(url.hashCode());
		File f = new File(getCacheDirectory(context), filename);

		// Is the bitmap in our memory cache?
		Bitmap bitmap = null;

		bitmap = (Bitmap) imageCache.get(f.getPath());

		if (bitmap == null) {

			bitmap = BitmapFactory.decodeFile(f.getPath());

			if (bitmap != null) {
				imageCache.put(f.getPath(), bitmap);
			}

		}

		return bitmap;
		
	}
	
	
	/**
	 * result 返回的Bitmap对象
	 */
	@Override
	protected void onPostExecute(Object result) {
		
		if (result instanceof Integer) {
			int tmp = (Integer)result;
			switch(tmp){
			case NetworkUtils.NETWORK_OFF:
				taskListener.onTaskFailed("哎呀，你好像没有打开网络连接...");
				break;
			case NetworkUtils.NETWORK_ERROR:
				taskListener.onTaskFailed("不好意思，网络连接好像好故障了...");
				break;
			case NetworkUtils.COVER_NOT_FOUND:
				taskListener.onTaskFailed("呃，封面木有找到...");
				break;
			}
			return;
		}
		
		taskListener.onTaskCompleted((Bitmap)result);
	}

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	private Object downloadImage(String myurl) throws IOException {
		InputStream is = null;

		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();
			Log.d("DEBUG_TAG", "The response is: " + response);
			is = conn.getInputStream();

			// Convert the InputStream into a bitmap
			Bitmap bm = convertInputStreamToBitmap(is);
			
			String filename = String.valueOf(url.hashCode());
	    	 	File f = new File(getCacheDirectory(context), filename);
	    	 	
	    	 	imageCache.put(f.getPath(), bm);
	    	 	
            writeFile(bm, f);
			
			return bm;

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public Bitmap convertInputStreamToBitmap(InputStream is) {
		Bitmap bitmap = BitmapFactory.decodeStream(is);
		return bitmap;
	}

	// our caching functions
	// Find the dir to save cached images
	private static File getCacheDirectory(Context context) {
		String sdState = android.os.Environment.getExternalStorageState();
		File cacheDir;

		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdDir = android.os.Environment.getExternalStorageDirectory();

			// TODO : Change your direcory here
			cacheDir = new File(sdDir, "data/tac/images");
		} else
			cacheDir = context.getCacheDir();

		if (!cacheDir.exists())
			cacheDir.mkdirs();
		return cacheDir;
	}
	
	private void writeFile(Bitmap bmp, File f) {
		  FileOutputStream out = null;

		  try {
		    out = new FileOutputStream(f);
		    bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
		  } catch (Exception e) {
		    e.printStackTrace();
		  }
		  finally { 
		    try { if (out != null ) out.close(); }
		    catch(Exception ex) {} 
		  }
	}

}
