package com.czzz.view;

import com.czzz.demo.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.czzz.base.User;
import com.czzz.bookcircle.BookCollection;
import com.czzz.demo.ConversationActivity;
import com.czzz.demo.UserPageActivity;
import com.czzz.utils.ImagesDownloader;
import com.czzz.utils.TextUtils;

public class CollectionDropDialog extends Dialog{

	private Context context;
	private BookCollection entry;
	
	public CollectionDropDialog(Context context, BookCollection entry) {
		super(context, R.style.drop_dialog_style);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.entry = entry;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_collection_detail);
        
        getWindow().setGravity(Gravity.TOP);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        this.setCanceledOnTouchOutside(true);
        
        TextView info = (TextView) findViewById(R.id.collection_dialog_info);
        Button toUser = (Button) findViewById(R.id.collection_dialog_to_userpage);
        Button toMsg = (Button) findViewById(R.id.collection_dialog_to_msg);
        ImageView avatar = (ImageView) findViewById(R.id.collection_dialog_avatar);
        TextView ownerV = (TextView) findViewById(R.id.collection_dialog_status);
        TextView timeV = (TextView) findViewById(R.id.collection_dialog_time);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.collection_dialog_rating);
        
        info.setText(entry.note);
        new ImagesDownloader(ImagesDownloader.AVATAR_TASK).download(entry.owner_avatar, avatar);
        ownerV.setText((entry.status == 0 ? "" : "二手 "));
        timeV.setText(TextUtils.formatHomeSmartTime(entry.create_at));
        ratingBar.setRating(entry.score);
        
        toUser.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent toUserPageIntent = new Intent(context, UserPageActivity.class);
				User user = new User();
				user.name = entry.owner;
				user.uid = entry.owner_id;
				user.avatar = entry.owner_avatar;
				toUserPageIntent.putExtra("user", user);
				toUserPageIntent.putExtra("from_book_detail", true);
				CollectionDropDialog.this.dismiss();
				context.startActivity(toUserPageIntent);
				((Activity) context).overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}

        });
        
        toMsg.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(entry.owner_id == User.getInstance().uid){
					Toast.makeText(context, "亲，不用给自己私信了吧", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Intent toMsgIntent = new Intent(context, ConversationActivity.class);
				toMsgIntent.putExtra("thread_uid", entry.owner_id);
				toMsgIntent.putExtra("thread_name", entry.owner);
				toMsgIntent.putExtra("thread_avatar", entry.owner_avatar);
				toMsgIntent.putExtra("book_title", entry.book.title);
				CollectionDropDialog.this.dismiss();
				context.startActivity(toMsgIntent);
				((Activity) context).overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
        	
        });
        
	}
	
}
