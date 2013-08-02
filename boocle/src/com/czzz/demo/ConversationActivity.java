package com.czzz.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.User;
import com.czzz.bookcircle.DirectMsg;
import com.czzz.bookcircle.MsgThread;
import com.czzz.bookcircle.MsgUtils;
import com.czzz.bookcircle.MyApplication;
import com.czzz.bookcircle.UserUtils;
import com.czzz.data.MsgHelper;
import com.czzz.data.MsgThreadHelper;
import com.czzz.demo.listadapter.DMAdapter;
import com.czzz.utils.HttpListener;
import com.czzz.utils.TextUtils;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class ConversationActivity extends AsyncTaskActivity{

	private MsgThread thread;
	private ListView msgListView;
	private EditText msgEdt;
	private ImageButton sendBtn, delBtn;
	private CheckBox emotionBtn;
	private View emotionLayout;
	private GridView emotionGrid;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.conversation_screen);
		
		msgEdt = (EditText) findViewById(R.id.conversation_msg_edt);
		sendBtn = (ImageButton) findViewById(R.id.conversation_msg_send);
		delBtn = (ImageButton) findViewById(R.id.conversation_edt_del);
		msgListView = (ListView) findViewById(R.id.conversation_list);
		emotionLayout = (View) findViewById(R.id.smiley_grid_layout);
		emotionGrid = (GridView) findViewById(R.id.smiley_grid);
		emotionBtn = (CheckBox) findViewById(R.id.conversation_msg_emotion);
		
		sendBtn.setEnabled(false);
		msgEdt.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "focus: " + arg1);
				emotionBtn.setChecked(false);
			}
			
		});
		
		msgEdt.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if(msgEdt.getText().toString().equals("")){
					sendBtn.setEnabled(false);
				}else{
					sendBtn.setEnabled(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		sendBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String body = msgEdt.getText().toString();
				if(!body.equals("")){
					msgEdt.setText("");
					sendMsg(body);
				}
			}
			
		});
		
		emotionBtn.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				
				if(isChecked){
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
					imm.hideSoftInputFromWindow(msgEdt.getWindowToken(), 0);
					
					if(emotions == null){
						
						loadEmotionsFromAssets();
						emotionGrid.setAdapter(emAdapter);
						
						int width = msgListView.getWidth() / 4 - 1;
						ViewGroup.LayoutParams params = delBtn.getLayoutParams();
						params.width = width;
						delBtn.setLayoutParams(params);
						delBtn.requestLayout();
						
					}
					
					emotionLayout.setVisibility(View.VISIBLE);
					
				}else{
					emotionLayout.setVisibility(View.GONE);
				}
			}
			
		});
		
		msgEdt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "click...");
				emotionBtn.setChecked(false);
				emotionLayout.setVisibility(View.GONE);
			}
			
		});
		
		emotionGrid.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				int cursorIndex = msgEdt.getSelectionStart();
				Editable editable = msgEdt.getText();
				editable.insert(cursorIndex, emotions.get(arg2));
			}
			
		});
		
		delBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int cursorIndex = msgEdt.getSelectionStart();
				Editable editable = msgEdt.getText();
				if(cursorIndex > 0)
					editable.delete(cursorIndex-1, cursorIndex);
			}
			
		});
		
		Intent data = getIntent();
		if(data.hasExtra("thread")){
			// 获取thread
			thread = (MsgThread) data.getSerializableExtra("thread");
		}else{
			// 根据thread_uid, 从本地数据库获取
			thread = MsgThreadHelper.getInstance(this)
						.getThread(data.getIntExtra("thread_uid", 0));
			
			// 新thread，创建
			if(thread == null){
				thread = new MsgThread();
				thread.thread_uid = data.getIntExtra("thread_uid", 0);
				thread.thread_name = data.getStringExtra("thread_name");
				thread.thread_avatar = data.getStringExtra("thread_avatar");
			}
		}
		
		this.setTitle(thread.thread_name);
		
		if(data.hasExtra("book_title")){
			msgEdt.append("#" + data.getStringExtra("book_title") + "# ");
		}
		
		fillConversationList();
		
