package com.czzz.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ChatListView extends ListView{

	public ChatListView (Context context) {
	    super(context);
	}

	public ChatListView (Context context, AttributeSet attrs) {
	    super(context, attrs);
	}

	public ChatListView (Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
	    super.onSizeChanged(xNew, yNew, xOld, yOld);

	    setSelection(getCount());

	}
	
}
