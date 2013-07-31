package com.czzz.demo;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshAttacher;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.czzz.base.BaseActivity;
import com.czzz.bookcircle.DirectMsg;
import com.czzz.bookcircle.MsgThread;
import com.czzz.bookcircle.UserUtils;
import com.czzz.data.MsgHelper;
import com.czzz.data.MsgThreadHelper;
import com.czzz.demo.MessageFragment.MessageResponeHandler;
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
        
		fetchDirectMsg(2);
		
		if(threads != null){
			checkNewMsg();
		}
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
