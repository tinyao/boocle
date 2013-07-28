package com.czzz.bookcircle.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.bookcircle.MyApplication;
import com.czzz.utils.NetworkUtils;

public class BookUploader extends Thread implements Runnable {

	private TaskListener listener;
	private BookCollection collection;

	public BookUploader(TaskListener listener) {
		this.listener = listener;
	}

	public void execute(BookCollection collection) {
		this.collection = collection;
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				listener.onTaskCompleted(msg.obj);
				break;
			case 1:
				listener.onTaskFailed("Error: " + msg.obj);
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public void run() {
		// TODO Auto-generated method stub

		if (!NetworkUtils.isOnline(MyApplication.getInstance())) {
			Message msg = handler.obtainMessage();
			msg.what = 1;
			msg.obj = "哎呀，你好像没有打开网络连接...";
			handler.sendMessage(msg);
			return;
		}

		Log.d("DEBUG", "thread uploading: " + collection.book.title);

		// 上传书籍
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("uid", "" + User.getInstance().uid));
		params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
		params.add(new BasicNameValuePair("isbn", collection.book.isbn13));
		params.add(new BasicNameValuePair("title", collection.book.title));
		params.add(new BasicNameValuePair("subtitle", collection.book.subtitle));
		params.add(new BasicNameValuePair("author", collection.book.author));
		params.add(new BasicNameValuePair("translator",
				collection.book.translator));
		params.add(new BasicNameValuePair("cover", collection.book.image));
		params.add(new BasicNameValuePair("publisher",
				collection.book.publisher));
		params.add(new BasicNameValuePair("pubdate", collection.book.pubdate));
		params.add(new BasicNameValuePair("rate_num", collection.book.rateNum));
		params.add(new BasicNameValuePair("rate_score",
				collection.book.rateAverage));
		params.add(new BasicNameValuePair("summary", collection.book.summary));
		params.add(new BasicNameValuePair("price", collection.book.price));

		params.add(new BasicNameValuePair("note", collection.note));
		params.add(new BasicNameValuePair("status", "" + collection.status));
		params.add(new BasicNameValuePair("score", "" + collection.score));

		String result = postBook(params, Pref.ADD_BOOK_URL);

	}

	public String postBook(List<BasicNameValuePair> params, String url) {

		HttpPost httpRequest = new HttpPost(url);
		HttpResponse httpResponse;
		String result = "";

		try {
			// 发出HTTP request
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			Log.d("DEBUG", "--- " + httpRequest.getURI());
			// 取得HTTP response
			httpResponse = new DefaultHttpClient().execute(httpRequest);

			Message msg = handler.obtainMessage();

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(httpResponse.getEntity());
				msg.what = 0;
			} else {
				result = EntityUtils.toString(httpResponse.getEntity());
				msg.what = 1;
			}

			Log.d("DEBUG", "" + result);

			msg.obj = result;
			handler.sendMessage(msg);

			return result;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
