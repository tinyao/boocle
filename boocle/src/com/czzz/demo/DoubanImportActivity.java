package com.czzz.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.czzz.base.BaseListActivity;
import com.czzz.demo.R;
import android.R.color;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.czzz.douban.BookCollectionEntry;
import com.czzz.douban.DoubanBookUtils;
import com.czzz.social.DoubanOAuth;
import com.czzz.utils.HttpListener;

public class DoubanImportActivity extends BaseListActivity implements HttpListener{

	private ActionBar mActionBar;

	private DoubanOAuth dbOAuth;
	private View loadingView;
	
	int taskType;
	
	MySimpleAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mActionBar = getSupportActionBar();

		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(false);

		setContentView(R.layout.douban_import);
		loadingView = (View)findViewById(R.id.douban_import_loading_view);

		dbOAuth = new DoubanOAuth(this);

		if (dbOAuth.getAccessToken().equals("")) {
			dbOAuth.lauchforVerifyCode(this);
		} else {
			fetchBookCollection(dbOAuth.getDoubanUserId());
		}

		listview = getListView();

	}

//	private ActionMode.Callback mActionActivityCallback = new ActionMode.Callback() {
//
//		@Override
//		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//			MenuInflater inflater = mode.getMenuInflater();
//			inflater.inflate(R.menu.actionbar_context_menu, menu);
//			return true;
//		}
//
//		@Override
//		public void onDestroyActionMode(ActionMode mode) {
//
//		}
//
//		@Override
//		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//			switch (item.getItemId()) {
//			case R.id.menu_item1:
//				return true;
//			case R.id.menu_item2:
//				// close the action mode
//				// mode.finish();
//				return true;
//			default:
//				mode.finish();
//				return false;
//			}
//		}
//
//		@Override
//		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//			// TODO Auto-generated method stub
//			return false;
//		}
//	};

	/**
	 * 豆瓣认证完成后，获取返回的code
	 * 
	 * @param intent
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d("DEBUG", "onNewIntent---");
		// 在这里处理获取返回的code参数
		Uri uri = intent.getData();
		String code = uri.getQueryParameter("code");
		// bookInfoTv.append("code: " + code + "\n");
		Log.d("DEBUG", "code: " + code);
		this.taskType = HttpListener.DOUBAN_OAUTH_JSON;
		dbOAuth.fetchAccessToken(this, code, (HttpListener)this);
	}

	/**
	 * 根据userId获取书籍收藏
	 * 
	 * @param userid
	 */
	protected void fetchBookCollection(String userid) {
//		pd = new ProgressDialog(this);
//		pd.setMessage("正在获取用户藏书...");
//		pd.show();
		this.taskType = HttpListener.FETCH_BOOK_COLLECTION;
		DoubanBookUtils.fetchBookCollection2(this, (HttpListener)this, taskType, userid);
	}

//	ActionMode mActionMode;

	ArrayList<Map<String, Object>> booklist;
	
	protected void fillBookList(List<BookCollectionEntry> books) {

		listview = this.getListView();

		booklist = new ArrayList<Map<String, Object>>();

		for (BookCollectionEntry book : books) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("title", book.book.title);
			map.put("publisher", book.book.publisher);
			map.put("status", book.status);
			map.put("checked", false);
			booklist.add(map);
		}

		adapter = new MySimpleAdapter(this, booklist);
		setListAdapter(adapter);

		listview.setItemsCanFocus(false);
//		listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
//		listview.setOnItemLongClickListener(new OnItemLongClickListener(){
//
//			@Override
//			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
//					int arg2, long arg3) {
//				// TODO Auto-generated method stub
//				if(mMode == null){
//					mMode = startActionMode(new ModeCallback());
//					listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//				}else{
//					mMode.finish();
//					listview.setChoiceMode(ListView.CHOICE_MODE_NONE);
//					mMode = null;
//				}
//				Toast.makeText(DoubanImportActivity.this, "long click", Toast.LENGTH_SHORT).show();
//				return false;
//			}
//			
//		});

	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Used to put dark icons on light action bar

