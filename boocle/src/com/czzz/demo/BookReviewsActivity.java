package com.czzz.demo;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.czzz.base.BaseListActivity;
import com.czzz.base.WebActivity;
import com.czzz.douban.BookCommentEntry;
import com.czzz.douban.DoubanBookUtils;
import com.czzz.utils.HttpListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class BookReviewsActivity extends BaseListActivity implements HttpListener{

	int taskType;
	private String isbn;
	private String title;
	private List<BookCommentEntry> reviews;
	private View progressView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mActionBar = getSupportActionBar();

        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayUseLogoEnabled(false);
        
        setContentView(R.layout.book_reviews);
        progressView = (View) findViewById(R.id.review_list_pd_view);
        
        isbn = getIntent().getStringExtra("isbn");
        mActionBar.setDisplayShowTitleEnabled(true);
        title = getIntent().getStringExtra("title");
        
        mActionBar.setTitle(getResources().getString(R.string.book_reviews) + ": " + title);
        
        if(isbn != null){
        	fetchBookComments(isbn);
        }
        
	}
	
	
	/**
	 * 根据isbn获取书籍评论
	 * @param isbn
	 */
	protected void fetchBookComments(String isbn) {
		// TODO Auto-generated method stub
//		pd = new ProgressDialog(this);
//		pd.setMessage("正在从豆瓣获取书籍评论...");
//		pd.show();
		taskType = HttpListener.FETCH_BOOK_COMMENTS;
		DoubanBookUtils.fetchBookComments(this, (HttpListener)this, taskType, isbn);
	}


	/**
	 * 获取评论列表后处理
	 */
	@Override
	public void onTaskCompleted(Object data) {
		// TODO Auto-generated method stub
//		if(pd != null) pd.dismiss();
		StringBuilder commentsBuilder = new StringBuilder();
		if(data == null){
			Crouton.makeText(this, "Error: book not found !", Style.ALERT).show();
			return;
		}
		reviews = (List<BookCommentEntry>)data;
		for(BookCommentEntry entry : reviews){
			commentsBuilder.append(entry + "\n\n");
		}
		
		displayReviewList(reviews);
		progressView.setVisibility(View.GONE);
		
		Log.d("DEBUG", "book-comments:\n" + commentsBuilder.toString());
	}


	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		
		String url = "http://m.douban.com/book/review/" + reviews.get(position).id + "/";
		Intent it = new Intent(this, WebActivity.class);
		it.putExtra("url", url);
		it.putExtra("title", title);
		startActivity(it);
		
	}


	private void displayReviewList(List<BookCommentEntry> reviewList) {
		// TODO Auto-generated method stub
		BookReviewsAdapter adapter = new BookReviewsAdapter(this, reviewList);
		setListAdapter(adapter);
		
	}


	@Override
	public void onTaskFailed(String data) {
		// TODO Auto-generated method stub
//		if(pd != null) { 
//			pd.dismiss();
//		}
		Crouton.makeText(BookReviewsActivity.this, data, Style.ALERT).show();
	}
	
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()){
		case R.id.abs__home:
			finish();
			break;
		case android.R.id.home:
			finish();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	

	private class BookReviewsAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private List<BookCommentEntry> list;

		public BookReviewsAdapter(Context context,
				List<BookCommentEntry> list) {
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
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.book_reviews_item, 
						parent, false);
				holder = new ViewHolder();
				holder.title = (TextView) convertView
						.findViewById(R.id.review_item_title);
				holder.author = (TextView) convertView
						.findViewById(R.id.review_item_author);
				holder.text = (TextView) convertView.findViewById(R.id.review_item_text);
				holder.rateBar = (RatingBar) convertView.findViewById(R.id.review_item_ratingbar);
				
				convertView.setTag(holder); 
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.title.setText(list.get(position).title);
			Resources res = getResources();
			String reviewFrom = String.format(
			    res.getString(R.string.book_review_from), list.get(position).author_name);
			holder.author.setText(reviewFrom);
			holder.text.setText(list.get(position).summary);
			if(list.get(position).rating != null){
				int rating = Integer.valueOf(list.get(position).rating);
				holder.rateBar.setRating(rating);
			}
			return convertView;
		}

		public class ViewHolder {
			TextView title;
			TextView author;
			TextView text;
			RatingBar rateBar;
		}

	}
	
}
