package com.example.photoutils;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class EditPhotoActivity  extends Activity implements OnClickListener {
	
	public static final String KEY_ACTION_DISPLAY = "keyActionDisplay";
	private Uri imageUri;
	private ImageView editImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editphotos);
		
		findViewById(R.id.sendPhotoBtn).setOnClickListener(this);
		Bundle extras = getIntent().getExtras();
		imageUri = (Uri)extras.get(KEY_ACTION_DISPLAY);
		editImage = (ImageView) findViewById(R.id.imageView_editImage);
		if(null != imageUri) {
			Bitmap bitmap =decodeUriAsBitmap(imageUri, this);
			editImage.setImageBitmap(bitmap);
			saveMyBitmap(imageUri.getPath(), getimage(imageUri.getPath(), 0f, 0f));
		}
		
	}
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.sendPhotoBtn:
			Log.i("result", ""+sharePicture(imageUri.getPath()));
			break;

		default:
			break;
		}
	}
	

	public Object sharePicture(String pictureAddr) {
		InputStream inputStream = null;
		BufferedInputStream bis = null;
		String result = null;
		try {
			AndroidHttpClient httpClient = AndroidHttpClient.newInstance("httpPost");
			
			HttpPost post = new HttpPost(getRequestRootUrlWithPlat().toString());
			File file = new File(pictureAddr);
			String fileName = file.getName();
			HttpEntity fileEntity = new FileEntity(file, null);
			post.setEntity(fileEntity);
			httpClient.execute(post);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(inputStream);
			close(bis);
		}
		return result;
	}
	protected StringBuilder getRequestRootUrlWithPlat() {
		StringBuilder sb = new StringBuilder();
		String str = getRootUrl();
		sb.append(str);
		sb.append("shareimage?plat");
		sb.append("=");
		sb.append("android");
		return sb;
	}
	protected String rootUrl;
	
	public static void close(Closeable closeable) {
		if(null!=closeable) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	protected String getRootUrl() {
		{
			StringBuilder sb = new StringBuilder();
			sb.append("http://");
			sb.append("www。baidu.com");
			sb.append(":");
			sb.append("8888");
			sb.append("/");
			rootUrl = sb.toString();
		}

		return rootUrl;
	}


	/**
	 * 获取BitMap
	 *
	 * @author: qubian
	 */
	public static Bitmap decodeUriAsBitmap(Uri uri, Context context) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return bitmap;
	}
	/**
	 * 图片处理
	 * @author: qubian
	 * @param srcPath
	 * @return
	 */
	public static Bitmap getimage(String srcPath,float hight,float weight) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空
		
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh;
		if (hight==0) {
			hh= 640f;//这里设置高度为800f
		}
		else 
		{
			hh=hight;
		}
		float ww;
		if (weight==0) {
			ww=640f;//这里设置宽度为480f
		}
		else
		{
			ww=weight;
		}
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
	}
	
	/**
	 * 图片压缩
	 * @author: qubian
	 * @param image
	 * @return
	 */
	private static  Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while ( baos.toByteArray().length / 1024>100) {	//循环判断如果压缩后图片是否大于100kb,大于继续压缩		
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;//每次都减少10
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
		return bitmap;
	}
	
	/**
	 * 保存图片
	 * @param bitNameUrl
	 * @param mBitmap
	 */
	public static  void saveMyBitmap(String bitNameUrl,Bitmap mBitmap){
		File f = new File(bitNameUrl);
		try {
			f.createNewFile();
		} catch (IOException e) {
			Log.i("saveMyBitmap", "在保存图片时出错：" + e.toString());
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
