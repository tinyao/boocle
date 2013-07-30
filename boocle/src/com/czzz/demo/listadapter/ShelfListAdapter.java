package com.czzz.demo.listadapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.czzz.bookcircle.BookCollection;
import com.czzz.demo.R;
import com.czzz.utils.ShelfCoverDownloader;
import com.czzz.utils.TextUtils;

public class ShelfListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<BookCollection> userlist;

	public ShelfListAdapter(Context context,
			ArrayList<BookCollection> list) {
		mInflater = LayoutInflater.from(context);
		this.userlist = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return userlist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return userlist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	ShelfCoverDownloader coverloader = ShelfCoverDownloader.getInstance();
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.bookcase_list_detail_item, 
					parent, false);
			holder = new ViewHolder();
			holder.cover = (ImageView) convertView
					.findViewById(R.id.bookcase_detail_item_cover);
			holder.title = (TextView) convertView
					.findViewById(R.id.bookcase_detail_item_title);
			holder.pulisher = (TextView) convertView.findViewById(R.id.bookcase_detail_item_publisher);
			holder.author = (TextView) convertView.findViewById(R.id.bookcase_detail_item_author);
			holder.addTime = (TextView) convertView.findViewById(R.id.bookcase_detail_item_time);
			holder.desc = (TextView) convertView.findViewById(R.id.bookcase_detail_item_desc);
			holder.ratingBar = (RatingBar) convertView.findViewById(R.id.shelf_book_score);
			holder.statusImg = (ImageView) convertView.findViewById(R.id.bookcase_detail_item_status);
			convertView.setTag(holder); 
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		BookCollection entry = userlist.get(position);

		holder.title.setText(entry.book.title);
		coverloader.download(entry.book.image, holder.cover);
		holder.pulisher.setText(entry.book.publisher);
		holder.author.setText(entry.book.author);
		holder.addTime.setText(TextUtils.formatSmartTime(entry.create_at));
		holder.desc.setText(entry.note.equals("") ? "" : "『" + entry.note + "』");
		holder.ratingBar.setRating(entry.score);
		if(entry.status == 1){
			holder.statusImg.setVisibility(View.VISIBLE);
		} else{
			holder.statusImg.setVisibility(View.GONE);
		}
		return convertView;
	}

	public class ViewHolder {
		ImageView cover;
		TextView title;
		TextView author;
		TextView pulisher;
		TextView desc;
		TextView addTime;
		RatingBar ratingBar;
		ImageView statusImg;
	}
	
	
//	SyncImageLoader.OnImageLoadListener imageLoadListener = new SyncImageLoader.OnImageLoadListener(){  
//		  
//	    @Override  
//	    public void onImageLoad(Integer t, Drawable drawable) {  
//	        //BookModel model = (BookModel) getItem(t);  
//	        View view = mListView.findViewWithTag(t);  
//	        if(view != null){  
//	            ImageView iv = (ImageView) view.findViewById(R.id.bookcase_detail_item_cover);  
//	            iv.setBackgroundDrawable(drawable);  
//	        }  
//	    }  
//	    @Override  
//	    public void onError(Integer t) {  
//	    	BookCollectionEntry model = (BookCollectionEntry) getItem(t);  
//	        View view = mListView.findViewWithTag(model);  
//	        if(view != null){  
//	            ImageView iv = (ImageView) view.findViewById(R.id.bookcase_detail_item_cover);  
//	            iv.setBackgroundResource(R.drawable.default_bookcover0);  
//	        }  
//	    }  
//	      
//	};  
	  
//	public void loadImage(){  
//	    int start = mListView.getFirstVisiblePosition();  
//	    int end =mListView.getLastVisiblePosition();  
//	    if(end >= getCount()){  
//	        end = getCount() -1;  
//	    }  
//	    syncImageLoader.setLoadLimit(start, end);  
//	    syncImageLoader.unlock();  
//	}  
//	  
//	AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {  
//	      
//	    @Override  
//	    public void onScrollStateChanged(AbsListView view, int scrollState) {  
//	        switch (scrollState) {  
//	            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:  
////	                DebugUtil.debug("SCROLL_STATE_FLING");
//	            	Log.d("DEBUG", "srcoll state fling");
////	                syncImageLoader.lock();  
//	                break;  
//	            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:  
////	                DebugUtil.debug("SCROLL_STATE_IDLE");  
//	                Log.d("DEBUG", "scroll state idle");
////	                loadImage();  
//	                //loadImage();  
//	                break;  
//	            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:  
////	                syncImageLoader.lock();  
//	            	Log.d("DEBUG", "scroll state _touch _scroll...");
//	                break;  
//	  
//	            default:  
//	                break;  
//	        }  
//	          
//	    }  
//	      
//	    @Override  
//	    public void onScroll(AbsListView view, int firstVisibleItem,  
//	            int visibleItemCount, int totalItemCount) {  
//	        // TODO Auto-generated method stub  
//	          Log.d("DEBUG", "on scroll ...");
//	    }  
//	};  
	
}