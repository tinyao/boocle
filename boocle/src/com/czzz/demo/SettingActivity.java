package com.czzz.demo;

import java.io.File;

import com.czzz.demo.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.czzz.base.BaseActivity;
import com.czzz.base.Pref;
import com.czzz.bookcircle.MyApplication;
import com.czzz.bookcircle.task.AlarmTask;
import com.czzz.data.SearchDBHelper;
import com.czzz.data.SearchResultDBHelper;
import com.czzz.utils.HttpDownloadAsyncTask;
import com.czzz.utils.HttpListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class SettingActivity extends BaseActivity implements HttpListener{

	private View msgDetectView, detectIntervalView;
	private View bookCoverView, picCacheView;
	private View searchCoverView, searchCacheView, hideTabsView;
	private View suggestView, updateView, aboutView, feedbackView, donateView;

	private CheckBox msgDetectToggle, displayCoverToggle, highCoverToggle, hideTabsToggle;
	private TextView detectIntervalTxt, versionNow;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d("DEBUG_ALARM", "Setting onCreate...");
		setContentView(R.layout.setting);

		initView();
		fillView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		msgDetectView = (View) findViewById(R.id.setting_noti_detect);
		msgDetectToggle = (CheckBox) findViewById(R.id.setting_noti_detect_toggle);
		detectIntervalView = (View) findViewById(R.id.setting_noti_interval);
		detectIntervalTxt = (TextView) findViewById(R.id.setting_noti_interval_txt);

		bookCoverView = (View) findViewById(R.id.setting_pic_cover);
		highCoverToggle = (CheckBox) findViewById(R.id.setting_pic_cover_high_toggle);
		picCacheView = (View) findViewById(R.id.setting_pic_cache_delete);
		hideTabsView = (View) findViewById(R.id.setting_display_tab);
		hideTabsToggle = (CheckBox) findViewById(R.id.setting_display_tab_toggle);

		searchCoverView = (View) findViewById(R.id.setting_search_cover);
		displayCoverToggle = (CheckBox) findViewById(R.id.setting_search_cover_toggle);
		searchCacheView = (View) findViewById(R.id.setting_search_cache_delete);

		suggestView = (View) findViewById(R.id.setting_about_suggest);
		updateView = (View) findViewById(R.id.setting_about_version_update);
		aboutView = (View) findViewById(R.id.setting_about_about);
		versionNow = (TextView) findViewById(R.id.setting_about_version_now);
		feedbackView = (TextView) findViewById(R.id.setting_about_feedback);
		donateView = (TextView) findViewById(R.id.setting_about_donate);

		msgDetectView.setOnClickListener(listener);
		searchCoverView.setOnClickListener(listener);
		hideTabsView.setOnClickListener(listener);
		detectIntervalView.setOnClickListener(listener);
		bookCoverView.setOnClickListener(listener);
		picCacheView.setOnClickListener(listener);
		searchCacheView.setOnClickListener(listener);
		aboutView.setOnClickListener(listener);
		updateView.setOnClickListener(listener);
		suggestView.setOnClickListener(listener);
		feedbackView.setOnClickListener(listener);
		donateView.setOnClickListener(listener);

		msgDetectToggle.setOnCheckedChangeListener(checkListener);
		displayCoverToggle.setOnCheckedChangeListener(checkListener);
		highCoverToggle.setOnCheckedChangeListener(checkListener);
		hideTabsToggle.setOnCheckedChangeListener(checkListener);
		
		versionNow.setText(getCurrentVersionName());
	}

	private void fillView() {
		// TODO Auto-generated method stub
		msgDetectToggle.setChecked(MyApplication.configPref.getBoolean(
				"msg_alarm_toggle", true));
		displayCoverToggle.setChecked(MyApplication.displayCover);
		highCoverToggle.setChecked(MyApplication.highCover);
		hideTabsToggle.setChecked(MyApplication.hideTabs);

		int interval = MyApplication.configPref.getInt("msg_interval", 3);
		String[] intervalTxs = SettingActivity.this.getResources()
				.getStringArray(R.array.msg_check_interval);
		detectIntervalTxt.setText(intervalTxs[getIntervalIndex(interval)]);

	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.setting_noti_detect:
				msgDetectToggle.setChecked(!msgDetectToggle.isChecked());
				break;
			case R.id.setting_search_cover:
				displayCoverToggle.setChecked(!displayCoverToggle.isChecked());
				break;
			case R.id.setting_display_tab:
				hideTabsToggle.setChecked(!hideTabsToggle.isChecked());
				break;
			case R.id.setting_pic_cover:
				highCoverToggle.setChecked(!highCoverToggle.isChecked());
				break;
			case R.id.setting_noti_interval:

				AlertDialog.Builder builder = new AlertDialog.Builder(
						SettingActivity.this);

				int interval = MyApplication.configPref.getInt("msg_interval",
						3);

				builder.setSingleChoiceItems(R.array.msg_check_interval,
						getIntervalIndex(interval),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// which是选中的位置(基于0的)
								dialog.dismiss();
								int[] interValues = SettingActivity.this
										.getResources()
										.getIntArray(
												R.array.msg_check_interval_value);
								String[] intervalTxs = SettingActivity.this
										.getResources().getStringArray(
												R.array.msg_check_interval);
								MyApplication.configPref
										.edit()
										.putInt("msg_interval",
												interValues[which]).commit();
								detectIntervalTxt.setText(intervalTxs[which]);

								// update the alarm
								AlarmTask.setMsgAlarm(SettingActivity.this);
							}
						});

				AlertDialog ad = builder.create();
				ad.setCanceledOnTouchOutside(true);
				ad.show();
				break;
			case R.id.setting_pic_cache_delete:
				deletePicCache();
				break;
			case R.id.setting_search_cache_delete:
				new Thread() {
					public void run() {
						SearchResultDBHelper resulthelper = new SearchResultDBHelper(
								SettingActivity.this);
						SearchDBHelper searchHelper = new SearchDBHelper(
								SettingActivity.this);
						resulthelper.openDataBase();
						searchHelper.openDataBase();
						resulthelper.deleteAll();
						searchHelper.deleteAll();
						resulthelper.close();
						searchHelper.close();
					}
				}.start();
				break;
			case R.id.setting_about_about:
				startActivity(new Intent(SettingActivity.this, AboutApp.class));
				break;
			case R.id.setting_about_suggest:
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, "" + SettingActivity.this.getResources()
						.getString(R.string.invite_friends_msg));
				sendIntent.setType("text/plain");
				startActivity(sendIntent);
				break;
			case R.id.setting_about_version_update:
				checkUpdateVersion();
				break;
			case R.id.setting_about_feedback:
				Intent feed = new Intent(SettingActivity.this, FeedbackActivity.class);
				startActivity(feed);
				break;
			case R.id.setting_about_donate:
				Intent i = new Intent(Intent.ACTION_VIEW, 
					       Uri.parse("https://me.alipay.com/yaochz"));
				startActivity(i);
				break;
			}
		}

	};

	OnCheckedChangeListener checkListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			switch (buttonView.getId()) {
			case R.id.setting_noti_detect_toggle:
				if (isChecked) {
					AlarmTask.setMsgAlarm(SettingActivity.this);
					MyApplication.configPref.edit()
							.putBoolean("msg_alarm_toggle", true).commit();
				} else {
					AlarmTask.cancelMsgAlarm(SettingActivity.this);
					MyApplication.configPref.edit()
							.putBoolean("msg_alarm_toggle", false).commit();
				}
				break;
			case R.id.setting_search_cover_toggle:
				MyApplication.configPref.edit()
						.putBoolean("display_search_cover", isChecked).commit();
				MyApplication.displayCover = isChecked;
				break;
			case R.id.setting_pic_cover_high_toggle:
				MyApplication.configPref.edit()
						.putBoolean("display_search_cover", isChecked).commit();
				MyApplication.highCover = isChecked;
				break;
			case R.id.setting_display_tab_toggle:
				MyApplication.configPref.edit().putBoolean("hide_tabs", isChecked).commit();
				MyApplication.hideTabs = isChecked;
				Toast.makeText(SettingActivity.this, "重启应用后生效", Toast.LENGTH_SHORT).show();
				break;
			}
		}

	};

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1: // delete the image cache
				pd.dismiss();
				pd = null;
				break;
			}
			super.handleMessage(msg);
		}

	};

	private int getIntervalIndex(int interval) {
		int index = 0;
		switch (interval) {
		case 3:
			index = 0;
			break;
		case 5:
			index = 1;
			break;
		case 10:
			index = 2;
			break;
		case 30:
			index = 3;
			break;
		case 60:
			index = 4;
			break;
		case 120:
			index = 5;
			break;
		}
		return index;
	}

	ProgressDialog pd;

	protected void deletePicCache() {
		// TODO Auto-generated method stub
		if (pd == null)
			pd = new ProgressDialog(this);
		pd.setMessage(getResources().getString(R.string.removing_images_cache));
		pd.show();
		final File sdDir = android.os.Environment.getExternalStorageDirectory();
		new Thread() {
			public void run() {
				File cacheDir = new File(sdDir, "data/tac/images");
				deleteDir(cacheDir);
				mHandler.sendEmptyMessage(1);
			}
		}.start();
	}

	private boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			// 递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return false;
		}

		return super.onKeyDown(keyCode, event);
	}

	public int getCurrentVersionCode() {
		try {
			PackageInfo info = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0);
			int versionCode = info.versionCode;
			return versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public String getCurrentVersionName() {
		try {
			PackageInfo info = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private void checkUpdateVersion() {
		// TODO Auto-generated method stub
		new HttpDownloadAsyncTask(this, this).execute(Pref.VERSION_JSON_URL);
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		// update version info
		int versionCode = getCurrentVersionCode();

		try {
			JSONObject json = new JSONObject("" + data);
			int remoteVerCode = json.getInt("verCode");
			if (remoteVerCode > versionCode) {
				// 有新版本
				String remoteVerName = json.getString("verName");
				String remoteVerInfo = json.getString("verInfo");
				String remoteVerApk = json.getString("apkName");
				String remoteUrl = json.getString("url");

				showUpdateDialog(remoteVerName, remoteVerInfo, remoteUrl);

			}else{
				Crouton.makeText(this, R.string.no_new_version, Style.ALERT).show();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Crouton.makeText(this, data, Style.ALERT).show();
	}
	
	private void showUpdateDialog(String verName, String verInfo, final String url) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
		builder.setTitle(getResources().getString(R.string.new_version) + ": " + verName)
				.setMessage(verInfo)
				.setPositiveButton(R.string.update_version_now,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								Intent ii = new Intent("bookcircle.task.download_apk");
								ii.putExtra("download_url", url);
								sendBroadcast(ii);
								
//								new FileDownLoader()
							}
						})
				.setNegativeButton(R.string.update_version_later,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
				}).create().show();
	}

}
