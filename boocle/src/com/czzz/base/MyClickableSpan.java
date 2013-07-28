package com.czzz.base;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

/**
 * If an object of this type is attached to the text of a TextView with a
 * movement method of LinkMovementMethod, the affected spans of text can be
 * selected. If clicked, the {@link #onClick} method will be called.
 * 
 * @author 张宁
 */
public class MyClickableSpan extends ClickableSpan {

	int color = -1;
	private Context context;
	private Intent intent;

	public MyClickableSpan(Context context, Intent intent) {
		this(-1, context, intent);
	}

	/**
	 * constructor
	 * @param color the link color
	 * @param context
	 * @param intent
	 */
	public MyClickableSpan(int color, Context context, Intent intent) {
		if (color!=-1) {
			this.color = color;
		}
		this.context = context;
		this.intent = intent;
	}

	/**
	 * Performs the click action associated with this span.
	 */
	public void onClick(View widget){
		context.startActivity(intent);
	};

	/**
	 * Makes the text without underline.
	 */
	@Override
	public void updateDrawState(TextPaint ds) {
		if (color == -1) {
			ds.setColor(ds.linkColor);
		} else {
			ds.setColor(color);
		}
		ds.setUnderlineText(false);
	}
	
}