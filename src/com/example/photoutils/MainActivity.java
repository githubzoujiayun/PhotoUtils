package com.example.photoutils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener{

	public static final String TAG="MainActivity";
	public static final String JPEG_FILE_PREFIX = "QUBIAN";
	public static final String JPEG_FILE_SUFFIX = ".jpg";
	public static final String CAMERA_DIR = "/dcim/";
	public static final int CAPTURE_CAMERA_RESULT = 100;
	public static final int IMAGE_CUT_RESULT = 101;
	
	public static final String INTENT_CROP = "com.android.camera.action.CROP";
	/** 裁剪最大宽度 */
	public static final int MAX_WIDTH = 400;
	/** 裁剪最大高度 */
	public static final int MAX_HEIGHT = 400;
	private static boolean isFromPhone=false;
	private Uri imageUri;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.takePhotoBtn).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(null==data)
			return;
		Log.i(TAG, "onActivityResult");
		if (Activity.RESULT_OK == resultCode) {
			switch (requestCode) {
			case CAPTURE_CAMERA_RESULT:
				Log.i(TAG, "CAPTURE_CAMERA");
				Intent intent = getImageClipIntent(INTENT_CROP, imageUri, MAX_WIDTH, MAX_HEIGHT);
				startActivityForResult(intent, IMAGE_CUT_RESULT);
				break;
			case IMAGE_CUT_RESULT:
				Log.i(TAG, "IMAGE_CUT");
				Intent edIntent = new Intent(this, EditPhotoActivity.class);
				edIntent.putExtra(EditPhotoActivity.KEY_ACTION_DISPLAY, imageUri);
				startActivity(edIntent);
				break;
			}
		}
	}
	
	public static Intent getImageClipIntent(String action, Uri uri, int width, int height) {

		Intent intent = new Intent(action);
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", width);
		intent.putExtra("outputY", height);
		intent.putExtra("scale", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra("return-data", false);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		intent.putExtra("noFaceDetection", true);
		return intent;
	}
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.takePhotoBtn:
			captrueCamera();
			break;

		default:
			break;
		}
		
	}
	/**
	 * 获取捕获Camera
	 * 
	 * @author: qubian
	 * 
	 */
	public void captrueCamera() {
		try {
			Log.i(TAG, "captrueCamera");
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			imageUri = Uri.fromFile(createImageFile(this));
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			Log.i(TAG, imageUri.getPath());
			startActivityForResult(intent, CAPTURE_CAMERA_RESULT);
			isFromPhone=true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onResume() {
		Log.i(TAG, "onResume");
		if(isFromPhone){
			File phono=new File(imageUri.getPath());
			if(phono.exists())
			{
				Intent intent = getImageClipIntent(INTENT_CROP, imageUri, MAX_WIDTH, MAX_HEIGHT);
				startActivityForResult(intent, IMAGE_CUT_RESULT);
			}
			isFromPhone=false;
		}
		super.onResume();
	}
	/**
	 * 创建图片文件
	 * 
	 * @author: qubian
	 * 
	 */
	@SuppressLint("SimpleDateFormat")
	public static File createImageFile(Context context) throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File image = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, getAlbumDir(context));
		return image;
	}
	/**
	 * 获取存储路径
	 * 
	 * @author: qubian
	 */
	protected static File getAlbumDir(Context context) {
		File storageDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			storageDir = getAlbumStorageDir(getAlbumName(context));
			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						return null;
					}
				}
			}
		} 
		return storageDir;
	}
	
	/**
	 * 获取存储名称
	 * 
	 * @author: qubian
	 */
	protected static String getAlbumName(Context ctx) {
		return ctx.getString(R.string.image_save_name);
	}
	
	/**
	 * 获取临时路径
	 * 
	 * @author: qubian
	 */
	protected static File getAlbumStorageDir(String albumName) {
		return new File(Environment.getExternalStorageDirectory() + CAMERA_DIR + albumName);
	}
}