//        menu.add("全选")
//            .setIcon(R.drawable.abs__ic_cab_done_holo_dark)
//            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//
//        menu.add("导入书架")
//            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
//
//        menu.add("忽略")
//            .setIcon(R.drawable.abs__ic_clear_disabled)
//            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		getSupportMenuInflater().inflate(R.menu.menu_douban_import, menu);
		
        return true;
    }
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.abs__home:
			finish();
			break;
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_select_all:
			for(Map<String, Object> map : booklist){
				map.put("checked", true);
			}
			adapter = new MySimpleAdapter(this, booklist);
			adapter.notifyDataSetChanged();
			break;
		case R.id.menu_import_books:
			
			break;
		case R.id.menu_select_null:
			
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	//	private ActionMode mMode;
	private ListView listview;
	
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position,
//			long id) {
//		
//		if(mMode == null){
////			view.setBackgroundResource(R.drawable.abs__list_selector_background_transition_holo_light);
//			return;
//		}else{
//			SparseBooleanArray checked = getListView().getCheckedItemPositions();
//			
//			boolean hasCheckedElement = false;
//			for (int i = 0; i < checked.size() && !hasCheckedElement; i++) {
//				hasCheckedElement = checked.valueAt(i);
//			}
//
//			if (hasCheckedElement) {
//				//...
//			} else {
//				mMode.finish();
//				listview.setChoiceMode(ListView.CHOICE_MODE_NONE);
//			}
//		}
//		
//	};
	

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		switch (taskType) {
		case HttpListener.DOUBAN_OAUTH_JSON: // 获取到access token等json
			// 解析json，获取accessToken
			dbOAuth.parseJson4OAuth(this, String.valueOf(data));

			Log.d("DEBUG", "oauth: " + dbOAuth.getAccessToken() + "--"
					+ dbOAuth.getDoubanUserId());

			fetchBookCollection(dbOAuth.getDoubanUserId());

			break;
		case HttpListener.FETCH_BOOK_COLLECTION:
			StringBuilder collectionsBuilder = new StringBuilder();
			if (data == null) {
				Toast.makeText(DoubanImportActivity.this,
						"error: user not found !", Toast.LENGTH_SHORT)
						.show();
				break;
			}
			try {
				ArrayList<BookCollectionEntry> cs = DoubanBookUtils.parseCollections(""+data);
				for (BookCollectionEntry entry : cs) {
					collectionsBuilder.append(entry + "\n\n");
				}
				fillBookList(cs);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(this, "parse json exception...", Toast.LENGTH_SHORT).show();
			}
			loadingView.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Toast.makeText(DoubanImportActivity.this, "error: " + data,
				Toast.LENGTH_SHORT).show();
	}
	
	
	
	
//	private final class ModeCallback implements ActionMode.Callback {
//
//		@Override
//		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//			// Create the menu from the xml file
//			MenuInflater inflater = getSupportMenuInflater();
//			inflater.inflate(R.menu.actionbar_context_menu, menu);
//			return true;
//		}
//
//		@Override
//		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//			// Here, you can checked selected items to adapt available actions
//			return false;
//		}
//
//		@Override
//		public void onDestroyActionMode(ActionMode mode) {
//			// Destroying action mode, let's unselect all items
//			for (int i = 0; i < listview.getAdapter().getCount(); i++)
//				listview.setItemChecked(i, false);
//
//			if (mode == mMode) {
//				mMode = null;
//			}
//		}
//
//		@Override
//		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//			long[] selected = listview.getCheckedItemIds();
//			if (selected.length > 0) {
//				for (long id : selected) {
//					// Do something with the selected item
//				}
//			}
//			mode.finish();
//			return true;
//		}
//	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
//		Log.d("DEBUG", data.getStringExtra("url"));
		
		if(resultCode == RESULT_OK) {
			
			// 在这里处理获取返回的code参数
			String code = data.getStringExtra("code");
			// bookInfoTv.append("code: " + code + "\n");
			Log.d("DEBUG", "code: " + code);
			this.taskType = HttpListener.DOUBAN_OAUTH_JSON;
			dbOAuth.fetchAccessToken(this, code, (HttpListener)this);
			
		}
		
		
	}




	private class MySimpleAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private ArrayList<Map<String, Object>> list;

		public MySimpleAdapter(Context context,
				ArrayList<Map<String, Object>> list) {
			mInflater = LayoutInflater.from(context);
			this.list = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
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
			final int index = position;
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.douban_import_list_item, 
						parent, false);
				holder = new ViewHolder();
				holder.title = (TextView) convertView
						.findViewById(R.id.import_book_title);
				holder.publisher = (TextView) convertView
						.findViewById(R.id.import_book_publisher);
				holder.status = (TextView) convertView.findViewById(R.id.import_book_status);
				holder.checkBox = (CheckBox) convertView.findViewById(R.id.import_book_check);
				
				convertView.setTag(holder);
				
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			Map<String, Object> item = list.get(position);
			
			holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					// TODO Auto-generated method stub
					list.get(index).put("checked", isChecked);
				}
				
			});
			
			holder.title.setText((String)item.get("title"));
			holder.publisher.setText((String)item.get("publisher"));
			holder.status.setText((String)item.get("status"));
			holder.checkBox.setChecked((Boolean)item.get("checked"));

			return convertView;
		}

		public class ViewHolder {
			TextView title;
			TextView publisher;
			TextView status;
			CheckBox checkBox;
		}

	}

}
