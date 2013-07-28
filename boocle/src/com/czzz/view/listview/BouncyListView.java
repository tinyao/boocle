package com.czzz.view.listview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;

public class BouncyListView extends ListView {
	private Context context;
	private boolean outBound = false;
	private int distance;
	private int firstOut;
	private static final String TAG = "wljie";

	public BouncyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		Log.d(TAG, "IN 1");
	}

	public BouncyListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		Log.d(TAG, "IN 2");
	}

	public BouncyListView(Context context) {
		super(context);
		this.context = context;
		Log.d(TAG, "IN 3");
	}

	@SuppressWarnings("deprecation")
	GestureDetector gestureDetector = new GestureDetector(
			new OnGestureListener() {
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void onShowPress(MotionEvent e) {
					// TODO Auto-generated method stub
				}

				/**
				 * 手势滑动的时候触发
				 */
				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2,
						float distanceX, float distanceY) {
					Log.d(TAG, "ENTER onscroll");
					int firstPos = getFirstVisiblePosition();
					int lastPos = getLastVisiblePosition();
					int itemCount = getCount();
					// outbound Top
					if (outBound && firstPos != 0 && lastPos != (itemCount - 1)) {
						scrollTo(0, 0);
						return false;
					}
					View firstView = getChildAt(firstPos);
					if (!outBound)
						firstOut = (int) e2.getRawY();
					if (firstView != null
							&& (outBound || (firstPos == 0
									&& firstView.getTop() == 0 && distanceY < 0))) {
						// Record the length of each slide
						distance = firstOut - (int) e2.getRawY();
						scrollTo(0, distance / 2);
						return true;
					}
					// outbound Bottom
					return false;
				}

				@Override
				public void onLongPress(MotionEvent e) {
					// TODO Auto-generated method stub
				}

				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2,
						float velocityX, float velocityY) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean onDown(MotionEvent e) {
					// TODO Auto-generated method stub
					return false;
				}
			});

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		Log.d(TAG, "dispatchTouchEvent");
		int act = event.getAction();
		if ((act == MotionEvent.ACTION_UP || act == MotionEvent.ACTION_CANCEL)
				&& outBound) {
			outBound = false;
			// scroll back
		}
		if (!gestureDetector.onTouchEvent(event)) {
			outBound = false;
		} else {
			outBound = true;
		}
		Rect rect = new Rect();
		getLocalVisibleRect(rect);
		TranslateAnimation am = new TranslateAnimation(0, 0, -rect.top, 0);
		am.setDuration(1000);
		startAnimation(am);
		scrollTo(0, 0);
		return super.dispatchTouchEvent(event);
	}
}
