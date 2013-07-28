package com.czzz.demo.setting;

import com.czzz.demo.R;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.czzz.utils.ImagesDownloader;
import com.czzz.view.tablelist.model.BasicItem;
import com.czzz.view.tablelist.widget.UITableView;
import com.czzz.view.tablelist.widget.UITableView.ClickListener;

public class BindSocialActivity extends SherlockActivity{

	UITableView tableView;
	ImageView img;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bind_social);
		tableView = (UITableView) findViewById(R.id.bind_social);        
        createList();        
        img = (ImageView) findViewById(R.id.img_s);
        Log.d("MainActivity", "total items: " + tableView.getCount());        
        tableView.commit();
        
        new ImagesDownloader().download("http://czzz.org/wp-content/uploads/2011/10/touxiang-211x300.png", img);
        
	}
	
	private void createList() {
    	CustomClickListener listener = new CustomClickListener();
    	tableView.setClickListener(listener);
    	BasicItem i1 = new BasicItem("绑定豆瓣帐号");
    	i1.setDrawable(R.drawable.douban_icon);   	
    	tableView.addBasicItem(i1);
    	
    	BasicItem i2 = new BasicItem("绑定人人帐号");
    	i2.setDrawable(R.drawable.renren_icon);   	
    	tableView.addBasicItem(i2);
    	    	
    	BasicItem i3 = new BasicItem("绑定新浪微博帐号");
    	i3.setDrawable(R.drawable.sina_weibo); 
    	tableView.addBasicItem(i3);

    }
    
    private class CustomClickListener implements ClickListener {

		@Override
		public void onClick(int index) {
			Toast.makeText(BindSocialActivity.this, "item clicked: " + index, Toast.LENGTH_SHORT).show();
		}
    	
    }

	
	
}
