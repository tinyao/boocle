package com.czzz.bookcircle.task;

public interface TaskListener {

	public void onTaskCompleted(Object data);
	
	public void onTaskFailed(String data);
	
}
