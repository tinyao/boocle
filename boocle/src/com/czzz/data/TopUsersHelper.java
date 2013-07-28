package com.czzz.data;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.czzz.base.User;

public class TopUsersHelper extends SQLiteOpenHelper{

	private final static String DATABASE_NAME = "users.db";
	private final static int DATABASE_VERSION = 1;
	private final static String TABLE_NAME = "top_users";
	public final static String KEY_ID = "_id";
	
	Context context;

	SQLiteDatabase resultsDb;
	
	public TopUsersHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	// 创建table
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		// 创建person表
		db.execSQL("CREATE TABLE " + TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ "uid" + " INTEGER, "
				+ "name" + " VARCHAR, "
				+ "email" + " VARCHAR, "
				+ "avatar" + " VARCHAR, "
				+ "desc" + " VARCHAR, "
				+ "book_total" + " INTEGER, "
				+ "fav_total" + " INTEGER, "
				+ "school_id" + " INTEGER, "
				+ "school_name" + " VARCHAR, "
				+ "gender" + " INTEGER, "
				+ "major" + " VARCHAR, "
				+ "phone" + " VARCHAR, "
				+ "im" + " VARCHAR, "
				+ "create_at" + " VARCHAR)");
	}

	/**
	 * 打开数据库
	 * @param helper
	 */
	public void openDataBase() {
		resultsDb = this.getWritableDatabase();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}

	private Cursor select() {
		Cursor cursor = resultsDb.query(TABLE_NAME, null, null, null, null, null,
				null);
		return cursor;
	}
	
	public ArrayList<User> getCachedTops(){
		this.openDataBase();
		ArrayList<User> tops = new ArrayList<User>();
		Cursor cursor = this.select();
		if(cursor == null || cursor.getCount() == 0) return null;
		while (cursor.moveToNext()) {  
            User user = new User();
            user.init(cursor);
            tops.add(user);
        }  
		return tops;
	}
	
	public boolean hasContained(String isbn) {
		String[] parms = { isbn };
		Cursor result = resultsDb.query(TABLE_NAME, null, "isbn13=?", parms, null,
				null, null);
		Log.d("DEBUG", "count: " + result.getCount());
		if(result.getCount() > 0){
			return true;
		}
		return false;
	}
	

	/**
	 * 将用户添加到数据库
	 * @param key
	 * @param user
	 * @return
	 */
	public long insert(User user) {
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put("uid", user.uid);
		cv.put("name", user.name);
		cv.put("email", user.email);
		cv.put("desc", user.desc);
		cv.put("avatar", user.avatar);
		cv.put("book_total", user.book_total);
		cv.put("fav_total", user.fav_total);
		cv.put("school_id", user.school_id);
		cv.put("school_name", user.school_name);
		cv.put("gender", user.gender);
		cv.put("major", user.major);
		cv.put("phone", user.phone);
		cv.put("im", user.im);
		cv.put("create_at", user.createTime);
		long row = resultsDb.insert(TABLE_NAME, null, cv);
		return row;
	}

	/**
	 * 根据id，删除条目
	 * @param id
	 */
	public void delete(int id) {
		String where = KEY_ID + " = ?";
		String[] whereValue = { Integer.toString(id) };
		resultsDb.delete(TABLE_NAME, where, whereValue);
	}
	
	public void cacheTopUsers(final ArrayList<User> tops){
		
		this.openDataBase();
		new Thread(){
			public void run(){
				for(User user:tops){
					TopUsersHelper.this.insert(user);
				}
				TopUsersHelper.this.resultsDb.close();
				
				// 保存结束，更新pref里的topUser记录
				mHandler.sendEmptyMessage(0);
			}
		}.start();
		
	}
	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what == 0){
				SharedPreferences sp = context.getSharedPreferences("config", 0);
				sp.edit()
					.putLong("top_user_update", Calendar.getInstance().getTimeInMillis()).commit();
			}
			super.handleMessage(msg);
		}
		
	};

}
