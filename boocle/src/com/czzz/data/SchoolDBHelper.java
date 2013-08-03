package com.czzz.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SchoolDBHelper {
	private final static String TABLE_NAME = "school_set";
	public final static String ID = "_id";
	public final static String SCHOOL_ID = "school_id";
	public final static String SCHOOL_NAME = "school_name";
	public final static String PROVINCE_ID = "province_id";
	public final static String PROVINCE_NAME = "province_name";
	
	SQLiteDatabase searchDb;

	public SchoolDBHelper(SQLiteDatabase db) {
		// TODO Auto-generated constructor stub
		searchDb = db;
	}
	
	public Cursor select() {
		Cursor cursor = searchDb.query(TABLE_NAME, null, null, null, null, null,
				null);
		return cursor;
	}
	
	public String getSchoolName(int school_id){
		String[] parms = { "" + school_id };
		Cursor cursor = searchDb.query(TABLE_NAME, null, "school_id=?",
				parms, null, null, "_id ASC");
		if(cursor == null || cursor.getCount() == 0) return "";
		cursor.moveToFirst();
		return cursor.getString(cursor.getColumnIndexOrThrow(SCHOOL_NAME));
	}

	public SQLiteDatabase getDB(){
		return searchDb;
	}
	
	// 增加操作
	public long insert(int schoolId, String schoolName, int pronvinceId, String province) {
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put(SCHOOL_ID, schoolId);
		cv.put(SCHOOL_NAME, schoolName);
		cv.put(PROVINCE_ID, pronvinceId);
		cv.put(PROVINCE_NAME, province);
		long row = searchDb.insert(TABLE_NAME, null, cv);
		return row;
	}

	// 删除操作
	public void delete(int id) {
		String where = ID + " = ?";
		String[] whereValue = { Integer.toString(id) };
		searchDb.delete(TABLE_NAME, where, whereValue);
	}

	// 修改操作
	public void update(int id, int schoolId, String schoolName, int pronvinceId, String province) {
		String where = ID + " = ?";
		String[] whereValue = { Integer.toString(id) };

		ContentValues cv = new ContentValues();
		cv.put(SCHOOL_ID, schoolId);
		cv.put(SCHOOL_NAME, schoolName);
		cv.put(PROVINCE_ID, pronvinceId);
		cv.put(PROVINCE_NAME, province);
		searchDb.update(TABLE_NAME, cv, where, whereValue);
	}
	
	
}