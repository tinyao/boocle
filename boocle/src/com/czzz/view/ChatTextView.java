package com.czzz.view;

import com.czzz.utils.TextUtils;

import android.content.Context;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.widget.TextView;

public class ChatTextView extends TextView{

	public ChatTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public ChatTextView(Context context, AttributeSet attrs){
		// TODO Auto-generated constructor stub
		super(context, attrs);
	}
	
	public ChatTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		// TODO Auto-generated method stub
		SpannableString text_SS = new SpannableString(text);  
		text_SS = TextUtils.decorateTrendInSpannableString(this.getContext(), text_SS);  
		
		super.setText(text_SS, type);
	}
	
}
