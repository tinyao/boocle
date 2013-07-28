package com.czzz.demo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.czzz.demo.R;
import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.czzz.social.SinaOAuth;
import com.czzz.social.net.AccessToken;
import com.czzz.social.net.DialogError;
import com.czzz.social.net.Weibo;
import com.czzz.social.net.WeiboDialogListener;
import com.czzz.social.net.WeiboException;
import com.czzz.utils.HttpListener;
import com.czzz.utils.PlacesUtils;

public class PlacesActivity extends SherlockListActivity{
	
	ActionBar mActionBar;
	TextView tv;
	Weibo weibo;
	SharedPreferences sp;
	String accessToken;
	ProgressDialog pd;
	
	LocationManager lm;
	MylocationListener locationListener;
	
	boolean hasLocated = false;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(SampleList.THEME); //Used for theme switching in samples
        super.onCreate(savedInstanceState);
        mActionBar = getSupportActionBar();

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayUseLogoEnabled(false);
        
        setContentView(R.layout.places_nearby);
        
        tv = (TextView) findViewById(R.id.places_result);
        Button sinaBtn = (Button) findViewById(R.id.sina_oauth);
		
		weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(SinaOAuth.APP_KEY,
				SinaOAuth.APP_SECRET);
		weibo.setRedirectUrl(SinaOAuth.REDIRECT_URL);
		
		sp = this.getSharedPreferences("sina_token", 0);
		accessToken = sp.getString("access_token", "");
		
		if(!accessToken.equals("")){
			sinaBtn.setVisibility(View.GONE);
		}
		
		lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new MylocationListener();
		
		sinaBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				weibo.authorize(PlacesActivity.this,
						new MyWeiboDialogListener());
			}
			
		});
		
		fetchCurrentLocation();
    }
	
	/**
	 * 
	 */
	public void fetchCurrentLocation(){
		
		if(lm == null){
			lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		}
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3600*60*24, 100000000, new MylocationListener());
		
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// TODO Auto-generated method stub
//		getSupportMenuInflater().inflate(R.menu.menu_places, menu);
//		return super.onCreateOptionsMenu(menu);
//	}

//	@Override
//	public boolean onOptionsItemSelected(
//			com.actionbarsherlock.view.MenuItem item) {
//		// TODO Auto-generated method stub
//		
//		switch(item.getItemId()){
//		case R.id.abs__home:
//			finish();
//			break;
//		case android.R.id.home:
//			finish();
//			break;
//		case R.id.menu_locate:
//			fetchCurrentLocation();
//			break;
//		}
//		
//		return super.onOptionsItemSelected(item);
//	}
	

	/**
	 * 用于位置定位的回调监听
	 * @author tinyao
	 *
	 */
	class MylocationListener implements LocationListener{

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub

			tv.setText("location:\n" + location.getLatitude() + ":" + location.getLongitude());
			
			
			pd = new ProgressDialog(PlacesActivity.this);
			pd.setMessage("正在获取附近地点...");
			pd.show();
			
			List<NameValuePair> params = new LinkedList<NameValuePair>();
			params.add(new BasicNameValuePair("lat", "" + location.getLatitude()));
			params.add(new BasicNameValuePair("long", "" + location.getLongitude()));
			params.add(new BasicNameValuePair("range", "2000"));
			params.add(new BasicNameValuePair("count", "20"));
			params.add(new BasicNameValuePair("access_token", accessToken));
			
			HttpTaskListener placesListener = new HttpTaskListener(HttpListener.FETCH_SINA_PLACES);
			PlacesUtils.getPlacesNearby(PlacesActivity.this, params, placesListener);

			lm.removeUpdates(locationListener);
			lm = null;
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	/**
	 * 用于新浪微博OAuth认证的回调监听器
	 * @author tinyao
	 *
	 */
	class MyWeiboDialogListener implements WeiboDialogListener {

		@Override
		public void onComplete(Bundle values) {
			/***
			 * 保存token and expires_in
			 */

			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			
			sp = PlacesActivity.this.getSharedPreferences("sina_token", 0);
			sp.edit().putString("access_token", token)
				.putString("expires_in", expires_in).commit();
			
			tv.setText("access_token : " + token + "  expires_in: "
					+ expires_in);
			AccessToken accessToken = new AccessToken(token,
					SinaOAuth.APP_SECRET);
			accessToken.setExpiresIn(expires_in);
			weibo.setAccessToken(accessToken);

			PlacesActivity.this.accessToken = token;
		}

		@Override
		public void onError(DialogError e) {
			Toast.makeText(getApplicationContext(),
					"Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "Auth cancel",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getApplicationContext(),
					"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}
	
	/**
	 * Http请求监听器，用于处理HttpAsyncTask中的响应事件
	 * @author tinyao
	 *
	 */
	private class HttpTaskListener implements HttpListener{

		int type;
		
		public HttpTaskListener(int type1){
			this.type = type1;
		}
		
		@Override
		public void onTaskCompleted(Object data) {
			// TODO Auto-generated method stub
			switch(type){
			case HttpListener.FETCH_SINA_PLACES:
				
				String rrt = PlacesUtils.unicodeToString(String.valueOf(data));
				// 解析返回的json字符串
				try {
					ArrayList<Map<String,String>> list = PlacesUtils.parsePlacesNearby(rrt);
					
					// 将数据填充到列表中
					fillPlaces2List(list);
					
					
//					tv.setText("places:");
//					for(Map<String,String> map : list){
//						tv.append("\n\n" + map);
//					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				if(pd != null){
					pd.dismiss();
				}
				
				break;
			}
			
		}

		@Override
		public void onTaskFailed(String data) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	protected void fillPlaces2List(ArrayList<Map<String,String>> list){
		
		ListView listview = this.getListView();
		
		SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.douban_import_list_item,
                new String[]{"name","address"},
                new int[]{R.id.import_book_title,R.id.import_book_publisher});
		setListAdapter(adapter);
	}
	
}
