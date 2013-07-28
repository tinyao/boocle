package com.czzz.utils;

import com.czzz.demo.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager conn = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conn.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			//...
		} else {
			Toast.makeText(context, "网络连接断开", Toast.LENGTH_SHORT).show();
		}

		// switch(networkInfo.getType()){
		// case ConnectivityManager.TYPE_WIFI:
		// networkInfo.get
		// break;
		//
		// }
	}

}
