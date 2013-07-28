package com.czzz.demo;

import java.util.ArrayList;

import com.czzz.demo.R;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.czzz.base.BaseFragment;
import com.czzz.bookcircle.BookUtils;
import com.czzz.bookcircle.DirectMsg;
import com.czzz.bookcircle.MsgThread;
import com.czzz.bookcircle.MsgUtils;
import com.czzz.bookcircle.MyApplication;
import com.czzz.bookcircle.UserUtils;
import com.czzz.data.MsgHelper;
import com.czzz.data.MsgThreadHelper;
import com.czzz.demo.BookShelfFragment.RefreshReceiver;
import com.czzz.demo.listadapter.DoubanRecommAdapter;
import com.czzz.demo.listadapter.MsgThreadAdapter;
import com.czzz.utils.TextUtils;
import com.czzz.view.LoadingView;
import com.czzz.view.RefreshListView;
import com.czzz.view.RefreshListView.OnRefreshListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


public class MessageFragment extends BaseFragment implements OnRefreshListener{
	
    private RefreshListView msgList;
	private LoadingView loadingView;
	private ImageView noMsgView;
	
	private int clickPosition = 0;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		Log.d("DEBUG", "Message onCreateActivity...");
		
		msgList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				
				Log.d("DEBUG", "ITEM: " + arg2);
				if(arg2 >= adapter.getCount()) return;
				MsgThread th = (MsgThread)adapter.getItem(arg2);
				clickPosition = arg2;
				Intent i = new Intent(MessageFragment.this.getActivity(), ConversationActivity.class);
				i.putExtra("thread", th);
				startActivity(i);
				
			}
			
		});
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		registerReceiver();
		setHasOptionsMenu(true);
		Log.d("DEBUG", "Message onCreate...");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d("DEBUG", "Message onCreateView...");
		
		View v = inflater.inflate(R.layout.fragment_msg, container, false);
		
		msgList = (RefreshListView) v.findViewById(R.id.direct_msg_list);
		loadingView = (LoadingView) v.findViewById(R.id.thread_loading_view);
		noMsgView = (ImageView) v.findViewById(R.id.thread_no_msg);
		msgList.setOnRefreshListener(this);
		
		fetchDirectMsg(2);
		
		if(threads != null){
			checkNewMsg();
		}
		
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
//		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.activity_main, menu);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isPaused = false;
		Log.d("DEBUG", "onResume...");
		MyApplication.MsgFragmentResumed();
	}

	
	/**
	 * 
	 * @param type 未读、已读、全部
	 */
	private void fetchDirectMsg(int type){
		
		threads = MsgThreadHelper
				.getInstance(getActivity()).getCachedThread();
		
		if(threads != null){
			adapter = new MsgThreadAdapter(this.getActivity(), threads);
			msgList.setAdapter(adapter);
			loadingView.setViewGone();
			noMsgView.setVisibility(View.GONE);
		}else{
			adapter = new MsgThreadAdapter(this.getActivity(), new ArrayList<MsgThread>());
			msgList.setAdapter(adapter);
			UserUtils.fetchUserMsg(type, new MessageResponeHandler(getActivity(), 0));
		}
		
	}

	private MsgThreadAdapter adapter;
	SharedPreferences sp;
	ArrayList<DirectMsg> msgs;
	
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
			if(isRefresh){
				Crouton.makeText(this.getActivity(), R.string.hint_no_msg, Style.CONFIRM).show();
				isRefresh = false;
			}
			msgList.completeRefreshing(false);
			noMsgView.setVisibility(View.GONE);
			loadingView.setViewGone();
			return;
		}
		
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

//	@Override
//	public void onTaskFailed(String data) {
//		// TODO Auto-generated method stub
//		msgList.completeRefreshingFail(false);
//		Toast.makeText(this.getActivity(), data, Toast.LENGTH_SHORT).show();
//	}
	
	ArrayList<MsgThread> threads;
	
	private void parsingMessages(ArrayList<DirectMsg> msgs){
		
		// 存储到数据库中, 同时建立thread表
		MsgHelper helper = MsgHelper.getInstance(getActivity());
		helper.saveMsgs(msgs);
		
		threads = MsgThreadHelper
				.getInstance(getActivity()).getCachedThread();
		
		adapter = new MsgThreadAdapter(MessageFragment.this.getActivity(), threads);
	}
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what){
			case 0:
				msgList.setAdapter(adapter);
				noMsgView.setVisibility(View.GONE);
				msgList.completeRefreshing(true);
				loadingView.setViewGone();
				break;
			}
			
			super.handleMessage(msg);
		}
		
	};

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		this.getActivity().unregisterReceiver(receiver);
		MyApplication.MsgFragmentPaused();
		super.onDestroy();
	}
	
	private boolean isPaused = false;
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		Log.d("DEBUG", "onPaused...");
		isPaused = true;
		super.onPause();
	}
	
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


	private boolean isRefresh = false;
	
	@Override
	public void onRefresh(RefreshListView listView) {
		// TODO Auto-generated method stub
		// things to do when refresh
		isRefresh = true;
		checkNewMsg();
	}
	

	/**
	 * 检测新消息
	 */
	private void checkNewMsg(){
		UserUtils.checkRemoteUnreadMsg(this.getActivity(), 
				new MessageResponeHandler(getActivity(), 0));
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
				clearItemUnRead(clickPosition);
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
	
	private RefreshThreadReceiver receiver;
	public static final String ACTION_UPDATE_THREAD = "action_update_thread";
	public static final String ACTION_THREAD_CLEAR_UNREAD = "action_clear_unread";
	
	private void registerReceiver(){
		receiver = new RefreshThreadReceiver();  
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_THREAD);
        filter.addAction(ACTION_THREAD_CLEAR_UNREAD);
        filter.addAction("bookcircle.task.load_msglist_new");
        filter.setPriority(1000);
        //动态注册BroadcastReceiver  
        this.getActivity().registerReceiver(receiver, filter); 
	}
	
	private void performNotification(ArrayList<DirectMsg> msgs) {
		// TODO Auto-generated method stub
		MsgUtils.notifyNewMsgs(this.getActivity(), msgs);
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
		public void onFailure(Throwable arg0, String arg1) {
			// TODO Auto-generated method stub
			super.onFailure(arg0, arg1);
			if(msgList.mIsRefreshing) msgList.completeRefreshing(false);
		}
	};
	
}
