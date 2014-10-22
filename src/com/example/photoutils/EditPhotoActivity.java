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
			sb.append("www��baidu.com");
			sb.append(":");
			sb.append("8888");
			sb.append("/");
			rootUrl = sb.toString();
		}

		return rootUrl;
	}


	/**
	 * ��ȡBitMap
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
	 * ͼƬ����
	 * @author: qubian
	 * @param srcPath
	 * @return
	 */
	public static Bitmap getimage(String srcPath,float hight,float weight) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//��ʼ����ͼƬ����ʱ��options.inJustDecodeBounds ���true��
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//��ʱ����bmΪ��
		
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//���������ֻ��Ƚ϶���800*480�ֱ��ʣ����ԸߺͿ���������Ϊ
		float hh;
		if (hight==0) {
			hh= 640f;//�������ø߶�Ϊ800f
		}
		else 
		{
			hh=hight;
		}
		float ww;
		if (weight==0) {
			ww=640f;//�������ÿ��Ϊ480f
		}
		else
		{
			ww=weight;
		}
		//���űȡ������ǹ̶��������ţ�ֻ�ø߻��߿�����һ�����ݽ��м��㼴��
		int be = 1;//be=1��ʾ������
		if (w > h && w > ww) {//�����ȴ�Ļ����ݿ�ȹ̶���С����
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//����߶ȸߵĻ����ݿ�ȹ̶���С����
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//�������ű���
		//���¶���ͼƬ��ע���ʱ�Ѿ���options.inJustDecodeBounds ���false��
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap);//ѹ���ñ�����С���ٽ�������ѹ��
	}
	
	/**
	 * ͼƬѹ��
	 * @author: qubian
	 * @param image
	 * @return
	 */
	private static  Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//����ѹ������������100��ʾ��ѹ������ѹ��������ݴ�ŵ�baos��
		int options = 100;
		while ( baos.toByteArray().length / 1024>100) {	//ѭ���ж����ѹ����ͼƬ�Ƿ����100kb,���ڼ���ѹ��		
			baos.reset();//����baos�����baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//����ѹ��options%����ѹ��������ݴ�ŵ�baos��
			options -= 10;//ÿ�ζ�����10
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//��ѹ���������baos��ŵ�ByteArrayInputStream��
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//��ByteArrayInputStream��������ͼƬ
		return bitmap;
	}
	
	/**
	 * ����ͼƬ
	 * @param bitNameUrl
	 * @param mBitmap
	 */
	public static  void saveMyBitmap(String bitNameUrl,Bitmap mBitmap){
		File f = new File(bitNameUrl);
		try {
			f.createNewFile();
		} catch (IOException e) {
			Log.i("saveMyBitmap", "�ڱ���ͼƬʱ����" + e.toString());
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
