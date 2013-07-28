package com.czzz.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.czzz.base.BaseActivity;
import com.czzz.base.Pref;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.data.SearchResultDBHelper;
import com.czzz.data.UserBooksHelper;
import com.czzz.demo.listadapter.OwnerAdapter;
import com.czzz.douban.DoubanBook;
import com.czzz.douban.DoubanBookUtils;
import com.czzz.utils.HttpListener;
import com.czzz.utils.NetHttpClient;
import com.czzz.utils.ShelfCoverDownloader;
import com.czzz.utils.TextUtils;
import com.czzz.view.CollectionDropDialog;
import com.czzz.view.LoadingView;
import com.loopj.android.http.RequestParams;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class BookInfoActivity extends BaseActivity{
	
	private ActionBar mActionBar;
	
	private DoubanBook book;
	private BookCollection collection;
	private String isbn;
	
	private TextView bookTitle;
	private TextView bookDetailbase;
	private CheckedTextView bookDetailSummary;
	private ImageView bookDetailCover;
	private CheckBox summaryCheck;
	private RatingBar ratingBar;
	private TextView ratingNum;
	private View parentView;
	private TextView reviewBtn;
	private LoadingView loadingView;
	private ProgressBar rateLoading;
	
	private int taskType2;
	private boolean taskSearchUser = false;
	boolean fromScanCode = false;
	private GridView ownerGrid;
	
	private ArrayList<BookCollection> ownerInfos = new ArrayList<BookCollection>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getSupportActionBar();

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayUseLogoEnabled(false);
        
        setContentView(R.layout.bookinfo);
        
        initViews();
        
        Intent in = getIntent();
        Bundle bnd = in.getExtras();
        
        if(bnd.getString("isbn") != null){
        	isbn = bnd.getString("isbn");
        	fromScanCode = true;
        	if(bnd.containsKey("notscan")){
        		fromScanCode = false;
        	}
        	fetchBookInfo(isbn);
        } else {
        	if(bnd.containsKey("collection")){
        		collection = (BookCollection) bnd.getSerializable("collection");
        		ownerInfos.add(collection);
        		book = collection.book;
        		fillDataGrid();
        	}else if(bnd.containsKey("book")){
        		book = (DoubanBook) bnd.getSerializable("book");
        	}
        	displayBookInfo();
        	queryUserbyBooks(book.isbn13);
			// 获取书籍封面
			fetchBookCover();
        }
        
        reviewBtn.setOnClickListener(vlistener);
        registReceiver();
    }
	
	/**
	 * 查询同校拥有这本书的书友
	 * @param isbn13
	 */
	private void queryUserbyBooks(String isbn13) {
		// TODO Auto-generated method stub
		
		RequestParams params = new RequestParams();
		params.put("isbn", isbn13);
		params.put("school_id", "" + User.getInstance().school_id);
		
		NetHttpClient.get(Pref.SEARCH_URL, params, new ResponseHandler(this, HttpListener.QUERY_NEARBYBOOK_BY_ISBN));
	}


	protected void initViews(){
		parentView = (View) findViewById(R.id.detail_book_view);
        bookTitle = (TextView) findViewById(R.id.detail_book_title);
        
        bookDetailCover = (ImageView) findViewById(R.id.detail_book_cover);
        bookDetailbase = (TextView) findViewById(R.id.detail_book_base);
        bookDetailSummary = (CheckedTextView) findViewById(R.id.detail_summary);
        summaryCheck = (CheckBox) findViewById(R.id.detail_summary_toggle);
        reviewBtn = (TextView) findViewById(R.id.detail_book_review);
        ratingBar = (RatingBar) findViewById(R.id.detail_book_rating);
        ratingNum = (TextView) findViewById(R.id.detail_book_rate_num);
        loadingView = (LoadingView) findViewById(R.id.book_info_loading_view);
        rateLoading = (ProgressBar) findViewById(R.id.detail_rate_loading);
        ownerGrid = (GridView) findViewById(R.id.owners_grid);
        
        TextView infoOwnerTitle = (TextView) findViewById(R.id.detail_owners_title);
        
        infoOwnerTitle.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(ownerInfos==null || ownerInfos.size()==0){
					return;
				}
				Intent i = new Intent(BookInfoActivity.this, BookOwnersActivity.class);
				i.putParcelableArrayListExtra("owners", ownerInfos);
				startActivity(i);
			}
        	
        });
        
        ownerGrid.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				// TODO Auto-generated method stub
				if(ownerInfos.get(position).owner_id == User.getInstance().uid){
					addbook(false);
				}else{
					CollectionDropDialog detailDialog = new CollectionDropDialog(
							BookInfoActivity.this, ownerInfos.get(position));
					detailDialog.show();
				}
			}
			
		});
	}
	
	private View.OnClickListener vlistener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.detail_book_review:
				Intent reviewIntent = new Intent(BookInfoActivity.this, BookReviewsActivity.class);
				reviewIntent.putExtra("isbn", book.isbn13);
				reviewIntent.putExtra("title", book.title);
				startActivity(reviewIntent);
				break;
			}
		}
		
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getSupportMenuInflater().inflate(R.menu.menu_bookinfo, menu);
		
		if(collection!=null && collection.owner_id == User.getInstance().uid){
			//menuBook.setIcon(R.drawable.ic_action_edit);
			menu.findItem(R.id.menu_add_book).setVisible(false);
			menu.findItem(R.id.menu_edit_book).setVisible(true);
			menu.findItem(R.id.menu_item_share_action_provider_action_bar).setVisible(false);
			menu.findItem(R.id.menu_item_bookinfo_submenu).setVisible(true);
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	Intent shareIntent;
	
	/**
     * Creates a sharing {@link Intent}.
     *
     * @return The sharing intent.
     */
    private Intent createShareIntent() {
        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, getCoverUri(book.image));
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "推荐一本书《"
        		+ book.title + "》，" + (collection==null ? "" : collection.note)
        		+ " 『来自书圈, http://bookcircle.us』");
        return shareIntent;
    }
    
    private Uri getCoverUri(String coverUrl){
//    	Uri.fromFile() 
//    	book.image;
    	String filename = String.valueOf(coverUrl.hashCode());
		File f = new File(getCacheDirectory(this), filename);
    	return Uri.fromFile(f);
    }
    
    private static File getCacheDirectory(Context context) {
		String sdState = android.os.Environment.getExternalStorageState();
		File cacheDir;

		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File sdDir = android.os.Environment.getExternalStorageDirectory();

			// TODO : Change your direcory here
			cacheDir = new File(sdDir, "data/tac/images");
		} else
			cacheDir = context.getCacheDir();

		if (!cacheDir.exists())
			cacheDir.mkdirs();
		return cacheDir;
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		case R.id.abs__home:
			finish();
			if(fromScanCode){
				overridePendingTransition(R.anim.enter_fade_in, R.anim.shrink_exit_top);
			} else {
				overridePendingTransition(R.anim.enter_fade_in,R.anim.right_exit);
			}
			break;
		case android.R.id.home:
			finish();
			if(fromScanCode){
				overridePendingTransition(R.anim.enter_fade_in, R.anim.shrink_exit_top);
			} else {
				overridePendingTransition(R.anim.enter_fade_in,R.anim.right_exit);
			}
			break;
		case R.id.menu_add_book:
			addbook(true);
			break;
		case R.id.menu_edit_book:
			if(collection != null) addbook(false); // edit book
			break;
		case R.id.menu_item_share_action_provider_action_bar:
			startActivity(Intent.createChooser(createShareIntent(), "分享到..."));
			break;
		case R.id.menu_add_delete:
			// 删除藏书
			AlertDialog mDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.delete_the_book_confirm)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						deleteCollection();
					}
								
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
								
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				}).create();
				
			mDialog.show();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void addbook(boolean isAdding) {
		// TODO Auto-generated method stub
		
		Intent postBookIntent = new Intent(this, AddBookPostActivity.class);
		Bundle bnd = new Bundle();
		bnd.putBoolean("editing", !isAdding);
		if(collection != null){
			bnd.putSerializable("collection", collection);
		}else{
			bnd.putSerializable("book", book);
		}
		postBookIntent.putExtras(bnd);
		startActivity(postBookIntent);
		overridePendingTransition(R.anim.add_book_dialog_anim,0);
	}

	private void deleteCollection(){
		
		RequestParams params = new RequestParams();
		
		params.put("uid", ""+User.getInstance().uid);
		params.put("passwd", User.getInstance().pass);
		params.put("isbn", "" + collection.book.isbn13);
		
		NetHttpClient.post(Pref.DELETE_ENTRY_URL, params, 
				new ResponseHandler(this, HttpListener.DELETE_COLLECTION));
		
		UserBooksHelper mhelper = UserBooksHelper.getInstance(this);
		mhelper.openDataBase();
		mhelper.delete(collection.cid);
		
		User.getInstance().book_total--;
		SharedPreferences sp = this.getSharedPreferences("account", 0);
		sp.edit().putInt("book_total", User.getInstance().book_total).commit();
		
		// 更新书架UI，删除书籍
		Intent ii = new Intent("delete_user_book_item");
		ii.putExtra("cid", collection.cid);
		this.sendBroadcast(ii);
		
		finish();
	}

	/**
	 * 根据isbn从豆瓣获取书籍文字信息
	 * @param isbn
	 */
	protected void fetchBookInfo(String isbn) {
		// TODO Auto-generated method stub
		loadingView.setVisibility(View.VISIBLE);
		DoubanBookUtils.fetchBookInfo(isbn, new ResponseHandler(this, HttpListener.FETCH_BOOK_INFO));
	}
	
	/**
	 * 根据isbn从豆瓣获取书籍rate
	 * @param isbn
	 */
	protected void fetchBookRate(String isbn) {
		// TODO Auto-generated method stub
		
		// 如果从图书收藏跳转过来，则已经携带评分信息
		if(!book.rateAverage.equals("")){
			Log.d("DEBUG", "rate from collection...");
			displayBookRate();
			return;
		}
		
		// 查询数据库缓存中是否已有评分
		resultshelper = new SearchResultDBHelper(this);
		resultshelper.openDataBase();
		String rating = resultshelper.obtainItemRated(isbn);
		Log.d("DEBUG", "rating: " + rating);
		if(rating != null){
			book.rateAverage = rating.split("/")[0];
			book.rateNum = rating.split("/")[1];
			displayBookRate();
			return;
		}
		
		// 没有评分缓存，从网络获取
		rateLoading.setVisibility(View.VISIBLE);
		this.taskType2 = HttpListener.FETCH_BOOK_RATE;
		DoubanBookUtils.fetchBookInfo(this, isbn, (HttpListener)this);
	}
	
	/**
	 * 根据url从豆瓣获取书籍封面
	 * @param url
	 */
	protected void fetchBookCover() {
		// TODO Auto-generated method stub
		DoubanBookUtils.fetchBookCover(BookInfoActivity.this, book, bookDetailCover);
	}
	
	protected void displayBookInfo(){
		parentView.setVisibility(View.VISIBLE);
		
		bookDetailbase.setText("");
		
		bookTitle.setText(book.title);
		
		if(!book.subtitle.equals("")){
			bookDetailbase.append(
					getResources().getString(R.string.book_sub_title) + ": " + book.subtitle + "\n");
		}
		if(!book.author.equals("")){
			bookDetailbase.append(getResources().getString(R.string.book_author) + ": "
					+ book.author + "\n");
		}
		if(!book.translator.equals("")){
			bookDetailbase.append(getResources().getString(R.string.book_trans) + ": "
					+ book.translator + "\n");
		}
		if(!book.publisher.equals("")){
			bookDetailbase.append(getResources().getString(R.string.book_publisher) + ": "
					+ book.publisher + "\n");
		}
		if(!book.pubdate.equals("")){
			bookDetailbase.append(getResources().getString(R.string.book_pubdate) + ": "
					+ book.pubdate + "\n");
		}
		if(!book.isbn13.equals("")){
			bookDetailbase.append(getResources().getString(R.string.book_isbn) + ": "
					+ book.isbn13);
		}
		
		if(book.summary.equals("")){
			bookDetailSummary.setText(R.string.book_not_found);
			summaryCheck.setVisibility(View.GONE);
		}else{
			bookDetailSummary.setText(book.summary);
			
//			if(bookDetailSummary.getText().toString().length() < book.summary.length()){
//				summaryCheck.setVisibility(View.VISIBLE);
//			}else{
//				summaryCheck.setVisibility(View.GONE);
//			}
		}
		
		displayBookRate();
		
		bookDetailSummary.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(bookDetailSummary.isChecked()){
					bookDetailSummary.setMaxLines(6);
					bookDetailSummary.setChecked(false);
					summaryCheck.setChecked(false);
				} else {
					bookDetailSummary.setMaxLines(1000);
					bookDetailSummary.setChecked(true);
					summaryCheck.setChecked(true);
				}
				
			}
			
		});
		
		summaryCheck.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					bookDetailSummary.setMaxLines(1000);
				}else{
					bookDetailSummary.setMaxLines(6);
				}
			}
			
		});
		
	}
	
	protected void displayBookRate(){
//		Log.d("DEBUG", "Book: " + book);
		if(book.rateAverage.equals("")) return;
		ratingBar.setRating(Float.valueOf(book.rateAverage) / 2.0f);
		ratingNum.setText(book.rateAverage);
		
		TextView rateUsers = (TextView) findViewById(R.id.detail_book_rate_users);
		rateUsers.setText(book.rateNum + getResources().getString(R.string.book_rating_label));
		
//		saveBookInfo();
	}
	
	SearchResultDBHelper resultshelper;
	
	private void saveBookInfo() {
		// TODO Auto-generated method stub
		resultshelper = new SearchResultDBHelper(this);
		
		new Thread(){
			public void run(){
				resultshelper.openDataBase();
				if(!resultshelper.hasContained("", book.isbn13)){ // 不存在
					resultshelper.insert("", book);
				} else { //已存在
					resultshelper.updataBookwithRating(book);
				}
				resultshelper.getDB().close();
//				resultshelper.close();
			}
		}.start();
	}

	OwnerAdapter ownerAdapter;
	
	private void fillDataGrid(){
		TextView hint = (TextView) findViewById(R.id.query_owner_hint);
		hint.setVisibility(View.GONE);
		ownerAdapter = new OwnerAdapter(this, ownerInfos);
		ownerGrid.setAdapter(ownerAdapter);
	}
	
	/**
	 * 解析书主信息
	 * @param jsonStr
	 * @return
	 */
	private boolean parseOwnersInfo(String jsonStr){
		try {
			JSONObject json = new JSONObject(jsonStr);
			if(json.getInt("status") == 2) {
				TextView hint = (TextView) findViewById(R.id.query_owner_hint);
				hint.setText("╮(╯_╰)╭   " + User.getInstance().school_name + getResources().getString(R.string.book_owner_not_found));
				return false;
			}
			JSONArray array = json.getJSONArray("data");
			
			if(ownerInfos==null) ownerInfos = new ArrayList<BookCollection>();
			for(int i=0; i<array.length(); i++){
				JSONObject ownerJson = array.getJSONObject(i);
				BookCollection owner = new BookCollection();
				owner.initCollection(ownerJson);
				owner.book = book;
				if( !( ownerInfos.size() != 0 && ownerInfos.get(0).owner_id == owner.owner_id) ) 
					ownerInfos.add(owner);
			}
			
			Log.d("DEBUG", "owners: " + ownerInfos);
			
			return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Crouton.makeText(this, "parse error", Style.ALERT).show();
		}
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			ShelfCoverDownloader.cancelCurrent(bookDetailCover);
			finish();
			if(fromScanCode) { 
				overridePendingTransition(R.anim.enter_fade_in, R.anim.shrink_exit_top);
			}
			return false;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	private UpdateReceiver receiver; 
	private static final String ACTION_UPDATE_ITEM = "update_user_book_item";
	
	private void registReceiver(){
		receiver = new UpdateReceiver();  
        IntentFilter filter=new IntentFilter();  
        filter.addAction(ACTION_UPDATE_ITEM);

        //动态注册BroadcastReceiver  
        registerReceiver(receiver, filter); 
	}
	
	class UpdateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(ACTION_UPDATE_ITEM)){
				int update_cid = intent.getIntExtra("cid", 0);
				if(collection.cid == update_cid){
					collection.note = intent.getStringExtra("note");
					collection.score = intent.getFloatExtra("score", 0);
					collection.status = intent.getIntExtra("status", 0);
					
					ownerInfos.set(0, collection);
					ownerAdapter.notifyDataSetChanged();
					Crouton.makeText(BookInfoActivity.this, R.string.edit_success, Style.CONFIRM).show();
				}
			}
		}
		
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	/**
	 * 解析Http请求的响应结果
	 * @author tinyao
	 *
	 */
	private class ResponseHandler extends CustomAsyncHttpResponseHandler{

		public ResponseHandler(Context con, int taskId) {
			super(con, taskId);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public void onSuccess(int httpResultCode, String response) {
			// TODO Auto-generated method stub
			super.onSuccess(httpResultCode, response);
			
			switch(taskId){
			case HttpListener.FETCH_BOOK_INFO: // 获取到书籍信息
				loadingView.setVisibility(View.GONE);
				book = DoubanBookUtils.parseBookInfo(response);
				if(book == null){
					Crouton.makeText(BookInfoActivity.this, "book not found", Style.ALERT).show();
				}else{
					// 显示书籍基本信息
					displayBookInfo();
					queryUserbyBooks(book.isbn13);
					fetchBookCover();
				}
				loadingView.setViewGone();
				break;
			case HttpListener.QUERY_NEARBYBOOK_BY_ISBN:
				boolean result = parseOwnersInfo(TextUtils.unicodeToString(response));
				if(result){
					fillDataGrid();
				}
				break;
			}
		}
		
		@Override
		public void onFailure(Throwable arg0, String arg1) {
			// TODO Auto-generated method stub
			super.onFailure(arg0, arg1);
			switch(taskId){
			case HttpListener.FETCH_BOOK_INFO: // 获取到书籍信息
				try {
					JSONObject json = new JSONObject(arg1);
					if(json.has("code")){
						if(json.getInt("code") == 6000){
							Crouton.makeText(BookInfoActivity.this, R.string.book_not_found, Style.ALERT).show();
							loadingView.setResultMsg("Book not found");
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		
	}
	
}
