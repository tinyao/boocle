package com.czzz.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Environment;

public class FileDownLoader extends AsyncTask<Object, Object, Object>{

	@Override
	protected Object doInBackground(Object... arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	private void download(String urlstr){
//		try {  
//            //服务器上新版apk地址  
//            URL url = new URL(urlstr);  
//            HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
//            conn.connect();  
//            int length = conn.getContentLength();  
//            InputStream is = conn.getInputStream();  
//            File file = new File(Environment.getExternalStorageDirectory()
//            		.getAbsolutePath() + "/download/");  
//            if(!file.exists()){  
//                //如果文件夹不存在,则创建  
//                file.mkdir();  
//            }  
//            //下载服务器中新版本软件（写文件）  
//            String apkFile = Environment.getExternalStorageDirectory()
//            		.getAbsolutePath() + "/download/bookcircle.apk";  
//            File ApkFile = new File(apkFile);  
//            FileOutputStream fos = new FileOutputStream(ApkFile);  
//            int count = 0;  
//            byte buf[] = new byte[1024];  
////            do{  
//                int numRead = is.read(buf);  
//                count += numRead;  
//                //更新进度条  
//                progress = (int) (((float) count / length) * 100); 
//                mHandler.sendEmptyMessage(1);  
//                if(numRead <= 0){  
//                    //下载完成通知安装  
//                	mHandler.sendEmptyMessage(0);  
//                }  
//                fos.write(buf,0,numRead);  
//                //当点击取消时，则停止下载  
////            }while(!isInterceptDownload);  
//        } catch (MalformedURLException e) {  
//            e.printStackTrace();  
//        } catch (IOException e) {  
//            e.printStackTrace();  
//        }  
//	}

}
