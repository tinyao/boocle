package com.czzz.demo;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.czzz.base.BaseActivity;
import com.czzz.bookcircle.DirectMsg;
import com.czzz.bookcircle.MsgThread;
import com.czzz.bookcircle.MsgUtils;
import com.czzz.bookcircle.MyApplication;
import com.czzz.bookcircle.UserUtils;
import com.czzz.data.MsgHelper;
import com.czzz.data.MsgThreadHelper;
import com.czzz.demo.listadapter.MsgThreadAdapter;
import com.czzz.utils.TextUtils;
import com.czzz.view.LoadingView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MessageActivity extends BaseActivity implements PullToRefreshAttacher.OnRefreshListener{

	private ListView msgList;
	private LoadingView loadingView;
	private ImageView noMsgView;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	
	ArrayList<MsgThread> threads;
	private MsgThreadAdapter adapter;
	SharedPreferences sp;
	private ArrayList<DirectMsg> msgs;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_msg);
        msgList = (ListView) findViewById(R.id.direct_msg_list);
		loadingView = (LoadingView) findViewById(R.id.thread_loading_view);
		noMsgView = (ImageView) findViewById(R.id.thread_no_msg);
		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        mPullToRefreshAttacher.addRefreshableView(msgList, this);
        
        registerReceiver();
		fetchDirectMsg(2);
		
		if(threads != null){
			checkNewMsg();
		}
		
		msgList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "ITEM: " + pos);
				if(pos >= adapter.getCount()) return;
				MsgThread th = (MsgThread)adapter.getItem(pos);
				clickPosition = pos;
				Intent i = new Intent(MessageActivity.this, ConversationActivity.class);
				i.putExtra("thread", th);
				startActivity(i);
			}
			
		});
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		isPaused = false;
		Log.d("DEBUG", "onResume...");
		MyApplication.MsgFragmentResumed();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(receiver);
		MyApplication.MsgFragmentPaused();
		super.onDestroy();
	}
	
	/**
	 * 检测新消息
	 */
	private void checkNewMsg(){
		UserUtils.checkRemoteUnreadMsg(this, 
				new MessageResponeHandler(this, 0));
	}
	
	private void fetchDirectMsg(int type){
		
		threads = MsgThreadHelper
				.getInstance(this).getCachedThread();
		
		if(threads != null){
			adapter = new MsgThreadAdapter(this, threads);
			msgList.setAdapter(adapter);
			loadingView.setViewGone();
			noMsgView.setVisibility(View.GONE);
		}else{
			adapter = new MsgThreadAdapter(this, new ArrayList<MsgThread>());
			msgList.setAdapter(adapter);
			UserUtils.fetchUserMsg(type, new MessageResponeHandler(this, 0));
		}
		
	}
	
	@Override
	public void onRefreshStarted(View view) {
		// TODO Auto-generated method stub
		checkNewMsg();
	}
	
	/* update msg */
	public void updateMsgandThread(String response) {
		// TODO Auto-generated method stub
		Log.d("DEBUG", "message: " + response);
		
		if(response == null) return;
		
		msgs = UserUtils.parseDMsg(TextUtils.unicodeToString(""+response));
		
		if(threads==null && msgs==null){
			noMsgView.setVisibility(View.VISIBLE);
		}
		
		if(msgs == null) {
			if(mPullToRefreshAttacher.isRefreshing()){
				Crouton.makeText(this, R.string.hint_no_msg, Style.CONFIRM).show();
			}
			mPullToRefreshAttacher.setRefreshComplete();
			noMsgView.setVisibility(View.GONE);
			loadingView.setViewGone();
			return;
		}
		
		/* 通知首页抽屉更新 */
		Intent updateMsgUnreadIntent = new Intent("bookcircle.task.new_msg_update_home");
		updateMsgUnreadIntent.putParcelableArrayListExtra("new_msgs", msgs);
		sendOrderedBroadcast(updateMsgUnreadIntent, null);
		
		new Thread(){
			public void run(){
				parsingMessages(msgs);
				Message handleMsg = mHandler.obtainMessage();
				handleMsg.what = 0;
				handleMsg.obj = msgs;
				mHandler.sendMessage(handleMsg);
			}
		}.start();
		
	}
	
	/**
	 * 解析新私信，建立thread
	 * @param msgs
	 */
	private void parsingMessages(ArrayList<DirectMsg> msgs){
		
		// 存储到数据库中, 同时建立thread表
		MsgHelper helper = MsgHelper.getInstance(this);
		helper.saveMsgs(msgs);
		
		threads = MsgThreadHelper
				.getInstance(this).getCachedThread();
		
		adapter = new MsgThreadAdapter(this, threads);
	}
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what){
			case 0:
				msgList.setAdapter(adapter);
				noMsgView.setVisibility(View.GONE);
				mPullToRefreshAttacher.setRefreshComplete();
				loadingView.setViewGone();
				break;
			}
			
			super.handleMessage(msg);
		}
		
	};
	
	private void clearItemUnRead(int position){
		
		Log.d("DEBUG", "clear unread num");
		
		threads.get(position).unread_count = 0;
		
		int firstPosition = msgList.getFirstVisiblePosition() - msgList.getHeaderViewsCount(); // This is the same as child #0
		int wantedChild = 0 - firstPosition;
		// Say, first visible position is 8, you want position 10, wantedChild will now be 2
		// So that means your view is child #2 in the ViewGroup:
		if (wantedChild < 0 || wantedChild >= msgList.getChildCount()) {
		  Log.w("DEBUG", "Unable to get view for desired position, because it's not being displayed on screen.");
		  return;
		}
		// Could also check if wantedPosition is between listView.getFirstVisiblePosition() and listView.getLastVisiblePosition() instead.
		View wantedView = msgList.getChildAt(wantedChild);
		
	    if(wantedView == null){
	    	return;
	    }
	    TextView countView = (TextView) wantedView.findViewById(R.id.thread_item_unread_count);
	    countView.setVisibility(View.GONE);
	}
	
	private int clickPosition = 0;
	private RefreshThreadReceiver receiver;
	public static final String ACTION_UPDATE_THREAD = "action_update_thread";
	public static final String ACTION_THREAD_CLEAR_UNREAD = "action_clear_unread";
	public static final String ACTION_LOAD_MSG_NEW = "bookcircle.task.load_msglist_new";
	
	private void performNotification(ArrayList<DirectMsg> msgs) {
		// TODO Auto-generated method stub
		MsgUtils.notifyNewMsgs(this, msgs);
	}
	
	private void registerReceiver(){
		receiver = new RefreshThreadReceiver();  
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_THREAD);
        filter.addAction(ACTION_THREAD_CLEAR_UNREAD);
        filter.addAction(ACTION_LOAD_MSG_NEW);
        filter.setPriority(1000);
        //动态注册BroadcastReceiver  
        this.registerReceiver(receiver, filter); 
	}
	
	class RefreshThreadReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			Log.d("DEBUG", intent.getAction());
			if(intent.getAction().equals(ACTION_UPDATE_THREAD)){
				clearItemUnRead(clickPosition);
				fetchDirectMsg(2);
			}else if(intent.getAction().equals(ACTION_THREAD_CLEAR_UNREAD)){
				Log.d("DEBUG", "clear unread num");
				MsgThread th = threads.get(clickPosition);
				th.unread_count = 0;
				adapter.notifyDataSetChanged();
//				clearItemUnRead(clickPosition);
			}else if(intent.getAction().equals("bookcircle.task.load_msglist_new")){
				
				Log.d("DEBUG", "fragment reveiver...");
				ArrayList<DirectMsg> new_msgs 
					= intent.getParcelableArrayListExtra("new_msgs");
				Log.d("DEBUG", "msgs: " + new_msgs);
				
				parsingMessages(new_msgs);
				msgList.setAdapter(adapter);
				
				// notify the conversation view
				performNotification(new_msgs);
			}
			
		}
	
	}
	
	class MessageResponeHandler extends CustomAsyncHttpResponseHandler{

		public MessageResponeHandler(Context con, int taskId) {
			super(con, taskId);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onSuccess(int httpResultCode, String response) {
			// TODO Auto-generated method stub
			super.onSuccess(httpResultCode, response);
			updateMsgandThread(response);
		}
		
		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			super.onFinish();
			if(mPullToRefreshAttacher.isRefreshing())
				mPullToRefreshAttacher.setRefreshComplete();
		}
		
	};

}
