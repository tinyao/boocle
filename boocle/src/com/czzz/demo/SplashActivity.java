package com.czzz.demo;

import com.czzz.base.User;
import com.czzz.demo.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {    
	  
    private final int SPLASH_DISPLAY_LENGHT = 1000; //延迟三秒  
  
    SharedPreferences sp;
    
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        
//        BroadcastReceiver receiver = new NetworkReceiver();
//        
//        IntentFilter filter = new IntentFilter(); 
//        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(receiver, filter);
        
        setContentView(R.layout.splash); 
        new Handler().postDelayed(new Runnable(){ 
  
         @Override 
         public void run() { 
        	 
        	sp = SplashActivity.this.getSharedPreferences("account", 0);
     		if(sp.contains("name")) {
     			// 已经登录，从pref里初始化用户对象
     			User.getInstance().init(SplashActivity.this);
     			Intent mainIntent = new Intent(SplashActivity.this, HomeActivity.class); 
                SplashActivity.this.startActivity(mainIntent); 
                SplashActivity.this.finish(); 
     		}else{
     			Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class); 
                SplashActivity.this.startActivity(mainIntent); 
                SplashActivity.this.finish(); 
     		}
         } 
            
        }, SPLASH_DISPLAY_LENGHT); 
    } 
    
    @Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
		overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
	}
    
}