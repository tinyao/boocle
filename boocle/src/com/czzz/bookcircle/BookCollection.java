package com.czzz.bookcircle;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.czzz.base.Pref;
import com.czzz.douban.DoubanBook;


public class BookCollection implements Serializable, Parcelable{

	private static final long serialVersionUID = 8563239841346672081L;
	
	public int cid;
	public int status;
	public String note;
	public float score;
	public String create_at;
	public String owner;
	public int owner_id;
	public int owner_gender = 2;
	public String owner_avatar;
	
	public DoubanBook book;
	
	
	public BookCollection initItem(JSONObject item) throws JSONException{
		cid = Integer.valueOf(item.getString("id"));
		status = Integer.valueOf(item.getString("status"));
		note = item.getString("note");
		create_at = item.getString("create_at");
		if(item.has("score")){
			score = Float.valueOf(item.getString("score"));
		}
		
		JSONObject bookJson =item.getJSONObject("book"); 
		initBook(bookJson);
		return this;
	}
	
	public BookCollection initCollection(JSONObject item) throws JSONException{
		
		status = Integer.valueOf(item.getString("status"));
		cid = Integer.valueOf(item.getString("id"));
		note = item.getString("note");
		owner_avatar = item.getString("avatar");
		if(item.has("score")){
			score = Float.valueOf(item.getString("score"));
		}
		
		if(owner_avatar.contains("http://") || owner_avatar.contains("https://")){	
			// 来自第三方登录
		}else{	// 系统注册
			owner_avatar = owner_avatar.equals("") ? "" : Pref.AVATAR_BASE_URL + owner_avatar;
		}
		
		create_at = item.getString("create_at");
		owner = item.getString("name");
		owner_gender = Integer.valueOf(item.getString("gender").equals("null") ? "1" : item.getString("gender"));
		owner_id = Integer.valueOf(item.getString("uid"));
		
		if(item.has("book")){
			JSONObject bookJson = item.getJSONObject("book"); 
			initBook(bookJson);
		}
		return this;
	}
	
	public DoubanBook initBook(JSONObject item) throws JSONException{
		book = new DoubanBook();
		book.isbn13 = item.getString("isbn");
		book.title = item.getString("title");
		book.subtitle = item.getString("sub_title");
		book.author = item.getString("author");
		book.image = item.getString("cover");
		book.publisher = item.getString("publisher");
		book.translator = item.getString("pubdate");
		book.rateNum = item.getString("rate_num");
		book.rateAverage = item.getString("rate_score");
		book.summary = item.getString("summary");
		return book;
	}
	
	public void init(Cursor cursor){
		cid = cursor.getInt(cursor.getColumnIndexOrThrow("cid"));
		owner_id = cursor.getInt(cursor.getColumnIndexOrThrow("owner_id"));
		owner = cursor.getString(cursor.getColumnIndexOrThrow("owner"));
		owner_avatar = cursor.getString(cursor.getColumnIndexOrThrow("owner_avatar"));
		owner_gender = cursor.getInt(cursor.getColumnIndexOrThrow("owner_gender"));
		status = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
		note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
		score = Float.valueOf(cursor.getFloat(cursor.getColumnIndexOrThrow("score")));
		create_at = cursor.getString(cursor.getColumnIndexOrThrow("create_at"));
		book = new DoubanBook();
		book.isbn13 = cursor.getString(cursor.getColumnIndexOrThrow("isbn"));
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if(book == null){
			return "BookCollection@null";
		}
		
		return "{" + "title=" + book.title + "\n"
				+ "status=" + status + "\n"
				+ "name=" + owner + "\n"
				+ "note=" + note + "\n"
				+ "create_at=" + create_at + "\n";
//				+ "book: {" + book + "}";
	}

	public static final Parcelable.Creator<BookCollection> CREATOR = new Creator<BookCollection>() {   

        public BookCollection createFromParcel(Parcel source) {   

        	BookCollection book = new BookCollection(); 
        	
        	book = (BookCollection) source.readSerializable();
        	
            return book;   

        }   

        public BookCollection[] newArray(int size) {   

            return new BookCollection[size];   

        }   

    };
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		// TODO Auto-generated method stub
		parcel.writeSerializable(this);
	}
	
	
}
