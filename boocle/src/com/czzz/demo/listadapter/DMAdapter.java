package com.czzz.demo.listadapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.czzz.base.User;
import com.czzz.bookcircle.DirectMsg;
import com.czzz.demo.ConversationActivity;
import com.czzz.demo.R;
import com.czzz.demo.UserPageActivity;
import com.czzz.utils.ImagesDownloader;
import com.czzz.utils.TextUtils;


/**
 * 
 * @author tinyao
 *
 */
public class DMAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<DirectMsg> msglist;
	private int RECV_MSG = 0;  
    private int SEND_MSG = 1; 
    private Context con;
    
	public DMAdapter(Context context,
			ArrayList<DirectMsg> list) {
		mInflater = LayoutInflater.from(context);
		this.con = context;
		this.msglist = list;
		
		if(User.getInstance().uid == 0){
			User.getInstance().init(context);
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return msglist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return msglist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override  
    public int getItemViewType(int position) {  
        // 区别两种view的类型，标注两个不同的变量来分别表示各自的类型  
        DirectMsg entity = msglist.get(position);  
        if (entity.is_recv)  
        {  
            return RECV_MSG;  
        }else{  
            return SEND_MSG;  
        }  
    }  

    @Override  
    public int getViewTypeCount() {  
        // 这个方法默认返回1，如果希望listview的item都是一样的就返回1，我们这里有两种风格，返回2  
        return 2;  
    }  
	
	private ImagesDownloader imagesLoader = ImagesDownloader.getInstance();
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			if(msglist.get(position).is_recv){
				convertView = mInflater.inflate(R.layout.msg_item_recv, 
						parent, false);
			}else{
				convertView = mInflater.inflate(R.layout.msg_item_send, 
						parent, false);
				holder.loading = (ProgressBar) convertView.findViewById(R.id.msg_item_send_progress);
			}
			
			holder.avatar = (ImageView) convertView.findViewById(R.id.msg_item_avatar);
			holder.body = (TextView) convertView.findViewById(R.id.msg_item_body);
			holder.time = (TextView) convertView.findViewById(R.id.msg_item_time);
			holder.bubble = (View) convertView.findViewById(R.id.msg_bubble_bg);
			convertView.setTag(holder); 
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		final DirectMsg msg = msglist.get(position);

		holder.body.setText(msg.body);
		
		if(position==0 || Math.abs((msg.create_at - msglist.get(position-1).create_at)) > 20 * 60){
			holder.time.setText(TextUtils.formatChatListTime(msg.create_at));
			holder.time.setVisibility(View.VISIBLE);
		}else{
			holder.time.setVisibility(View.GONE);
		}
		
		if(msg.is_recv){
			imagesLoader.download(msg.thread_avatar, holder.avatar);
		}else{
			imagesLoader.download(User.getInstance().avatar, holder.avatar);
		}
		
		if(!msg.is_recv){
			if(msg.is_sending){
				holder.loading.setVisibility(View.VISIBLE);
			}else{
				holder.loading.setVisibility(View.GONE);
			}
		}

		holder.bubble.setOnLongClickListener(new OnLongClickListener(){

			@SuppressWarnings("deprecation")
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) con.getSystemService(Context.CLIPBOARD_SERVICE);
			    clipboard.setText(msg.body);
				Toast.makeText(con, "复制到剪贴板", Toast.LENGTH_SHORT).show();
				return false;
			}
			
		});
		
		holder.avatar.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				User mmuser;
				
				if(msg.is_recv){
					mmuser = new User();
					mmuser.avatar = msg.thread_avatar;
					mmuser.uid = msg.thread_uid;
					mmuser.name = msg.thread_name;
				}else{
					mmuser = User.getInstance();
				}
				
				Intent userPageIntent = new Intent(con, UserPageActivity.class);
				userPageIntent.putExtra("user", mmuser);
				userPageIntent.putExtra("from_book_detail", true);
				con.startActivity(userPageIntent);
			}
			
		});
		
		return convertView;
	}

	public class ViewHolder {
		ImageView avatar;
		TextView body;
		TextView time;
		ProgressBar loading;
		View bubble;
	}

}
