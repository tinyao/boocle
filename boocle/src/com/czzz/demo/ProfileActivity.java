package com.czzz.demo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.bookcircle.MyApplication;
import com.czzz.bookcircle.task.AlarmTask;
import com.czzz.utils.AvatarUploader;
import com.czzz.utils.HttpListener;
import com.czzz.utils.HttpPostTask;
import com.czzz.utils.ImageUtils;
import com.czzz.utils.ImagesDownloader;
import com.czzz.view.tablelist.widget.UITableView;
import com.czzz.view.tablelist.widget.UITableView.ClickListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ProfileActivity extends AsyncTaskActivity {

	UITableView contactlist, educationList;
	Button logoutBtn;
	ImageView avatarImg;
	View avatarView;
	
	boolean isMyself = true;
	
	private SchoolChooserDialog schoolDialog;
	
	SharedPreferences sp;
	
	boolean isAvatarChange = false, isDescChanged = false, isGenderChanged = false;
	
	User mUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if(intent.hasExtra("user")){
			mUser = (User) intent.getSerializableExtra("user");
			isMyself = false;
		}else{
			mUser = User.getInstance();
		}
		
		mActionBar.setTitle(mUser.name);

		setContentView(R.layout.me_profile);
		
		sp = MyApplication.accoutPref;
		
		initView();

		setUserAvatar();
	}


	private void initView() {
		// TODO Auto-generated method stub
		logoutBtn = (Button) findViewById(R.id.pref_logout);
		avatarView = (View) findViewById(R.id.upload_avatar);
		avatarImg = (ImageView) findViewById(R.id.profile_change_avatar);
		educationList = (UITableView) findViewById(R.id.education_info);
		contactlist = (UITableView) findViewById(R.id.contact_info);
		createList();
		Log.d("Example1Activity", "total items: " + contactlist.getCount());
		contactlist.commit();
		educationList.commit();

		if(!isMyself){
			logoutBtn.setVisibility(View.GONE);
			findViewById(R.id.chevron).setVisibility(View.GONE);
			TextView avatarTxt = (TextView) findViewById(R.id.profile_avatar_txt);
			avatarTxt.setText(mUser.name);
			avatarView.setClickable(false);
			avatarView.setEnabled(false);
		}
		
		logoutBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
				builder.setTitle("确定退出登录?")
					.setPositiveButton("确定", new DialogInterface.OnClickListener(){
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							MyApplication.accoutPref.edit().clear().commit();
							MyApplication.configPref.edit().clear().commit();
							MyApplication.getInstance().clearApplicationData();
							
							AlarmTask.cancelMsgAlarm(ProfileActivity.this);
							
							Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
					        intent.putExtra("finish", true);
					        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
					        startActivity(intent);
					        finish();
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					})
					.create().show();
				
//				SharedPreferences sp = ProfileActivity.this
//						.getSharedPreferences("account", 0);
//				MyApplication.accoutPref.edit().clear().commit();
//				sp = ProfileActivity.this.getSharedPreferences("config", 0);
//				MyApplication.configPref.edit().clear().commit();
				
			}

		});

		avatarView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ImageUtils.showPickDialog(ProfileActivity.this);
			}

		});
		
		View pwv = findViewById(R.id.profile_change_passwd);
		if(mUser.uid != User.getInstance().uid) pwv.setVisibility(View.GONE);
		pwv.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent chpw = new Intent(ProfileActivity.this, PasswdChangeActivity.class);
				startActivity(chpw);
			}
			
		});
	}

	protected void uploadAvatar() {
		// TODO Auto-generated method stub
		AvatarUploader uploader = new AvatarUploader(this, (HttpListener) this);
		this.taskType = HttpListener.PROFILE_CHANGE_AVATAR;
		uploader.execute(ImageUtils.avatarPath);
	}

	private void createList() {
		CustomClickListener listener_1 = new CustomClickListener(R.id.contact_info);
		CustomClickListener listener_2 = new CustomClickListener(R.id.education_info);
		contactlist.setClickListener(listener_1);
		educationList.setClickListener(listener_2);
		
		Resources res= getResources();
		
		if(isMyself) educationList.addBasicItem(
				res.getString(R.string.profile_nick), 
				mUser.name.equals("") ? res.getString(R.string.profile_empty) : mUser.name, isMyself);
		educationList.addBasicItem(res.getString(R.string.profile_school), 
				mUser.school_name.equals("") ? res.getString(R.string.profile_empty) : mUser.school_name, isMyself);
		educationList.addBasicItem(res.getString(R.string.profile_major), 
				mUser.major.equals("") ? res.getString(R.string.profile_empty) : mUser.major, isMyself);
		educationList.addBasicItem(res.getString(R.string.profile_gender), 
				mUser.gender == 1 ? res.getString(R.string.profile_gender_girl) : res.getString(R.string.profile_gender_boy), isMyself);
		educationList.addBasicItem(res.getString(R.string.profile_desc), 
				mUser.desc.equals("") ? res.getString(R.string.profile_no_desc) : mUser.desc, isMyself);
		
		contactlist.addBasicItem(res.getString(R.string.profile_web), 
				mUser.site.equals("") ? res.getString(R.string.profile_empty) : mUser.site, isMyself);
		if(isMyself) contactlist.addBasicItem(res.getString(R.string.profile_email), 
				mUser.email.equals("") ? res.getString(R.string.profile_empty) : mUser.email, false);
		contactlist.addBasicItem("QQ  : ", 
				mUser.im.equals("") ? res.getString(R.string.profile_empty) : mUser.im, isMyself);
		
	}
	
	

	private class CustomClickListener implements ClickListener {
		
		private int viewId;
		
		public CustomClickListener(int resId){
			viewId = resId;
		}
		
		@Override
		public void onClick(int index) {
			switch(viewId){
			case R.id.education_info:
				switch(index){
				case 0:
					showUpdateDialog(0, 0, "修改昵称", mUser.name);
					break;
				case 1:	// 学校
					if(schoolDialog == null){
						schoolDialog = new SchoolChooserDialog(
								ProfileActivity.this, R.style.school_dialog_style);
					}
					schoolDialog.setOnDismissListener(new OnDismissListener(){

						@Override
						public void onDismiss(DialogInterface dialogInterface) {
							// TODO Auto-generated method stub
							String school = schoolDialog.updateUniversity;
							int sid = schoolDialog.updateUnivId;
							if(school != null && sid != 0){
								educationList.update(1, school);
								sp.edit().putString("school_name", school)
									.putInt("school_id", sid).commit();
								mUser.school_name = school;
								mUser.school_id = sid;
							}
						}
						
					});
					schoolDialog.show();
					break;
				case 2:	//专业
					showUpdateDialog(0, 2, "你的专业", mUser.major);
					break;
				case 3: //性别
					showGenderDialog();
					break;
				case 4: //签名
					showUpdateDialog(0, 4, "填写签名", mUser.desc);
					break;
				}
				break;
			case R.id.contact_info:
				switch(index){
				case 0: //手机
					showUpdateDialog(1, 0, "个人网页", mUser.site);
					break;
				case 1: //邮箱
//					showUpdateDialog(1 ,1, "你的邮箱", mUser.email);
					break;
				case 2: //IM
					showUpdateDialog(1, 2, "即时通信", mUser.im);
					break;
				}
				break;
			}
		
		}
	}
	
	private void showUpdateDialog(final int group, final int type, 
			String title, String preValue){
		LayoutInflater inflater = (LayoutInflater) ProfileActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.dialog_profile_edit, null);
        final EditText edt = (EditText) view.findViewById(R.id.profile_dialog_edit);
        edt.setText(preValue);