//		msgListView.setOnItemLongClickListener(new OnItemLongClickListener(){
//
//			@SuppressWarnings("deprecation")
//			@Override
//			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
//					int arg2, long arg3) {
//				// TODO Auto-generated method stub
//				
//			    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//			    clipboard.setText(msgs.get(arg2).body);
//				Toast.makeText(ConversationActivity.this, "复制到剪贴板" + msgs.get(arg2).body, Toast.LENGTH_SHORT).show();
//			    
//				return false;
//			}
//			
//		});
		
	}
	
	private ArrayList<String> emotions;
	private ArrayAdapter<String> emAdapter;
	
	private void loadEmotionsFromAssets() {
		// TODO Auto-generated method stub
		
		emotions = new ArrayList<String>();
		
		InputStream emotionInput = null;
		try {
			emotionInput = this.getAssets().open("txt_emotions.txt", 0);
			
			BufferedReader re = new BufferedReader(new InputStreamReader(emotionInput));
			String bstr = null;
			while(null != (bstr = re.readLine())){
				emotions.add(bstr);
			}
			emotionInput.close();
			
//			Log.d("DEBUG", emotions+"");
			
			// set grid adapter
			emAdapter = new ArrayAdapter<String>(ConversationActivity.this, 
					R.layout.smiley_grid_item, R.id.emotion_text, emotions);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	MsgNewReceiver receiver;
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d("DEBUG", "onResume...");
		
		if(receiver == null){
			IntentFilter intentFilter = new IntentFilter("bookcircle.task.load_msglist_new");
		    receiver = new MsgNewReceiver();
		    intentFilter.setPriority(100);
		    this.registerReceiver(receiver, intentFilter, null, null); 
		}
		
		MsgUtils.cancelMsgNoti(this, thread.thread_uid);
	    
	    MyApplication.chatActivityResumed();
	}

	DMAdapter conAdapter;
	MsgHelper helper;
	ArrayList<DirectMsg> msgs;
	
	private void fillConversationList() {
		// TODO Auto-generated method stub
		helper = new MsgHelper(this);
		msgs = helper.getMsgsbyThread(thread.thread_uid);
		
		conAdapter = new DMAdapter(this, msgs);
		msgListView.setAdapter(conAdapter);
		msgListView.setSelection(msgs.size()-1);
		
		// set Msg read
		new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(msgs.size() != 0){
					// 将未读信息设为已读
					setMsgRead(msgs);
				}
				super.run();
			}
		}.start();
	}
	
