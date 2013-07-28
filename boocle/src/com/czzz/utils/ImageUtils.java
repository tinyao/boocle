package com.czzz.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

public class ImageUtils {

	public static final String APP_DIR = "bookcircle";
	public static File SDFile = android.os.Environment.getExternalStorageDirectory();
	
	public static String avatarPath = SDFile + "/" + APP_DIR + "/avatar.jpg";
	
	public static Bitmap getResizedBitmap(Context context, Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        Log.i("view" , "height" + displayMetrics.heightPixels); 
        Log.i("view" , "width" + displayMetrics.widthPixels);
        
        float newWidth = (float) (displayMetrics.widthPixels / 3.0f);
        // float newHeight = newWidth / width * height;
        
        float scaleWidth = ((float) newWidth) / width;
        
        // float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleWidth);


        // RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
	
	public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        
//        float newHeight = newWidth / width * height;
        
        float scaleWidth = ((float) (newWidth)) / width;
        float scaleHeight = ((float) (newHeight)) / height;
        
        // float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        
        // RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = radius;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
	
	public static void showPickDialog(final Activity activity) {
		// TODO Auto-generated method stub
		
		File f = new File(SDFile, APP_DIR);
		if(!f.exists()){
			f.mkdir();
		}
		
		new AlertDialog.Builder(activity)
				.setTitle("设置头像...")
				.setNegativeButton("相册", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_PICK, null);

						intent.setDataAndType(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								"image/*");
						dialog.dismiss();
						activity.startActivityForResult(intent, 1);
					}
				})
				.setPositiveButton("拍照", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						// 下面这句指定调用相机拍照后的照片存储的路径
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
								.fromFile(new File(Environment
										.getExternalStorageDirectory(),
										APP_DIR + "/camera.jpg")));
						activity.startActivityForResult(intent, 2);
					}
				}).show();
	}
	
	private boolean isSDCARDMounted() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED))
			return true;
		return false;
	}
	
	/**
	 * resize bitmap and save into file
	 * @return Bitmap resized bitmap
	 */
	public static Bitmap resizeAvatarFile(){
		Bitmap bm = BitmapFactory.decodeFile(SDFile + "/" + APP_DIR + "/avatar.jpg", null);
		Bitmap out = getResizedBitmap(bm, 140, 140);
		FileOutputStream outs;
		try {
			outs = new FileOutputStream(SDFile + "/" + APP_DIR + "/avatar.jpg");
			out.compress(Bitmap.CompressFormat.JPEG, 100, outs);
			bm.recycle();
//			out.recycle();
			return out;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static void saveBitmap(Bitmap bm, String filepath){
		FileOutputStream outs;
		try {
			outs = new FileOutputStream(filepath);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, outs);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
