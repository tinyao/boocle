package com.czzz.utils.zxing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.MenuItem;
import com.czzz.base.BaseActivity;
import com.czzz.bookcircle.BookCollection;
import com.czzz.demo.AddBookPostActivity;
import com.czzz.demo.BookInfoActivity;
import com.czzz.demo.R;
import com.czzz.douban.DoubanBook;
import com.czzz.douban.DoubanBookUtils;
import com.czzz.utils.HttpListener;
import com.czzz.utils.zxing.camera.CameraManager;
import com.czzz.utils.zxing.decoding.CaptureActivityHandler;
import com.czzz.utils.zxing.decoding.InactivityTimer;
import com.czzz.utils.zxing.view.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

public class CaptureActivity extends BaseActivity implements Callback, HttpListener {

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		case R.id.abs__home:
			if(collections!=null && collections.size()>0){
				Toast.makeText(this, "您还没有提交书籍", Toast.LENGTH_SHORT).show();
			}else{
				finish();
			}
			break;
		case android.R.id.home:
			if(collections!=null && collections.size()>0){
				Toast.makeText(this, "您还没有提交书籍", Toast.LENGTH_SHORT).show();
			}else{
				finish();
			}
			break;
		}
		
		return false;
	}

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private TextView txtResult;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;

	private View scanResultView;
	private ListView resultListView;
	private Button scanAddButton;
	
	private ArrayList<DoubanBook> scanBooks;
	private ArrayList<HashMap<String, String>> scanData;
	
	private boolean isAddAction = false;
	private ScanResultAdapter resAdapter;
	SurfaceView surfaceView;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qr_camera);

		scanResultView = (View)findViewById(R.id.scan_out_view);
		
		if(getIntent().getAction().equals("com.czzz.action.qrscan.addbook")){
			Log.d("DEBUG", "addbook_acation");
			mActionBar.setTitle("添加藏书");
			isAddAction = true;		// 处理添加书籍的事件
			scanResultView.setVisibility(View.VISIBLE);
			resultListView = (ListView) findViewById(R.id.scan_result_list);
			scanAddButton = (Button) findViewById(R.id.scan_add_all_book);
			scanAddButton.setOnClickListener(listener);
		} 
		
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		
		if(isAddAction) {
			scanBooks = new ArrayList<DoubanBook>();
			scanData = new ArrayList<HashMap<String, String>>();
			resAdapter = new ScanResultAdapter(this, scanData);
			resultListView.setAdapter(resAdapter);
		}
		
		keepScreenOn();
		
		if(resultListView!=null){
			resultListView.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
						long arg3) {
					// TODO Auto-generated method stub
					Intent comIntent = new Intent(CaptureActivity.this, AddBookPostActivity.class);
					comIntent.putExtra("from_scan", true);
					comIntent.putExtra("list_pos", pos);
					comIntent.putExtra("book_note", collections.get(pos).note);
					comIntent.putExtra("book_score", collections.get(pos).score);
					comIntent.putExtra("book_status", collections.get(pos).status);
					CaptureActivity.this.startActivityForResult(comIntent, 0);
					overridePendingTransition(R.anim.add_book_dialog_anim,0);
				}
				
			});
		}
		
	}

	private WakeLock wakeLock;
	
	private void keepScreenOn() {
		PowerManager manager = ((PowerManager)getSystemService(POWER_SERVICE)); 
		wakeLock = manager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK| PowerManager.ON_AFTER_RELEASE, "ATAAW"); 
		wakeLock.acquire();
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Log.d("DEBUG", "on resume...");
		
		surfaceView = (SurfaceView) findViewById(R.id.preview_view);

		initScanning();
		
		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
		
	}
	
	public void initScanning() {
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;
		
	}

	
	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
		Log.d("DEBUG", "on pause...");
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		wakeLock.release();
		if(mediaPlayer != null) mediaPlayer.release();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d("DEBU", "surface change...");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
		Log.d("DEBUG", "surface create ... ");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	String isbn;
	
	public void handleDecode(Result obj, Bitmap barcode) {
		inactivityTimer.onActivity();
		viewfinderView.drawResultBitmap(barcode);
		playBeepSoundAndVibrate();
//		txtResult.setText(obj.getBarcodeFormat().toString() + ":"
//				+ obj.getText());
		
		
//		Bundle b = new Bundle();
//		b.putString("bracode_format", obj.getBarcodeFormat().toString());
//		b.putString("bracode_text", obj.getText());
//		i.putExtras(b);
		
		if(obj.getBarcodeFormat().toString().contains("EAN")){
			isbn = obj.getText();
		} else {
			Toast.makeText(this, 
					"not isbn for book", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		
		if(!isAddAction) {
			// if it is for searching books
			Intent i = new Intent(this, BookInfoActivity.class);
			i.putExtra("isbn", isbn);
			startActivity(i);
			overridePendingTransition(R.anim.shrink_from_bottom,R.anim.exit_fade_out);
			this.finish();
		} else {
			// if it is for adding book
			// 开始搜索书籍信息
			boolean hasAdded = false;
			for(DoubanBook book : scanBooks){
				if(book.isbn13.equals(isbn)){
					hasAdded = true;
				}
			}
			if(hasAdded){
				Toast.makeText(this, "重复添加..", Toast.LENGTH_SHORT).show();
				reinitCaptuer();
			}else{
				loadingBook(isbn);
				fetchBookInfo(isbn);
			}
		}
		
	}
	
	private OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()) {
			case R.id.scan_add_all_book:
				// 任务队列，将书籍列表上传至服务端
				Toast.makeText(CaptureActivity.this, "uploading...", Toast.LENGTH_SHORT).show();
				
				Intent intent = new Intent( "bookcircle.task.upload_books" );
				intent.putParcelableArrayListExtra("upload_books", collections);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        sendBroadcast(intent);
		        
		        finish();
//				new BookUploaderManager(CaptureActivity.this, scanBooks).test();
				break;
			}
		}
		
	};
	
	public void loadingBook(String isbn){
		HashMap<String, String> bookmap = new HashMap<String, String>();
		bookmap.put("title", isbn);
		bookmap.put("loading", "识别中...");
		scanData.add(bookmap);
		resAdapter.notifyDataSetChanged();
		resultListView.setSelection(resAdapter.getCount()-1);
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
//		if (vibrate) {
//			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//			vibrator.vibrate(VIBRATE_DURATION);
//		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	
	private int taskType;
	private DoubanBook book;
	
	/**
	 * 根据isbn从豆瓣获取书籍文字信息
	 * @param isbn
	 */
	protected void fetchBookInfo(String isbn) {
		// TODO Auto-generated method stub
		this.taskType = HttpListener.FETCH_BOOK_INFO;
		DoubanBookUtils.fetchBookInfo(this, isbn, (HttpListener)this);
	}
	
	
	private ArrayList<BookCollection> collections = new ArrayList<BookCollection>();
	
	
	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		switch(taskType) {
		case HttpListener.FETCH_BOOK_INFO:
			if(data == null){
				Toast.makeText(CaptureActivity.this, "book not found", Toast.LENGTH_SHORT).show();
				break;
			}
			
			book = DoubanBookUtils.parseBookInfo(String.valueOf(data));
			scanBooks.add(book);
			
			BookCollection entry = new BookCollection();
			entry.book = book;
			entry.note = "";	// default
			entry.status = 0;
			collections.add(entry);
			
			HashMap<String, String> bookmap = scanData.get(scanData.size()-1);
			bookmap.put("title", "《" + book.title + "》");
//			bookmap.put("author", );
			bookmap.put("loading", book.author.equals("") ? "未知作者" : book.author);
			
			Intent comIntent = new Intent(this, AddBookPostActivity.class);
			comIntent.putExtra("from_scan", true);
			comIntent.putExtra("list_pos", collections.size() - 1);
			this.startActivityForResult(comIntent, 0);
			overridePendingTransition(R.anim.add_book_dialog_anim,0);
			
			resAdapter.notifyDataSetChanged();
			scanAddButton.setEnabled(true);
			
			break;
		}
	}

	
	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		
	}
	
	private void reinitCaptuer(){
		viewfinderView.drawViewfinder();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
//		try {
//			CameraManager.get().openDriver(surfaceView.getHolder());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		initScanning();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == 100){
			int pos = data.getIntExtra("list_pos", 0);
			BookCollection en = collections.get(pos);
			en.note = data.getStringExtra("note");
			en.status = data.getIntExtra("status", 0);
			en.score = data.getFloatExtra("score", 0);
		}
		
		reinitCaptuer();
	}

	
	private class ScanResultAdapter extends BaseAdapter{

		private LayoutInflater mInflater;
		private ArrayList<HashMap<String,String>> booklist;

		public ScanResultAdapter(Context context,
				ArrayList<HashMap<String,String>> list) {
			mInflater = LayoutInflater.from(context);
			this.booklist = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return booklist.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return booklist.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.scan_list_item, 
						parent, false);
				holder = new ViewHolder();
				holder.title = (TextView) convertView
						.findViewById(R.id.scan_add_title);
				holder.status = (TextView) convertView
						.findViewById(R.id.scan_add_fetching_status);
				
				convertView.setTag(holder); 
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.title.setText(booklist.get(position).get("title"));
			holder.status.setText(booklist.get(position).get("loading"));
			
			return convertView;
		}

		public class ViewHolder {
			TextView title;
			TextView status;
		}

	}
	
	
	
}