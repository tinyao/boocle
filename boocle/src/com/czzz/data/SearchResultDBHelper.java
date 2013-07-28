package com.czzz.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.czzz.douban.DoubanBook;

public class SearchResultDBHelper extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "search.db";
	private final static int DATABASE_VERSION = 1;
	final static String TABLE_NAME = "history";
	public final static String KEY_ID = "_id";
	// 搜索的关键词，直接isbn查询时为空
	public final static String KEY_NAME = "key";
	
	// 普通搜索key为关键词
	// 豆瓣热书key为日期
	// 附近最新key为_latest_chache
	
	
	// 缓存的时间起点
	
	Context context;

	SQLiteDatabase resultsDb;
	
	private static SearchResultDBHelper instance = null;
	
	public static SearchResultDBHelper getInstance(Context con){
		if(instance == null){
			instance = new SearchResultDBHelper(con);
		}
		
		return instance;
	}
	
	public SearchResultDBHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// 创建table
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		// 创建person表
		db.execSQL("CREATE TABLE " + TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ KEY_NAME + " VARCHAR, "
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
		
		db.execSQL("CREATE TABLE " + SearchDBHelper.TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NAME + " VARCHAR, "
				+ SearchDBHelper.KEY_TIMES + " VARCHAR)");
	}

	/**
	 * 打开数据库
	 * @param helper
	 */
	public SQLiteDatabase openDataBase() {
		if(resultsDb==null || !resultsDb.isOpen()){
			return resultsDb = this.getWritableDatabase();
		}
		return resultsDb;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}

	public Cursor select() {
		Cursor cursor = resultsDb.query(TABLE_NAME, null, null, null, null, null,
				null);
		return cursor;
	}

	/**
	 * 获得数据库对象
	 * @return
	 */
	public SQLiteDatabase getDB(){
		return resultsDb;
	}
	
	/**
	 * 判断某本书是否有本地缓存
	 * @param isbn
	 * @return
	 */
	public boolean hasContained(String key, String isbn) {
		String[] parms = { isbn, key };
		Cursor result = resultsDb.query(TABLE_NAME, null, "isbn13=? AND key=?", parms, null,
				null, null);
		Log.d("DEBUG", "count: " + result.getCount());
		if(result.getCount() > 0){
			return true;
		}
		return false;
	}
	
	/**
	 * 获取书籍的 rate缓存
	 * @param isbn 
	 * @return
	 */
	public String obtainItemRated(String isbn) {
		String[] parms = { isbn };
		Cursor result = resultsDb.query(TABLE_NAME, null, "isbn13=?", parms, null,
				null, null);
		Log.d("DEBUG", "count: " + result.getCount());
		if(result.getCount() != 0){
			result.moveToFirst();
			String ratingAve = result.getString(result.getColumnIndexOrThrow(DoubanBook.RATE_AVEARAGE));
			if(ratingAve != null && !ratingAve.equals("")){
				String rateNum = result.getString(result.getColumnIndexOrThrow(DoubanBook.RATE_NUM));
				return ratingAve + "/" + rateNum;
			}else{
				return null;
			}
		} else {
			return null;
		}
		
//		if (ratingAve==null || ratingAve.equals("")) {
//			return "";
//		} else {
//			String rateNum = result.getString(result.getColumnIndexOrThrow(DoubanBook.RATE_NUM));
//			return ratingAve + "/" + rateNum;
//		}
	}
	
	/**
	 * 以关键词筛选 筛选
	 * @param key 搜索关键词
	 * @return
	 */
	public Cursor selectByKey(String key) {
		String[] parms = { key };
		Cursor result = resultsDb.query(TABLE_NAME, null, "key=?", parms, null,
				null, null);
		Log.d("DEBUG", "count: " + result.getCount());
		return result;
	}
	
	public DoubanBook obtainBook(String isbn) {
		String[] parms = { isbn };
		Cursor result = resultsDb.query(TABLE_NAME, null, "isbn13=?", parms, null,
				null, null);
		if(result.getCount() != 0){
			result.moveToFirst();
			DoubanBook book = new DoubanBook();
			book.init(result);
			return book;
		}
		return null;
	}
	

	/**
	 * 增加书本缓存条目
	 * @param key 搜索关键词
	 * @param book 书籍信息
	 * @return
	 */
	public long insert(String key, DoubanBook book) {
		
		if(book.isbn13.equals("")){
			return 0;
		}
		
		if(hasContained(key, book.isbn13)){
			//已存在，更新key
			String strFilter = "isbn13=" + book.isbn13
					+ " AND key=" + key;

			// 如果消息时间 比 thread时间 新

			ContentValues args = new ContentValues();
			args.put(KEY_NAME, key);
			
			Log.d("DEBUG", "key: " + key);
			
			if(!key.equals("")){
//				resultsDb.update(TABLE_NAME, args, strFilter, null);
				resultsDb.update(TABLE_NAME, args, "isbn13=? AND key=?", 
						new String[]{book.isbn13, key});
			}
			
			return 0;
		}
		
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put(KEY_NAME, key);
		cv = buildContentBook(cv, book);
		
		long row = resultsDb.insert(TABLE_NAME, null, cv);
		return row;
	}
	
	private ContentValues buildContentBook(ContentValues cv, DoubanBook book){
		cv.put(DoubanBook.TITLE, book.title);
		cv.put(DoubanBook.ISBN13, book.isbn13);
		cv.put(DoubanBook.SUBTITLE, book.subtitle);
		cv.put(DoubanBook.IMAGE, book.image);
		cv.put(DoubanBook.LARGE_IMAGE, book.large_img);
		cv.put(DoubanBook.MID_IMAGE, book.medium_img);
		cv.put(DoubanBook.SMALL_IMAGE, book.small_img);
		cv.put(DoubanBook.AUTHOR, book.author);
		cv.put(DoubanBook.TRANSLATOR, book.translator);
		cv.put(DoubanBook.PUBLISHER, book.publisher);
		cv.put(DoubanBook.PUBDATE, book.pubdate);
		cv.put(DoubanBook.RATE_AVEARAGE, book.rateAverage);
		cv.put(DoubanBook.RATE_NUM, book.rateNum);
		cv.put(DoubanBook.PRICE, book.price);
		cv.put(DoubanBook.AUTHOR_INTRO, book.author_intro);
		cv.put(DoubanBook.SUMMARY, book.summary);
		cv.put(DoubanBook.PAGES, book.pages);
		
		return cv;
	}

	/**
	 * 根据id，删除书籍条目
	 * @param id
	 */
	public void delete(int id) {
		String where = KEY_ID + " = ?";
		String[] whereValue = { Integer.toString(id) };
		resultsDb.delete(TABLE_NAME, where, whereValue);
	}
	
	/**
	 * delete all search item
	 */
	public void deleteAll(){
		String where = KEY_NAME + " <> ?";
		String[] whereValue = { "" };
		resultsDb.delete(TABLE_NAME, where, whereValue);
	}

	/**
	 * 更新书籍的rate字段
	 * @param id
	 * @param key
	 * @param book
	 */
	private void updateRating(int id, String key, DoubanBook book) {
		String where = KEY_ID + " = ?";
		String[] whereValue = { Integer.toString(id) };

		ContentValues cv = new ContentValues();
		cv.put(KEY_NAME, key);
		cv.put(DoubanBook.TITLE, book.title);
		cv.put(DoubanBook.ISBN13, book.isbn13);
		cv.put(DoubanBook.SUBTITLE, book.subtitle);
		cv.put(DoubanBook.IMAGE, book.image);
		cv.put(DoubanBook.LARGE_IMAGE, book.large_img);
		cv.put(DoubanBook.MID_IMAGE, book.medium_img);
		cv.put(DoubanBook.SMALL_IMAGE, book.small_img);
		cv.put(DoubanBook.AUTHOR, book.author);
		cv.put(DoubanBook.TRANSLATOR, book.translator);
		cv.put(DoubanBook.PUBLISHER, book.publisher);
		cv.put(DoubanBook.PUBDATE, book.pubdate);
		cv.put(DoubanBook.RATE_AVEARAGE, book.rateAverage);
		cv.put(DoubanBook.RATE_NUM, book.rateNum);
		cv.put(DoubanBook.PRICE, book.price);
		cv.put(DoubanBook.AUTHOR_INTRO, book.author_intro);
		cv.put(DoubanBook.SUMMARY, book.summary);
		cv.put(DoubanBook.PAGES, book.pages);
		
		resultsDb.update(TABLE_NAME, cv, where, whereValue);
	}

	/**
	 * 更新书籍的rate字段
	 * @param book
	 */
	public void updataBookwithRating(DoubanBook book) {
		// TODO Auto-generated method stub
		String[] parms = { book.isbn13 };
		Cursor result = resultsDb.query(TABLE_NAME, null, "isbn13=?", parms, null,
				null, null);
		Log.d("DEBUG", "count: " + result.getCount());
		result.moveToFirst();
		String rating = result.getString(result.getColumnIndexOrThrow(DoubanBook.RATE_AVEARAGE));
		if (rating==null || rating.equals("")) {
			int _id = result.getInt(result.getColumnIndexOrThrow(KEY_ID));
			String key = result.getString(result.getColumnIndexOrThrow(KEY_NAME));
			updateRating(_id, key, book);
		} else {
			
		}
	}
	
//	public void cacheNearbyBooks(final ArrayList<BookCollection> tops){
//		
//		this.openDataBase();
//		new Thread(){
//			public void run(){
//				for(User user:tops){
//					this.insert(user);
//				}
//				resultsDb.close();
//				
//				// 保存结束，更新pref里的topUser记录
////				mHandler.sendEmptyMessage(0);
//			}
//		}.start();
//		
//	}
}