//        if(group==1 && type==0) edt.setInputType(InputType.TYPE_CLASS_NUMBER);
        edt.setSelection(preValue.length());
        AlertDialog editDialog = new AlertDialog.Builder(ProfileActivity.this)
        	.setTitle(title).setView(view)
        	.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(group == 0){ // education
						educationList.update(type, edt.getText().toString());
					} else { // contact
						contactlist.update(type, edt.getText().toString());
					}
					updateUserInfo(group, type, edt.getText().toString());
				}
        	})
        	.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
        	}).create();
        
        editDialog.setCanceledOnTouchOutside(true);
        editDialog.show();
        
	}

	protected void updateUserInfo(int group, int type, String paramValue) {
		// TODO Auto-generated method stub
		String field = "";
		switch(group){
		case 0:
			switch(type){
			case 0:
				mUser.name = paramValue;
				sp.edit().putString("name", paramValue).commit();
				field = "name";
				break;
			case 2: // major
				mUser.major = paramValue;
				sp.edit().putString("major", paramValue).commit();
				field = "major";
				break;
			case 3:
				mUser.gender = Integer.valueOf(paramValue);
				sp.edit().putInt("gender", mUser.gender).commit();
				field = "gender";
				isGenderChanged = true;
				break;
			case 4:
				Log.d("DEBUG", "desc: " + paramValue);
				mUser.desc = paramValue;
				sp.edit().putString("desc", paramValue).commit();
				isDescChanged = true;
				field = "desc";
				break;
			}
			break;
		case 1:
			switch(type){
			case 0:
				mUser.site = paramValue;
				sp.edit().putString("site", paramValue).commit();
				field = "site";
				break;
			case 1:
				mUser.email = paramValue;
				sp.edit().putString("email", paramValue).commit();
				field = "email";
				break;
			case 2:
				mUser.im = paramValue;
				sp.edit().putString("im", paramValue).commit();
				field = "im";
				break;
			}
			break;
		}
		
		if(!field.equals("")){
			
			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
			params.add(new BasicNameValuePair("uid", ""+User.getInstance().uid));
			params.add(new BasicNameValuePair("passwd", User.getInstance().pass));
			params.add(new BasicNameValuePair(field, "" + paramValue));
			
			new HttpPostTask(this, (HttpListener)this).execute(Pref.USER_INFO_UPDATE, params);
		}
		
	}
	
	public void showGenderDialog(){  
		   
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);  
	    
	    builder.setSingleChoiceItems(R.array.gender_choose, mUser.gender - 1, new DialogInterface.OnClickListener() {  
	   
	        @Override  
	        public void onClick(DialogInterface dialog, int which) {  
	            //which是选中的位置(基于0的)  
	            String[] items = getResources().getStringArray(R.array.gender_choose);  
	            //Toast.makeText(ProfileActivity.this, which + "--" + items[which], Toast.LENGTH_LONG).show();
	            dialog.dismiss();
	            
	            educationList.update(3, which == 0 ? "女" : "男"); // 更新列表显示
	            updateUserInfo(0, 3, "" + (which + 1)); // 更新性别 1为女 2为男
	        }  
	    }); 

	    AlertDialog ad = builder.create();
	    ad.setCanceledOnTouchOutside(true);
	    ad.show();  
	}  


	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// TODO Auto-generated method stub

		switch (item.getItemId()) {
		case R.id.abs__home:
			finishWithResult();
			break;
		case android.R.id.home:
			finishWithResult();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTaskCompleted(Object newAvatarName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Crouton.makeText(this, data, Style.ALERT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		// 如果是直接从相册获取
		case 1:
			if (data != null)
				startPhotoZoom(data.getData());
			break;
		// 如果是调用相机拍照时
		case 2:
//			Log.d("DEBUG", "camera data: " + data.getExtras());
			if(data == null){
				File temp = new File(ImageUtils.SDFile,
						ImageUtils.APP_DIR + "/camera.jpg");
				startPhotoZoom(Uri.fromFile(temp));
			}
			break;
		// 取得裁剪后的图片
		case 3:
			if (data != null) {
				Bitmap bm = ImageUtils.resizeAvatarFile();
				if(bm != null){
					uploadAvatar();
					avatarImg.setImageBitmap(bm);
					isAvatarChange = true;
				}else{
					isAvatarChange = false;
					Crouton.makeText(this, "获取图片失败，请重试", Style.ALERT).show();
				}
			}
			break;
		default:
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		// intent.putExtra("outputX", 450);
		// intent.putExtra("outputY", 450);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri()); 
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); // 输入文件格式
		startActivityForResult(intent, 3);
	}

	private Uri getTempUri() {
		return Uri.fromFile(getTempFile());
	}
	
	private File getTempFile() {
		if (isSDCARDMounted()) {
			File f = new File(ImageUtils.SDFile,
					ImageUtils.APP_DIR + "/avatar.jpg");
			try {
				f.createNewFile();
			} catch (IOException e) {

			}
			return f;
		} else {
			return null;
		}
	}

	private boolean isSDCARDMounted() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED))
			return true;
		return false;
	}

	private void setUserAvatar() {
		// TODO Auto-generated method stub
		if(!mUser.avatar.equals("")) {
			new ImagesDownloader(ImagesDownloader.AVATAR_TASK)
				.download(mUser.avatar, avatarImg);
		}
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK){
			finishWithResult();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void finishWithResult(){
		Log.d("DEBUG", "intent back");
		Intent i = new Intent("update_user_info");  
        Bundle b = new Bundle();  
        b.putBoolean("avatar_change", isAvatarChange);
        b.putBoolean("desc_change", isDescChanged);
        b.putBoolean("gender_change", isGenderChanged);
        i.putExtras(b);  
        this.sendBroadcast(i);
	}

}
