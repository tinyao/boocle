package com.czzz.bookcircle;

import java.io.File;

import com.czzz.base.User;
import com.czzz.bookcircle.task.AlarmTask;
import com.czzz.data.MsgHelper;
import com.czzz.data.MsgThreadHelper;
import com.czzz.demo.ProfileActivity;
import com.czzz.utils.ImagesDownloader;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class MyApplication extends Application{
	
	private static Context sContext;
	private static MyApplication instance;
	public static SharedPreferences accoutPref, configPref;
	public static boolean displayCover;
	public static boolean highCover, hideTabs;
	public static ImagesDownloader imagesLoader;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		sContext = getApplicationContext();
		accoutPref = this.getSharedPreferences("account", 0);
		configPref = this.getSharedPreferences("config", 0);
		displayCover = configPref.getBoolean("display_search_cover", true);
		highCover = configPref.getBoolean("setting_high_cover", false);
		hideTabs = configPref.getBoolean("hide_tabs", false);
		imagesLoader = ImagesDownloader.getInstance();
		if(User.getInstance().uid == 0){
			User.getInstance().init(this);
		}
	}
	
	public static Context getContext() {
        return sContext;
    }

	public static MyApplication getInstance() {
		return instance;
	}
	
	/**
	 * clear all a
	 */
	public void clearApplicationData() {
        File cache = this.getCacheDir();
        File appDir = new File(cache.getParent());
        
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                }
            }
        }
        
        AlarmTask.cancelMsgAlarm(this);
        User.clearUser();
        MsgHelper.clear();
        MsgThreadHelper.clear();
    }
	
	public static boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		return dir.delete();
	}
	
	private static boolean isChatVis = false;
	
	public static boolean isChatActivityVisible(){
		return isChatVis;
	}
	
	public static void chatActivityResumed(){
		isChatVis = true;
	}
	
	public static void chatActivityPaused(){
		isChatVis = false;	
	}
	
	private static boolean isMsgThreadVis = false;
	private static boolean isMsgThreadDestroy = false;
	
	public static boolean isMsgFragmentVisible(){
		return isMsgThreadVis;
	}
	
	public static void MsgFragmentResumed(){
		isMsgThreadVis = true;
	}
	
	public static void MsgFragmentPaused(){
		isMsgThreadVis = false;	
	}
	
	public static void MsgFragmentDestroy(){
		isMsgThreadVis = false;	
	}
	
	
	
}
