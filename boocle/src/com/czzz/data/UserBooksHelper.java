package com.czzz.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;

public class UserBooksHelper extends SQLiteOpenHelper{

	private final static String DATABASE_NAME = "books.db";
	private final static int DATABASE_VERSION = 2;
	private final static String TABLE_NAME = "collection";
	public final static String KEY_ID = "_id";
	public final static String KEY_NAME = "key";
	
	Context context;

	SQLiteDatabase resultsDb;
	
	SearchResultDBHelper bookHelper;
	
	public UserBooksHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		bookHelper = new SearchResultDBHelper(context);
		bookHelper.openDataBase();
	}

	static UserBooksHelper mhelper = null;
	
	public static UserBooksHelper getInstance(Context context){
		if (mhelper == null){
			mhelper = new UserBooksHelper(context);
			mhelper.openDataBase();
			return mhelper;
		}
		
		return mhelper;
	}
	
	// 创建table
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		// 创建person表
		db.execSQL("CREATE TABLE " + TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "key" + " VARCHAR, "
				+ "cid" + " INTEGER, "
				+ "isbn" + " VARCHAR, "
				+ "owner_id" + " INTEGER, "
				+ "owner" + " VARCHAR, "
				+ "owner_avatar" + " VARCHAR, "
				+ "owner_gender" + " INTEGER, "
				+ "status" + " INTEGER, "
				+ "score" + " FLOAT, "
				+ "note" + " VARCHAR, "
				+ "create_at" + " VARCHAR)");
	}

	/**
	 * 打开数据库
	 * @param helper
	 */
	public void openDataBase() {
		if(resultsDb == null)
			resultsDb = this.getWritableDatabase();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}

	/**
	 * 选择key为空的 item，即用户个人藏书页面缓存
	 * @return
	 */
	private Cursor select() {
		String[] parms = { "personal" };
		Cursor cursor = resultsDb.query(TABLE_NAME, null, "key=?", parms, null, null,
				"cid DESC");
		return cursor;
	}
	
	/**
	 * 选择key为latest_cache_nearby 的藏书，即首页探索的书籍缓存
	 * @return
	 */
	private Cursor selectNearbyCahce() {
		String[] parms = { "latest_cache_nearby" };
		Cursor cursor = resultsDb.query(TABLE_NAME, null, "key=?", parms, null,
				null, "cid DESC");
		return cursor;
	}
	
	/**
	 * 将藏书信息添加到数据库
	 * @param key 
	 * @param key
	 * @param user
	 * @return
	 */
	public long insert(String key, BookCollection collection) {
		
		if(hasContained(key, collection.cid)){
			delete(collection.cid);
		}
		
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put("key", key);
		cv.put("cid", collection.cid);
		cv.put("isbn", collection.book.isbn13);
		cv.put("owner_id", collection.owner_id);
		cv.put("owner", collection.owner);
		cv.put("owner_avatar", collection.owner_avatar);
		cv.put("owner_gender", collection.owner_gender);
		cv.put("status", collection.status);
		cv.put("score", collection.score);
		cv.put("note", collection.note);
		cv.put("create_at", collection.create_at);
		long row = resultsDb.insert(TABLE_NAME, null, cv);
		
		Cursor c = this.selectNearbyCahce();
		// 只保留最近20本，删除多余的藏书
		if(c.getCount() > 20){
			c.moveToLast();
			int __id = c.getInt(c.getColumnIndexOrThrow("_id"));
			Log.d("DEBUG", "_id = " + __id);
			deleteById(__id);
			c.close();
			c = null;
		}
		
		// 添加完收藏信息后，将书籍信息添加到书籍缓存表中
		bookHelper.insert("", collection.book);
		
		return row;
	}
	
	/**
	 * 更新状态和备忘
	 * @param cid
	 * @param status
	 * @param note
	 */
	public void update(int cid, int status, String note, float score){
		String strFilter = "cid=" + cid;
		ContentValues args = new ContentValues();
		args.put("status", status);
		args.put("note", note);
		args.put("score", score);
		resultsDb.update(TABLE_NAME, args, strFilter, null);
	}
	
	/**
	 * 判断某收藏是否有本地缓存, 同isbn 同key
	 * 
	 * @param isbn
	 * @return
	 */
	public boolean hasContained(String key, String isbn) {
		String[] parms = { isbn, key};
		Cursor result = resultsDb.query(TABLE_NAME, null, "isbn=? AND key=?", parms, null,
				null, null);
		Log.d("DEBUG", "count: " + result.getCount());
		if(result.getCount() > 0){
			return true;
		}
		return false;
	}
	
	public boolean hasContained(String key, int cid) {
		String[] parms = { key, ""+cid };
		Cursor result = resultsDb.query(TABLE_NAME, null, "key=? AND cid=?", parms, null,
				null, null);
		Log.d("DEBUG", "count: " + result.getCount());
		if(result.getCount() > 0){
			return true;
		}
		return false;
	}

	/**
	 * 根据id，删除条目
	 * @param id
	 */
	public void delete(int cid) {
		String where = "cid=?";
		String[] whereValue = { Integer.toString(cid) };
		resultsDb.delete(TABLE_NAME, where, whereValue);
	}
	
	public void deleteById(int __id) {
		String where = "_id=?";
		String[] whereValue = { Integer.toString(__id) };
		resultsDb.delete(TABLE_NAME, where, whereValue);
	}
	
	/**
	 * 根据id，删除条目
	 * @param id
	 */
	public void delete(int cid, String key) {
		String where = "cid=? AND key=?";
		String[] whereValue = { Integer.toString(cid), key };
		resultsDb.delete(TABLE_NAME, where, whereValue);
	}
	
	public void deleteAll(){
//		String where = "cid=?";
//		String[] whereValue = { Integer.toString(cid) };
		resultsDb.delete(TABLE_NAME, null, null);
	}
	
	public void cacheUserCollections(final ArrayList<BookCollection> collections){
		if(resultsDb == null) this.openDataBase();
		new Thread(){
			public void run(){
				for(BookCollection item : collections){
					UserBooksHelper.this.insert("personal", item);
				}
			}
		}.start();
	}
	
	public void cacheNearbyCollections(final ArrayList<BookCollection> collections){
		if(resultsDb == null) this.openDataBase();
		new Thread(){
			public void run(){
				for(BookCollection item : collections){
					UserBooksHelper.this.insert("latest_cache_nearby", item);
				}
			}
		}.start();
	}
	
	public ArrayList<BookCollection> getCachedCollections(){
		
		ArrayList<BookCollection> collections = new ArrayList<BookCollection>();
		Cursor cursor = this.select();
		if(cursor == null || cursor.getCount() == 0) return null;
		while (cursor.moveToNext()) {  
			BookCollection item = new BookCollection();
			item.init(cursor);
			item.book = bookHelper.obtainBook(item.book.isbn13);
			if(item.book != null){
				collections.add(item);
			}
		}  
		return collections;
	}
	
	public ArrayList<BookCollection> getCachedNearbyCollections(){
		
		ArrayList<BookCollection> collections = new ArrayList<BookCollection>();
		Cursor cursor = this.selectNearbyCahce();
		if(cursor == null || cursor.getCount() == 0) return null;
		while (cursor.moveToNext()) {  
			BookCollection item = new BookCollection();
			item.init(cursor);
			item.book = bookHelper.obtainBook(item.book.isbn13);
			if(item.book != null){
				collections.add(item);
			}
		}  
		return collections;
	}
	
	public ArrayList<BookCollection> getNewCachedCollections(int start_cid){
		
		ArrayList<BookCollection> collections = new ArrayList<BookCollection>();
		Cursor cursor = this.select(start_cid);
		if(cursor == null || cursor.getCount() == 0) return null;
		while (cursor.moveToNext()) {  
			BookCollection item = new BookCollection();
			item.init(cursor);
			item.book = bookHelper.obtainBook(item.book.isbn13);
			if(item.book != null){
				collections.add(item);
			}
		}  
		return collections;
	}
	
	private Cursor select(int start_cid) {
		String[] parms = { "" + start_cid };
		Cursor cursor = resultsDb.query(TABLE_NAME, null, "cid>?", parms, null,
				null, "cid DESC");
		return cursor;
	}
	
}
