package com.czzz.view;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.czzz.base.MyClickableSpan;

public class LinkTextView {
	
	public static void setClickableTextView(Context context, TextView textView,  
            List<String> textList, List<Intent> intentList) {  
        if (textList == null || intentList == null) {  
            return;  
        }  
        SpannableStringBuilder builder = new SpannableStringBuilder();  
        int end = -1, length = -1;  
        int size = textList.size();  
        Intent intent;  
        for (int i = 0; i < size; i++) {  
            String text = textList.get(i);  
            if (TextUtils.isEmpty(text)) {  
                continue;  
            }  
            builder.append(textList.get(i));  
            if ((intent = intentList.get(i)) != null) {  
                end = builder.length();  
                length = textList.get(i).length();  
                builder.setSpan(getClickableSpan(context, intent),  
                        end - length, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  
            }  
            builder.append(" ");  
        }  
        textView.setText(builder);  
        textView.setFocusable(true);  
        textView.setMovementMethod(LinkMovementMethod.getInstance());  
    }  
	
	/** 
     *  make textview a clickable textview<br> 
     *  Note: make true the order of textList and intentList are mapped 
     * @param context 
     * @param textView 
     * @param text 
     * @param intent 
     */  
    public static void setClickableTextView(Context context, TextView textView,  
            String text, Intent intent) {  
//        SpannableStringBuilder builder = new SpannableStringBuilder(text);  
//        builder.setSpan(getClickableSpan(context, intent), 0, text.length(),   
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  
//        textView.setText(builder);  
    	com.czzz.utils.TextUtils.linkifyUsers(textView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());  
    } 
    
    /** 
     * return a custom ClickableSpan 
     *  
     * @param context 
     * @param intent 
     * @return 
     */  
    public static MyClickableSpan getClickableSpan(Context context,  
            Intent intent) {  
        return new MyClickableSpan(context, intent);  
    }  

}
