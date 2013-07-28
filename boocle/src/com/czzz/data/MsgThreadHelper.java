package com.czzz.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.czzz.bookcircle.DirectMsg;
import com.czzz.bookcircle.MsgThread;

public class MsgThreadHelper extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "message.db";
	private final static int DATABASE_VERSION = 1;
	final static String TABLE_NAME = "thread";
	public final static String KEY_ID = "_id";

	Context context;

	private SQLiteDatabase resultsDb;

	public MsgThreadHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		openDataBase();
	}

	/**
	 * 建立thread表
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		// 创建person表
		db.execSQL("CREATE TABLE " + TABLE_NAME
				+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "msg_id"
				+ " INTEGER, " + "thread_uid" + " INTEGER, " + "thread_name"
				+ " VARCHAR, " + "thread_avatar" + " VARCHAR, " + "msg_body"
				+ " INTEGER, " + "is_recv" + " INTEGER, " + "unread_count"
				+ " INTEGER, " + "msg_time" + " VARCHAR)");
		
		db.execSQL("DROP TABLE IF EXISTS " + MsgHelper.TABLE_NAME);
		
		db.execSQL("CREATE TABLE " + MsgHelper.TABLE_NAME
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
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	/**
	 * 打开数据库
	 * 
	 * @param helper
	 */
	public void openDataBase() {
		if (resultsDb == null)
			resultsDb = this.getWritableDatabase();
	}
	
	public void setDataBase(SQLiteDatabase database){
		resultsDb = database;
	}

	static MsgThreadHelper mhelper = null;
	
	public static MsgThreadHelper getInstance(Context context){
		if (mhelper == null){
			mhelper = new MsgThreadHelper(context);
			return mhelper;
		}
		
		return mhelper;
	}
	
	/**
	 * 获取全部thread， 按照时间排序
	 * 
	 * @return
	 */
	public Cursor select() {
		if(resultsDb == null) this.openDataBase();
		Cursor cursor = resultsDb.query(TABLE_NAME, null, null, null, null,
				null, "msg_time DESC");
		return cursor;
	}

	
	public ArrayList<MsgThread> getCachedThread(){
		Cursor cur = select();
		if(cur == null || cur.getCount() == 0) return null;
		
		ArrayList<MsgThread> threads = new ArrayList<MsgThread>();
		while(cur.moveToNext()){
			MsgThread thread = new MsgThread();
			thread.init(cur);
			threads.add(thread);
		}
		return threads;
	}
	
	private long insert(MsgThread thread) {

		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put("msg_id", thread.msg_id);
		cv.put("thread_uid", thread.thread_uid);
		cv.put("thread_name", thread.thread_name);
		cv.put("thread_avatar", thread.thread_avatar);
		cv.put("is_recv", thread.is_recv ? 1 : 0);
		cv.put("msg_body", thread.msg_body);
		cv.put("unread_count", "" + thread.unread_count);
		cv.put("msg_time", thread.msg_time);

		long row = resultsDb.insert(TABLE_NAME, null, cv);

		return row;
	}

	/**
	 * 查询uid对应的
	 * 
	 * @param thread_uid
	 * @return
	 */
	public MsgThread getThread(int thread_uid) {
		
		String[] parms = { "" + thread_uid };
		Cursor cursor = resultsDb.query(TABLE_NAME, null, "thread_uid=?",
				parms, null, null, null);
		if (cursor == null || cursor.getCount() == 0)
			return null;

		cursor.moveToFirst();

		MsgThread thread = new MsgThread();
		thread.init(cursor);

		return thread;
	}

	/**
	 * 更新thread,有则更新，无则创建
	 * 
	 * @param thread_uid
	 */
	public void update(DirectMsg msg) {

		MsgThread th = getThread(msg.thread_uid);
		if (th == null) {
			// 新建thread
			_create_thread(msg);
		} else {
			// 更新thread
			_update_thread(th, msg);
		}

	}

	private void _create_thread(DirectMsg msg) {
		MsgThread th = new MsgThread();
		th.msg_id = msg.msg_id;
		th.thread_uid = msg.thread_uid;
		th.thread_name = msg.thread_name;
		th.thread_avatar = msg.thread_avatar;
		th.is_recv = msg.is_recv;
		th.unread_count = (msg.is_unread && msg.is_recv) ? 1 : 0;
		th.msg_body = msg.body;
		th.msg_time = msg.create_at;

		insert(th);
	}

	private void _update_thread(MsgThread th, DirectMsg msg) {
		String strFilter = "_id=" + th._id;

		// 如果消息时间 比 thread时间 新

		ContentValues args = new ContentValues();
		if (msg.create_at > th.msg_time) {
			args.put("msg_body", msg.body);
			args.put("msg_time", "" + msg.create_at);
			args.put("msg_id", msg.msg_id);
		}

		
		if (msg.is_unread && msg.is_recv) {
			// 未读接收 消息加1
			args.put("unread_count", (th.unread_count+1));
		}
		
		args.put("thread_avatar", msg.thread_avatar);
		
		resultsDb.update(TABLE_NAME, args, strFilter, null);
	}
	
	/**
	 * thread未读消息数置零
	 * @param thread_uid
	 */
	public void update_unreadCount(int thread_uid, int count){
		String strFilter = "thread_uid=" + thread_uid;

		ContentValues args = new ContentValues();
		args.put("unread_count", count);
		
		resultsDb.update(TABLE_NAME, args, strFilter, null);
		
	}
	
	public static void clear(){
		if(mhelper != null) mhelper = null;
	}
}