//	Handler mHandler = new Handler(){
//
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
//			super.handleMessage(msg);
//		}
//		
//	};

	private void setMsgRead(ArrayList<DirectMsg> msgs2) {
		// TODO Auto-generated method stub
		int unread = 0;
		
		for(DirectMsg item : msgs2){
			if(item.is_recv && item.is_unread){
				// update msg read local
				MsgHelper.getInstance(this).setMsgRead(item);
				// update msg read remote
				UserUtils.setRemoteMsgRead(this, item.msg_id, this);
				unread++;
			}
		}
		
		if(unread == 0) return;
		
		// update thread unread_count to 0  
		MsgThreadHelper.getInstance(this).update_unreadCount(thread.thread_uid, 0);
		
		Intent updateThreadBroadcast = new Intent(MessageFragment.ACTION_THREAD_CLEAR_UNREAD);
		updateThreadBroadcast.putExtra("unread_clear",  unread);
		this.sendBroadcast(updateThreadBroadcast);
		Log.d("DEBUG", "send clear unread...");
	}

	
	private void sendMsg(String body){
		this.taskType = HttpListener.SEND_DIRECT_MSG;
		UserUtils.sendDirectMsg(ConversationActivity.this, thread.thread_uid, 
				body, 0, ConversationActivity.this);
		
		// 更新本地UI
		DirectMsg msgsend = new DirectMsg();
		msgsend.thread_uid = thread.thread_uid;
		msgsend.thread_name = thread.thread_name;
		msgsend.thread_avatar = thread.thread_avatar;
		msgsend.sender_id = User.getInstance().uid;
		msgsend.recver_id = thread.thread_uid;
		msgsend.body = body;
		msgsend.book_id = 0;
		msgsend.is_recv = false;
		msgsend.is_unread = true;
		msgsend.is_sending = true;
		msgsend.create_at = Calendar.getInstance().getTimeInMillis()/1000l;

		msgs.add(msgsend);
		notifyListView();
	}
	
	private void notifyListView(){
		conAdapter = new DMAdapter(this, msgs);
		conAdapter.notifyDataSetChanged();
		msgListView.setSelection(conAdapter.getCount()-1);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MyApplication.chatActivityPaused();
		Log.d("DEBUG", "onPaused...");
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("DEBUG", "onStart...");
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("DEBUG", "onStop...");
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(helper!=null) helper.close();
		this.unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		Log.d("DEBUG", "" + data);
		if(this.taskType == HttpListener.SEND_DIRECT_MSG){
			try {
				JSONObject json = new JSONObject("" + data);
				if(json.getInt("status") != 1){
					Crouton.makeText(this, R.string.send_msg_failed, Style.CONFIRM).show();
					return;
				}
				DirectMsg item = msgs.get(msgs.size()-1);
				JSONObject msgJson = json.getJSONObject("data");
				item.msg_id = Integer.valueOf(msgJson.getString("id"));
				item.create_at = Long.valueOf(msgJson.getString("create_at"));
				
				item.is_sending = false;
				updateItemView(msgs.size()-1, item.create_at);
				
				// save to database
				MsgHelper.getInstance(this).insert(item);
				
				// broadcast to update the msg-thread-fragment
				Intent updateThreadBroadcast = new Intent(MessageFragment.ACTION_UPDATE_THREAD);
				this.sendBroadcast(updateThreadBroadcast);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Crouton.makeText(this, data, Style.ALERT).show();
	}
	
	/**
	 * update item after MSG-SENT
	 * @param index
	 * @param time
	 */
	private void updateItemView(int index, long time){
	    View v = msgListView.getChildAt(index - 
	    		msgListView.getFirstVisiblePosition());
	    ProgressBar loading = (ProgressBar) v.findViewById(R.id.msg_item_send_progress);
	    TextView createAt = (TextView) v.findViewById(R.id.msg_item_time);
	    loading.setVisibility(View.GONE);
	    createAt.setText(TextUtils.formatSmartTime(time));
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		case R.id.abs__home:
			finish();
			return true;
		case android.R.id.home:
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode == event.KEYCODE_BACK){
			if(emotionBtn.isChecked()){
				emotionBtn.setChecked(false);
				return false;
			}else{
				this.setResult(100);
				finish();
				return false;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	class MsgNewReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			Log.d("DEBUG", "conver reveiver...");
			
			MsgUtils.cancelMsgNoti(ConversationActivity.this, thread.thread_uid);
			
			ArrayList<DirectMsg> new_msgs 
				= intent.getParcelableArrayListExtra("new_msgs");
			
			ArrayList<DirectMsg> msgNewHere = new ArrayList<DirectMsg>();
			
			for(DirectMsg msgi:new_msgs){
				if(msgi.thread_uid == thread.thread_uid){
					msgNewHere.add(msgi);
				}
			}
			
			msgs.addAll(msgNewHere);
			notifyListView();
			
			setMsgRead(msgNewHere);
			
			if(!MyApplication.isChatActivityVisible()){
				performNotification(new_msgs);
			}
//			else{
////				this.abortBroadcast();
//			}
		}
	}

	private void performNotification(ArrayList<DirectMsg> msgs) {
		// TODO Auto-generated method stub
		try {
	        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
	        r.play();
	    } catch (Exception e) {
	    	
	    }
	}

}
