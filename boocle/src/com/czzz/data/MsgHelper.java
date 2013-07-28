package com.czzz.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.czzz.base.User;
import com.czzz.bookcircle.DirectMsg;
import com.czzz.bookcircle.MsgThread;

public class MsgHelper extends SQLiteOpenHelper{

	private final static String DATABASE_NAME = "message.db";
	private final static int DATABASE_VERSION = 1;
	final static String TABLE_NAME = "msg";
	public final static String KEY_ID = "_id";
	
	Context context;

	SQLiteDatabase resultsDb;
	
	MsgThreadHelper threadHelper;
	
	public MsgHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		openDataBase();
	}
	

	private static MsgHelper mhelper = null;
	
	public static MsgHelper getInstance(Context context){
		if (mhelper == null){
			mhelper = new MsgHelper(context);
			return mhelper;
		}
		
		return mhelper;
	}
	
	// 创建table
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		// 创建 msg 和 thread 表
		db.execSQL("CREATE TABLE " + TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "msg_id" + " INTEGER, "
				+ "sender_id" + " INTEGER, "
				+ "recver_id" + " INTEGER, "
				+ "thread_uid" + " INTEGER, "
				+ "thread_name" + " VARCHAR, "
				+ "thread_avatar" + " VARCHAR, "
				+ "is_unread" + " INTEGER, "
				+ "is_recv" + " INTEGER, "
				+ "body" + " VARCHAR, "
				+ "book_id" + " INTEGER, "
				+ "create_at" + " VARCHAR)");
		
		db.execSQL("DROP TABLE IF EXISTS " + MsgThreadHelper.TABLE_NAME);

		db.execSQL("CREATE TABLE " + MsgThreadHelper.TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "msg_id"
				+ " INTEGER, " + "thread_uid" + " INTEGER, " + "thread_name"
				+ " VARCHAR, " + "thread_avatar" + " VARCHAR, " + "msg_body"
				+ " INTEGER, " + "is_recv" + " INTEGER, " + "unread_count"
				+ " INTEGER, " + "msg_time" + " VARCHAR)");
		
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

	private Cursor select(int thread_uid) {
		String[] parms = { "" + thread_uid };
		Cursor cursor = resultsDb.query(TABLE_NAME, null, "thread_uid=?",
				parms, null, null, "_id ASC");
		return cursor;
	}
	
	/**
	 * 将私信添加到数据库缓存
	 * @param msg
	 * @return
	 */
	public long insert(DirectMsg msg) {
		
		resultsDb = this.getWritableDatabase();
		
		if(hasContained(msg.msg_id)) {
			return -1;
		}
		
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put("msg_id", msg.msg_id);
		cv.put("sender_id", msg.sender_id);
		cv.put("recver_id", msg.recver_id);
		cv.put("thread_uid", msg.thread_uid);
		cv.put("thread_name", msg.thread_name);
		cv.put("thread_avatar", msg.thread_avatar);
		cv.put("is_unread", msg.is_unread);
		cv.put("body", msg.body);
		cv.put("is_recv", msg.is_recv ? 1 : 0);
		cv.put("book_id", msg.book_id);
		cv.put("create_at", "" + msg.create_at);
		
		long row = resultsDb.insert(TABLE_NAME, null, cv);
		
		if(threadHelper == null) 
			threadHelper = new MsgThreadHelper(context);
		
		// 更新thread表
		threadHelper.update(msg);
		
		return row;
	}
	
	private boolean hasContained(int msg_id){
		
		String[] parms = { "" + msg_id };
		Cursor cursor = resultsDb.query(TABLE_NAME, null, "msg_id=?",
				parms, null, null, null);
		if (cursor == null || cursor.getCount() == 0){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 获取同一thread的对话
	 * @param thread_uid
	 * @return
	 */
	public ArrayList<DirectMsg> getMsgsbyThread(int thread_uid){
		Cursor cursor = select(thread_uid);
		ArrayList<DirectMsg> msgs = new ArrayList<DirectMsg>();
		if(cursor == null || cursor.getCount() == 0) return msgs;
		
		while(cursor.moveToNext()){
			DirectMsg msg = new DirectMsg();
			msg.init(cursor);
			msgs.add(msg);
		}
		
		return msgs;
	}
	
	
	/**
	 * 保存私信，同时更新thread, 返回threads
	 * @param msgs
	 */
	public void saveMsgs(ArrayList<DirectMsg> msgs){
		
		threadHelper = new MsgThreadHelper(context);
		
		for(int i=msgs.size()-1; i>=0; i--){
			insert(msgs.get(i));
		}
		
//		for(DirectMsg msg : msgs) {
//			insert(msg);
//		}
		
		mhelper.close();
		mhelper = null;
	}
	
	/**
	 * 将消息设为已读
	 * @param msg
	 */
	public void setMsgRead(DirectMsg msg){
		String strFilter = "msg_id=" + msg.msg_id;
		ContentValues args = new ContentValues();
		args.put("is_unread", 0);
		resultsDb.update(TABLE_NAME, args, strFilter, null);
	}
	
	public int getMaxMsgId(){
		
		int startMax = 0;
		
		Cursor c = selectRecver();
		// 获取收消息的最大msg_id
		if(c.getCount() == 0){
			// 没有收消息，从所有消息获取最大msg_id
			c = this.select();
			if(c.getCount() == 0) {
				return 0;
			}else{
				c.moveToFirst();
			}
		}else{
			c.moveToLast();
		}
		
		return c.getInt(c.getColumnIndexOrThrow("msg_id"));
	}
	
	private Cursor selectRecver() {
		String[] parms = { "" + 1};
		Cursor cursor = resultsDb.query(TABLE_NAME, null, "is_recv=?",
				parms, null, null, null);
		return cursor;
	}
	
	private Cursor select() {
		Cursor cursor = resultsDb.query(TABLE_NAME, null, null,
				null, null, null, "_id ASC");
		return cursor;
	}
	
//	/**
//	 * 更新状态和备忘
//	 * @param cid
//	 * @param status
//	 * @param note
//	 */
//	public void update(int cid, int status, String note){
//		String strFilter = "cid=" + cid;
//		ContentValues args = new ContentValues();
//		args.put("status", status);
//		args.put("note", note);
//		resultsDb.update(TABLE_NAME, args, strFilter, null);
//	}
//
	
	public static void clear(){
		if(mhelper != null) mhelper = null;
	}
}
