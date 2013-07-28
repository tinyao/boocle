/*
 * Copyright 2011 woozzu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.czzz.view;

import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.czzz.demo.R;
import com.czzz.utils.TextUtils;

public class ShelfListView extends ListView {

    private View mHeaderContainer = null;
    private View mHeaderView = null;
    private ImageView mArrow = null;
    private ProgressBar mProgress = null;
    private float mY = 0;
    private float mHistoricalY = 0;
    private int mHistoricalTop = 0;
    private int mInitialHeight = 0;
    private boolean mFlag = false;
    private boolean mArrowUp = false;
    private boolean mIsRefreshing = false;
    private int mHeaderHeight = 0;
    private OnRefreshListener mListener = null;
    
    private boolean playBeep;

    private static final int REFRESH = 0;
    private static final int NORMAL = 1;
    private static final int HEADER_HEIGHT_DP = 285;
    private static final int HEADER_RFRESH_DP = 60;
    private static final String TAG = ShelfListView.class.getSimpleName();
    
//    private SharedPreferences sp;
    
    public ShelfListView(final Context context) {
        super(context);
//        initialize();
    }

    public ShelfListView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
//        initialize();
    }

    public ShelfListView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
//        initialize();
    }

    public void setOnRefreshListener(final OnRefreshListener l) {
        mListener = l;
    }

    public void setHeaderView(View container){
    	this.mHeaderContainer = container;
    	initialize();
    }
    
    public View getHeaderView(){
    	return mHeaderContainer;
    }
    
    public void completeRefreshing(boolean sound) {
    	
    	if(mIsRefreshing == false) return;
    	
        mProgress.setVisibility(View.INVISIBLE);
        mArrow.setVisibility(View.VISIBLE);
        mHandler.sendMessage(mHandler.obtainMessage(NORMAL, mHeaderHeight, 0));
        mIsRefreshing = false;
        invalidateViews();

        if(sound){
        	// play the pop music
        	playBeepSoundAndVibrate();
        }
        
    }
    
    public void completeRefreshingFail(boolean sound) {
        mProgress.setVisibility(View.INVISIBLE);
        mArrow.setVisibility(View.VISIBLE);
        
        mIsRefreshing = false;
        mHandler.sendMessage(mHandler.obtainMessage(NORMAL, mHeaderHeight, 0));
        invalidateViews();

        if(sound){
        	// play the pop music
        	playBeepSoundAndVibrate();
        }
        
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
    	
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHandler.removeMessages(REFRESH);
                mHandler.removeMessages(NORMAL);
                mY = mHistoricalY = ev.getY();
                if (mHeaderContainer.getLayoutParams() != null) {
                    mInitialHeight = mHeaderContainer.getLayoutParams().height;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
    	
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mHistoricalTop = getChildAt(0).getTop();
                break;
            case MotionEvent.ACTION_UP:
            	
            	int height = (int) (ev.getY() - mY) / 3 + mInitialHeight;
//            	if(height >= mHeaderHeight + 2){
//            		height = mHeaderHeight + 2;
//            	}
            	
                if (!mIsRefreshing) {
                    if (mArrowUp) {
                        startRefreshing();
                        mHandler.sendMessage(mHandler.obtainMessage(REFRESH, height, 0));
                    } else {
                        if (getChildAt(0).getTop() == 0) {
                            mHandler.sendMessage(mHandler.obtainMessage(NORMAL,
                            		height, 0));
                        }
                    }
                } else {
                    mHandler.sendMessage(mHandler.obtainMessage(REFRESH, height, 0));
                }
                mFlag = false;
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
    	
        if (ev.getAction() == MotionEvent.ACTION_MOVE && getFirstVisiblePosition() == 0) {
            float direction = ev.getY() - mHistoricalY;
            int height = (int) (ev.getY() - mY) / 3 + mInitialHeight;;
            
            float deltaY = Math.abs(mY - ev.getY());
            ViewConfiguration config = ViewConfiguration.get(getContext());
            if (deltaY > config.getScaledTouchSlop()) {

                // Scrolling downward
                if (direction > 0) {
                    // Refresh bar is extended if top pixel of the first item is
                    // visible
                    if (getChildAt(0).getTop() == 0) {
                        if (mHistoricalTop < 0) {

                            // mY = ev.getY(); // TODO works without
                            // this?mHistoricalTop = 0;
                        }

                        // Extends refresh bar
                        setHeaderHeight(height);
                        
                        // Stop list scroll to prevent the list from
                        // overscrolling
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        mFlag = false;
                    }
                } else if (direction < 0) {
                    // Scrolling upward

                    // Refresh bar is shortened if top pixel of the first item
                    // is
                    // visible
                	
//                	Log.d("DEBUG", "mInitHeight: " + mInitialHeight + "  height: " + height);
                	
                    if (getChildAt(0).getTop() == 0) {
                    	
                    	if(height < (int) (showHeaderHeight)){
                    		height = (int) (showHeaderHeight);
                    	}
                    	setHeaderHeight(height);

                        // If scroll reaches top of the list, list scroll is
                        // enabled
                        if (getChildAt(1) != null && getChildAt(1).getTop() <= 1 && !mFlag) {
                            ev.setAction(MotionEvent.ACTION_DOWN);
                            mFlag = true;
                        }
                    }
                }
            }

            
            mHistoricalY = ev.getY();
            
        }
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean performItemClick(final View view, final int position, final long id) {
        if (position == 0) {
            // This is the refresh header element
            return true;
        } else {
            return super.performItemClick(view, position - 1, id);
        }
    }
    
    private long lastUpdateAt;

    private synchronized void initHeaderView(){
    	if(mHeaderContainer == null){
    		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        	mHeaderContainer = inflater.inflate(R.layout.user_page_header2, this);
        }
    }
    
    private int showHeaderHeight = 0;
    
    private void initialize() {
    	
//    	sp = getContext().getSharedPreferences("config", 0);
//    	lastUpdateAt =
//    			sp.getLong("msg_last_update", Calendar.getInstance().getTimeInMillis()/1000l);
    	
    	initHeaderView();
    	
    	mHeaderView = mHeaderContainer.findViewById(R.id.user_page_header_main);
        mArrow = (ImageView) mHeaderContainer.findViewById(R.id.refreshable_list_arrow);
        mProgress = (ProgressBar) mHeaderContainer.findViewById(R.id.refreshable_list_progress);

        addHeaderView(mHeaderContainer);

        mHeaderHeight = (int) (HEADER_HEIGHT_DP * 
        		getContext().getResources().getDisplayMetrics().density);
        
        showHeaderHeight = (int) (mHeaderHeight - 
        		HEADER_RFRESH_DP * getContext().getResources().getDisplayMetrics().density);
        
        setHeaderHeight(showHeaderHeight);
        
        playBeep = true;
		AudioManager audioService = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
        
    }

    private synchronized void setHeaderHeight(final int height) {
    	
        if (height <= 1) {
            mHeaderView.setVisibility(View.GONE);
        } else {
            mHeaderView.setVisibility(View.VISIBLE);
        }

        // Extends refresh bar
        LayoutParams lp = (LayoutParams) mHeaderContainer.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        }
        lp.height = height;
        mHeaderContainer.setLayoutParams(lp);

        // Refresh bar shows up from bottom to top
        LinearLayout.LayoutParams headerLp = (LinearLayout.LayoutParams) mHeaderView
                .getLayoutParams();
        if (headerLp == null) {
            headerLp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT);
        }
        headerLp.topMargin = -mHeaderHeight + height;
        mHeaderView.setLayoutParams(headerLp);

        if (!mIsRefreshing) {
            // If scroll reaches the trigger line, start refreshing
            if (height > mHeaderHeight && !mArrowUp) {
                rotateArrow();
                mArrowUp = true;
            } else if (height < mHeaderHeight && mArrowUp) {
                rotateArrow();
                mArrowUp = false;
            }
        }
    }
    
    private void rotateArrow() {
    	mArrow.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate));
    	
        Drawable drawable = mArrow.getDrawable();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.save();
        canvas.rotate(180.0f, canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        canvas.restore();
        mArrow.setImageBitmap(bitmap);
//    	anim = AnimationUtils.loadAnimation(this.getContext(), R.anim.refresh_animation);      
//        
//    	mArrow.startAnimation(anim); 
    }

    private void startRefreshing() {
        mArrow.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        mIsRefreshing = true;
        
        if (mListener != null) {
            mListener.onRefresh(this);
        }
    }

    private final Handler mHandler = new Handler() {

        @Override
        public synchronized void handleMessage(final Message msg) {
            super.handleMessage(msg);

            int limit = 0;
            switch (msg.what) {
                case REFRESH:
                	// refresh 回到headerHeight
                	limit = mHeaderHeight;
                    break;
                case NORMAL:
                	// refresh over 回到0
                    limit = (int) (showHeaderHeight);
                    break;
            }

            // Elastic scrolling
            if (msg.arg1 >= limit) {
            	
            	// 当刷新时，被停止则 停止刷新状态下的header回弹
            	if(!mIsRefreshing && msg.what == REFRESH) return;
            	
                setHeaderHeight(msg.arg1);
                int displacement = (msg.arg1 - limit);
                
                if (displacement <= 20) {
//                	if(displacement == 0){
//                		mHandler.sendMessage(mHandler.obtainMessage(msg.what, msg.arg1 - 1, 0));
//                	}else{
//                		mHandler.sendMessage(mHandler.obtainMessage(msg.what, msg.arg1 - 3, 0));
//                	}
//                	mHandler.sendMessage(mHandler.obtainMessage(msg.what, limit,
//                            0));
                    setHeaderHeight(limit);
                } else {
                    mHandler.sendMessage(mHandler.obtainMessage(msg.what, msg.arg1 - 20,
                            0));
                }
                
//                if (displacement == 0) {
//                	mHandler.sendMessage(mHandler.obtainMessage(msg.what, msg.arg1 - 1, 0));
//                } else {
//                    mHandler.sendMessage(mHandler.obtainMessage(msg.what, msg.arg1 - displacement,
//                            0));
//                }
                
            }
        }

    };

    
    private MediaPlayer mediaPlayer;
    private static final float BEEP_VOLUME = 1.00f;
    
    private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			((Activity)getContext()).setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.weico_loaded);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}
    
    /**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	
	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
	}
    
    public interface OnRefreshListener {
        public void onRefresh(ShelfListView listView);
    }
    
    
    private class HeaderView extends View{

    	public HeaderView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
    	
    	public HeaderView(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
		}
    	
		public HeaderView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			// TODO Auto-generated constructor stub
		}
    	
    }

}