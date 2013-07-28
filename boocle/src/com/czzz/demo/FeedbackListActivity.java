package com.czzz.demo;

import java.util.ArrayList;

import com.czzz.demo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.czzz.base.BaseListActivity;
import com.czzz.base.Pref;
import com.czzz.bookcircle.BugItem;
import com.czzz.demo.listadapter.BugAdapter;
import com.czzz.utils.HttpDownloadAsyncTask;
import com.czzz.utils.HttpListener;

public class FeedbackListActivity extends BaseListActivity implements HttpListener{
	
	int taskType = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.feedback_list);
		
		fetchBugs(0);
		
		getListView().setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent toMsgIntent = new Intent(FeedbackListActivity.this, ConversationActivity.class);
				toMsgIntent.putExtra("thread_uid", bugs.get(arg2).uid);
				toMsgIntent.putExtra("thread_name", bugs.get(arg2).name);
				toMsgIntent.putExtra("thread_avatar", "");
				startActivity(toMsgIntent);
			}
			
		});
		
	}
	
	private void fetchBugs(int start_id) {
		// TODO Auto-generated method stub
		taskType = HttpListener.FETCH_BUGS;
		String url = Pref.FEED_BACK_FETCH_URL + "/id/" + start_id;
		
		new HttpDownloadAsyncTask(this, (HttpListener)this).execute(url);
	}
	
	ArrayList<BugItem> bugs;
	BugAdapter bugAdapter;

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		if(data == null){
			Toast.makeText(this, "no bug reports", Toast.LENGTH_SHORT).show();
			return;
		}
		
		try {
			JSONObject json = new JSONObject(data+"");
			if(json.getInt("status") == 1){
				bugs = new ArrayList<BugItem>();
				
				JSONArray array = json.getJSONArray("data");
				for(int i=0; i<array.length(); i++){
					JSONObject itemJson = array.getJSONObject(i);
					BugItem item = new BugItem(itemJson);
					bugs.add(item);
				}
				
				bugAdapter = new BugAdapter(this, bugs);
				
				getListView().setAdapter(bugAdapter);
				
			}else{
				Toast.makeText(this, "no bug reports", Toast.LENGTH_SHORT).show();
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
	}

}
