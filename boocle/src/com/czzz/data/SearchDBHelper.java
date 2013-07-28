package com.czzz.data;

import com.czzz.douban.DoubanBook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SearchDBHelper extends SQLiteOpenHelper {
	private final static String DATABASE_NAME = "search.db";
	private final static int DATABASE_VERSION = 1;
	final static String TABLE_NAME = "keyword";
	public final static String KEY_ID = "_id";
	public final static String KEY_NAME = "keyword";
	public final static String KEY_TIMES = "frequency";
	
	SQLiteDatabase searchDb;

	public SearchDBHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// 创建table
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

		db.execSQL("CREATE TABLE " + TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NAME + " VARCHAR, "
				+ KEY_TIMES + " VARCHAR)");
		
		db.execSQL("CREATE TABLE " + SearchResultDBHelper.TABLE_NAME
						+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " 
						+ SearchResultDBHelper.KEY_NAME + " VARCHAR, "
						+ DoubanBook.TITLE + " VARCHAR, "
						+ DoubanBook.ISBN13 + " VARCHAR, "
						+ DoubanBook.SUBTITLE + " VARCHAR, "
						+ DoubanBook.IMAGE + " VARCHAR, "
						+ DoubanBook.LARGE_IMAGE + " VARCHAR, "
						+ DoubanBook.MID_IMAGE + " VARCHAR, "
						+ DoubanBook.SMALL_IMAGE + " VARCHAR, "
						+ DoubanBook.AUTHOR + " VARCHAR, "
						+ DoubanBook.TRANSLATOR + " VARCHAR, "
						+ DoubanBook.PUBLISHER + " VARCHAR, "
						+ DoubanBook.PUBDATE + " VARCHAR, "
						+ DoubanBook.PRICE + " VARCHAR, "
						+ DoubanBook.RATE_AVEARAGE + " VARCHAR, "
						+ DoubanBook.RATE_NUM + " VARCHAR, "
						+ DoubanBook.AUTHOR_INTRO + " VARCHAR, "
						+ DoubanBook.SUMMARY + " VARCHAR, "
						+ DoubanBook.PAGES + " VARCHAR)");
	}

	public void openDataBase() {
		searchDb = this.getWritableDatabase();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}

	public Cursor select() {
		Cursor cursor = searchDb.query(TABLE_NAME, null, null, null, null, null,
				null);
		return cursor;
	}

	public SQLiteDatabase getDB(){
		return searchDb;
	}
	
//	public Cursor selectSet() {
//		String[] columns = { LEAF_ID, LEAF_NAME, LEAF_ADDR, LEAF_GROUP};
//		String[] parms = { "00" };
//		Cursor result = leafBD.query(TABLE_NAME, columns, "address<>?", parms, null,
//				null, null);
//		return result;
//	}

	// 增加操作
	public long insert(String keyword, String keyFreq) {
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put(KEY_NAME, keyword);
		cv.put(KEY_TIMES, keyFreq);
		long row = searchDb.insert(TABLE_NAME, null, cv);
		return row;
	}

	// 删除操作
	public void delete(int id) {
		String where = KEY_ID + " = ?";
		String[] whereValue = { Integer.toString(id) };
		searchDb.delete(TABLE_NAME, where, whereValue);
	}
	
	public void deleteAll(){
		searchDb.delete(TABLE_NAME, null, null);
	}

	// 修改操作
	public void update(int id, String keyword, String keyFreq) {
		String where = KEY_ID + " = ?";
		String[] whereValue = { Integer.toString(id) };

		ContentValues cv = new ContentValues();
		cv.put(KEY_TIMES, keyFreq);
		cv.put(KEY_NAME, keyword);
		searchDb.update(TABLE_NAME, cv, where, whereValue);
	}
	
	
}