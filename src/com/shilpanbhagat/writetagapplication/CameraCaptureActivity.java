package com.shilpanbhagat.writetagapplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images.Media;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;

@SuppressLint("NewApi")
public class CameraCaptureActivity extends Activity implements Camera.PictureCallback, OnClickListener {

	private Camera mCamera;
    private CameraPreview mPreview;
    
    //Set buttons for the activity
    Button captureButton;
    Button read;
    Button home;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_capture);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(this);
		
		read = (Button) findViewById(R.id.read);
		read.setOnClickListener(this);
		
		home = (Button) findViewById(R.id.home);
		home.setOnClickListener(this);
		
		if(checkCameraHardware(this)) {
			// Create an instance of Camera
	        mCamera = getCameraInstance();
	
	        // Create our Preview view and set it as the content of our activity.
	        mPreview = new CameraPreview(this, mCamera);
	        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
	        preview.addView(mPreview);
	        
	        //mPreview.setFocusable(true);
	        //mPreview.setFocusableInTouchMode(true);
	        //mPreview.setClickable(true);
	        //mPreview.setOnClickListener(this);
		}
		else {
			Log.v(SENSOR_SERVICE, "Your phone does not have a camera");
			//Open the activity for forms
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_write_tag, menu);
		return true;
	}
	
	/** Check if this device has a camera **/
	private boolean checkCameraHardware(Context context) {
		if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			//this device has a camera
			return true;
		}
		else {
			//no camera on this device
			return false;
		}
	}
	
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		Uri imageFileUri = getOutputMediaFileUri(); 
		System.out.println(imageFileUri);
		
		// Load up the image's dimensions not the image itself
		/*BitmapFactoryOptions bfo
		Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		System.out.println(bmp.getHeight());
		
		//Create the Bitmap of required size
		//Facing a memory out problem here
		Bitmap alteredBitmap = Bitmap.createBitmap(bmp,(bmp.getWidth() - bmp.getHeight())/2, 0, bmp.getHeight()-((bmp.getWidth() - bmp.getHeight())/2), bmp.getHeight());
		try
		{
			OutputStream imageFileOS = getContentResolver().openOutputStream(imageFileUri);;
		    alteredBitmap.compress(Bitmap.CompressFormat.JPEG, 300, imageFileOS);
		    imageFileOS.flush();
		    imageFileOS.close();
		    imageFileOS = null;
		}
		catch (IOException e)
		{
		    e.printStackTrace();
		}*/
		
		try
		{
			OutputStream imageFileOS = getContentResolver().openOutputStream(imageFileUri);;
		    //alteredBitmap.compress(Bitmap.CompressFormat.JPEG, 300, imageFileOS);
			imageFileOS.write(data);
		    imageFileOS.flush();
		    imageFileOS.close();
		    imageFileOS = null;
		}
		catch (IOException e)
		{
		    e.printStackTrace();
		}
		camera.startPreview();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == captureButton) {
			Log.v("Capture Button", "saving image!");
			mCamera.takePicture(null, null, null, this);
		}
		else if(v == read) {
			
		}
		else if(v == home) {
			
		}
	}
	
	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(){
	      return Uri.fromFile(getOutputMediaFile());
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "styleTapp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (!mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    
	    mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
	    return mediaFile;
	}
}
