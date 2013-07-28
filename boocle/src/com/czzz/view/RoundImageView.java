package com.czzz.view;

import java.util.Random;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.czzz.demo.R;
import com.czzz.utils.ImageUtils;

public class RoundImageView extends ImageView{

	private int corner = 5;
	
	public RoundImageView(Context context) {
        super(context);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray array = context.obtainStyledAttributes(attrs, 
        		R.styleable.RoundImageView);

        corner = array.getInt(R.styleable.RoundImageView_corner, 5);
        array.recycle();
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray array = context.obtainStyledAttributes(attrs, 
        		R.styleable.RoundImageView, defStyle, 0);

        corner = array.getInt(R.styleable.RoundImageView_corner, 5);
        array.recycle();
    }
    
    public void setCornerRadius(int radius){
    	this.corner = radius;
    }

    int[] colors = {this.getResources().getColor(R.color.orange_pumpkin), 
    		this.getResources().getColor(R.color.green_emerald), 
    		this.getResources().getColor(R.color.blue_peter_river),
    		this.getResources().getColor(R.color.red_alizarin), 
    		this.getResources().getColor(R.color.purple_amethyst)};
    Random r;
    
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
//		super.onDraw(canvas);
		BitmapDrawable drawable = (BitmapDrawable) getDrawable();

		Bitmap mScaledBitmap;
		
        if (drawable == null) {
        	Log.d("DEBUG", "no drawable");
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return; 
        }
        
        int scaledWidth = getMeasuredWidth();
        int scaledHeight = getMeasuredHeight();

        Bitmap fullSizeBitmap = drawable.getBitmap();
        if(fullSizeBitmap == null){
        	Log.d("DEBUG", "no bitmap");
        	Paint paint = new Paint();
        	Rect rect = new Rect(0, 0, scaledWidth, scaledHeight);
            RectF rectF = new RectF(rect);
        	
        	paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            
            if (r == null) r = new Random();
            
            paint.setColor(colors[r.nextInt(5)]);
            this.getResources().getColor(R.color.yellow_sun_flower);
            
            canvas.drawRoundRect(rectF, corner, corner, paint);
        	return;
        }

        if (scaledWidth == fullSizeBitmap.getWidth() && scaledHeight == fullSizeBitmap.getHeight()) {
            mScaledBitmap = fullSizeBitmap;
        } else {
            mScaledBitmap = Bitmap.createScaledBitmap(fullSizeBitmap, 
            		scaledWidth, scaledHeight, true /* filter */);
        }

        Bitmap roundBitmap = ImageUtils.getRoundedCornerBitmap(mScaledBitmap, corner);
        canvas.drawBitmap(roundBitmap, 0, 0, null);
	}
	
}
