package com.czzz.demo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CustomAsyncHttpResponseHandler extends AsyncHttpResponseHandler{

	Context context;
	int taskId;
	
	boolean isActivity = false;;
	
	public CustomAsyncHttpResponseHandler(Context con, int taskId){
		
		if(con instanceof Activity){
			isActivity = true;
		}
		
		this.context = con;
		this.taskId = taskId;
	}
	
	@Override
	public void onFailure(Throwable arg0, String arg1) {
		// TODO Auto-generated method stub
		super.onFailure(arg0, arg1);
		
		if(isActivity) {
			if(arg1.equals("can't resolve host")){
				Crouton.makeText((Activity) context, "呃，网络连接好像有问题", Style.ALERT).show();
			}else if(arg1.equals("can't resolve host")){
				Crouton.makeText((Activity) context, "呃，网络连接好像有问题", Style.ALERT).show();
			}else if(arg1.equals("socket time out")){
				Crouton.makeText((Activity) context, "网络连接超时了", Style.ALERT).show();
			}
		} else{
			if(arg1.equals("can't resolve host")){
				Toast.makeText(context, "呃，网络连接好像有问题", Toast.LENGTH_SHORT).show();
			}else if(arg1.equals("can't resolve host")){
				Toast.makeText(context, "呃，网络连接好像有问题", Toast.LENGTH_SHORT).show();
			}else if(arg1.equals("socket time out")){
				Toast.makeText(context, "网络连接超时了", Toast.LENGTH_SHORT).show();
			}
		}
		
		Log.d("DEBUG", "Throwable: " + arg0.getMessage() + "\narg1: " + arg1 + "\n Cause: " + arg0.getCause());
	}

	@Override
	public void onFailure(Throwable arg0) {
		// TODO Auto-generated method stub
		super.onFailure(arg0);
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		super.onFinish();
		
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onSuccess(int arg0, String arg1) {
		// TODO Auto-generated method stub
		super.onSuccess(arg0, arg1);
	}
	
	public void onSuccess(int arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSuccess(String arg0) {
		// TODO Auto-generated method stub
		super.onSuccess(arg0);
	}

}
