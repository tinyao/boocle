package com.czzz.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

import com.czzz.demo.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

public class ImagesDownloader {

	Map<String, Bitmap> imageCache;
	static ImagesDownloader instance;
	int type = -1;
	
	private static Context context;
	public static final int AVATAR_TASK = 1;
	public static final int COVER_TASK = 2;

	public static ImagesDownloader getInstance() {
		
		if (instance == null) {
			return new ImagesDownloader();
		}

		return instance;
	}

	public ImagesDownloader() {
		imageCache = new HashMap<String, Bitmap>();
	}

	public ImagesDownloader(int type) {
		File f = new File("/sdcard/bookcircle");
		if(!f.exists()){
			f.mkdir();
		}
		imageCache = new HashMap<String, Bitmap>();
		this.type = type;
	}

	public void setType(int type){
		this.type = type;
	}
	
	// download function
	public void download(final String url, final ImageView imageView) {

		context = imageView.getContext();
		if (cancelPotentialDownload(url, imageView)) {

			Bitmap bitmap = getCachedImage(imageView.getContext(), url);

			// No? download it
			if (bitmap == null) {
				BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
				DownloadedDrawable downloadedDrawable = new DownloadedDrawable(
						task);
				imageView.setImageDrawable(downloadedDrawable);
				task.execute(url);
			} else {
				// Yes? set the avatar
				if (type == AVATAR_TASK) {
					imageView.setImageBitmap(bitmap);
					ImageUtils.saveBitmap(bitmap, ImageUtils.avatarPath);
				} else {
					imageView.setImageBitmap(bitmap);
				}
			}
		}

	}
	
	// download function
	public void download(final String url, final ImageView imageView, int type) {
		this.type = type;
		download(url, imageView);
	}

	// cancel a download (internal only)
	private static boolean cancelPotentialDownload(String url,
			ImageView imageView) {
		BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null) {
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				bitmapDownloaderTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				return false;
			}
		}
		return true;
	}

	// gets an existing download if one exists for the imageview
	private static BitmapDownloaderTask getBitmapDownloaderTask(
			ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof DownloadedDrawable) {
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	private Bitmap getCachedImage(Context con, String url) {
		// TODO Auto-generated method stub
		// Caching code right here

		// Is the bitmap in our memory cache?
		Bitmap bitmap = null;

		bitmap = (Bitmap) imageCache.get(url);

		if (bitmap == null) {
			String filename = String.valueOf(url.hashCode());
			File f = new File(getCacheDirectory(con), filename);
			if(f.exists()){ // if the image cache exists
				bitmap = BitmapFactory.decodeFile(f.getPath());

				if (bitmap != null) {
					imageCache.put(url, bitmap);
				}
			}
		}

		return bitmap;

	}

	File sdDir = android.os.Environment.getExternalStorageDirectory();
	String sdState = android.os.Environment.getExternalStorageState();

	// our caching functions
	// Find the dir to save cached images
	private File getCacheDirectory(Context context) {
		File cacheDir;

		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			// TODO : Change your direcory here
			cacheDir = new File(sdDir, "data/tac/images");
		} else {
			cacheDir = context.getCacheDir();
		}

		if (!cacheDir.exists())
			cacheDir.mkdirs();
		return cacheDir;
	}

	private void writeFile(Bitmap bmp, File f) {

		Log.d("DEBUG", "save image cache...");
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(f);
			bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception ex) {
			}
		}
	}

	// download asynctask
	public class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private String url;
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		// Actual download method, run in the task thread
		protected Bitmap doInBackground(String... params) {
			// params comes from the execute() call: params[0] is the url.
			url = (String) params[0];
			return downloadBitmap(params[0]);
		}

		@Override
		// Once the image is downloaded, associates it to the imageView
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
				// Change bitmap only if this process is still associated with
				// it
				if (this == bitmapDownloaderTask) {

					if (bitmap == null) {
						// Toast.makeText(imageView.getContext(), "", duration)
						Log.d("DEBUG", "cover not found");
					} else {
						if (type == AVATAR_TASK) {
//							Bitmap bm = ImageUtils.getRoundedCornerBitmap(
//									bitmap, 10);
							ImageUtils.saveBitmap(bitmap, ImageUtils.avatarPath);
							imageView.setImageBitmap(bitmap);
						} else {
							imageView.setImageBitmap(bitmap);
						}
					}
					// cache the image

					String filename = String.valueOf(url.hashCode());
					File f = new File(
							getCacheDirectory(imageView.getContext()), filename);

					imageCache.put(url, bitmap);

					writeFile(bitmap, f);
				}
			}
		}

	}

	class DownloadedDrawable extends BitmapDrawable {
		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

		public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
//			super(Color.BLACK);
//			super(Resources res)
//			super(BitmapFactory.decodeResource(context.getResources(), 
//					R.drawable.avatar_default));
			bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(
					bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}

	// the actual download code
	static Bitmap downloadBitmap(String url) {
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
				HttpVersion.HTTP_1_1);
		HttpClient client = new DefaultHttpClient(params);
		final HttpGet getRequest = new HttpGet(url);

		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				//Log.w("ImageDownloader", "Error " + statusCode
				//		+ " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					final Bitmap bitmap = BitmapFactory
							.decodeStream(inputStream);
					return bitmap;
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			// Could provide a more explicit error message for IOException or
			// IllegalStateException
			getRequest.abort();
			Log.w("ImageDownloader", "Error while retrieving bitmap from "
					+ url + e.toString());
		} finally {
			if (client != null) {
				// client.close();
			}
		}
		return null;
	}

	public static Bitmap getResizedBitmap(Context context, Bitmap bm) {
		int width = bm.getWidth();
		int height = bm.getHeight();

		DisplayMetrics displayMetrics = context.getResources()
				.getDisplayMetrics();

		float newWidth = (float) (displayMetrics.widthPixels / 4.0f);
		// float newHeight = newWidth / width * height;

		float scaleWidth = ((float) newWidth) / width;

		// float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleWidth);

		// RECREATE THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);
		return resizedBitmap;
	}

}
