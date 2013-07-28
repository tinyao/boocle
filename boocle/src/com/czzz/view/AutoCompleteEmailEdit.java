package com.czzz.view;

import java.util.ArrayList;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class AutoCompleteEmailEdit extends AutoCompleteTextView {
	private Context mContext = null;

	private static final String[] emailSuffix = { "@gmail.com", "@qq.com", 
			"@163.com", "@126.com", "@sina.com", "@hotmail.com",
			"@yahoo.com.cn", "@sohu.com", "@foxmail.com", "@yeah.net", "@std.xidian.edu.cn"};

	public AutoCompleteEmailEdit(Context context) {
		this(context, null);
	}

	public AutoCompleteEmailEdit(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	private void init() {

		addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				clearListSelection();
				String input = s.toString();
				if (!isPopupShowing()) {
					showDropDown();
				}
				if(input.contains("@")){
					createCandidateEmail(input);
				}else{
					dismissDropDown();
				}

			}
		});
		setThreshold(1);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	private void createCandidateEmail(String name) {
		ArrayList<String> candidateString = new ArrayList<String>();
		int index = name.indexOf("@");
		if (index > 0) {
			String suffix = name.substring(index);
			for (String string : emailSuffix) {
				if (string.startsWith(suffix)) {
					candidateString.add(name.substring(0, index) + string);
				}
			}
		} else {
			for (int i = 0; i < emailSuffix.length; ++i) {
				candidateString.add(name + emailSuffix[i]);
			}
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, candidateString);
		setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

}