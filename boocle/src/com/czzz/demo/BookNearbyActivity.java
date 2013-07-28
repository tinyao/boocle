package com.czzz.demo;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.czzz.base.AsyncTaskActivity;
import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.bookcircle.BookUtils;
import com.czzz.demo.listadapter.NearbyBooksAdapter;
import com.czzz.utils.HttpListener;
import com.czzz.utils.TextUtils;
import com.czzz.view.SchoolPopupDialog;
import com.czzz.view.SpinnerPopupDialog;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class BookNearbyActivity extends AsyncTaskActivity{
	
	NearbyBooksAdapter nearbyAdapter;
	private ListView booksListView;
	private View loadingView;
//	private View footer;
	
	private Button schoolBtn, typeBtn, sortBtn;
	
	ArrayList<BookCollection> nearbyBooks = new ArrayList<BookCollection>();
	
	private ProgressBar morePd;
	private int sort = 0;
	private int status = 2;
	private int school_id = User.getInstance().school_id;
	private SchoolPopupDialog schoolDialog;
	
	private View footerView;
//	private boolean hasFooter = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_books_nearby);
		
		booksListView = (ListView)findViewById(R.id.nearby_books_list);
		loadingView = (View)findViewById(R.id.loading_view);
		sortBtn = (Button)findViewById(R.id.nearby_books_sort);
		typeBtn = (Button)findViewById(R.id.nearby_books_status);
		schoolBtn = (Button)findViewById(R.id.nearby_books_school);
		
		footerView = LayoutInflater.from(this)
				.inflate(R.layout.pulldown_footer, null);
		
		booksListView.addFooterView(footerView);
		View footer = findViewById(R.id.footer_view);
		morePd = (ProgressBar) findViewById(R.id.pulldown_footer_loading);
		morePd.setIndeterminate(false); 
		
		fetchNeabyBooks(0, 9, school_id, status, sort);
		
		footer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("DEBUG", "footer clicked ~");
				loadMoreBooks();
			}
			
		});

		schoolBtn.setOnClickListener(listener);
		sortBtn.setOnClickListener(listener);
		typeBtn.setOnClickListener(listener);
		
		booksListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				BookCollection book = nearbyBooks.get(arg2);
				Intent detailIntent = new Intent(BookNearbyActivity.this, BookCollectionDetailActivity.class);
				detailIntent.putExtra("book", (Serializable)book);
				startActivity(detailIntent);
			}
			
		});
		
	}
	
	
	private OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int[] location = new int[2];// x,y
			int height = sortBtn.getHeight();
			int width = sortBtn.getWidth();
			sortBtn.getLocationOnScreen(location);
			final SpinnerPopupDialog dialog;
			switch(v.getId()){
			case R.id.nearby_books_school:
				if(schoolDialog == null){
					schoolDialog = new SchoolPopupDialog(
							BookNearbyActivity.this, R.style.school_popup_style,
							location[1] + height/2);
				}
				schoolDialog.setOnDismissListener(new OnDismissListener(){

					@Override
					public void onDismiss(DialogInterface dialogInterface) {
						// TODO Auto-generated method stub
						String school = schoolDialog.updateUniversity;
						int sid = schoolDialog.updateUnivId;
						if(schoolDialog.changed){
							school_id = sid;
							if(school_id == User.getInstance().school_id){
								schoolBtn.setText("我的学校");
							}else{
								schoolBtn.setText(school);
							}
							nearbyBooks.clear();
							schoolDialog.changed = false;
							fetchNeabyBooks(0, 9, school_id, status, sort);
						}
					}
					
				});
				schoolDialog.show();
				break;
			case R.id.nearby_books_sort:
				dialog = new SpinnerPopupDialog(BookNearbyActivity.this, 
						R.style.spinner_popup_style, 
						location[1] + height/2, width, 3);
				dialog.show();
				dialog.setOnDismissListener(new OnDismissListener(){

					@Override
					public void onDismiss(DialogInterface arg0) {
						// TODO Auto-generated method stub
						if(dialog.selectId != -1){
							sort = dialog.selectId;
							nearbyBooks.clear();
							fetchNeabyBooks(0, 9, school_id, status, sort);
							sortBtn.setText(dialog.selectStr);
						}
					}
					
				});
				break;
			case R.id.nearby_books_status:
				dialog = new SpinnerPopupDialog(BookNearbyActivity.this, 
						R.style.spinner_popup_style, 
						location[1] + height/2, width, 2);
				dialog.show();
				dialog.setOnDismissListener(new OnDismissListener(){

					@Override
					public void onDismiss(DialogInterface arg0) {
						// TODO Auto-generated method stub
						if(dialog.selectId != -1){
							// status = 0, 1, 2 （非二手，二手，全部）
							status = dialog.selectId==0 ? 2 : dialog.selectId - 1;
							nearbyBooks.clear();
							fetchNeabyBooks(0, 9, school_id, status, sort);
							typeBtn.setText(dialog.selectStr);
						}
					}
					
				});
				break;
			}
		}
		
	};

	
	private void loadMoreBooks() {
		// TODO Auto-generated method stub
		footerView.setVisibility(View.VISIBLE);
		morePd.setVisibility(View.VISIBLE);
		this.taskType = HttpListener.FETCH_NEARBY_BOOKS;
		BookUtils.fetchNearby(
				school_id, nearbyBooks.size() , 9, status, sort, null);
	}


	/**
	 * 
	 * @param start
	 * @param count
	 * @param status
	 * @param sort
	 */
	private void fetchNeabyBooks(int start, int count, int school_id, int status, int sort) {
		// TODO Auto-generated method stub
		footerView.setVisibility(View.VISIBLE);
		loadingView.setVisibility(View.VISIBLE);
		this.taskType = HttpListener.FETCH_NEARBY_BOOKS;
		BookUtils.fetchNearby(
				school_id, start, count, status, sort, null);
	}
	

	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
		String recvData = TextUtils.unicodeToString("" + data);
		Log.d("DEBUG", "" + recvData);
		switch(taskType){
		case HttpListener.FETCH_NEARBY_BOOKS:
			
			ArrayList<BookCollection> books = BookUtils.parseNearybyBooks(recvData);
			
			if(books == null){
				Crouton.makeText(this, "没有了..", Style.ALERT).show();
				morePd.setVisibility(View.GONE);
				loadingView.setVisibility(View.GONE);
				footerView.setVisibility(View.GONE);
				return;
			}else{
				footerView.setVisibility(View.VISIBLE);
			}
			
			if(books.size() < 9){
				footerView.setVisibility(View.GONE);
			}
			
			nearbyBooks.addAll(books);
			
			if(nearbyAdapter == null){
				nearbyAdapter = new NearbyBooksAdapter(this, nearbyBooks);
				booksListView.setAdapter(nearbyAdapter);
			} else {
				nearbyAdapter = new NearbyBooksAdapter(this, nearbyBooks);
				nearbyAdapter.notifyDataSetChanged();  
			}
			
			loadingView.setVisibility(View.GONE);
			morePd.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
		Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
		loadingView.setVisibility(View.GONE);
		morePd.setVisibility(View.GONE);
	}
	
}

