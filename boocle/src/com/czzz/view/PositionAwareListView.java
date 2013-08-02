package com.czzz.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PositionAwareListView extends ListView{

	public PositionAwareListView(final Context context) {
        super(context);
    }

    public PositionAwareListView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public PositionAwareListView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

	@Override
	public void setAdapter(ListAdapter adapter) {
		// TODO Auto-generated method stub
		
		super.setAdapter(adapter);
	}
	
	public void setAdapter(ListAdapter adapter, boolean maintainOffset) {
		// TODO Auto-generated method stub
		if(!maintainOffset) setAdapter(adapter);
		Offset off = getListOffset();
		setAdapter(adapter);
		this.setSelectionFromTop(off.pos, off.top);
	}
	
	private class Offset{
		int pos, top;
		public Offset(int pos, int top){
			this.pos = pos;
			this.top = top;
		}
	}
	
	private Offset getListOffset(){
		int savedPosition = this.getFirstVisiblePosition();
		View firstVisibleView = this.getChildAt(0);
		int savedTop = (firstVisibleView == null) ? 0 : firstVisibleView.getTop();
		return new Offset(savedPosition, savedTop);
	}
    
	
}
